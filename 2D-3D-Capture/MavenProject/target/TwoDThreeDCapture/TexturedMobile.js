var canvas;
var gl;
var vertexPositionAttribute;
var perspectiveMatrix;
var cubeRotation = 0.0;
var dAPI;
var alpha=0;
var beta=0;
var gamma=0;
var gx;
var gy;
var gz;
var mobileType ="LG_stereoscopic"; //galaxy_s2,galaxy_tab_note3

function loadScript() {	
	  var script = document.createElement("script");
	  script.type = "text/javascript";
	  script.src = "https://maps.googleapis.com/maps/api/js?key=AIzaSyDqNrH21T3prB-yKHLiw8us-YHdVUOr5lQ&sensor=false&callback=start";
	  document.body.appendChild(script);
	  alert("Script Loaded");
}

function locationFound(pos) {
	alert("Location Found" +pos.coords.latitude+":"+ pos.coords.longitude);
	var location= new google.maps.LatLng(pos.coords.latitude, pos.coords.longitude);
	var sv = new google.maps.StreetViewService();
	sv.getPanoramaByLocation(location, 10, processSVData);
}

var processSVData = function(data, status) {
	alert("Street View found->"+ status);
	//if (status == google.maps.StreetViewStatus.OK) {
		//alert(data.location.latLng.lat());

		//var streetviewurl ="https://maps.googleapis.com/maps/api/streetview?size=600x400&location="+data.location.latLng.lat()+","+data.location.latLng.lng()+"&fov=90&heading="+yaw+"&pitch="+pitch+"&sensor=false";
		var streetviewurl ="https://maps.googleapis.com/maps/api/streetview?size=600x400&location="+65.010833+","+25.468889+"&fov=90&heading="+0+"&pitch="+90+"&sensor=false";
		alert(streetviewurl);
		$('#streetview').empty();
		$('#streetview').append("<img src=\""+streetviewurl+"\" alt=\"Loading...\" />");
		

	  //} else {
		//  $('#streetview').empty();
	   // alert('Street View data not found for this location.');
	  //}
};


function start(){
	//alert("Start");
	dAPI = new FIware_wp13.Device("dev.cyberlightning.com","dev.cyberlightning.com", "9090" , "17321" ,"17322");
//	dAPI.setupLogger();	
//	dAPI.getCurrentLocation(locationFound);
	
//    dAPI.registerDeviceOrentationEvent(handleOrientationChanges);
//  Activate for debugging
//  dAPI.log("Type ->"+dAPI.Type);
  dAPI.registerDeviceMotionEvents(handlacceleration, handleAccelerationWithGravity, handleRotation);
  if(dAPI.Type == "Desktop")
	  init();
  else {
	  dAPI.registerDeviceOrentationEvent(handleOrientationChanges); 
	  dAPI.registerDeviceMotionEvents(handlacceleration, handleAccelerationWithGravity, handleRotation);
	  init();
  	
  }
}

function initMaps(){
	var setStreetView = function(location) {
		var sv = new google.maps.StreetViewService();
		//panorama = new google.maps.StreetViewPanorama(document.getElementById('streetview'));
		sv.getPanoramaByLocation(location, 10, processSVData);
	};
}

function init() {
	
  canvas = document.getElementById("glcanvas");

  gl = initWebGL(canvas);      // Initialize the GL context
  
  // Only continue if WebGL is available and working
  
  if (gl) {
    gl.clearColor(0.0, 1.0, 1.0, 1.0);                      // Set clear color to black, fully opaque
    gl.enable(gl.DEPTH_TEST);   
 	gl.clearDepth(1.0);                         // Enable depth testing
    gl.depthFunc(gl.LEQUAL);                                // Near things obscure far things

    // Initialize the shaders; this is where all the lighting for the
	  // vertices and so forth is established.
	  
	  initShaders();
	  
	  // Here's where we call the routine that builds all the objects
	  // we'll be drawing.
	  
	  initBuffers();
	  initTextures();
	  setInterval(drawScene, 15);
  }  
}

