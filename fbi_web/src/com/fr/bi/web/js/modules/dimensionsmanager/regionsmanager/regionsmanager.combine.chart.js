/**
 * 区域管理器
 *
 * Created by GUY on 2016/3/17.
 * @class BI.CombineChartRegionsManager
 * @extends BI.RegionsManager
 */
BI.CombineChartRegionsManager = BI.inherit(BI.RegionsManager, {

    _defaultConfig: function () {
        return BI.extend(BI.CombineChartRegionsManager.superclass._defaultConfig.apply(this, arguments), {
            extraCls: "bi-combine-chart-regions-manager",
            dimensionCreator: BI.emptyFn,
            wId: ""
        });
    },

    _init: function () {
        BI.CombineChartRegionsManager.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.wrappers = {};
        this.scopes = {};
        var d1Header = this._createDimension1RegionHeader();
        var d2Header = this._createDimension2RegionHeader();
        var t1Header = this._createTarget1RegionHeader();
        var t2Header = this._createTarget2RegionHeader();
        this.wrappers[BICst.REGION.DIMENSION1] = this._createDimension1RegionWrapper();
        this.wrappers[BICst.REGION.DIMENSION2] = this._createDimension2RegionWrapper();
        this.wrappers[BICst.REGION.TARGET1] = this._createTarget1RegionWrapper();
        this.wrappers[BICst.REGION.TARGET2] = this._createTarget2RegionWrapper();
        var items = [{
            type: "bi.vtape",
            cls: "dimension-region-manager",
            items: [{
                el: d1Header,
                height: 26
            }, {
                el: this.wrappers[BICst.REGION.DIMENSION1]
            }]
        }, {
            type: "bi.vtape",
            cls: "dimension-region-manager",
            items: [{
                el: d2Header,
                height: 26
            }, {
                el: this.wrappers[BICst.REGION.DIMENSION2]
            }]
        }, {
            type: "bi.vtape",
            cls: "target-region-manager",
            items: [{
                el: t1Header,
                height: 26
            }, {
                el: this.wrappers[BICst.REGION.TARGET1]
            }]
        }, {
            type: "bi.vtape",
            cls: "target-region-manager",
            items: [{
                el: t2Header,
                height: 26
            }, {
                el: this.wrappers[BICst.REGION.TARGET2]
            }]
        }];

        BI.createWidget({
            type: "bi.float_center",
            element: this.element,
            hgap: 10,
            vgap: 10,
            items: items
        })
    },

    _createDimension1RegionHeader: function () {
        var self = this, o = this.options;
        var header = BI.createWidget({
            type: "bi.region_header",
            height: 26,
            titleName: BI.i18nText("BI-Basic_Category"),
            dimensionCreator: o.dimensionCreator,
            wId: o.wId,
            viewType: BICst.REGION.DIMENSION1
        });
        this.headers.push(header);
        return header;
    },

    _createDimension2RegionHeader: function () {
        var self = this, o = this.options;
        var header = BI.createWidget({
            type: "bi.region_header",
            height: 26,
            titleName: BI.i18nText("BI-Basic_Series"),
            dimensionCreator: o.dimensionCreator,
            wId: o.wId,
            viewType: BICst.REGION.DIMENSION2
        });
        this.headers.push(header);
        return header;
    },

    _createTarget1RegionHeader: function () {
        var self = this, o = this.options;
        var header = BI.createWidget({
            type: "bi.calculate_target_region_header",
            height: 26,
            titleName: BI.i18nText("BI-Left_Value_Axis"),
            dimensionCreator: o.dimensionCreator,
            wId: o.wId,
            viewType: BICst.REGION.TARGET1
        });
        this.headers.push(header);
        return header;
    },

    _createTarget2RegionHeader: function () {
        var self = this, o = this.options;
        var header = BI.createWidget({
            type: "bi.calculate_target_region_header",
            height: 26,
            titleName: BI.i18nText("BI-Right_Value_Axis"),
            dimensionCreator: o.dimensionCreator,
            wId: o.wId,
            viewType: BICst.REGION.TARGET2
        });
        this.headers.push(header);
        return header;
    },

    _createDimension1RegionWrapper: function () {
        var self = this, o = this.options;

        var region = BI.createWidget({
            type: "bi.dimension_region",
            dimensionCreator: function (dId, op) {
                return o.dimensionCreator(dId, BICst.REGION.DIMENSION1, op)
            },
            containment: this,
            wId: o.wId,
            viewType: BICst.REGION.DIMENSION1,
            regionType: BICst.REGION.DIMENSION1
        });
        region.on(BI.AbstractRegion.EVENT_CHANGE, function () {
            self.fireEvent(BI.RegionsManager.EVENT_CHANGE, arguments);
        });
        return region;
    },

    _createDimension2RegionWrapper: function () {
        var self = this, o = this.options;

        var region = BI.createWidget({
            type: "bi.dimension_region",
            dimensionCreator: function (dId, op) {
                return o.dimensionCreator(dId, BICst.REGION.DIMENSION2, op)
            },
            containment: this,
            wId: o.wId,
            viewType: BICst.REGION.DIMENSION2,
            regionType: BICst.REGION.DIMENSION2
        });
        region.on(BI.AbstractRegion.EVENT_CHANGE, function () {
            self.fireEvent(BI.RegionsManager.EVENT_CHANGE, arguments);
        });
        return region;
    },

    _createTarget1RegionWrapper: function () {
        var self = this, o = this.options;
        var region = BI.createWidget({
            type: "bi.target_settings_region_wrapper",
            containment: this,
            dimensionCreator: o.dimensionCreator,
            scopeCreator: BI.bind(this._createTargetScope, this),
            wId: o.wId,
            viewType: BICst.REGION.TARGET1
        });
        region.on(BI.RegionWrapper.EVENT_CHANGE, function () {
            self.fireEvent(BI.RegionsManager.EVENT_CHANGE, arguments);
        });
        return region;
    },

    _createTarget2RegionWrapper: function () {
        var self = this, o = this.options;
        var region = BI.createWidget({
            type: "bi.target_settings_region_wrapper",
            containment: this,
            dimensionCreator: o.dimensionCreator,
            scopeCreator: BI.bind(this._createTargetScope, this),
            wId: o.wId,
            viewType: BICst.REGION.TARGET2
        });
        region.on(BI.RegionWrapper.EVENT_CHANGE, function () {
            self.fireEvent(BI.RegionsManager.EVENT_CHANGE, arguments);
        });
        return region;
    },

    _createTargetScope: function (regionType) {
        var self = this, o = this.options;
        var accumulation = BI.Utils.getSeriesAccumulationByWidgetID(o.wId);
        if (!this.scopes[regionType]) {
            this.scopes[regionType] = BI.createWidget({
                type: "bi.combine_chart_target_scope",
                regionType: regionType,
                wId: o.wId
            });
            this.scopes[regionType].on(BI.CombineChartTargetScope.EVENT_CHANGE, function () {
                self.fireEvent(BI.RegionsManager.EVENT_CHANGE, arguments);
            });
        }
        this.scopes[regionType].setEnable(accumulation.type !== BICst.SERIES_ACCUMULATION.EXIST);
        return this.scopes[regionType];
    },

    getValue: function () {
        var val = BI.CombineChartRegionsManager.superclass.getValue.apply(this, arguments);
        var scopes = {};
        BI.each(this.scopes, function (regionType, scope) {
            scopes[regionType] = scope.getValue();
        });
        val.scopes = scopes;
        return val;
    },

    populate: function () {
        BI.CombineChartRegionsManager.superclass.populate.apply(this, arguments);
        var self = this, o = this.options;
        var view = BI.Utils.getWidgetViewByID(o.wId);
        var scopes = BI.Utils.getWidgetScopeByID(o.wId);
        BI.each(view, function (regionType) {
            if (BI.Utils.isTargetRegionByRegionType(regionType)) {
                var scope = self._createTargetScope(regionType);
                scope.setValue(scopes[regionType]);
            }
        });
        BI.remove(this.scopes, function (regionType) {
            return !view[regionType] || view[regionType].length === 0
        });
    }
});

$.shortcut('bi.combine_chart_regions_manager', BI.CombineChartRegionsManager);