/**
 * @class BI.RelationPane
 * @extend BI.Widget
 * 关联关系面板
 */
BI.RelationPane = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.RelationPane.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-relation-pane"
        })
    },

    _init: function () {
        BI.RelationPane.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.model = new BI.RelationPaneModel({
            field: o.field
        });
        this._createRelationTree();
        var addRelationTable = BI.createWidget({
            type: "bi.button",
            text: "+" + BI.i18nText("BI-Linked_To_Other"),
            height: 30,
            width: 140
        });
        addRelationTable.on(BI.Button.EVENT_CHANGE, function () {
            self._createSelectDataMask();
        });
        BI.createWidget({
            type: "bi.vertical",
            element: this.element,
            items: [this.relationTree, {
                type: "bi.left_right_vertical_adapt",
                items: {
                    right: [addRelationTable]
                },
                rrgap: 98,
                height: 60
            }]
        });
        if (this.model.getRelationIds().length === 0) {
            this._createSelectDataMask();
        }
        this._refreshBaseTablePrimaryKeyIcon();
    },

    _drawSVGLine: function () {
        var treeValue = this.relationTree.getValue();
        if (treeValue.length === 0 || treeValue[0] === "") {
            return;
        }
        var svg = BI.createWidget({
            type: "bi.svg"
        });
        svg.element.css({"z-index": -1});
        var branchLength = treeValue.length;
        svg.path("M160," + branchLength * 105 / 2 + "L180," + branchLength * 105 / 2)
            .attr({stroke: "gray"});
        svg.path("M180," + branchLength * 105 / 2 + "L180," + 105 / 2 +
            "M180," + branchLength * 105 / 2 + "L" + "180," + 105 * (2 * branchLength - 1) / 2)
            .attr({stroke: "gray"});
        var path = "";
        BI.each(treeValue, function (i, v) {
            path = path + "M180," + (2 * i + 1) * 105 / 2 + "L200," + (2 * i + 1) * 105 / 2 +
                "M300," + (2 * i + 1) * 105 / 2 + "L340," + (2 * i + 1) * 105 / 2;
        });
        svg.path(path).attr({stroke: "gray"});
        BI.createWidget({
            type: "bi.absolute",
            element: this.relationTree,
            items: [{
                el: svg,
                top: 0,
                left: 0,
                right: 0,
                bottom: 0
            }]
        })
    },

    _createSelectDataMask: function (fieldId) {
        var self = this, maskId = BI.UUID();
        var mask = BI.Maskers.make(maskId, BICst.BODY_ELEMENT);
        BI.Maskers.show(maskId);
        var selectDataMask = BI.createWidget({
            type: "bi.select_data_with_mask",
            element: mask,
            field: this.options.field,
            fieldId: fieldId,
            maskId: maskId
        });
        selectDataMask.on(BI.SelectDataWithMask.EVENT_VALUE_CANCEL, function () {
            BI.Maskers.remove(maskId);
        });
        selectDataMask.on(BI.SelectDataWithMask.EVENT_CHANGE, function (v) {
            selectDataMask.destroy();
            BI.Maskers.remove(maskId);
            var treeValue = self.relationTree.getValue();
            BI.isEmptyString(treeValue[0]) && (treeValue = []);
            if (BI.isNotNull(fieldId)) {
                BI.remove(treeValue, function (index, item) {
                    return item.fieldId === fieldId;
                });
            }
            treeValue.push({
                fieldId: v.field_id,
                relationType: self.model.getRelationType(fieldId)
            });
            self._refreshTree(treeValue);
        });
    },

    _createRelationTree: function () {
        var self = this;
        var relationIds = this.model.getRelationIds();
        var relationChildren = [];
        BI.each(relationIds, function (i, rId) {
            relationChildren.push({
                fieldId: rId,
                relationType: self.model.getRelationType(rId),
                model: self.model
            });
        });
        this.relationTree = BI.createWidget({
            type: "bi.branch_tree",
            items: this._createBranchItems(relationChildren)
        });
        this._drawSVGLine();
        this.relationTree.on(BI.Controller.EVENT_CHANGE, function (type, clickType, fieldId) {
            switch (clickType) {
                case BI.RelationSettingTable.CLICK_GROUP:
                    self._refreshBaseTablePrimaryKeyIcon();
                    self.fireEvent(BI.RelationPane.EVENT_VALID);
                    break;
                case BI.RelationSettingTable.CLICK_TABLE:
                    self._createSelectDataMask(fieldId);
                    break;
                case BI.RelationSettingTable.CLICK_REMOVE:
                    var treeValue = self.relationTree.getValue();
                    self._refreshTree(treeValue);
                    break;
            }
        });
    },

    _createBranchItems: function (relationChildren) {
        this.baseTableField = BI.createWidget({
            type: "bi.relation_table_field_button",
            table_name: this.model.getTableNameByFieldId(this.model.getFieldId()),
            field_name: this.model.getFieldNameByFieldId(this.model.getFieldId()),
            field_id: this.model.getFieldId()
        });
        return [{
            el: {
                type: "bi.float_center_adapt",
                items: [this.baseTableField],
                width: 180
            },
            children: BI.createItems(relationChildren, {
                type: "bi.relation_setting_table",
                model: this.model
            })
        }];
    },

    _refreshTree: function (relationChildren) {
        if (BI.isNotEmptyArray(relationChildren)) {
            var empty = true;
            BI.each(relationChildren, function (i, v) {
                BI.isNotNull(v.relationType) && (empty = false);
            });
        }
        this.relationTree.populate(this._createBranchItems(relationChildren));
        this._drawSVGLine();
        if (this._checkAllRelationIsMatchingValid(relationChildren)) {
            this.fireEvent(BI.RelationPane.EVENT_VALID);
        } else {
            this.fireEvent(BI.RelationPane.EVENT_ERROR);
        }
        this._refreshBaseTablePrimaryKeyIcon();
    },

    _checkAllRelationIsMatchingValid: function (items) {
        if (items.length === 0) {
            return true;
        }
        return BI.isNotNull(BI.find(items, function (idx, item) {
            return BI.isNotNull(item.relationType);
        }))
    },

    _refreshBaseTablePrimaryKeyIcon: function () {
        var treeValue = this.relationTree.getValue();
        this.baseTableField.setPrimaryKeyIconVisible(BI.some(treeValue, function (i, item) {
            return item.relationType === BICst.RELATION_TYPE.ONE_TO_N ||
                item.relationType === BICst.RELATION_TYPE.ONE_TO_ONE;
        }));
    },

    getValue: function () {
        return this.model.getParsedRelation(this.relationTree.getValue());
    }
});
BI.RelationPane.EVENT_VALID = "EVENT_VALID";
BI.RelationPane.EVENT_ERROR = "EVENT_ERROR";
BI.RelationPane.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.relation_pane", BI.RelationPane);