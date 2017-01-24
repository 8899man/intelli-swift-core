/**
 * 图表控件
 * @class BI.Chart
 * @extends BI.Widget
 */
BI.Chart = BI.inherit(BI.Pane, {

    _defaultConfig: function () {
        return BI.extend(BI.Chart.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-chart"
        })
    },

    _init: function () {
        BI.Chart.superclass._init.apply(this, arguments);
        var self = this, o = this.options;

        this.isSetOptions = false;
        var width = 0;
        var height = 0;

        this.vanCharts = VanCharts.init(self.element[0]);

        this._resizer = BI.debounce(function () {
            if (self.element.is(":visible") && self.vanCharts) {
                self.vanCharts.resize();
            }
        }, 30);
        BI.ResizeDetector.addResizeListener(this.element[0], function () {
            var newW = self.element.width(), newH = self.element.height();
            if (newW > 0 && newH > 0 && (width !== newW || height !== newH)) {
                self._resizer();
                width = newW;
                height = newH;
            }
        });
    },

    resize: function () {
        if (this.element.is(":visible") && this.isSetOptions === true) {
            this._resizer();
        }
    },

    magnify: function () {
        this.vanCharts.refreshRestore()
    },

    populate: function (items, options) {
        var self = this, o = this.options;
        o.items = items;
        this.config = options || {};
        this.config.series = o.items;

        var setOptions = function () {
            self.vanCharts.setOptions(self.config);
            self.isSetOptions = true;
        };
        BI.nextTick(setOptions);
    }
});
BI.Chart.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut('bi.chart', BI.Chart);