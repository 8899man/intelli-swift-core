/**
 * Created by GameJian on 2016/3/14.
 */
BIShow.ImageWidgetModel = BI.inherit(BI.Model, {
    _defaultConfig: function() {
        return BI.extend(BIShow.ImageWidgetModel.superclass._defaultConfig.apply(this,arguments), {
            href: "",
            size: "original",
            src: ""
        });
    },

    _init: function(){
        BIShow.ImageWidgetModel.superclass._init.apply(this, arguments);
    },

    refresh: function(){

    },

    local: function(){
        return false;
    }
});