// var newLayer = new Boolean();

(function() {
    // boolean to verify if new layer is loaded or new data fetched to already viewed layer
    var newlayer = true;

    var blocklengthX, blocklengthY = 0;

    //Array to track which blocks of the whole layer are loaded
    var LayerblockArray=[ [0,0,0],
                          [0,0,0],
                          [0,0,0] ];
    var layerBlockRow, layerBlockCol = 0;

    var screenHeight = $(document).height()-100;
    var screenWidth = $(document).width();

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

     // Fetches layer details from GeoServer and passes them to getElements()-function
    this.getLayerDetails = function(layername) {
        console.log("getLayerDetails");
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

    this.initSceneMngr = function(Identifier, LowerCorner, UpperCorner, DefaultCRS ){
        console.log("initSceneMngr");
        currentLayerName = Identifier;
        currentLayerCRS = DefaultCRS;
        console.log("initSceneMngr LowerCorner: "+LowerCorner+" UpperCorner: "+UpperCorner);

        //Store layer bounding box values for later usage
        var lowerCornerSplit = LowerCorner.split(" ");
        var higherCornerSplit = UpperCorner.split(" ");                
        LayerMinX = parseFloat(lowerCornerSplit[0]);
        LayerMinY = parseFloat(lowerCornerSplit[1]);
        LayerMaxX = parseFloat(higherCornerSplit[0]);
        LayerMaxY = parseFloat(higherCornerSplit[1]);
        console.log("minmax arvot BB "+LayerMinX, LayerMinY, LayerMaxX, LayerMaxY);

        var MinX, MinY, MaxX, MaxY;
        blocklengthX = parseFloat((LayerMaxX-LayerMinX)/3);
        blocklengthY = parseFloat((LayerMaxY-LayerMinY)/3);
        console.log("blocklenght X,Y "+blocklengthX, blocklengthY);

        MinX = LayerMinX;
        MinY = LayerMinY;
        MaxX = LayerMinX + blocklengthX;
        MaxY = LayerMinY + blocklengthY;
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
                    MinX, MinY, MaxX, MaxY, //custom size for layer min/max boundaries
                    // LayerMinX, LayerMinY, LayerMaxX, LayerMaxY, // Whole layer boundaries
                    DefaultCRS, 0, 0

        );
    }

    // Parses data for GeoServer GIS request
    // 
    // layerName = Layer Name
    // lowerCornerX, lowerCornerY, higherCornerX, higherCornerY = BoundingBox lower and higher corners, area for GIS request
    // layerCRS = Layer CRS
    function getElements(layerName, lowerCornerX, lowerCornerY, higherCornerX, higherCornerY, layerCRS, transfromX, transfromY){
        console.log("getElements");
        var baseUrl = "http://localhost:9090/geoserver/";
        // var texture_layer = "mml:UV41R_RVK_5";
        var texture_layer = "mml:pohjoisSuomi";
        var service = "w3ds";
        var version = "0.4.0";

        var xml3dobject = document.getElementById("xml3dContent");
        console.log("getElements transfromX, transfromY :"+ transfromX, transfromY);

        // var blocklengthX = parseInt((higherCornerX-lowerCornerX)/3);
        // var blocklengthY = parseInt((higherCornerY-lowerCornerY)/3);

        CamInitCenterX = parseFloat(lowerCornerX+(blocklengthX / 3));
        CamInitCenterY = parseFloat(lowerCornerY+(blocklengthY / 3));    
        
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

        


        var textureResolution = 1024
        var texture = "http://localhost:9090/geoserver/mml/wms?service=WMS&amp;version=1.1.0&amp;request=GetMap&amp;layers=" +
                                        texture_layer + 
                                        "&amp;styles=&amp;bbox=" + 
                                        lowerCornerX+","+
                                        lowerCornerY+","+
                                        higherCornerX+","+
                                        higherCornerY + 
                                        "&amp;width="+textureResolution+"&amp;height="+textureResolution+"&amp;srs=EPSG:404000&amp;format=image%2Fpng"        
        // addTextureToShader(texture);

        httpRequest(terrain, layerName, transfromX, transfromY, texture, addXml3DContent);
        
//         // Move camera to correct debugging position
//         var camera_node = document.getElementById("t_node-camera_player");

    // LayerblockArray[layerBlockRow][layerBlockCol] = 1;

    }

    function createGISRequest(baseUrl, layer, boundingbox, layerCRS) {
        console.log("createGISRequest");
        var requestUrl;
        var service = "w3ds?version=0.4&service=w3ds";
        var format = "&format=model/xml3d+xml";
        var crs = "&crs="+layerCRS;
        var request = "&request=GetScene";

        requestUrl = baseUrl + service + request + crs + format+"&layers="+layer+"&boundingbox="+boundingbox;
        console.log(requestUrl);
        return requestUrl;
    }

    function httpRequest(requestUrl, layerName, transfromX, transfromY, texture, callback) {
        console.log("httpRequest");
        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
        } else {
            xmlhttp = new XDomainRequest();
        }

        // Set callback function
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {                
                callback(xmlhttp.responseText, layerName, texture, transfromX, transfromY);
            }
        }

        xmlhttp.open("GET", requestUrl , true);
        xmlhttp.send();
    }

    // function addTextureToShader(shader, textureUrl) {
    //     console.log("addTextureToShader");
    //     var shader = document.getElementById("orangePhong");
    //     var str = "<texture name=\"diffuseTexture\">\n";
    //     str += "<img src=\"" + textureUrl + "\"/>\n" + "</texture>";

    //     $(shader).append(str);
    // }

    // parse XML3D object and fetch first elevation point to be used as reference elevation for camera
    function getTerrainElevationRefPoint(){
        console.log("getTerrainElevationRefPoint");
        var meshObjects = document.getElementsByTagName("mesh");
        //Get elev data from last node
        elevRefpoint = document.getElementsByTagName("mesh")[meshObjects.length-1].childNodes[1].value[10];
        console.log(meshObjects.length);
        console.log("getTerrainElevationRefPoint: "+ elevRefpoint);
        if (elevRefpoint > 0){
            currentTerrainElevRefPoint = elevRefpoint;
        }
        else {
            currentTerrainElevRefPoint = 0;
        }
        
    }

    // Used only when new layer is request
    function setCameraPosition(){
        console.log("setCameraPosition");
        // Move camera to correct debugging position
        var camera_node = document.getElementById("t_node-camera_player");

        // console.log("setCameraPosition"+layerCenterX+", "+layerCenterY);
        console.log("setCameraPosition: "+parseFloat(currentTerrainElevRefPoint+camHeightOffset));
        // camera_node.setAttribute( "translation", 
        //                            layerCenterX+" "+
        //                            (currentTerrainElevRefPoint+camHeightOffset)+" "+
        //                            layerCenterY);
        

        var camera_player = document.getElementById("camera_player-camera");
        // camera_player.setAttribute("orientation", "0.15 -0.99 -0.05 5.4");
        // camera_player.setAttribute("orientation", "-0.00065 1.0 0.01 3.0");
        camera_player.setAttribute("orientation", "0 -1 -0.11 2.6");
        camera_player.setAttribute("position", 
                                   "0 "+
                                   parseFloat(currentTerrainElevRefPoint+camHeightOffset)+" 0");
        // camera_player.setAttribute("position", "2800.548095703125 1144.894287109375 968.5448608398438");
        
        camPagedLoadCenterX = CamInitCenterX;
        camPagedLoadCenterY = CamInitCenterY;
    }

    function addXml3DContent(xml3dData, layerName, textureUrl, transfromX, transfromY) {
        console.log("addXml3DContent");
        // console.log(xml3dData);

         var newGroup = document.createElement('group');
         var IdName = layerName+transfromX+transfromY;
         var xmlnsTagContent = 'http://www.xml3d.org/2009/xml3d';
         newGroup.setAttribute('id',IdName);
         newGroup.setAttribute('xmlns',xmlnsTagContent);
         newGroup.setAttribute('shader','#'+IdName+'shader');
         newGroup.setAttribute('transform','#'+IdName+'transform');

         // Create layer specific shader
         var layerShader = document.createElement('shader');
         layerShader.setAttribute('id',IdName+'shader');
         layerShader.setAttribute('script','urn:xml3d:shader:phong');
         var layerFloat3 = document.createElement('float3');
         layerFloat3.setAttribute('name','diffuseColor');
         $(layerFloat3).append('1.0  1.0  1.0');
         $(layerShader).append(layerFloat3);
         var layerFloat = document.createElement('float');
         layerFloat.setAttribute('name','ambientIntensity');
         $(layerFloat).append('0.1')
         $(layerShader).append(layerFloat);
         var texture = "<texture name=\"diffuseTexture\">\n";
         texture += "<img src=\"" + textureUrl + "\"/>\n" + "</texture>";
         $(layerShader).append(texture);

         $("#defs").append(layerShader);

         var transformation = document.createElement('transform');
         transformation.setAttribute('id',IdName+"transform");
         transformation.setAttribute('rotation','0.0 0.0 0.0 0.0');
         // transformation.setAttribute('translation','0 0 0');
         transformation.setAttribute('translation',(transfromX*blocklengthX)+' 0 '+((-transfromY*blocklengthY)-200));

         $("#defs").append(transformation);

        $(newGroup).append(xml3dData);
        $("#MaxScene").append(newGroup);

        if (newLayer) {
            getTerrainElevationRefPoint();
            setCameraPosition(); 
            console.log("set newlayer to false: "+newlayer);   
            newLayer = false;
            console.log("set newlayer to false: "+newlayer);
        }
        console.log("set newlayer to false: "+newlayer);
    }

