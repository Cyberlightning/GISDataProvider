(function() {
    // Define global parameters
    var crs, boundingbox, layer; // What happens if there are several layers with different bb's?
    // Map some id to each layer and use that same id for all options in that layer (crs, bb and so on..)?

    function initApp() {
        var baseUrl = "http://localhost:9090/geoserver/";

        // XML3D.debug.loglevel = 0; // set log level to "all"       

        // Get server capabilities
        getServerCapabilities(baseUrl, "w3ds", "0.4.0");

        var xml3dobject = document.getElementById("xml3dContent");
        var height = $(document).height();
        var width = $(document).width();
        xml3dobject.setAttribute("width", width);
        xml3dobject.setAttribute("height", height);

        // Request building data from GIS server
        var buildings = createGISRequest(baseUrl, "buildings", "428000.59375,7210617,428046.125,7210682");
        httpRequest(buildings, addXml3DContent);

        // Request terrain data from GIS server
        var terrain = createGISRequest(baseUrl, "terrain", "410000.0,7194000.0,446000.0,7223960.0");
        //var terrain = createGISRequest(baseUrl, "lappi", "374000.0,7488050.0,422000.0,7536000.0");
        httpRequest(terrain, addXml3DContent);

        // Move camera to correct debugging position
        var camera_node = document.getElementById("t_node-camera_player");

        //lappi
        //camera_node.setAttribute("translation", "373573.78125 1676.4444580078125 7488050");
        //camera_node.setAttribute("translation", "399000 7488050 1676.4444580078125");

        //oulu
        camera_node.setAttribute("translation", "426990.5 7211167.5 328.6792907714844");

        var camera_player = document.getElementById("camera_player-camera");
        camera_player.setAttribute("orientation", "0.0 1.0 0.0 4.608328501404633");

    }

    function addXml3DContent(xml3dData) {
        console.log(xml3dData);

        //$("#xml3dContent").empty();
        $("#MaxScene").append(xml3dData);

    }

    function parseServerCapabilities(response) {
        // console.log(response);

    }

    function getServerCapabilities(baseUrl, service, version) {
        var requestUrl = baseUrl + "ows?service=" + service + "&version=" + version + "&request=GetCapabilities";
        httpRequest(requestUrl, parseServerCapabilities);
    }

    function createGISRequest(baseUrl, layer, boundingbox) {
        var requestUrl;
        var workspace = "fiware";
        var service = "w3ds?version=0.4&service=w3ds";
        var format = "&format=model/xml3d+xml";
        var crs = "&crs=EPSG:3047";
        var request = "&request=GetScene";

        requestUrl = baseUrl + service + request + crs + format+"&layers="+workspace+":"+layer+"&boundingbox="+boundingbox;
        console.log(requestUrl);
        return requestUrl;
    }

    function httpRequest(requestUrl, callback) {
        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
        } else {
            xmlhttp = new XDomainRequest();
        }

        // Set callback function
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {
                callback(xmlhttp.responseText);
            }
        }

        xmlhttp.open("GET", requestUrl , true);
        xmlhttp.send();
    }

    window.onload = initApp;

}());
