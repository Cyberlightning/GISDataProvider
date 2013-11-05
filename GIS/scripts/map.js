(function() {
    var xmlhttp = null;

    function initApp() {
        // XML3D.debug.loglevel = 0; // set log level to "all"
        initMoveable();
        requestGISData();
    }

    function createXml3D(xml3dData) {
        // console.log(xml3dData);

        //$("#xml3dContent").empty();
        $("#xml3dContent").append(xml3dData);

        var xml3dobject = document.getElementById("xml3dContent");
        var height = $(document).height();
        var width = $(document).width();
        console.log(width);
        xml3dobject.setAttribute("width", width);
        xml3dobject.setAttribute("height", height);

        // Move camera to correct debugging position
        var camera_node = document.getElementById("t_node-camera_player");
        camera_node.setAttribute("translation", "373573.78125 7488050 1676.4444580078125");
        camera_node.setAttribute("rotation", "-0.24106520414352417 0.007622132543474436 -0.9704789519309998 3.1453794327656754");
        
        var camera_player = document.getElementById("camera_player-camera");
        camera_player.setAttribute("orientation", "-0.30856573581695557 -0.7244177460670471 -0.6164464354515076 3.28808722904229");


    }

    function requestGISData() {
        var baseUrl = "http://localhost:9090/geoserver/";
        var workspace = "mml";
        // var layer = ":lappia100";
        var layer = ":lappia_63_block";
        // var layer = ":63_block_10_step";
        // var layer = ":Oulu_yksi_blokki";
        // var layer = ":buildings_3dPolygon";
        var service = "w3ds?version=0.4&service=w3ds";
        //var format = "&format=application/ole";
        var format = "&format=model/xml3d+xml";
        var crs = "&crs=EPSG:3047"
        // var crs = "&crs=EPSG:27492";
        var request = "&request=GetScene";

        var Oulu = "439970.0,7199970.0,446030.0,7206030.0";
        var Oulu1block = "440000.0,7200000.0,446000.0,7206000.0";
        var Lappi = "380000.0,7488000.0,386000.0,7494000.0";
        var Lappi2 = "382162.50000,7489195.31250,384570.31250,7491445.31250";
        var pallas_64Block = "374000.0,7488050.0,422000.0,7536000.0";
        var pallas = "374000.0,7488050.0,380000.0,7536000.0";
        var buildings3D ="-14153.81617,197004.47732,-13799.17587,197279.03756";

        var boundingbox = pallas_64Block;

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

        
        // xmlhttp.open("GET", "http://localhost:9090/geoserver/w3ds?version=0.4&service=w3ds&request=GetScene&crs=EPSG:3047&format=model/xml3d+xml&layers=mml:lappia_63_block&boundingbox=374000.0,7488050.0,422000.0,7536000.0" , true);
        // xmlhttp.open("GET", "http://localhost:9090/geoserver/w3ds?version=0.4&service=w3ds&request=GetScene&crs=EPSG:3047&format=model/xml3d+xml&layers=mml:63_block_10_step&boundingbox=374000.0,7488050.0,422000.0,7536000.0" , true);
        
        xmlhttp.open("GET", baseUrl + service + request + crs + format+"&layers="+workspace+layer+"&boundingbox="+boundingbox , true);
        xmlhttp.send();
    }

    window.onload = initApp;

}());
