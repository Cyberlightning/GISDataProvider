var xmlDoc;
(function() {
    // Define global parameters
    var crs, boundingbox, layer; // What happens if there are several layers with different bb's?
    // View radius which is used for defining how big area of the layer is fetched from the server
    var viewAreaRadius = 500000;
    
    // Map some id to each layer and use that same id for all options in that layer (crs, bb and so on..)?

    // function initApp(area) {
    //     var baseUrl = "http://localhost:9090/geoserver/";
    //     var service = "w3ds";
    //     var version = "0.4.0";
        

    //     // XML3D.debug.loglevel = 0; // set log level to "all"       

    //     // Get server capabilities
    //     getServerCapabilities(baseUrl, "w3ds", "0.4.0");

    //     var xml3dobject = document.getElementById("xml3dContent");
    //     var height = $(document).height()-100;
    //     var width = $(document).width();
    //     xml3dobject.setAttribute("width", width);
    //     xml3dobject.setAttribute("height", height);

    //     // Request building data from GIS server
    //     // var buildings = createGISRequest(baseUrl, "buildings", "428000.59375,7210617,428046.125,7210682");
    //     // httpRequest(buildings, addXml3DContent);

    //     // Request terrain data from GIS server
    //     // var terrain = createGISRequest(baseUrl, "terrain", "410000.0,7194000.0,446000.0,7223960.0");
    //     // var terrain = createGISRequest(baseUrl, "qgis_shp_merge", "374000.0,7488050.0,422000.0,7536000.0");
    //     var viewAreaRadius = 100000000
    //     var layerMinX = 374000.0;
    //     var layerMinY = 7488050.0;
    //     var layerMaxX = 422000.0;
    //     var layerMaxY = 7536000.0;
    //     var layerCenterX = ((layerMaxX - layerMinX)/2)+layerMinX;
    //     var layerCenterY = ((layerMaxY - layerMinY)/2)+layerMinY;
        

    //     var camHeight =1676.4444580078125;


    //     // var camMinx = 374000.0 + area
    //     // var camMinY = 374000.0 + area
    //     // var camMaxX = 374000.0 + area
    //     // var camMaxY = 7536000.0
    //     // var terrain = createGISRequest(baseUrl, "qgis_shp_merge", "374000.0,7488050.0,422000.0,7536000.0");
    //     var terrain = createGISRequest(baseUrl, "qgis_shp_merge", (layerCenterX-viewAreaRadius)+","+(layerCenterY-viewAreaRadius)+","+(layerCenterX+viewAreaRadius)+","+(layerCenterY+viewAreaRadius));
    //     httpRequest(terrain, addXml3DContent);

    //     // Move camera to correct debugging position
    //     var camera_node = document.getElementById("t_node-camera_player");

    //     //lappi
    //     //Center
    //     camera_node.setAttribute("translation", "398000 1676.4444580078125 7512050");
    //     // camera_node.setAttribute("translation", "373573.78125 1676.4444580078125 7488050");
    //     //camera_node.setAttribute("translation", "399000 7488050 1676.4444580078125");

    //     //oulu
    //     // camera_node.setAttribute("translation", "426990.5 7211167.5 328.6792907714844");

    //     var camera_player = document.getElementById("camera_player-camera");
    //     camera_player.setAttribute("orientation", "0.0 1.0 0.0 4.608328501404633");

    // }

    function getElements(layerName, lowerCorner, higherCorner, layerCRS ){
        var baseUrl = "http://localhost:9090/geoserver/";
        var service = "w3ds";
        var version = "0.4.0";

        var lowerCornerSplit = lowerCorner.split(" ");
        var higherCornerSplit = higherCorner.split(" ");
        console.log(lowerCorner);
        console.log(higherCorner);

        
        var layerMinX = lowerCornerSplit[0]
        var layerMinY = lowerCornerSplit[1];
        var layerMaxX = higherCornerSplit[0];
        var layerMaxY = higherCornerSplit[1];
        console.log("layerMinX: "+parseInt(layerMinX));
        console.log("layerMinY: "+layerMinY);
        console.log("layerMaxX: "+layerMaxX);
        console.log("layerMaxY: "+layerMaxY);
        
        var xml3dobject = document.getElementById("xml3dContent");
        var height = $(document).height()-100;
        var width = $(document).width();
        xml3dobject.setAttribute("width", width);
        xml3dobject.setAttribute("height", height);

        
        var layerCenterX = ((parseInt(layerMaxX) - parseInt(layerMinX))/2)+parseInt(layerMinX);
        var layerCenterY = ((parseInt(layerMaxY) - parseInt(layerMinY))/2)+parseInt(layerMinY);
        

        // var camHeight =1676.4444580078125;
        var camHeight =1500;

        var terrain = createGISRequest( baseUrl, 
                                        layerName, 
                                        (layerCenterX-viewAreaRadius)+","+
                                        (layerCenterY-viewAreaRadius)+","+
                                        (layerCenterX+viewAreaRadius)+","+
                                        (layerCenterY+viewAreaRadius),
                                        layerCRS);

        httpRequest(terrain, addXml3DContent);

        // Move camera to correct debugging position
        var camera_node = document.getElementById("t_node-camera_player");

        //lappi
        //Center
        console.log(layerCenterX);
        console.log(layerCenterY);
        camera_node.setAttribute("translation", layerCenterX+" "+camHeight+" "+layerCenterY);
        // camera_node.setAttribute("translation", "398000 1676.4444580078125 7512050");

        // camera_node.setAttribute("translation", "373573.78125 1676.4444580078125 7488050");
        //camera_node.setAttribute("translation", "399000 7488050 1676.4444580078125");

        //oulu
        // camera_node.setAttribute("translation", "426990.5 7211167.5 328.6792907714844");

        var camera_player = document.getElementById("camera_player-camera");
        camera_player.setAttribute("orientation", "0.22 -1.0 -0.15 5.0");

    }



    function parseServerCapabilities(response) {
        // console.log(response);

    }

    function getServerCapabilities(baseUrl, service, version) {
        var requestUrl = baseUrl + "ows?service=" + service + "&version=" + version + "&request=GetCapabilities";
        httpRequest(requestUrl, parseServerCapabilities);
    }

    function createGISRequest(baseUrl, layer, boundingbox, layerCRS) {
        var requestUrl;
        // var workspace = "fiware";
        var workspace = "mml";
        var service = "w3ds?version=0.4&service=w3ds";
        var format = "&format=model/xml3d+xml";
        // var crs = "&crs=EPSG:3047";
        var crs = "&crs="+layerCRS;
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

    function getLayerDetails(layername) {
        var x = xmlDoc.getElementsByTagName("Layer");
        console.log(x);
        for (i=0;i<x.length;i++) { 
            if (layername == x[i].getElementsByTagName("Title")[0].childNodes[0].nodeValue) {
                console.log("succes nimi löyty");
                console.log(x[i].getElementsByTagName("Title")[0].childNodes[0].nodeValue);
                console.log(x[i].getElementsByTagName("Identifier")[0].childNodes[0].nodeValue);
                console.log(x[i].getElementsByTagName("LowerCorner")[0].childNodes[0].nodeValue);
                console.log(x[i].getElementsByTagName("UpperCorner")[0].childNodes[0].nodeValue);
                console.log(x[i].getElementsByTagName("OutputFormat")[0].childNodes[0].nodeValue);
                console.log(x[i].getElementsByTagName("DefaultCRS")[0].childNodes[0].nodeValue);
                getElements(x[i].getElementsByTagName("Title")[0].childNodes[0].nodeValue,
                            x[i].getElementsByTagName("LowerCorner")[0].childNodes[0].nodeValue,
                            x[i].getElementsByTagName("UpperCorner")[0].childNodes[0].nodeValue,
                            x[i].getElementsByTagName("DefaultCRS")[0].childNodes[0].nodeValue
                );
            }
        }
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
                console.log(xmlhttp.responseText);

                xmlDoc = new DOMParser().parseFromString(xmlhttp.responseText,'text/xml');
                var x = xmlDoc.getElementsByTagName("Layer");
                console.log(x);
                for (i=0;i<x.length;i++)
                { 
                    var combo = document.getElementById("selector");
                    var option = document.createElement("option");
                    option.text = x[i].getElementsByTagName("Title")[0].childNodes[0].nodeValue;
                    option.value = x[i].getElementsByTagName("Title")[0].childNodes[0].nodeValue;
                    option.onchange = console.log("clicked");
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

     $(function() {
        $("#selector").click(function(e) {
            console.log(this.options[this.selectedIndex].text);
            e.preventDefault(); // if desired...
          // other methods to call...
          // initApp(); 

          getLayerDetails(this.options[this.selectedIndex].text);
        });
      });

    function addXml3DContent(xml3dData) {
        // console.log(xml3dData);

        //$("#xml3dContent").empty();
        $("#MaxScene").append(xml3dData);

    }

    function getTerrainElevationRefPoint(){
        var elevRefpoint = document.getElementsByTagName("mesh")[0].childNodes[1].value[10];
        console.log(elevRefpoint);
    }

    
    window.onload = getGeoserverCapabilities();
    // window.onload = initApp(); 


    // $("#camera_player-camera").bind("DOMAttrModified", function() {
    //     var coordinates = document.getElementById("camera_player-camera");
        
    //     getTerrainElevationRefPoint();        
    // })
    

}());

