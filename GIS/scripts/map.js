(function() {

    function initApp() {
        // XML3D.debug.loglevel = 0; // set log level to "all"

        var xml3dobject = document.getElementById("xml3dContent");
        var height = $(document).height();
        var width = $(document).width();
        xml3dobject.setAttribute("width", width);
        xml3dobject.setAttribute("height", height);

        initMoveable();

        // Request building data from GIS server
        var buildings = createGISRequest("buildings", "428000.59375,7210617,428046.125,7210682");
        requestGISData(buildings);

        // Request terrain data from GIS server
        var terrain = createGISRequest("terrain", "410000.0,7194000.0,446000.0,7223960.0");
        requestGISData(terrain);

        // Move camera to correct debugging position
        var camera_node = document.getElementById("t_node-camera_player");

        //lappi
        //camera_node.setAttribute("translation", "373573.78125 7488050 1676.4444580078125");
        //camera_node.setAttribute("translation", "399000 7488050 1676.4444580078125");

        //oulu
        camera_node.setAttribute("translation", "426990.5 7211167.5 328.6792907714844");


        camera_node.setAttribute("rotation", "-0.24106520414352417 0.007622132543474436 -0.9704789519309998 3.1453794327656754");
        var camera_player = document.getElementById("camera_player-camera");
        camera_player.setAttribute("orientation", "-0.74122554063797 -0.6073552966117859 -0.2858394682407379 4.331712643691064");

    }

    function addXml3DContent(xml3dData) {
        console.log(xml3dData);

        //$("#xml3dContent").empty();
        $("#MaxScene").append(xml3dData);

    }

    function createGISRequest(layer, boundingbox) {
        var request;
        var baseUrl = "http://localhost:9090/geoserver/";
        var workspace = "fiware";
        var service = "w3ds?version=0.4&service=w3ds";
        var format = "&format=model/xml3d+xml";
        var crs = "&crs=EPSG:3047";
        var request = "&request=GetScene";

        

        request = baseUrl + service + request + crs + format+"&layers="+workspace+":"+layer+"&boundingbox="+boundingbox;
        console.log(request);
        return request;
    }

    function requestGISData(httpRequest) {
        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
        } else {
            xmlhttp = new XDomainRequest();
        }

        xmlhttp.onreadystatechange=function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {
                addXml3DContent(xmlhttp.responseText);
            }
        }

        xmlhttp.open("GET", httpRequest , true);

        xmlhttp.send();
    }

    window.onload = initApp;

}());
