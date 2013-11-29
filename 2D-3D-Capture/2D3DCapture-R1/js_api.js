

///**
// * Java script API for HTML5 device motion, device orientation, 
// * location and camera feeds. This API is a simple extension for 
// * existing APIs so that one single doc provides means to test 
// * availability and the extraction of the information from GPS 
// * locator, accelerometer, Gyroscope for the usage of browser 
// * applications.
// * 
// * Author : Tharanga Wijethilake
// * copyrights : Cyberlightning
// * Project: FI-ware
// * 
// */



var deviceAgentString="";
 
 var getUAString = function(){
	 var uagent = navigator.userAgent.toLowerCase();
	 return  uagent;
 };
 
 var getDeviceType = function(){
	 var uagent = navigator.userAgent.toLowerCase();
	 var device="";
	 if(uagent.search("android") != -1){
		 device = "Android";
 	 }else if (uagent.search("windows") != -1){
		 device = "Windows";
	 }
	 return device;	 
 };
 
 var getBrowserType = function() {
	 var browser= "";
	 var uagent = navigator.userAgent.toLowerCase();
	 if(uagent.search("firefox") != -1){
		 browser = "FireFox";
	 } else if(uagent.search("chrome") != -1){
		 browser = "Chrome";
	 } else if(uagent.search("msie") != -1){
		 browser = "IE";
	 }
	 return browser;
 };

var accWithGravity = new Array();

///**
// * This function returns true if the browser is able to  
// * obtain readings from the Gyroscope. 
// * @returns {Boolean}
// */
function isDeviceOrientationSupported() {
	if (window.DeviceOrientationEvent) {		
		return true;
	} else {		  
		return false;
	}
}

///**
// * This function returns true if the browser is able to  
// * obtain readings from the accelerometer.
// * @returns {Boolean}
// */
function isDeviceMotionSupported(){
	if (window.DeviceMotionEvent) {		
		return true;
	} else {		  
		return false;
	}
}

///**
// * This function returns true if the browser is able to  
// * obtain readings from the accelerometer.
// * @returns {Boolean}
// */
function isGeolocationSupported()
{
	if ("geolocation" in navigator) {		
		return true;
	} 
	else { 
		return false;
	}
}

///**
// * This function returns true if the browser is able to  
// * obtain camera feed from the local camera to the video element. 
// * @returns {Boolean}
// */
function hasMediaSupport() {
	return !!(navigator.getUserMedia || navigator.webkitGetUserMedia ||navigator.mozGetUserMedia || navigator.msGetUserMedia);
}

var videoelement;

///**
// * This is the callback function if the acquiring of the video feed 
// * is a success.  
// */
var onVideoSuccess = function(stream) {
	videoelement = document.querySelector('video');
    videoelement.src = window.URL.createObjectURL(stream);
    videoelement = stream;
};

///**
// * This Function attempts to start the video feed from the local device
// * camera.
// * @param onFailure This is the call back function in an event of 
// * 					a failure to obtain the video feed.
// */
function startVideo(onFailure) {
	videoelement = document.querySelector('video');
	navigator.getUserMedia = (navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia);
	if(videoelement){
		if (typeof onFailure === "function") 				    
			navigator.getUserMedia({video: true, audio: true},onVideoSuccess , onFailure);
		return videoelement;
	} else{
		alert("There is not video element");
		return null;
	}	
}
//
///**
// * This dictionary is the default 
// */
var defaultmapoptions = {
		  enableHighAccuracy: true,
		  timeout: 27000,
		  maximumAge: 30000
		};

function locationFindingerror(err) {
	  alert("An Error Occured"+ err.data);
};

///**
// * This function returns the GPS coordinates of the current location.
// * @param callback On successful retrieval of location this function
// * 					is the callback function to handle the acquired data.
// */
function getLocation(callback,options){
	var mapoptions;
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
		}, locationFindingerror, mapoptions);	
}

