
var requestAnimFrame = function(callback) {
    return window.requestAnimationFrame(callback) ||
        window.webkitRequestAnimationFrame(callback) ||
        window.mozRequestAnimationFrame(callback) ||
        window.oRequestAnimationFrame(callback) ||
        window.msRequestAnimationFrame(callback) ;
        //function(callback, element) { window.setTimeout(callback, 1000 / 60); };
};

var gl;
function initGL(canvas) {
    try {
     	gl = canvas.getContext("webgl") || canvas.getContext("experimental-webgl");
        gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
    } catch (e) {
    }
    if (!gl) {
        alert("Could not initialise WebGL, sorry :-(");
    }
}


function getShader(gl, id) {
    var shaderScript = document.getElementById(id);
    if (!shaderScript) {
        return null;
    }
    var str = "";
    var k = shaderScript.firstChild;
    while (k) {
        if (k.nodeType == 3) {
            str += k.textContent;
        }
        k = k.nextSibling;
    }

    var shader;
    if (shaderScript.type == "x-shader/x-fragment") {
        shader = gl.createShader(gl.FRAGMENT_SHADER);
    } else if (shaderScript.type == "x-shader/x-vertex") {
        shader = gl.createShader(gl.VERTEX_SHADER);
    } else {
        return null;
    }

    gl.shaderSource(shader, str);
    gl.compileShader(shader);
    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
        alert(gl.getShaderInfoLog(shader));
        return null;
    }

    return shader;
}

var shaderProgram;
var shaderProgram_For_Color;

function initShaders() {
    	//INITIALIZING SHADERS FOR COLORED VERTICES
    	
  	var fragmentShader1 = getShader(gl, "shader-fs-COLOUR");
    var vertexShader1 = getShader(gl, "shader-vs-COLOUR");

    shaderProgram_For_Color = gl.createProgram();
    gl.attachShader(shaderProgram_For_Color, fragmentShader1);
    gl.attachShader(shaderProgram_For_Color, vertexShader1);
    gl.linkProgram(shaderProgram_For_Color);

    if (!gl.getProgramParameter(shaderProgram_For_Color, gl.LINK_STATUS)) {
    	alert("Could not initialise shaders for color");
    }

    gl.useProgram(shaderProgram_For_Color);

    shaderProgram_For_Color.vertexPositionAttribute = gl.getAttribLocation(shaderProgram_For_Color, "aVertexPosition");
    gl.enableVertexAttribArray(shaderProgram_For_Color.vertexPositionAttribute);
        
    shaderProgram_For_Color.vertexColorAttribute = gl.getAttribLocation(shaderProgram_For_Color, "aVertexColor");
    gl.enableVertexAttribArray(shaderProgram_For_Color.vertexColorAttribute);

    shaderProgram_For_Color.pMatrixUniform = gl.getUniformLocation(shaderProgram_For_Color, "uPMatrix");
    shaderProgram_For_Color.mvMatrixUniform = gl.getUniformLocation(shaderProgram_For_Color, "uMVMatrix");
    shaderProgram_For_Color.samplerUniform = gl.getUniformLocation(shaderProgram_For_Color, "uSampler");
    	
    	
    var fragmentShader = getShader(gl, "shader-fs");
    var vertexShader = getShader(gl, "shader-vs");

    shaderProgram = gl.createProgram();
    gl.attachShader(shaderProgram, vertexShader);
    gl.attachShader(shaderProgram, fragmentShader);
    gl.linkProgram(shaderProgram);

    if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
    	alert("Could not initialise shaders");
    }

    gl.useProgram(shaderProgram);

    shaderProgram.vertexPositionAttribute = gl.getAttribLocation(shaderProgram, "aVertexPosition");
    gl.enableVertexAttribArray(shaderProgram.vertexPositionAttribute);
        
    shaderProgram.vertexTextureAttribute = gl.getAttribLocation(shaderProgram, "aTextureCoord");
    gl.enableVertexAttribArray(shaderProgram.vertexColorAttribute);

    shaderProgram.pMatrixUniform = gl.getUniformLocation(shaderProgram, "uPMatrix");
    shaderProgram.mvMatrixUniform = gl.getUniformLocation(shaderProgram, "uMVMatrix");
    shaderProgram.samplerUniform = gl.getUniformLocation(shaderProgram, "uSampler");
}

var mvMatrix = mat4.create();
var pMatrix = mat4.create();
var mvStackMatrix = [];
    
function mvPushMatrix(){
	var copy = mat4.create();
    mat4.set(mvMatrix, copy);
    mvStackMatrix.push(copy);
}
    
function mvPopMatrix(){
	if(mvStackMatrix.length == 0)
		throw "Invalid Pop";
	mvMatrix = mvStackMatrix.pop();    	
}
    
