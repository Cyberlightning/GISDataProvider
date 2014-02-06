
//FIware_wp13.Device = function(localurl, resturl, localport, restport, wsp) 
//var dAPI = new FIware_wp13.Device("dev.cyberlightning.com" ,"dev.cyberlightning.com", "9090", "17324","17323");
var dAPI;

function startVideoClicked() {
	var bt = dAPI.browser.getBrowserType();
	if(bt == "Firefox" && dAPI.browser.releaseVersion !="29.0")
		alert("Vedio Element is not suppoted by your version of Firefox");
	else
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
	dAPI.log("current location retrieved");
}

function onLocationServiceSearchError(){
}

function testCallback() {	
}

window.onload=function() {
	//dAPI = new FIware_wp13.Device("dev.cyberlightning.com","dev.cyberlightning.com", "9090" , "17325" ,"17323");
	dAPI = new FIware_wp13.Device("localhost","localhost", "8080" , "17325" ,"17322");
	dAPI.setupLogger();	
	if(dAPI.Type=="Desktop"){
		alert("This Demo is intended for  Mobile Devices");
		var bt = dAPI.browser.getBrowserType();
		console.log("Browser type "+bt);
		if(bt == "Firefox" && dAPI.browser.releaseVersion !="29.0")
			Alert("Vedio Element is not suppoted by your version of Firefox");		
	} else {
		dAPI.subscribe(onLocationSearchSuccess, onLocationServiceSearchError, handlacceleration, handleAccelerationWithGravityEvent, handleRotation, handleOrientationChanges);
	}
//	dAPI.showVideo(testCallback);
//	dAPI.getCurrentLocation(onLocationSearchSuccess123);
//	This location tracker needs two functions as parameters
//	dAPI.registerForDeviceMovements(onLocationSearchSuccess, onLocationServiceSearchError);
//	dAPI.registerDeviceMotionEvents(handlacceleration, handleAccelerationWithGravityEvent, handleRotation);
//	dAPI.reigsterDeviceOrentationEvent(handleOrientationChanges);
	
};
