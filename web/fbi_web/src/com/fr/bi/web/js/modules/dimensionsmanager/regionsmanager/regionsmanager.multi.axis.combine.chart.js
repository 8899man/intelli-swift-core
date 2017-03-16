/**
 * 区域管理器
 *
 * Created by GUY on 2016/3/17.
 * @class BI.MultiAxisCombineChartRegionsManager
 * @extends BI.RegionsManager
 */
BI.MultiAxisCombineChartRegionsManager = BI.inherit(BI.RegionsManager, {

    _defaultConfig: function () {
        return BI.extend(BI.MultiAxisCombineChartRegionsManager.superclass._defaultConfig.apply(this, arguments), {
            extraCls: "bi-multi-axis-combine-chart-regions-manager",
            dimensionCreator: BI.emptyFn,
            wId: ""
        });
    },

    _init: function () {
        BI.MultiAxisCombineChartRegionsManager.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.wrappers = {};
        var d1Header = this._createDimension1RegionHeader();
        var t1Header = this._createTarget1RegionHeader();
        var t2Header = this._createTarget2RegionHeader();
        var t3Header = this._createTarget3RegionHeader();
        this.wrappers[BICst.REGION.DIMENSION1] = this._createDimension1RegionWrapper();
        this.wrappers[BICst.REGION.TARGET1] = this._createTarget1RegionWrapper();
        this.wrappers[BICst.REGION.TARGET2] = this._createTarget2RegionWrapper();
        this.wrappers[BICst.REGION.TARGET3] = this._createTarget3RegionWrapper();
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
        }, {
            type: "bi.vtape",
            cls: "target-region-manager",
            items: [{
                el: t3Header,
                height: 26
            }, {
                el: this.wrappers[BICst.REGION.TARGET3]
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
            titleName: BI.i18nText("BI-Right_Value_Axis_One"),
            dimensionCreator: o.dimensionCreator,
            wId: o.wId,
            viewType: BICst.REGION.TARGET2
        });
        this.headers.push(header);
        return header;
    },

    _createTarget3RegionHeader: function () {
        var self = this, o = this.options;
        var header = BI.createWidget({
            type: "bi.calculate_target_region_header",
            height: 26,
            titleName: BI.i18nText("BI-Right_Value_Axis_Two"),
            dimensionCreator: o.dimensionCreator,
            wId: o.wId,
            viewType: BICst.REGION.TARGET3
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

    _createTarget1RegionWrapper: function () {
        var self = this, o = this.options;
        var region = BI.createWidget({
            type: "bi.target_region",
            containment: this,
            dimensionCreator: function (dId, op) {
                return o.dimensionCreator(dId, BICst.REGION.TARGET1, op)
            },
            wId: o.wId,
            viewType: BICst.REGION.TARGET1,
            regionType: BICst.REGION.TARGET1
        });
        region.on(BI.AbstractRegion.EVENT_CHANGE, function () {
            self.fireEvent(BI.RegionsManager.EVENT_CHANGE, arguments);
        });
        return region;
    },

    _createTarget2RegionWrapper: function () {
        var self = this, o = this.options;
        var region = BI.createWidget({
            type: "bi.target_region",
            containment: this,
            dimensionCreator: function (dId, op) {
                return o.dimensionCreator(dId, BICst.REGION.TARGET2, op)
            },
            wId: o.wId,
            viewType: BICst.REGION.TARGET2,
            regionType: BICst.REGION.TARGET2
        });
        region.on(BI.AbstractRegion.EVENT_CHANGE, function () {
            self.fireEvent(BI.RegionsManager.EVENT_CHANGE, arguments);
        });
        return region;
    },

    _createTarget3RegionWrapper: function () {
        var self = this, o = this.options;
        var region = BI.createWidget({
            type: "bi.target_region",
            containment: this,
            dimensionCreator: function (dId, op) {
                return o.dimensionCreator(dId, BICst.REGION.TARGET3, op)
            },
            wId: o.wId,
            viewType: BICst.REGION.TARGET3,
            regionType: BICst.REGION.TARGET3
        });
        region.on(BI.AbstractRegion.EVENT_CHANGE, function () {
            self.fireEvent(BI.RegionsManager.EVENT_CHANGE, arguments);
        });
        return region;
    }
});

$.shortcut('bi.multi_axis_combine_chart_regions_manager', BI.MultiAxisCombineChartRegionsManager);