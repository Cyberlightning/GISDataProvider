var xmlDoc;

(function() {

    function parseServerCapabilities(response) {
        // console.log(response);

    }

    function getServerCapabilities(baseUrl, service, version) {
        var requestUrl = baseUrl + "ows?service=" + service + "&version=" + version + "&request=GetCapabilities";
        httpRequest(requestUrl, parseServerCapabilities);
    }

    function getGeoserverCapabilities() {
        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
        } else {
            xmlhttp = new XDomainRequest();
        }

        // Set callback function
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {
                // callback(xmlhttp.responseText);
                // console.log(xmlhttp.responseText);

                xmlDoc = new DOMParser().parseFromString(xmlhttp.responseText,'text/xml');
                var x = xmlDoc.getElementsByTagName("Layer");
                // console.log(x);
                for (i=0;i<x.length;i++)
                { 
                    var combo = document.getElementById("selector");
                    var option = document.createElement("option");
                    option.text = x[i].getElementsByTagName("Identifier")[0].childNodes[0].nodeValue;
                    option.value = x[i].getElementsByTagName("Identifier")[0].childNodes[0].nodeValue;
                    try {
                        combo.add(option, null); //Standard 
                    }catch(error) {
                        combo.add(option); // IE only
                    }
                    option.value = "";
                    }
                }
            }

        xmlhttp.open("GET", "http://localhost:9090/geoserver/ows?service=w3ds&version=0.4.0&request=GetCapabilities" , true);
        xmlhttp.send();
    }


    // Traps selection list click event and launch layer detail fetching funtion
     $(function() {
        $("#selector").click(function(e) {
            // console.log(this.options[this.selectedIndex].text);
            e.preventDefault(); // if desired...
          // other methods to call...
          // initApp(); 

          newLayer = true;
          // initSceneMngr(this.options[this.selectedIndex].text);
          getLayerDetails(this.options[this.selectedIndex].text);
        });
      });

    // Traps camera movement, used for analyzing when new layer data should be requested
    $("#camera_player-camera").bind("DOMAttrModified", function() {
        var cam = document.getElementById("camera_player-camera");
        var coordinates = cam.getAttribute("position");
        if (coordinates != null){
            var coordSplit = coordinates.split(" ");
            var currentX = parseInt(coordSplit[0]);
            var currentY = parseInt(coordSplit[2]);
            // calculateCurrentPosLayerBlock(currentX, currentY);    
        }
    })


    window.onload = getGeoserverCapabilities();

}());