var topSurfaceTexture ;
var bottomSurfaceTexture;
function initTextures(){
//	dAPI.log("Loading Texture 1");
	topSurfaceTexture = gl.createTexture();
	topSurfaceTexture.Image = new Image();
	topSurfaceTexture.Image.onload = function() {
//		dAPI.log("Texture Loaded for top");
		handleTextureLoaded( topSurfaceTexture);
	};
	if(mobileType=="LG_stereoscopic")
		topSurfaceTexture.Image.src = "img/LGFront.png";
	else if (mobileType=="galaxy_s2")
		topSurfaceTexture.Image.src = "img/SamsungFront.png";
	else if(mobileType=="galaxy_tab_note3")
		topSurfaceTexture.Image.src = "img/note3Front.png";
	
	bottomSurfaceTexture = gl.createTexture();
	bottomSurfaceTexture.Image = new Image();
	bottomSurfaceTexture.Image.onload = function() {
//		dAPI.log("Texture Loaded for Bottom");
		handleTextureLoaded( bottomSurfaceTexture);
	};
	if(mobileType=="LG_stereoscopic")
		bottomSurfaceTexture.Image.src = "img/LGBack.png";
	else if(mobileType=="galaxy_s2")
		bottomSurfaceTexture.Image.src = "img/samsungBack.png";
	else if(mobileType=="galaxy_tab_note3")
		bottomSurfaceTexture.Image.src = "img/note3Back.png";
}

function handleTextureLoaded(texture){
	//console.log("handleTextureLoaded, image = " + image);
	gl.bindTexture(gl.TEXTURE_2D, texture);
	gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);
	 gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, texture.Image);
	  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
	  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
//	  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
//	  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE); 
//	  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
	  //gl.generateMipmap(gl.TEXTURE_2D);
	  gl.bindTexture(gl.TEXTURE_2D, null);
}

function initWebGL(canvas) {
	  gl = null;
	  
	  try {
	    // Try to grab the standard context. If it fails, fallback to experimental.
	    gl = canvas.getContext("webgl") || canvas.getContext("experimental-webgl");
	    gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
	  }
	  catch(e) {}
	  
	  // If we don't have a GL context, give up now
	  if (!gl) {
	    alert("Unable to initialize WebGL. Your browser may not support it.");
	    gl = null;
	  }
	  gl.viewport(0, 0, canvas.width, canvas.height);
	  return gl;
}

function getShader(gl, id) {
	  var shaderScript, theSource, currentChild, shader;
	  
	  shaderScript = document.getElementById(id);
	  
	  if (!shaderScript) {
	    return null;
	  }
	  
	  theSource = "";
	  currentChild = shaderScript.firstChild;
	  
	  while(currentChild) {
	    if (currentChild.nodeType == currentChild.TEXT_NODE) {
	      theSource += currentChild.textContent;
	    }
	    
	    currentChild = currentChild.nextSibling;
	  }
	  if (shaderScript.type == "x-shader/x-fragment") {
		    shader = gl.createShader(gl.FRAGMENT_SHADER);
	  } else if (shaderScript.type == "x-shader/x-vertex") {
		  shader = gl.createShader(gl.VERTEX_SHADER);
	  } else {
		  // 		Unknown shader type
		  return null;
	  }
	  gl.shaderSource(shader, theSource);
	    
	  // Compile the shader program
	  gl.compileShader(shader);  
	    
	  // See if it compiled successfully
	  if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {  
	      alert("An error occurred compiling the shaders: " + gl.getShaderInfoLog(shader));  
	      return null;  
	  }
	    
	  return shader;
}

