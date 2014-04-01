/**
 * USE THIS FOR DEVELOPMENTS
 */

/**
 * Java script API for HTML5 device motion, device orientation, 
 * location and camera feeds. This API is a simple extension for 
 * existing APIs so that one single doc provides means to test 
 * availability and the extraction of the information from GPS 
 * locator, accelerometer, Gyroscope for the usage of browser 
 * applications.
 * 
 * Author : Tharanga Wijethilake
 * copyrights : Cyberlightning
 * Project: FI-ware
 * 
 */

/**
 * This library is focused to run on Firefox and Chrome. Some functionalities are tested on 
 * Safari mobile browser
 */

;(function (FIware_wp13, undefined) {

/**
 * Private function to extract the user agent string
 */
var getUAString = function(){
	 var uagent = navigator.userAgent.toLowerCase();
	 return  uagent;
};

/**PRIVATE OBJECT
 * This is a part of the device and will hold browser specific parameters 
 */
var Browser = function(){
}; 

/**
 * 
 */
Browser.prototype = {
		
		/**
		 * Required for communication with the python capture server
		 * @returns {Boolean}
		 */
		isWebSocketSupported : function() {
			 if ("WebSocket" in window)
		  	 {
				return true;
		  	 } else {
		  		return false;
		  	 }
		 },
		 
		 /**
		  * This Function returns the browser type in use.
		  * @returns {String}
		  */
		 getBrowserType : function() {
			 this.browser= "";
			 var uagent = navigator.userAgent.toLowerCase();
			 if(uagent.search("firefox") != -1){
				 this.browser = "Firefox";
				 var n = uagent.indexOf("rv:");
				 console.log(n);
				 this.releaseVersion = uagent.substring(n+3, n+7).trim();
				 if(this.releaseVersion == "26.0")
					 console.log(this.releaseVersion);
			 } else if(uagent.search("chrome") != -1){
				 this.browser = "Chrome";
			 } else if(uagent.search("msie") != -1){
				 this.browser = "IE";
			 } else if(uagent.search("opera") != -1){
				 this.browser = "Opera";
			 } else {
				 this.browser = "Unditected";
			 }
			 return this.browser;
		 },
	 
		/**
		 * This function returns true if the browser is able to  
		 * obtain readings from the Gyroscope. 
		 * @returns {Boolean}
		 */
		isDeviceOrientationSupported : function() {
			if (window.DeviceOrientationEvent) {		
				return true;
			} else {		  
				return false;
			}
		},

		/**
		 * This function returns true if the browser is able to  
		 * obtain readings from the accelerometer.
		 * @returns {Boolean}
		 */
		isDeviceMotionSupported : function (){
			if (window.DeviceMotionEvent) {		
				return true;
			} else {		  
				return false;
			}
		},
	
		/**
		 * This function returns true if the browser is able to  
		 * obtain readings from the accelerometer.
		 * @returns {Boolean}
		 */
		isGeolocationSupported : function()
		{
			if ("geolocation" in navigator) {		
				return true;
			} 
			else { 
				return false;
			}
		},

		/**
		 * This function returns true if the browser is able to  
		 * obtain camera feed from the local camera to the video element. 
		 * @returns {Boolean}
		 */	
		hasVideoCameraSupport : function() {
			return !!(navigator.getUserMedia || navigator.webkitGetUserMedia ||navigator.mozGetUserMedia || navigator.msGetUserMedia);
		},
	
	/**
	 * This method returns the list of sensors supported out of compass, accelerometer and the GPS. It also mentions if the video is supported. This is a private function
	 *  which is used by the device interface.
	 */
	supportedMediaList : function(){
			var compass;
			var accelerometer;
			var GPS;
			var camera;
			if (window.DeviceOrientationEvent) {		
				compass = "Supported";
			} else {		  
				compass = "Not Supported";
			}
			if ("geolocation" in navigator) {		
				GPS = "Supported";
			} else {		  
				GPS = "Not Supported";
			}
			if (window.DeviceMotionEvent) {		
				accelerometer = "Supported";
			} else {		  
				accelerometer = "Not Supported";
			}
			if(!!(navigator.getUserMedia || navigator.webkitGetUserMedia ||navigator.mozGetUserMedia || navigator.msGetUserMedia)){
				camera = "Supported";
			} else {		  
				camera = "Not Supported";
			}
			return { "GPS" :GPS , "Compass" : compass , "Accelerometer" : accelerometer, "Video" : camera };
		},
};


/**
 * PRIVATE OBJECT
 * Sensor represent sensor that is supported by the browser of a device.Each sensor has a name set of past values and latest values
 */
var Sensor = function(name){
	this.name = name;
	this.array =new Array();
	this.currentValue=-1000;
	this.active = false;
};

Sensor.prototype = {
		
		getName : function(){
			return this.name;
		},

		setCurrentValue : function(value){
			this.currentValue = value;
		},

		getCurrentValue : function(){
			return this.currentValue;
		},

		addValue : function(value){	
			
			if(this.name ==="GPS") {
				var len = this.array.length;
				if(len===20) {
					this.array.splice(0, 1); 
				}
				this.array.push(value);
			} else if(this.name ==="Compass"){
				var alpha;
				var beta;
				var gamma;
				if(!this.array[0]){
					alpha = new Array();
					if(!value.alpha)
						value.alpha=400;
					alpha.push(value.alpha);
					this.array[0]= alpha;
				}else {
					var len = this.array[0].length;
					if(len===10) {
						this.array[0].splice(0, 1); 
					}
					if(!value.alpha)
						value.alpha=400;
					this.array[0].push(value.alpha);
				}
				if(!this.array[1]){
					beta = new Array();
					if(!value.beta)
						value.beta=400;
					beta.push(value.beta);
					this.array[1]= beta;		
				}else {
					var len = this.array[1].length;
					if(len===10) {
						this.array[1].splice(0, 1); 
					}
					if(!value.beta)
						value.beta=400;
					this.array[1].push(value.beta);
				}
				if(!this.array[2]){
					gamma = new Array();
					if(!value.gamma)
						value.gamma=400;
					gamma.push(value.gamma);
					this.array[2]= gamma;
				}else {
					var len = this.array[2].length;
					if(len===10) {
						this.array[2].splice(0, 1); 
					}
					this.array[2].push(value.gamma);
					if(!value.gamma)
						value.gamma=400;
				}
			} else if(this.name ==="Accelerometer"){
				//For accelerometer you need 3 arrays to be managed. acceleration, acceleration with gravity and rotation. We will give minimum priority to rotation. 
				//We just store the value but will not be using any.
				//Acceleration this.array[0]
				//Acceleration with gravity this.array[1]
				//Rotation this.array[2]
				var acc;
				var accg;
				var rot;
				if(!this.array[0]){
					acc = new Array();
					if(!value.acceleration){
						value.acceleration.x=0.0;value.acceleration.y=0.0;value.acceleration.z=0.0;
					}
					acc.push(value.acceleration);
					this.array[0]= acc;
				}else {
					var len = this.array[0].length;
					if(len===10) {
						this.array[0].splice(0, 1); 
					}
					if(!value.acceleration){
						value.acceleration.x=0.0;value.acceleration.y=0.0;value.acceleration.z=0.0;
					}
					this.array[0].push(value.acceleration);
				}
				if(!this.array[1]){
					accg = new Array();
					if(!value.accelerationWithGravity){
						value.accelerationWithGravity.x=0.0;value.accelerationWithGravity.y=0.0;value.accelerationWithGravity.z=0.0;
					}
					accg.push(value.accelerationWithGravity);
					this.array[1]= accg;		
				}else {
					var len = this.array[1].length;
					if(len===10) {
						this.array[1].splice(0, 1); 
					}
					if(!value.accelerationWithGravity){
						value.accelerationWithGravity.x=0.0;value.accelerationWithGravity.y=0.0;value.accelerationWithGravity.z=0.0;
					}
					this.array[1].push(value.accelerationWithGravity);
				}
				if(!this.array[2]){
					rot = new Array();
					if(!value.rotationRate){
						value.rotationRate.alpha=400.0;value.rotationRate.beta=400.0;value.rotationRate.gamma=400.0;
					}
					rot.push(value.rotationRate);
					this.array[2]= rot;
				}else {
					var len = this.array[2].length;
					if(len===10) {
						this.array[2].splice(0, 1); 
					}
					if(!value.rotationRate){
						value.rotationRate.alpha=400.0;value.rotationRate.beta=400.0;value.rotationRate.gamma=400.0;
					}
					this.array[2].push(value.rotationRate);
				}		
			} else {
				var len = this.array[1].length;
				if(len===10) {
					this.array.splice(0, 1); 
				}
				this.array.push(value);
			}	
		},
		
		/**
		 * Not implemented yet
		 */
		getAdjustedValue :  function(){
	
		},
		
		/**
		 * returns the data array maintained by the 
		 * @returns {Array}
		 */
		getValues : function(){
			return this.array;
		},
		//END OF SENSOR
};

/**
 * Device Represent a collection of functions that is used to check support obtain access and record data from sensors. It provides access to each 
 * indivual sensor or all the supported sensors can be activated at once.
 * @param localurl url of the server which the mobile client is deployed
 * @param resturl url of the rest service
 * @param localport access port of the local application Eg Tomcat server port
 * @param restport If any..If the REST service is accessed via a specific port
 * @param wsp Web socket port of the image tagging service.
 */
FIware_wp13.Device = function(localurl, resturl, localport, wsp ,mT, restport ) {
	this.tomcatPORT = localport;
	this.localURL = localurl;
	this.websocketport = wsp;
	this.serverURL = resturl;
	this.mobileType =mT;
	if(restport)
		this.serverPORT = restport;
	this.userAgentString = navigator.userAgent.toLowerCase();
	var dos="";
	if(this.userAgentString.search("android") != -1){
		 dos = "Android";
	 }else if (this.userAgentString.search("windows") != -1){
		dos = "Windows";
	} else if(this.userAgentString.search("linux") != -1){
		dos = "Linux";
	 }else  {
		 dos = "Unditected";
	}
	
	var deviceType="";
	 if(this.userAgentString.search("mobile") != -1){
		 deviceType = "Mobile";
	 }else{
		deviceType = "Desktop";
	 }
	
	var b = new Browser();	
	var list = b.supportedMediaList();
	var temp= [];
	if(list["GPS"]==="Supported"){
		temp.push(new Sensor("GPS"));
	}
	if(list["Accelerometer"]==="Supported"){
		temp.push(new Sensor("Accelerometer"));
	}
	if(list["Compass"] === "Supported"){
		temp.push(new Sensor("Compass"));
	}
	if(list["Video"]==="Supported") {
		temp.push(new Sensor("Video"));
	}	
	this.OS = dos;
	this.sensorList = temp;
	this.browser = b;
	this.Type = deviceType;
	if(document.getElementById("consolelog")){
		log("Device Type-->" +deviceType+"\n<br>OS--> "+dos);
	}
};

/**
 * This is used for the GPS location service.
 */
var defaultmapoptions = {
		  enableHighAccuracy: true,
		  timeout: 27000,
		  maximumAge: 30000
		};

FIware_wp13.Device.prototype = {
		
		
		
		/**
		 * Creates a debugging logger on the side of the screen.
		 */
		setupLogger : function() {
			var logger = document.createElement("textarea");
			logger.id = "consolelog";
			logger.style.position="absolute";
			logger.style.width = "400px";
			logger.style.height = "400px";
			logger.style.top="700px";
			logger.style.left="500px";
			logger.style.zIndex= 500;
			logger.style.opacity = 0.5;
			document.body.appendChild(logger);
			var clbutton = document.createElement("button");
			clbutton.id = "clearlog";
			clbutton.style.position="absolute";
			clbutton.style.width = "400px";
			clbutton.style.top="600px";
			clbutton.style.left="500px";
			clbutton.style.zIndex= 1000;
			clbutton.style.opacity = 0.5;
			clbutton.onclick=function(){ document.getElementById("consolelog").value = "" ;};
			clbutton.innerHTML = "CLEAR LOG";
			document.body.appendChild(clbutton);
		},
		
		/**
		 * Returns the operating system of the device. 
		 * @returns
		 */
		getDeviceType : function(){
			return this.Type;	 
		},
		/**
		 * Return the Device OS
		 * @returns {String}
		 */
		getOS : function(){
			return this.OS;	 
		},

		/**
		 * Returns a string contains processed values of sensors. This returned string is the message that is converted to binary and sent to the
		 * Rest service as the image tag.
		 * @returns {___anonymous11241_12040}
		 */
		setValues : function(){
			var d = new Date();
			var time=d.getFullYear()+"."+( d.getMonth()+1)+"."+d.getDate()+"_"+d.getHours()+"."+d.getMinutes()+"."+d.getSeconds();
			var currentGPSValue=0;
			var currentAcceleration=0;
			var currentAccelerationWithGravity=0;
			var currentRotation;
			var alpha=0;
			var beta=0;
			var gamma=0;
			var mode=null;
			var metadata;
			var lon;
			if(this.Type =="Mobile"){
				try{
					var list = this.getSensorList();
					var count;
					for(count = 0 ; count < list.length; count++ ){
						var sn = list[count].getName();
						if(sn ==="GPS"){
							var s =list[count];
							currentGPSValue =s.getCurrentValue();
						}else if(sn ==="Accelerometer"){
							var s =list[count];
							var a  =s.getValues();
							if(a[0])
								currentAcceleration = processAccelerationValues(a[0]);
							else{
								currentAcceleration.x=0.0;currentAcceleration.y=0.0;currentAcceleration.z=0.0;
							}							
							//log("Accelerometer 1" +currentAcceleration.x+":"+currentAcceleration.y+":"+currentAcceleration.y);
							if(a[1])
								currentAccelerationWithGravity = processAccelerationDueToGravity(a[1]);
							else 
								currentAccelerationWithGravity.x = 0.0 ;currentAccelerationWithGravity.y=0.0; currentAccelerationWithGravity.z = 0.0;
							log("Accelerometer 1" +currentAccelerationWithGravity.x+":"+currentAccelerationWithGravity.y+":"+currentAccelerationWithGravity.y);
						} else if(sn ==="Compass"){
							var s =list[count];
							var a =s.getValues();
							if(a[0] && a[1] && a[2] ){
								var temp =processOrientationEvent(a[0],a[1],a[2]);
								temp = processOrientationInformation(this.mobileType,temp.alpha,temp.beta, temp.gamma,currentAccelerationWithGravity.x,currentAccelerationWithGravity.y, currentAccelerationWithGravity.z );
							}	else{
								temp.alpha = 400.0; temp.beta = 400.0 ; temp.gamma = 400.0;
							}
								
							alpha = temp.alpha;
							beta = temp.beta;
							gamma = temp.gamma;
							if(gamma < -25 || gamma > 25)
								mode = "landscape";
							else 
								mode = "potrait";
						} else {
							
						}
					}
					
				} catch(error){
					log("VALUE SET ERROR "+error.message);
				};
			metadata={
					 type:"image",
					 time:time, 
					 ext:"png",
					 deviceType :this.Type,
					 deviceOS : this.OS,
				 	 browsertype : this.browser.getBrowserType(),
					 position: {
						 lon:currentGPSValue.longitude,
						 lat:currentGPSValue.latitude,
						 alt:currentGPSValue.altitude,
						 acc:currentGPSValue.accuracy,
					 },
		//			 motion : {
		//				 heading:thisDevice.position.heading ,
		//				 speed:thisDevice.position.speed,
		//			},
					 device : {
						 ax:currentAcceleration.x,ay:currentAcceleration.y,az:currentAcceleration.z,
						 gx:currentAccelerationWithGravity.x,gy:currentAccelerationWithGravity.y,gz:currentAccelerationWithGravity.z,
						 ra:alpha,rb:beta,rg:gamma,
						 orientation:mode,
					 },	 
					 vwidth:this.videoelement.videoWidth, vheight:this.videoelement.videoHeight, };
			} else {
				var metadata={
						 type:"image",
						 time:time, 
						 ext:"png",
						 deviceType :this.Type,
						 deviceOS : this.OS,
					 	 browsertype : this.browser.getBrowserType(),
						 position: {
							 lon:currentGPSValue.longitude,
							 lat:currentGPSValue.latitude,
							 alt:currentGPSValue.altitude,
							 acc:currentGPSValue.accuracy,
						 },
			//			 motion : {
			//				 heading:thisDevice.position.heading ,
			//				 speed:thisDevice.position.speed,
			//			},
//						 device : {
//							 ax:currentAcceleration.x,ay:currentAcceleration.y,az:currentAcceleration.z,
//							 gx:currentAccelerationWithGravity.x,gy:currentAccelerationWithGravity.y,gz:currentAccelerationWithGravity.z,
//							 ra:alpha,rb:beta,rg:gamma,
//							 orientation:mode,
//						 },	 
						 vwidth:this.videoelement.videoWidth, vheight:this.videoelement.videoHeight, };
				}			
		//	log(JSON.stringify(metadata));
			return metadata;
		},

		/**
		 * Used to write values with the websokcets
		 */
		setAndSendValues : function(){
			this.imagedata = this.setValues();
			this.upladlImageWithWebSocket();
		},
		
		/**
		 * Meant to the public API function. uses web socket API
		 */
		sendImage : function() {			
			this.getCurrentLocation(this.setAndSendValues());
		},
		
		/**
		 * Meant to be the public API function for post
		 */
		postImage : function() {
			this.getCurrentLocation(this.setAndPostValues());
		},
		
		/**
		 * Used to post image and metadata to the rest service
		 */
		setAndPostValues : function(){
			this.imagedata = this.setValues();
			this.upladlImageWithPost();
		},
	
		/**
		 * Returns the list of supported 
		 * @returns {Array}
		 */
		getSensorList : function(){
			return this.sensorList;
		},
		
		/**
		 * Provide the video feed to a preincluded video element. In success it will call the callback method and at failiure returns a error message. 
		 * @param callback
		 */
		showVideo : function(callback){
			this.videoelement = document.querySelector('video');		
			navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia;			
			window.URL = window.URL || window.webkitURL || window.mozURL || window.msURL;
			if(this.videoelement){
				if(this.browser.hasVideoCameraSupport())
					navigator.getUserMedia({video: true, audio: false},
							function(stream) { 
								if (navigator.mozGetUserMedia) {
							        this.videoelement.mozSrcObject = stream;
							      } else {
							        var vendorURL = window.URL || window.webkitURL;
							        this.videoelement.src = vendorURL.createObjectURL(stream);
							      }
								this.videoelement.play();
								this.videoStream = stream;
								if (typeof callback === "function")
									callback(stream);
					}.bind(this) , function (err) {
						alert("Unknown Error "+err.message);
					});
			} else{
				alert("You HTML dom Does have a video element!");
			}	
		},
		
		setMobileType : function(mType){
			this.mobileType = mType;
		},
		
		/**
		* This function returns the GPS coordinates of the current location.
		* @param callback On successful retrieval of location this function
		* 					is the callback function to handle the acquired data.
		*/
		getCurrentLocation : function (callback,options){
			var icon;
			icon =document.getElementById("loading");
			if(icon)
				document.body.removeChild(icon);
			icon = document.createElement("img");
			icon.id = "loading";
			icon.src = "img/loader.GIF";
//			icon.height = 200px;	
			icon.style.position="fixed";
			icon.style.top="400px";
			icon.style.left=(300)+"px";
			icon.style.zIndex= 2001;
			document.body.appendChild(icon);
			var mapoptions;
			var r = confirm("Trying to obtain GPS location.");
			if(r){
				if (options) {
					mapoptions = options;
				} else {
					mapoptions = defaultmapoptions;
				}
				navigator.geolocation.getCurrentPosition(
						function (pos){
							var icon =document.getElementById("loading");
							if(icon)
								document.body.removeChild(icon);
							if (typeof callback === "function") {				    
							        callback(pos);
							    }
						}.bind(this), 
						function () { 
							var icon =document.getElementById("loading");
							if(icon)
								document.body.removeChild(icon);
							alert ("Location Not found");					
						}.bind(this), mapoptions);
			}
		},
		
		/**
		 * Registers to the GPS sensor to return location changes and at a location change will call the locationsuccess callback methode.
		 * @param onLocationSuccess
		 * @param onLocationError
		 * @param options
		 */
		registerForDeviceMovements : function(onLocationSuccess,onLocationError,options) {
			var r = confirm("Trying to obtain GPS cahgnes");
			var mapoptions;
			if(r){
				if (options) {
					mapoptions = options;
				}	else {
					mapoptions = defaultmapoptions;
				}
				if (typeof onLocationSuccess === "function" && typeof onLocationError === "function") {
					navigator.geolocation.watchPosition(
						function(pos) {//Success function					
							var Coordinates= new Object();
							coords = pos.coords;					
							if(coords.latitude)
								Coordinates.latitude = coords.latitude;
							else
								Coordinates.latitude = 100.00;
							if(coords.longitude)
								Coordinates.longitude = coords.longitude;
							else
								Coordinates.longitude = 181.00;
							if(coords.altitude)
								Coordinates.altitude = coords.altitude;
							else
								Coordinates.altitude = -1000;
							if(coords.accuracy)
								Coordinates.accuracy = coords.accuracy;
							else
								Coordinates.accuracy = 0;
							if(coords.altitudeAccuracy)
								Coordinates.altitudeAccuracy = coords.altitudeAccuracy;
							else
								Coordinates.altitudeAccuracy = 0;
							if(coords.speed) {
								Coordinates.speed = coords.speed;
								Coordinates.heading = coords.heading;
							}else {
								Coordinates.speed = 0;
								Coordinates.heading = 400;
							}
							
							try{
								var list = this.getSensorList();
								var count;
								for(count = 0 ; count < list.length; count++ ){
									var sn = list[count].getName();
									if(sn ==="GPS"){
										var s =list[count];
										s.setCurrentValue(Coordinates);
										s.addValue(Coordinates);
										var a =s.getValues();
										Coordinates =s.getCurrentValue();
										log(Coordinates.longitude +":"+Coordinates.latitude);
	//									Coordinates =a.pop();
										break;
									}							
								}					
							} catch(error){
								log("error message "+error.message);
							};
							onLocationSuccess(pos,Coordinates);					
							}.bind(this),
							function(){
								onLocationError();
							},
							mapoptions);
				};
			}
		},
		
		/**
		 * Creates a canvas and draws a snapshot of the current video feed. Returns a reference to the canvas with the snapshot
		 * @returns {___localcanvas8}
		 */
		snapshot : function(){
			var localcanvas=null;
			var r= true;
			if(this.Type =="Desktop")
				r = confirm("You are running on a Desktop/Latop so will not be able to obtain Meta Data and will result an error. Do you want to continue ?");
			if(r){
				if(this.videoStream) {
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
						log("ERROR1 "+e.message);
					}
				} else{
					alert("Video is not running");
				}
			}
			return localcanvas;
		},
		
		/**
		 *  Accepts a string and returns a binary message of the image and metadata
		 * @param metadata
		 * @returns
		 */
		setupBinaryMessage : function(metadata) {
			//IN PLACES WHERE YOU NEED TO TEST THE SERVICE ON A PC UNCOMMENT THE FOLLOWING LINE
			log("METADATA "+metadata);
			if(this.Type =="Desktop"){
				log("Fake Message Formed");
				var d = new Date();
				var time=d.getFullYear()+"."+( d.getMonth()+1)+"."+d.getDate()+"_"+d.getHours()+"."+d.getMinutes()+"."+d.getSeconds();
				metadata =JSON.stringify({"type":"image","time":time,"ext":"png","deviceType":"Desktop","deviceOS":"Linux","browsertype":"Chrome","vwidth":640,"vheight":480 , 'device': {'orientation': 'potrait', 'gz': 6.74, 'gy': 7.18, 'gx': 0, 'rg': 6.7995, 'ra': 322.6541, 'rb': 46.9973, 'ay': 0.13, 'ax': 0, 'az': 0}, 'position': {'lat': 65.01242298532382, 'acc': 128, 'alt': 142.17365173309813, 'lon': 25.47399834095566}, });
			}				
			var msglen = metadata.length;
			log("METADATA "+metadata);
			//log(msglen);			
			var localcanvas =document.getElementById("image");
			var fullBuffer;
			if(localcanvas){
				var localcontext = localcanvas.getContext('2d');
				//FOLLOWING 2 LINE OF CODE CONVERTS THE IMAGEDATA TO BINARY
				var imagedata = localcontext.getImageData(0, 0, localcanvas.width, localcanvas.height);
				var canvaspixelarray = imagedata.data;	    	    
				var canvaspixellen = canvaspixelarray.length;				
				var msghead= msglen+"";
				var fbuflen = msglen +canvaspixellen+msghead.length;
				//var myArray = new ArrayBuffer(fbuflen);
				fullBuffer = new Uint8Array(fbuflen);
				for (var i=0; i< msghead.length; i++) {
					fullBuffer[i] = msghead.charCodeAt(i);
			    }
				var count = 0;
				log(msghead.length+":"+msglen+":"+canvaspixelarray.length);
				for (var i=msghead.length; i< msglen+msghead.length; i++) {
					fullBuffer[i] = metadata.charCodeAt(count);
					count++;
			    }
				count = 0;
				for (var i=msglen+msghead.length;i<fbuflen;i++) {
					fullBuffer[i] = canvaspixelarray[count];
					count++;
				};
			}
			console.log("FULL BUFFER LENGTH "+fullBuffer.length);
			return fullBuffer;
		},

		/**
		 * This method calls local rest servlet to start the websocket. Uploads binary date once the websocket is started
		 */
		upladlImageWithWebSocket : function() {
			var xhr = new XMLHttpRequest();
			xhr.open('POST', "http://"+this.localURL+":"+this.tomcatPORT+"/TwoDThreeDCapture/RestRequestMultiplexer?command=wsstart&server="+this.serverURL, true);
			xhr.setRequestHeader("Content-type", "application/json; charset=utf-8");
			//xhr.responseType = 'arraybuffer';
			//var imgPostRequest = $.post( "http://"+this.localURL+":"+this.tomcatPORT+"/TwoDThreeDCapture/RestRequestMultiplexer",{command :"wsstart", server : this.serverURL },function(response) {
			//xhr.onload = function(e) {
			xhr.onreadystatechange = function() {
			    if(xhr.readyState == 4 && xhr.status == 200) {
				response = xhr.responseText.trim();
				if(response == "SERVER_READY"){
					try {
						var message =JSON.stringify(this.imagedata);
						var r = confirm("CONFIRM :" +message) ;
						if(r){
							var fullBuffer =this.setupBinaryMessage(message);						
							this.connection = new WebSocket('ws://'+this.serverURL+':'+this.websocketport+'/');
							this.connection.message = fullBuffer;
							this.connection.binaryType = "arraybuffer";
							this.connection.onopen = function() {
									log("CONNECTION OPEN");
								}.bind(this);
							
							this.connection.onmessage =	function (event) {					
								var svrmessage=window.atob(event.data);
								log("MESSAGE RECIEVED " +svrmessage.trim());
								if(svrmessage.trim() =="SERVER_READY"){
									log("message to send "+this.connection.message);
									this.connection.send(this.connection.message);
									
								//THIS PART TRIES TO EXTRACT DATA FROM THE CANVAS TO CREATE AN JPEG IMAGE
								//var url = localcanvas.toDataURL("image/jpeg");
								} else if(svrmessage.trim()=="SAVED"){
									log("Image Saved");
									var localcanvas =document.getElementById("image");
									var localcontext = localcanvas.getContext('2d');
									localcontext.clearRect(0, 0, localcanvas.width, localcanvas.height);	 
									document.body.removeChild(localcanvas);
									var icon;
									icon =document.getElementById("loading");
									if(icon)
										document.body.removeChild(icon);
								} else if(svrmessage.trim()=="ERROR"){
									log("Error Image saving.");
									var localcanvas =document.getElementById("image");
									if(localcanvas) {
										var localcontext = localcanvas.getContext('2d');
										localcontext.clearRect(0, 0, localcanvas.width, localcanvas.height);	 
										document.body.removeChild(localcanvas);
										var icon;
										icon =document.getElementById("loading");
										if(icon)
											document.body.removeChild(icon);
									}
								}									
							}.bind(this);
							
							this.connection.onclose = function(){ 
								log("CONNECTION CLOSSED");
								var localcanvas =document.getElementById("image");
								if(localcanvas) {
									var localcontext = localcanvas.getContext('2d');
									localcontext.clearRect(0, 0, localcanvas.width, localcanvas.height);	 
									document.body.removeChild(localcanvas);
									var icon;
									icon =document.getElementById("loading");
									if(icon)
										document.body.removeChild(icon);
								}
							}.bind(this);
							
							this.connection.onerror =  function (errors){
									log(errors.message);
							}.bind(this);
						} else{
							alert("Upload cancelled");
							var localcanvas =document.getElementById("image");
							var localcontext = localcanvas.getContext('2d');
							localcontext.clearRect(0, 0, localcanvas.width, localcanvas.height);	 
							document.body.removeChild(localcanvas);
						}
					}catch (error) {
						alert("Web Socket Error "+error.message);
					}
				} else {
					alert("SERVER ERROR");
				}
			    }}.bind(this);
			xhr.send();
		},

		/**
		 * Uploadimagewithpost posts binary image and meta to the back end server
		 */
		upladlImageWithPost : function() {
			var message =JSON.stringify(this.imagedata);	
			var fullBuffer = this.setupBinaryMessage(message);
			var message =JSON.stringify(this.imagedata);
			var r = confirm("Confirm " +message) ;
			if(r){
				var formdata = { command : "post", server : this.serverURL ,imagedata : fullBuffer};
				alert(jQuery.isPlainObject( formdata ));
	
				xhr = new XMLHttpRequest();
				xhr.open('POST', "http://"+this.localURL+":"+this.tomcatPORT+"/TwoDThreeDCapture/RestRequestMultiplexer", true);
				xhr.responseType = 'arraybuffer';
				xhr.onload = function(e) {
					if(e.currentTarget.readyState == 4  ) {
						if(e.currentTarget.status == 200){
							var localcanvas =document.getElementById("image");
							var localcontext = localcanvas.getContext('2d');
							localcontext.clearRect(0, 0, localcanvas.width, localcanvas.height);	 
							document.body.removeChild(localcanvas);
							alert("SUCCESS");
						} else if (e.currentTarget.status == 500) {
							alert("ERROR..!.May be try again.");
						}
					} 
				}.bind(this);
				xhr.send(fullBuffer);
			} else {
				alert("Post Cancelled");
				var localcanvas =document.getElementById("image");
				var localcontext = localcanvas.getContext('2d');
				localcontext.clearRect(0, 0, localcanvas.width, localcanvas.height);	 
				document.body.removeChild(localcanvas);
			}
//			var imgPostRequest = $.post( "http://"+this.serverURL+":"+this.tomcatPORT+"/RestClient/RestRequestMultiplexer",formdata,
		},

		/**
		* This function registers to device motion data from the accelerometer and from the gyroscope.There are
		* three callback functions to handle acceleration, acceleration due to gravity and device rotation.
		* @param handlacceleration
		* @param handleAccelerationWithGravity
		* @param handleRotation
		*/
		registerDeviceMotionEvents : function(handlacceleration, handleAccelerationWithGravity, handleRotation){
			var r = confirm("Trying to obtain Accelerometer Values");
			if(r){			
				if(this.browser.isDeviceMotionSupported()){
					window.addEventListener("devicemotion", function(event) {
						var rotationRate = event.rotationRate;		
						var acceleration = event.acceleration;
						var accelerationWithGravity = event.accelerationIncludingGravity;
						var tempEvent = new Object();
						if(acceleration ) {
							if(!acceleration.x)
								acceleration.x=0;
							if(!acceleration.y)
								acceleration.y=0;
							if(!acceleration.z)
								acceleration.z=0;
							tempEvent.acceleration = acceleration;
						}								
						if(accelerationWithGravity){
							if(!accelerationWithGravity.x)
								accelerationWithGravity.x=0;
							if(!accelerationWithGravity.y)
								accelerationWithGravity.y=0;
							if(!accelerationWithGravity.z)
								accelerationWithGravity.z=0;
							tempEvent.accelerationWithGravity = accelerationWithGravity;			
						};
						if(rotationRate){
							if(!rotationRate.alpha)
								rotationRate.alpha =0;
							if(!rotationRate.beta)
								rotationRate.beta =0;
							if(!rotationRate.gamma)
								rotationRate.gamma =0;
							tempEvent.rotationRate = rotationRate;
						};	
						try{
							var list = this.getSensorList();
							var count;
							for(count = 0 ; count < list.length; count++ ){
								var sn = list[count].getName();
								if(sn ==="Accelerometer"){
									var s =list[count];
									s.addValue(tempEvent);
									
									s.setCurrentValue(tempEvent);
	//								var a =s.getValues();
									break;
								}
							}				
						} catch(error){
						    log(error.message);
						};
						if (typeof handleAccelerationWithGravity === "function"){
							handleAccelerationWithGravity(tempEvent.accelerationWithGravity);
						}
						if (typeof handlacceleration === "function"){
							handlacceleration(tempEvent.acceleration);
						}
						if (typeof handleRotation === "function"){
							handleRotation(tempEvent.rotationRate);
						}
					}.bind(this), true);
				} else {
					alert("Device browser does not support this event..!");
				}
			}
			
		},

		/**
		 * This function registers to device orientation data and calls eventHandlingFunction in case of device
		 * orientation is changed in along x, y and x axis. These are thoroughly explained in the following doc.
		 * //https://developer.mozilla.org/en-US/docs/Web/Guide/API/DOM/Events/Orientation_and_motion_data_explained?redirectlocale=en-US&redirectslug=Web%2FGuide%2FDOM%2FEvents%2FOrientation_and_motion_data_explained
		 * 
		 * This method passes 5 values to the call back function, rotated angle around x,y,z axis , if the values
		 * are calculated based on the earth coordinate system and device orientation in mode.(landscape /portrait.) 
		 *  
		 * @param eventHandlingFunction callback function for a successful data retrieval
		 */
		registerDeviceOrentationEvent : function(eventHandlingFunction){
			var r = confirm("Access Device Compass..!");
			if(r){
				if (this.browser.isDeviceOrientationSupported()) {
				    window.addEventListener("deviceorientation", function( event ) {
						if(!event.alpha)
							event.alpha = 400.0;
						if(!event.beta)
							event.beta = 400.0;
						if(!event.gamma)
							event.gamma = 400.0;
						try{
							var list = this.getSensorList();
							var count;
							for(count = 0 ; count < list.length; count++ ){
								var sn = list[count].getName();
								if(sn ==="Compass"){
									var s =list[count];
									s.addValue(event);
									s.setCurrentValue(event);
									var a =s.getValues();
	//								log("Rot->"+a[0][0] + ":" +a[1][0]+ ":" +a[2][0]);						
									eventHandlingFunction(event.alpha, event.beta, event.gamma );
									break;
								}							
							}
							
						} catch(error){
						    
						};				
				    }.bind(this), true);
				} else {
					alert("Your browser does not support ");
				}
			}
		},
		
		/**
		 * For the logging of the debugging mode.
		 * @param text
		 */
		log : function(text){	
//			document.getElementById("consolelog").value = document.getElementById("consolelog").value + "\n"+text;
			if(document.getElementById("consolelog"))
				document.getElementById("consolelog").value = text+ "\n";
		},
		
		/**
		 * Subscribes for all the supported device sensros and returns a message for un supported sensors.
		 * @param onLocationSearchSuccess
		 * @param onLocationServiceSearchError
		 * @param handlacceleration
		 * @param handleAccelerationWithGravityEvent
		 * @param handleRotation
		 * @param handleOrientationChanges
		 */
		subscribe : function(onLocationSearchSuccess,onLocationServiceSearchError,handlacceleration,handleAccelerationWithGravityEvent,handleRotation, handleOrientationChanges){
			this.registerForDeviceMovements(onLocationSearchSuccess, onLocationServiceSearchError);
			this.registerDeviceMotionEvents(handlacceleration, handleAccelerationWithGravityEvent, handleRotation);
			this.registerDeviceOrentationEvent(handleOrientationChanges);
		},
		//END OF DEVICE METHODS
};

function log(text){	
	var logcanvas = document.getElementById("consolelog");
	if(logcanvas)
		document.getElementById("consolelog").value = document.getElementById("consolelog").value + "\n"+text;
	else 
		console.log(text);
	
}

function locationFindingerror(err) {
	  alert("An Error Occured"+ err.data);
};


function locationFindingSuccess(pos) {
	  console.log("An Error Occured"+pos.coords.latitude);
};


/**This method tries to stabalise the values generated by the accelerometer
* to identify in which direction the divice may be moving.
*/
var processAccelerationValues= function(acceleration){
	var count = 0;	
	var accx = new Array();
	var accy = new Array();
	var accz = new Array();
	for(count = 0; count < acceleration.length; count ++ ){
		accx.push(acceleration[count].x);
		accy.push(acceleration[count].y); 
		accz.push(acceleration[count].z);
	}
	var adjustedAX= adjust(average(accx),0.1);	
	var adjustedAY= adjust(average(accy),0.1);		
	var adjustedAZ= adjust(average(accz),0.4);
	var temp = new Object();
	temp.x = adjustedAX;
	temp.y = adjustedAY;
	temp.z = adjustedAZ;
//	log("processAccelerationValues 2"+temp.x+":"+temp.y+":"+temp.z);
	return temp;
	
};

/**
* This function would determine the down in the context the mobile is used. This function ignores values in between +2ms-2 and -2ms-2 for
* convinient purpoeses. 
*/
var processAccelerationDueToGravity= function(acceleration){
	var count = 0;
	var gaccx = new Array();
	var gaccy = new Array();
	var gaccz = new Array();
	for(count = 0; count < acceleration.length; count ++ ){
//		log("processAccelerationDueToGravity1 "+acceleration[count].x+":"+acceleration[count].y+":"+acceleration[count].z);
		gaccx.push(adjust(acceleration[count].x,2.0));
		gaccy.push(adjust(acceleration[count].y,2.0)) ; 
		gaccz.push(adjust(acceleration[count].z,2.0))  ;
	}	
	var adjustedGAX= adjust(average(gaccx),2.0);
	var adjustedGAY= adjust(average(gaccy),2.0);		
	var adjustedGAZ= adjust(average(gaccz),2.0);
	
	var temp = new Object();
	temp.x = adjustedGAX;
	temp.y = adjustedGAY;
	temp.z = adjustedGAZ;
//	log("processAccelerationDueToGravity 2 "+temp.x+":"+temp.y+":"+temp.z);
	return temp;	
};

/**
* ditermine if a value is bounded by a certain value about zero.
* If not bouded value is rounded to two decimal places.
*/
var adjust = function(value,cutoff){
	if(-cutoff < value && value < cutoff ){
		value = 0.0;			
	} else {		
		value = Math.round(value * 100) / 100 ;
	}	
	return value;
};

/**
* Calculates the average value of the array of values
	*/
var average = function(values) {
	var ave= 0.0;
	var sum = 0.0;
	var count;
	var devideBy = 0.0;
	if (values instanceof Array) {
		var oneBefore = 0;
		var oneAfter = 0;
		var limit = values.length;
		for(count = 1 ; count < limit ; count ++) {			
			oneBefore= values[count-1];
			oneAfter = values[count+1];
			if(((oneBefore <= 0)&&(values[count] <= 0)&&(oneAfter <=0)) || ((oneBefore >= 0)&&(values[count] >= 0)&&(oneAfter >= 0))) {
				if(count == 1){
					sum = values[0] +values[1];
				}
				else if(count == (limit-2)){
					sum = sum +values[limit-1]+values[limit-2];
				}else{
					sum = sum + values[count];
				}				
				if(count == 1 || count ==  (limit-2)){
					devideBy= devideBy +2;
				}else {
					devideBy ++;
				}
			}
		}
		
		if(devideBy > 0){
			ave = sum/devideBy;
		}
	} else 
		alert('Invalid parameter');
	return ave;
};

 var processOrientationInformation = function(mobileType, alpha ,beta ,gamma, gx, gy, gz ) {
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
			  }
		  } else if  (gy < (-6.0) & gy < gx & gy < gz ) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma = degToRad(gamma);
	//		  } else if ((alpha>140.0 & alpha <20.0) ){
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } 
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
		  }
	  } else if  (gy < (-6.0) & gy < gx & gy < gz ) {
		  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
			  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);	
		  } else if ((alpha>90.0 & alpha <270.0) ){
			  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
		  } 
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
		  } else if  (gy < (-6.0) & gy < gx & gy < gz ) {
			//beta >135 at this points beta sesnsor value is negative
			  //if((alpha<40.0 & alpha >0) || (alpha<360.0 & alpha >320.0)) {
			  if((alpha<90.0 & alpha >0) || (alpha<360.0 & alpha >270.0)) {
				  mbeta = -degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } else if ((alpha>90.0 & alpha <270.0) ){
				  mbeta = degToRad(beta); malpha = -degToRad(alpha); mgamma =degToRad(gamma);
			  } 
			  /**END OF gz */
		  }
	}
	 var temp = new Object();
	 temp.alpha = malpha;
	 temp.beta = mbeta;
	 temp.gamma = mgamma;
	 return temp;
 };



/**
 * Returns avarage of the most recent 10 values from the device compus
 */
processOrientationEvent = function (alphaValues,betaValues,gammaValues){
	var adjustedAlpha = 0;
	var adjustedBeta = 0;
	var adjustedGamma = 0;
	var len = alphaValues.length;
	var count = 0;	
	for(count = 0; count < len ; count++){
		adjustedAlpha += alphaValues[count];
		adjustedBeta += betaValues[count];
		adjustedGamma += gammaValues[count];
	}
	var temp = new Object();
	temp.alpha = adjustAngle(adjustedAlpha/len);
	temp.beta = adjustAngle(adjustedBeta/len);
	temp.gamma = adjustAngle(adjustedGamma/len);
	return temp;
};

/**
 * Private function. Value is rounded to 4 decimal values. Used for GPS and rotation angle of orientation.
 */
var adjustAngle = function(value){
	value = Math.round(value * 10000) / 10000 ;		
	return value;
};



})(window.FIware_wp13 = window.FIware_wp13 || {});

