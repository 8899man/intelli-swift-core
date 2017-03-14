package com.fr.bi.cal.analyze.session;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.cal.analyze.cal.result.ComplexAllExpander;
import com.fr.bi.cal.analyze.cal.sssecret.PageIteratorGroup;
import com.fr.bi.cal.analyze.executor.detail.key.DetailSortKey;
import com.fr.bi.cal.analyze.report.report.widget.BIDetailWidget;
import com.fr.bi.cal.analyze.report.report.widget.TableWidget;
import com.fr.bi.cal.report.main.impl.BIWorkBook;
import com.fr.bi.cal.stable.engine.TempCubeTask;
import com.fr.bi.cal.stable.loader.CubeReadingTableIndexLoader;
import com.fr.bi.cal.stable.loader.CubeTempModelReadingTableIndexLoader;
import com.fr.bi.cluster.utils.ClusterEnv;
import com.fr.bi.conf.report.BIReport;
import com.fr.bi.conf.report.BIWidget;
import com.fr.bi.fs.BIFineDBConfigLockDAO;
import com.fr.bi.fs.BIReportNode;
import com.fr.bi.fs.BIReportNodeLock;
import com.fr.bi.fs.BIReportNodeLockDAO;
import com.fr.bi.stable.constant.BIExcutorConstant;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.log.CubeGenerateStatusProvider;
import com.fr.data.TableDataSource;
import com.fr.fs.base.entity.CompanyRole;
import com.fr.fs.base.entity.CustomRole;
import com.fr.fs.control.CompanyRoleControl;
import com.fr.fs.control.CustomRoleControl;
import com.fr.fs.control.UserControl;
import com.fr.fs.web.service.ServiceUtils;
import com.fr.general.FRLogManager;
import com.fr.general.GeneralContext;
import com.fr.main.FineBook;
import com.fr.main.TemplateWorkBook;
import com.fr.main.workbook.ResultWorkBook;
import com.fr.report.report.ResultReport;
import com.fr.report.stable.fun.Actor;
import com.fr.script.Calculator;
import com.fr.stable.bridge.StableFactory;
import com.fr.stable.script.CalculatorProvider;
import com.fr.web.core.SessionDealWith;
import com.fr.web.core.SessionIDInfor;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by GUY on 2015/4/8.
 */
public class BISession extends BIAbstractSession {

    private static final long serialVersionUID = 7928902437685692328L;
    private static final long EDIT_TIME = 45000;
    private boolean isEdit;
    private boolean isRealTime;
    private BIReportNode node;
    //pony 缓存loader
    private ICubeDataLoader loader;
    private CubeGenerateStatusProvider provider;
    private String tempTableMd5;
    private String tempTableId;
    //pony 明细表缓存的分页信息
    private Map<DetailSortKey, ConcurrentHashMap<Integer, Integer>> detailIndexMap = new ConcurrentHashMap<DetailSortKey, ConcurrentHashMap<Integer, Integer>>();
    private Map<DetailSortKey, ConcurrentHashMap<Integer, Object[]>> detailValueMap = new ConcurrentHashMap<DetailSortKey, ConcurrentHashMap<Integer, Object[]>>();

    //统计组建的分页信息
    private Map<String, ConcurrentHashMap<Object, PageIteratorGroup>> pageGroup = new ConcurrentHashMap<String, ConcurrentHashMap<Object, PageIteratorGroup>>();
    private Map<String, ConcurrentHashMap<Object, PageIteratorGroup>> partPageGroup = new ConcurrentHashMap<String, ConcurrentHashMap<Object, PageIteratorGroup>>();

    //young 当前用户（普通）的角色信息
    private List<CustomRole> customRoles = new ArrayList<CustomRole>();
    private List<CompanyRole> companyRoles = new ArrayList<CompanyRole>();


    public BISession(String remoteAddress, BIWeblet let, long userId) {
        super(remoteAddress, let, userId);
    }

    private BISession(String remoteAddress, BIWeblet let, long userId, BIReportNode node) {
        super(remoteAddress, let, userId);
        this.node = node;
        updateTime();
    }

    public BISession(String remoteAddress, BIWeblet let, long userId, Locale locale, BIReportNode node) {
        this(remoteAddress, let, userId, node);
        // TODO:richie在这里改变国际化的环境
        GeneralContext.setLanguage(1);
        updateTime();
        initRoles();

    }

    //TODO : 这边userid 也要把loader什么的换下，在这边实现起来不好
    public long getUserIdFromSession(HttpServletRequest req) {
        if (isShareReq) {
            return accessUserId;
        } else {
            return ServiceUtils.getCurrentUserID(req);
        }
    }

    public void removeBookByName(String widgetName) {

    }

    //通过updatesession，更新模板锁的时间
    public void updateReportNodeLockTime() {
        BIReportNodeLockDAO lockDAO = StableFactory.getMarkedObject(BIReportNodeLockDAO.class.getName(), BIReportNodeLockDAO.class);
        if (lockDAO == null) {
            return;
        }
        lockDAO.updateLock(sessionID, node.getUserId(), node.getId());
    }

