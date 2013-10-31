(function() {
    var xmlhttp = null;

    function initApp() {
        XML3D.debug.loglevel = 0; // set log level to "all"
        initMoveable();
        requestGISData();
    }

    function createXml3D(xml3dData) {
        console.log(xml3dData);

        //$("#xml3dContent").empty();
        $("#xml3dContent").append(xml3dData);

        // Move camera to correct debugging position
        var camera_node = document.getElementById("t_node-camera_player");
        camera_node.setAttribute("translation", "405485 7519060 130.99200439453125");
        
        var camera_player = document.getElementById("camera_player-camera");
        camera_player.setAttribute("orientation", "-0.30856573581695557 -0.7244177460670471 -0.6164464354515076 3.28808722904229");

    }

    function requestGISData() {
        var baseUrl = "http://localhost:9090/geoserver/";
        var workspace = "it.geosolutions";
        var service = "/w3ds?version=0.4&service=w3ds";
        var format = "&format=model/xml3d+xml";
        var crs = "&crs=EPSG:3047"
        var request = "&request=GetScene";

        var Oulu = "439970.0,7199970.0,446030.0,7206030.0";
        var Lappi = "373760.0,7487760.0,422240.0,7536190.0";
        //var Lappi = "400485.0,7509960.0,410885.0,7521830.0"; 
        //var Lappi = "400485.0,7509060.0,400495.0,7509130.0";

        var boundingbox = Lappi;

        if (xmlhttp === null) {
            if (window.XMLHttpRequest) {
                xmlhttp = new XMLHttpRequest();
            } else {
                xmlhttp = new XDomainRequest();
            }
        }

        xmlhttp.onreadystatechange=function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {
                createXml3D(xmlhttp.responseText);
            }
        }

        xmlhttp.open("GET", baseUrl + workspace + service + request + crs + format+"&layers="+workspace+":lappi_shp&boundingbox="+boundingbox , true);
        xmlhttp.send();
    }

    window.onload = initApp;

}());
