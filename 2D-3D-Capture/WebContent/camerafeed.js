var localstream;
var localcanvas;
var localcontext;
var localImage;
var video;


function hasGetUserMedia() {
	return !!(navigator.getUserMedia || navigator.webkitGetUserMedia ||
            navigator.mozGetUserMedia || navigator.msGetUserMedia);
}

var onFailSoHard = function(e) {
	alert(e);
  };
  
  var videoStream = function(stream) {	
	    var video = document.querySelector('video');
	    video.src = window.URL.createObjectURL(stream);
	    video = stream;
//	    video.onloadedmetadata = function(e) {
//	      // Ready to go. Do some stuff.
//	    };
  };
  
  var snapshot = function() {
//	  if(video){
//		  var h= video.videoHeight;
//		  var w = video.videoWidth;
//		  alert(w );
//		  alert(h );
//		  localcanvas.width = w;
//		  localcanvas.height = h;}
	  var image = new Image();
	  image.onload = function(){
		  localcanvas.width = 640;
		  localcanvas.height = 427;
		  localcontext.drawImage(image,0, 0);
      };
      image.src = "img/1.jpg";      
            
	  //localcontext.drawImage(video, 0, 0);
	  //input.value = localcanvas.toDataURL('image/jpeg');
	  if ("WebSocket" in window)
	  {
	     var ws = new WebSocket("ws://localhost:17000/");	     
	     ws.onopen = function()
	     {
	    	 //Sending text works
	    	 //ws.send("This is a test message from client");
	    	 ws.binaryType = "arraybuffer";
	    	 var imagedata = localcontext.getImageData(0, 0, image.width,image.height);
	    	 document.getElementById('imagedata').value = imagedata.data;
	    	 var canvaspixelarray = imagedata.data;	    	    
	    	    var canvaspixellen = canvaspixelarray.length;
	    	    var bytearray = new Uint8Array(canvaspixellen);	    	    
	    	    for (var i=0;i<canvaspixellen;++i) {
	    	        bytearray[i] = canvaspixelarray[i];
	    	        
	    	    }
	    	    for(var i=0;i<100;++i) {
	    	    	document.getElementById('bufferdata').value = document.getElementById('bufferdata').value+bytearray[i];
	    	    }
	    	    ws.send(bytearray.buffer);
	    	    alert("Message is sent...");
	     };
	     ws.onmessage = function (evt) 
	     {
	    	 alert("Message is received...");
	    	 var textarea = document.createElement('textarea');
	    	 document.body.appendChild(textarea);	    	 
	    	 var received_msg = evt.data;
	    	 textarea.value = received_msg;
	        
	     };
	     ws.onclose = function()
	     { 
	        // websocket is closed.
	        alert("Connection is closed..."); 
	     };
	     ws.onerror =  function  ( fault )  { 
	       console . log ( 'WebSocket Error'  + errors ); 
	     };
	  }
	  else
	  {
	     // The browser doesn't support WebSocket
	     alert("WebSocket NOT supported by your Browser!");
	  }

//		  form.appendChild(input);
//		  var imageformdata = new FormData();
//		  var filename="image";
//		  var now = new Date();
//		  filename = filename+"_"+now.getFullYear()+ now.getMonth() +now.getDate()+"_"+now.getHours()+now.getMinutes()+now.getSeconds()+ ".jpg";
//		  form.appendChild(input);
//		  document.body.appendChild(form);
		  //form.submit();
//		  imageformdata.append("FILE_NAME", filename);		  
//		  imageformdata.append(filename, image);
//		  var oReq = new XMLHttpRequest();
//		  oReq.onload = function(oEvent) {
//			  alert('Got a response');
//			  if (oReq.status == 200) {
//				  alert('This works');
//			      document.getElementById('response').innerHTML = "Uploaded!";
//			  } else {
//				  alert(' file not uploaded');
//				  document.getElementById('response').innerHTML = "not Uploaded!";
//			  }
//		  };
//		  oReq.open("POST", "http://localhost:18000/");
//		  oReq.send(imageformdata);		  
//};
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