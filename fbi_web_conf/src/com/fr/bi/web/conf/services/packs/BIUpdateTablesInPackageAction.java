package com.fr.bi.web.conf.services.packs;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.conf.BICubeManagerProvider;
import com.finebi.cube.conf.BISystemPackageConfigurationProvider;
import com.finebi.cube.conf.pack.data.*;
import com.finebi.cube.conf.relation.BITableRelationHelper;
import com.finebi.cube.conf.table.BusinessTable;
import com.finebi.cube.conf.trans.UserAliasManager;
import com.finebi.cube.relation.BITableRelation;
import com.fr.bi.base.BIBusinessPackagePersistThread;
import com.fr.bi.base.BIBusinessPackagePersistThreadHolder;
import com.fr.bi.base.BIUser;
import com.fr.bi.cal.generate.CubeBuildHelper;
import com.fr.bi.conf.data.pack.exception.BIGroupAbsentException;
import com.fr.bi.conf.data.pack.exception.BIGroupDuplicateException;
import com.fr.bi.conf.data.pack.exception.BIPackageAbsentException;
import com.fr.bi.conf.data.pack.exception.BIPackageDuplicateException;
import com.fr.bi.conf.data.source.DBTableSource;
import com.fr.bi.conf.data.source.ExcelTableSource;
import com.fr.bi.conf.data.source.ServerTableSource;
import com.fr.bi.conf.data.source.TableSourceFactory;
import com.fr.bi.conf.manager.excelview.source.ExcelViewSource;
import com.fr.bi.conf.manager.update.source.UpdateSettingSource;
import com.fr.bi.conf.provider.BIConfigureManagerCenter;
import com.fr.bi.exception.BIKeyDuplicateException;
import com.fr.bi.exception.BIRuntimeException;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.data.BITableID;
import com.fr.bi.stable.data.source.AbstractTableSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.web.conf.AbstractBIConfigureAction;
import com.fr.fs.web.service.ServiceUtils;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;
import com.fr.stable.bridge.StableFactory;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


public class BIUpdateTablesInPackageAction extends AbstractBIConfigureAction {

    @Override
    public String getCMD() {
        return "update_tables_in_package";
    }

    @Override
    protected void actionCMDPrivilegePassed(HttpServletRequest req,
                                            HttpServletResponse res) throws Exception {
        updatePackageTables(req);
    }

