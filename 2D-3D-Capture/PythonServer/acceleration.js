var handleAccelerationEvent = function(acceleration) {
	document.getElementById("acc").innerHTML = "Boo Hoo This gets called";
	if(acceleration)	{
		document.getElementById("acc").innerHTML = "x-->"+acceleration.x+"::y-->"+acceleration.y+"::z-->"+acceleration.z ;
	};
};

var handleAccelerationWithGravityEvent = function(accelerationWithGravity) {
		document.getElementById("accwithGravity").innerHTML = "x-->"+accelerationWithGravity.x+"::y-->"+accelerationWithGravity.y+"::z-->"+accelerationWithGravity.z ;
};
	
var handleRotationEvent = function( rotation ) {
	if(rotation){
		document.getElementById("rotation").innerHTML = rotation.alpha ;
	}	
};

var registerForAccelerometerEvents = function(handlacceleration, handleAccelerationWithGravity, handleRotation){
	
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

window.onload = function(){
	registerForAccelerometerEvents(handleAccelerationEvent,handleAccelerationWithGravityEvent,handleRotationEvent);
};