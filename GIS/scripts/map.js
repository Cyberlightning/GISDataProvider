var xmlDoc;
var spinner;

(function() {
    var layerNames = [];

    var spinOpts = {
          lines: 30, // The number of lines to draw
          length: 20, // The length of each line
          width: 4, // The line thickness
          radius: 30, // The radius of the inner circle
          rotate: 0, // The rotation offset
          color: '#000', // #rgb or #rrggbb
          speed: 2, // Rounds per second
          trail: 60, // Afterglow percentage
          shadow: true, // Whether to render a shadow
          hwaccel: false, // Whether to use hardware acceleration
          className: 'spinner', // The CSS class to assign to the spinner
          zIndex: 2e9, // The z-index (defaults to 2000000000)
          top: 'auto', // Top position relative to parent in px
          left: 'auto' // Left position relative to parent in px
        };
    
    spinner = new Spinner(spinOpts).spin();
    $("#loading").append(spinner.el);

    var baseUrl = "http://localhost:9090/geoserver/";
    // var baseUrl = "http://dev.cyberlightning.com:9091/geoserver/";
    var oldCoordinates = null;

    function parseServerCapabilities(response) {
        // console.log(response);

        xmlDoc = new DOMParser().parseFromString(response,'text/xml');
        var x = xmlDoc.getElementsByTagNameNS("http://www.opengis.net/w3ds/0.4.0", "Layer");


        $('#checkboxdiv').append(' | ');
        for (i=0;i<x.length;i++)
            { 
            var checkboxtext = x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Identifier")[0].childNodes[0].nodeValue;
            var checkboxvalue = x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Title")[0].childNodes[0].nodeValue;
            $('#checkboxdiv').append(
               $(document.createElement('input')).attr({
                   id:    checkboxvalue
                  ,name:  checkboxtext
                  ,value: checkboxvalue
                  ,type:  'checkbox'
               })
            );
            $('#checkboxdiv').append(
               $(document.createElement('label')).text(checkboxtext));
            $('#checkboxdiv').append(' | ');

            layerNames.push(checkboxvalue);
        }
    }

    function getGeoserverCapabilities() {
        // console.log("getGeoserverCapabilities");
        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
        } else {
            xmlhttp = new XDomainRequest();
        }

        // Set callback function
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {
                // console.log(xmlhttp.responseText);
                parseServerCapabilities(xmlhttp.responseText);
            }
        }

        xmlhttp.open("GET", baseUrl + "ows?service=w3ds&version=0.4.0&request=GetCapabilities" , true);
        xmlhttp.send();
    }


    // Traps selection list click event and launch layer detail fetching funtion
     $(function() {
        $("#checkboxdiv").click(function(e) {
            // console.log("Selection list item: "+this.options[this.selectedIndex].value);
            // e.preventDefault(); // if desired...

            for (i=0; i<layerNames.length; i++){
                console.log(layerNames[i]);
                if($('#'+layerNames[i]).is(':checked')){
                    console.log(layerNames[i]+" is checked");
                    newLayer = true;
                    getLayerDetails(baseUrl, layerNames[i]);
                }
            }
        });
      });

    // Traps camera movement, used for analyzing when new layer data should be requested
    $("#camera_player-camera").bind("DOMAttrModified", function() {
        // console.log("#camera_player-camera).bind(DOMAttrModified");
        // check flag if new layer is loaded, because in this case camera height needs to be adjusted 
        // and that operation tricks this function unneseccary. We want to see only camera movements after new layer is initialized
        // if (newLayer){
            var cam = document.getElementById("camera_player-camera");
            var coordinates = cam.getAttribute("position");
            if (!oldCoordinates && coordinates !== null){
                oldCoordinates = coordinates;
            }
            // console.log("oldCoordinates: "+oldCoordinates);
            // console.log("coordinates: "+coordinates);
            if ((coordinates != null) && (coordinates !==oldCoordinates)) {
                var coordSplit = coordinates.split(" ");
                var currentX = parseFloat(coordSplit[0]);
                var currentY = parseFloat(coordSplit[2]);
                calculateCurrentPosLayerBlock(currentX, currentY);        
                }
        // }
    })


    function init(){
        getGeoserverCapabilities();
    }

    window.onload = init();

    
}());

function startSpinner(){

    $("#loading").show();
    // spinner(spinOpts).spin();
    
};

function stopSpinner(){
    // spinner.spin();
    $("#loading").hide(true);
};