    private void writeResource(long userId) {
        try {
            BICubeConfigureCenter.getPackageManager().persistData(userId);
            BICubeConfigureCenter.getTableRelationManager().persistData(userId);
            BIConfigureManagerCenter.getExcelViewManager().persistData(userId);
            BIConfigureManagerCenter.getUpdateFrequencyManager().persistData(userId);
            BICubeConfigureCenter.getAliasManager().persistData(userId);
            BICubeConfigureCenter.getDataSourceManager().persistData(userId);
            BIConfigureManagerCenter.getCubeConfManager().persistData(userId);
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
    }

    /**
     * update
     *
     * @param req
     * @throws Exception
     */
    public void updatePackageTables(HttpServletRequest req) throws Exception {

        String packageName = WebUtils.getHTTPRequestParameter(req, "name");
        String groupName = WebUtils.getHTTPRequestParameter(req, "groupName");
        String packageId = WebUtils.getHTTPRequestParameter(req, "id");
        String tableIds = WebUtils.getHTTPRequestParameter(req, "tables");
        String tableString = WebUtils.getHTTPRequestParameter(req, "table_data");
        String relations = WebUtils.getHTTPRequestParameter(req, "relations");
        String translations = WebUtils.getHTTPRequestParameter(req, "translations");
        String usedFields = WebUtils.getHTTPRequestParameter(req, "used_fields");
        String excelViews = WebUtils.getHTTPRequestParameter(req, "excel_views");
        String updateSettings = WebUtils.getHTTPRequestParameter(req, "update_settings");
        final long userId = ServiceUtils.getCurrentUserID(req);
        JSONArray tableIdsJO = new JSONArray(tableIds);
        JSONObject tableDataJO = new JSONObject(tableString);
        JSONObject relationsJO = relations != null ? new JSONObject(relations) : new JSONObject();
        JSONObject translationsJO = translations != null ? new JSONObject(translations) : new JSONObject();
        JSONObject usedFieldsJO = usedFields != null ? new JSONObject(usedFields) : new JSONObject();
        JSONObject excelViewJO = excelViews != null ? new JSONObject(excelViews) : new JSONObject();
        JSONObject updateSettingJO = updateSettings != null ? new JSONObject(updateSettings) : new JSONObject();

        BIBusinessPackage pack = (BIBusinessPackage) EditPackageConfiguration(packageName, groupName, packageId, userId);
        Set<BusinessTable> oldTables = new HashSet<BusinessTable>();
        oldTables.addAll(pack.getBusinessTables());
        BISystemPackageConfigurationProvider packageConfigProvider = BICubeConfigureCenter.getPackageManager();
        packageConfigProvider.parseSinglePackageJSON(userId, pack.getID(), tableIdsJO, usedFieldsJO, tableDataJO);
        pack = (BIBusinessPackage) packageConfigProvider.getPackage(userId, pack.getID());

        saveTables(packageName, userId, tableIdsJO, tableDataJO, pack, packageConfigProvider);
        saveTranslations(translationsJO, userId);
        saveRelations(relationsJO, userId);
        saveExcelView(excelViewJO, userId);
        saveUpdateSetting(updateSettingJO, userId);
        BIConfigureManagerCenter.getCubeConfManager().updatePackageLastModify();

//      更新远程的pack到本地
        pack = (BIBusinessPackage) packageConfigProvider.getPackage(userId, pack.getID());
        //实时生成excel cube
        updateExcelTables(userId, getExcelTable(oldTables, pack.getBusinessTables()));

        BIBusinessPackagePersistThreadHolder.getInstance().getBiBusinessPackagePersistThread().triggerWork(new BIBusinessPackagePersistThread.Action() {//单独的线程写业务包配置文件，web端立即返回
            @Override
            public void work() {
                writeResource(userId);
            }
        });
    }

    private void saveTables(String packageName, long userId, JSONArray tableIdsJO, JSONObject tableDataJO, BIBusinessPackage pack, BISystemPackageConfigurationProvider packageConfigProvider) throws Exception {
        for (int i = 0; i < tableIdsJO.length(); i++) {
            String tableId = tableIdsJO.optJSONObject(i).optString("id");
            JSONObject tableJson = tableDataJO.optJSONObject(tableId);
            if (tableJson != null) {
                BusinessTable table = pack.getSpecificTable(new BITableID(tableId));
                if (BICubeConfigureCenter.getDataSourceManager().containTableSource(table)) {
                    CubeTableSource tableSource = TableSourceFactory.createTableSource(tableJson, userId);
                    CubeTableSource storeTableSource = BICubeConfigureCenter.getDataSourceManager().getTableSource(table);
                    /**
                     *ELT删除了ETL操作以后也可能是DBTable,但是这个时候需要重新构建TableSource
                     */
                    if (reuseTableSource(tableSource) && reuseTableSource(storeTableSource)) {
                        if (reuseTableSource(storeTableSource)) {
//                            BICubeConfigureCenter.getDataSourceManager().addTableSource(table, storeTableSource);
                            packageConfigProvider.packageAddTableSource(userId, pack.getID(), tableId, storeTableSource, false);
                        }
                    } else {
                        /**
                         * 如果ETL已经存在，那么不再刷新字段。
                         * 因为如果数据库连接断掉，那么字段没有的。
                         */
                        if (ComparatorUtils.equals(storeTableSource.getSourceID(), tableSource.getSourceID())) {
//                            addTableSource(table, storeTableSource);
                            packageConfigProvider.packageAddTableSource(userId, pack.getID(), tableId, storeTableSource, false);
                        } else {
                            /**
                             * 否则必须确保保存的TableSource是完整的。
                             * 如果首次添加ETL连接断掉，依然没有字段。
                             */
//                            ensureFieldInitial(tableSource);
//                            addTableSource(table, tableSource);
                            packageConfigProvider.packageAddTableSource(userId, pack.getID(), tableId, tableSource, true);
                        }
                    }
                } else {
                    CubeTableSource tableSource = TableSourceFactory.createTableSource(tableJson, userId);
//                    ensureFieldInitial(tableSource);
//                    addTableSource(table, tableSource);
                    packageConfigProvider.packageAddTableSource(userId, pack.getID(), tableId, tableSource, true);
                }
            } else {
                BILoggerFactory.getLogger().error("table : id = " + tableId + " in pack: " + packageName + " save failed");
            }
        }
    }

    private boolean reuseTableSource(CubeTableSource tableSource) {
        return tableSource instanceof DBTableSource && !(tableSource instanceof ServerTableSource);
    }

    private void ensureFieldInitial(CubeTableSource tableSource) {
        ((AbstractTableSource) tableSource).getRecordedFields();
    }

    private void addTableSource(BusinessTable businessTable, CubeTableSource source) throws BIKeyDuplicateException {
        try {
            BICubeConfigureCenter.getDataSourceManager().addTableSource(businessTable, source);
        } catch (BIRuntimeException e) {
            BILoggerFactory.getLogger(BIUpdateTablesInPackageAction.class).error(e.getMessage(), e);
        }
    }

    private List<CubeTableSource> getExcelTable(Set<BusinessTable> oldTables, Set<BusinessTable> newTables) {
        //检查excel是否是原先的excel
        List<CubeTableSource> excelSource = new ArrayList<CubeTableSource>();
        for (BusinessTable newTable : newTables) {
            CubeTableSource tableSource = newTable.getTableSource();
            if (tableSource instanceof ExcelTableSource) {
                boolean isExist = false;
                for (BusinessTable oldTable : oldTables) {
                    CubeTableSource oldSource = oldTable.getTableSource();
                    if (ComparatorUtils.equals(oldTable.getID(), newTable.getID())
                            && ComparatorUtils.equals(oldSource.getSourceID(), tableSource.getSourceID())) {
                        isExist = true;
                    }
                }
                if (!isExist) {
                    excelSource.add(tableSource);
                }
            }
        }
        return excelSource;
    }

    private void updateExcelTables(long userId, List<CubeTableSource> excelSources) throws InterruptedException {
        for (CubeTableSource source : excelSources) {
//            new CubeBuildManager().CubeBuildSingleTable(userId, source.getSourceID(), DBConstant.SINGLE_TABLE_UPDATE_TYPE.ALL);
//            通过RPC接口远程进行cube生成
            CubeBuildHelper.getInstance().addSingleTableTask2Queue(userId, source.getSourceID(), DBConstant.SINGLE_TABLE_UPDATE_TYPE.ALL);
        }
    }

    private IBusinessPackageGetterService EditPackageConfiguration(String packageName, String groupName, String packageId, long userId) throws BIPackageDuplicateException, BIPackageAbsentException, BIGroupDuplicateException, BIGroupAbsentException {
        BIPackageID packageID = new BIPackageID(packageId);
        BISystemPackageConfigurationProvider packageConfigProvider = BICubeConfigureCenter.getPackageManager();
        IBusinessPackageGetterService pack;
        if (!packageConfigProvider.containPackageID(userId, packageID)) {
            BIBusinessPackage biBasicBusinessPackage = new BIBasicBusinessPackage(new BIPackageID(packageId), new BIPackageName(packageName), new BIUser(userId), System.currentTimeMillis());
            packageConfigProvider.addPackage(userId, biBasicBusinessPackage);
            pack = packageConfigProvider.getPackage(userId, packageID);
        } else {
            pack = packageConfigProvider.getPackage(userId, packageID);
            if (!ComparatorUtils.equals(packageName, pack.getName().getValue())) {
                BICubeConfigureCenter.getPackageManager().renamePackage(userId, packageID, new BIPackageName(packageName));
            }
        }
        if (!"".equals(groupName)) {
            BIGroupTagName groupTagName = new BIGroupTagName(groupName);
            if (!packageConfigProvider.containGroup(userId, groupTagName)) {
                packageConfigProvider.createEmptyGroup(userId, groupTagName, System.currentTimeMillis());
            }
            if (!packageConfigProvider.isPackageTaggedSpecificGroup(userId, packageID, groupTagName)) {
                packageConfigProvider.stickGroupTagOnPackage(userId, packageID, groupTagName);
            }

        }
        return pack;
    }

    // TODO: 2016/10/28 别名的操作使用了接口获取container，但是container的操作全在本地内存，未更新到远程
    private void saveTranslations(JSONObject translations, long userId) throws Exception {
        Iterator<String> tranIds = translations.keys();
        UserAliasManager userAliasManager = new UserAliasManager(userId);
        while (tranIds.hasNext()) {
            String tranId = tranIds.next();
            String tranName = translations.optString(tranId);
            userAliasManager.setTransName(tranId, tranName);
        }
        BICubeConfigureCenter.getAliasManager().setTransManager(userId, userAliasManager);
    }

    private void saveRelations(JSONObject relationsJO, long userId) throws JSONException {
        BICubeConfigureCenter.getTableRelationManager().clear(userId);
        Set<BITableRelation> relationsSet = new HashSet<BITableRelation>();
        JSONArray relationConn = relationsJO.getJSONArray("connectionSet");
        for (int k = 0; k < relationConn.length(); k++) {
            try {
                JSONObject r = relationConn.getJSONObject(k);
                JSONObject pKeyJO = r.getJSONObject("primaryKey");
                JSONObject fKeyJO = r.getJSONObject("foreignKey");
                JSONObject reConstructedRelationJo = new JSONObject();
                JSONObject reConstructedPrimaryKeyJo = new JSONObject();
                JSONObject reConstructedForeignKeyJo = new JSONObject();
                reConstructedPrimaryKeyJo.put("field_id", getFieldIdFromRelationKeyJo(pKeyJO));
                reConstructedForeignKeyJo.put("field_id", getFieldIdFromRelationKeyJo(fKeyJO));
                reConstructedRelationJo.put("primaryKey", reConstructedPrimaryKeyJo);
                reConstructedRelationJo.put("foreignKey", reConstructedForeignKeyJo);
                BITableRelation tableRelation = BITableRelationHelper.getRelation(reConstructedRelationJo);
                relationsSet.add(tableRelation);
            } catch (Exception e) {
                BILoggerFactory.getLogger().error(e.getMessage(), e);
            }
        }
        BICubeConfigureCenter.getTableRelationManager().registerTableRelationSet(userId, relationsSet);
    }


    private void saveExcelView(JSONObject excelViewJO, long userId) throws Exception {
        Iterator<String> viewTableIds = excelViewJO.keys();
        BIConfigureManagerCenter.getExcelViewManager().clear(userId);
        while (viewTableIds.hasNext()) {
            String viewId = viewTableIds.next();
            ExcelViewSource source = new ExcelViewSource();
            source.parseJSON(excelViewJO.getJSONObject(viewId));
            BIConfigureManagerCenter.getExcelViewManager().saveExcelView(viewId, source, userId);
        }
    }

    public void saveUpdateSetting(JSONObject updateSettingJO, long userId) throws Exception {
        UpdateSettingSource originalGlobalSetting = BIConfigureManagerCenter.getUpdateFrequencyManager().getUpdateSettingManager(userId).getUpdateSettings().get(DBConstant.CUBE_UPDATE_TYPE.GLOBAL_UPDATE);
        Iterator<String> sourceTableIds = updateSettingJO.keys();
        BIConfigureManagerCenter.getUpdateFrequencyManager().clear(userId);
        while (sourceTableIds.hasNext()) {
            String sourceTableId = sourceTableIds.next();
            UpdateSettingSource source = new UpdateSettingSource();
            if (ComparatorUtils.equals(sourceTableId, DBConstant.CUBE_UPDATE_TYPE.GLOBAL_UPDATE)) {
                source = null != originalGlobalSetting ? originalGlobalSetting : source;
            } else {
                source.parseJSON(updateSettingJO.getJSONObject(sourceTableId));
            }
            BIConfigureManagerCenter.getUpdateFrequencyManager().saveUpdateSetting(sourceTableId, source, userId);
        }
//        BICubeManager biCubeManager = StableFactory.getMarkedObject(BICubeManagerProvider.XML_TAG, BICubeManager.class);
//        biCubeManager.resetCubeGenerationHour(userId);
        StableFactory.getMarkedObject(BICubeManagerProvider.XML_TAG, BICubeManagerProvider.class).resetCubeGenerationHour(userId);
    }

    private JSONObject createTablesJsonObject(JSONArray tableIdsJA, JSONObject usedFieldsJO, JSONObject tableDataJO) throws Exception {
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        if (tableIdsJA != null) {
            for (int i = 0; i < tableIdsJA.length(); i++) {
                String tId = tableIdsJA.optJSONObject(i).optString("id");
                JSONObject idJo = new JSONObject();
                idJo.put("id", tId);
                JSONArray usedFields = usedFieldsJO.optJSONArray(tId);
                idJo.put("used_fields", usedFields);
                JSONObject table = tableDataJO.getJSONObject(tId);
                idJo.put("fields", table.getJSONArray("fields"));
                ja.put(idJo);
            }
        }
        return jo.put("data", ja);
    }

    private String getFieldIdFromRelationKeyJo(JSONObject relationJo) throws JSONException {
        String fieldId = StringUtils.EMPTY;
        //新增的表不能通过tableHelper获得
//        BusinessTable table = BusinessTableHelper.getBusinessTable(new BITableID(relationJo.getString("table_id")));
//        try {
//            fieldId = BusinessTableHelper.getSpecificField(table, relationJo.getString("field_name")).getFieldID().getIdentityValue();
//        } catch (BIFieldAbsentException e) {
//            BILogger.getLogger().error(e.getMessage(), e);
//        }
        if (relationJo.has("field_id")) {
            fieldId = relationJo.getString("field_id");
        }
        return fieldId;
    }
}
