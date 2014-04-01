/**
 * 
 */

var websocket;
var subID;
//var remoteURL="localhost";
var remoteURL="dev.cyberlightning.com";
var remotePort="17321";
var wsport="17322";
var localURL = "localhost";
var localPORT = "8080";

var startWSServer = function() {
	var http = new XMLHttpRequest();
	//var url = "http://"+localURL+":"+localPORT+"/TwoDThreeDCapture/DesktopClientMultiplexer?command=get&url="+remoteURL+":"+remotePort+"/startwebsocketserver";
	var url = "http://"+remoteURL+":"+remotePort+"/startwebsocketserver";
	http.open("GET", url, true);
	http.onreadystatechange = function() {
	    if(http.readyState == 4 && http.status == 200) {
	        alert(http.responseText);
	    }
	};
	http.send();
	
};

var shutdownWSServer = function() {
	var http = new XMLHttpRequest();
	//var url = "http://"+localURL+":"+localPORT+"/TwoDThreeDCapture/DesktopClientMultiplexer?command=get&url="+localURL+":"+remotePort+"/closewebsocketserver";
	var url = "http://"+remoteURL+":"+remotePort+"/closewebsocketserver";
	http.open("GET", url, true);
	http.onreadystatechange = function() {
	    if(http.readyState == 4 && http.status == 200) {
	        alert(http.responseText);
	    }
	};
	http.send();
};

var init = function (){
	if('WebSocket' in window) {
		websocket = new WebSocket('ws://'+remoteURL+':'+wsport);		
		websocket.onopen =onSocketOpen ;
		websocket.onmessage= onMessageArrive;
		websocket.onerror = onWebSocketError;
		websocket.onclose = onSocketClose;
	} else  {
		console.log("Web Socket not supported");
	}
};

var onSocketOpen =  function() {
//	var subscriptionMessage = { "type":"subscribe" , "data" : {	'type' : "Local-Directional","data" : {"lon" :"2.1","lat":"1.2", "roll":"0.0", "pitch": "0.0", "yaw": "0.0"}}};
//	websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
};

var onMessageArrive = function(event) {
	alert("Message arrived");
	var svrmessage=window.atob(event.data);
	console.log(svrmessage);
	if(svrmessage)
		svrmessage= svrmessage.trim();
	if(svrmessage=='READY') {
		console.log("Server Ready");
	} else {
		var temp = JSON.stringify(eval("(" + svrmessage + ")"));
		var JSONObj = JSON.parse(temp);
		var message_type = JSONObj['type'];
		if(message_type) {
			switch(message_type){
				case 1 :
					subRes = JSONObj["subscribe"];
					if(subRes=="True") {
						subID = JSONObj["sub_id"];
						console.log(subRes+":"+subID);						
					} else 
						console.log("ERROR");
					break;
				case 2 :
					console.log("Unsuscribed");
					break;
				case 3:
					console.log("Event recieved");					
					break;
			}				
		}
	}	
};

var onWebSocketError = function (error) {
	console.log("Error Occured");
};

var onSocketClose = function() {
	console.log("Conenction Closed");
};

var subscribeAny = function() {
	if(!subID) {
		var subscriptionMessage = { "type":"subscribe" , "data" : {	'type' : "Any"}};
		websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
	}
};

var subscribeLocal= function() {
	if(!subID) {
		var subscriptionMessage = { "type":"subscribe" , "data" : {	'type' : "Local","data" : {"lon" :"2.1","lat":"1.2", "roll":"0.0", "pitch": "0.0", "yaw": "0.0"}}};
		websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
	}
	else 
		alert("subscription exist");
}; 

subscribeDirectional= function() {
	if(!subID) {
		var subscriptionMessage = { "type":"subscribe" , "data" : {	'type' : "Directional","data" : {"lon" :"2.1","lat":"1.2", "roll":"0.0", "pitch": "0.0", "yaw": "0.0"}}};
		websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
	} else 
		alert("subscription exist");
};

var sendAllAnEvent = function() {
	var subscriptionMessage = { "type":"test" , "data" : {	'type' : "publish" }};
	websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
};

var sendMeAnEvent = function() {
	var subscriptionMessage = { "type":"test" , "data" : {	'type' : "single", 'sub_id' : subID }};
	websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
};

var unsubscribe = function() {
	if (subID) {
		var subscriptionMessage = { "type":"unsubscribe" , "sub_id" : subID };
		websocket.send("2<---->"+JSON.stringify(subscriptionMessage));
	} else 
		console.log("No subscription found");
	subID = null;
};