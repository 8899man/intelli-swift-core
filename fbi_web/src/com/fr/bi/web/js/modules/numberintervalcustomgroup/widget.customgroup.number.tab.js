/**
 * @class BI.NumberIntervalCustomGroupTab
 * @extend BI.Widget
 */

BI.NumberIntervalCustomGroupTab = BI.inherit(BI.Widget,{

    constants:{
        itemHeight:25,
        addButtonHeight:30,
        intervalHeight:40,
        editorWidth:155,
        tgap:10,
        hgap:10,
        textWidth:85
    },

    _defaultConfig:function(){
        return BI.extend(BI.NumberIntervalCustomGroupTab.superclass._defaultConfig.apply(this,arguments),{
            baseCls:"bi-number-custom-group-tab",
            tab:{

            }
        });
    },

    _init:function(){
        BI.NumberIntervalCustomGroupTab.superclass._init.apply(this,arguments);

        var self = this,o = this.options;

        this.tab = BI.createWidget({
            direction: "custom",
            type: "bi.tab",
            element: this.element,
            tab: o.tab,
            cardCreator: BI.bind(this._createTabs, this)
        });

        this.tab.on(BI.Tab.EVENT_CHANGE, function(){
            self._checkState();
        });
        
        this.tab.setSelect(BI.NumberIntervalCustomGroupCombo.Type_Auto);
    },

    _checkState: function(){
        switch (this.tab.getSelect()) {
            case BI.NumberIntervalCustomGroupCombo.Type_Custom:
                this.panel && (this.panel.isEmptyPanel() ? this.fireEvent(BI.NumberIntervalCustomGroupTab.EVENT_EMPTY_GROUP) :
                    (this.panel.isValid() ? this.fireEvent(BI.NumberIntervalCustomGroupTab.EVENT_VALID) : this.fireEvent(BI.NumberIntervalCustomGroupTab.EVENT_ERROR)));
                break;
            case BI.NumberIntervalCustomGroupCombo.Type_Auto:
                if(BI.isNull(this.space)){
                    this._getGeneMinMax();
                    this.space = (BI.parseFloat(this.genMax.sub(this.genMin)).div(5));
                    this.editor.setValue(this.space);
                }
                this.fireEvent(BI.NumberIntervalCustomGroupTab.EVENT_VALID);
                break;
        }
    },

    _createPanel:function(){
        var self = this;
        this.panel = BI.createWidget({
            type:"bi.number_custom_group_panel"
        });
        this.panel.on(BI.NumberIntervalCustomGroupPanel.EVENT_ERROR,function(){
            self.fireEvent(BI.NumberIntervalCustomGroupTab.EVENT_ERROR);
        });

        this.panel.on(BI.NumberIntervalCustomGroupPanel.EVENT_VALID,function(){
            self.fireEvent(BI.NumberIntervalCustomGroupTab.EVENT_VALID);
        });

        this.panel.on(BI.NumberIntervalCustomGroupPanel.EVENT_EMPTY_GROUP,function(){
            self.fireEvent(BI.NumberIntervalCustomGroupTab.EVENT_EMPTY_GROUP);
        });
        this.panel.populate(this._createItems());
    },

    _createTabs : function(v){
        var self = this;
        switch (v){
            case BI.NumberIntervalCustomGroupCombo.Type_Custom:
                this._createPanel();
                this.other = BI.createWidget({
                    type:"bi.number_custom_group_other"
                });
                return BI.createWidget({
                    type:"bi.absolute",
                    items:[{
                        el:{
                            type:"bi.vertical",
                            items:[this.panel,{
                                el:this.other,
                                tgap:this.constants.tgap
                            }]
                        },
                        top:0,
                        left:0,
                        right:0
                    }]
                });
            case BI.NumberIntervalCustomGroupCombo.Type_Auto:
                this.editor = BI.createWidget({
                    type:"bi.editor",
                    cls:"group-tab-editor",
                    height:this.constants.addButtonHeight,
                    validationChecker: function (v) {
                        if (!BI.isNumeric(v) || BI.parseFloat(v) > self.max || BI.parseFloat(v) <= 0) {
                            return false;
                        }
                        return true;
                    }
                });
                this.editor.on(BI.Editor.EVENT_CONFIRM, function(){
                    self.space = this.getValue();
                });
                return BI.createWidget({
                    type:"bi.htape",
                    items:[{
                        el:{
                            type:"bi.label",
                            cls:"group-tab-label",
                            textAlign:"left",
                            height:this.constants.itemHeight,
                            text:BI.i18nText("BI-Interval_Span")
                        },
                        width:this.constants.textWidth
                    },this.editor]
                });
        }
    },

    _createItems:function(nodes){
        if(BI.isNull(nodes)){
            var current = this.genMin;
            var items = [];
            while(current + this.space < this.genMax){
                items.push({
                    type:"bi.number_custom_group_item",
                    id: BI.UUID(),
                    group_name:current + "-" + (current + this.space),
                    height:this.constants.intervalHeight,
                    min:current,
                    max:current + this.space,
                    closemin:true,
                    closemax:false
                });
                current += this.space;
            }
            if(current <= this.genMax){
                items.push({
                    type:"bi.number_custom_group_item",
                    id: BI.UUID(),
                    group_name:current + "-" + this.genMax,
                    height:this.constants.intervalHeight,
                    min:current,
                    max:this.genMax,
                    closemin:true,
                    closemax:true
                });
            }
            return items;
        }

        BI.each(nodes,function(idx,node){
            if(!BI.has(node,"max")){
                node["max"] = "";
            }
            if(!BI.has(node,"closemax")){
                if(idx === nodes.length - 1){
                    node["closemax"] = true;
                }else{
                    node["closemax"] = true;
                }
            }
            if(!BI.has(node,"min")){
                node["min"] = "";
            }
            if(!BI.has(node,"closemin")){
                node["closemin"] = true;
            }
        });
        return BI.createItems(nodes,{
            type:"bi.number_custom_group_item",
            height:this.constants.intervalHeight
        });
    },

    _checkMagnifyCount: function (number) {
        var numText = number + "";
        var dotText = numText.split(".")[0];
        return dotText.length;
    },

    _getGeneMinMax:function(){
        var self = this;
        var min = Math.abs(this.min) + "";
        var max = Math.abs(this.max) + "";
        var minCount = this._checkMagnifyCount(min);
        var maxCount = this._checkMagnifyCount(max);
        var count = minCount > maxCount ? minCount : maxCount;
        var magnify = 1;
        //缩小补零
        var s = "0.";
        while (count - minCount > 0) {
            s += "0";
            minCount++;
        }
        min = min.replace(".", "");
        min = s + min;
        s = "0.";
        while (count - maxCount > 0) {
            s += "0";
            maxCount++;
        }
        max = max.replace(".", "");
        max = s + max;

        //后面补零对齐
        var zeros = max.length - min.length;
        if(zeros > 0){
            while (zeros-- > 0) {
                min += "0";
            }
        }else{
            while (zeros++ < 0) {
                max += "0";
            }
        }
        //截零
        var i = max.length - 1, add = "0.";
        while (min[i] === "0" && max[i] === "0" && this.min != 0 && this.max != 0) {
            i--;
        }

        //截位/截位+1
        min = this.min < 0 ? -(cutBig(min)) : cutSmall(min);
        max = this.max < 0 ? -(cutSmall(max)) : cutBig(max);

        while (count-- > 0) {
            magnify *= 10;
        }

        //(max - min) / 5
        this.genMin = min.mul(magnify);
        this.genMax = max.mul(magnify);
        //return BI.parseFloat(this.genMax.sub(this.genMin)).div(5);

        function cutSmall(val){
            return BI.parseFloat(val.substring(0, i));
        }

        function  cutBig(val){
            if(val[i] === "0"){
                return BI.parseFloat(val);
            }
            val = val.substring(0, i);
            var length = val.length - 2;
            while (--length > 0) {
                add += "0";
            }
            add += "1";
            if(val[i-1] === "."){
                return BI.parseFloat(val) + 1;
            }
            return BI.parseFloat(val) + BI.parseFloat(add);
        }
    },

    getValue:function(){
        var group = {};
        var value = group["group_value"] = {};
        switch (this.tab.getSelect()){
            case BI.NumberIntervalCustomGroupCombo.Type_Custom:
                group["type"] = BI.NumberIntervalCustomGroupTab.Type_Group_Custom;
                value["group_nodes"] = this.panel.getValue();
                if(this.other.isValid()){
                    value["use_other"] = this.other.getValue();
                }
                break;
            case BI.NumberIntervalCustomGroupTab.Type_Group_Auto:
                group["type"] = BI.NumberIntervalCustomGroupTab.Type_Group_Auto;
                value["group_interval"] = this.space;
                value["min"] = this.min;
                value["max"] = this.max;
                break;
        }
        return group;
    },

    populate:function(configs){
        var self = this, o = this.options;
        if(BI.isNull(configs)){
            return;
        }
        Data.BufferPool.getNumberFieldMinMaxValueById(BI.Utils.getFieldIDByDimensionID(o.dId), function(value){
            self.max = self.genMax = BI.parseInt(value.max);
            self.min = self.genMin = BI.parseInt(value.min);
            var config = configs.group_value;
            switch (configs.type) {
                case BICst.NUMBER_INTERVAL_CUSTOM_GROUP_CUSTOM:
                    self.tab.setSelect(BI.NumberIntervalCustomGroupTab.Type_Group_Custom);
                    self.panel && self.panel.populate(self._createItems(BI.isNull(config) ? [] : (BI.isNull(config.group_nodes) ? [] : config.group_nodes)));
                    self.other && self.other.setValue(BI.isNull(config) ? config : config.use_other);
                    break;
                case BICst.NUMBER_INTERVAL_CUSTOM_GROUP_AUTO:
                default :
                    self.tab.setSelect(BI.NumberIntervalCustomGroupTab.Type_Group_Auto);
                    self._getGeneMinMax();
                    self.space = (BI.isNull(config) || BI.isNull(config.group_interval)) ? (BI.parseFloat(self.genMax.sub(self.genMin)).div(5)) : BI.parseFloat(config.group_interval);
                    self.editor && self.editor.setValue(self.space);
                    self.panel && self.panel.populate(self._createItems());
                    break;
            }
        });
    }
});

BI.extend(BI.NumberIntervalCustomGroupTab,{
    Type_Group_Custom:BICst.NUMBER_INTERVAL_CUSTOM_GROUP_CUSTOM,
    Type_Group_Auto:BICst.NUMBER_INTERVAL_CUSTOM_GROUP_AUTO
});

BI.NumberIntervalCustomGroupTab.EVENT_ERROR = "EVENT_ERROR";
BI.NumberIntervalCustomGroupTab.EVENT_EMPTY_GROUP = "EVENT_EMPTY_GROUP";
BI.NumberIntervalCustomGroupTab.EVENT_VALID = "EVENT_VALID";

$.shortcut("bi.number_custom_group_tab",BI.NumberIntervalCustomGroupTab);