///**
// * This function returns location in case of a location change. It is an 
// * asynchronous function that passes the data to a onLocationSuccess method.
// * @param onLocationSuccess callback function for a successfull retrieval of the changed location
// * @param onLocationError callback function on failure
// * @param options A dictionary to make the location call default dictionary is used in case of 
// * 					null value.
// */
var registerForDeviceMovements = function(onLocationSuccess,onLocationError,options) {
	var mapoptions;
	if (options) {
		mapoptions = options;
	}	else {
		mapoptions = defaultmapoptions;
	}
	if (typeof onLocationSuccess === "function" && typeof onLocationError === "function") {
		id = navigator.geolocation.watchPosition(onLocationSuccess, onLocationError, mapoptions);
	};	
};

///**
// * This function registers to device motion data from the accelerometer and from the gyroscope.There are
// * three callback functions to handle acceleration, acceleration due to gravity and device rotation.
// * @param handlacceleration
// * @param handleAccelerationWithGravity
// * @param handleRotation
// */


///**
//* This function registers to device motion data from the accelerometer and from the gyroscope.There are
//* three callback functions to handle acceleration, acceleration due to gravity and device rotation.
//* @param handlacceleration
//* @param handleAccelerationWithGravity
//* @param handleRotation
//*/

var accx = new Array();
var accy = new Array();
var accz = new Array();
var gaccx = new Array();
var gaccy = new Array();
var gaccz = new Array();


var registerDeviceMotionEvents = function(handlacceleration, handleAccelerationWithGravity, handleRotation){

	window.addEventListener("devicemotion", function(event) {
		var rotationRate = event.rotationRate;		
		var acceleration = event.acceleration;
		if(acceleration ) {
			var temp = processAccelerationValues(acceleration);
			if (typeof handlacceleration === "function") {
				if(temp.x || temp.y || temp.z)
					handlacceleration(temp);
			};
		}
		var accelerationWithGravity = event.accelerationIncludingGravity;		
		if(accelerationWithGravity){
			var temp = processAccelerationDueToGravity(accelerationWithGravity);
				if (typeof handleAccelerationWithGravity === "function")
					handleAccelerationWithGravityEvent(temp);			
		};
		if(rotationRate){
			if (typeof handleRotation === "function")
				handleRotation(rotationRate);
		};		
	}, true);
};

/**This method tries to stabalise the values generated by the accelerometer
* to identify in which direction the divice may be moving.
*/
var processAccelerationValues= function(acceleration){
	var adjustedAX=0;
	var adjustedAY=0;
	var adjustedAZ=0;
	var count = 0;

//	var value = acceleration.x;
	var value = adjust(acceleration.x,0.1);
	var len = accx.length;
	accx.push(value);
	if(len===10) {
		adjustedAX= adjust(average(accx),0.1);		
		accx.splice(0, 1); 
	}
	
//	value = acceleration.y;
	value = adjust(acceleration.y,0.1);
	len = accy.length;
	accy.push(value);
	if(len===10) {
		adjustedAY= adjust(average(accy),0.1);		
		accy.splice(0, 1); 
	}
	
//	value = acceleration.z;
	value = adjust(acceleration.z,0.1);
	len = accy.length;
	accz.push(value);
	if(len===10) {
		adjustedAZ= adjust(average(accz),0.4);
		accz.splice(0, 1); 
	}
	var temp = new Object();
	temp.x = adjustedAX;
	temp.y = adjustedAY;
	temp.z = adjustedAZ;
	return temp;
	
};
/**
* This function would determine the down in the context the mobile is used.
*/
var processAccelerationDueToGravity= function(acceleration){
	var adjustedGAX=0;
	var adjustedGAY=0;
	var adjustedGAZ=0;
	var count = 0;

//	var value = acceleration.x;
	var value = adjust(acceleration.x, 2.0);
	var len = gaccx.length;
	gaccx.push(value);
	if(len===10) {
		adjustedGAX= adjust(average(gaccx),2.0);		
		gaccx.splice(0, 1); 
	}
	
//	value = acceleration.y;
	value = adjust(acceleration.y, 2.0);
	len = gaccy.length;
	gaccy.push(value);
	if(len===10) {
		adjustedGAY= adjust(average(gaccy),2.0);		
		gaccy.splice(0, 1); 
	}
	
//	value = acceleration.z;
	value = adjust(acceleration.z, 2.0);
	len = gaccy.length;
	gaccz.push(value);
	if(len===10) {
		adjustedGAZ= adjust(average(gaccz),2.0);
		gaccz.splice(0, 1); 
	}
	
	var temp = new Object();
	temp.x = adjustedGAX;
	temp.y = adjustedGAY;
	temp.z = adjustedGAZ;
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
	var ave= 0;
	var sum = 0;
	var count;
	var devideBy = 0;
	if (values instanceof Array) {
		var oneBefore = 0;
		var oneAfter = 0;		
		for(count = 1 ; count < 9 ; count ++) {
			oneBefore= values[count-1];
			oneAfter = values[count+1];
			if(((oneBefore <= 0)&&(values[count] <= 0)&&(oneAfter <=0)) || ((oneBefore >= 0)&&(values[count] >= 0)&&(oneAfter >= 0))) {
				if(count == 1)
					sum = values[0];				
				sum = sum + values[count];
				if(count == 8)
					sum = sum +values[9];
				if(count == 1 || count == 8){
					devideBy= devideBy +2;
				}else {
					devideBy ++;
				}
			}
		}
		if(devideBy > 0)
			ave = sum/devideBy;
	} else 
		alert('Invalid parameter');
	return ave;
};

