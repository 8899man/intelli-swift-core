package com.fr.bi.etl.analysis.manager;

import com.finebi.cube.conf.BISystemDataManager;
import com.finebi.cube.conf.pack.data.*;
import com.finebi.cube.conf.pack.group.BIBusinessGroup;
import com.finebi.cube.conf.singletable.SingleTableUpdateManager;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.common.factory.BIFactoryHelper;
import com.fr.bi.conf.data.pack.exception.BIGroupAbsentException;
import com.fr.bi.conf.data.pack.exception.BIGroupDuplicateException;
import com.fr.bi.conf.data.pack.exception.BIPackageAbsentException;
import com.fr.bi.conf.data.pack.exception.BIPackageDuplicateException;
import com.fr.bi.etl.analysis.Constants;
import com.fr.bi.etl.analysis.conf.AnalysisBusiTable;
import com.fr.bi.exception.BIKeyAbsentException;
import com.fr.bi.stable.data.BITableID;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.exception.BITableAbsentException;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 先注释掉普通用户看管理员的部分
 * Created by 小灰灰 on 2015/12/11.
 */
public class AnalysisBusiPackManager extends BISystemDataManager<SingleUserAnalysisBusiPackManager> implements BIAnalysisBusiPackManagerProvider {

    private static final String TAG = "AnalysisBusiPackManager";

    public SingleUserAnalysisBusiPackManager getUserAnalysisBusiPackManager(long userId) {
        try {
            return getValue(userId);
        } catch (BIKeyAbsentException e) {

            throw new NullPointerException("Please check the userID:" + userId + ",which getIndex a empty manager");
        }
    }

    @Override
    public SingleUserAnalysisBusiPackManager constructUserManagerValue(Long userId) {
        return BIFactoryHelper.getObject(SingleUserAnalysisBusiPackManager.class, userId);
    }

    @Override
    public String managerTag() {
        return TAG;
    }

    @Override
    public String persistUserDataName(long key) {
        return "sue" + File.separator + "pack" + key;
    }

    @Override
    public Set<IBusinessPackageGetterService> getAllPackages(long userId) {
        Set<IBusinessPackageGetterService> result = new HashSet<IBusinessPackageGetterService>();
        for (BIBusinessPackage biBusinessPackage : getUserAnalysisBusiPackManager(userId).getAllPacks()) {
            result.add(biBusinessPackage);
        }
//        if (userId != UserControl.getInstance().getSuperManagerID()){
//            for (BIBusinessPackage biBusinessPackage : getUserAnalysisBusiPackManager(UserControl.getInstance().getSuperManagerID()).getAllPacks()) {
//                result.add(biBusinessPackage);
//            }
//        }
        return result;
    }


    @Override
    public boolean isPackageDataChanged(long userId) {
        return false;
    }

    @Override
    public IBusinessPackageGetterService getPackage(long userId, BIPackageID packageID) throws BIPackageAbsentException {
        return null;
    }

    @Override
    public void renamePackage(long userId, BIPackageID packageID, BIPackageName packageName) throws BIPackageAbsentException, BIPackageDuplicateException {

    }

    @Override
    public void renameGroup(long userId, BIGroupTagName oldName, BIGroupTagName newName) throws BIGroupAbsentException, BIGroupDuplicateException {

    }

    @Override
    public void addPackage(long userId, BIBusinessPackage biBusinessPackage) throws BIPackageDuplicateException {

    }

    @Override
    public void finishGenerateCubes(long userId, Set<CubeTableSource> absentTables) {

    }

    @Override
    public void endBuildingCube(long userId, Set<CubeTableSource> absentTable) {

    }

    @Override
    public Boolean containPackage(long userId, BIBusinessPackage biPackage) {
        return null;
    }

    @Override
    public Boolean containPackageID(long userId, BIPackageID packageID) {
        return null;
    }

    @Override
    public Boolean containGroup(long userId, BIGroupTagName groupTagName) {
        return null;
    }

    @Override
    public void removePackage(long userId, BIPackageID packageID) throws BIPackageAbsentException {

    }

    @Override
    public void removeGroup(long userId, BIGroupTagName groupTagName) throws BIGroupAbsentException {

    }

    @Override
    public boolean isTableIncreased(long userId) {
        return false;
    }

    @Override
    public boolean isTableNoChange(long userId) {
        return false;
    }

    @Override
    public void persistData(long userId) {
        persistUserData(userId);
    }

    @Override
    public void createEmptyGroup(long userId, BIGroupTagName groupTagName, long position) throws BIGroupDuplicateException {

    }

    @Override
    public Boolean isPackageTaggedSomeGroup(long userId, BIPackageID packageID) {
        return null;
    }

