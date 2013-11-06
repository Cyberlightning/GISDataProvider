//var moveable;
var slowthis = 1;
var pressedKeys = {};
var vec3 = XML3D.math.vec3;
var cam;
var loop = false;

var mouseButtonIsDown = false;
var oldMousePosition = {x:0,y:0};
var rotationSensivityMouse = 0.00125*slowthis;
var angleUp = 0;


function initMoveable(){
	var factory = new XMOT.ClientMotionFactory();

	//moveable
	// var cam = document.getElementById("NodeClassName");
	var cam = document.getElementById("node-camera_player");
	// cam = document.getElementById("camera-view");
	//console.log(cam);
	moveable = factory.createMoveable(cam, new XMOT.SimpleConstraint(true));
	moveable.setPosition([0, 0, 0]);
	moveable.setOrientation([0, 0, 0]);

	// setConstraints(3, 20);

	initEvents();
}

// ----------- keyboard controlls ------------
function initEvents(){
	console.log("initKeyboardEvents");

	var xml3dobject = document.getElementById("xml3dContent");
	
	window.addEventListener("keydown", keypressEventHandler, false);
    window.addEventListener("keyup", keyupEventHandler, false);
	xml3dobject.addEventListener("mousemove", mouseMovementHandler, false);
	xml3dobject.addEventListener("mousedown", mouseDownHandler, false);
	xml3dobject.addEventListener("mouseup", mouseUpHandler, false);
}


function moveBackAndForward(x){
	console.log("moveBackAndForward");
	var vecX = [0, 0, 1];
	var result = vec3.create();
	var normalizedOut = vec3.create();
	var translated = vec3.create();

	vec3.transformQuat(result, vecX, moveable.getOrientation());
	vec3.normalize(normalizedOut, result)
	console.log(normalizedOut);

	// vec3.scale(translated, normalizedOut, x);
	// console.log(result);

	// moveable.translate(translated);
	moveable.translate(vec3.scale(translated, normalizedOut, x));
	console.log(moveable.translate(vec3.scale(translated, normalizedOut, x)))
	console.log(moveable.getPosition());

}

function moveLeftAndRight(y){
	var vecY = [1, 0, 0]; // global x is local z of the camera
	var result = vec3.create();
	var normalizedOut = vec3.create();
	var translated = vec3.create();

	vec3.transformQuat(result, vecY, moveable.getOrientation());
	vec3.normalize(normalizedOut, result)
	// console.log(normalizedOut);

	vec3.scale(translated, normalizedOut, y);
	// console.log(result);

	moveable.translate(translated);
}

function moveUpAndDown(z){
	var vecZ = [0, 1, 0]; // global x is local z of the camera
	var result = vec3.create();
	var normalizedOut = vec3.create();
	var translated = vec3.create();

	// XML3D.math.quat.multiply(moveable.getOrientation(),vecY, result);
	// moveable.translate(vec3.scale(vec3.normalize(result), z));

	vec3.transformQuat(result, [1, 0, 0], moveable.getOrientation());
	vec3.normalize(normalizedOut, result)
	// console.log(normalizedOut);

	vec3.scale(translated, normalizedOut, z);
	// console.log(result);

	moveable.translate(translated);
}

function cameraUpAndDown(a){
	console.log("cameraUpAndDown");
	angleUp += a*Math.PI;
	moveable.rotate( XMOT.axisAngleToQuaternion( [1,0,0], a*Math.PI) );
}

function cameraLeftAndRight(a){
	console.log("cameraLeftAndRight");
	//rotate up/down befor rotating sidewards, this prevends from rolling
	moveable.rotate( XMOT.axisAngleToQuaternion( [1,0,0], -angleUp) );
	moveable.rotate( XMOT.axisAngleToQuaternion( [0,1,0], a*Math.PI) );
	//and rotate up/down again
	moveable.rotate( XMOT.axisAngleToQuaternion( [1,0,0], angleUp) );
}

function keyupEventHandler(e){
    e = window.event || e;
    var kc = e.keyCode;
    loop = false;
    delete pressedKeys[kc];
}

function updateKeyMovement(delta){
    for(var kc in pressedKeys){
        moveWithKey(kc * 1, delta / 20);
    }
    if (loop) {
    	setTimeout(updateKeyMovement, 10);
    }
}

function moveWithKey(kc, factor){
	var swadMultiplier = 15;
	// var swadMultiplier = 0.5;
	var arrowMultiplier = 0.001;
	// var arrowMultiplier = 0.01;
    factor = factor || 1;
    switch(kc){
        case 83 : moveBackAndForward(factor * swadMultiplier*slowthis); break; // s
        case 87 : moveBackAndForward(factor * -swadMultiplier*slowthis); break; // w
        case 65 : moveLeftAndRight(factor * -swadMultiplier*slowthis); break; // a
        case 68 : moveLeftAndRight(factor * swadMultiplier*slowthis); break; // d
        case 33 : moveUpAndDown(factor * swadMultiplier*slowthis); break; //page up
        case 34 : moveUpAndDown(factor * -swadMultiplier*slowthis); break; //page down
        case 38 : cameraUpAndDown(factor * arrowMultiplier*slowthis); break; // up Arrow
        case 40 : cameraUpAndDown(factor * -arrowMultiplier*slowthis); break; // down Arrow
        case 37 : cameraLeftAndRight(factor * arrowMultiplier*slowthis); break; // left Arrow
        case 39 : cameraLeftAndRight(factor * -arrowMultiplier*slowthis); break; // right Arrow
        default : return false; break;
    }
    return true;
}

function keypressEventHandler(e){
	e = window.event || e;
	console.log("key pressed: " + e.keyCode);
	var kc = e.keyCode;
    if(!pressedKeys[kc]){
        var flag = moveWithKey(kc);
        if(flag){
            pressedKeys[kc] = true;
            loop = true;
            updateKeyMovement(10);
        }
       if(flag){
       	e.preventDefault();
       } 
    }
}


function mouseMovementHandler(e){
	// console.log("mouseMovementHandler");
	//e.preventDefault();
	// console.log(moveable.getPosition());
	if(!mouseButtonIsDown) {
		return;
	}
	var currentX = e.pageX;
	var currentY = e.pageY;
	console.log("Current: x: " + currentX + " y: " + currentY + " - old x: " + oldMousePosition.x + " y: " + oldMousePosition.y);
	var x = currentX - oldMousePosition.x;
	var y = currentY - oldMousePosition.y;
	oldMousePosition.x = currentX;
	oldMousePosition.y = currentY;
	console.log("x: ["+ x + "] y:[" + y + "]");
	
	if(x != 0)
		cameraLeftAndRight(-rotationSensivityMouse*x);
	if(y != 0)
		cameraUpAndDown(-rotationSensivityMouse*y);
}

function mouseUpHandler(e){
	//e.preventDefault();
	console.log("mouseup" + e.button + ": " + e.pageX+", "+e.pageY);
	// e.stopPropagation();
	if(e.button == 2){
		mouseButtonIsDown = false;
	}
}

function mouseDownHandler(e){
	console.log("mousedown" + e.button);
	// e.stopPropagation();
	console.log(e.pageX+", "+e.pageY);
	//e.preventDefault();
	if(e.button == 2){
		// var camera_node = document.getElementById("t_node-camera_player");
  //       camera_node.setAttribute("translation", "389000 7488050 1676.4444580078125");
		mouseButtonIsDown = true;
		oldMousePosition.x = e.pageX;
		oldMousePosition.y = e.pageY;
	}
}