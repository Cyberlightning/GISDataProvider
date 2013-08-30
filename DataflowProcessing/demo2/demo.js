(function () {
    navigator.getUserMedia = (navigator.getUserMedia ||
        navigator.webkitGetUserMedia ||
        navigator.mozGetUserMedia ||
        navigator.msGetUserMedia);

    var video = document.querySelector( 'video' ),
        button1 = document.querySelector( '#button1' ),
        button2 = document.querySelector( '#button2' ),
        button3 = document.querySelector( '#button3' ),
        button4 = document.querySelector( '#button4' ),
        canvas = document.querySelector( 'canvas' ),
        ctx2D = canvas.getContext( '2d' ),
        logEl = document.querySelector( '#log' ),
        snapshot = document.querySelector( '#snapshot' ),
	 snapshotinput = document.querySelector( '#snapshotinput' ),
        log = wex.Util.log,
        fallbackMode = false,
        metaDataLoaded = false,
        videoLoaded = false,
        localVideoStream = null;
        backEndServer = "http://176.9.92.67:8088";


    function hasGetUserMedia() {
        return !!(navigator.getUserMedia);
    }

    function getCameraFeed() {
        if ( hasGetUserMedia() ) {
            log( "Requesting Camera feed." );
            // Not showing vendor prefixes.
            navigator.getUserMedia( {video: true, audio: true}, function ( stream ) {
                video.src = window.URL.createObjectURL( stream );
                localVideoStream = stream;

                fallbackMode = false;

            }, fallBack );
        } else {

            fallBack();
        }
        button1.innerHTML = "Stop";
        button2.innerHTML = "Mute";
    }

    function fallBack() {
        log( "Web Camera feed not available." );
        log( "Switching to fallback demo video." );
        video.src = 'ar_marker.ogg';
        fallbackMode = true;
    }

    function toggleStart() {
        if ( video.paused ) {
           start();
        } else {
           stop();
        }
    }

    function start(){
        log( "Starting stream." );

        // Starting video
        if ( !fallbackMode ) {
            getCameraFeed();
        }
        video.play();
        button1.innerHTML = "Stop";

    }

    function stop() {
        log( "Stopping stream." );

        // Pausing video
        video.pause();

        if ( localVideoStream ) {
            // Stopping camera feed
            localVideoStream.stop();
            localVideoStream = null;
        }
        button1.innerHTML = "Start";
    }


    function toggleMute() {
        if ( localVideoStream && localVideoStream.getAudioTracks ) {
            var audioTracks = localVideoStream.getAudioTracks();
            for ( var i = 0, l = audioTracks.length; i < l; i++ ) {
                audioTracks[i].enabled = !audioTracks[i].enabled;
            }
            button2.innerHTML = button2.innerHTML === "Mute" ? "Unmute" : "Mute";
        }
    }

    function switchCamera() {
        stop();
        // Requesting the camera feed again, user can then choose correct camera
        // Changing camera directly trough the JavaScript API is not possible at the moment
        getCameraFeed();
    }

    function takeSnapshot() {
        if ( metaDataLoaded ) {
            var w = video.videoWidth;
            var h = video.videoHeight;
            canvas.width = w;
            canvas.height = h;
            ctx2D.drawImage( video, 0, 0);
            //snapshot.src = canvas.toDataURL( 'image/webp');
            snapshot.src = canvas.toDataURL( 'image/jpeg');
            snapshot.style.visibility = "visible";
	     snapshotinput.src = canvas.toDataURL( 'image/jpeg');
            snapshotinput.style.visibility = "visible";
            uploadImageToServer(backEndServer, snapshot);
        }
    }

    function uploadImageToServer(serverUrl, imageData){

        var imagefile = imageData.src;

        log('trying to send image to: ' + serverUrl + "...");

        var xhr = new XMLHttpRequest();
        xhr.open("POST", serverUrl, true);
        //xhr.setRequestHeader("Content-Type", "image/jpeg")
        xhr.send(imagefile);

        xhr.onreadystatechange = function () {

            if(xhr.readyState === 4){
                if(xhr.status  === 200){
                    log('"send succeeded."'); 
                    log(serverUrl + ": " + xhr.responseText);
                }
                else if (request.status === 404){
                    log('send failed.'); 
                    log(serverUrl + ": " + xhr.responseText);
                }
            }
        }

        xhr.onerror = function (e) {
            log("sending image failed");
        };
    }

    if ( button1 ) {
        button1.onclick = toggleStart;
    }

    if ( button2 ) {
        button2.onclick = toggleMute;
    }
    if ( button3 ) {
        button3.onclick = takeSnapshot;
    }

    if ( button4 ) {
        button4.onclick = switchCamera;
    }

    video.addEventListener( "loadstart", function () {
        log( "Loading video stream." );
    }, false );

    video.addEventListener( "loadedmetadata", function () {
        metaDataLoaded = true;
        log( "Video metadata loaded." );
    }, false );

    video.addEventListener( "canplay", function () {
        videoLoaded = true;
        log( "Video data loaded." );
    }, false );

    // Initializing camera
    getCameraFeed();

}());
