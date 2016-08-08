/**
 * guy
 * 最基础的dom操作
 */
BI.extend(jQuery.fn, {

    destroy: function () {
        this.remove();
        if ($.browser.msie === true) {
            this[0].outerHTML = '';
        }
    },
    /**
     * 高亮显示
     * @param text 必需
     * @param keyword
     * @param py 必需
     * @returns {*}
     * @private
     */
    __textKeywordMarked__: function (text, keyword, py) {
        if (!BI.isKey(keyword)) {
            return this.text(text);
        }
        keyword = keyword + "";
        keyword = BI.toUpperCase(keyword);
        var textLeft = (text || "") + "";
        py = (py || BI.makeFirstPY(text)) + "";
        if (py != null) {
            py = BI.toUpperCase(py);
        }
        this.empty();
        while (true) {
            var tidx = BI.toUpperCase(textLeft).indexOf(keyword);
            var pidx = null;
            if (py != null) {
                pidx = py.indexOf(keyword);
                if (pidx >= 0) {
                    pidx = pidx % text.length;
                }
            }

            if (tidx >= 0) {
                this.append(textLeft.substr(0, tidx));
                this.append($("<span>").addClass("bi-keyword-red-mark")
                    .text(textLeft.substr(tidx, keyword.length)));

                textLeft = textLeft.substr(tidx + keyword.length);
                if (py != null) {
                    py = py.substr(tidx + keyword.length);
                }
            } else if (pidx != null && pidx >= 0 && Math.floor(pidx / text.length) === Math.floor((pidx + keyword.length - 1) / text.length)) {
                this.append(textLeft.substr(0, pidx));
                this.append($("<span>").addClass("bi-keyword-red-mark")
                    .text(textLeft.substr(pidx, keyword.length)));
                if (py != null) {
                    py = py.substr(pidx + keyword.length);
                }
                textLeft = textLeft.substr(pidx + keyword.length);
            } else {
                this.append(textLeft);
                break;
            }
        }

        return this;
    },

    getDomHeight: function (parent) {
        var clone = $(this).clone();
        clone.appendTo($(parent || "body"));
        var height = clone.height();
        clone.remove();
        return height;
    },

    //是否有竖直滚动条
    hasVerticalScroll: function () {
        return this.height() > 0 && this[0].clientWidth < this[0].offsetWidth;
    },

    //是否有水平滚动条
    hasHorizonScroll: function () {
        return this.width() > 0 && this[0].clientHeight < this[0].offsetHeight;
    },

    //获取计算后的样式
    getStyle: function (name) {
        var node = this[0];
        var computedStyle = void 0;

        // W3C Standard
        if (window.getComputedStyle) {
            // In certain cases such as within an iframe in FF3, this returns null.
            computedStyle = window.getComputedStyle(node, null);
            if (computedStyle) {
                return computedStyle.getPropertyValue(BI.hyphenate(name));
            }
        }
        // Safari
        if (document.defaultView && document.defaultView.getComputedStyle) {
            computedStyle = document.defaultView.getComputedStyle(node, null);
            // A Safari bug causes this to return null for `display: none` elements.
            if (computedStyle) {
                return computedStyle.getPropertyValue(BI.hyphenate(name));
            }
            if (name === 'display') {
                return 'none';
            }
        }
        // Internet Explorer
        if (node.currentStyle) {
            if (name === 'float') {
                return node.currentStyle.cssFloat || node.currentStyle.styleFloat;
            }
            return node.currentStyle[BI.camelize(name)];
        }
        return node.style && node.style[BI.camelize(name)];
    },

    __isMouseInBounds__: function (e) {
        var offset2Body = this.offset();
        return !(e.pageX < offset2Body.left || e.pageX > offset2Body.left + this.outerWidth()
        || e.pageY < offset2Body.top || e.pageY > offset2Body.top + this.outerHeight())
    },

    __hasZIndexMask__: function (zindex) {
        return zindex && this.zIndexMask[zindex] != null;
    },

    __buildZIndexMask__: function (zindex, domArray) {
        this.zIndexMask = this.zIndexMask || {};//存储z-index的mask
        this.indexMask = this.indexMask || [];//存储mask
        var mask = BI.createWidget({
            type: "bi.center_adapt",
            cls: "bi-z-index-mask",
            items: domArray
        });

        mask.element.css({"z-index": zindex});
        BI.createWidget({
            type: "bi.absolute",
            element: this,
            items: [{
                el: mask,
                left: 0,
                right: 0,
                top: 0,
                bottom: 0
            }]
        });
        this.indexMask.push(mask);
        zindex && (this.zIndexMask[zindex] = mask);
        return mask.element;
    },

    __releaseZIndexMask__: function (zindex) {
        if (zindex && this.zIndexMask[zindex]) {
            this.indexMask.remove(this.zIndexMask[zindex]);
            this.zIndexMask[zindex].destroy();
            return;
        }
        this.indexMask = this.indexMask || [];
        var indexMask = this.indexMask.pop();
        indexMask && indexMask.destroy();
    }
});

