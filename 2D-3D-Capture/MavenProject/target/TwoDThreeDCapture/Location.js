//var options = {
//  enableHighAccuracy: true,
//  timeout: 30000,
//  maximumAge: 0
//};

var crd;
var latitude;
var latestLocation = new Boolean();

function locationRetrieved(pos) {
  crd = pos;
  initGoogleMaps(crd);
};

function getLocationData(){
	return crd;
};

function error(err) {
  alert(err);
};

var initGoogleMaps = function(pos) {	

		var centerPosition = new google.maps.LatLng(pos.coords.latitude, pos.coords.longitude);
		var mapTypeID =  google.maps.MapTypeId.ROADMAP;
        var mapOptions = {
          center: centerPosition,
          zoom: 8,
          mapTypeId: mapTypeID,
        };
        var map = new google.maps.Map(document.getElementById("mapcanvas"), mapOptions);        
        
        var circle = new google.maps.Circle({
            center: centerPosition,
            radius: pos.coords.accuracy,
            map: map,//your map,
            fillColor: '#FF0000',//color,
            fillOpacity: 0.5,//opacity from 0.0 to 1.0,
            strokeColor: '#FF0000',//stroke color,
            strokeOpacity: 0.5,//opacity from 0.0 to 1.0
        });
        new google.maps.Circle(circle);
        var marker = new google.maps.Marker({
     	   position: centerPosition,
     	   map: map, //your map,
     	   draggable:false,
     	   animation: google.maps.Animation.DROP,
     	});        
};

window.onload=function(){
	var dAPI = new FIware_wp13.Device("dev.cyberlightning.com","dev.cyberlightning.com", "9090" , "17321" ,"17322");
	//dAPI.registerForDeviceMovements(locationRetrieved, error);
	document.getElementById("browser").innerHTML = dAPI.browser.getBrowserType();
	document.getElementById("deviceOS").innerHTML = dAPI.getOS();
	if(dAPI.getDeviceType() == "Desktop"){
		alert("You are running the application on a Desktop. Our sensor part of the application will not work properly on a desktop. For a better result please use a mobile device equipped with accelerometer, compass and GPS sensor.");
	}
	document.getElementById("device").innerHTML = dAPI.getDeviceType();
	document.getElementById("uastring").innerHTML = dAPI.userAgentString;
	if(dAPI.browser.isGeolocationSupported()){
		document.getElementById("location").innerHTML = "Supported";
		dAPI.getCurrentLocation(locationRetrieved);
	}	else
		document.getElementById("location").innerHTML = "Not supported";
	if(dAPI.browser.isDeviceOrientationSupported()) {
		document.getElementById("orientationsupport").innerHTML = "Supported";
		if(dAPI.getDeviceType() == "Desktop"){
			document.getElementById("orientationsupport").innerHTML =document.getElementById("orientationsupport").innerHTML +" May not work on this desktop/Laptop.";
		}
	}	
	else
		document.getElementById("orientationsupport").innerHTML = "Not supported";
	if(dAPI.browser.isDeviceMotionSupported()) {
		document.getElementById("acclerationsupport").innerHTML = "Supported";
		if(dAPI.getDeviceType() == "Desktop"){
			document.getElementById("acclerationsupport").innerHTML =document.getElementById("acclerationsupport").innerHTML +" May not work on this desktop/Laptop.";
		}
	} else 
		document.getElementById("acclerationsupport").innerHTML = "Not Supported";	
};