
//FIware_wp13.Device = function(localurl, resturl, localport, restport, wsp) 
var dAPI = new FIware_wp13.Device("dev.cyberlightning.com" ,"dev.cyberlightning.com", "9090", "17324","17323");
//var dAPI = new FIware_wp13.Device("localhost","localhost", "8080" , "17324" ,"17323");

function startVideoClicked() {	
	dAPI.showVideo(videoCallback);	
}

function videoCallback(videoStream) {
	dAPI.log("Video Works");
}

function uploadSnapshotClicked(){
	dAPI.snapshot();
	dAPI.sendImage();
}

function upladlImageWithPost(){
	dAPI.snapshot();
	dAPI.postImage();
}

function handleOrientationChanges(alpha, gamma, beta , orientation){
	document.getElementById("orientation").innerHTML ="<h1><"+alpha+"><"+beta+"><"+gamma+"></h1>";
}

function handlacceleration(a){
}

var handleAccelerationWithGravityEvent = function(accelerationWithGravity) {
	document.getElementById("acceleration").innerHTML = "<h1><"+accelerationWithGravity.x+"><"+accelerationWithGravity.y+"><"+accelerationWithGravity.z+"></h1>" ;
};

function handleRotation(r) {
	dAPI.log("DeviceRotation-->"+r.alpha+":"+r.beta+":"+r.gamma);
}

function onLocationSearchSuccess(pos,coords){
}

function onLocationSearchSuccess123(pos){
}

function onLocationServiceSearchError(){
}

function testCallback() {	
}

window.onload=function() {
	var list = dAPI.getSensorList();
	dAPI.setupLogger();
//	dAPI.showVideo(testCallback);
//	dAPI.getCurrentLocation(onLocationSearchSuccess123);
//	This location tracker needs two functions as parameters
//	dAPI.registerForDeviceMovements(onLocationSearchSuccess, onLocationServiceSearchError);
//	dAPI.registerDeviceMotionEvents(handlacceleration, handleAccelerationWithGravityEvent, handleRotation);
//	dAPI.reigsterDeviceOrentationEvent(handleOrientationChanges);
	dAPI.subscribe(onLocationSearchSuccess, onLocationServiceSearchError, handlacceleration, handleAccelerationWithGravityEvent, handleRotation, handleOrientationChanges)
};
