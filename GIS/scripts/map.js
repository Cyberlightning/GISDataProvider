var xmlDoc;
(function() {
    // Define global parameters
    // View radius which is used for defining how big area of the layer is fetched from the server
    var viewAreaRadius = 10000;
    var camHeightOffset = 1000;
    var currentTerrainElevRefPoint = 0;

    var screenHeight = $(document).height()-100;
    var screenWidth = $(document).width();

    var camPagedLoadCenterX = 0;
    var camPagedLoadCenterY = 0;

    var currentLayerName = null;
    var currentLayerCRS = null;

    // layer center coordinates for camera placement when first block of the  terrain is loaded
    var CamInitCenterX, CamInitCenterY = 0;

    // Currently loaded layer bounding box min and max values    
    var LayerMinX = 0;
    var LayerMinY = 0;
    var LayerMaxX = 0;
    var LayerMaxY = 0;

    // boolean to verify if new layer is loaded or new data fetched to already viewed layer
    var newLayer = new Boolean();
    newLayer = false;

    //Array to track which blocks of the whole layer are loaded
    var LayerblockArray=[ [0,0,0],
                          [0,0,0],
                          [0,0,0] ]
    

    // Parses data for GeoServer GIS request
    // 
    // layerName = Layer Name
    // lowerCornerX, lowerCornerY, higherCornerX, higherCornerY = BoundingBox lower and higher corners, area for GIS request
    // layerCRS = Layer CRS
    function getElements(layerName, lowerCornerX, lowerCornerY, higherCornerX, higherCornerY, layerCRS ){
        var baseUrl = "http://localhost:9090/geoserver/";
        var service = "w3ds";
        var version = "0.4.0";

        var xml3dobject = document.getElementById("xml3dContent");
        
        xml3dobject.setAttribute("width", screenWidth);
        xml3dobject.setAttribute("height", screenHeight);
        
        console.log("getElements: "+lowerCornerX+", "+lowerCornerY+", "+higherCornerX+", "+higherCornerY);
        var terrain = createGISRequest( baseUrl, 
                                        layerName, 
                                        lowerCornerX+","+
                                        lowerCornerY+","+
                                        higherCornerX+","+
                                        higherCornerY,
                                        layerCRS);

        httpRequest(terrain, layerName, addXml3DContent);

    }


    // Used only when new layer is request
    function setCameraPosition(){
        // Move camera to correct debugging position
        var camera_node = document.getElementById("t_node-camera_player");

        // console.log("setCameraPosition"+layerCenterX+", "+layerCenterY);
        console.log("setCameraPosition"+currentTerrainElevRefPoint+parseInt(camHeightOffset));
        // camera_node.setAttribute( "translation", 
        //                            layerCenterX+" "+
        //                            (currentTerrainElevRefPoint+camHeightOffset)+" "+
        //                            layerCenterY);
        

        var camera_player = document.getElementById("camera_player-camera");
        // camera_player.setAttribute("orientation", "0.15 -0.99 -0.05 5.4");
        camera_player.setAttribute("orientation", "-0.00065 1.0 0.01 3.0");
        camera_player.setAttribute( "position", 
                                   CamInitCenterX+" "+
                                   (currentTerrainElevRefPoint+camHeightOffset)+" "+
                                   CamInitCenterY);
        
        camPagedLoadCenterX = CamInitCenterX;
        camPagedLoadCenterY = CamInitCenterY;
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
        var service = "w3ds?version=0.4&service=w3ds";
        var format = "&format=model/xml3d+xml";
        var crs = "&crs="+layerCRS;
        var request = "&request=GetScene";

        requestUrl = baseUrl + service + request + crs + format+"&layers="+layer+"&boundingbox="+boundingbox;
        console.log(requestUrl);
        return requestUrl;
    }

    function httpRequest(requestUrl, layerName, callback) {
        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
        } else {
            xmlhttp = new XDomainRequest();
        }

        // Set callback function
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {                
                callback(xmlhttp.responseText, layerName);
            }
        }

        xmlhttp.open("GET", requestUrl , true);
        xmlhttp.send();
    }

    // Fetches layer details from GeoServer and passes them to getElements()-function
    function getLayerDetails(layername) {
        var x = xmlDoc.getElementsByTagName("Layer");
        console.log(x);
        for (i=0;i<x.length;i++) { 
            if (layername == x[i].getElementsByTagName("Identifier")[0].childNodes[0].nodeValue) {
                // console.log("success nimi löyty");
                // console.log(x[i].getElementsByTagName("Title")[0].childNodes[0].nodeValue);
                // console.log(x[i].getElementsByTagName("Identifier")[0].childNodes[0].nodeValue);
                console.log(x[i].getElementsByTagName("LowerCorner")[0].childNodes[0].nodeValue);
                console.log(x[i].getElementsByTagName("UpperCorner")[0].childNodes[0].nodeValue);
                // console.log(x[i].getElementsByTagName("OutputFormat")[0].childNodes[0].nodeValue);
                // console.log(x[i].getElementsByTagName("DefaultCRS")[0].childNodes[0].nodeValue);

                //Store layer bounding box values for later usage
                var lowerCornerSplit = x[i].getElementsByTagName("LowerCorner")[0].childNodes[0].nodeValue.split(" ");
                var higherCornerSplit = x[i].getElementsByTagName("UpperCorner")[0].childNodes[0].nodeValue.split(" ");                
                LayerMinX = parseInt(lowerCornerSplit[0]);
                LayerMinY = parseInt(lowerCornerSplit[1]);
                LayerMaxX = parseInt(higherCornerSplit[0]);
                LayerMaxY = parseInt(higherCornerSplit[1]);
                // console.log("minmax arvot BB "+LayerMinX, LayerMinY, LayerMaxX, LayerMaxY);

                var MinX, MinY, MaxX, MaxY, blocklengthX, blocklengthY;
                blocklengthX = parseInt((LayerMaxX-LayerMinX)/3);
                blocklengthY = parseInt((LayerMaxY-LayerMinY)/3);
                // console.log("blocklenght X,Y "+blocklengthX, blocklengthY);

                MinX = parseInt(LayerMinX);
                MinY = parseInt(LayerMinY);
                MaxX = parseInt(LayerMinX + blocklengthX);
                MaxY = parseInt(LayerMinY + blocklengthY);
                // console.log("MinX, MinY, MaxX, MaxY "+MinX, MinY, MaxX, MaxY);
                CamInitCenterX = parseInt(LayerMinX+(blocklengthX / 2));
                CamInitCenterY = parseInt(LayerMinY+(blocklengthY / 2));
                console.log(CamInitCenterX, CamInitCenterY);

                console.log(LayerblockArray[0]);
                LayerblockArray[0][0] = 1;
                console.log(LayerblockArray[0]);


                getElements(x[i].getElementsByTagName("Identifier")[0].childNodes[0].nodeValue,
                            // x[i].getElementsByTagName("LowerCorner")[0].childNodes[0].nodeValue,
                            // x[i].getElementsByTagName("UpperCorner")[0].childNodes[0].nodeValue,
                            MinX, MinY, MaxX, MaxY,
                            x[i].getElementsByTagName("DefaultCRS")[0].childNodes[0].nodeValue
                );


                currentLayerName = x[i].getElementsByTagName("Identifier")[0].childNodes[0].nodeValue;
                currentLayerCRS = x[i].getElementsByTagName("DefaultCRS")[0].childNodes[0].nodeValue;

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
            console.log(this.options[this.selectedIndex].text);
            e.preventDefault(); // if desired...
          // other methods to call...
          // initApp(); 

          newLayer = true;
          getLayerDetails(this.options[this.selectedIndex].text);
        });
      });


    function addXml3DContent(xml3dData, layerName) {
        // console.log(xml3dData);

         var newGroup = document.createElement('group');
         var groupIdName = layerName;
         var xmlnsTagContent = 'http://www.xml3d.org/2009/xml3d';

         newGroup.setAttribute('id',groupIdName);
         newGroup.setAttribute('xmlns',xmlnsTagContent);
         // ni.appendChild(newdiv);
         $("#MaxScene").append(newGroup);


        // $("#MaxScene").append(xml3dData);
        $(newGroup).append(xml3dData);

        if (newLayer) {
            getTerrainElevationRefPoint();
            setCameraPosition();    
            newLayer = false;
        }
    }

    // parse XML3D object and fetch first elevation point to be used as reference elevation for camera
    function getTerrainElevationRefPoint(){
        var meshObjects = document.getElementsByTagName("mesh");
        //Get elev data from last node
        elevRefpoint = document.getElementsByTagName("mesh")[meshObjects.length-1].childNodes[1].value[10];
        console.log(meshObjects.length);
        console.log(elevRefpoint);
        if (elevRefpoint > 0){
            currentTerrainElevRefPoint = elevRefpoint;
        }
        else {
            currentTerrainElevRefPoint = 0;
        }
        
    }

    
    window.onload = getGeoserverCapabilities();
    // window.onload = initApp(); 

    function calculateCurrentPosLayerBlock(currentX, currentY){
        // console.log("calculateCurrentPosLayerBlock:currentX, currentY "+currentX, currentY);    
        var MinX, MinY, MaxX, MaxY, blocklengthX, blocklengthY;
        blocklengthX = parseInt((LayerMaxX-LayerMinX)/3);
        blocklengthY = parseInt((LayerMaxY-LayerMinY)/3);
        // console.log("blocklenght X,Y "+blocklengthX, blocklengthY);

        // X0 = parseInt(LayerMinX);
        X1 = parseInt(LayerMinX + blocklengthX);
        X2 = parseInt(LayerMinX + (2*blocklengthX));
        X3 = parseInt(LayerMinX + (3*blocklengthX));
        // Y0 = parseInt(LayerMinY);
        Y1 = parseInt(LayerMinY + blocklengthY);
        Y2 = parseInt(LayerMinY + (2*blocklengthY));
        Y3 = parseInt(LayerMinY + (3*blocklengthY));

        var col, row, MinX, MinY, MaxX, MaxY = 0;
        var offset = parseInt(blocklengthX/3);

        if (parseInt(currentX+offset) < parseInt(X1)){
            // console.log("currentX <= X1");
            row = 0;
            MaxX = X1;
            MinX = X1 - blocklengthX;
        }else if (parseInt(currentX+offset) < parseInt(X2)){
            // console.log("currentX <= X2");
            row = 1;
            MaxX = X2;
            MinX = X2 - blocklengthX;
        }else if (parseInt(currentX+offset) < parseInt(X3)){
            // console.log("currentX <= X3");
            row = 2;
            MaxX = X3;
            MinX = X3 - blocklengthX;
        }else{
            console.log("currentX out of layer BB");
        }

        if (parseInt(currentY+offset) < parseInt(Y1)){
            // console.log("currentY <= Y1");
            col = 0;
            MaxY = Y1;
            MinY = Y1 - blocklengthY;

        }else if (parseInt(currentY+offset) < parseInt(Y2)){
            // console.log("currentY <= Y2");
            col = 1;
            MaxY = Y2;
            MinY = Y2 - blocklengthY;
        }else if (parseInt(currentY+offset) < parseInt(Y3)){
            // console.log("currentY <= Y3");
            col = 2; 
            MaxY = Y3; 
            MinY = Y3 - blocklengthY;
        }else{
            console.log("currentY out of layer BB");
        }

        if (LayerblockArray[row][col] == 0){
            console.log("load new block. row:"+row+", col: "+col );   
            getElements(currentLayerName, MinX, MinY, MaxX, MaxY, currentLayerCRS);
            LayerblockArray[row][col] =1;
            console.log(LayerblockArray[0],LayerblockArray[1],LayerblockArray[2]);
        }
    }

    // Traps camera movement, used for analyzing when new layer data should be requested
    $("#camera_player-camera").bind("DOMAttrModified", function() {
        var cam = document.getElementById("camera_player-camera");
        var coordinates = cam.getAttribute("position");
        if (coordinates != null){
            var coordSplit = coordinates.split(" ");
            var currentX = parseInt(coordSplit[0]);
            var currentY = parseInt(coordSplit[2]);
            calculateCurrentPosLayerBlock(currentX, currentY);    
        }
    })
    

}());