var alphaValues = new Array();
var betaValues = new Array();
var gammaValues = new Array();

///**
// * This function registers to device orientation data and calls eventHandlingFunction in case of device
// * orientation is changed in along x, y and x axis. These are thoroughly explained in the following doc.
// * //https://developer.mozilla.org/en-US/docs/Web/Guide/API/DOM/Events/Orientation_and_motion_data_explained?redirectlocale=en-US&redirectslug=Web%2FGuide%2FDOM%2FEvents%2FOrientation_and_motion_data_explained
// * 
// * This method passes 5 values to the call back function, rotated angle around x,y,z axis , if the values
// * are calculated based on the earth coordinate system and device orientation in mode.(landscape /portrait.) 
// *  
// * @param eventHandlingFunction callback function for a successful data retrieval
// */
var reigsterDeviceOrentationEvent = function(eventHandlingFunction){
	if (window.DeviceOrientationEvent) {
	    window.addEventListener("deviceorientation", function( event ) {
			//gamma: left to right
			var aroundYaxis = event.gamma;
			var orienation;
			if(aroundYaxis < -25 || aroundYaxis > 25)
				orienation = "landscape";
			else 
				orienation = "potrait";
			var temp = processOrientationEvent(event);
			if (typeof eventHandlingFunction === "function") {				    
				eventHandlingFunction(temp.alpha, temp.gamma, temp.beta , orienation );
		    };
	    }, true);
	};
};

var processOrientationEvent = function (event){
	var adjustedAlpha = 0;
	var adjustedBeta = 0;
	var adjustedGamma = 0;
	
	var value = adjustAngle(event.alpha);
	var len = alphaValues.length;
	var count;	
	alphaValues.push(value);
	
	if(len===10) {
		adjustedAlpha= (average(alphaValues));		
		alphaValues.splice(0, 1); 
	}
	
	value = adjustAngle(event.beta);
	len = betaValues.length;	
	betaValues.push(value);
	
	if(len===10) {
		adjustedBeta= (average(betaValues));		
		betaValues.splice(0, 1); 
	}
	
	value = adjustAngle(event.gamma);
	len = gammaValues.length;	
	gammaValues.push(value);
	
	if(len===10) {
		adjustedGamma= (average(gammaValues));		
		gammaValues.splice(0, 1); 
	}
	var temp = new Object();
	temp.alpha = adjustAngle(adjustedAlpha);
	temp.beta = adjustAngle(adjustedBeta);
	temp.gamma = adjustAngle(adjustedGamma);
	return temp;
};

/**
 * ditermine if a value is bounded by a certain value about zero.
 * If not bouded value is rounded to two decimal places.
 */
var adjustAngle = function(value){
	value = Math.round(value * 10000) / 10000 ;		
	return value;
};

///**
// * This function converts an extracted image data from a canvas to binary coded array. 
// * @param  imagedata 
// */
var binaryCodeImage = function(imagedata){
	var canvaspixelarray = imagedata.data;	    	    
	var canvaspixellen = canvaspixelarray.length;
	var bytearray = new Uint8Array(canvaspixellen);	    	    
	for (var i=0;i<canvaspixellen;++i) {
	    bytearray[i] = canvaspixelarray[i];	        
	}
	return bytearray;
};

