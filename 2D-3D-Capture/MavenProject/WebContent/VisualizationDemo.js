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
var translateX = 0;
var translateY = 0;
var translateZ = 0;
var rotateX = 0;
var rotateY = 0;
var rotateZ = 0;


var onSocketOpen =  function() {

};

var onMessageArrive = function(event) {
	alert("Message arrived");
	var svrmessage=window.atob(event.data);
	console.log(svrmessage);
	if(svrmessage)
		svrmessage= svrmessage.trim();
	if(svrmessage=='READY') {
//		console.log("Server Ready");
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
					run = false;
					console.log("Event recieved");
					dataList.push(JSONObj);
					var a = dataList.length;		
					var i;
					jsonData= dataList[a-1];	
					initTextures(jsonData["url"],a-1);
					var a = dataList.length;		
//					var i;
					if(a > 0) {						
						var centerLong=0.0;
						var centerLat= 0.0;
						for(var i = 0; i < a ; i++ ){
							jsonData= dataList[i];	
							centerLong += jsonData["long"];
							centerLat +=  jsonData["lat"];
						}
						longitude = centerLong/a;
						latitude = centerLat/a ;
						console.log(longitude+""+ latitude+""+ jsonData["alpha"]+""+ jsonData["beta"]+""+ jsonData["gamma"]);
						initEarthTexture();
					}		
					run = true;
					requestAnimationFrame(renderScene);
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
	run = false;
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
		var a = dataList.length;		
//		var i;
		if(a > 0) {
			
			var centerLong=0.0;
			var centerLat= 0.0;
			for(var i = 0; i < a ; i++ ){
				jsonData= dataList[i];	
				initTextures(jsonData["url"],i);
				centerLong += jsonData["long"];
				centerLat +=  jsonData["lat"];
			}
			longitude = centerLong/a;
			latitude = centerLat/a ;
			console.log(longitude);
			console.log(latitude);
			initEarthTexture();
		}		
		requestAnimationFrame(renderScene);
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
var earthTexture;
var longitude = 25.4579856;
var latitude = 65.0599453 ;
function initTextures(url,i){	
		imageTexture[i] = gl.createTexture();
		imageTexture[i].Image = new Image();
		imageTexture[i].Image.onload = function() {
			handleTextureLoaded( imageTexture[i]);
		};
		imageTexture[i].Image.src = url;	
}

function initEarthTexture() {
	var imageURL = latLonToTM35BB(latitude ,longitude);
//	console.log(imageURL);
	earthTexture = gl.createTexture();
	earthTexture.Image = new Image();
	earthTexture.Image.crossOrigin = "Anonymous";
	earthTexture.Image.onload = function() {
		handleTextureLoaded( earthTexture);
	};		
	earthTexture.Image.src = imageURL;	
}

function handleTextureLoaded(texture){	
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

var imageSquareVerticesBuffer;
var imageSquareVerticesBufferLandscape;
var textureCoordinateBuffer;
var textureCoordinateBufferLandscape;
var surfaceIndexBuffer;

var earthSurfaceVirticesBuffer;
function initBuffers(oriantation,x,y) {
	//Initiating buffers to draw the gis texture
	var earthSurfaceVirtices =  [    
							-22.6,  -10.0, 29.0,
							27.4,  -10.0, 19.0,
							27.4, -10.0, -21.0,
							-22.6, -10.0, -21.0,
							];
	earthSurfaceVirticesBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, earthSurfaceVirticesBuffer);
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(earthSurfaceVirtices), gl.STATIC_DRAW);
	earthSurfaceVirticesBuffer.itemSize = 3;
	
	//These buffers are to draw the images
	imageSquareVerticesBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, imageSquareVerticesBuffer);  
	
	var imageVerticesBuffer;	
		imageVerticesBuffer = [
	                            0.0, 0.0, 8.0,
	                            4.8, 0.0, 8.0,
	                            4.8,  0.0, 0.0,
		                         0.0,  0.0, 0.0,		                         
		                    ];  
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(imageVerticesBuffer), gl.STATIC_DRAW);
  imageSquareVerticesBuffer.itemSize = 3;
  
  var textureCoordinatesLandscape = [                                     
                                 	0.0,  1.0, 
                                 	0.0,  0.0,
                                 	1.0,  0.0,
                                 	1.0,  1.0,
                                 ];

  textureCoordinateBufferLandscape = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, textureCoordinateBufferLandscape);
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(textureCoordinatesLandscape), gl.STATIC_DRAW);
  textureCoordinateBufferLandscape.itemSize = 2;
  
  var textureCoordinates = [
                            0.0,  0.0,
                            1.0,  0.0,
                            1.0,  1.0,
                        	0.0,  1.0,                        	
                        ];
  textureCoordinateBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, textureCoordinateBuffer);
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(textureCoordinates), gl.STATIC_DRAW);
  textureCoordinateBuffer.itemSize = 2;

  var texturedSurfaceIndices = [0,  1, 2,     0, 2, 3, ];
  surfaceIndexBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, surfaceIndexBuffer);
  gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(texturedSurfaceIndices), gl.STATIC_DRAW);
  surfaceIndexBuffer.itemSize = 6;
}