var shaderProgram;
var shaderProgram4Tex;
function initShaders() {
//	dAPI.log("Initialising shaders");
		//Initialising shaders for color
	  var fragmentShader = getShader(gl, "shader-fs");
	  var vertexShader = getShader(gl, "shader-vs");
	  //initialising shaders for texture
	  var fragmentShader4Tex = getShader(gl, "shader-fs-texture");
	  var vertexShader4Tex = getShader(gl, "shader-vs-texture");
	  
	  // Create the shader program
	  
	  shaderProgram = gl.createProgram();
	  gl.attachShader(shaderProgram, vertexShader);
	  gl.attachShader(shaderProgram, fragmentShader);
	  gl.linkProgram(shaderProgram);
	  
	  shaderProgram4Tex = gl.createProgram();
	  gl.attachShader(shaderProgram4Tex, vertexShader4Tex);
	  gl.attachShader(shaderProgram4Tex, fragmentShader4Tex);
	  gl.linkProgram(shaderProgram4Tex);
	  
	  // If creating the shader program failed, alert
	  
	  if (!gl.getProgramParameter(shaderProgram4Tex, gl.LINK_STATUS)) {
	    alert("Unable to initialize the shader program.");
	  }
	  if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
		    alert("Unable to initialize the shader program.");
	  }
	  
	  //gl.useProgram(shaderProgram);
	  
	  shaderProgram.vertexPositionAttribute = gl.getAttribLocation(shaderProgram, "aVertexPosition");
	  gl.enableVertexAttribArray(shaderProgram.vertexPositionAttribute);	  
	  shaderProgram.vertexColorAttribute = gl.getAttribLocation(shaderProgram, "aVertexColor");
	  gl.enableVertexAttribArray(shaderProgram.vertexColorAttribute);	  
	  shaderProgram.pMatrixUniform = gl.getUniformLocation(shaderProgram, "uPMatrix");
	  shaderProgram.mvMatrixUniform = gl.getUniformLocation(shaderProgram, "uMVMatrix");
	  
	  shaderProgram4Tex.vertexPositionAttribute = gl.getAttribLocation(shaderProgram4Tex, "aVertexPosition");
	  gl.enableVertexAttribArray(shaderProgram4Tex.vertexPositionAttribute);	  
	  shaderProgram4Tex.vertexTextureAttribute = gl.getAttribLocation(shaderProgram4Tex, "aTextureCoord");
	  gl.enableVertexAttribArray(shaderProgram4Tex.vertexTextureAttribute);	  
	  shaderProgram4Tex.pMatrixUniform = gl.getUniformLocation(shaderProgram4Tex, "uPMatrix");
	  shaderProgram4Tex.mvMatrixUniform = gl.getUniformLocation(shaderProgram4Tex, "uMVMatrix");
	  gl.enableVertexAttribArray(shaderProgram4Tex.vertexNormalAttribute);
	  shaderProgram4Tex.samplerUniform = gl.getUniformLocation(shaderProgram4Tex, "uSampler");  
}


var squareVerticesBuffer;
var topFaceTextureVerticesBuffer;
var bottomFaceTextureVerticesBuffer;
var topTextureCoordinateBuffer;
var bottomTextureCoordinateBuffer;
var topTextureSurfaceIndexBuffer;
var bottomTextureSurfaceIndexBuffer;
var topTextureSurfaceNormalsBuffer;
var bottomTextureSurfaceNormalsBuffer;
var squareVerticesColorBuffer;
var cubeVerticesIndexBuffer;
var cubeVerticesNormalsBuffer;

function initBuffers() {
  
  topFaceTextureVerticesBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, topFaceTextureVerticesBuffer);
  var topFaceVertices = [    
	// Top face
	0.0,  0.64, -10.0,
	0.0,  0.64,  0.0,
	5.0,  0.64,  0.0,
	5.0,  0.64, -10.0,
	];
  topFaceTextureVerticesBuffer.itemSize = 3;
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(topFaceVertices), gl.STATIC_DRAW);
//  
//  
  bottomFaceTextureVerticesBuffer =  gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, bottomFaceTextureVerticesBuffer);
  var bottomFaceVertices = [
	// Bottom face
	0.0, 0.0, -10.0,
	5.0, 0.0, -10.0,
	5.0, 0.0,  0.0,
	0.0, 0.0,  0.0,
 ];
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(bottomFaceVertices), gl.STATIC_DRAW);
  bottomFaceTextureVerticesBuffer.itemSize = 3;
//  
  var topFaceTextureCoordinates = [
                            // Top                            
                            0.0,  1.0,
                            0.0,  0.0,
                            1.0,  0.0,
                            1.0,  1.0,
                            ];
  
  topTextureCoordinateBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, topTextureCoordinateBuffer);
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(topFaceTextureCoordinates), gl.STATIC_DRAW);
  topTextureCoordinateBuffer.itemSize = 2;
