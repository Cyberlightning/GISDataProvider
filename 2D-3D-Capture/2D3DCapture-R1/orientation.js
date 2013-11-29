var g = 1.1;
var img;
var x = 0;
var y = 0;
var img ;

function readImage() {
	
}

function updateYValue(angle){
	if(angle < 0 ){
		angle = angle*(-1);
		if(0 <angle <=30)
			y=y-1;
		else if(31 <angle <=60)
			y=y-5;
		else if(61 <angle <=90)
			y=y-10;
		if(y<0)
			y=0;
	} else if(angle >0){
		if(0 <angle <=30)
			y=y+1;
		else if(31 <angle <=60)
			y=y+5;
		else if(61 <angle <=90)
			y=y+10;
		if(y>840)
			y=840;
	}	
}

function updateXValue(angle){
	if(angle < 0 ){
		angle = angle*(-1);
		if(0 <angle <=30)
			x=x-1;
		else if(31 <angle <=60)
			x=x-5;
		else if(61 <angle <=90)
			x=x-10;
		if(x<0)
			x=0;
	} else if(angle >0){
		if(0 <angle <=30)
			x=x+1;
		else if(31 <angle <=60)
			x=x+5;
		else if(61 <angle <=90)
			x=x+10;
		if(x>480)
			x=480;
	}
}

//This observation is for LG Optimus 3D P920
//If gamma value is less than -25 --> right side up, left side down
//If Gamma is higher than 25 --> right side down and left side up
//in between 25 and -25 mobile is potrait narrow side up

var handleOrientationEvent = function( alpha, beta, gamma , orienation  ){
	document.getElementById("alpha").innerHTML = "<h1>Alpha -->"+alpha+"</h1>";
	document.getElementById("beta").innerHTML = "<h1>Beta -->"+beta+"</h1>";
	document.getElementById("gamma").innerHTML = "<h1>Gamma -->"+gamma+"</h1>";
	document.getElementById("orientation").innerHTML = "You are in " +orienation + " mode.";
	var c=document.getElementById("myCanvas");
	var ctx=c.getContext("2d");
	updateYValue(frontToBack);
	updateXValue(leftToRight);
	var imgEle;
	if(imgEle=document.getElementById("img"))
		document.removeChile(imgEle);
	img = document.createElement('img');	
	img.onload = function () {		
	};		
	img.src = "img/1.jpg";
	ctx.clearRect(0,0,c.width,c.height);
	ctx.drawImage(img,x,y,170,107);	
};

window.onload = function (){
	readImage();
	reigsterDeviceOrentationEvent(handleOrientationEvent);
	
};