var latLonToTM35BB = function(lat, lon) {
	var xy_lowerleft = latLonToTM35(lat - 0.001, lon - 0.001);
	var xy_upperright = latLonToTM35(lat + 0.001, lon + 0.001);
	console.log(xy_lowerleft[0]+"+"+ xy_lowerleft[1]+"+"+ xy_upperright[0] +"+"+xy_upperright[1]);
	//var img_url = "http://dev2.cyberlightning.com:9091/geoserver/fiware/wms?service=WMS&version=1.1.0&request=GetMap&layers=imagemosaic_texture_aerial&styles=&bbox="+307039.96875+","+7143599.5+","+500960.03125+"'"+7628400.5+"&width=204&height=512&srs=EPSG:3047&format=image/jpeg";
	var img_url = "http://dev2.cyberlightning.com:9091/geoserver/fiware/wms?service=WMS&version=1.1.0&request=GetMap&layers=imagemosaic_texture_aerial&styles=&bbox="+xy_lowerleft[0]+","+xy_lowerleft[1]+","+xy_upperright[0]+","+xy_upperright[1]+"&width=1024&height=1024&srs=EPSG:3047&format=image/png";
//	document.getElementById("map").setAttribute("src", img_url);
	return img_url;
};

var init = function (){
	
	var xy = latLonToTM35(65.0599453 , 25.4579856);
//	console.log(xy[0] + ":" + xy[1]);
	//var img_url = "http://dev2.cyberlightning.com:9091/geoserver/fiware/wms?service=WMS&version=1.1.0&request=GetMap&layers=imagemosaic_texture_aerial&styles=&bbox="+307039.96875+","+7143599.5+","+500960.03125+"'"+7628400.5+"&width=204&height=512&srs=EPSG:3047&format=image/jpeg";
	//var img_url = "http://dev2.cyberlightning.com:9091/geoserver/fiware/wms?service=WMS&version=1.1.0&request=GetMap&layers=imagemosaic_texture_aerial&styles=&bbox="+307039.96875+","+7143599.5+","+500960.03125+"'"+7628400.5+"&width=204&height=512&srs=EPSG:3047&format=image/jpeg";
	
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
	var jsonObject1 = { "type" : 3, "url" : "http://localhost:9090/TwoDThreeDCapture/img/image_2014.3.26_14.0.34.png", "alpha" : 3.3859145141030798 , "beta" :1.3933574283908932 , "gamma" :0.04131357637850719, "lat" :65.0599453 , "long" :25.4579986, "height" : 800 , "width": 480};
	var jsonObject2 = { "type" : 3, "url" : "http://localhost:9090/TwoDThreeDCapture/img/image_2014.3.26_14.1.26.png", "alpha" : 2.390337280631597 , "beta" :1.3749476954408568 , "gamma" :0.032918025540465044, "lat" :65.0600162 , "long" :25.4579898, "height" : 800 , "width": 480};
	var jsonObject3 = { "type" : 3, "url" : "http://localhost:9090/TwoDThreeDCapture/img/image_2014.3.26_14.2.1.png", "alpha" : 1.4777944804715855 , "beta" :1.424167725676349 , "gamma" :0.01364509387591418, "lat" :65.0600286 , "long" :25.45800235, "height" : 800 , "width": 480};
	var jsonObject4 = { "type" : 3, "url" : "http://localhost:9090/TwoDThreeDCapture/img/image_2014.3.26_14.2.35.png", "alpha" : 0.5716762901252813 , "beta" :1.390966327315661 , "gamma" :0.0030808989730785083, "lat" :65.0600286 , "long" :25.4580023, "height" : 800 , "width": 480};
	var jsonObject5 = { "type" : 3, "url" : "http://localhost:9090/TwoDThreeDCapture/img/image_2014.3.26_14.3.13.png", "alpha" : 5.914111127510955 , "beta" :1.389697472949461 , "gamma" : 0.02570614161604383, "lat" :65.0600323 , "long" :25.4579533, "height" : 800 , "width": 480};
	var jsonObject6 = { "type" : 3, "url" : "http://localhost:9090/TwoDThreeDCapture/img/image_2014.3.26_14.3.52.png", "alpha" : 5.281239123961179 , "beta" :1.4151216841632623 , "gamma" :0.002300589392719064, "lat" :65.0600313 , "long" :25.45795965, "height" : 800 , "width": 480};
	var jsonObject7 = { "type" : 3, "url" : "http://localhost:9090/TwoDThreeDCapture/img/image_2014.3.26_16.9.22.png", "alpha" : -2.06037339928407 , "beta" :-0.006981317007977318 , "gamma" :-1.462015190505846, "lat" :65.0600449 , "long" :25.4579665, "height" : 480 , "width": 800};
	var jsonObject8 = { "type" : 3, "url" : "http://localhost:9090/TwoDThreeDCapture/img/image_2014.3.26_16.20.59.png", "alpha" : 1.5916983899167805 , "beta" :0.004331907203449926 , "gamma" :-1.4173382523132951, "lat" :65.0600271 , "long" :25.457983, "height" : 480 , "width": 800};
	var jsonObject9 = { "type" : 3, "url" : "http://localhost:9090/TwoDThreeDCapture/img/image_2014.3.26_16.43.47.png", "alpha" : -1.4946964807494394 , "beta" :-0.03322757829946805 , "gamma" :-1.3621107987924386, "lat" :65.05994712 , "long" :25.45819176, "height" : 480 , "width": 800};

	dataList.push(jsonObject1);
	dataList.push(jsonObject2);
	dataList.push(jsonObject3);
	dataList.push(jsonObject4);
	dataList.push(jsonObject5);
	dataList.push(jsonObject6);
//	dataList.push(jsonObject7);
//	dataList.push(jsonObject8);
//	dataList.push(jsonObject9);
	startWebGl();
};