//  
  var bottomFaceTextureCoordinates = [
                                      // Bottom                                      
                                      1.0,  1.0,
                                      0.0,  1.0,
                                      0.0,  0.0,
                                      1.0,  0.0,
                                      ];
  bottomTextureCoordinateBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, bottomTextureCoordinateBuffer);
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(bottomFaceTextureCoordinates), gl.STATIC_DRAW);
  bottomTextureCoordinateBuffer.itemSize = 2;
//  
  var texturedSurfaceIndices = [0,  1, 2,     0, 2, 3, ]; 
  
  topTextureSurfaceIndexBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, topTextureSurfaceIndexBuffer);
  gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(texturedSurfaceIndices), gl.STATIC_DRAW);
  topTextureSurfaceIndexBuffer.itemSize = 6;
//  
  bottomTextureSurfaceIndexBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, bottomTextureSurfaceIndexBuffer);
  gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(texturedSurfaceIndices), gl.STATIC_DRAW);
  bottomTextureSurfaceIndexBuffer.itemSize = 6;
//  
//  topTextureSurfaceNormalsBuffer = gl.createBuffer();
//  gl.bindBuffer(gl.ARRAY_BUFFER, topTextureSurfaceNormalsBuffer);
//  
//  var texturedVertexNormals = [
//		//Top
//		0.0,  1.0,  0.0,
//		0.0,  1.0,  0.0,
//		0.0,  1.0,  0.0,
//		0.0,  1.0,  0.0,
//		];
//
//  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(texturedVertexNormals), gl.STATIC_DRAW);
//  topTextureSurfaceNormalsBuffer.itemSize = 3;
////  
//  bottomTextureSurfaceNormalsBuffer = gl.createBuffer();
//  gl.bindBuffer(gl.ARRAY_BUFFER, bottomTextureSurfaceNormalsBuffer);
//  
//  var bottomTexturedVertexNormals = 
//	  [
//		// Bottom
//		0.0, -1.0,  0.0,
//		0.0, -1.0,  0.0,
//		0.0, -1.0,  0.0,
//		0.0, -1.0,  0.0,
//	];
//  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(bottomTexturedVertexNormals), gl.STATIC_DRAW);
//  bottomTextureSurfaceNormalsBuffer.itemSize = 3;
  
  squareVerticesBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, squareVerticesBuffer);
  var vertices = [
    //Front
	0.0,  0.0,  0.0, //0.0,0.0,0.0,
	5.0, 0.0,  0.0,  // 5.0,0.0, 0.0,
	5.0,  0.64, 0.0,   // 0.0, 0.64,0.0,
	0.0, 0.64, 0.0,   // 0.0, 0.64, 0.0,
	//Back
    0.0,  0.0,  -10.0, //0.0 ,
    0.0, 0.64,  -10.0,
    5.0,  0.64, -10.0,
    5.0, 0.0, -10.0,
    
    // Right face
     5.0, 0.0, -10.0,
     5.0,  0.64, -10.0,
     5.0,  0.64,  0.0,
     5.0, 0.0,  0.0,
    
    // Left face
    0.0, 0.0, -10.0,
    0.0, 0.0,  0.0,
    0.0,  0.64,  0.0,
    0.0,  0.64, -10.0,
  ];
  
  squareVerticesBuffer.itemSize = 3;
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);
  
  var colors =  [0.5,  1.0,  0.0,  1.0];              
              var generatedColors = [];
              
              for (var j=0; j<16; j++) {
                 generatedColors = generatedColors.concat(colors);
              }
  squareVerticesColorBuffer = gl.createBuffer();  
  gl.bindBuffer(gl.ARRAY_BUFFER, squareVerticesColorBuffer);
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(generatedColors), gl.STATIC_DRAW);
  squareVerticesColorBuffer.itemSize = 4;
  
  cubeVerticesIndexBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, cubeVerticesIndexBuffer);
  
  // This array defines each face as two triangles, using the
  // indices into the vertex array to specify each triangle's
  // position.
  
  var cubeVertexIndices = [
    0,  1,  2,      0,  2,  3,    // front
    4,  5,  6,      4,  6,  7,    // back
    8,  9,  10,     8,  10, 11,   // right
    12, 13, 14,     12, 14, 15,   // left
  ];
  
  // Now send the element array to GL
  
  gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(cubeVertexIndices), gl.STATIC_DRAW);
  cubeVerticesIndexBuffer.itemSize = 24;
}

