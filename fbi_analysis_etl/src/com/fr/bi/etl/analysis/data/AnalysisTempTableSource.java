package com.fr.bi.etl.analysis.data;

import com.finebi.cube.api.ICubeDataLoader;
import com.fr.bi.common.inter.Traversal;
import com.fr.bi.conf.report.BIWidget;
import com.fr.bi.etl.analysis.Constants;
import com.fr.bi.stable.constant.BIBaseConstant;
import com.fr.bi.stable.data.db.BIDataValue;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.db.IPersistentTable;
import com.fr.bi.stable.data.db.PersistentTable;
import com.fr.bi.stable.data.source.AbstractCubeTableSource;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by 小灰灰 on 2016/4/12.
 */
public class AnalysisTempTableSource extends AbstractCubeTableSource implements AnalysisCubeTableSource {

    private static final String UNSUPPORT = "Temp Source do not support";

    private List<AnalysisCubeTableSource> sourceList;

    public AnalysisTempTableSource(List<AnalysisCubeTableSource> sourceList) {
        this.sourceList = sourceList;
    }

    @Override
    public UserCubeTableSource createUserTableSource(long userId) {
        throw new RuntimeException(UNSUPPORT);
    }

    @Override
    public List<AnalysisETLSourceField> getFieldsList() {
        return new ArrayList<AnalysisETLSourceField>();
    }

    @Override
    public void getSourceUsedAnalysisETLSource(Set<AnalysisCubeTableSource> set) {
    }
    @Override
    public void getSourceNeedCheckSource(Set<AnalysisCubeTableSource> set){

    }
    @Override
    public void refreshWidget() {

    }

    @Override
    public void refresh() {
    }

    @Override
    public Set<BIWidget> getWidgets() {
        return new HashSet<BIWidget>();
    }
    @Override
    public void reSetWidgetDetailGetter() {
    }

    @Override
    public void getParentAnalysisBaseTableIds(Set<String> set) {
    }

    @Override
    public IPersistentTable getPersistentTable() {
        return new PersistentTable(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
    }


    @Override
    public int getType() {
        return BIBaseConstant.TABLE_TYPE.TEMP;
    }

    @Override
    public JSONObject createJSON() throws Exception {
        JSONArray ja = new JSONArray();
        for (AnalysisCubeTableSource source : this.sourceList) {
            ja.put(source.createJSON());
        }
        JSONObject table = new JSONObject();
        table.put(Constants.ITEMS, ja);
        return table;
    }

    @Override
    public long read(Traversal<BIDataValue> travel, ICubeFieldSource[] field, ICubeDataLoader loader) {
        throw new RuntimeException(UNSUPPORT);
    }


}
