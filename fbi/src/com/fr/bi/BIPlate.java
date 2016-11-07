package com.fr.bi;


import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.conf.BICubeManagerProvider;
import com.finebi.cube.conf.BISystemPackageConfigurationProvider;
import com.finebi.cube.conf.BITableRelationConfigurationProvider;
import com.finebi.cube.utils.CubeUpdateUtils;
import com.fr.base.FRContext;
import com.fr.bi.cal.generate.TimerRunner;
import com.fr.bi.cal.report.db.DialectCreatorImpl;
import com.fr.bi.conf.VT4FBI;
import com.fr.bi.conf.base.datasource.BIConnectionManager;
import com.fr.bi.conf.provider.BIConfigureManagerCenter;
import com.fr.bi.conf.utils.BIModuleManager;
import com.fr.bi.fs.BIReportNode;
import com.fr.bi.fs.BISharedReportNode;
import com.fr.bi.fs.BITableMapper;
import com.fr.bi.fs.entry.BIReportEntry;
import com.fr.bi.fs.entry.BIReportEntryDAO;
import com.fr.bi.fs.entry.EntryConstants;
import com.fr.bi.module.BICoreModule;
import com.fr.bi.module.BIModule;
import com.fr.bi.resource.ResourceHelper;
import com.fr.bi.stable.utils.program.BIClassUtils;
import com.fr.bi.stable.utils.program.BINonValueUtils;
import com.fr.data.core.db.DBUtils;
import com.fr.data.core.db.dialect.Dialect;
import com.fr.data.core.db.dialect.DialectFactory;
import com.fr.data.core.db.tableObject.Column;
import com.fr.data.core.db.tableObject.ColumnSize;
import com.fr.data.dao.*;
import com.fr.dav.LocalEnv;
import com.fr.fs.AbstractFSPlate;
import com.fr.fs.control.EntryPoolFactory;
import com.fr.fs.control.UserControl;
import com.fr.fs.control.dao.tabledata.TableDataDAOControl.ColumnColumn;
import com.fr.fs.dao.EntryDAO;
import com.fr.fs.dao.FSDAOManager;
import com.fr.general.FRLogger;
import com.fr.general.GeneralContext;
import com.fr.general.GeneralUtils;
import com.fr.plugin.ExtraClassManager;
import com.fr.stable.*;
import com.fr.stable.bridge.StableFactory;
import com.fr.stable.fun.Service;
import com.fr.stable.plugin.PluginSimplify;
import com.fr.web.core.db.PlatformDB;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;

/**
 * BI模块启动时做的一些初始化工作，通过反射调用
 */
public class BIPlate extends AbstractFSPlate {

    @Override
    public void initData() {
        try{
            ((LocalEnv) FRContext.getCurrentEnv()).setBuildFilePath("bibuild.txt");
        } catch(Throwable e){
        }
        System.out.println("FINE BI :" + GeneralUtils.readBuildNO());
        initModules();
        super.initData();
        startModules();
        initPlugin();
        registerEntrySomething();
        initOOMKillerForLinux();
        BICubeManagerProvider markedObject = StableFactory.getMarkedObject(BICubeManagerProvider.XML_TAG, BICubeManagerProvider.class);
        loadMemoryData();
        /*载入定时任务*/
        TimerRunner timerRunner = new TimerRunner(UserControl.getInstance().getSuperManagerID());
        timerRunner.reGenerateTimeTasks();
        /*若发现cube需要更新的话,更新cube*/
        if (CubeUpdateUtils.cubeStatusCheck(UserControl.getInstance().getSuperManagerID())) {
//            if (markedObject.checkCubeStatus(UserControl.getInstance().getSuperManagerID())) {
            markedObject.generateCubes();
        }
        BIConfigureManagerCenter.getLogManager().logEnd(UserControl.getInstance().getSuperManagerID());
        addBITableColumn4NewConnection();
        addSharedTableColumn4NewConnection();

        //兼容FR工程中可能存在BID这一列的情况
        dropColumnBID();
    }

