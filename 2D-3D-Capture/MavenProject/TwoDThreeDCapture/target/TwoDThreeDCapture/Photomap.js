"use strict";
/*global Float32Array, Uint16Array */
/*jslint vars: true, white: true, browser: true */
var mapCenter = new Object();
var map;
var streetviewMap;
var imageList ;
var marker;
var panorama;

//Our context. Used everywhere, including GPU code
var gl = null;

// Cube geometry
var cubeVerts = new Float32Array([
    -0.75,  0.1,  1.5,   // 0 a
    -0.75, -0.1,  1.5,	//B
     0.75,  0.1,  1.5,	//C
     0.75, -0.1,  1.5,	//D
    -0.75,  0.1, -1.5,   // 4
    -0.75, -0.1, -1.5,
    -0.75,  0.1,  1.5,
    -0.75, -0.1,  1.5,
     0.75,  0.1, -1.5,   // 8
    -0.75,  0.1, -1.5,
     0.75,  0.1,  1.5,
    -0.75,  0.1,  1.5,
     0.75,  0.1,  1.5,   // 12
     0.75, -0.1,  1.5,
     0.75,  0.1, -1.5,
     0.75, -0.1, -1.5,
     0.75, -0.1,  1.5,   // 16
    -0.75, -0.1,  1.5,
     0.75, -0.1, -1.5,
    -0.75, -0.1, -1.5,
     0.75,  0.1, -1.5,   // 20
     0.75, -0.1, -1.5,
    -0.75,  0.1, -1.5,
    -0.75, -0.1, -1.5,
]);

var cubeNorms = new Float32Array([
     0,  0,  1,
     0,  0,  1,
     0,  0,  1,
     0,  0,  1,
    -1,  0,  0,
    -1,  0,  0,
    -1,  0,  0,
    -1,  0,  0,
     0,  1,  0,
     0,  1,  0,
     0,  1,  0,
     0,  1,  0,
     1,  0,  0,
     1,  0,  0,
     1,  0,  0,
     1,  0,  0,
     0, -1,  0,
     0, -1,  0,
     0, -1,  0,
     0, -1,  0,
     0,  0, -1,
     0,  0, -1,
     0,  0, -1,
     0,  0, -1,
]);

var cubeIdx = new Uint16Array([
     0,  1,  2,  1,  3,  2,
     4,  5,  6,  5,  7,  6,
     8,  9, 10,  9, 11, 10,
    12, 13, 14, 13, 15, 14,
    16, 17, 18, 17, 19, 18,
    20, 21, 22, 21, 23, 22,
]);

//Adding the 
var axisyVBuf = -1;

//The scene. Really ought to be an object
var cubeVBuf = -1;
var cubeNBuf = -1;
var cubeIdxBuf = -1;

var pMatrix  = mat4.create();
var mvMatrix = mat4.create();
var cubeColor = [ 0, 1, 0, 1 ];
var cubeSpinx  = 0;
var cubeSpiny  = 0;
var cubeSpinz  = 0;

// Shader program and handles to uniforms, attributes
// Also should be an object
var gpuShade = null;
var hProjectionMatrix = -1;
var hModelViewMatrix = -1;
var hNormalMatrix = -1;
var hLightPos = -1;
var hColor = -1;
var vaPosition = -1;
var vaNormal = -1;
var yaw;
var pitch;

var initGoogleMaps = function() {
		$("#loading-map").remove();
		var mapOptions = {
	          center: new google.maps.LatLng( mapCenter.lat, mapCenter.lon),
	          zoom: 8,
	          mapTypeId: google.maps.MapTypeId.ROADMAP
	        };
	    map= new google.maps.Map(document.getElementById("photoMap"), mapOptions);
};

function loadScript() {	
	  var script = document.createElement("script");
	  script.type = "text/javascript";
	  script.src = "https://maps.googleapis.com/maps/api/js?key=AIzaSyDqNrH21T3prB-yKHLiw8us-YHdVUOr5lQ&sensor=false&callback=initGoogleMaps";
	  document.body.appendChild(script);
	}

var getImageList = function(data){
	console.log(data);
	imageList = data['imageList'];
	var i;
	var avLon=0;
	var avLat=0;
	$("#loading-list").remove();
	for(i =0; i< imageList.length; i++ ){
		var image = JSON.parse(imageList[i]);
		avLat= avLat+image.latitude;
		avLon= avLon+image.longitude;
		var node=document.createElement("li");
		var link = document.createElement('button');
		link.setAttribute('id', i);
		link.onclick= reply_click;
		var textnode=document.createTextNode(image.imagename);
		link.appendChild(textnode);
		node.appendChild(link);
		document.getElementById("imglist").appendChild(node);
		console.log(image.imagename);
	}
	mapCenter.lat= avLat/i;
	mapCenter.lon= avLon/i;
	loadScript();
};

