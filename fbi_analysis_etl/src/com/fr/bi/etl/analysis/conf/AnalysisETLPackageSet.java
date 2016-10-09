package com.fr.bi.etl.analysis.conf;


import com.finebi.cube.conf.pack.data.BIPackageID;
import com.finebi.cube.conf.pack.imp.BIPackageContainer;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.conf.data.pack.exception.BIPackageAbsentException;
import com.fr.bi.conf.data.pack.exception.BIPackageDuplicateException;
import com.fr.bi.etl.analysis.Constants;
import com.fr.bi.stable.data.BITableID;
import com.fr.bi.stable.exception.BITableAbsentException;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.general.Inter;
import com.fr.json.JSONObject;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by 小灰灰 on 2015/12/23.
 */
public class AnalysisETLPackageSet extends BIPackageContainer {
    private static final String PACK_NAME = Inter.getLocText("BI-MYETL");
    private transient AnalysisETLBusiPack pack;

    public AnalysisETLPackageSet(long userId) {
        super(userId);
    }

    protected AnalysisETLBusiPack createPackage(String id, String pack) {
        return new AnalysisETLBusiPack(id, pack, user, System.currentTimeMillis());
    }

    private AnalysisETLBusiPack getPack() {
        if (pack != null) {
            return pack;
        }
        synchronized (container) {
            try {
                pack = (AnalysisETLBusiPack) getPackage(new BIPackageID(Constants.PACK_ID));
            } catch (BIPackageAbsentException ignore_) {
                BILoggerFactory.getLogger().error(ignore_.getMessage());
            }
            if (pack == null) {
                try {
                    pack = createPackage(Constants.PACK_ID, PACK_NAME);
                    addPackage(pack);
                } catch (BIPackageDuplicateException ignore) {

                }

            }
            return pack;
        }
    }

    public void removeTable(BITableID tableId) {
        getPack().removeBusinessTableByID(tableId);
    }

    public void addTable(AnalysisBusiTable table) {
        removeTable(table.getID());
        getPack().addBusinessTable(table);
    }

    public AnalysisBusiTable getTable(String tableId) throws BITableAbsentException {
        return getPack().getSpecificTable(new BITableID(tableId));
    }

    public JSONObject createJSON(Locale locale) throws Exception {
        JSONObject jo = new JSONObject();
        JSONObject pack = getPack().createJSON();
        if (pack.getJSONArray("tables").length() > 0){
            pack.put("name", Inter.getLocText(PACK_NAME, locale));
            jo.put(Constants.PACK_ID, pack);
        }
        return jo;
    }

    public Set<BusinessTable> getAllTables(){
        Set<BusinessTable> result = new HashSet<BusinessTable>();
        result.addAll(getPack().getBusinessTables());
        return result;
    }

}