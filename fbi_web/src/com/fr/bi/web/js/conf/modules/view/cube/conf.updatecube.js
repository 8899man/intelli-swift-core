/**
 * @class BIConf.UpdateCubePaneView
 * @extend BI.View
 * Cube更新界面
 */
BIConf.UpdateCubePaneView = BI.inherit(BI.View, {
    _defaultConfig: function () {
        return BI.extend(BIConf.UpdateCubePaneView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-update-cube-pane"
        })
    },

    _init: function () {
        BIConf.UpdateCubePaneView.superclass._init.apply(this, arguments);
    },

    _render: function (vessel) {
        BI.createWidget({
            type: "bi.vertical",
            element: vessel,
            items: [{
                type: "bi.cube_path"
            },
                this._buildImmediateButton(),
                this._buildTimeSetting(),
                this._buildLog()
            ],
            hgap: 20,
            vgap: 20
        })
    },

    _buildImmediateButton: function () {
        var self = this;
        this.immediateButton = BI.createWidget({
            type: "bi.button",
            text: BI.i18nText("BI-Immediate_Update_DataBase"),
            height: 28,
            handler: function () {
                self._setImmediateButtonStatus(false);
                self.cubeLog.setStart();
                BI.Utils.generateCube(function (data) {
                    if (data.result) {
                        self._createCheckInterval();
                    } else {
                        self._setImmediateButtonStatus(true);
                    }
                });
            }
        });

        return BI.createWidget({
            type: "bi.left",
            items: [this.immediateButton],
            height: 30
        })
    },

    _checkCubeStatus: function () {
        var self = this;
        this.update({
            noset: true,
            success: function (data) {
                if (BI.isNotNull(data.cube_end) || (BI.isNull(data.cube_end) && BI.isNull(data.cube_start))) {
                    self._setImmediateButtonStatus(true);
                    self._clearCheckInterval();
                } else {
                    self._setImmediateButtonStatus(false);
                }
                self.cubeLog.refreshLog(data);
            }
        });
    },

    _createCheckInterval: function () {
        var self = this;
        this.interval = setInterval(function () {
            self._checkCubeStatus();
        }, 2000)
    },

    _clearCheckInterval: function () {
        var self = this;
        if (undefined != self.interval) {
            clearInterval(self.interval);
        }
    },

    _buildTimeSetting: function () {
        return BI.createWidget({
            type: "bi.global_update_setting"
        })
    },

    _buildLog: function () {
        return this.cubeLog = BI.createWidget({
            type: "bi.cube_log"
        })

    },

    _setImmediateButtonStatus: function (isAvailable) {
        var self = this;
        if (isAvailable) {
            self.immediateButton.setEnable(true);
            self.immediateButton.setText(BI.i18nText("BI-Immediate_Update_DataBase"));
        } else {
            self.immediateButton.setEnable(false);
            self.immediateButton.setText(BI.i18nText("BI-Cube_is_Generating"));
        }
    },

    refresh: function () {
        this._checkCubeStatus();
        this._createCheckInterval();
    },

    local: function () {
        if (this.model.has("immediateUpdate")) {
            this.model.get("immediateUpdate");
            return true;
        }
        return false;
    }
});
