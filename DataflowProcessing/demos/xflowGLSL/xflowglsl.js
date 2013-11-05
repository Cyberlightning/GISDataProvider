(function () {

    function showDebugImage(pixelData, width, height) {
        var debugCanvas = document.getElementById("debug"), debugCtx, imageData;

        if (!debugCanvas) {
            return;
        }

        debugCtx = debugCanvas.getContext("2d");
        imageData = debugCtx.createImageData(width, height);
        imageData.data.set(new Uint8ClampedArray(pixelData));
        debugCtx.putImageData(imageData, 0, 0);
    }


    XML3D.debug.loglevel = 4;
    var forwardPipeline, renderI, injectXFlowGLSLPipeline, shaderPass;

    var webgl = XML3D.webgl;
    injectXFlowGLSLPipeline = function () {
        var xml3ds = document.getElementsByTagName("xml3d");

        if (xml3ds[0]) {
            renderI = xml3ds[0].getRenderInterface();

            //The normal forward rendering pipeline is always available initially
            //It's also available as a render pass under the constructor XML3D.webgl.ForwardRenderPass(context),
            forwardPipeline = renderI.getRenderPipeline();

            shaderPass = new webgl.ShaderPass(forwardPipeline);
            shaderPass.init(forwardPipeline.context);

            /**
             * Initialising grayscale shader
             */

            XML3D.shaders.register("grayscale", {
                vertex: [
                    "attribute vec3 position;",

                    "void main(void) {",
                    "   gl_Position = vec4(position, 1.0);",
                    "}"
                ].join("\n"),

                fragment: [
                    "precision highp float;",
                    "uniform sampler2D inputTexture;",
                    "uniform vec2 quadSize;",
                    "vec2 texcoord = (gl_FragCoord.xy / quadSize.xy);",

                    "void main(void)",
                    "{",
                    "vec4 frameColor = texture2D(inputTexture, texcoord);",
                    "float luminance = frameColor.r * 0.3 + frameColor.g * 0.59 + frameColor.b * 0.11;",
                    "gl_FragColor = vec4(luminance, luminance, luminance, frameColor.a);",

                    "}"
                ].join("\n")
            });

            forwardPipeline.addShader("grayscaleShader",
                forwardPipeline.context.programFactory.getProgramByName("grayscale"));


            /**
             * Initialising blur shader
             */

            XML3D.shaders.register("blur", {

                vertex: [
                    "attribute vec3 position;",

                    "void main(void) {",
                    "   gl_Position = vec4(position, 1.0);",
                    "}"
                ].join("\n"),

                fragment: [
                    "uniform sampler2D inputTexture;",
                    "uniform vec2 quadSize;",
                    "    vec2 texcoord = (gl_FragCoord.xy / quadSize.xy);",

                    "const float blurSize = 1.0/64.0;",
                    "void main(void)",
                    "{",

                    "vec4 sum = vec4(0.0);",

                    "sum += texture2D(inputTexture, vec2(texcoord.x, texcoord.y - 4.0*blurSize)) * 0.05;",
                    "sum += texture2D(inputTexture, vec2(texcoord.x, texcoord.y - 3.0*blurSize)) * 0.09;",
                    "sum += texture2D(inputTexture, vec2(texcoord.x, texcoord.y - 2.0*blurSize)) * 0.12;",
                    "sum += texture2D(inputTexture, vec2(texcoord.x, texcoord.y - blurSize)) * 0.15;",
                    "sum += texture2D(inputTexture, vec2(texcoord.x, texcoord.y)) * 0.16;",
                    "sum += texture2D(inputTexture, vec2(texcoord.x, texcoord.y + blurSize)) * 0.15;",
                    "sum += texture2D(inputTexture, vec2(texcoord.x, texcoord.y + 2.0*blurSize)) * 0.12;",
                    "sum += texture2D(inputTexture, vec2(texcoord.x, texcoord.y + 3.0*blurSize)) * 0.09;",
                    "sum += texture2D(inputTexture, vec2(texcoord.x, texcoord.y + 4.0*blurSize)) * 0.05;",

                    "gl_FragColor = sum;",
                    "}"
                ].join("\n")
            });
            forwardPipeline.addShader("blurShader",
                forwardPipeline.context.programFactory.getProgramByName("blur"));
        }
    };

    window.addEventListener("load", injectXFlowGLSLPipeline);


    (function () {

        var ShaderPass = function (pipeline, output, opt) {
            webgl.BaseRenderPass.call(this, pipeline, output, opt);
        };

        XML3D.createClass(ShaderPass, webgl.BaseRenderPass, {
            init: function (context) {
                var gl = this.gl = this.pipeline.context.gl;
                this.screenQuad = new webgl.FullscreenQuad(context);
                this.resultTexture = gl.createTexture();
                this.frameBuffer = gl.createFramebuffer();
            },

            applyShader: function (image, shader) {
                var gl = this.gl,
                    width = image.width,
                    height = image.height,
                    program = forwardPipeline.getShader(shader),
                    screenQuad = this.screenQuad,
                    texture = this.resultTexture,
                    frameBuffer = this.frameBuffer,
                    textureSize = new Float32Array([width, height]),
                    textureBuffer = new Uint8Array(image.data);

                // Render scene to fbo
                gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, 0);
                gl.bindFramebuffer(gl.FRAMEBUFFER, frameBuffer);

                program.bind();
                gl.viewport(0, 0, width, height);

                gl.activeTexture(gl.TEXTURE0);
                gl.bindTexture(gl.TEXTURE_2D, texture);

                // Creating texture from pixel data
                gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, width, height, 0, gl.RGBA, gl.UNSIGNED_BYTE, textureBuffer);

                gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
                gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
                gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
                gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);

                gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, texture, 0);

                program.setUniformVariables({ inputTexture: texture, quadSize: textureSize});
                screenQuad.draw(program);

                gl.bindTexture(gl.TEXTURE_2D, null);

                program.unbind();

                gl.readPixels(0, 0, width, height, gl.RGBA, gl.UNSIGNED_BYTE, textureBuffer);

                gl.bindFramebuffer(gl.FRAMEBUFFER, null);
                gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, 1);

                //showDebugImage(textureBuffer, width, height);

                return textureBuffer;
            }
        });

        webgl.ShaderPass = ShaderPass;

    }());


    /*
     * Registering operators
     *
     */

    // Grayscaling operator (utilises gray scale shader via shader pass)
    Xflow.registerOperator("xflow.glslGrayscale", {
        outputs: [
            {type: 'texture', name: 'result', sizeof: 'image'}
        ],
        params: [
            {type: 'texture', source: 'image' }
        ],
        evaluate: function (result, image) {
            //console.log("Grayscaling image...");

            result.data.set(shaderPass.applyShader(image, "grayscaleShader"));
            showDebugImage(result.data, result.width, result.height);

            return true;
        }
    });

    // Grayscaling operator (utilises blur shader via shader pass)
    Xflow.registerOperator("xflow.glslBlur", {
        outputs: [
            {type: 'texture', name: 'result', sizeof: 'image'}
        ],
        params: [
            {type: 'texture', source: 'image' }
        ],
        evaluate: function (result, image) {
            //console.log("Blurring image...");

            result.data.set(shaderPass.applyShader(image, "blurShader"));
            //showDebugImage(result.data, result.width, result.height);

            return true;
        }
    });

}());
