(function () {
    var webgl = XML3D.webgl,
        webcl = XML3D.webcl;

    // Registering WebCL kernels and WebGL shaders
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

    webcl.kernels.register("clThresholdImage",
        ["__kernel void clThresholdImage(__global const uchar4* src, __global uchar4* dst, uint width, uint height)",
            "{",
            "int x = get_global_id(0);",
            "int y = get_global_id(1);",
            "if (x >= width || y >= height) return;",
            "int i = y * width + x;",
            "int color = src[i].x;",
            "if (color < 100)",
            "{",
            "color=0;",
            "}else if (color >= 100 && color < 120){",
            "color=200;",
            "}else{",
            "color=255;",
            "}",
            "dst[i] = (uchar4)(color, color, color, 255);",
            "}"].join("\n"));

    XML3D.shaders.register("drawTexture", {

        vertex: [
            "attribute vec3 position;",
            "void main(void) {",
            "   gl_Position = vec4(position, 0.0);",
            "}"
        ].join("\n"),

        fragment: [
            "uniform sampler2D inputTexture;",
            "uniform float flipY;",
            "uniform vec2 canvasSize;",

            "void main(void) {",
            "    vec2 texCoord = (gl_FragCoord.xy / canvasSize.xy);",
            "    gl_FragColor = texture2D(inputTexture, vec2(texCoord.s, (1.0-flipY) * texCoord.t + flipY * (1.0 - texCoord.t)));",
            "}"
        ].join("\n"),

        uniforms: {
            canvasSize: [512, 512]
        },

        samplers: {
            inputTexture: null
        }
    });

    // Defining post processing pipeline

    (function () {

        var PostProcessingPipeline = function (context) {
            webgl.RenderPipeline.call(this, context);
            this.createRenderPasses();
        };

        XML3D.createClass(PostProcessingPipeline, webgl.RenderPipeline);

        XML3D.extend(PostProcessingPipeline.prototype, {
            init: function () {
                var context = this.context;

                //Also available: webgl.GLScaledRenderTarget
                var backBuffer = new webgl.GLRenderTarget(context, {
                    width: context.canvasTarget.width,
                    height: context.canvasTarget.height,
                    colorFormat: context.gl.RGBA,
                    depthFormat: context.gl.DEPTH_COMPONENT16,
                    stencilFormat: null,
                    depthAsRenderbuffer: true
                });

                //Register this target under the name "backBufferOne" so render passes may use it
                this.addRenderTarget("backBufferOne", backBuffer);

                //The screen is always available under context.canvastarget
                this.addRenderTarget("screen", context.canvastarget);

                //Remember to initialize each render pass
                this.renderPasses.forEach(function (pass) {
                    if (pass.init) {
                        pass.init(context);
                    }
                });
            },

            createRenderPasses: function () {
                //This is where the render process is defined as a series of render passes. They will be executed in the
                //order that they are added. XML3D.webgl.ForwardRenderPass may be used to draw all visible objects to the given target

                var forwardPass1 = new webgl.ForwardRenderPass(this, "backBufferOne"),
                    webCLPass = new webgl.WebCLPass(this, "screen", {inputs: { inputBuffer: "backBufferOne" }});

                this.addRenderPass(forwardPass1);
                this.addRenderPass(webCLPass);
            }
        });

        webgl.PostProcessingPipeline = PostProcessingPipeline;

    }());


    (function () {

        var WebCLPass = function (pipeline, output, opt) {
            webgl.BaseRenderPass.call(this, pipeline, output, opt);
        };

        XML3D.createClass(WebCLPass, webgl.BaseRenderPass, {
            init: function (context) {

                var shader = context.programFactory.getProgramByName("drawTexture");
                this.pipeline.addShader("blitShader", shader);

                // WebGL
                this.gl = this.pipeline.context.gl;
                this.debugCanvas = document.getElementById("debug");
                this.debugCtx = this.debugCanvas.getContext("2d");
                this.canvasWidth = context.canvasTarget.width;
                this.canvasHeight = context.canvasTarget.height;
                this.canvasSize = new Float32Array([this.canvasWidth, this.canvasHeight]);
                this.bufSize = (this.canvasWidth * this.canvasHeight * 4);
                this.screenQuad = new webgl.FullscreenQuad(context);
                this.tempTexBuffer = new Uint8Array(this.bufSize);
                this.resultTexture = this.gl.createTexture();

                //WebCL
                this.clCtx = webcl.ctx;
                this.grayScaleKernel = webcl.kernels.getKernel("clDesaturate");
                this.thresholdKernel = webcl.kernels.getKernel("clThresholdImage");
                this.localWS = [16, 4];
                this.globalWS = [Math.ceil(this.canvasWidth / this.localWS[0]) * this.localWS[0],
                    Math.ceil(this.canvasHeight / this.localWS[1]) * this.localWS[1]];
                this.clBufIn = this.clCtx.createBuffer(WebCL.CL_MEM_READ_ONLY, this.bufSize); //Buffer in WebCL Ctx
                this.clBufOut = this.clCtx.createBuffer(WebCL.CL_MEM_WRITE_ONLY, this.bufSize); //Buffer in WebCL Ctx
            },

            render: function (scene) {
                var gl = this.gl, clCtx = this.clCtx,
                    grayScaleKernel = this.grayScaleKernel,
                    width = this.canvasWidth,
                    height = this.canvasHeight,
                    program = this.pipeline.getShader("blitShader"),
                    thresholdKernel = this.thresholdKernel,
                    globalWS = this.globalWS, localWS = this.localWS,

                    bufSize = this.bufSize,
                    clBufOut = this.clBufOut,
                    clBufIn = this.clBufIn,
                    tempTexBuffer = this.tempTexBuffer,
                    texture = this.resultTexture,

                //Request the framebuffer from the render pipeline, using its name (in this case 'backBufferOne')
                    sourceTex = this.pipeline.getRenderTarget(this.inputs.inputBuffer),
                    renderTarget = this.pipeline.getRenderTarget(this.output),

                //Variables for debugging
                    pixelData, imageData;
                sourceTex.bind();
                gl.readPixels(0, 0, width, height, gl.RGBA, gl.UNSIGNED_BYTE, tempTexBuffer);
                sourceTex.unbind();

                grayScaleKernel.setKernelArg(0, clBufIn);
                grayScaleKernel.setKernelArg(1, clBufOut);
                grayScaleKernel.setKernelArg(2, width, WebCL.types.UINT);
                grayScaleKernel.setKernelArg(3, height, WebCL.types.UINT);

                // Write the buffer to OpenCL device memory
                webcl.cmdQueue.enqueueWriteBuffer(clBufIn, false, 0, bufSize, tempTexBuffer, []);

                // Execute (enqueue) kernel
                webcl.cmdQueue.enqueueNDRangeKernel(grayScaleKernel, globalWS.length, [], globalWS, localWS, []);

                // Read the result buffer from OpenCL device
                webcl.cmdQueue.enqueueReadBuffer(clBufOut, false, 0, bufSize, tempTexBuffer, []);

                thresholdKernel.setKernelArg(0, clBufIn);
                thresholdKernel.setKernelArg(1, clBufOut);
                thresholdKernel.setKernelArg(2, width, WebCL.types.UINT);
                thresholdKernel.setKernelArg(3, height, WebCL.types.UINT);


                // Write the buffer to OpenCL device memory
                webcl.cmdQueue.enqueueWriteBuffer(clBufIn, false, 0, bufSize, tempTexBuffer, []);

                // Execute (enqueue) kernel
                webcl.cmdQueue.enqueueNDRangeKernel(thresholdKernel, globalWS.length, [], globalWS, localWS, []);

                // Read the result buffer from OpenCL device
                webcl.cmdQueue.enqueueReadBuffer(clBufOut, false, 0, bufSize, tempTexBuffer, []);

                webcl.cmdQueue.finish(); //Finish all the operations

                /* Debug code start ---
                 pixelData = new Uint8ClampedArray(this.outputTexBuffer);
                 imageData = this.debugCtx.createImageData(width, height);
                 imageData.data.set(pixelData);
                 this.debugCtx.putImageData(imageData, 0, 0);
                 // --- Debug end */

                // Rendering results from WebCL kernels on canvas
                renderTarget.bind();
                program.bind();

                gl.clear(gl.DEPTH_BUFFER_BIT || gl.COLOR_BUFFER_BIT);

                gl.activeTexture(gl.TEXTURE0);
                gl.bindTexture(gl.TEXTURE_2D, texture);
                gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);
                // Creating texture from pixel data

                //gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, imageData);
                gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, 800, 600, 0, gl.RGBA, gl.UNSIGNED_BYTE, tempTexBuffer);
                gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
                gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
                gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
                gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
                //gl.generateMipmap(gl.TEXTURE_2D);

                //Request the framebuffer from the render pipeline, using its name (in this case 'backBufferOne')
                program.setUniformVariables({ inputTexture: texture, canvasSize: this.canvasSize, flipY: 1});

                this.screenQuad.draw(program);

                gl.bindTexture(gl.TEXTURE_2D, null);
                program.unbind();
                renderTarget.unbind();

                //this.output = sourceTex;

            }
        });

        webgl.WebCLPass = WebCLPass;

    }());


    // --- Initialisation ---

    var PPPipeline, forwardPipeline, currentPipeline, renderI, initPPPipeLine, swapPipelines;

    initPPPipeLine = function () {
        var xml3ds = document.getElementsByTagName("xml3d"), ctx;

        if (xml3ds[0]) {
            //Render pipeline is gettable only from XMl3D element only after xml3d has been properly initialised
            renderI = xml3ds[0].getRenderInterface();

            //The normal forward rendering pipeline is always available initially
            //It's also available as a render pass under the constructor XML3D.webgl.ForwardRenderPass(context),
            forwardPipeline = renderI.getRenderPipeline();

            PPPipeline = new webgl.PostProcessingPipeline(renderI.context);
            PPPipeline.init();
            console.log(PPPipeline)
            renderI.setRenderPipeline(PPPipeline);
            currentPipeline = "postProcess";
        }

        swapPipelines = function (evt) {
            if (evt.keyCode === 112 || evt.charCode === 112) /* P */ {
                if (currentPipeline === "postProcess") {
                    renderI.setRenderPipeline(forwardPipeline);
                    currentPipeline = "forward";

                } else {
                    renderI.setRenderPipeline(PPPipeline);
                    currentPipeline = "postProcess";
                }
                console.log("Current pipeline:", currentPipeline);
            }

        };

        document.addEventListener("keypress", swapPipelines);
    };

    window.addEventListener("load", initPPPipeLine);


}());