function drawScene(){	
	gl.useProgram(shaderProgram);		
	var a = dataList.length;		
	var i;
	gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
	mat4.perspective(90 , gl.viewportWidth / gl.viewportHeight, 0.1, 100.0, pMatrix);
	mat4.identity(mvMatrix);		
	mat4.rotate(mvMatrix,rotateY , [0, 1, 0]); //Mobile phones z is opengl y axis
	mat4.rotate(mvMatrix, rotateZ, [0, 0, 1]);// Mobile Phones y axis opengl z axis
	mat4.rotate(mvMatrix, rotateX, [1, 0, 0]);// beta value -
	mat4.translate(mvMatrix, [translateX , translateY, -20.0+translateZ]);	
	mvPushMatrix();
	if(a > 0)
		drawEarth();
		for(i = 0; i < a ; i++ ){
			mvPushMatrix();			
			//Mobile phones z is opengl y axis
			var orientation;
			if(dataList[i]["height"] > dataList[i]["width"]){
				mat4.rotate(mvMatrix,dataList[i]["alpha"] , [0, 1, 0]); //Mobile phones z is opengl y axis
				orientation = "p";
			} else {
				mat4.rotate(mvMatrix,dataList[i]["alpha"]+3.14159 , [0, 1, 0]); //Mobile phones z is opengl y axis
//				mat4.rotate(mvMatrix,dataList[i]["alpha"] , [0, 1, 0]); //Mobile phones z is opengl y axis
				orientation = "l";
			}
			mat4.rotate(mvMatrix, dataList[i]["gamma"], [0, 0, 1]);// Mobile Phones y axis opengl z axis
			mat4.rotate(mvMatrix, dataList[i]["beta"], [1, 0, 0]);
			mat4.translate(mvMatrix, [0.0 , -6.0, 0.0]);			
			drawImage(i,orientation);
			mvPopMatrix();
		}	
	mvPopMatrix();
}

