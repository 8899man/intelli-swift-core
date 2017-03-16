/**
 * 区域管理器
 *
 * Created by GUY on 2016/3/17.
 * @class BI.DetailTableRegionsManager
 * @extends BI.RegionsManager
 */
BI.DetailTableRegionsManager = BI.inherit(BI.RegionsManager, {

    _defaultConfig: function () {
        return BI.extend(BI.DetailTableRegionsManager.superclass._defaultConfig.apply(this, arguments), {
            extraCls: "bi-detail-regions-manager",
            dimensionCreator: BI.emptyFn,
            wId: ""
        });
    },

    _init: function () {
        BI.DetailTableRegionsManager.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.wrappers = {};
        var dHeader = this._createDimensionRegionHeader();
        this.wrappers[BICst.REGION.DIMENSION1] = this._createDimensionRegionWrapper();
        var items = [{
            type: "bi.vtape",
            cls: "dimension-region-manager",
            items: [{
                el: dHeader,
                height: 26
            }, {
                el: this.wrappers[BICst.REGION.DIMENSION1]
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

    _createDimensionRegionHeader: function () {
        var self = this, o = this.options;
        var header = BI.createWidget({
            type: "bi.detail_region_header",
            height: 26,
            titleName: BI.i18nText("BI-Basic_Data"),
            dimensionCreator: o.dimensionCreator,
            wId: o.wId,
            viewType: BICst.REGION.DIMENSION1
        });
        this.headers.push(header);
        return header;
    },

    _createDimensionRegionWrapper: function () {
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
    }
});

$.shortcut('bi.detail_table_regions_manager', BI.DetailTableRegionsManager);