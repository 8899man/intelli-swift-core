package com.fr.bi.etl.analysis.data;

import com.finebi.cube.api.ICubeDataLoader;
import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.common.inter.Traversal;
import com.fr.bi.common.persistent.xml.BIIgnoreField;
import com.fr.bi.conf.data.source.AbstractETLTableSource;
import com.fr.bi.conf.data.source.operator.IETLOperator;
import com.fr.bi.conf.report.BIWidget;
import com.fr.bi.etl.analysis.Constants;
import com.fr.bi.stable.data.db.BIDataValue;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 小灰灰 on 2015/12/14.
 */
public class AnalysisETLTableSource extends AbstractETLTableSource<IETLOperator, AnalysisCubeTableSource> implements AnalysisCubeTableSource {

    @BIIgnoreField
    private transient Map<Long, UserCubeTableSource> userBaseTableMap = new ConcurrentHashMap<Long, UserCubeTableSource>();

    private int invalidIndex = -1;

    private String name;
    @BICoreField
    private List<AnalysisETLSourceField> fieldList;

    @Override
    public List<AnalysisETLSourceField> getFieldsList() {
        return fieldList;
    }

    @Override
    public Set<BIWidget> getWidgets() {
        Set<BIWidget> widgets = new HashSet<BIWidget>();
        for (AnalysisCubeTableSource source : getParents()) {
            widgets.addAll(source.getWidgets());
        }
        return widgets;
    }

    @Override
    public void getSourceUsedAnalysisETLSource(Set<AnalysisCubeTableSource> set) {
        if (set.contains(this)) {
            return;
        }
        for (AnalysisCubeTableSource source : getParents()) {
            source.getSourceUsedAnalysisETLSource(set);
            set.add(source);
        }
        set.add(this);
    }

    @Override
    public void refreshWidget() {
        for (AnalysisCubeTableSource source : getParents()) {
            source.refreshWidget();
        }
    }

    @Override
    public JSONObject createJSON() throws Exception {
        JSONObject jo = super.createJSON();
        if (fieldList != null && !fieldList.isEmpty()) {
            JSONArray ja = new JSONArray();
            for (AnalysisETLSourceField f : fieldList) {
                ja.put(f.createJSON());
            }
            jo.put(Constants.FIELDS, ja);
        }
        jo.put("table_name", name);
        if (invalidIndex != -1) {
            jo.put("invalidIndex", invalidIndex);
        }
        JSONArray tables = new JSONArray();
        for (int i = 0; i < parents.size(); i++) {
            tables.put(parents.get(i).createJSON());
        }
        jo.put(Constants.PARENTS, tables);
        AnalysisETLOperatorFactory.createJSONByOperators(jo, oprators);
        return jo;
    }

    public void setInvalidIndex(int invalidIndex) {
        this.invalidIndex = invalidIndex;
    }

    @Override
    public int getType() {
        return Constants.TABLE_TYPE.ETL;
    }

    /**
     * 写简单索引
     *
     * @param travel
     * @param field
     * @param loader
     * @return
     */
    @Override
    public long read(Traversal<BIDataValue> travel, ICubeFieldSource[] field, ICubeDataLoader loader) {
        throw new RuntimeException("Only UserTableSource can read");
    }

    @Override
    public long read4Part(Traversal<BIDataValue> traversal, ICubeFieldSource[] cubeFieldSources, String sql, long rowCount) {
        return 0;
    }

    public AnalysisETLTableSource(List<AnalysisETLSourceField> fieldList, String name, List<IETLOperator> operators, List<AnalysisCubeTableSource> parents) {
        super(operators, parents);
        this.fieldList = fieldList;
        this.name = name;
    }

    @Override
    public UserCubeTableSource createUserTableSource(long userId) {
        UserCubeTableSource source = userBaseTableMap.get(userId);
        if (source == null) {
            synchronized (userBaseTableMap) {
                UserCubeTableSource tmp = userBaseTableMap.get(userId);
                if (tmp == null) {
                    List<UserCubeTableSource> parents = new ArrayList<UserCubeTableSource>();
                    for (AnalysisCubeTableSource parent : getParents()) {
                        parents.add(parent.createUserTableSource(userId));
                    }
                    source = new UserETLTableSource(this, parents, userId);
                    userBaseTableMap.put(userId, source);
                } else {
                    source = tmp;
                }
            }
        }
        return source;
    }

    public void clearUserBaseTableMap() {
        userBaseTableMap.clear();
    }

}
