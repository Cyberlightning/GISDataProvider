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

        //var camera_player = document.getElementById("t_node-camera_player");
        //camera_player.setAttribute("translation", "409000.0 7499590.0 198.209");

    }

    function requestGISData() {
        var baseUrl = "http://localhost:9090/geoserver/";
        var workspace = "it.geosolutions";
        var service = "/w3ds?version=0.4&service=w3ds";
        var format = "&format=application/ole";
        var crs = "&crs=EPSG:3047"
        var request = "&request=GetScene";

        var Oulu = "439970.0,7199970.0,446030.0,7206030.0";
        var Lappi = "394000.0,7488000.0,422000.0,7535950.0";

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
