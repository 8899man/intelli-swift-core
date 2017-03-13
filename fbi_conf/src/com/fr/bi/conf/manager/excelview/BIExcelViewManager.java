package com.fr.bi.conf.manager.excelview;

import com.finebi.cube.conf.BISystemDataManager;
import com.fr.bi.conf.manager.excelview.source.ExcelViewSource;
import com.fr.bi.conf.provider.BIExcelViewManagerProvider;
import com.fr.bi.exception.BIKeyAbsentException;
import com.fr.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by Young's on 2016/4/20.
 */
public class BIExcelViewManager extends BISystemDataManager<SingleUserBIExcelViewManager> implements BIExcelViewManagerProvider {

    private static final long serialVersionUID = -8131499784554883877L;

    @Override
    public SingleUserBIExcelViewManager getExcelViewManager(long userId) {
        try {
            return getValue(userId);
        } catch (BIKeyAbsentException e) {
            throw new NullPointerException("Please check the userID:" + userId + ",which getIndex a empty manager");
        }
    }

    @Override
    public void saveExcelView(String tableId, ExcelViewSource source, long userId) {
        getExcelViewManager(userId).saveExcelView(tableId, source);
    }

    @Override
    public Map<String, ExcelViewSource> getExcelViews(long userId) {
        return getExcelViewManager(userId).getViews();
    }

    @Override
    public JSONObject createJSON(long userId) throws Exception {
        JSONObject jo = new JSONObject();
        Map<String, ExcelViewSource> views = getExcelViews(userId);
        Iterator<Map.Entry<String, ExcelViewSource>> iterator = views.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ExcelViewSource> map = iterator.next();
            jo.put(map.getKey(), map.getValue().createJSON());
        }
        return jo;
    }

    @Override
    public void clear(long userId) {
        getExcelViewManager(userId).clear();
    }

    @Override
    public SingleUserBIExcelViewManager constructUserManagerValue(Long userId) {
        return new SingleUserBIExcelViewManager();
    }

    @Override
    public String managerTag() {
        return "ExcelView";
    }

    @Override
    public String persistUserDataName(long key) {
        return managerTag();
    }

    @Override
    public void persistData(long userId) {
        persistUserData(userId);
    }
}