var drawEarth = function() {
  	gl.activeTexture(gl.TEXTURE0);
  	gl.bindTexture(gl.TEXTURE_2D, earthTexture );
  	gl.uniform1i(shaderProgram.samplerUniform, 0);
  	
  	gl.bindBuffer(gl.ARRAY_BUFFER, earthSurfaceVirticesBuffer);
  	gl.vertexAttribPointer(shaderProgram.vertexPositionAttribute, earthSurfaceVirticesBuffer.itemSize, gl.FLOAT, false, 0, 0);
  	
  	gl.bindBuffer(gl.ARRAY_BUFFER,textureCoordinateBuffer);
  	gl.vertexAttribPointer(shaderProgram.vertexTextureAttribute, textureCoordinateBuffer.itemSize, gl.FLOAT, false, 0, 0);
  	
  	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, surfaceIndexBuffer);
  	setMatrixUniforms(shaderProgram);	  
  	gl.drawElements(gl.TRIANGLES,  surfaceIndexBuffer.itemSize , gl.UNSIGNED_SHORT, 0);
};

var drawImage = function(i,orientation){
//	gl.useProgram(shaderProgram);
  	gl.activeTexture(gl.TEXTURE0);
  	gl.bindTexture(gl.TEXTURE_2D, imageTexture[i]);
//  	gl.bindTexture(gl.TEXTURE_2D, earthTexture);
  	gl.uniform1i(shaderProgram.samplerUniform, 0);
  	
  	gl.bindBuffer(gl.ARRAY_BUFFER, imageSquareVerticesBuffer);
  	gl.vertexAttribPointer(shaderProgram.vertexPositionAttribute, imageSquareVerticesBuffer.itemSize, gl.FLOAT, false, 0, 0);
  	if(orientation == "p") {
  		//console.log("p");
  		gl.bindBuffer(gl.ARRAY_BUFFER,textureCoordinateBuffer);
  		gl.vertexAttribPointer(shaderProgram.vertexTextureAttribute, textureCoordinateBuffer.itemSize, gl.FLOAT, false, 0, 0);
  	} else {
  		//console.log("h");
  		gl.bindBuffer(gl.ARRAY_BUFFER,textureCoordinateBufferLandscape);
  		gl.vertexAttribPointer(shaderProgram.vertexTextureAttribute, textureCoordinateBufferLandscape.itemSize, gl.FLOAT, false, 0, 0);
  	}
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
//	console.log(event.keyCode);
    currentlyPressedKeys[event.keyCode] = true;
    handleKeys();
}

function handleKeyUp(event) {
//	console.log(event.keyCode);
    currentlyPressedKeys[event.keyCode] = false;
    handleKeys();
}

var translateX = 0;
var translateY = 0;
var translateZ = 0;
var rotateX = 0;
var rotateY = 0;
var rotateZ = 0;
var run = true;
function handleKeys() {
	    if (currentlyPressedKeys[33]) { // Page Up
	    	translateY -= 0.2;
	    }
	    if (currentlyPressedKeys[34]) { // Page Down
	    	translateY += 0.2;
	    }
	    if (currentlyPressedKeys[37]) { // Left cursor key
	    	translateX -=0.3;
	    }
	    if (currentlyPressedKeys[39]) { // Right cursor key
	    	translateX +=  0.3;	
	    }
	    if (currentlyPressedKeys[38]) { // Up cursor key
	    	translateZ += 0.3;
	    }
	    if (currentlyPressedKeys[40]) { // Down cursor key
	    	translateZ += -0.3;
	    }
	    if (currentlyPressedKeys[67]) { // C
	    	rotateZ -= 0.1;
	    }
	    if (currentlyPressedKeys[65]) { // A
	    	rotateZ += 0.1;
	    }
	    if (currentlyPressedKeys[85]) { // U
	    	rotateX -=0.1;
	    }
	    if (currentlyPressedKeys[68]) { // D
	    	rotateX +=  0.1;
	    }
	    if (currentlyPressedKeys[82]) { // U
	    	rotateY -=0.1;
	    }
	    if (currentlyPressedKeys[76]) { // D
	    	rotateY +=  0.1;
	    }
}

var renderScene = function (){
	handleKeys();
	drawScene();
	if(run)
		requestAnimationFrame(renderScene);	
};

