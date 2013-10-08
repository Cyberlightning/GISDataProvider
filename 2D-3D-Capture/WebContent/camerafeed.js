var localstream;
var localcanvas;
var localcontext;
var video;
var videowidth;
var videoheight;
var connection;
var image;
var thisDevice;

/**
 * This javascript code is the underlying functionality to camera feed. It provides following functionality 
 * to the demo.
 * 1. Take snapshots from a browser app using the device camera
 * 2. Upload them using a websocket connection as a jpeg or a png
 * 3. Provide information related to all supported  the sensors to be stored in a database. 
 * 
 */

/**
 * Browser Object saves the supported sensor elements by the browser 
 * that application is being used.
 */
var Browser = function(video,gps,socket){
	this.videoSupport =video;
	this.gpsSupport = gps;
	this.webSocketSupport= socket;
};

/**
 * Encapsulates device related information.
 */
var Device = function (browser) {
	this.browser=browser;
};

Device.prototype.getBrowser = function(){
	return this.browser;
};
Browser.prototype.getVideoSupport = function() {
	return this.videoSupport;
};
Browser.prototype.getGPSSupport = function() {
	return this.gpsSupport;
};
Browser.prototype.getSocketSupport = function(){
	return this.webSocketSupport;
};

/**
 * ConnectionHandler is responsible for handling the websocket connection.
 * It implements functions that handles onOpen, onMessage, onClose and onError 
 * events.
 */
var ConnectionHandler = function () {	 
};


var onFailSoHard = function(e) {
	log("Video not supported");
};
  
var binaryCodeImage = function(imagedata){
	  var canvaspixelarray = imagedata.data;	    	    
	  var canvaspixellen = canvaspixelarray.length;
	  var bytearray = new Uint8Array(canvaspixellen);	    	    
	  for (var i=0;i<canvaspixellen;++i) {
	      bytearray[i] = canvaspixelarray[i];
	        
	  }
	  return bytearray;
};
  
 var getBinaryData = function() {
	//FOLLOWING 2 LINE OF CODE CONVERTS THE IMAGEDATA TO BINARY
	var imagedata = localcontext.getImageData(0, 0, videowidth,videoheight);
	return  binaryCodeImage(imagedata);		
}; 

var snapshot = function(pos) {
	thisDevice.position = pos.coords;	
	thisDevice.positionTime= pos.timestamp ;
	if(video){
		 localcanvas = document.createElement("canvas");
		 localcanvas.id = "snapshot";
		 localcontext = localcanvas.getContext('2d');
		 videoheight= video.videoHeight;
		 videowidth = video.videoWidth;
		 localcanvas.width = videowidth;
		 localcanvas.height = videoheight;	
		 localcanvas.style.position="fixed";
		 localcanvas.style.top="80px";
		 localcanvas.style.left=(videowidth+400+20)+"px";
		 localcanvas.style.zIndex= 10;
		 localcontext.drawImage(video, 0, 0);
		 document.body.appendChild(localcanvas);
		 setupConnection();	   	 
	};	  
};
 
var setupConnection = function() {
	log("Imagepos-->"+thisDevice.position.latitude+"."+ thisDevice.position.longitude);
	 connection = new WebSocket("ws://localhost:17000/");
	 //connection = new WebSocket("ws://dev.cyberlightning.com:17000/");
	 connection.binaryType = "arraybuffer";
	 var handler = new ConnectionHandler;	 
	 connection.onopen = handler.onOpen;
	 connection.onmessage =	handler.onMessage;
	 connection.onclose = handler.onClose;
	 connection.onerror =  handler.onError;
 };
 
ConnectionHandler.prototype.onOpen= function() {
	log("Imagepos-->"+thisDevice.position.latitude+"."+ thisDevice.position.longitude);
	 var d = new Date();
	 var time=d.getFullYear()+"."+( d.getMonth()+1)+"."+d.getDate()+"_"+d.getHours()+"."+d.getMinutes()+"."+d.getSeconds();
	 if(!thisDevice.position.longitude || thisDevice.position.longitude == null ){
		 thisDevice.position.longitude = -181.0;
	 }
	 if(!thisDevice.position.latitude || thisDevice.position.latitude == null ){
		 thisDevice.position.latitude = -181.0;
	 }
	 if(!thisDevice.position.altitude || thisDevice.position.altitude == null ){
		 thisDevice.position.altitude = -1.0;
	 }
	 if(!thisDevice.position.accuracy || thisDevice.position.accuracy == null ){
		 thisDevice.position.accuracy =-1.0;
	 }
	 if(!thisDevice.ax || thisDevice.ax == null)
		 thisDevice.ax = 400.0;
	 if(!thisDevice.ay || thisDevice.ay == null)
		 thisDevice.ay = 400.0;
	 if(!thisDevice.az || thisDevice.az == null)
		 thisDevice.az = 400.0;
	 if(!thisDevice.gx || thisDevice.gx == null)
		 thisDevice.gx = 400.0;
	 if(!thisDevice.gy || thisDevice.gy == null)
		 thisDevice.gy = 400.0;
	 if(!thisDevice.gz || thisDevice.gz == null)
		 thisDevice.gz = 400.0;
	 if(thisDevice.position.speed == null ||  thisDevice.position.speed == 0 ){
		 thisDevice.position.speed=0;
		 thisDevice.position.heading=400.0;
	 }
	 if(!thisDevice.heading || thisDevice.heading == null){
		 thisDevice.heading = 400.0;
	 }
	if(!thisDevice.hor_Vir || thisDevice.hor_Vir == null){
		thisDevice.hor_Vir = -181.0;
		 }
	if(!thisDevice.tiltAngle || thisDevice.tiltAngle == null){
		thisDevice.tiltAngle = -181.0;
	}
	 var imagematadata={
		 type:"image",
		 time:time, 
		 ext:"jpg",
		 position: {
			 lon:thisDevice.position.longitude,
			 lat:thisDevice.position.latitude,
			 alt:thisDevice.position.altitude,
			 acc:thisDevice.position.accuracy,
		 },
		 motion : {
			 heading:thisDevice.position.heading ,
			 speed:thisDevice.position.speed,
		},
		 device : {
			 ax:thisDevice.ax,ay:thisDevice.ay,az:thisDevice.az,
			 gx:thisDevice.gx,gy:thisDevice.gy,gz:thisDevice.gz,
			 ra:thisDevice.heading,rb:thisDevice.hor_Vir,rg:thisDevice.tiltAngle,
			 orientation:thisDevice.orienation,
		 },	 
		 dTime:thisDevice.positionTime,};
	 if(connection) {
		 var sendingString = JSON.stringify(imagematadata);
		 log("Sending message"+sendingString);
		 connection.send(sendingString);			 
	 } else {
			 log("Connection is null");
	 }
};

