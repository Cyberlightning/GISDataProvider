const IMAGE_MARKERS_PATH = 'imageMarkers';

function Alvar(width, height){
	// Size
	this.width = width;
	this.height = height;
	
	// In case of reinitialization?
	if(this.imageBuffer)
		Module._free(this.imageBuffer);
	// Memory for shared memory space
	var channels = 4;
	this.imageBuffer = Module._malloc(width*height*channels*Uint8Array.BYTES_PER_ELEMENT);

	// Init Alvar
	var projection = Module.ccall(
			'init', // Function 
			'string', // Return value
			// Parameters
			['number', 		'number', 		'number'], 
			[this.width, 	this.height, 	this.imageBuffer]);
	
	// Set projection matrix
	this.projectionMatrix = JSON.parse(projection);

	this.imageMarkers = [];
	this.basicMarkers = [];
}
Alvar.prototype.setMarkers = function(basicMarkers, imageMarkers, allowedImageMarkerErrors){
	// Add markers that are not already added
	for(var i=0; i<basicMarkers.length; i++){
		var id = basicMarkers[i];
		if(this.basicMarkers.indexOf(id) == -1){
			this.basicMarkers.push(id);
			this.addBasicMarker(id);
		}
	}

	for(var i=0; i<imageMarkers.length; i++){
		var id = imageMarkers[i];
		if(this.imageMarkers.indexOf(id) == -1){
			this.imageMarkers.push(id);
			var allowedErrors = (allowedImageMarkerErrors && allowedImageMarkerErrors[i]) ? allowedImageMarkerErrors[i] : -1;
			this.addImageMarker(id, allowedErrors);

		}
	}
}

Alvar.prototype.addBasicMarker = function(id){
	Module.ccall(
			'add_basic_marker', // Function 
			null, // Return value
			// Parameters
			['number'], 
			[id]);
}
Alvar.prototype.addImageMarker = function(id, allowedErrors){
	var image = new Image();
	// Using separate function in order to sustain Alvar reference, loop index and marker ID
	image.onload = function(){
		var canvas = document.createElement('canvas');
		canvas.width = this.width;
		canvas.height = this.height;
		// Copy the image contents to the canvas
		var ctx = canvas.getContext('2d');
		ctx.drawImage(this, 0, 0);
		// Get image data
		var imageData = ctx.getImageData(0, 0, this.width, this.height);
		imageData = new Uint8Array(imageData.data);    
		var imageMarkerBuffer = Module._malloc(imageData.length*imageData.BYTES_PER_ELEMENT);
		Module.HEAPU8.set(imageData, imageMarkerBuffer);
		
		// Call Alvar
		Module.ccall(
				'add_image_marker', // Function 
				null, // Return value
				// Parameters
				['number',		'number',		'number',			'number',	'number'], 
				[this.width,	this.height,	imageMarkerBuffer,	id,			allowedErrors]);
		
		// Gives error: Uncaught abort() at Error
		Module._free(imageMarkerBuffer);
		console.log('Loading image for ID ' + id + ' was successful');
	};
	image.onabort = function(event){
		console.log('Loading image for ID ' + id + ' was aborted');
	};
	image.onerror = function(event){
		console.log('Error while loading image for ID ' + id);
	};

	var filename = IMAGE_MARKERS_PATH + '/' + id + '.png';

	image.src = filename;
};
Alvar.prototype.getProjectionMatrix = function() {
	return this.projectionMatrix;
};
Alvar.prototype.detectMarkers = function(imageData) {
	// Set image data
	var arr = new Uint8Array(imageData.data);
	Module.HEAPU8.set(arr, this.imageBuffer);
	// Call Alvar
	var markers = Module.ccall('process_image', 'string');
	// Get markers
	this.markersJson = JSON.parse(markers);
	// Return the number of detected markers
	var length = (this.markersJson) ? this.markersJson.length : 0;
	
	return length;
};
Alvar.prototype.getMarker = function(index) {
	return this.markersJson[index];
};
Alvar.prototype.areImageMarkersReady = function(index) {
	// Check if any of the markers isn't ready
	var ready = (this.imageMarkerStates.indexOf(false) == -1) ? true : false; 
	return ready;
};


var alvar = null;