function drawScene() {
	var malpha=0;
	var mbeta=0;
	var mgamma=0;
	  gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
	  mat4.perspective(60 , gl.viewportWidth / gl.viewportHeight, 0.1, 100.0, pMatrix);
	  mat4.identity(mvMatrix);
	  mat4.translate(mvMatrix, [0.0, -0.0, -13.0]);
	  mvPushMatrix();
	  
	//For SAMSUNG GALAXY S2  -DO NOT CHANGE THIS WORKS
	/**
	 * Logic for this part is that mobile device is held bottom up or screen up and it is recognized by accelerometer.
	 * Then South is assumed to be in the direction of the viewport. user is placed right on the origin.
	 *  
	 */
	 		  
			/**
			 * CALIBRATED FOR LG STEREOSCOPIC
			 * TODO GZ < 0 LANDSCAPE MODE
			 */
	  if(mobileType=="LG_stereoscopic"){
		  if(gz >6.0 & gz > gy  & gz > gx) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } 
		  } else if  (gz < (-6.0) & gz < gx & gz < gy ) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = degToRad(beta); malpha = -degToRad(alpha+180); mgamma =degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				 mbeta = -degToRad(beta); malpha = -degToRad(alpha-180); mgamma =degToRad(gamma);
			  }
		  } else if(gx > 6.0 & gx > gy & gx > gz){
			  if(gz >0 ) {
				  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
					  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
				  } else if ((alpha>90.0 & alpha <270.0) ){
					  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
				  }
		  	} else{
		  		 if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
					  if(beta < 0)
					  		mbeta = -degToRad(beta+180);
					  else
					  		bmeta = -degToRad(beta-180);
					  malpha = -degToRad(alpha); mgamma =degToRad(180-gamma);
				  } else if ((alpha>90.0 & alpha <270.0) ){
					  if(beta < 0)
						  mbeta = -degToRad(beta+180);
					  else
						  mbeta = -degToRad(beta-180);
					  mlpha = -degToRad(alpha); mgamma =degToRad(180-gamma);
				  }
		  	}
		  } else if  (gx < (-6.0) & gx < gy & gx < gz ) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = degToRad(beta); malpha = degToRad(alpha-180); mgamma =-degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mmbeta = -degToRad(beta); malpha = degToRad(alpha-180); mgamma =-degToRad(gamma);
			  }
		  } else if (gy >6.0 & gy > gz  & gy > gx) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } else  if(alpha>90.0 & alpha <270.0) {
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  }//else if (alpha>220.0 & alpha <320.0){
	//			  /**TODO*/
	//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
	//			  mat4.rotate(mvMatrix, degToRad(alpha), [0, 1, 0]); 
	//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
	//		  } else if (alpha>40.0 & alpha <140.0){
	//			  /**TODO*/
	//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
	//			  mat4.rotate(mvMatrix, degToRad(alpha), [0, 1, 0]); 
	//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
	//		  }
		  } else if  (gy < (-6.0) & gy < gx & gy < gz ) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma = degToRad(gamma);
	//		  } else if ((alpha>140.0 & alpha <20.0) ){
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } //else if (alpha>220.0 & alpha <320.0){
				  /**TODO*/
	//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
	//			  mat4.rotate(mvMatrix, degToRad(alpha), [0, 1, 0]); 
	//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
	//		  } else if (alpha>40.0 & alpha <140.0){
	//			  /**TODO*/
	//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
	//			  mat4.rotate(mvMatrix, -degToRad(alpha), [0, 1, 0]); 
	//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
	//		  }
			  /**END OF gz */
		  }
	  } /**
		 * CALIBRATED FOR GALAXY S2
		 * TODO GZ < 0 LANDSCAPE MODE
		 * TODO POTRATAIT MODE AT 90 AND 270
		 */	
	  else if(mobileType=="galaxy_s2"){
	  
	  if(gz >6.0 & gz > gy  & gz > gx) {
		  //beta <45 at this points beta sesnsor value is negative
		  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
			  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
		  } else if ((alpha>90.0 & alpha <270.0) ){
			  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);			  
		  }
	  } else if  (gz < (-6.0) & gz < gx & gz < gy ) {
		  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
			  mbeta = -degToRad(beta); malpha = degToRad(alpha); mgamma =degToRad(gamma);
		  } else if ((alpha>90.0 & alpha <270.0) ){
			  mbeta = degToRad(beta); malpha = degToRad(alpha); mgamma =-degToRad(gamma);
		  }
		  /**END OF gz */
	  } else if(gx > 6.0 & gx > gy & gx > gz){
		  if(gz >0 ) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma = degToRad(gamma);
	  	} else{
	  			 malpha = -degToRad(alpha); mgamma = degToRad(180-gamma);
				  if(beta < 0)
					  mbeta = -degToRad(beta+180);
				  else
					  beta = -degToRad(beta-180);
	  	}
	  } else if  (gx < (-6.0) & gx < gz & gx < gy ) {
		//beta >135 at this points beta sesnsor value is negative
		  if(gz > 0) {
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);				  
		  } else {
				  malpha = -degToRad(alpha); mgamma =degToRad(180-gamma);
				  if(beta < 0)
					  mbeta = degToRad(beta+180);
				  else
					 mbeta = degToRad(beta-180);
		  }
		  /**END OF gx */
	  } else if (gy >6.0 & gy > gz  & gy > gx) {
		  
		 // if((alpha<40.0 & alpha >0) || (alpha<360.0 & alpha >320.0)) {
		  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
			  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);	
		 // } else  if(alpha>140.0 & alpha <220.0) {
		  } else  if(alpha>90.0 & alpha <270.0) {
			  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);	
		  }//else if (alpha>220.0 & alpha <320.0){
