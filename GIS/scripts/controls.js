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
	// var cam = document.getElementById("node-camera_player");
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
	
	window.addEventListener("keydown", keypressEventHandler, false);
    window.addEventListener("keyup", keyupEventHandler, false);
	window.addEventListener("mousemove", mouseMovementHandler, false);
	window.addEventListener("mousedown", mouseDownHandler, false);
	window.addEventListener("mouseup", mouseUpHandler, false);
}


function moveBackAndForward(x){
	var vecX = [0, 0, 1];
	var result = vec3.create();
	var normalizedOut = vec3.create();
	var translated = vec3.create();

	vec3.transformQuat(result, vecX, moveable.getOrientation());
	vec3.normalize(normalizedOut, result)
	// console.log(normalizedOut);

	vec3.scale(translated, normalizedOut, x);
	// console.log(result);

	moveable.translate(translated);
	
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

	vec3.transformQuat(result, vecZ, moveable.getOrientation());
	vec3.normalize(normalizedOut, result)
	// console.log(normalizedOut);

	vec3.scale(translated, normalizedOut, z);
	// console.log(result);

	moveable.translate(translated);
}

function cameraUpAndDown(a){
	angleUp += a*Math.PI;
	moveable.rotate( XMOT.axisAngleToQuaternion( [1,0,0], a*Math.PI) );
}

function cameraLeftAndRight(a){
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
    factor = factor || 1;
    switch(kc){
        case 83 : moveBackAndForward(factor * 0.5*slowthis); break; // s
        case 87 : moveBackAndForward(factor * -0.5*slowthis); break; // w
        case 65 : moveLeftAndRight(factor * -0.5*slowthis); break; // a
        case 68 : moveLeftAndRight(factor * 0.5*slowthis); break; // d
        case 33 : moveUpAndDown(factor * 0.5*slowthis); break; //page up
        case 34 : moveUpAndDown(factor * -0.5*slowthis); break; //page down
        case 38 : cameraUpAndDown(factor * 0.01*slowthis); break; // up Arrow
        case 40 : cameraUpAndDown(factor * -0.01*slowthis); break; // down Arrow
        case 37 : cameraLeftAndRight(factor * 0.01*slowthis); break; // left Arrow
        case 39 : cameraLeftAndRight(factor * -0.01*slowthis); break; // right Arrow
        default : return false; break;
    }
    return true;
}

function keypressEventHandler(e){
	e = window.event || e;
	// console.log("key pressed!");
	// console.log(e);
	var kc = e.keyCode;
    if(!pressedKeys[kc]){
        var flag = moveWithKey(kc);
        if(flag){
            pressedKeys[kc] = true;
            loop = true;
            updateKeyMovement(10);
        }
    }

}


function mouseMovementHandler(e){
	//e.preventDefault();
	if(!mouseButtonIsDown) {
		return;
	}
	var currentX = e.pageX;
	var currentY = e.pageY;
	var x = currentX - oldMousePosition.x;
	var y = currentY - oldMousePosition.y;
	oldMousePosition.x = currentX;
	oldMousePosition.y = currentY;
	//console.log("Current: x: " + currentX + " y: " + currentY + " - move x: " + x + " y: " + y);
	if(x != 0)
		cameraLeftAndRight(-rotationSensivityMouse*x);
	if(y != 0)
		cameraUpAndDown(-rotationSensivityMouse*y);
}

function mouseUpHandler(e){
	//e.preventDefault();
	if(e.button == 2){
		mouseButtonIsDown = false;
	}
}

function mouseDownHandler(e){
	//e.preventDefault();
	if(e.button == 2){
		mouseButtonIsDown = true;
		oldMousePosition.x = e.pageX;
		oldMousePosition.y = e.pageY;
	}
}