    public void loadMemoryData() {
        try {
            loadResources();
            BICubeConfigureCenter.getAliasManager().getTransManager(UserControl.getInstance().getSuperManagerID());
            BIConnectionManager.getInstance();
            BICubeConfigureCenter.getTableRelationManager().getAllTablePath(UserControl.getInstance().getSuperManagerID());
            BICubeConfigureCenter.getDataSourceManager().getAllBusinessTable();
            BIConfigureManagerCenter.getUpdateFrequencyManager().getUpdateSettings(UserControl.getInstance().getSuperManagerID());
        } catch (Exception e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }

    private void loadResources() {
        if (!StableUtils.isDebug()) {
            Locale[] locales = new Locale[]{Locale.CHINA, Locale.US};
            for (Locale locale : locales) {
                try {
                    com.fr.web.ResourceHelper.createDefaultJs(locale);
                } catch (Exception e) {
                }
            }
            try {
                com.fr.web.ResourceHelper.createDefaultCss();
            } catch (Exception e) {
            }
            for (BIModule module : BIModuleManager.getModules()) {
                module.loadResources(locales);
            }
        }
    }

    private void registerEntrySomething() {
        EntryPoolFactory.registerEntryDAO(EntryConstants.BIREPORT, BIReportEntryDAO.getInstance());
        EntryPoolFactory.registerEntry("bireport", BIReportEntry.class);
        EntryPoolFactory.registerEntryTableNames(new String[]{BIReportEntry.TABLE_NAME});
        EntryPoolFactory.registerMobileEntryTableNames(new String[]{BIReportEntry.TABLE_NAME});
    }

    private void addBITableColumn4NewConnection() {
        Connection cn = null;
        String tableName = BIReportEntry.TABLE_NAME;
        try {
            cn = PlatformDB.getDB().createConnection();
            try {
                cn.setAutoCommit(false);
            } catch (Exception e) {

            }
            Dialect dialect = DialectFactory.generateDialect(cn, PlatformDB.getDB().getDriver());
            FSDAOManager.addTableColumn(cn, dialect,
                    new Column("createBy", Types.BIGINT, new ColumnSize(10)), tableName);
            cn.commit();
        } catch (Exception e) {
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException e1) {
                    BILoggerFactory.getLogger().error(e1.getMessage(), e1);
                }
            }
        } finally {
            DBUtils.closeConnection(cn);
        }
    }

    private static void addSharedTableColumn4NewConnection() {
        Connection cn = null;
        String tableName = "FR_T_" + DAOUtils.getClassNameWithOutPath(BISharedReportNode.class);
        try {
            cn = PlatformDB.getDB().createConnection();
            try{
                cn.setAutoCommit(false);
            }catch(Exception e){

            }
            Dialect dialect = DialectFactory.generateDialect(cn,PlatformDB.getDB().getDriver());
            FSDAOManager.addTableColumn(cn, dialect,
                    new Column("createByName", Types.VARCHAR, new ColumnSize(50)), tableName);
            FSDAOManager.addTableColumn(cn, dialect,
                    new Column("shareToName", Types.VARCHAR, new ColumnSize(50)), tableName);
            cn.commit();
        } catch (Exception e) {
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException e1) {
                    FRContext.getLogger().error(e1.getMessage(), e1);
                }
            }

            FRContext.getLogger().info("Add" + tableName + "Column Action Failed!");
            FRContext.getLogger().info(e.getMessage());
        } finally {
            DBUtils.closeConnection(cn);
        }
    }

    private static void dropColumnBID() {
        Connection cn = null;
        String tableName = "FR_T_" + DAOUtils.getClassNameWithOutPath(BIReportNode.class);
        try {
            cn = PlatformDB.getDB().createConnection();
            Statement st = cn.createStatement();
            st.execute("ALTER TABLE " + tableName + " DROP BID ");
        } catch (Exception e) {
            BILoggerFactory.getLogger().info(e.getMessage());
        } finally {
            DBUtils.closeConnection(cn);
        }
    }

    private void initOOMKillerForLinux() {
        String os = System.getProperty("os.name");
        BILoggerFactory.getLogger().info("OS:" + os);
        if (os.toUpperCase().contains("LINUX")) {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            String pid = name.split("@")[0];
            try {
                String cmd = "echo -17 > /proc/" + pid + "/oom_adj";
                BILoggerFactory.getLogger().info("execute command:" + cmd);
                Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
            } catch (IOException e) {
                BILoggerFactory.getLogger().error(e.getMessage(), e);
            }

        }
    }

    private void startModules() {
        for (BIModule module : BIModuleManager.getModules()) {
            module.start();
        }
    }

    private void initModules() {
        BIModuleManager.registModule(new BICoreModule());
        Set<Class<?>> set = BIClassUtils.getClasses("com.fr.bi.module");
        set.addAll(BIClassUtils.getClasses("com.fr.bi.test.module"));
        for (Class c : set) {
            if (BIModule.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                try {
                    BIModule module = (BIModule) c.newInstance();
                    BIModuleManager.registModule(module);
                } catch (Exception e) {
                    BILoggerFactory.getLogger().error(e.getMessage(), e);
                }
            }
        }
    }

    private static void changeSystemEnv() {
        StableFactory.getMarkedObject(BISystemPackageConfigurationProvider.XML_TAG, BISystemPackageConfigurationProvider.class).envChanged();
        StableFactory.getMarkedObject(BITableRelationConfigurationProvider.XML_TAG, BITableRelationConfigurationProvider.class).envChanged();

    }

    static {
        GeneralContext.addEnvChangedListener(new EnvChangedListener() {
            @Override
            public void envChanged() {
                synchronized (BIPlate.class) {
                    changeSystemEnv();
                }
            }
        });
    }

    private void initPlugin() {
        try {
            ExtraClassManager.getInstance().addMutable(DialectCreatorImpl.XML_TAG, new DialectCreatorImpl(), PluginSimplify.create("bi", "com.fr.bi.plugin.db.ads"));
            ExtraClassManager.getInstance().addHackActionCMD("fs_load", "fs_signin", "com.fr.bi.plugin.login", "com.fr.bi.web.base.services.BISignInAction");
        } catch (Exception e) {
            FRLogger.getLogger().error(e.getMessage(), e);
        }
    }


    @Override
    public String[] getPlateStyleFiles4WebClient() {
        return (String[]) ArrayUtils.addAll(ResourceHelper.getFoundationCss(), new String[]{
                "/com/fr/bi/web/cross/css/bi.toolbar.add.css",
                "/com/fr/bi/web/cross/css/bi.shared.table.css",

                "/com/fr/bi/web/cross/css/bi.extra.dialog.css",
                "/com/fr/bi/web/cross/css/bi.edit.dialog.css",
                "/com/fr/bi/web/cross/css/bi.share.dialog.css",
                "/com/fr/bi/web/cross/css/bi.quicklist.css",
                "/com/fr/bi/web/cross/css/bi.rename.dialog.css",

                "/com/fr/bi/web/cross/css/bi.template.list.css",
                "/com/fr/bi/web/cross/css/bi.template.createdlist.css",

                "/com/fr/bi/web/cross/css/theme/bi.chartpreview.css",
                "/com/fr/bi/web/cross/css/theme/bi.stylesetting.css",
                "/com/fr/bi/web/cross/css/theme/bi.theme.css",

                "/com/fr/bi/web/cross/css/reporthangout/hangoutreport.plate.css",

                "/com/fr/bi/web/cross/css/bi.text.css",
        });
    }

    /**
     * 启动时BI加载的js文件
     *
     * @return 路径地址
     */
    @Override
    public String[] getPlateJavaScriptFiles4WebClient() {
        return (String[]) ArrayUtils.addAll(ResourceHelper.getFoundationJs(), new String[]{
                "/com/fr/bi/web/cross/js/bi.user.manager.js",
                "/com/fr/bi/web/cross/js/effect/create.by.me.js",
                "/com/fr/bi/web/cross/js/effect/share.to.me.js",
                "/com/fr/bi/web/cross/js/effect/allreports.js",
                "/com/fr/bi/web/cross/js/bi.share.js",
                "/com/fr/bi/web/cross/js/theme/bi.chartpreview.js",
                "/com/fr/bi/web/cross/js/theme/bi.stylesetting.js",
                "/com/fr/bi/web/cross/js/theme/bi.theme.js",
                "/com/fr/bi/web/cross/js/theme/bi.widget.newanalysis.js",
                "/com/fr/bi/web/cross/js/bi.toolbar.add.js",
                "/com/fr/bi/web/cross/js/bi.directory.edit.js",
                "/com/fr/bi/web/cross/js/reporthangout/hangoutreport.plate.js",
                "/com/fr/bi/web/cross/js/reporthangout/bireportdialog.js"

        });
    }

    static {
        VT4FBI.dealWithLic();
    }

    @Override
    public String[] getLocaleFile() {
        return new String[]{"com/fr/bi/stable/locale/fbi"};
    }

    static {
        ActorFactory.registerActor(ActorConstants.TYPE_BI, new BIActor());
    }


    /**
     * 注册使用的需要service
     *
     * @return 所有需要注册的服务
     */
    @Override
    public Service[] service4Register() {
        List<Service> list = new ArrayList<Service>();
        for (BIModule module : BIModuleManager.getModules()) {
            Service[] service = module.service4Register();
            if (service != null) {
                list.addAll(Arrays.asList(service));
            }
        }
        return list.toArray(new Service[list.size()]);
    }

    /**
     * 注册getRelationClass对象的表结构
     *
     * @return 所有需要注册的表结构
     */
    @Override
    public ObjectTableMapper[] mappers4Register() {
        return new ObjectTableMapper[]{
                BITableMapper.BI_REPORT_NODE.TABLE_MAPPER,
                BITableMapper.BI_SHARED_REPORT_NODE.TABLE_MAPPER,
                BITableMapper.BI_CREATED_TEMPLATE_FOLDER.TABLE_MAPPER,
                BITableMapper.BI_REPORT_NODE_LOCK.TABLE_MAPPER,
                BIReportEntry.TABLE_MAPPER
        };
    }

    @Override
    public Class<?> getRelationClass() {
        return null;
    }

    /**
     * 注册Company角色表权限关系映射到对象到company表的内容中
     *
     * @return 模块和公司角色对应的权限关系
     */
    @Override
    public FieldColumnMapper[] columnMappers4Company() {
        return new FieldColumnMapper[]{};
    }

    /**
     * 注册Custom角色权限表关系映射到对象到custom表的内容中
     *
     * @return 模块和自定义角色对应的权限关系
     */
    @Override
    public FieldColumnMapper[] columnMappers4Custom() {
        return new FieldColumnMapper[]{};
    }


    @Override
    public RelationFCMapper getRelationFCMapper4Custom() {
        return new MToMRelationFCMapper("bisPrivileges", getRelationClass());
    }

    @Override
    public RelationFCMapper getRelationFCMapper4Company() {
        return new MToMRelationFCMapper("bisPrivileges", getRelationClass());
    }

    /**
     * 创建id为 id的plate getRelationClass所对应的对象
     *
     * @param id 对象的ID
     * @return 创建的对象(dao)
     */
    @Override
    public Object createPrivilegeObject(long id) {
        return null;
    }

    @Override
    public List<String> getAllPrivilegesID() {
        return null;
    }

    @Override
    public ColumnColumn[] getTableDataColumns() {
        return new ColumnColumn[0];
    }


    /**
     * 释放一些东东
     */
    @Override
    public void release() {

    }

    /**
     * 目前主要做兼容的一些东西
     */
    @Override
    public void refreshManager() {

    }

    /**
     * 注册文件是否支持此模块
     *
     * @return 产品是否支持此模块
     */
    @Override
    public boolean isSupport() {
        return true;
    }

    /**
     * 是否需要权限控制
     *
     * @return 模块是否需要权限支持
     */
    @Override
    public boolean needPrivilege() {
        return false;
    }

    @Override
    public List<EntryDAO> getEntryDaoAccess() {
        List<EntryDAO> daoList = new ArrayList<EntryDAO>();
        daoList.add(BIReportEntryDAO.getInstance());
        return daoList;
    }

}