//			  /**TODO*/
//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
//			  mat4.rotate(mvMatrix, degToRad(alpha), [0, 1, 0]); 
//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
//		  } else if (alpha>40.0 & alpha <140.0){
//			  /**TODO*/
//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
//			  mat4.rotate(mvMatrix, degToRad(alpha), [0, 1, 0]); 
//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
//		  }
	  } else if  (gy < (-6.0) & gy < gx & gy < gz ) {
		  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
			  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);	
		  } else if ((alpha>90.0 & alpha <270.0) ){
			  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
		  } //else if (alpha>220.0 & alpha <320.0){
			  /**TODO*/
//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
//			  mat4.rotate(mvMatrix, degToRad(alpha), [0, 1, 0]); 
//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
//		  } else if (alpha>40.0 & alpha <140.0){
//			  /**TODO*/
//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
//			  mat4.rotate(mvMatrix, -degToRad(alpha), [0, 1, 0]); 
//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
//		  }
		  /**END OF gz */
	  }
	} else if  (mobileType=="galaxy_tab_note3") {
		/**
		 * CALIBRATED FOR GALAXY NOTE 3
		 * TODO GZ <0 ALL SCENARIOS 
		 * TODO POTRATAIT MODE AT 90 AND 270
		 */
		 if(gz >6.0 & gz > gy  & gz > gx) {
			  //beta <45 at this points beta sesnsor value is negative
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } 
		  } else if  (gz < (-6.0) & gz < gx & gz < gy ) {
			//beta >135 at this points beta sesnsor value is negative
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = degToRad(beta); malpha = -degToRad(alpha+180); mgamma =degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha-180); mgamma =degToRad(gamma);
			  }
			  /**END OF gz */
		  } else if(gx > 6.0 & gx > gy & gx > gz){
			  if(gz >0 ) {
					  beta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
		  	} else{
		  		 malpha = -degToRad(alpha); mgamma =degToRad(180-gamma);
		  		 if(beta < 0)
					mbeta = -degToRad(beta+180);
				else
					mbeta = -degToRad(beta-180); 
		  	}
		  } else if  (gx < (-6.0) & gx < gy & gx < gz ) {
			//beta >135 at this points beta sesnsor value is negative
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = degToRad(beta); malpha = degToRad(alpha-180); mgamma = -degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = -degToRad(beta); malpha = degToRad(alpha-180); mgamma = -degToRad(gamma);
			  }
			  /**END OF gx */
		  } else if (gy >6.0 & gy > gz  & gy > gx) {
			  
			 // if((alpha<40.0 & alpha >0) || (alpha<360.0 & alpha >320.0)) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			 // } else  if(alpha>140.0 & alpha <220.0) {
			  } else  if(alpha>90.0 & alpha <270.0) {
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  }
			  //else if (alpha>220.0 & alpha <320.0){
	//			  /**TODO*/
	//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
	//			  mat4.rotate(mvMatrix, degToRad(alpha), [0, 1, 0]); 
	//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
	//		  } else if (alpha>40.0 & alpha <140.0){
	//			  /**TODO*/
	//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
	//			  mat4.rotate(mvMatrix, degToRad(alpha), [0, 1, 0]); 
	//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
	//		  }
		  } else if  (gy < (-6.0) & gy < gx & gy < gz ) {
			//beta >135 at this points beta sesnsor value is negative
			  //if((alpha<40.0 & alpha >0) || (alpha<360.0 & alpha >320.0)) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } //else if (alpha>220.0 & alpha <320.0){
				  /**TODO*/
	//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
	//			  mat4.rotate(mvMatrix, degToRad(alpha), [0, 1, 0]); 
	//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
	//		  } else if (alpha>40.0 & alpha <140.0){
	//			  /**TODO*/
	//			  mat4.rotate(mvMatrix, degToRad(beta), [1, 0, 0]);
	//			  mat4.rotate(mvMatrix, -degToRad(alpha), [0, 1, 0]); 
	//			  mat4.rotate(mvMatrix, degToRad(gamma), [0, 0, 1]);
	//		  }
			  /**END OF gz */
		  }
	}
	  mat4.rotate(mvMatrix, mbeta, [1, 0, 0]);// beta value -	
	  mat4.rotate(mvMatrix, malpha, [0, 1, 0]); //Mobile phones z is opengl y axis
	  mat4.rotate(mvMatrix, mgamma, [0, 0, 1]);// Mobile Phones y axis opengl z axis
	  
	  gl.useProgram(shaderProgram4Tex);
	  gl.activeTexture(gl.TEXTURE0);
	  gl.bindTexture(gl.TEXTURE_2D, topSurfaceTexture);
	  gl.uniform1i(shaderProgram4Tex.samplerUniform, 0);
	  //Drawing the top Surface with the texture
	  
	  gl.bindBuffer(gl.ARRAY_BUFFER, topFaceTextureVerticesBuffer);
	  gl.vertexAttribPointer(shaderProgram4Tex.vertexPositionAttribute, topFaceTextureVerticesBuffer.itemSize, gl.FLOAT, false, 0, 0);
	  
	  gl.bindBuffer(gl.ARRAY_BUFFER,topTextureCoordinateBuffer);
	  gl.vertexAttribPointer(shaderProgram4Tex.vertexTextureAttribute, topTextureCoordinateBuffer.itemSize, gl.FLOAT, false, 0, 0);	 
	  
	  gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, topTextureSurfaceIndexBuffer);
	  setMatrixUniforms(shaderProgram4Tex);	  
	  gl.drawElements(gl.TRIANGLES,  topTextureSurfaceIndexBuffer.itemSize , gl.UNSIGNED_SHORT, 0);
	  
