/**
 * 
 */

var websocket;
var subID;
var remoteURL="dev.cyberlightning.com";
var wsport="17322";
var dataList =[];
var x = 4.8;
var y = 0.0;
var z = 6.4;


var onSocketOpen =  function() {

};

var onMessageArrive = function(event) {
	alert("Message arrived");
	var svrmessage=window.atob(event.data);
	console.log(svrmessage);
	if(svrmessage)
		svrmessage= svrmessage.trim();
	if(svrmessage=='READY') {
		console.log("Server Ready");
	} else {
		var temp = JSON.stringify(eval("(" + svrmessage + ")"));
		var JSONObj = JSON.parse(temp);
		var message_type = JSONObj['type'];
		if(message_type) {
			switch(message_type){
				case 1 :
					subRes = JSONObj["subscribe"];
					if(subRes=="True") {
						subID = JSONObj["sub_id"];
						console.log(subRes+":"+subID);
					} else 
						console.log("ERROR");
					break;
				case 2 :
					console.log("Unsuscribed");
					break;
				case 3:
					console.log("Event recieved");
					dataList.push(JSONObj);
					requestAnimationFrame(drawScene);
					break;
			}				
		}
	}	
};

var onWebSocketError = function (error) {
	console.log("Error Occured");
};

var onSocketClose = function() {
	console.log("Conenction Closed");
};

var subscribeAny = function() {
	if(!subID) {
		var subscriptionMessage = { "type":"subscribe" , "data" : {	'type' : "Any"}};
		websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
	}
};

var subscribeLocal= function() {
	if(!subID) {
		var subscriptionMessage = { "type":"subscribe" , "data" : {	'type' : "Local","data" : {"lon" :"2.1","lat":"1.2", "roll":"0.0", "pitch": "0.0", "yaw": "0.0"}}};
		websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
	}
	else 
		alert("subscription exist");
}; 

subscribeDirectional= function() {
	if(!subID) {
		var subscriptionMessage = { "type":"subscribe" , "data" : {	'type' : "Directional","data" : {"lon" :"2.1","lat":"1.2", "roll":"0.0", "pitch": "0.0", "yaw": "0.0"}}};
		websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
	} else 
		alert("subscription exist");
};

var sendAllAnEvent = function() {
	var subscriptionMessage = { "type":"test" , "data" : {	'type' : "publish" }};
	websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
};

var sendMeAnEvent = function() {
	var subscriptionMessage = { "type":"test" , "data" : {	'type' : "single", 'sub_id' : subID }};
	websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
};

var unsubscribe = function() {
	if (subID) {
		var subscriptionMessage = { "type":"unsubscribe" , "sub_id" : subID };
		websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
	} else 
		console.log("No subscription found");
	subID = null;
};

var gl;
var startWebGl = function(){
	var canvas = document.getElementById("glcanvas");
	gl = initWebGL(canvas);
	if(gl){
		gl.clearColor(0.0, 1.0, 1.0, 1.0);                      // Set clear color to black, fully opaque
	    gl.enable(gl.DEPTH_TEST);   
	 	gl.clearDepth(1.0);                         // Enable depth testing
	    gl.depthFunc(gl.LEQUAL); 
		initShaders();
		initBuffers();
//		initTextures("http://dev.cyberlightning.com/~twijethilake/images/image_2013.11.5_15.41.24.png");
//		setInterval(drawScene, 4000);
		requestAnimationFrame(drawScene);
	}
	else {
		alert("ERROR");
	}
};

var initWebGL = function(canvas){
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
	  return gl;	  
};

var imageTexture= [] ;
function initTextures(url,i){
	imageTexture[i] = gl.createTexture();
	imageTexture[i].Image = new Image();
	imageTexture[i].Image.onload = function() {
		handleTextureLoaded( imageTexture[i]);
	};
//	console.log(url);
	imageTexture[i].Image.src = url;
}

