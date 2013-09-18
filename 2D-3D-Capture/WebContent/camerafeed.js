var localstream;
var localcanvas;
var localcontext;
var video;
var videowidth;
var videoheight;
var connection;
var image;


function hasGetUserMedia() {
	return !!(navigator.getUserMedia || navigator.webkitGetUserMedia ||
            navigator.mozGetUserMedia || navigator.msGetUserMedia);
}

var onFailSoHard = function(e) {
	log("Video not supported");
};
  
 var videoStream = function(stream) {	
	    var videoelement = document.querySelector('video');
	    videoelement.src = window.URL.createObjectURL(stream);
	    videoelement = stream;
//	    video.onloadedmetadata = function(e) {
//	      // Ready to go. Do some stuff.
//	    };
  };
  
  var binaryCodeImage = function(imagedata){
	  var canvaspixelarray = imagedata.data;	    	    
	    var canvaspixellen = canvaspixelarray.length;
	    var bytearray = new Uint8Array(canvaspixellen);	    	    
	    for (var i=0;i<canvaspixellen;++i) {
	        bytearray[i] = canvaspixelarray[i];
	        
	    }
	    return bytearray;
  };
  
 var send = function(message,type) {
	 
	 
 };
 
 var setConnection = function(){
	 connection = new WebSocket("ws://dev.cyberlightning.com:17000/");	 
 };
 
 
  
 var snapshot = function() {
	 if(video){
		 localcanvas = document.createElement("canvas");
		 localcanvas.id = "snapshot";
		 localcontext = localcanvas.getContext('2d');
		 videoheight= video.videoHeight;
		 videowidth = video.videoWidth;
		 localcanvas.width = videowidth;
		 localcanvas.height = videoheight;	 	  
		 //image.onload = function(){
		 localcanvas.style.position="fixed";
		 localcanvas.style.top="10px";
		 localcanvas.style.left=(videowidth+400+20)+"px";		 
		 localcontext.drawImage(video, 0, 0);
		 document.body.appendChild(localcanvas);
		 if ("WebSocket" in window)
	   	 {
			 setupConnection();     
	   	 } else {
	   		 log("WebSocket NOT supported by your Browser!");		   	     
	   	 }
	 };            
	  //localcontext.drawImage(video, 0, 0);
	  //input.value = localcanvas.toDataURL('image/jpeg');
	  
  };
  
 var generateJPEG = function(){	 
 };
 
 var setupConnection = function() {
	 log("WebSockets supported");
	 //connection = new WebSocket("ws://localhost:17000/");
	 connection = new WebSocket("ws://dev.cyberlightning.com:17000/");
	 connection.binaryType = "arraybuffer";
	 var handler = new ConnectionHandler;
	 connection.onopen = handler.onOpen;
	 connection.onmessage =	handler.onMessage;
	 connection.onclose = function()
	 { 
		 log("Connection is closed...");
		 localcontext.clearRect(0, 0, localcanvas.width, localcanvas.height);
		 document.body.removeChild(localcanvas);
	 };
	 connection.onerror =  function  ( fault )  { 
		 log('WebSocket Error'  + errors ); 
	 };	
 }; 
  
 var ConnectionHandler = function () {	 
 };

 ConnectionHandler.prototype.onOpen= function() {
	 log("Connection opened-->"+connection.readyState);
	 var d = new Date();
	 var time=d.getFullYear()+"."+d.getMonth()+"."+d.getUTCDay()+"_"+d.getHours()+"."+d.getMinutes()+"."+d.getSeconds();
	 //alert(time); 
	 var imagematadata={
		 type:"image",
		 time:time, 
			 ext:"jpg",};
	 if(connection) {
			 connection.send(JSON.stringify(imagematadata));			 
	 } else {
			 log("Connection is null");
	 }
};

ConnectionHandler.prototype.onMessage = function (event) {
	log("message arrived");
	message=window.atob(event.data);	
	log(message);
	if(message =="FILENAME"){
		//FOLLOWING 2 LINE OF CODE CONVERTS THE IMAGEDATA TO BINARY
		//var imagedata = localcontext.getImageData(0, 0, videowidth,videoheight);
		//var bytearray =  binaryCodeImage(url);
		//THIS PART TRIES TO EXTRACT DATA FROM THE CANVAS TO CREATE AN JPEG IMAGE
		 var url = localcanvas.toDataURL("image/jpeg");
		 log(url.length);
		 log(url);
		//var bytearray =  binaryCodeImage(url);
		if(connection.readyState == 1) 
			connection.send(url);
		else {
			log("Connecttion status is not OPEN -->"+connection.readyState);
		 }
	}	
};

function videostart() {

	navigator.getUserMedia = (navigator.getUserMedia || navigator.webkitGetUserMedia ||
	        navigator.mozGetUserMedia || navigator.msGetUserMedia);
	if (hasGetUserMedia()) 
	{
		video = document.querySelector('video');
		navigator.getUserMedia({video: true, audio: true},videoStream , onFailSoHard);
	} else {
		onFailSoHard();
	}
}

function log(text){
	document.getElementById("consolelog").value = document.getElementById("consolelog").value + "\n"+text;
}

function clearlogs(){
	document.getElementById("consolelog").value = "";
}

