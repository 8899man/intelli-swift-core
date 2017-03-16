/**
 * Copyright (c) 2015-present, Alibaba Group Holding Limited.
 * All rights reserved.
 *
 */
'use strict';

var CSSPropertyOperations = require('react-dom/lib/CSSPropertyOperations');

// some number that react not auto add px
var numberTransformProperties = {
    translateX: true,
    translateY: true,
    translateZ: true
};

function processTransformValue(value, key) {
    if (numberTransformProperties[key] && typeof value === 'number') {
        value += 'px';
    }
    return value;
}

function convertTransform(style) {
    var result = {};
    let transform = '';
    for (var k in style) {
        if (k === 'transformMatrix') {

            transform += ( 'matrix3d(' + style[k].join(',') + ') ' );

        }
        if (k === 'transform') {
            let value = style[k];
            if (Array.isArray(value)) {

                var transformations = [];
                value.forEach(function (transformation) {

                    var key = Object.keys(transformation)[0];
                    var val = transformation[key];

                    // translate matrix have an array as the value
                    if (Array.isArray(val)) {

                        var len = val.length;

                        if ((key === 'matrix' && len === 16) || (key === 'translate' && len === 3)) {
                            key += '3d';
                        }

                        val = val.map(function (v) {
                            return processTransformValue(v, key);
                        }).join(',');

                    } else {
                        val = processTransformValue(val, key);
                    }

                    transformations.push(key + '(' + val + ')');
                });

                transform += transformations.join(' ');
            }
        } else {
            result[k] = style[k];
        }
    }

    if (transform) {
        result.transform = transform;
    }

    return result;
}

function setNativeProps(node, props, component) {

    for (var name in props) {
        if (name === 'style') {
            var style = props[name], s = {};
            if (!Array.isArray(style)) {
                style = [style];
            }
            for (var i = 0; i < style.length; i++) {
                s = {...s, ...style[i]};
            }
            if ('transformMatrix' in s || 'transform' in s) {
                s = convertTransform(s);
            }

            CSSPropertyOperations.setValueForStyles(node, s, component);
        } else {
            node.setAttribute(name, props[name]);
        }
    }
}

module.exports = setNativeProps;