function degToRad(degrees){
	return degrees * (Math.PI / 180);
}

    
function setMatrixUniforms() {
	gl.uniformMatrix4fv(shaderProgram.pMatrixUniform, false, pMatrix);
	gl.uniformMatrix4fv(shaderProgram.mvMatrixUniform, false, mvMatrix);
}

var cubeVertexPositionBuffer;
var cubeVertexTexturerBuffer;
var cubeVertexIndexBuffer;
var cubeVertexColorBuffer;
function initBuffers() {        
	cubeVertexPositionBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, cubeVertexPositionBuffer);
//    vertices = [ -0.75,  0.1,  1.5,   // 0 a
//                 -0.75, -0.1,  1.5,	//1
//                 0.75,  -0.1,  1.5,	//2
//                 0.75, 0.1,  1.5,	//3
//                 
//                 -0.75,  0.1, -1.5,   // 4
//                 -0.75, -0.1, -1.5,
//                  0.75, -0.1, -1.5,
//                  0.75, 0.1, -1.5,
//
//                 
//                 -0.75,  0.1, -1.5,   // 16
//                 -0.75, -0.1, -1.5,
//                 -0.75,  0.1,  1.5,
//                 -0.75, -0.1,  1.5,
//                                      
//                 0.75,  0.1,  1.5,   // 21
//                 0.75, -0.1,  1.5,
//                 0.75,  0.1, -1.5,
//                 0.75, -0.1, -1.5,];
    vertices = [ -0.75,  0.1,  1.5,   // 0 a
                       -0.75, -0.1,  1.5,	//1
                       0.75,  -0.1,  1.5,	//2
                       0.75, 0.1,  1.5,	//3
                       
                       -0.75,  0.1, -1.5,   // 4
                       -0.75, -0.1, -1.5,
                        0.75, -0.1, -1.5,
                        0.75, 0.1, -1.5,
                      
                      0.75,  0.1, -1.5,   // 8
                      -0.75,  0.1, -1.5,
                      -0.75,  0.1,  1.5,
                      0.75,  0.1,  1.5,
                      
                      0.75, -0.1,  -1.5,   // 12
                      -0.75, -0.1,  -1.5,
                       -0.75, -0.1, 1.5,
                       0.75, -0.1, 1.5,
                       
                       -0.75,  0.1, -1.5,   // 16
                       -0.75, -0.1, -1.5,
                       -0.75,  0.1,  1.5,
                       -0.75, -0.1,  1.5,
                                            
                       0.75,  0.1,  1.5,   // 21
                       0.75, -0.1,  1.5,
                       0.75,  0.1, -1.5,
                       0.75, -0.1, -1.5,];
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);
    cubeVertexPositionBuffer.itemSize = 3;
    cubeVertexPositionBuffer.numItems = 24;
    
    var generatedColors = [];
    var colors = [0.0,  1.0,  0.0,  1.0];
    for (var i=0; i<24; i++) {
    	generatedColors = generatedColors.concat(colors);
    }
    cubeVertexColorBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, cubeVertexColorBuffer);
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(generatedColors), gl.STATIC_DRAW);
    cubeVertexColorBuffer.itemSize = 4;
    cubeVertexColorBuffer.numItems = 24;
        
    cubeVertexTexturerBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, cubeVertexTexturerBuffer);
    var textureCoords = [
                             // Front face
                             0.0, 0.0,
                             1.0, 0.0,
                             1.0, 1.0,
                             0.0, 1.0,

                             // Back face
                             1.0, 0.0,
                             1.0, 1.0,
                             0.0, 1.0,
                             0.0, 0.0,

                             // Top face
                             0.0, 1.0,
                             0.0, 0.0,
                             1.0, 0.0,
                             1.0, 1.0,

                             // Bottom face
                             1.0, 1.0,
                             0.0, 1.0,
                             0.0, 0.0,
                             1.0, 0.0,

                             // Right face
                             1.0, 0.0,
                             1.0, 1.0,
                             0.0, 1.0,
                             0.0, 0.0,

                             // Left face
                             0.0, 0.0,
                             1.0, 0.0,
                             1.0, 1.0,
                             0.0, 1.0,
                           ];
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(textureCoords), gl.STATIC_DRAW);
        
    cubeVertexTexturerBuffer.itemSize = 2;
    cubeVertexTexturerBuffer.numItems = 24;
        
    cubeVertexIndexBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,cubeVertexIndexBuffer);
    var coloredCubeVertexIndices = [
                                 0, 1, 2,      0, 2, 3,    // Front face
                                 4, 5, 6,      4, 6, 7,    // Back face                                 
                                 16, 17, 18,   17, 18, 19, // Right face
                                 20, 21, 22,   21, 22, 23  // Left face
                               ];        
    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,new Uint16Array(coloredCubeVertexIndices), gl.STATIC_DRAW);
    cubeVertexIndexBuffer.itemSize = 1;
    cubeVertexIndexBuffer.numItems = 24;
    var texturedTopVirtexIndices =[8, 9, 10,     8, 10, 11,];   // Top face
    var TexturedBottomVertexIndices =[12, 13, 14,   12, 14, 15, ];// Bottom face
}
    
    var frontTexture;
    var dataURL;
    var frontImage;
    function initTextures() {
    	frontTexture = gl.createTexture();
    	frontImage = new Image();
    	frontImage.onload = function() { handleTextureLoaded(frontImage, frontTexture); };
    	frontImage.src = "img/mobileFront.png";
    	}

    	function handleTextureLoaded(image, texture) {
    	  gl.bindTexture(gl.TEXTURE_2D, texture);
    	  gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, image);
    	  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR_MIPMAP_NEAREST);
    	  gl.generateMipmap(gl.TEXTURE_2D);
    	  gl.bindTexture(gl.TEXTURE_2D, null);
    	}

    //Added For Rotation of the aquare
    var rCube= 0;

    function drawScene() {
    	//Uncoment for Video
    	//updateTexture();
        gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

        mat4.perspective(45, gl.viewportWidth / gl.viewportHeight, 0.1, 100.0, pMatrix);
        mat4.identity(mvMatrix);

        mat4.translate(mvMatrix, [1.5, 0.0, -7.0]);
        
        mvPushMatrix();
//        mat4.rotate(mvMatrix, degToRad(rCube), [1 , 0 ,0]);
        mat4.rotate(mvMatrix, 90, [0 , 0 ,1]);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, cubeVertexPositionBuffer);
        gl.vertexAttribPointer(shaderProgram_For_Color.vertexPositionAttribute, cubeVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, cubeVertexColorBuffer);
        gl.vertexAttribPointer( shaderProgram_For_Color.vertexColorAttribute, cubeVertexColorBuffer.itemSize , gl.FLOAT, false, 0, 0);
        