this.calculateCurrentPosLayerBlock = function(currentX, currentY){
        console.log("calculateCurrentPosLayerBlock");
        console.log("calculateCurrentPosLayerBlock:currentX, currentY "+currentX, currentY);    
       
        var MinX, MinY, MaxX, MaxY;

        //       X1|X2|X3 
        //     ------------
        //   Y3||31|32|33||
        //   Y2||21|22|23||
        //     |+--------||
        //   Y1||11|12|13||
        //     ------------
        //first block loaded is 11. Origo is upper left of the 1st block
        
        X1_0 = 0;
        X1_1 = parseFloat(blocklengthX);
        X2_1 = parseFloat(2*blocklengthX);
        X3_1 = parseFloat(3*blocklengthX);
        Y1_0 = 0;
        Y1_1 = parseFloat(-blocklengthY);
        Y2_1 = parseFloat(-(2*blocklengthY));
        Y3_1 = parseFloat(-(3*blocklengthY));

        var col=0, row=0, MinX=0, MinY=0, MaxX=0, MaxY = 0;
        var offsetX = parseFloat(blocklengthX/3);
        var offsetY = parseFloat(blocklengthY/3);
        var transfromX=0, transfromY=0;
        // console.log("blocklenght X,Y "+blocklengthX, blocklengthY);
        // console.log("offset: "+offset);
        // console.log("currentX+offset: "+parseInt(currentX+offset));
        console.log("currentY-offsetY: "+currentY-offsetY);

        if (currentX+offsetX < X1_1){
            console.log("currentX <= X1");
            col = 0;
            MinX = X1_0;
            MaxX = X1_1;            
        }else if (currentX+offsetX < X2_1){
            console.log("currentX <= X2");
            col = 1;
            MinX = X1_1;
            MaxX = X2_1;            
        }else if (currentX+offsetX < X3_1){
            console.log("currentX <= X3");
            col = 2;
            MinX = X2_1;
            MaxX = X3_1;            
        }

        if (currentY-blocklengthY > Y1_1){
            console.log("currentY <= Y1");
            row = 0;
            MinY = Y1_0;
            MaxY = Y1_1;
        }else if (currentY-blocklengthY > Y2_1){
            console.log("currentY <= Y2");
            row = 1;
            MinY = Y1_1;
            MaxY = Y2_1;
        }else if (currentY-blocklengthY > Y3_1){
            console.log("currentY <= Y3");
            row = 2;
            MinY = Y2_1;
            MaxY = Y3_1;
        }

        if (LayerblockArray[row][col] == 0){
            console.log("load new block. row:"+row+", col: "+col );   
            console.log("load new block. min:"+MinX + MinY+", Max: "+MaxX + MaxY ); 
            console.log("load new block. (Math.abs(MinY)+LayerMinY):"+(Math.abs(MinY)+LayerMinY) );
            // layerBlockRow = row;
            // layerBlockCol = col;
            // if (col>0){
            //     transfromX = col * blocklengthX
            // }
            // else {
            //     transfromX = blocklengthX
            // }
            // if (row>0){
            //     transfromY = row * blocklengthY
            // }
            // else {
            //     transfromY = blocklengthY
            // }
            getElements(currentLayerName,
                        MinX+LayerMinX, (Math.abs(MinY)+LayerMinY)+0,
                        MaxX+LayerMinX, (Math.abs(MaxY)+LayerMinY),
                        currentLayerCRS, col, row );
            LayerblockArray[row][col] =1;
            console.log(LayerblockArray[0],LayerblockArray[1],LayerblockArray[2]);
        }
    }


}());