function handleTextureLoaded(texture){	
	//console.log("handleTextureLoaded, image = " + image);
	gl.bindTexture(gl.TEXTURE_2D, texture);
	gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);
	gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, texture.Image);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE); 
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
	gl.bindTexture(gl.TEXTURE_2D, null);
}

var getShader = function getShader(gl, id) {
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
		     // Unknown shader type
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
};

var shaderProgram;
var initShaders = function(){
	var fragmentShader = getShader(gl, "shader-fs-texture");
	var vertexShader = getShader(gl, "shader-vs-texture");
	
	shaderProgram = gl.createProgram();
	gl.attachShader(shaderProgram, vertexShader);
	gl.attachShader(shaderProgram, fragmentShader);
	gl.linkProgram(shaderProgram);
	  
	  // If creating the shader program failed, alert
	  
	if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
	    alert("Unable to initialize the shader program.");
	}
	  
	gl.useProgram(shaderProgram);	
	shaderProgram.vertexPositionAttribute = gl.getAttribLocation(shaderProgram, "aVertexPosition");
	gl.enableVertexAttribArray(shaderProgram.vertexPositionAttribute);	  
	shaderProgram.vertexTextureAttribute = gl.getAttribLocation(shaderProgram, "aTextureCoord");
	gl.enableVertexAttribArray(shaderProgram.vertexTextureAttribute);	  
	shaderProgram.pMatrixUniform = gl.getUniformLocation(shaderProgram, "uPMatrix");
	shaderProgram.mvMatrixUniform = gl.getUniformLocation(shaderProgram, "uMVMatrix");
	gl.enableVertexAttribArray(shaderProgram.vertexNormalAttribute);
	shaderProgram.samplerUniform = gl.getUniformLocation(shaderProgram, "uSampler");
};

var horizAspect = 480.0/640.0;
var imageSquareVerticesBuffer;
var textureCoordinateBuffer;
var surfaceIndexBuffer;
var earthSurfaceVirtices;

function initBuffers() {
	imageSquareVerticesBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, imageSquareVerticesBuffer);
  
	 var imageVerticesBuffer = [    
	                         0.0,  0.0, 0.0,
	                         4.8,  0.0, 0.0,
	 						4.8, 0.0, 6.4,
	                         0.0, 0.0, 6.4,
	                    ];
	
//	 var imageVerticesBuffer = [ 
//	                            0.0,   0.0, 0.0,
//	                            6.4,  0.0, 0.0,
//	                            6.4, 4.8, 0.0,
//		                        0.0,  4.8, 0.0,
//		                        ];
  
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(imageVerticesBuffer), gl.STATIC_DRAW);
  imageSquareVerticesBuffer.itemSize = 3;
  
//  var textureCoordinates = [
//		0.0,  0.0,
//		1.0,  0.0,
//		1.0,  1.0,
//			0.0,  1.0,
//];
  
  var textureCoordinates = [
                        	0.0,  1.0,
                        	1.0,  1.0,
                        	1.0,  0.0,
                        	0.0,  0.0,
                        ];
  textureCoordinateBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, textureCoordinateBuffer);
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(textureCoordinates), gl.STATIC_DRAW);
  textureCoordinateBuffer.itemSize = 2;
//  
  var texturedSurfaceIndices = [0,  1, 2,     0, 2, 3, ];
  surfaceIndexBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, surfaceIndexBuffer);
  gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(texturedSurfaceIndices), gl.STATIC_DRAW);
  surfaceIndexBuffer.itemSize = 6;
}

