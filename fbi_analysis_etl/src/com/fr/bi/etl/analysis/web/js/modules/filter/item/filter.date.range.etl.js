BI.ETLDateRangePane = BI.inherit(BI.Single, {
    _constants: {
        height: 30,
        width: 30,
        gap : 15,
        timeErrorCls: "time-error",
        DATE_MIN_VALUE: "1900-01-01",
        DATE_MAX_VALUE: "2099-12-31"
    },
    _defaultConfig: function () {
        var conf = BI.ETLDateRangePane.superclass._defaultConfig.apply(this, arguments);
        return BI.extend(conf, {
            extraCls: "bi-filter-time-interval"
        })
    },
    _init: function () {
        var self = this;
        BI.ETLDateRangePane.superclass._init.apply(this, arguments);
        this.up = this._createCombo();
        this.down = this._createCombo();
        BI.createWidget({
            element: this,
            type: "bi.vertical",
            scrolly : false,
            items: [{
                el: this.up,
                tgap : this._constants.gap
            }, {
                type: "bi.label",
                text: "|",
                cls: "date-range-link",
                textAlign: "center",
                height: this._constants.gap
            }, {
                el: this.down,
                tgap : 5
            }]
        });
    },

    _createCombo: function () {
        var self = this;
        var combo = BI.createWidget({
            type: 'bi.date_filter_combo_etl',
            height: this._constants.height
        });
        combo.on(BI.ETLDateFilterCombo.EVENT_ERROR, function () {
            self._clearTitle();
            self.element.removeClass(self._constants.timeErrorCls);
        });
        combo.on(BI.ETLDateFilterCombo.EVENT_BEFORE_POPUPVIEW, function () {
            self.up.hidePopupView();
            self.down.hidePopupView();
        });
        combo.on(BI.ETLDateFilterCombo.EVENT_CHANGE, function () {
            var smallDate = self.up.getValue(), bigDate = self.down.getValue();
            if (self._compare(smallDate, bigDate)) {
                self._setTitle(BI.i18nText("BI-Time_Interval_Error_Text"));
                self.element.addClass(self._constants.timeErrorCls);
            } else {
                self._clearTitle();
                self.element.removeClass(self._constants.timeErrorCls);
            }
        });

        combo.on(BI.ETLDateFilterCombo.EVENT_VALID, function () {
            var smallDate = self.up.getValue(), bigDate = self.down.getValue();
            if (self._compare(smallDate, bigDate)) {
                self._setTitle(BI.i18nText("BI-Time_Interval_Error_Text"));
                self.element.addClass(self._constants.timeErrorCls);
            } else {
                self._clearTitle();
                self.element.removeClass(self._constants.timeErrorCls);
                self.fireEvent(BI.ETLDateRangePane.EVENT_CHANGE);
            }
        });
        return combo;
    },

    _compare: function (smallDate, bigDate) {
       return smallDate > bigDate;
    },
    _setTitle: function (v) {
        this.up.setTitle(v);
        this.down.setTitle(v);
    },
    _clearTitle: function () {
        this.up.setTitle("");
        this.down.setTitle("");
    },
    setValue: function (date) {
        if (BI.isNotNull(date)){
            if (BI.isNotNull(date.start)){
                this.up.setValue(date.start);
            }
            if (BI.isNotNull(date.end)){
                this.down.setValue(date.end);
            }
        }
    },
    getValue: function () {
        return {start: this.up.getValue(), end: this.down.getValue()};
    }
});
BI.ETLDateRangePane.EVENT_CHANGE = "EVENT_CHANGE";
BI.shortcut("bi.date_range_pane_etl", BI.ETLDateRangePane);