    //通过updatesession，更新配置锁的时间
    public void updateConfigLockTime() {
        BIFineDBConfigLockDAO lockDAO = StableFactory.getMarkedObject(BIFineDBConfigLockDAO.class.getName(), BIFineDBConfigLockDAO.class);
        if (lockDAO == null) {
            return;
        }
        lockDAO.updateLock(sessionID, node.getUserId());
    }

    /**
     * 强奸
     */
    public void forceEdit() {
        BIReportNodeLockDAO lockDAO = StableFactory.getMarkedObject(BIReportNodeLockDAO.class.getName(), BIReportNodeLockDAO.class);
        if (lockDAO == null) {
            this.isEdit = true;
            return;
        }
        lockDAO.forceLock(sessionID, node.getUserId(), node.getId());
        this.isEdit = true;
    }


    private static final long TIME_OUT = 45000;

    /**
     * 半推半就
     *
     * @param isEdit
     * @return
     */
    public boolean setEdit(boolean isEdit) {
        BIReportNodeLockDAO lockDAO = StableFactory.getMarkedObject(BIReportNodeLockDAO.class.getName(), BIReportNodeLockDAO.class);
        if (lockDAO == null) {
            this.isEdit = isEdit;
            return isEdit;
        }
        if (isEdit) {
            isEdit = lockDAO.lock(sessionID, node.getUserId(), node.getId());
            if (!isEdit) {
                List<BIReportNodeLock> locks = lockDAO.getLock(node.getUserId(), node.getId());
                boolean doForce = true;
                for (BIReportNodeLock l : locks) {
                    long t = 0;
                    if (ClusterEnv.isCluster()) {
                        t = l.getLockedTime();
                    } else {
                        //                    集群模式下，无法获得远程机器的session，将时间和锁绑定
                        SessionIDInfor ss = SessionDealWith.getSessionIDInfor(l.getSessionId());
                        t = ((BISession) ss).lastTime;
                    }
                    //45- 30 超过15-45秒还没反應可能是没有心跳
                    if (System.currentTimeMillis() - t < EDIT_TIME) {
                        doForce = false;
                        break;
                    }


                }
                if (doForce) {
                    forceEdit();
                    return this.isEdit;
                }
            }
        } else {
            releaseLock();
        }
        this.isEdit = isEdit;
        return isEdit;
    }

    private void releaseLock() {
        BIReportNodeLockDAO lockDAO = StableFactory.getMarkedObject(BIReportNodeLockDAO.class.getName(), BIReportNodeLockDAO.class);
        if (lockDAO == null) {
            return;
        }
        BIReportNodeLock lock = lockDAO.getLock(this.sessionID, node.getUserId(), node.getId());
        if (lock != null) {
            lockDAO.release(lock);
        }
    }

