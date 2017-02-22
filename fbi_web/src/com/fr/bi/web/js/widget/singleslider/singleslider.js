/**
 * Created by zcf on 2016/9/22.
 */
BI.SingleSlider = BI.inherit(BI.Widget, {
    _constant: {
        EDITOR_WIDTH: 90,
        EDITOR_HEIGHT: 30,
        HEIGHT: 28,
        SLIDER_WIDTH_HALF: 15,
        SLIDER_WIDTH: 30,
        SLIDER_HEIGHT: 30,
        TRACK_HEIGHT: 24
    },
    _defaultConfig: function () {
        return BI.extend(BI.SingleSlider.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-single-slider bi-slider-track"
        });
    },
    _init: function () {
        BI.SingleSlider.superclass._init.apply(this, arguments);

        var self = this;
        var c = this._constant;
        this.enable = false;
        this.value = "";

        this.backgroundTrack = BI.createWidget({
            type: "bi.layout",
            cls: "background-track",
            height: c.TRACK_HEIGHT
        });
        this.grayTrack = BI.createWidget({
            type: "bi.layout",
            cls: "gray-track",
            height: 8
        });
        this.blueTrack = BI.createWidget({
            type: "bi.layout",
            cls: "blue-track",
            height: 8
        });
        this.track = this._createTrackWrapper();

        this.slider = BI.createWidget({
            type: "bi.single_slider_slider"
        });
        this.slider.element.draggable({
            axis: "x",
            containment: this.grayTrack.element,
            scroll: false,
            drag: function (e, ui) {
                var percent = (ui.position.left) * 100 / (self._getGrayTrackLength());
                self._setBlueTrack(percent);
                self._setLabelPosition(percent);
                var v = self._getValueByPercent(percent);
                self.label.setValue(v);
                self.value = v;
            },
            stop: function (e, ui) {
                var percent = (ui.position.left) * 100 / (self._getGrayTrackLength());
                self._setSliderPosition(percent);
                self.fireEvent(BI.SingleSlider.EVENT_CHANGE);
            }
        });
        var sliderVertical = BI.createWidget({
            type: "bi.vertical",
            items: [{
                type: "bi.absolute",
                items: [this.slider]
            }],
            hgap: c.SLIDER_WIDTH_HALF,
            height: c.SLIDER_HEIGHT
        });
        sliderVertical.element.click(function (e) {
            if (self.enable) {
                var offset = e.clientX - self.element.offset().left - c.SLIDER_WIDTH_HALF;
                var trackLength = self.track.element[0].scrollWidth;
                var percent = 0;
                if (offset < 0) {
                    percent = 0
                }
                if (offset > 0 && offset < (trackLength - c.SLIDER_WIDTH)) {
                    percent = offset * 100 / self._getGrayTrackLength();
                }
                if (offset > (trackLength - c.SLIDER_WIDTH)) {
                    percent = 100
                }
                self._setAllPosition(percent);
                var v = self._getValueByPercent(percent);
                self.label.setValue(v);
                self.value = v;
                self.fireEvent(BI.SingleSlider.EVENT_CHANGE);
            }
        });
        this.label = BI.createWidget({
            type: "bi.sign_editor",
            cls: "slider-editor-button",
            errorText: "",
            height: c.HEIGHT,
            width: c.EDITOR_WIDTH,
            allowBlank: false,
            validationChecker: function (v) {
                return self._checkValidation(v);
            },
            quitChecker: function (v) {
                return self._checkValidation(v);
            }
        });
        this.label.on(BI.SignEditor.EVENT_CONFIRM, function () {
            var percent = self._getPercentByValue(this.getValue());
            self._setAllPosition(percent);
            self.fireEvent(BI.SingleSlider.EVENT_CHANGE);
        });
        this._setVisible(false);
        BI.createWidget({
            type: "bi.absolute",
            element: this.element,
            items: [{
                el: {
                    type: "bi.vertical",
                    items: [{
                        type: "bi.absolute",
                        items: [{
                            el: this.track,
                            width: "100%",
                            height: c.TRACK_HEIGHT
                        }]
                    }],
                    hgap: 7,
                    height: c.TRACK_HEIGHT
                },
                top: 33,
                left: 0,
                width: "100%"
            }, {
                el: sliderVertical,
                top: 30,
                left: 0,
                width: "100%"
            }, {
                el: {
                    type: "bi.vertical",
                    items: [{
                        type: "bi.absolute",
                        items: [this.label]
                    }],
                    rgap: c.EDITOR_WIDTH,
                    height: c.EDITOR_HEIGHT
                },
                top: 0,
                left: 0,
                width: "100%"
            }]
        })
    },

    _createTrackWrapper: function () {
        return BI.createWidget({
            type: "bi.absolute",
            items: [{
                el: this.backgroundTrack,
                width: "100%"
            }, {
                el: {
                    type: "bi.vertical",
                    items: [{
                        type: "bi.absolute",
                        items: [{
                            el: this.grayTrack,
                            top: 0,
                            left: 0,
                            width: "100%"
                        }, {
                            el: this.blueTrack,
                            top: 0,
                            left: 0,
                            width: "0%"
                        }]
                    }],
                    hgap: 8,
                    height: 8
                },
                top: 8,
                left: 0,
                width: "100%"
            }]
        })
    },

    _checkValidation: function (v) {
        return !(BI.isNull(v) || v < this.min || v > this.max)
    },
    _setBlueTrack: function (percent) {
        this.blueTrack.element.css({"width": percent + "%"});
    },
    _setLabelPosition: function (percent) {
        this.label.element.css({"left": percent + "%"});
    },
    _setSliderPosition: function (percent) {
        this.slider.element.css({"left": percent + "%"});
    },
    _setAllPosition: function (percent) {
        this._setSliderPosition(percent);
        this._setLabelPosition(percent);
        this._setBlueTrack(percent);
    },
    _setVisible: function (visible) {
        this.slider.setVisible(visible);
        this.label.setVisible(visible);
    },
    _getGrayTrackLength: function () {
        return this.grayTrack.element[0].scrollWidth
    },
    _getValueByPercent: function (percent) {
        return (((this.max - this.min) * percent) / 100 + this.min);
    },
    _getPercentByValue: function (v) {
        return (v - this.min) * 100 / (this.max - this.min);
    },

    getValue: function () {
        return this.value;
    },

    setValue: function (v) {
        var value = BI.parseFloat(v);
        if ((!isNaN(value))) {
            if (this._checkValidation(value)) {
                this.value = value;
            }
            if (value > this.max) {
                this.value = this.max;
            }
            if (value < this.min) {
                this.value = this.min;
            }
        }
    },

    setMinAndMax: function (v) {
        var minNumber = BI.parseFloat(v.min);
        var maxNumber = BI.parseFloat(v.max);
        if ((!isNaN(minNumber)) && (!isNaN(maxNumber)) && (maxNumber > minNumber )) {
            this.min = minNumber;
            this.max = maxNumber;
        }
    },

    reset: function () {
        this._setVisible(false);
        this.enable = false;
        this._setBlueTrack(0);
    },

    populate: function () {
        if (!isNaN(this.min) && !isNaN(this.max)) {
            this._setVisible(true);
            this.enable = true;
            this.label.setErrorText(BI.i18nText("BI-Please_Enter") + this.min + "-" + this.max + BI.i18nText("BI-De") + BI.i18nText("BI-Number"));
            if (BI.isNumeric(this.value) || BI.isNotEmptyString(this.value)) {
                this.label.setValue(this.value);
                this._setAllPosition(this._getPercentByValue(this.value));
            } else {
                this.label.setValue(this.max);
                this._setAllPosition(100);
            }
        }
    }
});
BI.SingleSlider.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.single_slider", BI.SingleSlider);