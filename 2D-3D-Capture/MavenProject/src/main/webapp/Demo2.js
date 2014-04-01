var dAPI;

function startVideoClicked() {
	var bt = dAPI.browser.getBrowserType();
	if(dAPI.Type =="Desktop" && bt == "Firefox" && dAPI.browser.releaseVersion !="29.0")
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

function handleOrientationChanges(alpha, gamma, beta ){
	document.getElementById("Orientation").innerHTML ="<"+alpha+"><"+beta+"><"+gamma+">";
}

function handlacceleration(a){
	if(a)
		document.getElementById("Acceleration").innerHTML ="<"+a.x+">:::<"+a.y+">:::<"+a.z+"></";
	else
		document.getElementById("Acceleration").innerHTML = "<h3>Not Supported</h3>" ;
}

var handleAccelerationWithGravityEvent = function(accelerationWithGravity) {
	if(accelerationWithGravity)
		document.getElementById("Accelerationwg").innerHTML = "<"+accelerationWithGravity.x+"><br><"+accelerationWithGravity.y+"><"+accelerationWithGravity.z+">" ;
	else
		document.getElementById("Accelerationwg").innerHTML = "<h3>Not Supported</h3>" ;
};

function handleRotation(r) {
	if(r)
		document.getElementById("Rotation").innerHTML = ("DeviceRotation-->"+r.alpha+":"+r.beta+":"+r.gamma);
	else
		document.getElementById("Rotation").innerHTML ="Not Supported";
}

function onLocationSearchSuccess(pos,coords){
	if(coords)
		document.getElementById("Geolocation").innerHTML='<p>Latitude is ' + coords.latitude + '° <br>Longitude is ' + coords.longitude + '°</p>';
}

function onLocationSearchSuccess123(pos){
	document.getElementById("Velocity").innerHTML=("current location retrieved");
}

function onLocationServiceSearchError(){
}

function testCallback() {	
}

window.onload=function() {
	dAPI = new FIware_wp13.Device("dev.cyberlightning.com","dev.cyberlightning.com", "9090" , "17322" ,"17321");
		
	if(dAPI.Type=="Desktop"){
		alert("This Demo is intended for  Mobile Devices");
		var bt = dAPI.browser.getBrowserType();
		console.log("Browser type "+bt);
		if(bt == "Firefox" && dAPI.browser.releaseVersion !="29.0")
			Alert("Vedio Element is not suppoted by your version of Firefox");		
	} else {
		dAPI.subscribe(onLocationSearchSuccess, onLocationServiceSearchError, handlacceleration, handleAccelerationWithGravityEvent, handleRotation, handleOrientationChanges);
	}	
	window.addEventListener('devicelight', function(event) {	
		if(event)
			document.getElementById("Ambient").innerHTML= event.value ;
		else 
			document.getElementById("Ambient").innerHTML="Not Supported" ;
			
	});
	
	window.addEventListener('deviceorientation', function(e) {		
		if(e)
			{
				var c = e.compassHeading || e.webkitCompassHeading || 0;
				document.getElementById("MozOrientation").innerHTML= "ORIANTATION ::<"+e.alpha+">::<"+e.beta+">::<"+e.gamma+">::--><"+c+"" ;
				
			} else 
			document.getElementById("MozOrientation").innerHTML= "Not Supported" ;
	}, true);
	
};
