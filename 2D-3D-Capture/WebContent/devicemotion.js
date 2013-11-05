var handleAccelerationEvent = function(acc) {
	document.getElementById("acc").innerHTML = "Adjusted Sensor values x-->"+acc.x+"::y-->"+acc.y+"::z-->"+acc.z ;
	if(acc.x>0)
		document.getElementById("x").innerHTML="<h1>Moving EAST</h1>";
	else if(acc.x ==0)
		document.getElementById("x").innerHTML="<h1>Moving NEITHER EAST NOR WEST</h1>";
	else
		document.getElementById("x").innerHTML="<h1>Moving WEST</h1>";
	
	if(acc.y>0)
		document.getElementById("z").innerHTML="<h1>Moving UP</h1>";
	else if(acc.y ==0)
		document.getElementById("z").innerHTML="<h1>Moving NEITHER UP NOR DOWN</h1>";
	else
		document.getElementById("z").innerHTML="<h1>Moving DOWN</h1>";
	
	if(acc.z>0)
		document.getElementById("y").innerHTML="<h1>Moving NORTH</h1>";
	else if(acc.z ==0)
		document.getElementById("y").innerHTML="<h1>Moving NEITHER NORTH NOR SOUTH</h1>";
	else
		document.getElementById("y").innerHTML="<h1>Moving SOUTH</h1>";
};

var handleAccelerationWithGravityEvent = function(accelerationWithGravity) {
	document.getElementById("accwithGravity").innerHTML = " Adjusted Sensor values  gx-->"+accelerationWithGravity.x+"::gy-->"+accelerationWithGravity.y+"::gz-->"+accelerationWithGravity.z ;
	document.getElementById("LGZ").innerHTML = "";
	document.getElementById("LGX").innerHTML = "";
	document.getElementById("LGY").innerHTML = "";
	if(accelerationWithGravity.z > 8.0)
		document.getElementById("LGZ").innerHTML ="<h1>Phone screen facing up</h1>";
	else if (accelerationWithGravity.z < -8.0)
		document.getElementById("LGZ").innerHTML ="<h1>Phone screen facing down</h1>";
	
	if(accelerationWithGravity.y > 8.0)
		document.getElementById("LGY").innerHTML ="<h1>You might be holding the phone  Potrait mode</h1>";
	else if (accelerationWithGravity.y < -8.0)
		document.getElementById("LGY").innerHTML ="<h1>You might be holding the phone  Potrait mode, But top Down</h1>";
	
	if(accelerationWithGravity.x > 8.0)
		document.getElementById("LGX").innerHTML ="<h1>You might be holding the in Landscape , top of the phone facing facing fairly east</h1>";
	else if (accelerationWithGravity.x < -8.0)
		document.getElementById("LGX").innerHTML ="<h1>You might be holding the phone in Landscape , top of the phone facing facing fairly West</h1>";
};
	
var handleRotationEvent = function( rotation ) {
	if(rotation){
		document.getElementById("rotation").innerHTML = "Rotaion information is avaialble" ;
	}	
};

//var registerForAccelerometerEvents = function(handlacceleration, handleAccelerationWithGravity, handleRotation){
//	
//	window.addEventListener("devicemotion", function(event) {
//		var rotationRate = event.rotationRate;		
//		var acceleration = event.acceleration;
//		var accelerationWithGravity = event.accelerationIncludingGravity;
//		if(acceleration ) {
//			if (typeof handlacceleration === "function") {
//				handlacceleration(acceleration);
//			};
//		};
//		if(accelerationWithGravity){
//				if (typeof handleAccelerationWithGravity === "function")
//					handleAccelerationWithGravityEvent(accelerationWithGravity);			
//		};
//		if(rotationRate){
//			if (typeof handleRotation === "function")
//				handleRotation(rotationRate);
//		}
//		if(interval){
//			document.getElementById("updateInterval").innerHTML = interval ;
//		}
//		
//	}, true);
//	
//
//}; 

window.onload = function(){
	registerDeviceMotionEvents(handleAccelerationEvent,handleAccelerationWithGravityEvent,handleRotationEvent);
};