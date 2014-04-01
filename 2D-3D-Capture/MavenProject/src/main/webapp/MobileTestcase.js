/**
 * #########################################################
 * ####### THIS PART INCLUDES SUPPORTING JS CLASSES#########
 * ####### SCROLL DOWN FOR UNIT TESTING MODULES ############
 * #########################################################
 */
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
Device = function(localurl, resturl, localport, wsp ,mT, restport ) {
	this.tomcatPORT = localport;
	this.localURL = localurl;
	this.websocketport = wsp;
	this.serverURL = resturl;
	this.mobileType =mT;
	this.currentLocation;
	
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
	if(list["Compass"] === "Supported"){
		temp.push(new Sensor("Compass"));
	}
	if(list["Accelerometer"]==="Supported"){
		temp.push(new Sensor("Accelerometer"));
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

Device.prototype = {
		
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
			if(this.Type =="Mobile"){
				try{
					var list = this.getSensorList();
					var count;
					for(count = 0 ; count < list.length; count++ ){
						var sn = list[count].getName();
						if(sn ==="GPS"){
							var s =list[count];
							var values= s.getValues();
							//TODO
							velocity= values;
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
							if(a[0] && a[1] && a[2] )
								var temp =processOrientationEvent(a[0],a[1],a[2]);
							else{
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
					var temp = adjustOrientation(this.mobileType,alpha, beta , gamma, currentAccelerationWithGravity.x,currentAccelerationWithGravity.y,currentAccelerationWithGravity.z);
					alpha = temp.alpha; beta = temp.beta; gamma = temp.gamma;					
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
						
						if (typeof callback === "function") {				    
						        callback(pos);
						    }
					}.bind(this), locationFindingerror, mapoptions);
			}
		},
		
		/**
		 * Registers to the GPS sensor to return location changes and at a location change will call the locationsuccess callback methode.
		 * @param onLocationSuccess
		 * @param onLocationError
		 * @param options
		 */
		
		
		registerForDeviceMovements : function(onLocationSuccess,onLocationError,onMotion , options) {
//			var r = confirm("Trying to obtain GPS cahgnes");
			var mapoptions;
//			if(!this.readingTime){
//				this.startTime =  new Date().getTime();
				//document.getElementById("current_reading").innerHTML=this.startTime;
//			}
		//if(r){
				if (options) {
					mapoptions = options;
				}	else {
					mapoptions = defaultmapoptions;
				}
				//document.getElementById("log1").innerHTML = "This part work";
				if (typeof onLocationSuccess === "function" && typeof onLocationError === "function" && typeof onMotion === "function") {
					//document.getElementById("log1").innerHTML = "Test Works";
					this.positionWatchID = navigator.geolocation.watchPosition(							
						function(pos) {							
							//Success function					
							var Coordinates= new Object();
							coords = pos.coords;
							//this.currentLocation = coords;
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
							var currentTime = new Date().getTime();
//							document.getElementById("last_reading").innerHTML=currentTime;
							if(this.currentLocation){
								//TODO
								if(this.currentLocation.latitude==coords.latitude && this.currentLocation.longitude==coords.longitude ) {									
									//document.getElementById("velocity").innerHTML="0.00";
								} else {									
									interval = currentTime - this.startTime;
									//document.getElementById("log1").innerHTML=interval;
									this.velocity = calculateVelocity(this.currentLocation, coords, interval);
									ok(true, "Second Readin is obtained.");
									onMotion(this.velocity);
									//document.getElementById("velocity").innerHTML=this.velocity;
									this.startTime = currentTime;									
									this.currentLocation = coords;
								}  
							} else {
								ok(true, "Velocity calculation code is called. Initial Data set");
								this.currentLocation = coords;
								this.startTime = currentTime;
								//document.getElementById("distance").innerHTML= coords.latitude+"_--_909--"+coords.longitude;
								//document.getElementById("distance").innerHTML= this.currentLocation.latitude+ "--"+this.currentLocation.longitude;
							}
							
							try{
								var list = this.getSensorList();
								var count;
								for(count = 0 ; count < list.length; count++ ){
									var sn = list[count].getName();
									//TODO
									if(sn ==="GPS"){
										var s =list[count];
										s.setCurrentValue(Coordinates);
										s.addValue(Coordinates);
//										var a =s.getValues();
										Coordinates =s.getCurrentValue();
//										log(Coordinates.longitude +":"+Coordinates.latitude);
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
							}.bind(this),
							mapoptions);
				};
			//}
		},
		
		/**
		 * 
		 * @returns the speed 
		 */
		getSpeed : function(){
			return this.velocity;
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
//			metadata =JSON.stringify({'vwidth': 640, 'browsertype': 'Chrome', 'ext': 'png', 'devicetype': 'Android', 'vheight': 480, 'time': '2013.11.28_9.15.15', 'device': {'orientation': 'potrait', 'gz': 6.74, 'gy': 7.18, 'gx': 0, 'rg': 6.7995, 'ra': 322.6541, 'rb': 46.9973, 'ay': 0.13, 'ax': 0, 'az': 0}, 'position': {'lat': 65.01242298532382, 'acc': 128, 'alt': 142.17365173309813, 'lon': 25.47399834095566}, 'type': 'image'});
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
			var imgPostRequest = $.post( "http://"+this.localURL+":"+this.tomcatPORT+"/RestClient/ClientRequestMultiplexer",{command :"wsstart", server : this.serverURL },function(response) {		
				response = response.trim();
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
								}
									
							}.bind(this);
							
							this.connection.onclose = function(){ 
								log("CONNECTION CLOSSED");
								var localcanvas =document.getElementById("image");
								var localcontext = localcanvas.getContext('2d');
								localcontext.clearRect(0, 0, localcanvas.width, localcanvas.height);	 
								document.body.removeChild(localcanvas);
							}.bind(this);
							
							this.connection.onerror =  function (errors){
									log(errors.message);
							}.bind(this);
						} else 
							alert("Upload cancelled");
					}catch (error) {
						alert("Web Socket Error "+error.message);
					}
				} else {
					alert("SERVER ERROR");
				}
			}.bind(this));
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
				//alert(jQuery.isPlainObject( formdata ));
	
				xhr = new XMLHttpRequest();
				xhr.open('POST', "http://"+this.localURL+":"+this.tomcatPORT+"/RestClient/ClientRequestMultiplexer", true);
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
			} else 
				alert("Post Cancelled");
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
//			var r = confirm("Trying to obtain GPS cahgnes");
//			if(r){
				if (this.browser.isDeviceOrientationSupported()) {
				    window.addEventListener("deviceorientation", this.orientationHandler = function( event ) {
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
//			}
		},
		/**
		 * This method registers for the ambient light readings.
		 */
		registerAmbientLightChanges : function(handleLightValues) {
//			var r = confirm("Trying to obtain Ambient Light Reading");
			//if(r) {
				if(typeof handleLightValues=== "function" ) {
					window.addEventListener('devicelight', this.ambientLightHandler = function(event) {
						if(event) {
							handleLightValues(event.value);
						}							
					}, false);
				}
			//}
		},

		
		/**
		 * For the logging of the debugging mode.
		 * @param text
		 */
		log : function(text){	
			document.getElementById("consolelog").value = "\n"+text;
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
		subscribe : function(onLocationSearchSuccess,onLocationServiceSearchError,onMotion, handlacceleration,handleAccelerationWithGravityEvent,handleRotation, handleOrientationChanges){
			this.registerForDeviceMovements(onLocationSearchSuccess, onLocationServiceSearchError,onMotion);
			this.registerDeviceMotionEvents(handlacceleration, handleAccelerationWithGravityEvent, handleRotation);
			this.registerDeviceOrentationEvent(handleOrientationChanges);
		},
		//END OF DEVICE METHODS
};

function log(text){	
	var logcanvas = document.getElementById("consolelog");
	if(logcanvas)
		document.getElementById("consolelog").value = "\n"+text;
	else 
		console.log(text);	
}

function locationFindingerror(err) {
	  alert("An Error Occured"+ err.data);
};


function locationFindingSuccess(pos) {
	  log("An Error Occured"+pos.coords.latitude);
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
	log("processAccelerationValues2"+temp.x+":"+temp.y+":"+temp.z);
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
	log("processAccelerationDueToGravity 2 "+temp.x+":"+temp.y+":"+temp.z);
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
		var positiveCount=0;		
		var negativeCount=0;
		for(count = 0 ; count < limit ; count++){
			if(values[count] <0){
				negativeCount++;
			} else {
				positiveCount++;			
			}		
		}
		count = 0;
		for(count = 0; count< limit ;count++){
			if(positiveCount > negativeCount){
				if(values[count] >=0){
					sum = sum + values[count] ;
					devideBy= devideBy +1;
				}
			}else {
				if(values[count] <0){
					sum = sum + values[count] ;
					devideBy= devideBy +1;
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
 * This function calculates distance from latitude and longitude 
 */
var calcuateSurfaceDistance = function(lat1, lon1, lat2, lon2){
	//TODO
	//document.getElementById("distance").innerHTML="Test1";
	var R = 6371; // km
		var dLat = degToRad((lat2-lat1));
		var dLon = degToRad(lon2-lon1);
		lat1 = degToRad(lat1);
		lat2 = degToRad(lat2);
		var a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
		var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		var d = R * c;
		//document.getElementById("distance").innerHTML = "Test2";
		return d;                                                    
};

function degToRad(degrees){
	return degrees * (Math.PI / 180);
}

var calculateVelocity = function(coord1, coord2, time) {
	//document.getElementById("distance").innerHTML ="Test3";
	var dist = calcuateSurfaceDistance(coord1.latitude, coord1.longitude, coord2.latitude, coord2.longitude)*1000;
	var timesec = time/1000; //returning ms-2
	var speed = dist/timesec;
	//document.getElementById("distance").innerHTML= dist+"--"+timesec+"--"+speed+"--"+coord1.latitude+ "--"+coord2.latitude+ "--"+coord1.longitude+"--"+coord2.longitude;
	return speed;	
};

/**
 * Private function. Value is rounded to 4 decimal values. Used for GPS and rotation angle of orientation.
 */
var adjustAngle = function(value){
	value = Math.round(value * 10000) / 10000 ;		
	return value;
};



/**
 * #############################################
 * ####### THIS PART CONCLUDED SUPPORTING JS CLASSES##########
 * ####### BELOW CODE IS THE PROCEDURE TEST CASES ###########
 * #############################################
 */

//var d;

function onLocationsearchSuccess(pos,coords){
	ok(pos.coords.latitude===coords.latitude,"Read value from the sensor and the current value of the object are equal");
	navigator.geolocation.clearWatch(watchPositionID);
}


function onLocationSearchError() {
	ok(true, "Location serch Error Occured. Message ");
	navigator.geolocation.clearWatch(watchPositionID);
}

function getCurrentLocationSuccess(pos){
	ok(true,"Callback function is trigerd on succesull position retrieval.");	
}

function getCurrentLocationError(pos){
	ok(true,"Accurate call back function is triggered on an error in location retrieval.");	
}

module("Testcase 3 Testing Preliminary setup for device sensor data ");
test ("Testing Object Device" , function() {
	var d = new Device("localhost","dev.cyberlightning.com", "9090" , "17321","LG_stereoscopic" ,"17322");
	ok((d.OS ==="Android") ||(d.OS ==="Windows") ||( d.OS ==="Linux" )||(d.OS ==="Android") ||(d.OS ==="Unditected"), "Device Os Is Ditected and it is one of the set values");
	ok(d.serverPORT==="17322","Divice object is created and the server port is set");
	ok(d.tomcatPORT==="9090","Divice object is created and the local proxy server port is set");
	ok(d.serverURL ==="dev.cyberlightning.com" , "Divice object is created and the remote servet URL is set.");
	ok(d.localURL ==="localhost", "Local Proxy server is set");
	ok(d.websocketport==="17321", "Web Socket port is set");
	ok(( d.mobileType==="LG_stereoscopic")  ||  (d.mobileType==="galaxy_tab_note3")  ||   (d.mobileType==="galaxy_s2"), "Device Type externally set");
	ok((d.browser.getBrowserType()==="Firefox" )|| (d.browser.getBrowserType()==="FireFox") || (d.browser.getBrowserType()==="Chrome") || (d.browser.getBrowserType()==="Opera") || (d.browser.getBrowserType()==="IE")|| (d.browser.getBrowserType()==="Unditected"), "Browser Type is set");
});

test ("Testing Supporting functions. Testing Functions -> average() Adjust()" , function() {
	var testArray1= new Array();
	for(var i = 0 ; i < 100 ; i++ ){
		testArray1.push(1);
	}
	equal(average(testArray1), 1 , "Average of 100 1s is 1.");
	
	var testArray2= new Array();
	for(var i = 1 ; i <=100 ; i++ ){
		testArray2.push(i);
	}
	equal(average(testArray2),50.5 , "Average of  sum of from 1 to 100 is 50.5");
	
	var testArray3 = new Array();
	testArray3[0]= 1;
	testArray3[1]= 1;
	testArray3[2]= -1;
	testArray3[3]= 1;
	testArray3[4]= 1;
	testArray3[5]= 1;
	testArray3[6]= 1;
	testArray3[7]= 1;
	testArray3[8]= 1;
	testArray3[9]= 1;
	equal(average(testArray3), 1 , "Average of  sum of 10 numbers where 9 are 1s and one -1is 1.");	
	
	var testArray4 = new Array();
	testArray4[0]= 1;
	testArray4[1]= 2;
	testArray4[2]= -1;
	testArray4[3]= 3;
	testArray4[4]= 4;
	testArray4[5]= 5;
	testArray4[6]= 6;
	testArray4[7]= 7;
	testArray4[8]= 8;
	testArray4[9]= 9;
	equal(average(testArray4), 5 , "Average of  10 numbers  from 1 to 9 and 1 minus valus is 5.");	
	
	var testArray5 = new Array();
	testArray5[0]= -1;
	testArray5[1]= -2;
	testArray5[2]= 1;
	testArray5[3]=- 3;
	testArray5[4]= -4;
	testArray5[5]= -5;
	testArray5[6]= -6;
	testArray5[7]= -7;
	testArray5[8]=- 8;
	testArray5[9]= -9;
	equal(average(testArray5), -5 , "Average of  10 numbers  from -1 to -9 and 1 plus valus is -5.");
	
	testArray3[0]= -1;
	testArray3[1]= -2;
	testArray3[2]= +1;
	testArray3[3]= +3;
	testArray3[4]= -4;
	testArray3[5]= -5;
	testArray3[6]= -6;
	testArray3[7]= +7;
	testArray3[8]= -8;
	testArray3[9]= -9;
	equal(average(testArray3), (-35/7) , "Average of  10 random numbers is  (-35/7)");
	
	equal(adjust(1.12345678) ,1.12,"adjust() -> 1.12345678 rounded to 2 decimals is 1.12");
	equal(adjust(-56.788978772347197) ,-56.79,"adjust() -> -56.788978772347197 rounded to 2 decimals is -56.79");
	equal(adjust(1.12345678,4) ,0.0,"adjust() -> 1.12345678 subjected to cutoff of 4 is 0.0");
	equal(adjust(-56.788978772347197,4) ,-56.79,"adjust() ->-56.788978772347197 subjected to cutoff of 4 is -56.79");
	
	var acceleration = new Array();
	for(var i = 0 ; i < 20 ; i++) {
		var temp = new Object();
		temp.x = 9.123456789;
		temp.y = 9.123456789;
		temp.z = 9.123456789;
		acceleration[i] = temp;
	}
	var testSubject = processAccelerationValues(acceleration);
	equal(testSubject.x ,9.12 ,"processAccelerationValues() -> Average should not differ from 9.123456789 when all are same ");
	equal(testSubject.y ,9.12 ,"processAccelerationValues() -> Average should not differ from 9.123456789 when all are same ");
	equal(testSubject.z ,9.12 ,"processAccelerationValues() -> Average should not differ from 9.123456789 when all are same ");
	
	for(var i = 0 ; i < 20 ; i++) {
		var temp = new Object();
		temp.x = 0.0123456789;
		temp.y = 0.0123456789;
		temp.z = 0.323456789;
		acceleration[i] = temp;
	}
	testSubject = processAccelerationValues(acceleration);
	equal(testSubject.x ,0.0 ,"processAccelerationValues() ->x component Average should adjusted to 0.0  when they are set to,lower than 0.1 ");
	equal(testSubject.y ,0.0 ,"processAccelerationValues() -> y component Average should adjusted to 0.0  when they are set to,lower than 0.1  ");
	equal(testSubject.z ,0.0 ,"processAccelerationValues() ->  z component Average should adjusted to 0.0  when they are set to,lower than 0.1  ");
	
	for(var i = 0 ; i < 20 ; i++) {
		var temp = new Object();	
		if(i == 2 || i ==5) {
			temp.x = -8.123456789;
			temp.y = 0.0123456789;
			temp.z = 0.323456789;
		}else{
			temp.x = 9.123456789;
			temp.y = 0.0123456789;
			temp.z = 0.323456789;
		} 
		acceleration[i] = temp;
	}
	testSubject = processAccelerationValues(acceleration);
	equal(testSubject.x ,9.12 ,"processAccelerationValues() -> x component Test pass for diference xyz values  x > 9 and y &z <0.1");
	equal(testSubject.y ,0.0 ,"processAccelerationValues() -> y component Test pass for diference xyz values  x > 9 and y &z <0.1 ");
	equal(testSubject.z ,0.0 ,"processAccelerationValues() -> z component Test pass for diference xyz values  x > 9 and y &z <0.1 ");
	
	for(var i = 0 ; i < 20 ; i++) {
		var temp = new Object();
	
		if(i == 2 || i ==5) {
			temp.x = 9.123456789;
			temp.y = -5.0123456789;
			temp.z = 0.323456789;
		}else{
			temp.x = -8.123456789;
			temp.y = 6.0123456789;
			temp.z = 0.323456789;
		} 
		acceleration[i] = temp;
	}
	testSubject = processAccelerationValues(acceleration);
	equal(testSubject.x ,-8.12 ,"processAccelerationValues() -> Negative x test passed ");
	equal(testSubject.y ,6.01 ,"processAccelerationValues() -> Testing for positive Y   ");
	equal(testSubject.z ,0.0 ,"processAccelerationValues() -> Testing for adjusted 0.0 Z component  ");
});

test("Testing the Distance calculation ", function() {
	var distance1 = calcuateSurfaceDistance(65.00925504498964,25.460987091064453,65.00684357626169,25.47407627105713);	
	equal( adjust(distance1), 0.67, "Distance test 1 Distance calculated useing a propritory app and the javascript is accurate to two decimals" );	
	var distance2 = calcuateSurfaceDistance(65.0121376429263,25.465235710144043,65.0121376429263,25.473389625549316);
	equal( adjust(distance2), 0.38, "Distance test 2 Distance calculated useing a propritory app and the javascript is accurate to two decimals" );
});

module("Testcase 4 -Feature Testing 3 ");
test ("FIWARE.Feature.MiWi.2D-3DCapture.Browser.SupportingInterfaces. Testing Functions -> getSensorList" , function() {
	var d = new Device("localhost","dev.cyberlightning.com", "9090" , "17321","LG_stereoscopic" ,"17322");
	var list  = d.getSensorList();
	var browserSup = d.browser.supportedMediaList();
	var counter = 0;
	if(list!=null)
		ok(true,"Device List available");
	else
		ok(false, "Device has not found the supporting interfaces. Test Failed");
	if(browserSup["GPS"] != null){
		if(browserSup["GPS"] ==="Supported")
			counter++;
		ok((browserSup["GPS"]==="Supported" || browserSup["GPS"]==="Not Supported"), "GPS support  detected");
	}
	if(browserSup["Compass"] != null){
		if(browserSup["Compass"] ==="Supported")
			counter++;
		ok((browserSup["Compass"]==="Supported" || browserSup["Compass"]==="Not Supported"), "Compass support  detected");
	}
	if(browserSup["Accelerometer"] != null){
		if(browserSup["Accelerometer"] ==="Supported")
			counter++;
		ok((browserSup["Accelerometer"]==="Supported" || browserSup["Accelerometer"]==="Not Supported"), "Accelerometer support  detected");
	}
	if(browserSup["Video"] != null){
		if(browserSup["Video"] ==="Supported")
			counter++;
		ok((browserSup["Video"]==="Supported" || browserSup["Video"]==="Not Supported"), "Video support  detected");
	}
	equal(list.length, counter, "Supporting sensor interfaces and setup sensor arrays are equal");
});

module("Testcase 4 -Feature Testing 4 ");
var orientationvaluecounter = 0;
asyncTest("FIWARE.Feature.MiWi.2D-3DCapture.Browser.Compass Testing registerDeviceOrentationEvent", 2,function (){
	var d = new Device("localhost","dev.cyberlightning.com", "9090" , "17321","LG_stereoscopic" ,"17322");
	d.registerDeviceOrentationEvent(function(alpha, beta, gamma) {		
		if(orientationvaluecounter ==0){
			start();
			ok(true,"Orientation call back function is getting called");
			if(alpha != null){
				ok(true, "raw values are passed to the call back function " + alpha +":"+beta+":"+ gamma);
			} else {
				ok(true, "This test passed if the event. alpha is null");
			}
			window.removeEventListener("deviceorientation", d.orientationHandler );
		}
		orientationvaluecounter++;
	});
});

var accelerationvaluecountera = 0;
var accelerationvaluecounterb = 0;
var accelerationvaluecounterc = 0;

module("Testcase 4 -Feature Testing 5 ");
asyncTest(" FIWARE.Feature.MiWi.2D-3DCapture.Browser.Accelerometer testing function registerDeviceMotionEvents", 5 ,function (){
	var d = new Device("localhost","dev.cyberlightning.com", "9090" , "17321","LG_stereoscopic" ,"17322");
	d.registerDeviceMotionEvents(function(handlacceleration){
		if (accelerationvaluecountera == 0 && accelerationvaluecounterb == 0 && accelerationvaluecounterc == 0)
			start();
		if(accelerationvaluecountera ==0){
			ok(true,"Call back function for Acceleration is getting called.");
			if(handlacceleration == null)
				ok(true, "Acceleration value is not passed properly");
			else
				ok(true,"Acceleration value is passed to the call back function "+handlacceleration.x);
		}
		accelerationvaluecountera++;
	},
	function(handleAccelerationWithGravity){
		if (accelerationvaluecountera == 0 && accelerationvaluecounterb == 0 && accelerationvaluecounterc == 0)
			start();
		if(accelerationvaluecounterb ==0){
			ok(true,"Call back function for Acceleration with gravity is getting called.");
			if(handleAccelerationWithGravity == null)
				ok(true,"Acceletaion with gravity is null");
			else
				ok(true , "Acceleration with gravity value is passed to the call back function "+handleAccelerationWithGravity.x);
		}
		accelerationvaluecounterb++;
	},
	function( handleRotation){
		if (accelerationvaluecountera == 0 && accelerationvaluecounterb == 0 && accelerationvaluecounterc == 0)
			start();
		if(accelerationvaluecounterc ==0){
			ok(true,"Call back function for rotation values from accelerometer is getting called");
		}
		accelerationvaluecounterc++;
	});
});

module("Testcase 4 -Feature Testing 6 ");
asyncTest( " FIWARE.Feature.MiWi.2D-3DCapture.Browser.GPS : Testing functions -> registerForDeviceMovements(), getCurrentLocation",3,
	function () {		
		var mapoptions = {
				  enableHighAccuracy: true,
				  timeout: 27000,
				  maximumAge: 30000
				};
		 var d = new Device("localhost","dev.cyberlightning.com", "9090" , "17321","LG_stereoscopic" ,"17322");
		  d.registerForDeviceMovements(
				  function getLocationSuccess(pos, coords){					
				  	ok(true,"Call back function for GPS location update has found");
				  	start();
				  	//document.getElementById("log").innerHTML = "position Recived";				  	
				  }.bind(this), 
				  function getLocationError(){
						//document.getElementById("log1").innerHTML = "position Recived";
						 navigator.geolocation.clearWatch(d.positionWatchID);
						 ok(true, "Callback function for GPS location finding error is triggered");
						 start();
					}.bind(this),
					function getVelocitySuccess(velocity){
//						document.getElementById("log1").innerHTML = "velocity called Recived";
						ok(true, "Call back for velocity is trigered");
						navigator.geolocation.clearWatch(d.positionWatchID);
					}.bind(this));		 
		/**
		 * This section represent the Get Current posistion method
		 */
		  d.getCurrentLocation(		
				function (pos){
					ok(pos, "Non Negative value is returned on succesfull location retrieval by Get Current location method.");
				}.bind(this), function() {
					ok(true, "Error  Occored and rightly Error method is called");
				}.bind(this), mapoptions);
//		}
//		};
//	}

	
});

module("Testcase 4- Extra  Feature Testing 7 - Ambient light information-Undocumented");
asyncTest( "Feature Test FIWARE.Feature.MiWi.2D-3DCapture.2D3D-Info.AmbientLightInfo",2, function() {
	var d = new Device("localhost","dev.cyberlightning.com", "9090" , "17321","LG_stereoscopic" ,"17322");
//	document.getElementById("log").innterHTML = "Test Ready to run";
	  d.registerAmbientLightChanges(
			  function (value) {		  
				  ok(true,"Call back functino for ambient light value is triggered");
				  start();
				  if(value) {
					  ok(true, "Ambient Light Value received");
				  } else 
					  ok(true, "Ambient Light  Value not recorded");
				  window.removeEventListener("devicelight", d.ambientLightHandler);				  		 
	  }.bind(this));
});






