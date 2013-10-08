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
 * This function returns true if the browser is able to  
 * obtain readings from the Gyroscope. 
 * @returns {Boolean}
 */
function isDeviceOrientationSupported() {
	if (window.DeviceOrientationEvent) {		
		return true;
	} else {		  
		return false;
	}
}


/**
 * This function returns true if the browser is able to  
 * obtain readings from the accelerometer.
 * @returns {Boolean}
 */
function isDeviceMotionSupported(){
	if (window.DeviceMotionEvent) {		
		return true;
	} else {		  
		return false;
	}
}

/**
 * This function returns true if the browser is able to  
 * obtain readings from the accelerometer.
 * @returns {Boolean}
 */
function isGeolocationSupported()
{
	if ("geolocation" in navigator) {		
		return true;
	} 
	else { 
		return false;
	}
}

/**
 * This function returns true if the browser is able to  
 * obtain camera feed from the local camera to the video element. 
 * @returns {Boolean}
 */
function hasMediaSupport() {
	return !!(navigator.getUserMedia || navigator.webkitGetUserMedia ||navigator.mozGetUserMedia || navigator.msGetUserMedia);
}

var videoelement;

/**
 * This is the callback function if the acquiring of the video feed 
 * is a success.  
 */
var onVideoSuccess = function(stream) {
	videoelement = document.querySelector('video');
    videoelement.src = window.URL.createObjectURL(stream);
    videoelement = stream;
};

/**
 * This Function attempts to start the video feed from the local device
 * camera.
 * @param onFailure This is the call back function in an event of 
 * 					a failure to obtain the video feed.
 */
function startVideo(onFailure) {
	videoelement = document.querySelector('video');
	navigator.getUserMedia = (navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia);
	if(videoelement){
		if (typeof onFailure === "function") 				    
			navigator.getUserMedia({video: true, audio: true},onVideoSuccess , onFailure);
	} else
		log("There is not video element");
}

/**
 * This dictionary is the default 
 */
var defaultmapoptions = {
		  enableHighAccuracy: true,
		  timeout: 27000,
		  maximumAge: 30000
		};

function locationFindingerror(err) {
	  alert("An Error Occured"+ err.data);
};
/**
 * This function returns the GPS coordinates of the current location.
 * @param callback On successful retrieval of location this function
 * 					is the callback function to handle the acquired data.
 */
function getLocation(callback,options){
	var mapoptions;
	if (options) {
		mapoptions = options;
	}
	else {
		mapoptions = defaultmapoptions;
	}
	navigator.geolocation.getCurrentPosition(
		function (pos){
			if (typeof callback === "function") {				    
			        callback(pos);
			    }
		}, locationFindingerror, mapoptions);	
}

/**
 * This function returns location in case of a location change. It is an 
 * asynchronous function that passes the data to a onLocationSuccess method.
 * @param onLocationSuccess callback function for a successfull retrieval of the changed location
 * @param onLocationError callback function on failure
 * @param options A dictionary to make the location call default dictionary is used in case of 
 * 					null value.
 */
var registerForDeviceMovements = function(onLocationSuccess,onLocationError,options) {
	var mapoptions;
	if (options) {
		mapoptions = options;
	}
	else {
		mapoptions = defaultmapoptions;
	}
	if (typeof onLocationSuccess === "function" && typeof onLocationError === "function") {
		id = navigator.geolocation.watchPosition(onLocationSuccess, onLocationError, mapoptions);
	};	
};

/**
 * This function registers to device motion data from the accelerometer and from the gyroscope.There are
 * three callback functions to handle acceleration, acceleration due to gravity and device rotation.
 * @param handlacceleration
 * @param handleAccelerationWithGravity
 * @param handleRotation
 */
var registerDeviceMotionEvents = function(handlacceleration, handleAccelerationWithGravity, handleRotation){
	//alert("It starts too");
	window.addEventListener("devicemotion", function(event) {
		var rotationRate = event.rotationRate;		
		var acceleration = event.acceleration;
		var accelerationWithGravity = event.accelerationIncludingGravity;
		if(acceleration ) {
			if (typeof handlacceleration === "function") {
				handlacceleration(acceleration);
			};
		};
		if(accelerationWithGravity){
				if (typeof handleAccelerationWithGravity === "function")
					handleAccelerationWithGravityEvent(accelerationWithGravity);			
		};
		if(rotationRate){
			if (typeof handleRotation === "function")
				handleRotation(rotationRate);
		}
		
	}, true);
	

};

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
var reigsterDeviceOrentationEvent = function(eventHandlingFunction){
	if (window.DeviceOrientationEvent) {
	    window.addEventListener("deviceorientation", function( event ) {
			//gamma: left to right
			var leftToRight = event.gamma;

			var orienation;
			if(leftToRight < -25 || leftToRight > 25)
				orienation = "landscape";
			else 
				orienation = "potrait";
	//		alert(frontToBack );
			if (typeof eventHandlingFunction === "function") {				    
				eventHandlingFunction(event.alpha, event.gamma, event.beta , event.absolute , orienation );
		    }
	    }, true);
	}
};

