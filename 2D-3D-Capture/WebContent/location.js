//var options = {
//  enableHighAccuracy: true,
//  timeout: 30000,
//  maximumAge: 0
//};

var crd;
var latitude;
var latestLocation = new Boolean();

function locationRetrieved(pos) {
  crd = pos.coords;
  initGoogleMaps(crd);
};

function getLocationData(){
	return crd;
};

function error(err) {
  alert(err);
};

function initGoogleMaps(pos) {	
		var centerPosition = new google.maps.LatLng(pos.latitude, pos.longitude);
		var mapTypeID =  google.maps.MapTypeId.TERRAIN;
        var mapOptions = {
          center: centerPosition,
          zoom: 10,
          mapTypeId: mapTypeID,
        };
        var map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);        
        
        var circle = new google.maps.Circle({
            center: centerPosition,
            radius: pos.accuracy,
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
        
}
      
 function readSystemInfo(){
	 document.getElementById("browser").innerHTML = navigator.platform;
 }

window.onload=function(){
	readSystemInfo();
	if(isGeolocationSupported()){
		document.getElementById("location").innerHTML = "Supported";		
		getLocation(initGoogleMaps);
	}	else
		document.getElementById("location").innerHTML = "Not supported";
	if(isDeviceOrientationSupported()) {
		document.getElementById("orientation").innerHTML = "supported.<a href=\'orientationTest.html\'>See Demo</a>";
	}	
	else
		document.getElementById("orientation").innerHTML = "Not supported";
	if(isDeviceMotionSupported()) {
		document.getElementById("accleration").innerHTML = "Supported. <a href=\'accelerometer.html\'>See Demo</a>";
	} else 
		document.getElementById("accleration").innerHTML = "Not Supported";	
};