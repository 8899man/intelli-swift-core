package com.fr.bi.conf.utils;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.api.ICubeTableService;
import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.BIAliasManagerProvider;
import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.conf.BIDataSourceManagerProvider;
import com.finebi.cube.conf.BISystemPackageConfigurationProvider;
import com.finebi.cube.conf.field.BusinessField;
import com.finebi.cube.conf.pack.data.BIPackageID;
import com.finebi.cube.conf.pack.data.IBusinessPackageGetterService;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.base.BIUser;
import com.fr.bi.exception.BIKeyAbsentException;
import com.fr.bi.module.BIModule;
import com.fr.bi.stable.data.BIFieldID;
import com.fr.bi.stable.data.BITableID;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.utils.program.BINonValueUtils;
import com.fr.fs.control.UserControl;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;

import java.util.*;

/**
 * Created by 小灰灰 on 2015/12/16.
 */
public class BIModuleUtils {

    public static JSONObject createPackJSON(long userId, Locale locale) throws Exception {
        JSONObject jo = new JSONObject();
        for (BIModule module : BIModuleManager.getModules()) {
            BISystemPackageConfigurationProvider provider = module.getBusiPackManagerProvider();
            if (provider == null) {
                continue;
            }
            jo.join(provider.createPackageJSON(userId, locale));
        }
        return jo;
    }

    /**
     * 分析用pack
     *
     * @param userId
     * @param locale
     * @return
     * @throws Exception
     */
    public static JSONObject createAnalysisPackJSON(long userId, Locale locale) throws Exception {
        JSONObject jo = new JSONObject();
        for (BIModule module : BIModuleManager.getModules()) {
            BISystemPackageConfigurationProvider provider = module.getBusiPackManagerProvider();
            if (provider == null) {
                continue;
            }
            jo.join(provider.createAnalysisPackageJSON(userId, locale));
        }
        return jo;
    }

    public static ICubeTableService getTableIndex(CubeTableSource tableSource, Map<String, ICubeDataLoader> childLoaderMap) {
        for (BIModule module : BIModuleManager.getModules()) {
            BIDataSourceManagerProvider provider = module.getDataSourceManagerProvider();
            if (provider == null) {
                continue;
            }
            if (provider.isRecord(tableSource)) {
                return childLoaderMap.get(module.getModuleName()).getTableIndex(tableSource);
            }
        }
        BINonValueUtils.beyondControl();
        return null;
    }

    public static Set<IBusinessPackageGetterService> getAllPacks(long userId) {
        Set<IBusinessPackageGetterService> set = new HashSet<IBusinessPackageGetterService>();
        for (BIModule module : BIModuleManager.getModules()) {
            BISystemPackageConfigurationProvider provider = module.getBusiPackManagerProvider();
            if (provider == null) {
                continue;
            }
            set.addAll(provider.getAllPackages(userId));
        }
        return set;
    }

    public static CubeTableSource getSourceByID(BITableID id, BIUser user) {
        CubeTableSource source = null;
        for (BIModule module : BIModuleManager.getModules()) {
            BIDataSourceManagerProvider provider = module.getDataSourceManagerProvider();
            if (provider == null) {
                continue;
            }
            try {
                BusinessTable table = provider.getBusinessTable(id);
                if (table != null) {
                    source = table.getTableSource();
                }
            } catch (BIKeyAbsentException e) {
            }
            if (source != null) {
                return source;
            }
        }
        if (source == null) {
            BINonValueUtils.beyondControl();
        }
        return null;
    }

    public static BusinessField getBusinessFieldById(BIFieldID id) {
        BusinessField field = null;
        for (BIModule module : BIModuleManager.getModules()) {
            BIDataSourceManagerProvider provider = module.getDataSourceManagerProvider();
            if (provider == null) {
                continue;
            }
            try {
                field = provider.getBusinessField(id);
            } catch (BIKeyAbsentException e) {
            }
            if (field != null) {
                return field;
            }
        }
        if (field == null) {
            BINonValueUtils.beyondControl();
        }
        return null;
    }

    public static BusinessTable getBusinessTableById(BITableID id) {
        BusinessTable field = null;
        for (BIModule module : BIModuleManager.getModules()) {
            BIDataSourceManagerProvider provider = module.getDataSourceManagerProvider();
            if (provider == null) {
                continue;
            }
            try {
                field = provider.getBusinessTable(id);
            } catch (BIKeyAbsentException e) {
            }
            if (field != null) {
                return field;
            }
        }
        if (field == null) {
            BINonValueUtils.beyondControl();
        }
        return null;
    }

    public static JSONObject createAliasJSON(long userId) throws JSONException {
        JSONObject jo = new JSONObject();
        for (BIModule module : BIModuleManager.getModules()) {
            BIAliasManagerProvider provider = module.getAliasManagerProvider();
            if (provider == null) {
                continue;
            }
            jo.join(provider.getAliasJSON(userId));
        }
        return jo;
    }

    public static List<BIPackageID> getAvailablePackID(long userId) {
        List<BIPackageID> authPacks = new ArrayList<BIPackageID>();
        for (BIModule module : BIModuleManager.getModules()) {
            Collection<BIPackageID> packs = module.getAvailablePackID(userId);
            if (packs != null) {
                authPacks.addAll(packs);
            }
        }
        return authPacks;
    }

    public static void clearAnalysisETLCache(long userId) {
        for (BIModule module : BIModuleManager.getModules()) {
            try {
                module.clearAnalysisETLCache(userId);
            } catch (Exception e) {
                BILoggerFactory.getLogger(BIModuleUtils.class).warn(e.getMessage(), e);
            }
        }
    }

    public static CubeTableSource getActualDBTableSource(CubeTableSource tableSource) {
        for (BusinessTable table : BICubeConfigureCenter.getPackageManager().getAllTables(UserControl.getInstance().getSuperManagerID())) {
            try {
                if (table.getTableSource().equals(tableSource)) {
                    return table.getTableSource();
                }
            } catch (Exception e) {
                BILoggerFactory.getLogger(BIModuleUtils.class).error(e.getMessage(), e);
            }
        }
        return tableSource;
    }
}