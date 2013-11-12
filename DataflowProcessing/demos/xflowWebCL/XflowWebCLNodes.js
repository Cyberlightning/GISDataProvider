(function () {
    var webcl = XML3D.webcl,
        cmdQueue = webcl.cmdQueue,
        ctx = webcl.ctx;

    (function () {
        webcl.kernels.register("clThresholdImage",
            ["__kernel void clThresholdImage(__global const uchar4* src, __global uchar4* dst, uint width, uint height)",
                "{",
                "int x = get_global_id(0);",
                "int y = get_global_id(1);",
                "if (x >= width || y >= height) return;",
                "int i = y * width + x;",
                "int color = src[i].x;",
                "if (color < 50)",
                "{",
                "color=0;",
                "}else{",
                "color=255;",
                "}",
                "dst[i] = (uchar4)(color, color, color, 255);",
                "}"].join("\n"));

        var kernel = webcl.kernels.getKernel("clThresholdImage"),
            oldBufSize = 0,
            buffers = {bufIn: null, bufOut: null};


        Xflow.registerOperator("xflow.clThresholdImage", {
            outputs: [
                {type: 'texture', name: 'result', sizeof: 'image'}
            ],
            params: [
                {type: 'texture', source: 'image'}
            ],

            evaluate: function (result, image) {
                //console.time("clThresholdImage");

                //passing xflow operators input data
                var width = image.width,
                    height = image.height,
                    imgSize = width * height,

                // Setup buffers
                    bufSize = imgSize * 4, // size in bytes
                    bufIn = buffers.bufIn,
                    bufOut = buffers.bufOut;

                if (bufSize !== oldBufSize) {
                    oldBufSize = bufSize;

                    if (bufIn && bufOut) {
                        bufIn.releaseCLResources();
                        bufOut.releaseCLResources();
                    }
                    // Setup WebCL context using the default device of the first available platform
                    bufIn = buffers.bufIn = ctx.createBuffer(WebCL.CL_MEM_READ_ONLY, bufSize);
                    bufOut = buffers.bufOut = ctx.createBuffer(WebCL.CL_MEM_WRITE_ONLY, bufSize);

                }

                kernel.setArg(0, bufIn);
                kernel.setArg(1, bufOut);
                kernel.setArg(2, new Uint32Array([width]));
                kernel.setArg(3, new Uint32Array([height]));


                // Write the buffer to OpenCL device memory
                cmdQueue.enqueueWriteBuffer(bufIn, false, 0, bufSize, image.data, []);

                // Init ND-range
                var localWS = [16, 4],
                    globalWS = [Math.ceil(width / localWS[0]) * localWS[0],
                        Math.ceil(height / localWS[1]) * localWS[1]];

                // Execute (enqueue) kernel
                cmdQueue.enqueueNDRangeKernel(kernel, globalWS.length, [], globalWS, localWS, []);

                // Read the result buffer from OpenCL device
                cmdQueue.enqueueReadBuffer(bufOut, false, 0, bufSize, result.data, []);

                cmdQueue.finish(); //Finish all the operations

                //console.timeEnd("clThresholdImage");

                return true;
            }

        });
    }());

    /**
     * WebCL accelerated Image Desaturation (gray scaling)
     */

    (function () {
        webcl.kernels.register("clDesaturate",
            ["__kernel void clDesaturate(__global const uchar4* src, __global uchar4* dst, uint width, uint height)",
                "{",
                "int x = get_global_id(0);",
                "int y = get_global_id(1);",
                "if (x >= width || y >= height) return;",
                "int i = y * width + x;  uchar4 color = src[i];",
                "uchar lum = (uchar)(0.30f * color.x + 0.59f * color.y + 0.11f * color.z);",
                "dst[i] = (uchar4)(lum, lum, lum, 255);",
                "}"].join("\n"));

        var kernel = webcl.kernels.getKernel("clDesaturate"),
            oldBufSize = 0,
            buffers = {bufIn: null, bufOut: null};


        Xflow.registerOperator("xflow.clDesaturateImage", {
            outputs: [
                {type: 'texture', name: 'result', sizeof: 'image'}
            ],
            params: [
                {type: 'texture', source: 'image'}
            ],
            evaluate: function (result, image) {
                //console.time("clDesaturate");

                //passing xflow operators input data
                var width = image.width,
                    height = image.height,
                    imgSize = width * height,

                // Setup buffers
                    bufSize = imgSize * 4,
                    bufIn = buffers.bufIn,
                    bufOut = buffers.bufOut;

                if (bufSize !== oldBufSize) {
                    oldBufSize = bufSize;

                    if (bufIn && bufOut) {
                        bufIn.releaseCLResources();
                        bufOut.releaseCLResources();
                    }

                    // Setup WebCL context using the default device of the first available platform
                    bufIn = buffers.bufIn = ctx.createBuffer(WebCL.CL_MEM_READ_ONLY, bufSize);
                    bufOut = buffers.bufOut = ctx.createBuffer(WebCL.CL_MEM_WRITE_ONLY, bufSize);

                }

                kernel.setArg(0, bufIn);
                kernel.setArg(1, bufOut);
                kernel.setArg(2, new Uint32Array([width]));
                kernel.setArg(3, new Uint32Array([height]));

                // Write the buffer to OpenCL device memory
                cmdQueue.enqueueWriteBuffer(bufIn, false, 0, bufSize, image.data, []);

                // Init ND-range
                var localWS = [16, 4], globalWS = [Math.ceil(width / localWS[0]) * localWS[0],
                    Math.ceil(height / localWS[1]) * localWS[1]];

                // Execute (enqueue) kernel
                cmdQueue.enqueueNDRangeKernel(kernel, globalWS.length, [], globalWS, localWS, []);

                // Read the result buffer from OpenCL device
                cmdQueue.enqueueReadBuffer(bufOut, false, 0, bufSize, result.data, []);

                cmdQueue.finish(); //Finish all the operations

                //console.timeEnd("clDesaturate");
                return true;
            }
        });
    }());

}());