BI.extend(jQuery, {

    getLeftPosition: function (combo, popup, extraWidth) {
        return {
            top: combo.element.offset().top,
            left: combo.element.offset().left - popup.element.outerWidth() - (extraWidth || 0)
        };
    },

    getRightPosition: function (combo, popup, extraWidth) {
        var el = combo.element;
        return {
            top: el.offset().top,
            left: el.offset().left + el.outerWidth() + (extraWidth || 0)
        }
    },

    getTopPosition: function (combo, popup, extraHeight) {
        return {
            top: combo.element.offset().top - popup.element.outerHeight() - (extraHeight || 0),
            left: combo.element.offset().left
        };
    },

    getBottomPosition: function (combo, popup, extraHeight) {
        var el = combo.element;
        return {
            top: el.offset().top + el.outerHeight() + (extraHeight || 0),
            left: el.offset().left
        };
    },

    isLeftSpaceEnough: function (combo, popup, extraWidth) {
        return $.getLeftPosition(combo, popup, extraWidth).left >= 0;
    },

    isRightSpaceEnough: function (combo, popup, extraWidth) {
        var viewBounds = popup.element.bounds(), windowBounds = $("body").bounds();
        return $.getRightPosition(combo, popup, extraWidth).left + viewBounds.width <= windowBounds.width;
    },

    isTopSpaceEnough: function (combo, popup, extraHeight) {
        return $.getTopPosition(combo, popup, extraHeight).top >= 0;
    },

    isBottomSpaceEnough: function (combo, popup, extraHeight) {
        var viewBounds = popup.element.bounds(), windowBounds = $("body").bounds();
        return $.getBottomPosition(combo, popup, extraHeight).top + viewBounds.height <= windowBounds.height;
    },

    isRightSpaceLarger: function (combo) {
        var windowBounds = $("body").bounds();
        return windowBounds.width - combo.element.offset().left - combo.element.bounds().width >= combo.element.offset().left;
    },

    isBottomSpaceLarger: function (combo) {
        var windowBounds = $("body").bounds();
        return windowBounds.height - combo.element.offset().top - combo.element.bounds().height >= combo.element.offset().top;
    },

    getLeftAlignPosition: function (combo, popup, extraWidth) {
        var viewBounds = popup.element.bounds(), windowBounds = $("body").bounds();
        var left = combo.element.offset().left;
        if (left + viewBounds.width + extraWidth > windowBounds.width) {
            left = windowBounds.width - viewBounds.width - extraWidth;
        }
        if (left < 0) {
            left = 0;
        }
        return {
            left: left
        }
    },

    getLeftAdaptPosition: function (combo, popup, extraWidth) {
        if ($.isLeftSpaceEnough(combo, popup, extraWidth)) {
            return $.getLeftPosition(combo, popup, extraWidth);
        }
        return {
            left: 0
        }
    },

    getRightAlignPosition: function (combo, popup, extraWidth) {
        var comboBounds = combo.element.bounds(), viewBounds = popup.element.bounds();
        var left = combo.element.offset().left + comboBounds.width - viewBounds.width - extraWidth;
        if (left < 0) {
            left = 0;
        }
        return {
            left: left
        }
    },

    getRightAdaptPosition: function (combo, popup, extraWidth) {
        if ($.isRightSpaceEnough(combo, popup, extraWidth)) {
            return $.getRightPosition(combo, popup, extraWidth);
        }
        return {
            left: $("body").bounds().width - popup.element.bounds().width - extraWidth
        }
    },

    getTopAlignPosition: function (combo, popup, extraHeight, needAdaptHeight) {
        var comboOffset = combo.element.offset();
        var comboBounds = combo.element.bounds(), popupBounds = popup.element.bounds(), windowBounds = $("body").bounds();
        var top, adaptHeight;
        if ($.isBottomSpaceEnough(combo, popup, -1 * comboBounds.height + extraHeight)) {
            top = comboOffset.top + extraHeight;
        } else if (needAdaptHeight) {
            top = comboOffset.top + extraHeight;
            adaptHeight = windowBounds.height - top - extraHeight;
        } else {
            top = windowBounds.height - popupBounds.height - extraHeight;
            if (top < 0) {
                adaptHeight = windowBounds.height - extraHeight;
            }
        }
        if (top < 0) {
            top = 0;
        }
        return adaptHeight ? {
            top: top,
            adaptHeight: adaptHeight
        } : {
            top: top
        }
    },

    getTopAdaptPosition: function (combo, popup, extraHeight, needAdaptHeight) {
        var popupBounds = popup.element.bounds(), windowBounds = $("body").bounds();
        if ($.isTopSpaceEnough(combo, popup, extraHeight)) {
            return $.getTopPosition(combo, popup, extraHeight);
        }
        if (needAdaptHeight) {
            return {
                top: 0,
                adaptHeight: combo.element.offset().top - extraHeight
            }
        }
        if (popupBounds.height + extraHeight > windowBounds.height) {
            return {
                top: 0,
                adaptHeight: windowBounds.height - extraHeight
            }
        }
        return {
            top: 0
        }
    },

    getBottomAlignPosition: function (combo, popup, extraHeight, needAdaptHeight) {
        var comboOffset = combo.element.offset();
        var comboBounds = combo.element.bounds(), popupBounds = popup.element.bounds(), windowBounds = $("body").bounds();
        var top, adaptHeight;
        if ($.isTopSpaceEnough(combo, popup, -1 * comboBounds.height + extraHeight)) {
            top = comboOffset.top + comboBounds.height - popupBounds.height - extraHeight;
        } else if (needAdaptHeight) {
            top = 0;
            adaptHeight = comboOffset.top + comboBounds.height - extraHeight;
        } else {
            top = 0;
            if (popupBounds.height + extraHeight > windowBounds.height) {
                adaptHeight = windowBounds.height - extraHeight;
            }
        }
        if (top < 0) {
            top = 0;
        }
        return adaptHeight ? {
            top: top,
            adaptHeight: adaptHeight
        } : {
            top: top
        }
    },

    getBottomAdaptPosition: function (combo, popup, extraHeight, needAdaptHeight) {
        var comboOffset = combo.element.offset();
        var comboBounds = combo.element.bounds(), popupBounds = popup.element.bounds(), windowBounds = $("body").bounds();
        if ($.isBottomSpaceEnough(combo, popup, extraHeight)) {
            return $.getBottomPosition(combo, popup, extraHeight);
        }
        if (needAdaptHeight) {
            return {
                top: comboOffset.top + extraHeight,
                adaptHeight: windowBounds.height - comboOffset.top - comboBounds.height - extraHeight
            }
        }
        if (popupBounds.height + extraHeight > windowBounds.height) {
            return {
                top: 0,
                adaptHeight: windowBounds.height - extraHeight
            }
        }
        return {
            top: windowBounds.height - popupBounds.height - extraHeight
        }
    },

    getComboPositionByDirections: function (combo, popup, extraWidth, extraHeight, needAdaptHeight, directions) {
        extraWidth || (extraWidth = 0);
        extraHeight || (extraHeight = 0);
        var space = "", i, direct;
        var leftRight = [], topBottom = [];
        var isNeedAdaptHeight = false;
        var left, top, pos;
        for (i = 0; i < directions.length; i++) {
            direct = directions[i];
            switch (direct) {
                case "left":
                    leftRight.push(direct);
                    break;
                case "right":
                    leftRight.push(direct);
                    break;
                case "top":
                    topBottom.push(direct);
                    break;
                case "bottom":
                    topBottom.push(direct);
                    break;
            }
        }
        for (i = 0; i < directions.length; i++) {
            direct = directions[i];
            switch (direct) {
                case "left":
                    if (!isNeedAdaptHeight && $.isLeftSpaceEnough(combo, popup, extraWidth)) {
                        left = $.getLeftPosition(combo, popup, extraWidth).left;
                        if (topBottom[0] === "bottom") {
                            pos = $.getTopAlignPosition(combo, popup, extraHeight, needAdaptHeight);
                            pos.left = left;
                            return pos;
                        }
                        pos = $.getBottomAlignPosition(combo, popup, extraHeight, needAdaptHeight);
                        pos.left = left;
                        return pos;
                    }
                    break;
                case "right":
                    if (!isNeedAdaptHeight && $.isRightSpaceEnough(combo, popup, extraWidth)) {
                        left = $.getRightPosition(combo, popup, extraWidth).left;
                        if (topBottom[0] === "bottom") {
                            pos = $.getTopAlignPosition(combo, popup, extraHeight, needAdaptHeight);
                            pos.left = left;
                            return pos;
                        }
                        pos = $.getBottomAlignPosition(combo, popup, extraHeight, needAdaptHeight);
                        pos.left = left;
                        return pos;
                    }
                    break;
                case "top":
                    if ($.isTopSpaceEnough(combo, popup, extraHeight)) {
                        top = $.getTopPosition(combo, popup, extraWidth).top;
                        if (leftRight[0] === "right") {
                            pos = $.getLeftAlignPosition(combo, popup, extraWidth, needAdaptHeight);
                            pos.top = top;
                            return pos;
                        }
                        pos = $.getRightAlignPosition(combo, popup, extraWidth);
                        pos.top = top;
                        return pos;
                    }
                    if (needAdaptHeight) {
                        isNeedAdaptHeight = true;
                    }
                    break;
                case "bottom":
                    if ($.isBottomSpaceEnough(combo, popup, extraHeight)) {
                        top = $.getBottomPosition(combo, popup, extraWidth).top;
                        if (leftRight[0] === "right") {
                            pos = $.getLeftAlignPosition(combo, popup, extraWidth, needAdaptHeight);
                            pos.top = top;
                            return pos;
                        }
                        pos = $.getRightAlignPosition(combo, popup, extraWidth);
                        pos.top = top;
                        return pos;
                    }
                    if (needAdaptHeight) {
                        isNeedAdaptHeight = true;
                    }
                    break;
            }
        }

        switch (directions[0]) {
            case "left":
            case "right":
                if ($.isRightSpaceLarger(combo)) {
                    left = $.getRightAdaptPosition(combo, popup, extraWidth).left;
                } else {
                    left = $.getLeftAdaptPosition(combo, popup, extraWidth).left;
                }
                if (topBottom[0] === "bottom") {
                    pos = $.getTopAlignPosition(combo, popup, extraHeight, needAdaptHeight);
                    pos.left = left;
                    return pos;
                }
                pos = $.getBottomAlignPosition(combo, popup, extraHeight, needAdaptHeight);
                pos.left = left;
                return pos;
            default :
                if ($.isBottomSpaceLarger(combo)) {
                    top = $.getBottomAdaptPosition(combo, popup, extraHeight, needAdaptHeight).top;
                } else {
                    top = $.getTopAdaptPosition(combo, popup, extraHeight, needAdaptHeight).top;
                }
                if (leftRight[0] === "right") {
                    pos = $.getLeftAlignPosition(combo, popup, extraWidth, needAdaptHeight);
                    pos.top = top;
                    return pos;
                }
                pos = $.getRightAlignPosition(combo, popup, extraWidth);
                pos.top = top;
                return pos;
        }
    },


    getComboPosition: function (combo, popup, extraHeight, needAdaptHeight) {
        extraHeight || (extraHeight = 0);
        var maxHeight = popup.attr("maxHeight") || $("body").bounds().height - extraHeight;
        popup.resetHeight && popup.resetHeight(maxHeight);
        return $.getComboPositionByDirections(combo, popup, 0, extraHeight, needAdaptHeight, ['bottom', 'top', 'right', 'left'])
    },

    /**
     **获取相对目标的左边平行位置
     **
     */
    getComboLeftPosition: function (combo, popup, extraWidth, extraHeight) {
        extraWidth || (extraWidth = 0);
        extraHeight || (extraHeight = 0);
        var adjustWidth = 0, position;
        if ($.isLeftSpaceEnough(combo, popup, extraWidth, adjustWidth)) {
            position = $.getLeftPosition(combo, popup, extraWidth, adjustWidth);
        } else {
            position = $.getRightPosition(combo, popup, extraWidth, adjustWidth);
        }
        if (!$.isBottomSpaceEnough(combo, popup, extraHeight, 0)) {
            position.top = Math.min($("body").bounds().height - popup.element.outerHeight(), position.top);
        }
        return position;
    },
    getComboTopLeftPosition: function (combo, popup, extraWidth, extraHeight) {
        extraWidth || (extraWidth = 0);
        extraHeight || (extraHeight = 0);
        var adjustWidth = 0, position;
        if ($.isLeftSpaceEnough(combo, popup, extraWidth, adjustWidth)) {
            position = $.getLeftPosition(combo, popup, extraWidth, adjustWidth);
        } else {
            position = $.getRightPosition(combo, popup, extraWidth, adjustWidth);
        }
        if (!$.isTopSpaceEnough(combo, popup, -1 * combo.element.outerHeight(), extraHeight)) {
            position.top = 0;
        } else {
            position.top = $.getTopPosition(combo, popup, extraWidth).top + combo.element.outerHeight() - extraHeight;
        }
        return position;
    },
    /**
     **获取相对目标的右边平行位置
     **
     */
    getComboRightPosition: function (combo, popup, extraWidth, extraHeight) {
        extraWidth || (extraWidth = 0);
        extraHeight || (extraHeight = 0);
        var adjustWidth = 0, position;
        if ($.isRightSpaceEnough(combo, popup, extraWidth, adjustWidth)) {
            position = $.getRightPosition(combo, popup, extraWidth, adjustWidth);
        } else {
            position = $.getLeftPosition(combo, popup, extraWidth, adjustWidth);
        }
        if (!$.isBottomSpaceEnough(combo, popup, extraHeight, 0)) {
            position.top = Math.min($("body").bounds().height - popup.element.outerHeight(), position.top);
        }
        return position;
    },
    getComboTopRightPosition: function (combo, popup, extraWidth, extraHeight) {
        extraWidth || (extraWidth = 0);
        extraHeight || (extraHeight = 0);
        var adjustWidth = 0, position;
        if ($.isRightSpaceEnough(combo, popup, extraWidth, adjustWidth)) {
            position = $.getRightPosition(combo, popup, extraWidth, adjustWidth);
        } else {
            position = $.getLeftPosition(combo, popup, extraWidth, adjustWidth);
        }
        if (!$.isTopSpaceEnough(combo, popup, -1 * combo.element.outerHeight(), extraHeight)) {
            position.top = 0;
        } else {
            position.top = $.getTopPosition(combo, popup).top + combo.element.outerHeight() - extraHeight;
            if (position.top < 0) {
                position.top = 0;
            }
        }
        return position;
    },
    /**
     *获取下拉框的位置 combo:trigger popup: 下拉图, extraHeight:调整的高度
     * needAdaptHeight : 是否需要调整下拉框的高度  false,
     *
     * @return {
     * topPosition :   0,
     * leftPosition: 0,
     * //adaptHeight: 220,
     * }
     */
    //getComboPosition: function (combo, popup, needAdaptHeight, extraHeight, offsetStyle) {
    //    extraHeight = extraHeight || 0;
    //    var el = combo.element, popEl = popup.element,
    //        el_offset = el.offset(), view_offset = popEl.offset(),
    //        comboBound = el.bounds(), viewBound = popEl.bounds(), windowBounds = $("body").bounds(),
    //        leftPosition = el_offset.left, topPosition, windowHeight = windowBounds.height,
    //        maxHeight = Math.min(windowHeight, popup.attr("maxHeight") || windowHeight), currentHeight, viewHeight;
    //
    //    if ((leftPosition + viewBound.width) > windowBounds.width) {
    //        leftPosition = windowBounds.width - viewBound.width;
    //        if (leftPosition < 0) {
    //            leftPosition = 0;
    //        }
    //    }
    //    if ("center" === offsetStyle) {
    //        leftPosition = el_offset.left + (comboBound.width - viewBound.width) / 2;
    //        if (leftPosition < 0) {
    //            leftPosition = 0;
    //        }
    //    }
    //    if ("right" === offsetStyle) {
    //        leftPosition = el_offset.left + comboBound.width - viewBound.width;
    //        if (leftPosition < 0) {
    //            leftPosition = 0;
    //        }
    //    }
    //    if (needAdaptHeight === true) {
    //        popup.resetHeight && popup.resetHeight(maxHeight);
    //        currentHeight = Math.min(maxHeight, popEl.outerHeight());
    //        viewHeight = currentHeight + extraHeight;
    //        currentHeight = maxHeight;
    //    } else {
    //        viewHeight = viewBound.height + extraHeight;
    //    }
    //
    //    if (el_offset.top + el.outerHeight() + viewHeight < windowHeight) {
    //        topPosition = el_offset.top + el.outerHeight() + extraHeight;
    //    } else if (el_offset.top - viewHeight > 0 || !needAdaptHeight) {
    //        topPosition = el_offset.top - viewHeight - extraHeight;
    //    } else {
    //        //如果上面下面都放不下的话, 则比较下面和下面哪个地方剩下的位置大
    //        if (el_offset.top > windowHeight - ( el_offset.top + el.outerHeight() )) {
    //            topPosition = 0;
    //            currentHeight = el_offset.top - extraHeight;
    //        } else {
    //            topPosition = el_offset.top + el.outerHeight() + extraHeight;
    //            currentHeight = windowHeight - el_offset.top - el.outerHeight() - extraHeight;
    //        }
    //    }
    //
    //    var ob = {
    //        left: leftPosition,
    //        top: topPosition
    //    };
    //
    //    if (currentHeight != null) {
    //        ob.adaptHeight = currentHeight;
    //    }
    //
    //    return ob;
    //},

    getComboTopPosition: function (combo, popup, needAdaptHeight, extraHeight, offsetStyle) {
        extraHeight = extraHeight || 0;
        var el = combo.element, popEl = popup.element,
            el_offset = el.offset(), view_offset = popEl.offset(),
            comboBound = el.bounds(), viewBound = popEl.bounds(), windowBounds = $("body").bounds(),
            leftPosition = el_offset.left, topPosition, windowHeight = windowBounds.height,
            maxHeight = Math.min(windowHeight, popup.attr("maxHeight") || windowHeight), currentHeight, viewHeight;

        if ((leftPosition + viewBound.width) > windowBounds.width) {
            leftPosition = windowBounds.width - viewBound.width;
            if (leftPosition < 0) {
                leftPosition = 0;
            }
        }
        if ("center" === offsetStyle) {
            leftPosition = el_offset.left + (comboBound.width - viewBound.width) / 2;
            if (leftPosition < 0) {
                leftPosition = 0;
            }
        }
        if ("right" === offsetStyle) {
            leftPosition = el_offset.left + comboBound.width - viewBound.width;
            if (leftPosition < 0) {
                leftPosition = 0;
            }
        }
        if (needAdaptHeight === true) {
            popup.resetHeight && popup.resetHeight(maxHeight);
            currentHeight = Math.min(maxHeight, popEl.outerHeight());
            viewHeight = currentHeight + extraHeight;
            currentHeight = maxHeight;
        } else {
            viewHeight = viewBound.height + extraHeight;
        }

        if (el_offset.top - viewHeight > 0) {
            topPosition = el_offset.top - viewHeight - extraHeight;
        } else if (el_offset.top + el.outerHeight() + viewHeight < windowHeight || !needAdaptHeight) {
            topPosition = el_offset.top + el.outerHeight() + extraHeight;
        } else {
            //如果上面下面都放不下的话, 则比较下面和下面哪个地方剩下的位置大
            if (el_offset.top > windowHeight - ( el_offset.top + el.outerHeight() )) {
                topPosition = 0;
                currentHeight = el_offset.top - extraHeight;
            } else {
                topPosition = el_offset.top + el.outerHeight() + extraHeight;
                currentHeight = windowHeight - el_offset.top - el.outerHeight() - extraHeight;
            }
        }

        var ob = {
            left: leftPosition,
            top: topPosition
        };

        if (currentHeight != null) {
            ob.adaptHeight = currentHeight;
        }

        return ob;
    }
});