function latLonToTM35(lat, lon) {

    var xy = new Array(2);

    // Constants
    var F = 1 / 298.257222101, // Ellipsoidin litistyssuhde
        A = 6378137,// Isoakselin puolikas
        LAMBDA_ZERO = 0.471238898,// Keskimeridiaani (rad), 27 astetta
        K_ZERO = 0.9996, // Mittakaavakerroin
        E_ZERO = 500000; // Itäkoordinaatti

    // Kaavat

    // Muunnetaan astemuotoisesta radiaaneiksi
    var fii = degToRad(lat),
        lambda = degToRad(lon);

    var n = F / (2 - F),
        a1 = (A / (1 + n)) * (1 + (Math.pow(n, 2) / 4) + (Math.pow(n, 4) / 64)),
        e_toiseen = (2 * F) - Math.pow(F, 2),
        e_pilkku_toiseen = e_toiseen / (1 - e_toiseen),
        h1_pilkku = (1 / 2) * n - (2 / 3) * Math.pow(n, 2) + (5 / 16) * Math.pow(n, 3) + (41 / 180) * Math.pow(n, 4),
        h2_pilkku = (13 / 48) * Math.pow(n, 2) - (3 / 5) * Math.pow(n, 3) + (557 / 1440) * Math.pow(n, 4),
        h3_pilkku = (61 / 240) * Math.pow(n, 3) - (103 / 140) * Math.pow(n, 4),
        h4_pilkku = (49561 / 161280) * Math.pow(n, 4),
        Q_pilkku = asinh(Math.tan(fii)),
        Q_2pilkku = atanh(Math.sqrt(e_toiseen) * Math.sin(fii)),
        Q = Q_pilkku - Math.sqrt(e_toiseen) * Q_2pilkku,
        l = lambda - LAMBDA_ZERO,
        beeta = Math.atan(sinh(Q)),
        eeta_pilkku = atanh(Math.cos(beeta) * Math.sin(l)),
        zeeta_pilkku = Math.asin(Math.sin(beeta) / (1 / cosh(eeta_pilkku))),
        zeeta1 = h1_pilkku * Math.sin(2 * zeeta_pilkku) * cosh(2 * eeta_pilkku),
        zeeta2 = h2_pilkku * Math.sin(4 * zeeta_pilkku) * cosh(4 * eeta_pilkku),
        zeeta3 = h3_pilkku * Math.sin(6 * zeeta_pilkku) * cosh(6 * eeta_pilkku),
        zeeta4 = h4_pilkku * Math.sin(8 * zeeta_pilkku) * cosh(8 * eeta_pilkku),
        eeta1 = h1_pilkku * Math.cos(2 * zeeta_pilkku) * sinh(2 * eeta_pilkku),
        eeta2 = h2_pilkku * Math.cos(4 * zeeta_pilkku) * sinh(4 * eeta_pilkku),
        eeta3 = h3_pilkku * Math.cos(6 * zeeta_pilkku) * sinh(6 * eeta_pilkku),
        eeta4 = h4_pilkku * Math.cos(8 * zeeta_pilkku) * sinh(8 * eeta_pilkku),
        zeeta = zeeta_pilkku + zeeta1 + zeeta2 + zeeta3 + zeeta4,
        eeta = eeta_pilkku + eeta1 + eeta2 + eeta3 + eeta4;

    // Tulos tasokoordinaatteina
    xy[0] = a1 * eeta * K_ZERO + E_ZERO;
    xy[1] = a1 * zeeta * K_ZERO;

    return xy;
}

function radToDeg(rad) {
    return (rad / pi * 180.0);
}

function asinh(arg) {
    // From: http://phpjs.org/functions
    // +   original by: Onno Marsman
    // *     example 1: asinh(8723321.4);
    // *     returns 1: 16.67465779841863
    return Math.log(arg + Math.sqrt(arg * arg + 1));
}

function atanh(arg) {
    // From: http://phpjs.org/functions
    // +   original by: Onno Marsman
    // *     example 1: atanh(0.3);
    // *     returns 1: 0.3095196042031118
    return 0.5 * Math.log((1 + arg) / (1 - arg));
}

function sinh(arg) {
    // From: http://phpjs.org/functions
    // +   original by: Onno Marsman
    // *     example 1: sinh(-0.9834330348825909);
    // *     returns 1: -1.1497971402636502
    return (Math.exp(arg) - Math.exp(-arg)) / 2;
}

function cosh(arg) {
    // From: http://phpjs.org/functions
    // +   original by: Onno Marsman
    // *     example 1: cosh(-0.18127180117607017);
    // *     returns 1: 1.0164747716114113
    return (Math.exp(arg) + Math.exp(-arg)) / 2;
}