var init = function (){
	window.requestAnimationFrame = window.requestAnimationFrame || window.mozRequestAnimationFrame || window.webkitRequestAnimationFrame || window.msRequestAnimationFrame;    	
    document.onkeydown = handleKeyDown;
    document.onkeyup = handleKeyUp;

	if('WebSocket' in window) {
		websocket = new WebSocket('ws://'+remoteURL+':'+wsport);		
		websocket.onopen =onSocketOpen ;
		websocket.onmessage= onMessageArrive;
		websocket.onerror = onWebSocketError;
		websocket.onclose = onSocketClose;
	} else  {
		console.log("Web Socket not supported");
	}
//	var jsonObject1 = { "type" : 3, "url" : "http://dev.cyberlightning.com/~twijethilake/images/image_2014.3.21_14.29.14.png", "alpha" : 3.3979 , "beta" :-0.7793449 , "gamma" :-0.0522080482, "lat" :65.0599453 , "long" :25.4579856, "height" : 800 , "width": 400};
	var jsonObject2 = { "type" : 3, "url" : "http://localhost:8080/TwoDThreeDCapture/image_2014.3.21_14.29.25.png", "alpha" : 3.386813331 , "beta" :-1.24438185989 , "gamma" :-0.023278890, "lat" :65.0599401 , "long" :30.319000244140625, "height" : 800 , "width": 400};
//	var jsonObject2 = { "type" : 3, "url" : "http://dev.cyberlightning.com/~twijethilake/images/image_2014.3.21_14.29.25.png", "alpha" : 3.386813331 , "beta" :-1.24438185989 , "gamma" :-0.023278890, "lat" :65.0599401 , "long" :30.319000244140625, "height" : 800 , "width": 400};
//	var jsonObject3 = { "type" : 3, "url" : "http://dev.cyberlightning.com/~twijethilake/images/image_2014.3.21_14.29.42.png", "alpha" : 2.696459658452935 , "beta" :-1.2728902004106164 , "gamma" :0.04329483091028244, "lat" :65.0599401 , "long" :30.319000244140625, "height" : 800 , "width": 400};
//	var jsonObject4 = { "type" : 3, "url" : "http://dev.cyberlightning.com/~twijethilake/images/image_2014.3.21_14.30.3.png", "alpha" : 4.150500872529886 , "beta" :-1.3727000185865514 , "gamma" :0.004470274535036375, "lat" :65.0599401 , "long" :30.319000244140625, "height" : 800 , "width": 400};
//	dataList.push(jsonObject1);
	dataList.push(jsonObject2);
//	dataList.push(jsonObject3);
//	dataList.push(jsonObject4);
	startWebGl();
};