//	  gl.bindBuffer(gl.ARRAY_BUFFER, topTextureSurfaceNormalsBuffer);
//	  gl.vertexAttribPointer(shaderProgram4Tex.vertexNormalAttribute, topTextureSurfaceNormalsBuffer.itemSize, gl.FLOAT, false, 0, 0);
//
//	  //drawing the bottom surface with the texture
//	  
	  gl.activeTexture(gl.TEXTURE0);
	  gl.bindTexture(gl.TEXTURE_2D, bottomSurfaceTexture);
	  gl.uniform1i(shaderProgram4Tex.samplerUniform, 0);
	  gl.bindBuffer(gl.ARRAY_BUFFER, bottomFaceTextureVerticesBuffer);
	  gl.vertexAttribPointer(shaderProgram4Tex.vertexPositionAttribute, bottomFaceTextureVerticesBuffer.itemSize, gl.FLOAT, false, 0, 0);
	  
	  gl.bindBuffer(gl.ARRAY_BUFFER,bottomTextureCoordinateBuffer);
	  gl.vertexAttribPointer(shaderProgram4Tex.vertexTextureAttribute, bottomTextureCoordinateBuffer.itemSize, gl.FLOAT, false, 0, 0);	 
	  
	  gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, topTextureSurfaceIndexBuffer);
	  setMatrixUniforms(shaderProgram4Tex);	  
	  gl.drawElements(gl.TRIANGLES,  bottomTextureSurfaceIndexBuffer.itemSize , gl.UNSIGNED_SHORT, 0);
	  