    private void initRoles() {
        try {
            if (this.getUserId() != UserControl.getInstance().getSuperManagerID()) {
                Set<CustomRole> cusRoles = CustomRoleControl.getInstance().getCustomRoleSet(this.getUserId());
                for (CustomRole role : cusRoles) {
                    customRoles.add(role);
                }
                Set<CompanyRole> comRoles = CompanyRoleControl.getInstance().getCompanyRoleSet(this.getUserId());
                for (CompanyRole role : comRoles) {
                    companyRoles.add(role);
                }
            }
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
    }

    public CubeGenerateStatusProvider getProvider() {
        return provider;
    }

    public void setProvider(CubeGenerateStatusProvider provider) {
        this.provider = provider;
    }

    public String getTempTableMd5() {
        return tempTableMd5;
    }

    public String getTempTableId() {
        return tempTableId;
    }

    public void setTempTableMd5(String tempTableMd5) {
        this.tempTableMd5 = tempTableMd5;
    }

    public void setTempTableId(String tempTableId) {
        this.tempTableId = tempTableId;
    }

    public boolean isRealTime() {
        return isRealTime;
    }

    public void setIsRealTime(boolean isRealTime) {
        this.isRealTime = isRealTime;
    }

    public ResultWorkBook getExportBookByName(String name) throws CloneNotSupportedException {
        BIWidget widget = report.getWidgetByName(name);
        if (widget != null) {
            widget = (BIWidget) widget.clone();
            switch (widget.getType()) {
                case TABLE:
                case CROSS_TABLE:
                case COMPLEX_TABLE:
                    ((TableWidget) widget).setComplexExpander(new ComplexAllExpander());
                    ((TableWidget) widget).setOperator(BIReportConstant.TABLE_PAGE_OPERATOR.ALL_PAGE);
                    break;
                case DETAIL:
                    ((BIDetailWidget) widget).setPage(BIExcutorConstant.PAGINGTYPE.NONE);
                    break;
            }

            widget.setWidgetName(widget.getWidgetName() + Math.random());
            TemplateWorkBook workBook = widget.createWorkBook(this);
            return ((BIWorkBook) workBook).execute4BI(getParameterMap4Execute());
        }
        return null;
    }

    @Override
    public String getDurationPrefix() {
        return null;
    }

    @Override
    public boolean isPackAvailableForDesign(String packageName) {
        return false;
    }

    @Override
    public FineBook getContextBook() {
        return null;
    }

    @Override
    public Actor getActor() {
        return null;
    }

    @Override
    public ResultReport getReport2Show(int reportIndex) {
        return null;
    }

    @Override
    public int getReportCount() {
        return 0;
    }

    @Override
    public String getReportName(int index) {
        return null;
    }

    @Override
    public TemplateWorkBook getWorkBookDefine() {
        return null;
    }

    @Override
    public TableDataSource getTableDataSource() {
        return null;
    }

    @Override
    protected Object resolveVariable(Object var, CalculatorProvider ca) {
        return null;
    }

    @Override
    public String getWebTitle() {
        return null;
    }

    @Override
    public List<CustomRole> getCustomRoles() {
        return customRoles;
    }

    @Override
    public List<CompanyRole> getCompanyRoles() {
        return companyRoles;
    }

    @Override
    public void release() {
        super.release();
        detailIndexMap.clear();
        detailValueMap.clear();
        partPageGroup.clear();
        pageGroup.clear();
        releaseLock();
        FRLogManager.setSession(null);
        Calculator.putThreadSavedNameSpace(null);
    }

    @Override
    public ICubeDataLoader getLoader() {
        synchronized (this) {
            if (!isRealTime()) {
                return CubeReadingTableIndexLoader.getInstance(accessUserId);
            } else {
                if (loader == null) {
                    loader = CubeTempModelReadingTableIndexLoader.getInstance(new TempCubeTask(getTempTableMd5(), getTempTableId(), getUserId()));
                }
                return loader;
            }
        }
    }

    @Override
    public void updateTime() {
        this.lastTime = System.currentTimeMillis();
        if (isRealTime()) {
            CubeTempModelReadingTableIndexLoader loader = (CubeTempModelReadingTableIndexLoader) CubeTempModelReadingTableIndexLoader.getInstance(new TempCubeTask(getTempTableMd5(), getTempTableId(), getUserId()));
            loader.updateTime();
        }
    }

    public PageIteratorGroup getPageIteratorGroup(boolean useRealData, String widgetName) {
        return getPageIteratorGroup(useRealData, widgetName, 0);
    }

    @Override
    public BIReport getBIReport() {
        return report;
    }


    public PageIteratorGroup getPageIteratorGroup(boolean useRealData, String widgetName, int i) {
        Map<String, ConcurrentHashMap<Object, PageIteratorGroup>> pmap = useRealData ? pageGroup : partPageGroup;
        if (!pmap.containsKey(widgetName)) {
            throw new RuntimeException("error! page not found");
        }
        ConcurrentHashMap<Object, PageIteratorGroup> map = pmap.get(widgetName);
        return map.get(i);
    }

    public void setPageIteratorGroup(boolean useRealData, String widgetName, PageIteratorGroup pg) {
        setPageIteratorGroup(useRealData, widgetName, pg, 0);
    }

    public void setPageIteratorGroup(boolean useRealData, String widgetName, PageIteratorGroup pg, int i) {
        Map<String, ConcurrentHashMap<Object, PageIteratorGroup>> pmap = useRealData ? pageGroup : partPageGroup;
        ConcurrentHashMap<Object, PageIteratorGroup> map = pmap.get(widgetName);
        if (map == null) {
            pmap.put(widgetName, new ConcurrentHashMap<Object, PageIteratorGroup>());
            map = pmap.get(widgetName);
        }
        map.put(i, pg);
    }

    public GroupValueIndex createFilterGvi(BusinessTable key) {
        return getLoader().getTableIndex(key.getTableSource()).getAllShowIndex();
    }

    public Long getReportId() {
        return node.getId();
    }

    public int getDetailLastIndex(DetailSortKey key, int page) {
        int index = 0;
        ConcurrentHashMap<Integer, Integer> imap = detailIndexMap.get(key);
        if (imap != null && imap.containsKey(page)) {
            index = imap.get(page);
        }
        return index;
    }

    public Object[] getDetailLastValue(DetailSortKey key, int page) {
        ConcurrentHashMap<Integer, Object[]> vmap = detailValueMap.get(key);
        if (vmap != null && vmap.containsKey(page)) {
            return vmap.get(page);
        }
        return new Object[0];
    }

    public void setDetailIndexMap(DetailSortKey key, int page, int lastIndex) {
        ConcurrentHashMap<Integer, Integer> imap = detailIndexMap.get(key);
        if (imap != null) {
            imap.put(page, lastIndex);
        } else {
            imap = new ConcurrentHashMap<Integer, Integer>();
            detailIndexMap.put(key, imap);
            imap.put(page, lastIndex);
        }
    }

    public void setDetailValueMap(DetailSortKey key, int page, Object[] value) {
        ConcurrentHashMap<Integer, Object[]> vmap = detailValueMap.get(key);
        if (vmap != null) {
            vmap.put(page, value);
        } else {
            vmap = new ConcurrentHashMap<Integer, Object[]>();
            detailValueMap.put(key, vmap);
            vmap.put(page, value);
        }
    }

    /**
     * @return
     */
    public BIReportNode getReportNode() {
        return node;
    }

}