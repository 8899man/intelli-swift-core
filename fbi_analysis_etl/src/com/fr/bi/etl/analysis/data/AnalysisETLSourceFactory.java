package com.fr.bi.etl.analysis.data;

import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.cal.analyze.report.report.BIWidgetFactory;
import com.fr.bi.conf.report.BIWidget;
import com.fr.bi.etl.analysis.Constants;
import com.fr.bi.etl.analysis.conf.AnalysisBusiTable;
import com.fr.bi.etl.analysis.manager.BIAnalysisETLManagerCenter;
import com.fr.bi.stable.data.BITableID;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;
import com.fr.stable.core.UUID;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by 小灰灰 on 2016/4/7.
 */
public class AnalysisETLSourceFactory {
    public static AnalysisCubeTableSource createTableSource(JSONArray ja, long userId) throws Exception {
        if (ja.length() == 1){
            return createOneTableSource(ja.getJSONObject(0), userId);
        } else {
            List<AnalysisCubeTableSource> sources = new ArrayList<AnalysisCubeTableSource>();
            for (int i = 0; i < ja.length(); i++){
                sources.add(createOneTableSource(ja.getJSONObject(i), userId));
            }
            return new AnalysisTempTableSource(sources);
        }
    }

    private static AnalysisCubeTableSource createOneTableSource(JSONObject jo, long userId) throws Exception {
        int type = jo.getInt("etlType");
        List<AnalysisETLSourceField> fieldList = new ArrayList<AnalysisETLSourceField>();
        if (jo.has(Constants.FIELDS)){
            JSONArray ja = jo.getJSONArray(Constants.FIELDS);
            for (int i = 0; i < ja.length(); i++){
                AnalysisETLSourceField field = new AnalysisETLSourceField();
                field.parseJSON(ja.getJSONObject(i));
                fieldList.add(field);
            }
        }
        String name = jo.optString("table_name", StringUtils.EMPTY);
        switch (type){
            case Constants.ETL_TYPE.SELECT_DATA :
                return new AnalysisBaseTableSource(createWidget(jo.getJSONObject("operator"), userId), type, fieldList, name, StringUtils.EMPTY);
            case Constants.ETL_TYPE.SELECT_NONE_DATA :
                AnalysisBaseTableSource temp = new AnalysisBaseTableSource(createWidget(jo.getJSONObject("operator"), userId), type, fieldList, name, StringUtils.EMPTY);
                BusinessTable businessTable = getAnyTableWithSource(temp);
                AnalysisBaseTableSource baseSource = null;
                if (businessTable == null){
                    AnalysisBusiTable table = new AnalysisBusiTable(UUID.randomUUID().toString(), userId);
                    baseSource = new AnalysisBaseTableSource(createWidget(jo.getJSONObject("operator"), userId), type, fieldList, name, table.getID().getIdentity());
                    table.setSource(baseSource);
                    BIAnalysisETLManagerCenter.getDataSourceManager().addTableSource(table, baseSource);
                } else {
                    baseSource = (AnalysisBaseTableSource) businessTable.getTableSource();
                }
                return baseSource;
            default :
                JSONArray parents = jo.getJSONArray("parents");
                List<AnalysisCubeTableSource> ps = new ArrayList<AnalysisCubeTableSource>();
                for (int i = 0; i < parents.length(); i ++){
                    ps.add(createOneTableSource(parents.getJSONObject(i), userId));
                }
                AnalysisETLTableSource source = new AnalysisETLTableSource(fieldList, name,AnalysisETLOperatorFactory.createOperatorsByJSON(jo, userId), ps );
                if (jo.has("invalidIndex")){
                source.setInvalidIndex(jo.getInt("invalidIndex"));
            }
                return source;
        }
    }

    private static BusinessTable getAnyTableWithSource(AnalysisBaseTableSource temp) {
//        for (BusinessTable table :  BIAnalysisETLManagerCenter.getDataSourceManager().getAllBusinessTable()){
//            if (ComparatorUtils.equals(table.getTableSource().getSourceID(), temp.getSourceID())){
//                return table;
//            }
//        }
        return null;
    }

    private static BIWidget createWidget(JSONObject jo, long userId) throws Exception {
        if (jo.has("widgetTableId")){
            BusinessTable talbe = BIAnalysisETLManagerCenter.getDataSourceManager().getBusinessTable(new BITableID(jo.getString("widgetTableId")));
            AnalysisCubeTableSource source = (AnalysisCubeTableSource) BIAnalysisETLManagerCenter.getDataSourceManager().getTableSource(talbe);
            if (source.getType() == Constants.TABLE_TYPE.BASE){
                return ((AnalysisBaseTableSource)source).getWidget();
            }
        }
        return BIWidgetFactory.parseWidget(jo, userId);
    }

}