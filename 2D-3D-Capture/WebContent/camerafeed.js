var localstream;
var localcanvas;
var localcontext;
var video;
var videowidth;
var videoheight;
var connection;
var image;
var thisDevice;

var Browser = function(video,gps,socket){
	this.videoSupport =video;
	this.gpsSupport = gps;
	this.webSocketSupport= socket;
};

var Location = function(){
	
};

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
}

var ConnectionHandler = function () {	 
};


//function hasGetUserMedia() {
//	return !!(navigator.getUserMedia || navigator.webkitGetUserMedia ||
//            navigator.mozGetUserMedia || navigator.msGetUserMedia);
//}

var onFailSoHard = function(e) {
	log("Video not supported");
};
  
 var videoStream = function(stream) {	
	    var videoelement = document.querySelector('video');
	    videoelement.src = window.URL.createObjectURL(stream);
	    videoelement = stream;
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
  
// var send = function(message,type) {
//	 
//	 
// };
 

var snapshot = function(pos) {
	thisDevice.position = pos.coords;
	log("Imagepos-->"+thisDevice.position.latitude+"."+ thisDevice.position.longitude);
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
		 localcanvas.style.top="10px";
		 localcanvas.style.left=(videowidth+400+20)+"px";		 
		 localcontext.drawImage(video, 0, 0);
		 document.body.appendChild(localcanvas);
		 setupConnection();	   	 
	};            
	  //localcontext.drawImage(video, 0, 0);
	  //input.value = localcanvas.toDataURL('image/jpeg');	  
};
 
var setupConnection = function() {	
	 //connection = new WebSocket("ws://localhost:17000/");
	 connection = new WebSocket("ws://dev.cyberlightning.com:17000/");
	 connection.binaryType = "arraybuffer";
	 var handler = new ConnectionHandler;	 
	 connection.onopen = handler.onOpen;
	 connection.onmessage =	handler.onMessage;
	 connection.onclose = function()
	 { 
		 log("Connection is closed...");
		 localcontext.clearRect(0, 0, localcanvas.width, localcanvas.height);
		 document.body.removeChild(localcanvas);
	 };
	 connection.onerror =  function  ( fault )  { 
		 log('WebSocket Error'  + errors ); 
	 };	
 };
 
Device.prototype.getGPS = function(){
	if(thisDevice.getBrowser().getGPSSupport()){		
		
	} else {
		log("Location is not supported");
	}
}; 


ConnectionHandler.prototype.onOpen= function() {
//	log(thisDevice.position.latitude+"."+ thisDevice.position.longitude);
//	log(thisDevice.position.altitude+"."+ thisDevice.position.accuracy);
//	log(thisDevice.position.heading+"."+ thisDevice.position.speed);
	log("Connection opened-->"+connection.readyState);
	 var d = new Date();
	 var time=d.getFullYear()+"."+d.getMonth()+"."+d.getUTCDay()+"_"+d.getHours()+"."+d.getMinutes()+"."+d.getSeconds();
	 if(thisDevice.position.coords.heading===NaN)
		 thisDevice.position.coords.heading=1000;
	 var imagematadata={
		 type:"image",
		 time:time, 
		 ext:"jpg",
		 //lon:"25.4737936",
         //lat :"65.012351",};
		 position: {
			 lon:thisDevice.position.coords.longitude,
			 lat:thisDevice.position.coords.latitude,
			 alt:thisDevice.position.coords.altitude,
			 acc:thisDevice.position.coords.accuracy,
		 },
		 motion : {
			 heading:thisDevice.position.coords.heading ,
			 speed:thisDevice.position.coords.speed,
		},
		 device : {
			 ax:thisDevice.ax,ay:thisDevice.ay,az:thisDevice.az,
			 //gx:thisDevice.gx,gy:thisDevice.gy,gz:thisDevice.gz,
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
	//log("Message Arrived.");
	message=window.atob(event.data);	
	log(message);
	if(message =="FILENAME"){		
		//THIS PART TRIES TO EXTRACT DATA FROM THE CANVAS TO CREATE AN JPEG IMAGE
		 var url = localcanvas.toDataURL("image/jpeg");
		 //log(url.length);
		 //log(url);
		if(connection.readyState == 1) 
			connection.send(url);
//		else 
//			log("Connecttion status is not OPEN -->"+connection.readyState);		 
	}
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

//https://developer.mozilla.org/en-US/docs/Web/Guide/API/DOM/Events/Orientation_and_motion_data_explained?redirectlocale=en-US&redirectslug=Web%2FGuide%2FDOM%2FEvents%2FOrientation_and_motion_data_explained
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
	thisDevice.position = pos;
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
		registerForDeviceMovements(onLocationSearchSuccess,onLocationServiceSearchError);
	} else
		log("Device Could care less if you move or not");

	thisDevice = new Device(b);
};

