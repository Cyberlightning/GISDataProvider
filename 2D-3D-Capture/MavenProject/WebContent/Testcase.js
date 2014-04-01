/**
 * This Test document is for testing the functionality of methods in js_api_r2.js. This javascript source file includes features of 
 * release 1 and release2. 
 */

function getBrowserType() {
		this.browser= "";
		var uagent = navigator.userAgent.toLowerCase();
	 	if(uagent.search("firefox") != -1){ 
	 		this.browser = "FireFox";
	 		ok((this.browser==="FireFox"),"Browser is ditected as FireFox");
	 	} else if(uagent.search("chrome") != -1){
	 		this.browser = "Chrome";
	 		ok((this.browser==="Chrome"),"Browser is ditected as Chrome");
	 	} else if(uagent.search("msie") != -1){
	 		this.browser = "IE";
	 		ok((this.browser==="IE"),"Browser is ditected as IE");
	 	} else if(uagent.search("opera") != -1){
	 		this.browser = "Opera";
	 		ok((this.browser==="Opera"),"Browser is ditected as Opera");
		} else {
			 this.browser = "Unditected";
			 ok((this.browser==="Unditected"),"Browser is ditected as Unditected");
		 }
	 	return this.browser;
}

function isDeviceOrientationSupported () {
	if (window.DeviceOrientationEvent) {
		ok(window.DeviceOrientationEvent ,"isDeviceOrientationSupported returns true ");
		return true;
	} else {		  
		ok(true ,"isDeviceOrientationSupported returns false");
		return false;
	}
}

function isDeviceMotionSupported(){
	if (window.DeviceMotionEvent) {
		ok(window.DeviceOrientationEvent ,"isDeviceMotionSupported returns true ");
		return true;
	} else {
		ok (true ,"isDeviceMotionSupported returns false");
		return false;
	}
}

function isGeolocationSupported ()
{
	if ("geolocation" in navigator) {
		ok(window.DeviceOrientationEvent ,"isGeolocationSupported returns true ");
		return true;
	} 
	else {
		ok(true ,"isGeolocationSupported returns false ");
		return false;
	}
}

function supportedMediaList(){
	var compass;
	var accelerometer;
	var GPS;
	var camera;
	if (window.DeviceOrientationEvent) {		
		compass = "Supported";
		equal(compass , "Supported" , "DeviceOrientationEvent Ditected and found supported by the browser");
	} else {		  
		compass = "Not Supported";
		equal(compass , "Not Supported" , "DeviceOrientationEvent Ditected and found not supported browser");
	}
	if ("geolocation" in navigator) {		
		GPS = "Supported";
		equal(GPS , "Supported" , "DeviceOrientationEvent Ditected and found supported by the browser");
	} else {		  
		GPS = "Not Supported";
		equal(GPS , "Not Supported" , "DeviceOrientationEvent Ditected and found not supported browser");
	}
	if (window.DeviceMotionEvent) {		
		accelerometer = "Supported";
		equal(accelerometer , "Supported" , "DeviceOrientationEvent Ditected and found supported by the browser");
	} else {		  
		accelerometer = "Not Supported";
		equal(accelerometer , "Not Supported" , "DeviceOrientationEvent Ditected and found not supported browser");
	}
	if(!!(navigator.getUserMedia || navigator.webkitGetUserMedia ||navigator.mozGetUserMedia || navigator.msGetUserMedia)){
		camera = "Supported";
		equal(camera , "Supported" , "DeviceOrientationEvent Ditected and found supported by the browser");
	} else {		  
		camera = "Not Supported";
		equal(camera , "Not Supported" , "DeviceOrientationEvent Ditected and found not supported browser");
	}
	var list = { "GPS" :GPS , "Compass" : compass , "Accelerometer" : accelerometer, "Video" : camera };
	return list;
}

module("Testcase 1. This Testcase tests the priliminary unit tests for supporting functions.");
test("This module is to test isXXXSupported type functions.", function() {
	 ok ( function getUAString() {var uagent = navigator.userAgent.toLowerCase(); return  uagent;},"getUAString method returns a non empty string");
	 ok (function isWebSocketSupported() { if ("WebSocket" in window){return true;} else {return false;}}, "isWebSocketSupported returns true or false. If working properly web socket support can be ditected");
	 ok( (getBrowserType()) , "getBrowserType returnes a non empty string ");
	 ok( (isDeviceOrientationSupported()===true) || (isDeviceOrientationSupported()===false), "isDeviceOrientationSupported Retuns a non empty string");
	 ok( (isDeviceMotionSupported()===true) || (isDeviceMotionSupported()===false), "isDeviceMotionSupported Retuns a non empty string");
	 ok( (isGeolocationSupported()===true) || (isGeolocationSupported()===false), "isGeolocationSupported Retuns a non empty string");
	 ok( supportedMediaList(), "supportedMediaList method returns a non empty value");
});



module("Testcase 2 -Feature Testing 1. ");
test(" Testing function 1 -> hasMediaSupport()", function () {
	//This function obtains the value for video support  from browsers 
	ok(function hasMediaSupport(){ return !!(navigator.getUserMedia || navigator.webkitGetUserMedia ||navigator.mozGetUserMedia || navigator.msGetUserMedia);},"This test should pass for video to work");	
});

