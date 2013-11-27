(function() {

    var blocklengthX, blocklengthY = 0;

    var TESTIARVO = 0;

    //Array to track which blocks of the whole layer are loaded
    var LayerblockArray=[ [0,0,0],
                          [0,0,0],
                          [0,0,0] ];
    var layerBlockRow, layerBlockCol = 0;

    var screenHeight = $(document).height()-100;
    var screenWidth = $(document).width();

    // boolean to verify if new layer is loaded or new data fetched to already viewed layer
    // var newLayer = new Boolean();
    // newLayer = true;

    // View radius which is used for defining how big area of the layer is fetched from the server
    var viewAreaRadius = 10000;
    var camHeightOffset = 1000;
    var currentTerrainElevRefPoint = 0;

    var camPagedLoadCenterX = 0;
    var camPagedLoadCenterY = 0;

    var currentLayerName = null;
    var currentLayerCRS = null;

    // Currently loaded layer bounding box min and max values    
    var LayerMinX = 0;
    var LayerMinY = 0;
    var LayerMaxX = 0;
    var LayerMaxY = 0;

    // layer center coordinates for camera placement when first block of the  terrain is loaded
    var CamInitCenterX, CamInitCenterY = 0;

    this.initSceneMngr = function(Identifier, LowerCorner, UpperCorner, DefaultCRS ){       
        currentLayerName = Identifier;
        currentLayerCRS = DefaultCRS;
         console.log(LowerCorner, UpperCorner);

        //Store layer bounding box values for later usage
        var lowerCornerSplit = LowerCorner.split(" ");
        var higherCornerSplit = UpperCorner.split(" ");                
        LayerMinX = parseInt(lowerCornerSplit[0]);
        LayerMinY = parseInt(lowerCornerSplit[1]);
        LayerMaxX = parseInt(higherCornerSplit[0]);
        LayerMaxY = parseInt(higherCornerSplit[1]);
        console.log("minmax arvot BB "+LayerMinX, LayerMinY, LayerMaxX, LayerMaxY);

        var MinX, MinY, MaxX, MaxY;
        blocklengthX = parseInt((LayerMaxX-LayerMinX)/3);
        blocklengthY = parseInt((LayerMaxY-LayerMinY)/3);
        console.log("blocklenght X,Y "+blocklengthX, blocklengthY);

        MinX = parseInt(LayerMinX);
        MinY = parseInt(LayerMinY);
        MaxX = parseInt(LayerMinX + blocklengthX);
        MaxY = parseInt(LayerMinY + blocklengthY);
        // console.log("MinX, MinY, MaxX, MaxY "+MinX, MinY, MaxX, MaxY);
        // CamInitCenterX = parseInt(LayerMinX+(blocklengthX / 2));
        // CamInitCenterY = parseInt(LayerMinY+(blocklengthY / 2));
        // console.log(CamInitCenterX, CamInitCenterY);

        // console.log(LayerblockArray[0]);
        // LayerblockArray[0][0] = 1;
        layerBlockRow, layerBlockCol = 0;


        getElements(Identifier,
                    // x[i].getElementsByTagName("LowerCorner")[0].childNodes[0].nodeValue,
                    // x[i].getElementsByTagName("UpperCorner")[0].childNodes[0].nodeValue,
                    // MinX, MinY, MaxX, MaxY, //custom size for layer min/max boundaries
                    LayerMinX, LayerMinY, LayerMaxX, LayerMaxY, // Whole layer boundaries
                    DefaultCRS

        );
    }

    // Parses data for GeoServer GIS request
    // 
    // layerName = Layer Name
    // lowerCornerX, lowerCornerY, higherCornerX, higherCornerY = BoundingBox lower and higher corners, area for GIS request
    // layerCRS = Layer CRS
    function getElements(layerName, lowerCornerX, lowerCornerY, higherCornerX, higherCornerY, layerCRS, transfromX, transfromY){
        var baseUrl = "http://localhost:9090/geoserver/";
        var texture_layer = "mml:UV41R_RVK_5";
        var service = "w3ds";
        var version = "0.4.0";

        var xml3dobject = document.getElementById("xml3dContent");
        console.log("getElements transfromX, transfromY :"+ transfromX, transfromY);

        // var blocklengthX = parseInt((higherCornerX-lowerCornerX)/3);
        // var blocklengthY = parseInt((higherCornerY-lowerCornerY)/3);

        CamInitCenterX = parseInt(lowerCornerX+(blocklengthX / 2));
        CamInitCenterY = parseInt(lowerCornerY+(blocklengthY / 2));    
        
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

        httpRequest(terrain, layerName, transfromX, transfromY, addXml3DContent);


        // var texture = baseUrl + "mml/wms?service=WMS&version=1.1.0&request=GetMap&layers=" +
        //                                 texture_layer + 
        //                                 "&styles=&bbox=" + 
        //                                 lowerCornerX+","+
        //                                 lowerCornerY+","+
        //                                 higherCornerX+","+
        //                                 higherCornerY + 
        //                                 "&width=512&height=512&srs=EPSG:404000&format=application/openlayers"        
        // var texture = "http://localhost:9090/geoserver/mml/wms?service=WMS&amp;version=1.1.0&amp;request=GetMap&amp;layers=mml:UV41R_RVK_5" +
        //                                 texture_layer + 
        //                                 "&amp;styles=&amp;bbox=" + 
        //                                 lowerCornerX+","+
        //                                 lowerCornerY+","+
        //                                 higherCornerX+","+
        //                                 higherCornerY + 
        //                                 "&amp;width=512&amp;height=512&amp;srs=EPSG:404000&amp;format=image%2Fpng"        
        // addTextureToShader(texture);
        
//         // Move camera to correct debugging position
//         var camera_node = document.getElementById("t_node-camera_player");

    // LayerblockArray[layerBlockRow][layerBlockCol] = 1;

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

    function httpRequest(requestUrl, layerName, transfromX, transfromY, callback) {
        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
        } else {
            xmlhttp = new XDomainRequest();
        }

        // Set callback function
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {                
                callback(xmlhttp.responseText, layerName, transfromX, transfromY);
            }
        }

        xmlhttp.open("GET", requestUrl , true);
        xmlhttp.send();
    }

    function addTextureToShader(textureUrl) {
        var shader = document.getElementById("orangePhong");
        var str = "<texture name=\"diffuseTexture\">\n";
        str += "<img src=\"" + textureUrl + "\"/>\n" + "</texture>";

        $("#orangePhong").append(str);
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
        // camera_player.setAttribute("orientation", "-0.00065 1.0 0.01 3.0");
        camera_player.setAttribute("orientation", "0.00527839083224535 -0.9837312698364258 -0.1795685887336731 3.199385726672364");
        // camera_player.setAttribute("position", 
        //                            CamInitCenterX+" "+
        //                            (currentTerrainElevRefPoint+camHeightOffset)+" "+
        //                            CamInitCenterY);
        camera_player.setAttribute("position", "2800.548095703125 1144.894287109375 968.5448608398438");
        
        camPagedLoadCenterX = CamInitCenterX;
        camPagedLoadCenterY = CamInitCenterY;
    }

    function addXml3DContent(xml3dData, layerName, transfromX, transfromY) {
        // console.log(xml3dData);

         var newGroup = document.createElement('group');
         var groupIdName = layerName;
         var xmlnsTagContent = 'http://www.xml3d.org/2009/xml3d';

         newGroup.setAttribute('id',groupIdName);
         newGroup.setAttribute('xmlns',xmlnsTagContent);
         newGroup.setAttribute('shader','#orangePhong');
         // newGroup.setAttribute('setCameraPosition',transfromX+' 0 '+transfromY);
         // newGroup.setAttribute('style','transform: translate3d(0px,-20px, -30px');   

         $("#MaxScene").append(newGroup);


        // $("#MaxScene").append(xml3dData);
        $(newGroup).append(xml3dData);

        if (newLayer) {
            getTerrainElevationRefPoint();
            setCameraPosition();    
            newLayer = false;
        }
    }

    this.calculateCurrentPosLayerBlock = function(currentX, currentY){
        // console.log("calculateCurrentPosLayerBlock:currentX, currentY "+currentX, currentY);    
        currentX += LayerMinX; 
        currentY += LayerMinY;
        // console.log("calculateCurrentPosLayerBlock:currentX, currentY "+currentX, currentY);    
        // console.log("calculateCurrentPosLayerBlock:LayerMinX, LayerMinY "+LayerMinX, LayerMinY);
        var MinX, MinY, MaxX, MaxY;
        
        // X0 = parseInt(LayerMinX);
        X1 = parseInt(LayerMinX+blocklengthX);
        X2 = parseInt(LayerMinX+(2*blocklengthX));
        X3 = parseInt(LayerMinX+(3*blocklengthX));
        // Y0 = parseInt(LayerMinY);
        Y1 = parseInt(LayerMinY+blocklengthY);
        Y2 = parseInt(LayerMinY+(2*blocklengthY));
        Y3 = parseInt(LayerMinY+(3*blocklengthY));

        var col=0, row=0, MinX=0, MinY=0, MaxX=0, MaxY = 0;
        var offset = parseInt(blocklengthX/3);
        var transfromX=0, transfromY=0;
        // console.log("blocklenght X,Y "+blocklengthX, blocklengthY);
        // console.log("offset: "+offset);
        // console.log("currentX+offset: "+parseInt(currentX+offset));

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
            // layerBlockRow = row;
            // layerBlockCol = col;
            if (row>0){
                transfromX = row * blocklengthX
            }
            else {
                transfromX = blocklengthX
            }
            if (col>0){
                transfromY = col * blocklengthX
            }
            else {
                transfromY = blocklengthY
            }
            getElements(currentLayerName, MinX, MinY, MaxX, MaxY, currentLayerCRS, transfromX, transfromY );
            LayerblockArray[row][col] =1;
            console.log(LayerblockArray[0],LayerblockArray[1],LayerblockArray[2]);
        }
    }

 // Fetches layer details from GeoServer and passes them to getElements()-function
    this.getLayerDetails = function(layername) {
        var x = xmlDoc.getElementsByTagName("Layer");
        for (i=0;i<x.length;i++) { 
            if (layername == x[i].getElementsByTagName("Identifier")[0].childNodes[0].nodeValue) {
                // console.log(x[i].getElementsByTagName("Title")[0].childNodes[0].nodeValue);
                // console.log(x[i].getElementsByTagName("Identifier")[0].childNodes[0].nodeValue);
                console.log(x[i].getElementsByTagName("LowerCorner")[0].childNodes[0].nodeValue);
                console.log(x[i].getElementsByTagName("UpperCorner")[0].childNodes[0].nodeValue);
                // console.log(x[i].getElementsByTagName("OutputFormat")[0].childNodes[0].nodeValue);
                // console.log(x[i].getElementsByTagName("DefaultCRS")[0].childNodes[0].nodeValue);

                initSceneMngr(x[i].getElementsByTagName("Identifier")[0].childNodes[0].nodeValue,
                              x[i].getElementsByTagName("LowerCorner")[0].childNodes[0].nodeValue,
                              x[i].getElementsByTagName("UpperCorner")[0].childNodes[0].nodeValue,
                              x[i].getElementsByTagName("DefaultCRS")[0].childNodes[0].nodeValue
                              );

            }
        }
    }

}());