//        gl.bindBuffer(gl.ARRAY_BUFFER, cubeVertexTexturerBuffer);
//        gl.vertexAttribPointer(shaderProgram.vertexTextureAttribute, cubeVertexTexturerBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
//        gl.activeTexture(gl.TEXTURE0);
//        gl.bindTexture(gl.TEXTURE_2D, frontTexture);
//        gl.uniform1i(shaderProgram.samplerUniform, 0);
        
        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, cubeVertexIndexBuffer);
        setMatrixUniforms();
        gl.drawElements(gl.TRIANGLES, cubeVertexIndexBuffer.numItems, gl.UNSIGNED_SHORT, 0);
        
        mvPopMatrix();        
    }
    
    var lastTime = 0;
    
    function animate() {
    	var timeNow = new Date().getTime();
    	if (lastTime != 0) {
    		var elapsed = timeNow - lastTime;
    		rCube += (75 * elapsed) / 1000.0;
    	}
    	lastTime = timeNow;
    }

    function tick() {
    	requestAnimFrame(tick);
    	drawScene();
    	animate();
    	//snapshot();
    }

    function webGLStart() {
        var canvas = document.getElementById("glcanvas");
        initGL(canvas);
        initShaders();
        initBuffers();
        
//        initTextures();
        gl.clearColor(1.0, 0.0, 0.0, 1.0);
        gl.clearDepth(1.0);  
        gl.enable(gl.DEPTH_TEST);
        // Enable depth testing
        gl.depthFunc(gl.LEQUAL);  
        
//        tick();
        drawScene();
    }
    
    var videoelement;
    var videoStream;
    function startVideo() { 
    	videoelement = document.querySelector('video');	
    	if(videoelement){
    		console.log("There is not video element");    			
    		navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia;			
    		window.URL = window.URL || window.webkitURL || window.mozURL || window.msURL;
			if(navigator.getUserMedia){
				navigator.getUserMedia({video: true, audio: false},
						function(stream) { 
							if (navigator.mozGetUserMedia) {
						        this.videoelement.mozSrcObject = stream;
						      } else {
						        var vendorURL = window.URL || window.webkitURL;
						        videoelement.src = vendorURL.createObjectURL(stream);
						      }
							videoelement.play();
							videoStream = stream;
							webGLStart();
				}.bind(this) , function (err) {	alert("Unknown Error "+err.message);  });
			} else{
				
			}	
		}
    	else 
    		alert("You HTML dom Does have a video element!");
    }
      
    var snapshot = function() {
    	if(videoelement){
    		 localcanvas = document.createElement('canvas');
    		 localcontext = localcanvas.getContext('2d');
    		 videoheight= videoelement.videoHeight;
    		 videowidth = videoelement.videoWidth;
    		 localcanvas.width = videowidth;
    		 localcanvas.height = videoheight;		 
    		 localcontext.drawImage(videoelement, 0, 0);
    		 dataURL = localcontext.getImageData(0, 0, videowidth,videoheight);
    	}
    	webGLStart();
    	//localcontext.drawImage(video, 0, 0);
    	//input.value = localcanvas.toDataURL('image/jpeg');	  
    };
