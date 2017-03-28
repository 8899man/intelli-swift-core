/**
 * Created by 小灰灰 on 2016/3/10.
 */
BI.ETLFilterGroupPopup = BI.inherit(BI.BarPopoverSection, {
    _constants:{
        NORTH_HEIGHT : 50,
        LABEL_HEIGHT : 30,
        REGION_WIDTH : 260,
        REGION_HEIGHT : 360,
        LIST_HEIGHT : 330,
        WEST_LEFT : 20,
        CENTER_LEFT : 300
    },

    _init: function () {
        BI.ETLFilterGroupPopup.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        self.storedValue = [];
        BI.each(o.value, function (i ,item) {
            if (BI.find(o[ETLCst.FIELDS], function (i, field) {
                    if (field.fieldName === item){
                        return true;
                    }
                }) && item !== o.field){
                self.storedValue.push(item)
            }
        });
        var items = [];
        BI.each(self.storedValue, function (i, item) {
            items.push({
                text : item,
                value : item,
                title : item,
                selected : true
            });
        })
        BI.each(o[ETLCst.FIELDS], function (i, item) {
            if (BI.indexOf(self.storedValue, item.fieldName) === -1 && item.fieldName !== o.field){
                items.push({text : item.fieldName, value : item.fieldName, title : item.fieldName});
            }
        })
        self.list = BI.createWidget({
            type : 'bi.etl_group_sortable_list',
            items : items,
            height : self._constants.LIST_HEIGHT
        });
        self.list.on(BI.ETLGroupSortableList.EVENT_CHANGE, function(){
            self.storedValue = self.list.getValue();
            self._afterListChanged();
        })
        self.labels = BI.createWidget({
            type : 'bi.vertical',
            cls : 'detail-view',
            lgap : 10,
            height : self._constants.LIST_HEIGHT
        });
    },

    _afterListChanged : function(){
        var self = this, o = this.options;
        self.labels.clear();
        if (self.storedValue.length !== 0){
            BI.each(self.storedValue, function (i, item) {
                self.labels.addItem(BI.createWidget({
                    type : 'bi.label',
                    textAlign : 'left',
                    height : 25,
                    text : BI.i18nText('BI-ETL_Group_Field_Name_Same', item) + (i === self.storedValue.length - 1 ? BI.i18nText('BI-Relation_In') : '')
                }))
            })
            self.labels.addItem(BI.createWidget({
                type : 'bi.label',
                textAlign : 'left',
                height : 25,
                text : BI.i18nText('BI-Basic_De')  + o.targetText
            }))
        } else {
            self.labels.addItem(BI.createWidget({
                type : 'bi.label',
                textAlign : 'left',
                height : 25,
                text : o.targetText
            }))
        }
    },

    rebuildNorth: function (north) {
        var self = this, o = this.options;
        BI.createWidget({
            type: "bi.label",
            element: north,
            text: o.title || BI.i18nText("BI-ETL_Filter_Group_Setting"),
            textAlign: "left",
            height: self._constants.NORTH_HEIGHT
        });
        return true
    },

    _createList: function () {
        var self = this;
        return BI.createWidget({
            type : 'bi.vertical',
            cls : 'bi-etl-filter-group-pop-list',
            width : self._constants.REGION_WIDTH,
            height : self._constants.REGION_HEIGHT,
            scrolly : false,
            items : [
                {
                    el : BI.createWidget({
                        type : 'bi.label',
                        text : BI.i18nText('BI-Group_Detail_Setting'),
                        cls : 'first-label',
                        textAlign : 'center',
                        height : self._constants.LABEL_HEIGHT
                    })
                },
                {
                    el : self.list
                }
            ]
        });
    },
    _createShowBoard: function () {
        var self = this;
        return BI.createWidget({
            type : 'bi.vertical',
            width : self._constants.REGION_WIDTH,
            height : self._constants.REGION_HEIGHT,
            cls :'bi-etl-filter-group-pop-detail',
            scrolly : false,
            items : [
                {
                    el : BI.createWidget({
                        type : 'bi.label',
                        cls : 'detail-label',
                        text : BI.i18nText('BI-Group_Detail_Short'),
                        textAlign : 'left',
                        height : self._constants.LABEL_HEIGHT
                    })
                },
                {
                    el : self.labels
                }
            ]
        });
    },
    rebuildCenter: function (center) {
        var self = this;
        BI.createWidget({
            type : 'bi.absolute',
            element : center,
            items : [
                {
                    el : self._createList(),
                    left : self._constants.WEST_LEFT
                },
                {
                    el : self._createShowBoard(),
                    left : self._constants.CENTER_LEFT
                }
            ]
        })
    },

    getValue: function () {
        return this.storedValue;
    },

    end: function(){
        this.fireEvent(BI.ETLFilterGroupPopup.EVENT_CHANGE);
    },

    populate: function () {
        this._afterListChanged();
    }

});
BI.ETLFilterGroupPopup.EVENT_CHANGE = "EVENT_CHANGE";
BI.shortcut("bi.etl_filter_group_popup", BI.ETLFilterGroupPopup);