function drawScene(){
	gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
	mat4.perspective(60 , gl.viewportWidth / gl.viewportHeight, 0.1, 100.0, pMatrix);
	if(dataList.length <=0){
		jsonData = { "type" : 3, "url" : "http://dev.cyberlightning.com/~twijethilake/images/image_2014.2.4_9.27.5.png", "alpha" : 173.612 , "beta" :-45.6686 , "gamma" :6.6671, "lat" :65.0117567828 , "long" :25.4751403464, "height" : 480 , "width": 640};
		initTextures(jsonData["url"],0);
		setTimeout ( function(){
			gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
			mat4.perspective(60 , gl.viewportWidth / gl.viewportHeight, 0.1, 100.0, pMatrix);
	  		mat4.identity(mvMatrix); 
			mat4.translate(mvMatrix, [0.0, -0.0, -13.0]);		
		  
		 	 gl.useProgram(shaderProgram);
		  	gl.activeTexture(gl.TEXTURE0);
		  	gl.bindTexture(gl.TEXTURE_2D, imageTexture[0]);
		  	gl.uniform1i(shaderProgram.samplerUniform, 0);
		  
		  	gl.bindBuffer(gl.ARRAY_BUFFER, imageSquareVerticesBuffer);
		  	gl.vertexAttribPointer(shaderProgram.vertexPositionAttribute, imageSquareVerticesBuffer.itemSize, gl.FLOAT, false, 0, 0);
		  
		  	gl.bindBuffer(gl.ARRAY_BUFFER,textureCoordinateBuffer);
		  	gl.vertexAttribPointer(shaderProgram.vertexTextureAttribute, textureCoordinateBuffer.itemSize, gl.FLOAT, false, 0, 0);	 
		  
		  	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, surfaceIndexBuffer);
		  	setMatrixUniforms(shaderProgram);	  
		  	gl.drawElements(gl.TRIANGLES,  surfaceIndexBuffer.itemSize , gl.UNSIGNED_SHORT, 0);
		  	},100);
	} else {
		var a = dataList.length;		
		var i;
//		jsonData = { "type" : 3, "url" : "http://dev.cyberlightning.com/~twijethilake/images/image_2014.2.4_9.27.5.png", "alpha" : 173.612 , "beta" :-45.6686 , "gamma" :6.6671, "lat" :65.0117567828 , "long" :25.4751403464, "height" : 480 , "width": 640};
		for(i = 0; i < a ; i++ ){
			jsonData= dataList[i];	
			initTextures(jsonData["url"],i);
		}
		setTimeout ( function(){
			gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
			mat4.perspective(90 , gl.viewportWidth / gl.viewportHeight, 0.1, 100.0, pMatrix);
			mat4.identity(mvMatrix);
			mat4.translate(mvMatrix, [0 , -0.0, -20.0]);
			mvPushMatrix();
			var x = -7.0;
			for(i = 0; i < a ; i++ ){
				mvPushMatrix();
				console.log(dataList[i]["url"]+":"+dataList[i]["beta"] +":"+dataList[i]["alpha"]+":"+dataList[i]["gamma"]);				
				mat4.rotate(mvMatrix,dataList[i]["alpha"] , [0, 1, 0]); //Mobile phones z is opengl y axis
				mat4.rotate(mvMatrix, dataList[i]["gamma"], [0, 0, 1]);// Mobile Phones y axis opengl z axis
				mat4.rotate(mvMatrix, dataList[i]["beta"], [1, 0, 0]);// beta value -	
				mat4.translate(mvMatrix, [0.0 , -7.0, 0.0]);
				drawImage(i);
				mvPopMatrix();
			}
			mvPopMatrix();
		}, 100);		
	}
}

var drawImage = function(i){
	gl.useProgram(shaderProgram);
  	gl.activeTexture(gl.TEXTURE0);
  	gl.bindTexture(gl.TEXTURE_2D, imageTexture[i]);
  	gl.uniform1i(shaderProgram.samplerUniform, 0);
  
  	gl.bindBuffer(gl.ARRAY_BUFFER, imageSquareVerticesBuffer);
  	gl.vertexAttribPointer(shaderProgram.vertexPositionAttribute, imageSquareVerticesBuffer.itemSize, gl.FLOAT, false, 0, 0);
  
  	gl.bindBuffer(gl.ARRAY_BUFFER,textureCoordinateBuffer);
  	gl.vertexAttribPointer(shaderProgram.vertexTextureAttribute, textureCoordinateBuffer.itemSize, gl.FLOAT, false, 0, 0);	 
  
  	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, surfaceIndexBuffer);
  	setMatrixUniforms(shaderProgram);	  
  	gl.drawElements(gl.TRIANGLES,  surfaceIndexBuffer.itemSize , gl.UNSIGNED_SHORT, 0);
};

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

var currentlyPressedKeys = {};
function handleKeyDown(event) {
    currentlyPressedKeys[event.keyCode] = true;
}

function handleKeyUp(event) {
    currentlyPressedKeys[event.keyCode] = false;
}

function handleKeys() {
	    if (currentlyPressedKeys[33]) { // Page Up
	        z -= 0.05;
	    }
	    if (currentlyPressedKeys[34]) { // Page Down
	        z += 0.05;
	    }
	    if (currentlyPressedKeys[37]) { // Left cursor key
	        ySpeed -= 1;
	    }
	    if (currentlyPressedKeys[39]) { // Right cursor key
	        ySpeed += 1;
	    }
	    if (currentlyPressedKeys[38]) { // Up cursor key
	        xSpeed -= 1;
}
	    if (currentlyPressedKeys[40]) { // Down cursor key
	        xSpeed += 1;
	    }
}