Xflow.registerOperator('alvar-mobile-xflow.detect', {
    outputs: [ {type: 'float4x4', name : 'basicMarkerTransforms', customAlloc: true},
               {type: 'float4x4', name : 'imageMarkerTransforms', customAlloc: true},
               {type: 'bool', name: 'basicMarkerVisibilities', customAlloc: true},
               {type: 'bool', name: 'imageMarkerVisibilities', customAlloc: true},
               {type: 'float4x4', name : 'perspective', customAlloc: true}
             ],
    params:  [ {type: 'texture', source : 'imageData', optional: true},
               {type: 'int', source: 'basicMarkers', optional: true},
               {type: 'int', source: 'imageMarkers', optional: true},
               {type: 'int', source: 'allowedImageMarkerErrors', optional: true},
               {type: 'bool', source: 'flip', optional: true}
             ],
    alloc: function(sizes, imageData, basicMarkers, imageMarkers) {
        var basicMarkerLength = basicMarkers.length;
        var imageMarkerLength = imageMarkers.length;
        sizes['basicMarkerTransforms'] = basicMarkerLength;
        sizes['imageMarkerTransforms'] = imageMarkerLength;
        sizes['basicMarkerVisibilities'] = basicMarkerLength;
        sizes['imageMarkerVisibilities'] = imageMarkerLength;
        sizes['perspective'] = 1;
    },
    evaluate: function(basicMarkerTransforms, imageMarkerTransforms, basicMarkerVisibilities, imageMarkerVisibilities, 
    		perspective, imageData, basicMarkers, imageMarkers, allowedImageMarkerErrors, flip) {
    	// Initialize projection matrix to default values
    	for (var i = 0; i < 16; ++i) {
    		perspective[0] = 1; perspective[1] = 0; perspective[2] = 0; perspective[3] = 0;
    		perspective[4] = 0; perspective[5] = 1; perspective[6] = 0; perspective[7] = 0;
    		perspective[8] = 0; perspective[9] = 0; perspective[10] = 1; perspective[11] = 0;
    		perspective[12] = 0; perspective[13] = 0; perspective[14] = 0; perspective[15] = 1;
    	}
    	// Initialize visibilities and transforms to default values
    	for (var i = 0; i < basicMarkerTransforms.length; ++i) {
    		basicMarkerVisibilities[i] = false;
    		var mi = 16*i;
    		basicMarkerTransforms[mi+0] = 1; basicMarkerTransforms[mi+1] = 0; basicMarkerTransforms[mi+2] = 0; basicMarkerTransforms[mi+3] = 0;
    		basicMarkerTransforms[mi+4] = 0; basicMarkerTransforms[mi+5] = 1; basicMarkerTransforms[mi+6] = 0; basicMarkerTransforms[mi+7] = 0;
    		basicMarkerTransforms[mi+8] = 0; basicMarkerTransforms[mi+9] = 0; basicMarkerTransforms[mi+10] = 1; basicMarkerTransforms[mi+11] = 0;
    		basicMarkerTransforms[mi+12] = 0; basicMarkerTransforms[mi+13] = 0; basicMarkerTransforms[mi+14] = 0; basicMarkerTransforms[mi+15] = 1;
    	}
    	for (var i = 0; i < imageMarkerTransforms.length; ++i) {
    		imageMarkerVisibilities[i] = false;
    		var mi = 16*i;
    		imageMarkerTransforms[mi+0] = 1; imageMarkerTransforms[mi+1] = 0; imageMarkerTransforms[mi+2] = 0; imageMarkerTransforms[mi+3] = 0;
    		imageMarkerTransforms[mi+4] = 0; imageMarkerTransforms[mi+5] = 1; imageMarkerTransforms[mi+6] = 0; imageMarkerTransforms[mi+7] = 0;
    		imageMarkerTransforms[mi+8] = 0; imageMarkerTransforms[mi+9] = 0; imageMarkerTransforms[mi+10] = 1; imageMarkerTransforms[mi+11] = 0;
    		imageMarkerTransforms[mi+12] = 0; imageMarkerTransforms[mi+13] = 0; imageMarkerTransforms[mi+14] = 0; imageMarkerTransforms[mi+15] = 1;
    	}
    	
    	// Skip marker detection if image data is not ready
    	if(!imageData || !imageData.data || imageData.length == 0)
    		return;
    	
    	// Initialize Alvar if not done already
    	if(!alvar){
    		alvar = new Alvar(imageData.width, imageData.height);
    	}
    	
    	// Set markers
    	alvar.setMarkers(basicMarkers, imageMarkers, allowedImageMarkerErrors);
    	
    	// Set projection matrix
    	var projectionMatrix = alvar.getProjectionMatrix();
    	for (var i = 0; i < 16; ++i) {
    		perspective[i] = projectionMatrix[i];
    	}

        // Detect markers from frame
        var detected = alvar.detectMarkers(imageData);

        // Loop all detected markers
        for (var i = 0; i < detected; i++) {
        	// Get marker
        	var marker = alvar.getMarker(i);
        	
        	// Initialize some array references
        	if(marker.type == 'image'){
        		var transforms = imageMarkerTransforms;
        		var markers = imageMarkers;
        		var visibilities = imageMarkerVisibilities;
        	}
        	else{
        		var transforms = basicMarkerTransforms;
        		var markers = basicMarkers;
        		var visibilities = basicMarkerVisibilities;
        	}

        	// Loop markers add set values using references
        	var markerIndex = 0;
        	for (; markerIndex < markers.length; markerIndex++) {
        		if (markers[markerIndex] == marker.id) {
        			visibilities[markerIndex] = true;
        			break;
        		}
        	}
        	
            // Get the transform matrix for the marker
            var t = marker.transform;

            var mOffset = 16*markerIndex;

            if (flip && flip[0]) {
                // webcam (we show mirrored picture on the screen)
                transforms[mOffset+0]  = t[0];
                transforms[mOffset+1]  = -t[1];
                transforms[mOffset+2]  = -t[2];
                //transforms[mOffset+3]  = 0;
                transforms[mOffset+4]  = -t[4];
                transforms[mOffset+5]  = t[5];
                transforms[mOffset+6]  = t[6];
                //transforms[mOffset+7]  = 0;
                transforms[mOffset+8]  = -t[8];
                transforms[mOffset+9]  = t[9];
                transforms[mOffset+10] = t[10];
                //transforms[mOffset+11] = 0;
                transforms[mOffset+12] = -t[12];
                transforms[mOffset+13] = t[13];
                transforms[mOffset+14] = t[14];
                //transforms[mOffset+15] = 1;
            } else {
                transforms[mOffset+0]  = t[0];
                transforms[mOffset+1]  = t[1];
                transforms[mOffset+2]  = t[2];
                //transforms[mOffset+3]  = 0;
                transforms[mOffset+4]  = t[4];
                transforms[mOffset+5]  = t[5];
                transforms[mOffset+6]  = t[6];
                //transforms[mOffset+7]  = 0;
                transforms[mOffset+8]  = t[8];
                transforms[mOffset+9]  = t[9];
                transforms[mOffset+10] = t[10];
                //transforms[mOffset+11] = 0;
                transforms[mOffset+12] = t[12];
                transforms[mOffset+13] = t[13];
                transforms[mOffset+14] = t[14];
                //transforms[mOffset+15] = 1;
        	}
        }

        return true;
    }
});

