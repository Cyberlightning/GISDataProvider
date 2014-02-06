(function () {
    "use strict";
    XML3D.debug.loglevel = 0;

    /**
     * Registering JS Operators
     *
     */

    Xflow.registerOperator("xflow.desaturateImage", {
        outputs: [
            {type: 'texture', name: 'output', sizeof: 'input'}
        ],
        params: [
            {type: 'texture', source: 'input'}
        ],

        platform: Xflow.PLATFORM.JAVASCRIPT,

        evaluate: function (output, input) {
            var s = input.data;
            var d = output.data;
            for (var i = 0; i < s.length; i += 4) {
                // CIE luminance        (HSI Intensity: Averaging three channels)
                d[i] = d[i + 1] = d[i + 2] = 0.2126 * s[i] + 0.7152 * s[i + 1] + 0.0722 * s[i + 2];
                d[i + 3] = s[i + 3];
            }
            return true;
        }
    });


    Xflow.registerOperator("xflow.thresholdImage", {
        outputs: [
            {type: 'texture', name: 'output', sizeof: 'input'}
        ],
        params: [
            {type: 'texture', source: 'input'},
            {type: 'int', source: 'threshold'}
        ],

        platform: Xflow.PLATFORM.JAVASCRIPT,

        evaluate: function (output, input, threshold) {
            var s = input.data,
                d = output.data;

            for (var i = 0; i < s.length; i += 4) {
                d[i] = d[i + 1] = d[i + 2] = ((s[i] > threshold[0]) ? 0 : 255);
                d[i + 3] = s[i + 3];
            }

            return true;
        }
    });

    function floatToByte(f) {
        f = Math.max(0.0, Math.min(1.0, f));
        return Math.floor(f === 1.0 ? 255 : f * 256.0);
    }

    function byteToFloat(b) {
        b = Math.max(0, Math.min(255, b));
        return b === 255 ? 1.0 : b / 256.0;
    }

    Xflow.registerOperator("xflow.blurImage", {
        outputs: [
            {type: 'texture', name: 'output', sizeof: 'input'}
        ],
        params: [
            {type: 'texture', source: 'input'},
            {type: 'int', source: 'blurSize'}
        ],

        platform: Xflow.PLATFORM.JAVASCRIPT,

        evaluate: function (output, input, blurSize) {
            var s = input.data;
            var sLen = s.length;
            var d = output.data;
            var m = [0.05, 0.09, 0.12, 0.15, 0.16, 0.15, 0.12, 0.09, 0.05];
            var sumR = 0;
            var sumG = 0;
            var sumB = 0;
            var currentCoord = 0;
            var i, j;

            for (i = 0; i < sLen; i += 4) {
                for (j = 0; j < 9; j++) {
                    currentCoord = (i - (16 - j * 4) * blurSize[0]);
                    if (currentCoord >= 0 && currentCoord <= sLen) {
                        sumR += byteToFloat(s[currentCoord]) * m[j];
                        sumG += byteToFloat(s[currentCoord + 1]) * m[j];
                        sumB += byteToFloat(s[currentCoord + 2]) * m[j];
                    }
                }

                d[i] = floatToByte(sumR);
                d[i + 1] = floatToByte(sumG);
                d[i + 2] = floatToByte(sumB);
                d[i + 3] = 255;
                sumR = sumG = sumB = 0;
            }

            return true;
        }
    });


    /**
     * Registering WebCL Operators
     *
     */


    Xflow.registerOperator("xflow.thresholdImage", {
        outputs: [
            {type: 'texture', name: 'result', sizeof: 'image'}
        ],
        params: [
            {type: 'texture', source: 'image'},
            {type: 'int', source: 'threshold'}
        ],

        platform: Xflow.PLATFORM.CL,

        evaluate: [
            "int color = image[image_i].x;",
            "if (color > threshold)",
            "{",
            "color=0;",
            "}else{",
            "color=255;",
            "}",
            "result[image_i] = (uchar4)(color, color, color, 255);"
        ]
    });


    Xflow.registerOperator("xflow.desaturateImage", {
        outputs: [
            {type: 'texture', name: 'result', sizeof: 'image'}
        ],
        params: [
            {type: 'texture', source: 'image'}
        ],

        platform: Xflow.PLATFORM.CL,

        evaluate: [
            "uchar4 color = image[image_i];",
            "uchar lum = (uchar)(0.30f * color.x + 0.59f * color.y + 0.11f * color.z);",
            "result[image_i] = (uchar4)(lum, lum, lum, 255);"
        ]
    });


    Xflow.registerOperator("xflow.blurImage", {
        outputs: [
            {type: 'texture', name: 'result', sizeof: 'image'}
        ],
        params: [
            {type: 'texture', source: 'image'},
            {type: 'int', source: 'blurSize'}
        ],

        platform: Xflow.PLATFORM.CL,

        evaluate: [
            "const float m[9] = {0.05f, 0.09f, 0.12f, 0.15f, 0.16f, 0.15f, 0.12f, 0.09f, 0.05f};",
            "float3 sum = {0.0f, 0.0f, 0.0f};",
            "uchar3 resultSum;",
            "int currentCoord;",

            "for(int j = 0; j < 9; j++) {",
            "currentCoord = convert_int(image_i - (4-j)*blurSize);",
            "if(currentCoord >= 0 || currentCoord <= image_width * image_height) {",
            "sum.x += convert_float_rte(image[currentCoord].x) * m[j];",
            "sum.y += convert_float_rte(image[currentCoord].y) * m[j];",
            "sum.z += convert_float_rte(image[currentCoord].z) * m[j];",
            "}",
            "}",

            "resultSum = convert_uchar3_rte(sum);",
            "result[image_i] = (uchar4)(resultSum.x, resultSum.y, resultSum.z, 255);",
        ]
    });

}());