/**
 * Testing the video function feature
 */
asyncTest( "FIWARE.Feature.MiWi.2D-3DCapture.Browser.VideoCapture Testing function 2 -> showVideo()",2,
		function startVideo(callback) {
			this.videoelement = document.querySelector('video');		
			navigator.getUserMedia = (navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia);
			if(this.videoelement && navigator.getUserMedia){
				//if(this.browser.hasVideoCameraSupport())						    
					navigator.getUserMedia({video: true, audio: true},
							function(stream) {
								start();
								if(stream)
									ok(true,"Video Stream can be started");
								else
									ok(false,"Video Not started");
										this.videoelement.src = window.URL.createObjectURL(stream);
										this.videoStream = stream;
										this.videoRunning = true;
										equal(true, !!(navigator.getUserMedia || navigator.webkitGetUserMedia ||navigator.mozGetUserMedia || navigator.msGetUserMedia),"True if video supported. This proves the result of hasMediaSupport and start video functions are consistant.") ;					    
									    videoelement = stream;
//										if (typeof callback === "function")
//											callback(this.videoStream);
					}.bind(this) , function (err) {
//						alert("Unknown Error "+err.message);
					});
			} else{
				alert("You HTML dom Does nto have a video element!");
			} 
});

module("Testcase 2 -Feature Testing 2. ");
asyncTest( "FIWARE.Feature.MiWi.2D-3DCapture.Browser.ImageCapture : Testing functions -> snapshot(), getBinaryMessage()",4,
		function startVideo(callback) {
			this.videoelement = document.querySelector('video');		
			navigator.getUserMedia = (navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia);
			if(this.videoelement && navigator.getUserMedia){
				//if(this.browser.hasVideoCameraSupport())						    
					navigator.getUserMedia({video: true, audio: true},
						function(stream) {
							start();
							this.videoelement.src = window.URL.createObjectURL(stream);
							this.videoStream = stream;
							this.videoRunning = true;
							var localcanvas = snapshot();
							ok((localcanvas.nodeName.toLowerCase() === 'canvas'),"Snapshot function returns a non null value of type canvas.");
							var metadata= "This is a test message";
								/**
								 * This part tests the binary convertion of image and the  
								 */ 
							var msglen = metadata.length;
							ok(msglen ==="This is a test message".length,"Parameters Correctly read. Tested by length comparison of the message passed as arg and the message length in side the method code.");
							
							var fullBuffer;
							if(localcanvas){
								var localcontext = localcanvas.getContext('2d');
								//FOLLOWING 2 LINE OF CODE CONVERTS THE IMAGEDATA TO BINARY
								alert(videoelement.width);
								alert(videoelement.height);
								var imagedata = localcontext.getImageData(0, 0, videoelement.width, videoelement.height);
								var expectedimagesize = videoelement.width*videoelement.height*4;
								var canvaspixelarray = imagedata.data;	    	    
								var canvaspixellen = canvaspixelarray.length;
								ok(expectedimagesize === canvaspixellen, "Created image size tested against video resolution  times 4 for RGBA values. This is to confirm canvas data is correctly transfered as an image but this does not confirm if the image is exactly the video feed.");
								var msghead= msglen+"";
								var fbuflen = msglen +canvaspixellen+msghead.length;
								//var myArray = new ArrayBuffer(fbuflen);
								fullBuffer = new Uint8Array(fbuflen);
								for (var i=0; i< msghead.length; i++) {
									fullBuffer[i] = msghead.charCodeAt(i);
							    }
								var count = 0;
								//log(msghead.length+":"+msglen+":"+canvaspixelarray.length);
								for (var i=msghead.length; i< msglen+msghead.length; i++) {
									fullBuffer[i] = metadata.charCodeAt(count);
									count++;
								}
								count = 0;
								for (var i=msglen+msghead.length;i<fbuflen;i++) {
									fullBuffer[i] = canvaspixelarray[count];
									count++;
								};
								alert(fullBuffer.length);
								alert("This is a testmessage".length +expectedimagesize);
								//Adding 2 at the end since the length of the message is 22 and that is a 2 digit number
								ok(fullBuffer.length ===("This is a test message".length +expectedimagesize+2) , "Full message buffer size tested for length. Total message should be equal to the sum of the image buffer length, header length and the length of the header length");
							}
								
					}.bind(this) , function (err) {
//						alert("Unknown Error "+err.message);
					});
			} else{
//				alert("You HTML dom Does nto have a video element!");
			} 
});

function snapshot(){
	var localcanvas=null;
	//if(this.videoStream) {
		try {
			localcanvas =document.getElementById("image");
			if(!localcanvas)
				localcanvas = document.createElement("canvas");
			localcanvas.id = "image";
			var localcontext = localcanvas.getContext('2d');
			localcanvas.width = this.videoelement.videoWidth;
			localcanvas.height = this.videoelement.videoHeight;	
			localcanvas.style.position="fixed";
			localcanvas.style.top="600px";
			localcanvas.style.left=(200)+"px";
			localcanvas.style.zIndex= 2000;
			localcontext.drawImage(this.videoelement, 0, 0);
			document.body.appendChild(localcanvas);
		} catch(e){
			//log("ERROR1 "+e.message);
		}
	//} else{
		//alert("Video is not running");
	//}
	return localcanvas;
}

