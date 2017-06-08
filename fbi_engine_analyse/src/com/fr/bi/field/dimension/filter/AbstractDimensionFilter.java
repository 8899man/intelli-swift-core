package com.fr.bi.field.dimension.filter;


import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.base.BICore;
import com.fr.bi.base.BICoreGenerator;
import com.fr.bi.conf.report.widget.field.dimension.filter.DimensionFilter;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.report.result.DimensionCalculator;
import com.fr.json.JSONObject;

/**
 * Created by Hiram on 2015/2/5.
 */
public abstract class AbstractDimensionFilter implements DimensionFilter {

    private static final long serialVersionUID = -1136067827261929823L;
    private DimensionFilter traversingResultFilter;

    @Override
    public GroupValueIndex createFilterIndex(DimensionCalculator dimension, BusinessTable target, ICubeDataLoader loader, long userId) {
        return null;
    }

    @Override
    public JSONObject createJSON() throws Exception {
        return new JSONObject();
    }

    public DimensionFilter getTraversingResultFilter() {
        return null;
    }

    public boolean hasTraversingResultFilter() {
        return traversingResultFilter != null;
    }


    @Override
    public boolean canCreateDirectFilter() {
        return false;
    }

    @Override
    public BICore fetchObjectCore() {
        return new BICoreGenerator(this).fetchObjectCore();
    }
}