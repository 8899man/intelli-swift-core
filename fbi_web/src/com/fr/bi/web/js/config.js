//工程配置
$(function () {
    var isSupportFlex = BI.isSupportCss3("flex");
    BI.Plugin.registerWidget("bi.horizontal", function (ob) {
        if (isSupportFlex) {
            return BI.extend({}, ob, {type: "bi.flex_horizontal"});
        } else {
            return ob;
        }
    });
    BI.Plugin.registerWidget("bi.center_adapt", function (ob) {
        if (isSupportFlex && ob.items && ob.items.length <= 1) {
            //有滚动条的情况下需要用到flex_wrapper_center布局
            if (ob.scrollable === true || ob.scrollx === true || ob.scrolly === true) {
                //不是IE用flex_wrapper_center布局
                if (!BI.isIE()) {
                    return BI.extend({}, ob, {type: "bi.flex_wrapper_center"});
                }
                return ob;
            }
            return BI.extend({}, ob, {type: "bi.flex_center"});
        } else {
            return ob;
        }
    });
    BI.Plugin.registerWidget("bi.vertical_adapt", function (ob) {
        if (isSupportFlex) {
            //有滚动条的情况下需要用到flex_wrapper_center布局
            if (ob.scrollable === true || ob.scrollx === true || ob.scrolly === true) {
                //不是IE用flex_wrapper_center布局
                if (!BI.isIE()) {
                    return BI.extend({}, ob, {type: "bi.flex_wrapper_vertical_center"});
                }
                return ob;
            }
            return BI.extend({}, ob, {type: "bi.flex_vertical_center"});
        } else {
            return ob;
        }
    });
    BI.Plugin.registerWidget("bi.float_center_adapt", function (ob) {
        if (isSupportFlex) {
            //有滚动条的情况下需要用到flex_wrapper_center布局
            if (ob.scrollable === true || ob.scrollx === true || ob.scrolly === true) {
                //不是IE用flex_wrapper_center布局
                if (!BI.isIE()) {
                    return BI.extend({}, ob, {type: "bi.flex_wrapper_center"});
                }
                return ob;
            }
            return BI.extend({}, ob, {type: "bi.flex_center"});
        } else {
            return ob;
        }
    });
});