/**
 * Created by Young's on 2016/9/12.
 */
BI.ComplexRegionWrapper = BI.inherit(BI.Widget, {

    constants: {
        TITLE_ICON_HEIGHT: 20,
        TITLE_ICON_WIDTH: 20,
        REGION_HEIGHT_NORMAL: 25,
        REGION_DIMENSION_GAP: 5
    },

    _defaultConfig: function () {
        return BI.extend(BI.ComplexRegionWrapper.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-complex-region-wrapper",
            titleName: ""
        });
    },

    _init: function () {
        BI.ComplexRegionWrapper.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.regions = {};
        this.center = BI.createWidget({
            type: "bi.button_group",
            layouts: [{
                type: "bi.vertical",
                cls: "regions-container",
                scrolly: true,
                width: "100%",
                height: "100%",
                hgap: this.constants.REGION_DIMENSION_GAP,
                vgap: this.constants.REGION_DIMENSION_GAP
            }]
        });
        
        this._appendEmptyRegion();
        BI.createWidget({
            type: "bi.vtape",
            element: this.element,
            items: [{
                el: {
                    type: "bi.border",
                    items: {
                        west: {
                            el: {
                                type: "bi.label",
                                cls: "region-north-title",
                                text: o.titleName,
                                height: this.constants.REGION_HEIGHT_NORMAL
                            },
                            height: this.constants.REGION_HEIGHT_NORMAL,
                            left: this.constants.REGION_DIMENSION_GAP
                        }
                    },
                    cls: "region-north"
                },
                height: this.constants.REGION_HEIGHT_NORMAL
            }, {
                type: "bi.default",
                items: [this.center]
            }]
        });
    },

    _appendEmptyRegion: function () {
        var self = this;
        var emptyRegion = this.center.getNodeById(BI.ComplexEmptyRegion.ID);
        if (BI.isNotNull(emptyRegion)) {
            this.center.removeItems([emptyRegion.getValue()]);
        }
        emptyRegion = BI.createWidget({
            type: "bi.complex_empty_region",
            id: BI.ComplexEmptyRegion.ID
        });
        emptyRegion.on(BI.ComplexEmptyRegion.EVENT_CHANGE, function (data) {
            self._addRegionAndDimension(data);
        });
        this.emptyRegion = emptyRegion;
        this.center.addItems([emptyRegion]);
    },

    _addRegionAndDimension: function (data) {
        var self = this, o = this.options;
        var wrapperType = o.wrapperType;
        var regionTypes = BI.keys(this.regions);
        var newRegionType = o.wrapperType;
        if (regionTypes.length !== 0) {
            //找到最大的 +1
            newRegionType = BI.parseInt(BI.sortBy(regionTypes)[regionTypes.length - 1]) + 1;
        }
        this.regions[newRegionType] = BI.createWidget({
            type: "bi.complex_dimension_region",
            dimensionCreator: o.dimensionCreator,
            wId: o.wId,
            regionType: newRegionType
        });
        this.center.addItems([this.regions[newRegionType]]);
        BI.each(data, function (i, dimension) {
            self.regions[newRegionType].addDimension(dimension.dId || BI.UUID(), dimension);
        });
        this._appendEmptyRegion();
    },
    
    //排序 
    sortRegion: function() {
        var self = this, o = this.options;
        var sortedRegions = this.center.element.sortable("toArray");
        var originalRegionKeys = BI.keys(this.regions);
        var originalRegions = {};
        BI.each(this.regions, function (idx, region) {
            originalRegions[region.getRegionType()] = region;
        });
        self.regions = {};
        BI.each(sortedRegions, function(i, regionType) {
            self.regions[regionType] = originalRegions[originalRegionKeys[i]];
        });
    },

    refreshRegion: function (type, dimensions) {
        var o = this.options;
        if (BI.isNull(this.regions[type])) {
            this.regions[type] = BI.createWidget({
                type: "bi.complex_dimension_region",
                dimensionCreator: o.dimensionCreator,
                wId: o.wId,
                regionType: type
            });
            this.center.addItems([this.regions[type]]);
            this._appendEmptyRegion();
        }
        if (dimensions.length > 0) {
            this.regions[type].populate(dimensions);
        } else {
            this.regions[type].destroy();
            delete this.regions[type];
        }
    },

    getWrapperType: function () {
        return this.options.wrapperType;
    },

    getCenterArea: function() {
        return this.center;  
    },

    getRegions: function () {
        return this.regions;
    },

    getEmptyRegionValue: function () {
        return this.emptyRegion && this.emptyRegion.getDimensions();
    },


    populate: function () {

    }
});
$.shortcut("bi.complex_region_wrapper", BI.ComplexRegionWrapper);