//	  gl.bindBuffer(gl.ARRAY_BUFFER, bottomTextureSurfaceNormalsBuffer);
//	  gl.vertexAttribPointer(shaderProgram4Tex.vertexNormalAttribute, bottomTextureSurfaceNormalsBuffer.itemSize, gl.FLOAT, false, 0, 0);
	  
	  gl.useProgram(shaderProgram);
	  
	  gl.bindBuffer(gl.ARRAY_BUFFER, squareVerticesBuffer);
	  gl.vertexAttribPointer(shaderProgram.vertexPositionAttribute, squareVerticesBuffer.itemSize, gl.FLOAT, false, 0, 0);
	  
	  gl.bindBuffer(gl.ARRAY_BUFFER, squareVerticesColorBuffer);
	  gl.vertexAttribPointer(shaderProgram.vertexColorAttribute, squareVerticesColorBuffer.itemSize, gl.FLOAT, false, 0, 0);
	  
	  gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, cubeVerticesIndexBuffer);
	  setMatrixUniforms(shaderProgram);	  
	  gl.drawElements(gl.TRIANGLES,  cubeVerticesIndexBuffer.itemSize , gl.UNSIGNED_SHORT, 0);
	  mvPopMatrix();
	  
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

function setMatrixUniforms(shader) {
	gl.uniformMatrix4fv(shader.pMatrixUniform, false, pMatrix);
	gl.uniformMatrix4fv(shader.mvMatrixUniform, false, mvMatrix);
}

function setMatrixUniformsforTexture(shader) {
	gl.uniformMatrix4fv(shader.pMatrixUniform, false, pMatrix);
	gl.uniformMatrix4fv(shader.mvMatrixUniform, false, mvMatrix);	
    var normalMatrix = mat4.create();
    var tMatrix = mat4.create();
    mat4.inverse(mvMatrix, normalMatrix);
    mat4.transpose(normalMatrix, tMatrix);
    gl.uniformMatrix4fv(shader.normalMatrix, false, new Float32Array(tMatrix));    
}

function setMobileType(id){
    mobileType = id;
    start();
}

function handleOrientationChanges(a, b, g ){
	//dAPI.log(a +":"+b+":"+g);
	
	alpha = a;
	beta = b;
	gamma = g;
	document.getElementById("data").innerHTML = "<TABLE BORDER=\"1\"><TR><TD COLSPAN=3>"+alpha+"</TD></TR><TR><TD COLSPAN=3>"+beta+"</TD></TR><TR><TD>"+gamma+"</TD><TD COLSPAN=3></TR><TR><TD COLSPAN=3>"+gx+"</TD></TR><TR><TD COLSPAN=3 >"+gy+"</TD></TR><TR><TD COLSPAN=3>"+gz+"</TD></TR></TABLE>";
//	dAPI.log(alpha+":"+beta+":"+gamma+":"+gx+":"+gy+":"+gz);	
//	dAPI.log(alpha+":"+beta+":"+gamma+":"+gx+":"+gy+":"+gz);
}

var handlacceleration = function(event) {
	
};

var handleAccelerationWithGravity = function(event) {
	gx = event.x  ;
	gy = event.y  ;
	gz = event.z;
};

var handleRotation = function(event) {
	
};