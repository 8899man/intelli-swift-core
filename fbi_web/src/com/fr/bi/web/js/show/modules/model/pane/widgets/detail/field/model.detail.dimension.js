/**
 * @class BIShow.DetailDimensionModel
 * @extend BI.Model
 *
 */
BIShow.DetailDimensionModel = BI.inherit(BI.Model, {
    _defaultConfig: function(){
        return BI.extend(BIShow.DetailDimensionModel.superclass._defaultConfig.apply(this, arguments), {

        });
    },

    _init: function(){
        BIShow.DetailDimensionModel.superclass._init.apply(this, arguments);
    },

    change: function(changed){
        if(BI.has(changed, "name")){
            var name = this.get("name");
            var hyperlink = this.get("hyperlink");
            if(BI.isNotNull(hyperlink)){
                var expression = hyperlink.expression;
                hyperlink.expression = expression.replaceAll("\\$\\{.*\\}", "${"+ name +"}");
                this.set("hyperlink", hyperlink);
            }
        }

    },

    local: function(){
        if(this.has("valueChange")){
            var value = this.get("valueChange");
            this.set("value", value);
            return true;
        }
        return false;
    }
});