Xflow.registerOperator('alvar-mobile-xflow.selectTransform', {
    outputs: [ {type: 'float4x4', name : 'result', customAlloc: true} ],
    params:  [ {type: 'int', source : 'index'},
               {type: 'float4x4', source: 'transform'} ],
    alloc: function(sizes, index, transform) {
        sizes['result'] = 1;
    },
    evaluate: function(result, index, transform) {
        var i = 16 * index[0];
        if (i < transform.length && i+15 < transform.length) {
			result[0] = transform[i+0]; result[1] = transform[i+1]; result[2] = transform[i+2]; result[3] = transform[i+3];
			result[4] = transform[i+4]; result[5] = transform[i+5]; result[6] = transform[i+6]; result[7] = transform[i+7];
			result[8] = transform[i+8]; result[9] = transform[i+9]; result[10] = transform[i+10]; result[11] = transform[i+11];
			result[12] = transform[i+12]; result[13] = transform[i+13]; result[14] = transform[i+14]; result[15] = transform[i+15];
        } else {
			result[0] = 1; result[1] = 0; result[2] = 0; result[3] = 0;
			result[4] = 0; result[5] = 1; result[6] = 0; result[7] = 0;
			result[8] = 0; result[9] = 0; result[10] = 1; result[11] = 0;
			result[12] = 0; result[13] = 0; result[14] = 0; result[15] = 1;
        }
    }
});