ConnectionHandler.prototype.onMessage = function (event) {
	message=window.atob(event.data);	
	log(message);
	//if(message =="Hello Client\n..Server is listening..!"){		
	//THIS PART TRIES TO EXTRACT DATA FROM THE CANVAS TO CREATE AN JPEG IMAGE
	var url = localcanvas.toDataURL("image/jpeg");
	if(connection.readyState == 1) 
		connection.send(url);		 
	
};

ConnectionHandler.prototype.onClose = function()
{ 
	 log("Connection is closed...");
	 var localcanvas =document.getElementById("snapshot");
	 localcontext.clearRect(0, 0, localcanvas.width, localcanvas.height);	 
	 document.body.removeChild(localcanvas);
};

ConnectionHandler.prototype.onError = function (errors){
	log('WebSocket Error'  + errors ); 
};

function startVideoCliecked() {
	video = document.querySelector('video');
	if(video){
		if(thisDevice.getBrowser().getVideoSupport())
			startVideo(onFailSoHard);
		else 
			log("Your Browser Does not support video");
	}else
		log("There is no video element");
}

function uploadSnapshotClicket(){
	if(thisDevice.getBrowser().getSocketSupport()) {
		getLocation(snapshot);
	} else {
		log("Can not upload image");
	}
//	snapshot();
}

function log(text){
	document.getElementById("consolelog").value = document.getElementById("consolelog").value + "\n"+text;
}

function clearlogs(){
	document.getElementById("consolelog").value = "";	
}


function handleOrientationChanges(alpha, gamma, beta , abs , orientation){
	thisDevice.orienation = orientation;
	thisDevice.heading=alpha;//Rotation arround z axis
	thisDevice.tiltAngle =gamma;//Rotation around Y axis
	thisDevice.hor_Vir =beta;//Rotation around x axis
	//log("Orientation-->"+alpha+":"+gamma+":"+beta+":"+orientation);
}

function handlacceleration(a){
	//log(a.x+":"+a.y+":"+a.z);
	thisDevice.ax = a.x;
	thisDevice.ay = a.y;
	thisDevice.az = a.z;
	//log("Acceleration-->"+a.x+":"+a.y+":"+a.z);	
}

function handleAccelerationWithGravity(g){
	thisDevice.gx = g.x;
	thisDevice.gy = g.y;
	thisDevice.gz = g.z;
	//log("Accelerationwithgravity-->"+g.x+":"+g.y+":"+g.z);
}

function handleRotation(r) {
	thisDevice.ra = r.alpha;
	thisDevice.rb = r.beta;
	thisDevice.rg = r.gamma;
	log("DeviceRotation-->"+r.alpha+":"+r.beta+":"+r.gamma);
}

function onLocationSearchSuccess(pos){
	log("Pos-->"+pos.coords.latitude+ ":"+pos.coords.longitude+":"+pos.coords.accuracy+ ":"+pos.coords.altitude);
	//thisDevice.position = pos;
}

function onLocationServiceSearchError(){
	log("Position not available");
}

window.onload=function() {
	var b;	
	if ("WebSocket" in window)
  	 {
		b = new Browser(hasMediaSupport(),isGeolocationSupported(),true);
		log("WebSockets supported");
  	 } else {
  		b = new Browser(hasMediaSupport(),isGeolocationSupported(),false);
  		
  		log("WebSocket NOT supported by your Browser!");		   	     
  	 }
	var orientation = window.screen.orientation;
	if(orientation)
		log("You are running FireFox Mozilla");
	else
		log("You browser orinatation can not be detected.");	
	b.orientation= orientation;
	if(isDeviceOrientationSupported()){
		log("Subscrbing to device orientation events");
		reigsterDeviceOrentationEvent(handleOrientationChanges);
	} else 
		log("Browser couldnt care less if you tilt.");
	if(isDeviceMotionSupported()){
		log("Subscribing for deviceMotionEvent");
		registerDeviceMotionEvents(handlacceleration, handleAccelerationWithGravity, handleRotation);
		var mapoptions = {
				  enableHighAccuracy: true,
				  timeout: 27000,
				  maximumAge: 30000
				};
		registerForDeviceMovements(onLocationSearchSuccess,onLocationServiceSearchError,mapoptions);
	} else
		log("Device Could care less if you move or not");

	thisDevice = new Device(b);
};

