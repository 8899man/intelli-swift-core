ComplexCanvasView = BI.inherit(BI.View, {
    _defaultConfig: function () {
        return BI.extend(ComplexCanvasView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-mvc-complex-canvas bi-mvc-layout"
        })
    },

    _init: function () {
        ComplexCanvasView.superclass._init.apply(this, arguments);
    },

    _render: function (vessel) {

        var canvas = BI.createWidget({
            type: "bi.complex_canvas",
            width: 500,
            height: 600
        });
        canvas.branch(55, 100, 10, 10, 100, 10, 200, 10, {
            offset: 20,
            strokeStyle: "red",
            lineWidth: 2
        });

        canvas.branch(220, 155, 120, 110, 150, 200, {
            offset: 40
        });

        canvas.stroke();

        BI.createWidget({
            type: "bi.absolute",
            element: vessel,
            items: [{
                el: canvas,
                left: 100,
                top: 50
            }]
        })
    }
});

ComplexCanvasModel = BI.inherit(BI.Model, {});