function deviceOrientationSupported() {
	if (window.DeviceOrientationEvent) {		
		return true;
	} else {		  
		return false;
	}
}

function deviceAccelerometerSupported()
{
	if(window.DeviceMotionEvent) 
		return true;
	else 
		return false;
}

function suportForGeolocationAvailablity()
{
	if ("geolocation" in navigator) {		
		return true;
	} 
	else { 
		return false;
	}
}

var options = {
		  enableHighAccuracy: true,
		  timeout: 30000,
		  maximumAge: 0
		};

function locationFindingerror(err) {
	  alert(err);
};

function getLocation(callback){
	navigator.geolocation.getCurrentPosition(
		function (pos){
			if (typeof callback === "function") {				    
			        callback(pos.coords);
			    }
		}, locationFindingerror, options);	
}
var registerForAccelerometerEvents = function(handlacceleration, handleAccelerationWithGravity, handleRotation){
	alert("It starts too");
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
				eventHandlingFunction(frontToBack, leftToRight, rotateDegrees , abs , orienation );
		    }
	    }, true);
	}
};