var makeRequest = function() {
	$('#photoList').append("<div id=\"loading-list\" class=\"tempicon\"><img src=\"img/loader.GIF\" alt=\"Loading...\" style= \" width: 25%; margin-left: 100px;margin-top: 300px;\"/></div>");
	$('#photoMap').append("<div id=\"loading-map\" class=\"tempicon\"><img src=\"img/loader.GIF\" style=\"	width: 12%;margin-top: 310px;margin-left: 240px;\"alt=\"Loading...\" /></div>");
	$.getJSON( "http://localhost:8080/RestClient/RestRequestMultiplexer", {command : "getAll", server : "dev.cyberlightning.com" }, getImageList);
	//$.getJSON( "http://localhost:8080/RestClient/RestRequestMultiplexer", {command : "getAll", server : "dev.cyberlightning.com" } , getImageList);
	cubeMain();
};

var pitch = -90.00;
var roll = 0;
var yaw = 0;

function reply_click(e) {
	var evt = e || window.event;
    var clicked = e.target || e.srcElement;
    if (clicked.nodeName === 'BUTTON') {
        console.log(clicked.id);
        var image = JSON.parse(imageList[clicked.id]);
        $('#image').empty();
        $('#image').append("<img src=\""+image.url+"\" alt=\"Loading...\" />");
		var lat= image.latitude;
		var lon= image.longitude;
		var location= new google.maps.LatLng(lat,lon);
		pitch= image.pitch -90.00;
		if(image.deviceorientation==="potrait"){
			yaw=image.yaw;			
		} else if(image.deviceorientation="landscape"){			
			yaw=image.yaw+90;		
		}		
		marker = new google.maps.Marker({
	     	   position: location,
	     	   map: map, //your map,
	     	   draggable:false,
	     	   animation: google.maps.Animation.DROP,
	     	});
		updateCube(image.yaw,image.roll,image.pitch ,image.deviceorientation);
		setStreetView(location);
    }
}

var setStreetView = function(location) {
	var sv = new google.maps.StreetViewService();
	//panorama = new google.maps.StreetViewPanorama(document.getElementById('streetview'));
	sv.getPanoramaByLocation(location, 10, processSVData);
};

var processSVData = function(data, status) {
	if (status == google.maps.StreetViewStatus.OK) {
		//alert(data.location.latLng.lat());
		var streetviewurl ="https://maps.googleapis.com/maps/api/streetview?size=600x400&location="+data.location.latLng.lat()+","+data.location.latLng.lng()+"&fov=90&heading="+yaw+"&pitch="+pitch+"&sensor=false";
		$('#streetview').empty();
		$('#streetview').append("<img src=\""+streetviewurl+"\" alt=\"Loading...\" />");
//	    panorama.setPano(data.location.pano);
//	    panorama.setPov({
//	      heading: 270,
//	      pitch: 0
//	    });
//	    panorama.setVisible(true);
	  } else {
		  $('#streetview').empty();
	    alert('Street View data not found for this location.');
	  }
};

window.onload = makeRequest;





// Until requestAnimationFrame works everywhere, use this code from Google
var requestAnimFrame = (function() {
    return window.requestAnimationFrame ||
        window.webkitRequestAnimationFrame ||
        window.mozRequestAnimationFrame ||
        window.oRequestAnimationFrame ||
        window.msRequestAnimationFrame ||
        function(callback, element) { window.setTimeout(callback, 1000 / 60); };
})();



// Setting up WebGL

var initShaders = function()
{
    var vShader, fShader;
    
    vShader = gpu.loadShader(gl.VERTEX_SHADER, "shade_vert");
    fShader = gpu.loadShader(gl.FRAGMENT_SHADER, "shade_frag");
    gpuShade = gpu.newProgram(vShader, fShader);
    
    hProjectionMatrix = gpu.getUniform(gpuShade, "gProjectionMatrix");
    hModelViewMatrix  = gpu.getUniform(gpuShade, "gModelViewMatrix");
    hNormalMatrix     = gpu.getUniform(gpuShade, "gNormalMatrix");
    hLightPos = gpu.getUniform(gpuShade, "gLightPos");
    hColor = gpu.getUniform(gpuShade, "gColor");
    
    vaPosition = gpu.getAttribute(gpuShade, "vPosition");
    vaNormal   = gpu.getAttribute(gpuShade, "vNormal");
};

