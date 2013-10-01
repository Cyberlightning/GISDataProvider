//This Function is to check if the gyroscope is 
//accessible on the browser
function isDeviceOrientationSupported() {
	if (window.DeviceOrientationEvent) {		
		return true;
	} else {		  
		return false;
	}
}

function isDeviceMotionSupported(){
	if (window.DeviceMotionEvent) {		
		return true;
	} else {		  
		return false;
	}
}

//This function is to check if the accelerometer is 
//accessible from the device
function isDeviceAccelerometerSupported()
{
	if(window.DeviceMotionEvent) 
		return true;
	else 
		return false;
}

//This function is to check if GPS is avaiable to the 
//browser
function isGeolocationSupported()
{
	if ("geolocation" in navigator) {		
		return true;
	} 
	else { 
		return false;
	}
}

//This function is to check if the media (Video/Audio)
//elements are accessible from the browser
function hasMediaSupport() {
	return !!(navigator.getUserMedia || navigator.webkitGetUserMedia ||navigator.mozGetUserMedia || navigator.msGetUserMedia);
}

//callback function on video success
var onVideoSuccess = function(stream) {	
    var videoelement = document.querySelector('video');
    videoelement.src = window.URL.createObjectURL(stream);
    videoelement = stream;
};

//This Function is to start the video stream to a 
//video element in the browser. YOu need to have two call back functions for this to
//activate if the media is supported(videoStream) and to fall back if it is not supported(onFailSoHard).
function startVideo(onFailure) {

	navigator.getUserMedia = (navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia);
	video = document.querySelector('video');
	if(video){
		if (typeof onFailure === "function") 				    
			navigator.getUserMedia({video: true, audio: true},onVideoSuccess , onFailure);
	} else
		log("There is not video element");
}

//This Function is for the location functions
var options = {
		  enableHighAccuracy: true,
		  timeout: 27000,
		  maximumAge: 30000
		};

function locationFindingerror(err) {
	  alert("An Error Occured"+ err.data);
};

//This function is to get the location and the pass the coordinates to a call back function.
//Name of the function is provided as the parameter to the function
function getLocation(callback){
	navigator.geolocation.getCurrentPosition(
		function (pos){
			if (typeof callback === "function") {				    
			        callback(pos);
			    }
		}, locationFindingerror, options);	
}
var registerForDeviceMovements = function(onLocationSuccess,onLocationError) {
	if (typeof onLocationSuccess === "function" && typeof onLocationError === "function") {
		id = navigator.geolocation.watchPosition(onLocationSuccess, onLocationError, options);
	};	
};

//This is for the accelerometer and there are three separate callback functions to handle aceleration
//acceleration with gravity and rotation.
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
		if(interval){
			document.getElementById("updateInterval").innerHTML = interval ;
		}
		
	}, true);
	

};

//This function is to handle the device orientation.
//Function needs more polishing
var reigsterDeviceOrentationEvent = function(eventHandlingFunction){
	if (window.DeviceOrientationEvent) {
	    window.addEventListener("deviceorientation", function( event ) {
			//alpha: rotation around z-axis
			var rotateDegrees = event.alpha;
			//gamma: left to right
			var leftToRight = event.gamma;
			//beta: front back motion
			var frontToBack = event.beta;
			var abs =event.absolute;
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

