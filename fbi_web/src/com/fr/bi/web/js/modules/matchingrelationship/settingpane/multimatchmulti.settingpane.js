/**
 * 多对多选择路径
 *
 * @class BI.MultiMatchMultiPathChooser
 * @extends BI.Widget
 */

BI.MultiMatchMultiPathChooser = BI.inherit(BI.Widget, {

    constants: {
        NoPath: 0,
        OnePath: 1,
        MorePath: 2
    },

    _defaultConfig: function () {
        return BI.extend(BI.MultiMatchMultiPathChooser.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-multi-match-multi-path-chooser"
        });
    },

    _init: function () {
        BI.MultiMatchMultiPathChooser.superclass._init.apply(this, arguments);
        var self = this;
        this.pathChooser = BI.createWidget({
            type: "bi.direction_path_chooser"
        });
        this.pathChooser.on(BI.DirectionPathChooser.EVENT_CHANGE, function () {
            var paths = self._packageValueByValue(this.getValue());
            self.lpath = paths.lpath;
            self.rpath = paths.rpath;
        });
        this.lpath = [];
        this.rpath = [];
        this.pathValueMap = {};
        this.pathRelationMap = {};

        this.tipTab = BI.createWidget({
            direction: "custom",
            type: "bi.tab",
            defaultShowIndex: false,
            logic: {
                dynamic: true
            },
            cardCreator: BI.bind(this._createTabs, this)
        });
        BI.createWidget({
            type: "bi.vertical",
            hgap: 10,
            scrolly: false,
            element: this.element,
            items: [{
                type: "bi.horizontal",
                scrollx: false,
                items: [this.pathChooser]
            }, this.tipTab]
        });
    },

    _createTabs: function (v) {
        switch (v) {
            case this.constants.NoPath:
                return BI.createWidget({
                    type: "bi.label",
                    textAlign: "left",
                    lgap: 20,
                    text: BI.i18nText("BI-Tip_Dimension_Target_No_Relation_May_Need_Change"),
                    cls: "no-path-label"
                });
            case this.constants.OnePath:
                return BI.createWidget({
                    type: "bi.label",
                    textAlign: "left",
                    lgap: 20,
                    text: BI.i18nText("BI-Tip_Only_One_Path_Between_Dimension_Target"),
                    cls: "one-path-label"
                });
            case this.constants.MorePath:
                return BI.createWidget();
        }
    },

    _checkPathOfOneTable: function(value){
        value = value || [];
        //if(value.length === 1){
        //    var pTId = BI.Utils.getTableIdByFieldID(value[0].primaryKey.field_id);
        //    var fTId = BI.Utils.getTableIdByFieldID(value[0].foreignKey.field_id);
        //    if(pTId === fTId){
        //        return [];
        //    }
        //}
        return value;
    },

    _createRightRegionPath: function (combineFieldId, dimensionFieldId, joinRegion) {
        var self = this;
        var ptId = BI.Utils.getTableIdByFieldID(dimensionFieldId);
        var paths = BI.Utils.getPathsFromFieldAToFieldB(combineFieldId, dimensionFieldId);
        if(paths.length === 1){
            this.rpath = paths[0];
        }
        return BI.map(paths, function (idx, path) {
            var p = [], pId = BI.UUID();
            BI.each(path, function (id, relation) {
                var foreignId = BI.Utils.getForeignIdFromRelation(relation);
                var primaryId = BI.Utils.getPrimaryIdFromRelation(relation);
                if (id === 0) {
                    p.push({
                        region: BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(primaryId)),
                        regionText: BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(primaryId)),
                        text: BI.i18nText("BI-Primary_Key"),
                        value: self.finalId,
                        direction: -1
                    });
                }
                if(BI.Utils.getTableIdByFieldID(foreignId) === BI.Utils.getTableIdByFieldID(primaryId)){
                    p.push({
                        region: BI.UUID(),
                        regionText: BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(foreignId)),
                        text: BI.Utils.getFieldNameByID(foreignId),
                        value: foreignId,
                        direction: -1
                    });
                }else{
                    p.push({
                        region: BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(foreignId)),
                        text: BI.Utils.getFieldNameByID(foreignId),
                        regionText: BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(foreignId)),
                        value: foreignId,
                        direction: -1
                    });
                }
            });
            //左右两侧路径上的value值可能相同，相同的话要改一下
            var leftValues = BI.pluck(joinRegion[idx], "value");
            BI.each(p, function(id, obj){
                if(BI.contains(leftValues, obj.value) && obj.text !== BI.i18nText("BI-Primary_Key")){
                    obj.value = BI.UUID();
                    obj.region = BI.UUID();
                }
            });
            self.pathValueMap[pId] = BI.pluck(p, "value");
            self.pathRelationMap[pId] = path;
            return p;
        });
    },

    _createLeftRegionPath: function (combineFieldId, targetFieldId) {
        var self = this;
        var ptId = BI.Utils.getTableIdByFieldID(targetFieldId);
        var paths = BI.Utils.getPathsFromFieldAToFieldB(combineFieldId, targetFieldId);
        if(paths.length === 1){
            this.lpath = paths[0];
        }
        return BI.map(paths, function (idx, path) {
            var p = [], pId = BI.UUID();
            BI.backEach(path, function (id, relation) {
                var foreignId = BI.Utils.getForeignIdFromRelation(relation);
                var primaryId = BI.Utils.getPrimaryIdFromRelation(relation);
                if(BI.Utils.getTableIdByFieldID(foreignId) === BI.Utils.getTableIdByFieldID(primaryId)){
                    p.push({
                        region: BI.UUID(),
                        regionText: BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(foreignId)),
                        text: BI.Utils.getFieldNameByID(foreignId),
                        value: foreignId
                    });
                }else{
                    p.push({
                        region: BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(foreignId)),
                        text: BI.Utils.getFieldNameByID(foreignId),
                        value: foreignId
                    });
                }
                if (id === 0) {
                    p.push({
                        region: BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(primaryId)),
                        text: BI.i18nText("BI-Primary_Key"),
                        value: self.finalId,
                        direction: -1
                    });
                }
            });
            self.pathValueMap[pId] = BI.pluck(p, "value");
            self.pathRelationMap[pId] = path;
            return p;
        });
    },

    _createRegionPathsByItems: function(items){
        this.options.dimensionFieldId = items.dimensionFieldId;
        this.options.combineTableId = items.combineTableId;
        if(BI.isNull(this.options.combineTableId)){
            return [];
        }
        var combineFieldId = BI.Utils.getFieldIDsOfTableID(items.combineTableId)[0];
        this.finalId = BI.UUID();
        var lregion = this._createLeftRegionPath(combineFieldId, BI.Utils.getFieldIDByDimensionID(items.targetIds[0]));
        var rregion = this._createRightRegionPath(combineFieldId, items.dimensionFieldId, lregion);
        var newRegion = [];
        if(BI.isEmptyArray(lregion)){
            return rregion;
        }
        BI.each(lregion, function(idx, lg){
            BI.each(rregion, function(id, rg){
                rg.splice(0, 1);
                var lgValues = BI.pluck(lg, "value");
                var rgValues = BI.pluck(rg, "value");
                var result = BI.find(rgValues, function(idx, value){
                    return BI.contains(lgValues, value);
                });
                BI.isNull(result) && newRegion.push(BI.concat(lg, rg));
            })
        });
        return newRegion;
    },

    _packageValueByValue: function (value) {
        var self = this, o = this.options;
        var combineIndex = 0;
        BI.find(value, function(idx, val){
            if(val === self.finalId){
                combineIndex = idx;
                return true;
            }
        });
        var lvalue = BI.first(value, combineIndex + 1);
        var rvalue = BI.rest(value, combineIndex);
        var lkey = null, rkey = null;
        BI.any(BI.keys(this.pathValueMap), function (idx, key) {
            if(BI.isEqual(lvalue, self.pathValueMap[key])){
                lkey = key;
            }
            if(BI.isEqual(rvalue, self.pathValueMap[key])){
                rkey = key;
            }
            return BI.isNotNull(lkey) && BI.isNotNull(rkey);
        });
        var result = {};
        result.lpath = this.pathRelationMap[lkey] || this.lpath;
        result.rpath = this.pathRelationMap[rkey] || this.rpath;
        return result;
    },

    _unpackValueByValue: function (value) {
        var v = [], self = this;
        var lvalue = this._checkPathOfOneTable(value.lpath);
        var rkey = null;
        BI.any(BI.keys(this.pathRelationMap), function (idx, key) {
            if(BI.isEqual(value.rpath, self.pathRelationMap[key])){
                rkey = key;
            }
            return BI.isNotNull(rkey);
        });
        var rvalue = BI.isNotNull(rkey) ? this.pathValueMap[rkey] : [];
        BI.backEach(lvalue, function (idx, val) {
            v.push(BI.Utils.getForeignIdFromRelation(val));
        });
        BI.each(rvalue, function (idx, val) {
            v.push(val);
        });
        return v;
    },

    populate: function (items) {
        this.path = [];
        this.pathRelationMap = {};
        this.pathValueMap = {};
        this.options.combineTableId = items.combineTableId;
        items = this._createRegionPathsByItems(items);
        this.pathChooser.populate(items);
        if(items.length > 1){
            this.pathChooser.setValue();
        }
        var pathCount = items.length;
        this.fireEvent(BI.MultiMatchMultiPathChooser.EVENT_PATH_CHANGE, pathCount === 1);
        this.tipTab.setSelect(pathCount > 1 ? this.constants.MorePath : (pathCount === 1 ? this.constants.OnePath : this.constants.NoPath));
    },

    _assertValue: function (v) {
        v = v || [];
        v[0] = v[0] || [];
        v[1] = v[1] || [];
        return v;
    },

    setValue: function (v) {
        v = this._assertValue(v);
        this.lpath = v[0];
        this.rpath = v[1];
        this.pathChooser.setValue(this._unpackValueByValue({lpath: this.lpath, rpath: this.rpath}));
    },

    getValue: function () {
        return [this.lpath, this.rpath];
    }
});
BI.MultiMatchMultiPathChooser.EVENT_PATH_CHANGE = "EVENT_PATH_CHANGE";
$.shortcut('bi.multi_match_multi_path_chooser', BI.MultiMatchMultiPathChooser);