    @Override
    public Boolean isPackageTaggedSpecificGroup(long userId, BIPackageID packageID, BIGroupTagName groupTagName) throws BIGroupAbsentException {
        return null;
    }

    @Override
    public void stickGroupTagOnPackage(long userId, BIPackageID packageID, BIGroupTagName groupTagName) throws BIPackageAbsentException, BIGroupAbsentException, BIPackageDuplicateException {

    }

    @Override
    public void removeGroupTagFromPackage(long userId, BIPackageID packageID, BIGroupTagName groupTagName) throws BIPackageAbsentException, BIGroupAbsentException, BIPackageDuplicateException {

    }

    @Override
    public BIBusinessGroup getGroup(long userId, BIGroupTagName groupTagName) throws BIGroupAbsentException {
        return null;
    }

    @Override
    public Set<BIBusinessPackage> getPackageByName(long userId, BIPackageName packName) {
        return null;
    }

    @Override
    public void startBuildingCube(long userId) {

    }

    @Override
    public void endBuildingCube(long userId) {

    }

    @Override
    public Boolean isPackagesEmpty(long userId) {
        return null;
    }

    @Override
    public JSONObject createGroupJSON(long userId) throws JSONException {
        return new JSONObject();
    }

    @Override
    public void parseGroupJSON(long userId, JSONObject jo) throws JSONException {

    }

    @Override
    public JSONObject createPackageJSON(long userId) throws Exception {
        return getUserAnalysisBusiPackManager(userId).createJSON(Locale.CHINA);
    }

    @Override
    public JSONObject createPackageJSON(long userId, Locale locale) throws Exception {
//        getUserAnalysisBusiPackManager(UserControl.getInstance().getSuperManagerID()).createJSON(locale);
//        JSONObject jo = getUserAnalysisBusiPackManager(userId).createJSON(locale);
//        if (userId != UserControl.getInstance().getSuperManagerID()){
//            JSONObject adminJO = getUserAnalysisBusiPackManager(UserControl.getInstance().getSuperManagerID()).createJSON(locale);
//            setEdit(adminJO);
//            jo.join(adminJO);
//        }
//        return jo;
        return getUserAnalysisBusiPackManager(userId).createJSON(locale);

    }

    @Override
    public JSONObject createAnalysisPackageJSON(long userId, Locale locale) throws Exception {
        return getUserAnalysisBusiPackManager(userId).createJSON(locale);
    }

    private void setEdit(JSONObject jo) throws Exception {
        if (jo.has(Constants.PACK_ID)) {
            JSONObject pack = jo.getJSONObject(Constants.PACK_ID);
            JSONArray tables = pack.getJSONArray("tables");
            for (int i = 0; i < tables.length(); i++) {
                tables.getJSONObject(i).put("inedible", true);
            }
        }
    }

    @Override
    public void removeTable(long userId, BIPackageID packageID, BITableID biTableID) throws BIPackageAbsentException, BITableAbsentException {

    }

    /**
     * 完成生成cube
     *
     * @param userId 用户id
     */
    @Override
    public void finishGenerateCubes(long userId) {

    }

    @Override
    public boolean isTableReduced(long userId) {
        return false;
    }

    public SingleTableUpdateManager getSingleTableUpdateManager(long userId) {
        return null;
    }

    /**
     * 更新
     */
    @Override
    public void envChanged() {

    }


    public boolean hasPackageAccessiblePrivilege(BusinessTable table, long userId) throws BITableAbsentException {
        return getUserAnalysisBusiPackManager(userId).getTable(table.getID().getIdentityValue()) != null;
    }

    @Override
    public void addTable(AnalysisBusiTable table) {
        getUserAnalysisBusiPackManager(table.getUserId()).addTable(table);
    }

    @Override
    public void removeTable(String tableId, long userId) {
        getUserAnalysisBusiPackManager(userId).removeTable(tableId);
    }

    /**
     * 先到管理员那找下，再找自己的吧
     *
     * @param tableId
     * @param userId
     * @return
     * @throws BITableAbsentException
     */
    @Override
    public AnalysisBusiTable getTable(String tableId, long userId) throws BITableAbsentException {
//        try{
//            return getUserAnalysisBusiPackManager(UserControl.getInstance().getSuperManagerID()).getTable(tableId);
//        } catch (BITableAbsentException e){
        return getUserAnalysisBusiPackManager(userId).getTable(tableId);
//        }
    }

    @Override
    public Set<BusinessTable> getAllTables(long userId) {
        return getUserAnalysisBusiPackManager(userId).getAllTables();
    }

    @Override
    public Set<BusinessTable> getAnalysisAllTables(long userId) {
        return null;
    }

    @Override
    public Set<BIBusinessPackage> getPackages4CubeGenerate(long userId) {
        return null;
    }
}