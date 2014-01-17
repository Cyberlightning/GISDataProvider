window.URL = window.URL || window.webkitURL;
navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia
		|| navigator.mozGetUserMedia || navigator.msGetUserMedia;

function init(){
	if (!navigator.getUserMedia)
		return;
	
	navigator.getUserMedia({
		video: {
			'mandatory': {
				'minWidth': WEBCAM_WIDTH,
				'minHeight': WEBCAM_HEIGHT
			}
		},
		audio : false
	}, function(stream) {
		var url = window.URL.createObjectURL(stream);
		// Init webcam to background 
		var background = document.getElementById('background');
		background.src = url;
		// Init webcam for xflow
		var webcam = document.getElementById('webcam');
		webcam.src = url;
	}, function(err) {
		console.log("The following error occured: " + err);
	});
}

window.onload = init;
