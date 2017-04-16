/**
 * Created by Young's on 2017/4/16.
 */
BI.AnalysisTableRename = BI.inherit(BI.BarPopoverSection, {
    _constants: {
        NORTH_HEIGHT: 50,
        EDITOR_WIDTH: 260,
        EDITOR_HEIGHT: 28,
        LABEL_WIDTH: 100,
        LABEL_HEIGHT: 30,
        VGAP: 20,
        HGAP: 10
    },

    props: {
        renameChecker: BI.emptyFn
    },

    _init: function () {
        BI.AnalysisTableRename.superclass._init.apply(this, arguments);
    },

    rebuildNorth: function (north) {
        var self = this, o = this.options;
        BI.createWidget({
            type: "bi.label",
            element: north,
            text: BI.i18nText('BI-Basic_Rename'),
            textAlign: "left",
            height: self._constants.NORTH_HEIGHT
        });
        return true
    },

    rebuildCenter: function (center) {
        var self = this, o = this.options;
        self.name = BI.createWidget({
            type: "bi.sign_editor",
            width: self._constants.EDITOR_WIDTH,
            height: self._constants.EDITOR_HEIGHT,
            cls: "rename-input",
            allowBlank: false,
            errorText: function (v) {
                if (v === "") {
                    return BI.i18nText("BI-Report_Name_Not_Null");
                } else {
                    return BI.i18nText("BI-Template_Name_Already_Exist");
                }
            },
            validationChecker: function (v) {
                if (v.indexOf("\'") > -1) {
                    return false;
                }
                return o.renameChecker.call(this, v);
            }
        });

        this.name.on(BI.SignEditor.EVENT_ERROR, function (v) {
            if (BI.isEmptyString(v)) {
                self.sure.setWarningTitle(BI.i18nText("BI-Report_Name_Not_Null"));
            } else {
                self.sure.setWarningTitle(BI.i18nText("BI-Template_Name_Already_Exist"));
            }
            self.sure.setEnable(false);
        });
        this.name.on(BI.SignEditor.EVENT_VALID, function () {
            self.sure.setEnable(true);
        });

        BI.createWidget({
            type: "bi.vertical",
            cls: "bi-etl-rename-center",
            element: center,
            items: [{
                type: "bi.left",
                items: [{
                    type: "bi.label",
                    text: BI.i18nText("BI-Table_Name") + ' :',
                    height: self._constants.LABEL_HEIGHT,
                    width: self._constants.LABEL_WIDTH,
                    textAlign: "left",
                    cls: "rename-label",
                    hgap: self._constants.HGAP
                }, this.name]
            }],
            vgap: self._constants.VGAP
        })
    },

    end: function () {
        this.fireEvent(BI.AnalysisTableRename.EVENT_CHANGE, this.name.getValue());
    },

    setTemplateNameFocus: function () {
        this.name.focus();
    },

    populate: function (name) {
        this.name.setValue(name);
    }

});
BI.AnalysisTableRename.EVENT_CHANGE = "EVENT_CHANGE";
BI.shortcut("bi.analysis_table_rename", BI.AnalysisTableRename);