var createCube = function()
{
	// Transfer data to GPU
	cubeVBuf = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, cubeVBuf);
	gl.bufferData(gl.ARRAY_BUFFER, cubeVerts, gl.STATIC_DRAW);
	gl.bindBuffer(gl.ARRAY_BUFFER, null);
	
	cubeNBuf = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, cubeNBuf);
	gl.bufferData(gl.ARRAY_BUFFER, cubeNorms, gl.STATIC_DRAW);
	gl.bindBuffer(gl.ARRAY_BUFFER, null);
	
	cubeIdxBuf = gl.createBuffer();
	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, cubeIdxBuf);
	gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, cubeIdx, gl.STATIC_DRAW);
	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, null);
};

var createCoordinateSystem = function()
{
	
};

var initGL = function(canvas)
{
    // Do we have a context?
    try {
        gl = canvas.getContext("webgl") || canvas.getContext("experimental-webgl");
        gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
    } catch(e) {
        gl = null;
    }
    if (! gl) {
        alert("Could not get WebGL context: does your browser support WebGL?");
    }
    // Regular OpenGL setup
    gl.clearColor(0, 0, 0, 1);
    gl.enable(gl.DEPTH_TEST);
    gl.enable(gl.CULL_FACE);
    mat4.identity(pMatrix);
    mat4.identity(mvMatrix);
    initShaders();
    createCube();
};


var setProjection = function()
{
    mat4.perspective(60, gl.viewportWidth / gl.viewportHeight, 0.1, 10.0, pMatrix);
};

var setViewpoint = function()
{
    mat4.lookAt([0, 2, 4], [0, 0, 0], [0, 1, 0], mvMatrix);
};

var drawWorld = function()
{
	//createCube();
	//Next three lines rotates the camera.
    mat4.rotate(mvMatrix, cubeSpiny, [0, 1, 0], mvMatrix);
    mat4.rotate(mvMatrix, cubeSpinz, [0, 0, 1], mvMatrix);
    mat4.rotate(mvMatrix, cubeSpinx, [1, 0, 0], mvMatrix); 
	
    //These three lines controles the projection matrix which I believe represent the world
/*     mat4.rotate(pMatrix, cubeSpiny, [0, 1, 0], pMatrix);
    mat4.rotate(pMatrix, cubeSpinz, [0, 0, 1], pMatrix);
    mat4.rotate(pMatrix, cubeSpinx, [1, 0, 0], pMatrix); */
    
    var nv3 = mat4.toInverseMat3(mvMatrix);
    mat3.transpose(nv3, nv3);
    var nvMatrix = mat3.toMat4(nv3);
    
    gl.useProgram(gpuShade);
    gl.uniformMatrix4fv(hProjectionMatrix, false, pMatrix);
    gl.uniformMatrix4fv(hModelViewMatrix, false, mvMatrix);
    gl.uniformMatrix4fv(hNormalMatrix, false, nvMatrix);
    gl.uniform4f(hLightPos, 0.5, 1.0, 1.0, 0.0);
    gl.uniform4f(hColor, cubeColor[0], cubeColor[1], cubeColor[2], cubeColor[3]);
    
    gl.bindBuffer(gl.ARRAY_BUFFER, cubeVBuf);
	gl.enableVertexAttribArray(vaPosition);
	gl.vertexAttribPointer(vaPosition, 3, gl.FLOAT, false, 0, 0);
	
    gl.bindBuffer(gl.ARRAY_BUFFER, cubeNBuf);
	gl.enableVertexAttribArray(vaNormal);
	gl.vertexAttribPointer(vaNormal, 3, gl.FLOAT, false, 0, 0);

	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, cubeIdxBuf);
    gl.drawElements(gl.TRIANGLES, cubeIdx.length, gl.UNSIGNED_SHORT, 0);
};

var draw = function()
{
    gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    
    try {
        setProjection();
        setViewpoint();
        drawWorld();
    } catch(e) {
        alert("draw: " + e.message);
    }    
    //requestAnimFrame(draw);
};

function cubeMain()
{
    var canvas = document.getElementById("cube-webgl");
    try {
        initGL(canvas);
        draw();
    } catch(e) {
        alert("initGL: " + e.message);
    }
}

function updateCube(alpha, gamma, beta ,orientation ){
	
	cubeSpinx  = beta;
	cubeSpiny  = -gamma;
	cubeSpinz  = -alpha;
	var cubeColor = (gamma /360); 
	if(orientation==="landscape"){
		cubeColor = [ 0, 1, cubeColor, 1 ];
	}
	else {
		cubeColor = [ 0, 1, 0, 1 ];		
	}
	draw();
}
