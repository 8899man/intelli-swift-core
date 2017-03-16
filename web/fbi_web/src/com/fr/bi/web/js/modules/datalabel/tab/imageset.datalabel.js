/**
 * Created by Fay on 2016/7/7.
 */
BI.DataLabelImageSet = BI.inherit(BI.Widget, {
    _defaultImg: [
        "defaultimage-01.png",
        "defaultimage-02.png",
        "defaultimage-03.png",
        "defaultimage-04.png",
        "defaultimage-05.png",
        "defaultimage-06.png",
        "defaultimage-07.png",
        "defaultimage-08.png",
        "defaultimage-09.png",
        "defaultimage-10.png",
        "defaultimage-11.png",
        "defaultimage-12.png",
        "defaultimage-13.png",
        "defaultimage-14.png",
        "defaultimage-15.png",
        "defaultimage-16.png",
        "defaultimage-17.png",
        "defaultimage-18.png",
        "defaultimage-19.png",
        "defaultimage-20.png",
        "defaultimage-21.png",
        "defaultimage-22.png",
        "defaultimage-23.png",
        "defaultimage-24.png",
        "defaultimage-25.png",
        "defaultimage-26.png",
        "defaultimage-27.png"
    ],

    _imageSelect: "",

    _defaultConfig: function () {
        var conf = BI.DataLabelImageSet.superclass._defaultConfig.apply(this, arguments);
        return BI.extend(conf, {
            baseCls: "bi-image-set",
            defaultSelect: 1
        });
    },

    _init: function () {
        BI.DataLabelImageSet.superclass._init.apply(this, arguments);
        var o = this.options;
        this.wId = BI.Utils.getWidgetIDByDimensionID(o.dId);
        this._img = BI.Utils.getImagesByWidgetID(this.wId);
        this._createTab();
        this.tabs.setSelect(o.defaultSelect);
    },

    _createTab: function () {
        var tab = BI.createWidget({
            type: "bi.button_group",
            cls: "image-set-tab",
            items: [{
                type: "bi.single_select_item",
                text: BI.i18nText("BI-Default_Image"),
                value: 1,
                cls: "image-set-tab-item",
                height: 30
            }, {
                type: "bi.single_select_item",
                text: BI.i18nText("BI-Custom_Image"),
                value: 2,
                cls: "image-set-tab-item",
                height: 30
            }],
            width: 380,
            height: 30,
            layouts: [{
                type: "bi.left_vertical_adapt",
                items: [{
                    el: {
                        type: "bi.horizontal"
                    }
                }]
            }]
        });
        this.tabs = BI.createWidget({
            direction: "custom",
            element: this.element,
            type: "bi.tab",
            tab: tab,
            cardCreator: BI.bind(this._createPanel, this)
        });

        BI.createWidget({
            type: "bi.absolute",
            element: this.element,
            items: [{
                el: tab,
                left: 0,
                top: 115
            }],
            width: 380,
            height: 145
        })
    },

    _createPanel: function (v) {
        switch (v) {
            case 1:
                return this._createPanelOne();
            case 2:
                return this._createPanelTwo();
        }
    },

    _createPanelOne: function () {
        this.imgs = this._createDefaultImgs();
        return BI.createWidget({
            type: "bi.vertical",
            items: [this.imgs]
        })
    },

    _createPanelTwo: function () {
        var header = this._createHeader();
        this.imgs = this._createImgs();
        return BI.createWidget({
            type: "bi.vertical",
            items: [header, {
                el: this.imgs
            }]
        })
    },

    _createHeader: function () {
        var self = this, o = this.options;
        var headerLabel = BI.createWidget({
            type: "bi.label",
            text: BI.i18nText("BI-Added"),
            cls: "header-label"
        });
        var headerButton = BI.createWidget({
            type: "bi.button",
            cls: "button-ignore",
            text: BI.i18nText("BI-Upload_Image"),
            width: 70,
            height: 26,
            hgap: 5
        });
        var image = BI.createWidget({
            type: "bi.multifile_editor",
            accept: "*.jpg;*.png;*.gif;"
        });
        headerButton.on(BI.Button.EVENT_CHANGE, function () {
            image.select();
        });
        image.on(BI.MultifileEditor.EVENT_CHANGE, function () {
            this.upload();
        });
        image.on(BI.MultifileEditor.EVENT_UPLOADED, function () {
            var files = this.getValue();
            var file = files[files.length - 1];
            var attachId = file.attach_id, fileName = file.filename;
            var src = attachId + "_" + fileName;
            BI.requestAsync("fr_bi_base", "save_upload_image", {
                attach_id: attachId
            }, function () {
                var button = BI.createWidget({
                    type: "bi.data_label_image_button",
                    src: src,
                    width: 50,
                    height: 35,
                    iconWidth: 14,
                    iconHeight: 14
                });
                button.on(BI.DataLabelImageButton.EVENT_CHANGE, function (src) {
                    self._imageSelect = src;
                    self.fireEvent(BI.DataLabelImageSet.EVENT_CHANGE, arguments);
                });
                button.on(BI.DataLabelImageButton.DELETE_IMAGE, function () {
                    self.refreshImg();
                    BI.Broadcasts.send(BICst.BROADCAST.IMAGE_LIST_PREFIX + self.wId, self._img);
                });
                self.imageGroup.prependItems([button]);
                self.refreshImg();
                BI.Broadcasts.send(BICst.BROADCAST.IMAGE_LIST_PREFIX + self.wId, self._img);
            });
        });
        var header = BI.createWidget({
            type: "bi.center_adapt",
            cls: "image-set-header",
            items: [{
                type: "bi.left",
                items: [headerLabel],
                lgap: 6
            }, {
                type: 'bi.right',
                items: [headerButton],
                rgap: 6
            }],
            width: 380,
            height: 35
        });
        return header;
    },

    _createDefaultImgs: function () {
        var self = this, result = [];
        BI.each(this._defaultImg, function (i, src) {
            var img = {
                type: "bi.image_button",
                width: 49,
                height: 50,
                handler: function () {
                    self._imageSelect = src;
                    self.fireEvent(BI.DataLabelImageSet.EVENT_CHANGE, arguments);
                }
            };
            img.src = BI.Func.getCompleteImageUrl(src);
            result.push(img);
        });
        var imgs = BI.createWidget({
            type: "bi.inline",
            cls: "image-group",
            items: result,
            hgap: 2,
            tgap: 5
        });
        return BI.createWidget({
            type: "bi.vertical",
            items: [imgs],
            height: 110
        })
    },

    _createImgs: function () {
        this.imageGroup = BI.createWidget({
            type: "bi.button_group",
            cls: "image-group",
            items: this.convert2Images(BI.Utils.getImagesByWidgetID(this.wId)),
            width: 380,
            layouts: [{
                type: "bi.inline",
                hgap: 2,
                vgap: 2
            }]
        });
        return BI.createWidget({
            type: "bi.vertical",
            items: [this.imageGroup],
            height: 80
        });
    },

    convert2Images: function (items) {
        var self = this, o = this.options, result = [];
        BI.each(items, function (i, item) {
            var button = BI.createWidget({
                type: "bi.data_label_image_button",
                src: item,
                width: 50,
                height: 35,
                iconWidth: 14,
                iconHeight: 14
            });
            button.on(BI.DataLabelImageButton.EVENT_CHANGE, function (src) {
                self._imageSelect = src;
                self.fireEvent(BI.DataLabelImageSet.EVENT_CHANGE, arguments);
            });
            button.on(BI.DataLabelImageButton.DELETE_IMAGE, function () {
                self.refreshImg();
                BI.Broadcasts.send(BICst.BROADCAST.IMAGE_LIST_PREFIX + self.wId, self._img);
            });
            result.push(button)
        });
        return result;
    },

    refreshImg: function () {
        var self = this;
        this._img = [];
        BI.each(self.imageGroup.getAllButtons(), function (i, image) {
            self._img.push(image.getSrc());
        });
    },

    populate: function () {
        var img = BI.Utils.getImagesByWidgetID(this.wId);
        if(!BI.isEqual(this._img, img)) {
            this._img = img;
            this.imageGroup && this.imageGroup.populate(this.convert2Images(this._img));
        }
    },

    setValue: function (v) {
        v || (v = {});
        this._imageSelect = v.src || "";
    },

    getValue: function () {
        return {
            src: this._imageSelect
        };
    }
});
BI.DataLabelImageSet.EVENT_CHANGE = "BI.DataLabelImageSet.EVENT_CHANGE";
$.shortcut("bi.data_label_image_set", BI.DataLabelImageSet);