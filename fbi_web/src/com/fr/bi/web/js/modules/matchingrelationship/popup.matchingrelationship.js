/**
 * 匹配关系弹出层
 *
 * @class BI.MatchingRelationPopup
 * @extends BI.BarPopoverSection
 */

BI.MatchingRelationPopup = BI.inherit(BI.BarPopoverSection, {

    _defaultConfig: function () {
        return BI.extend(BI.MatchingRelationPopup.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-matching-relation-info-popup",
            dId: ""
        });
    },

    _init: function () {
        BI.MatchingRelationPopup.superclass._init.apply(this, arguments);
    },

    _setCompleteButtonState: function(v){
        this.complete.setEnable(v);
        if(v === true){
            this.complete.setWarningTitle("");
        }else{
            this.complete.setWarningTitle(BI.i18nText("BI-Please_Select_Multi_Path"));
        }
    },

    rebuildNorth: function (north) {
        var o = this.options;
        var name = BI.Utils.getDimensionNameByID(o.dId);
        BI.createWidget({
            type: "bi.label",
            element: north,
            text: BI.i18nText("BI-De_Field_Relation", name),
            height: 50,
            textAlign: "left",
            lgap: 10
        });
        return true;
    },

    rebuildCenter: function (center) {
        var self = this;
        this.matchTab = BI.createWidget({
            type: "bi.matching_relationship_tab",
            dId: this.options.dId,
            element: center
        });
        this.matchTab.on(BI.MatchingRelationShipTab.EVENT_CHANGE, function (v) {
            self._setCompleteButtonState(!BI.isEmptyObject(v));
            self._showPreConfig();
        });
        this.matchTab.on(BI.MatchingRelationShipTab.EVENT_COMPLETE_VISIABLE_CHANGE, function(v){
            self._setCompleteButtonState(!!v);
        });
        return true;
    },

    rebuildSouth: function (south) {
        var self = this;
        this.save = BI.createWidget({
            type: 'bi.button',
            text: BI.i18nText("BI-Save"),
            height: 30,
            value: 0,
            handler: function (v) {
                self.end();
                self.close(v);
            }
        });
        this.cancel = BI.createWidget({
            type: 'bi.button',
            text: BI.i18nText("BI-Cancel"),
            height: 30,
            value: 1,
            level: 'ignore',
            handler: function () {
                self.close();
            }
        });
        this.back = BI.createWidget({
            type: 'bi.button',
            text: BI.i18nText("BI-Back_Step"),
            height: 30,
            value: 1,
            level: 'ignore',
            handler: function () {
                self.matchTab.setSelect(BI.MatchingRelationShipTab.INFO_PANE);
                self._showSaveConfig();
            }
        });
        this.complete = BI.createWidget({
            type: 'bi.button',
            text: BI.i18nText("BI-Complete_Select"),
            height: 30,
            value: 1,
            handler: function () {
                var value = self.matchTab.getValue();
                self.matchTab.setSelect(BI.MatchingRelationShipTab.INFO_PANE);
                self._showSaveConfig();
                self.matchTab.populate(value);
            }
        });
        this._showSaveConfig();
        BI.createWidget({
            type: 'bi.left_right_vertical_adapt',
            element: south,
            rhgap: 5,
            lhgap: 5,
            items: {
                left: [this.back],
                right: [this.cancel, this.save, this.complete]
            }
        });
    },

    _showSaveConfig: function () {
        this.save.setVisible(true);
        this.cancel.setVisible(true);
        this.back.setVisible(false);
        this.complete.setVisible(false);
    },

    _showPreConfig: function () {
        this.save.setVisible(false);
        this.cancel.setVisible(false);
        this.back.setVisible(true);
        this.complete.setVisible(true);
    },

    populate: function () {
        this.matchTab.populate();
    },

    end: function () {
        this.fireEvent(BI.MatchingRelationPopup.EVENT_CHANGE, this.matchTab.getValue());
    }
});
BI.MatchingRelationPopup.EVENT_CHANGE = "MatchingRelationPopup.EVENT_CHANGE";
$.shortcut('bi.matching_relation_popup', BI.MatchingRelationPopup);