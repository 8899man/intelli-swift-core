BIShow.WidgetModel = BI.inherit(BI.Model, {
    _defaultConfig: function () {
        return BI.extend(BIShow.WidgetModel.superclass._defaultConfig.apply(this), {
            name: "",
            bounds: {},
            linkages: [],
            type: BICst.WIDGET.TABLE,
            dimensions: {},
            view: {},
            settings: {},
            _page_: {}  //一个只前台使用的当前page属性 不要随便拿了用
        })
    },

    change: function (changed, pre) {
        if (BI.has(changed, "detail")) {
            this.set(this.get("detail"), {
                // notrefresh: true
            });
        }
        //维度或指标改变时需要调节联动设置
        if (BI.has(changed, "dimensions")) {
            var dimensions = this.cat("dimensions");
            this.refresh();
        }
        if (BI.has(changed, "filter_value")) {
            this.refresh();
        }
    },

    refresh: function () {
        this.tmp({
            detail: {
                name: this.get("name"),
                dimensions: this.get("dimensions"),
                view: this.get("view"),
                type: this.get("type"),
                settings: this.get("settings"),
                filter_value: this.get("filter_value"),
                scopes: this.get("scopes")
            }
        }, {
            silent: true
        });
    },

    local: function () {
        return false;
    },

    _init: function () {
        BIShow.WidgetModel.superclass._init.apply(this, arguments);
    }
});