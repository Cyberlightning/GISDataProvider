var localstream;
var localcanvas;
var localcontext;
var localImage;
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
	log(e);
  };
  
  var videoStream = function(stream) {	
	    var video = document.querySelector('video');
	    video.src = window.URL.createObjectURL(stream);
	    video = stream;
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
		  videoheight= video.videoHeight;
		  videowidth = video.videoWidth;
		  localcanvas.width = videowidth;
		  localcanvas.height = videoheight;	 	  
		  //image.onload = function(){
		  document.getElementById("snapshot").style.position="fixed";
		  document.getElementById("snapshot").style.top="10px";
		  document.getElementById("snapshot").style.left=(videowidth+150)+"px";
		  //localcontext.drawImage(image,0, 0);
		  localcontext.drawImage(video, 0, 0);	    	 
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
 
 var setupConnection = function() {
	 log("WebSockets supported");
	 connection = new WebSocket("ws://localhost:17000/");
	 connection.binaryType = "arraybuffer";
	 var handler = new ConnectionHandler;
	 connection.onopen = handler.onOpen;
	 connection.onmessage =	handler.onMessage;

	 connection.onclose = function()
	 { 
		 log("Connection is closed..."); 
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
	 var time=d.getFullYear()+d.getMonth()+d.getDate()+"_"+d.getHours()+d.getMinutes()+d.getSeconds();	 
	 var imagematadata={
		 type:"image",
		 time:time, 
			 ext:"png",};
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
	if(message)
	if(message =="FILENAME"){
		var imagedata = localcontext.getImageData(0, 0, videowidth,videoheight);
		var bytearray =  binaryCodeImage(imagedata);
		if(connection.readyState == 1) 
			connection.send(bytearray.buffer);
		else {
			log("Connecttion status is not OPEN -->"+connection.readyState);
		 }
	}
	
};

function videostart() {
	video = document.querySelector('video');
	localcanvas = document.querySelector('canvas');
	localImage = document.querySelector('img');
	localcontext = localcanvas.getContext('2d');
	navigator.getUserMedia = (navigator.getUserMedia || navigator.webkitGetUserMedia ||
	        navigator.mozGetUserMedia || navigator.msGetUserMedia);
	if (hasGetUserMedia()) 
	{
		navigator.getUserMedia({video: true, audio: true},videoStream , onFailSoHard);
	} else {
		onFailSoHard();
	}
}

function log(text){
	document.getElementById("consolelog").value = document.getElementById("consolelog").value + "\n"+text;
}