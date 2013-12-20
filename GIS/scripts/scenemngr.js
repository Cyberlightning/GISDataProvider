(function() {
    var baseUrl;
    // var texture_layer = "fiware:NorthernFinland_texture";
    var texture_layer = "fiware:pallas_iso_rasteri";
    // var texture_layer = "saana_rasteri";
    // var texture_layer = "fiware:V4132H_texture";
    // var texture_layer = "fiware:S5231B_mustavaara";

    // Array which contains all layer names which data is loaded
    var layerToBeLoaded = [];

    var blocklengthX, blocklengthY = 0;

    //has to track which blocks of the layers are loaded
    var LayerBlockHash = new Object();

    var screenHeight = $(document).height()-100;
    var screenWidth = $(document).width();

    // View radius which is used for defining how big area of the layer is fetched from the server
    var viewAreaRadius = 10000;
    var camHeightOffset = 1000;
    var currentTerrainElevRefPoint = 0;

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
    this.getLayerDetails = function(serverUrl, selectedLayerArray) {
        console.log("getLayerDetails(): "+serverUrl, selectedLayerArray);
        baseUrl = serverUrl;
        var x = xmlDoc.getElementsByTagNameNS("http://www.opengis.net/w3ds/0.4.0", "Layer");

        for (k=0; k<selectedLayerArray.length;k++){
            for (i=0;i<x.length;i++) {
                if (selectedLayerArray[k] === x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Title")[0].childNodes[0].nodeValue) {
                    // console.log(x[i].getElementsByTagName("Title")[0].childNodes[0].nodeValue);
                    // console.log(x[i].getElementsByTagName("Identifier")[0].childNodes[0].nodeValue);

                    // console.log(x[i].getElementsByTagName("OutputFormat")[0].childNodes[0].nodeValue);
                    // console.log(x[i].getElementsByTagName("DefaultCRS")[0].childNodes[0].nodeValue);

                    initSceneMngr(x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Identifier")[0].childNodes[0].nodeValue,
                                  x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "LowerCorner")[0].childNodes[0].nodeValue,
                                  x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "UpperCorner")[0].childNodes[0].nodeValue,
                                  x[i].getElementsByTagNameNS("http://www.opengis.net/w3ds/0.4.0", "DefaultCRS")[0].childNodes[0].nodeValue
                                  );
                    layerToBeLoaded.push(x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Identifier")[0].childNodes[0].nodeValue);
                }
            }
        }            
    }

    function initLayerBlockArray(){
        console.log("initLayerBlockArray()");
        var twoDimArray =[];
        for (var i=0;i<5;i++){
            var data = [];
            for (var j=0;j<5;j++){
                data.push(0);
            }
            twoDimArray.push(data);
        }
        // console.log(twoDimArray);

        //Set first block as 1 because it's data will be always downloaded during initialization
        twoDimArray[0][0]=1;
        return twoDimArray;
    }

    // Function checks if the layerblock is already loaded. 
    // Checking is done based on the grid, if grid value is '0' it means that block is not yet loaded.
    function checkIfLayerBlockIsLoaded(layername, row, col){
        // console.log("checkIfLayerBlockIsLoaded()"+layername, row, col);
        for (var k in LayerBlockHash) {
            // use hasOwnProperty to filter out keys from the Object.prototype
            if (LayerBlockHash.hasOwnProperty(k)) {
                if (k===layername){
                    if(LayerBlockHash[k][row][col]===0){
                        console.log("checkIfLayerBlockIsLoaded: block is NOT loaded ");
                        LayerBlockHash[k][row][col]=1;
                        return false;
                        break;
                    }else{
                        // console.log("checkIfLayerBlockIsLoaded: block IS loaded ");
                        return true;
                        break;
                    }
                }
            }
        }
    }

    this.initSceneMngr = function(Identifier, LowerCorner, UpperCorner, DefaultCRS ){
        console.log("initSceneMngr");
        currentLayerName = Identifier;
        currentLayerCRS = DefaultCRS;
        // console.log("initSceneMngr LowerCorner: "+LowerCorner+" UpperCorner: "+UpperCorner);
        // console.log("initSceneMngr,LayerBlockHash: "+ LayerBlockHash);

        // Check if layerblockhash for the layer already exists.
        var LayerblockHashDoesntExist = new Boolean();
        LayerblockHashDoesntExist = true;
        console.log(LayerblockHashDoesntExist);
        for (var k in LayerBlockHash) {
        // use hasOwnProperty to filter out keys from the Object.prototype
            if (LayerBlockHash.hasOwnProperty(k)) {
                console.log("initSceneMngr: "+k);
                if (k===Identifier){
                    console.log('löytyi jo olemassa oleva layerblock!!!');
                    LayerblockHashDoesntExist = false;
                }
            }
        }

        if (LayerblockHashDoesntExist){
            console.log("initSceneMngr: create NEW LayerBlockHash object");
            LayerBlockHash[Identifier] = initLayerBlockArray();  
        }else{
            console.log("initSceneMngr: DONT create new LayerBlockHash object");
        }

        //Store layer bounding box values for later usage
        var lowerCornerSplit = LowerCorner.split(" ");
        var higherCornerSplit = UpperCorner.split(" ");                
        LayerMinX = parseFloat(lowerCornerSplit[0]);
        LayerMinY = parseFloat(lowerCornerSplit[1]);
        LayerMaxX = parseFloat(higherCornerSplit[0]);
        LayerMaxY = parseFloat(higherCornerSplit[1]);
        console.log("minmax arvot BB "+LayerMinX, LayerMinY, LayerMaxX, LayerMaxY);

        var MinX, MinY, MaxX, MaxY;
        blocklengthX = parseFloat((LayerMaxX-LayerMinX)/5);
        blocklengthY = parseFloat((LayerMaxY-LayerMinY)/5);
        console.log("blocklenght X,Y "+blocklengthX, blocklengthY);

        MinX = LayerMinX;
        MinY = LayerMinY;
        MaxX = LayerMinX + blocklengthX;
        MaxY = LayerMinY + blocklengthY;

        getElements(Identifier,
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
    function getElements(layerName, lowerCornerX, lowerCornerY, higherCornerX, higherCornerY, layerCRS, transformX, transformY){
        console.log("getElements(): "+layerName, lowerCornerX, lowerCornerY, higherCornerX, higherCornerY, layerCRS, transformX, transformY);

        var service = "w3ds";
        var version = "0.4.0";

        var xml3dobject = document.getElementById("xml3dContent");
        console.log("getElements transformX, transformY :"+ transformX, transformY);

        // var blocklengthX = parseInt((higherCornerX-lowerCornerX)/3);
        // var blocklengthY = parseInt((higherCornerY-lowerCornerY)/3);

        CamInitCenterX = parseFloat(lowerCornerX+(blocklengthX / 5));
        CamInitCenterY = parseFloat(lowerCornerY+(blocklengthY / 5));    
        
        xml3dobject.setAttribute("width", screenWidth);
        xml3dobject.setAttribute("height", screenHeight);
        
        console.log("getElements: "+lowerCornerX+", "+lowerCornerY+", "+higherCornerX+", "+higherCornerY);
        var xml3drequest = createGISRequest( baseUrl, 
                                        layerName, 
                                        lowerCornerX+","+
                                        lowerCornerY+","+
                                        higherCornerX+","+
                                        higherCornerY,
                                        layerCRS);

        // external xml files contains all needed info, also textures. 
        // With other layers, e.g. terrain textures needs to be downloaded separately
        if (layerName !== "fiware:building_coordinates"){
            var textureResolution = 128
            var texture = baseUrl+"fiware/wms?service=WMS&amp;version=1.1.0&amp;request=GetMap&amp;layers=" +
                                    texture_layer + 
                                    "&amp;styles=&amp;bbox=" + 
                                    lowerCornerX+","+
                                    lowerCornerY+","+
                                    higherCornerX+","+
                                    higherCornerY+ 
                                    "&amp;width="+textureResolution+"&amp;height="+textureResolution+"&amp;srs=EPSG:3067&amp;format=image%2Fpng"
                                    // "&amp;width="+textureResolution+"&amp;height="+textureResolution+"&amp;srs=EPSG:404000&amp;format=image%2Fpng";

            httpRequest(xml3drequest, layerName, transformX, transformY, texture, addXml3DContent);
        }else{
            console.log("buildings");
            httpRequest3dObjects(xml3drequest, layerName, transformX, transformY, parseMeshSrc);
        }

        

    }

    function createGISRequest(baseUrl, layer, boundingbox, layerCRS) {
        console.log("createGISRequest");
        var requestUrl;
        var service = "w3ds?version=0.4&service=w3ds";
        
        var format = "&format=model/xml3d+xml";
        //var format = "&format=application/octet-stream";

        var crs = "&crs="+layerCRS;
        var request = "&request=GetScene";

        requestUrl = baseUrl + service + request + crs + format+"&layers="+layer+"&boundingbox="+boundingbox;
        console.log(requestUrl);
        return requestUrl;
    }

    function httpRequest(requestUrl, layerName, transformX, transformY, texture, callback) {
        console.log("httpRequest(): "+requestUrl, layerName, transformX, transformY, texture);
        startSpinner();

        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
        } else {
            xmlhttp = new XDomainRequest();
        }

        // Set callback function
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {       
                if (callback === "parseMeshSrc"){
                    // REMOVE; THIS DIDN'T WORK
                    callback(xmlhttp.responseText, transformX, transformY);
                }else{
                    callback(xmlhttp.responseText, layerName, texture, transformX, transformY);
                }
            }
        }
        xmlhttp.open("GET", requestUrl , true);
        xmlhttp.send();
    }
        function httpRequest3dObjects(requestUrl, layerName, transformX, transformY, callback) {
        console.log("httpRequest3dObjects(): "+requestUrl, layerName, transformX, transformY);
        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
        } else {
            xmlhttp = new XDomainRequest();
        }

        // Set callback function
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {       
                callback(xmlhttp.responseText, transformX, transformY);
            }
        }
        xmlhttp.open("GET", requestUrl , true);
        xmlhttp.send();
    }

    function parseMeshSrc(xml3dData, transformX, transformY){
            console.log("parseMeshSrc(xml3dData): transformX, transformY: "+transformX, transformY);
            console.log(xml3dData);
            var style, meshSrc;
        if ($(xml3dData).find("mesh").attr("src")!= undefined){
            
            startSpinner();

            meshSrc = $(xml3dData).find("mesh").attr("src");
            console.log("mesh src found: "+$(xml3dData).find("mesh").attr("src"));
            translation = $(xml3dData).attr('translation');
            console.log('parseMeshSrc:translation: '+translation);
            console.log('parseMeshSrc:blocklengthX & blocklengthY: '+blocklengthX, blocklengthY);

            //HOX: change translation according to used grid
            split = translation.split(' ');
            console.log(split[0],split[1],split[2]);
            if (transformX>0){
                split[0] = parseFloat(split[0]) + parseFloat(transformX*blocklengthX);
            }
            if(transformY>0){
                split[2] = (split[2] + parseFloat(transformY*blocklengthY));
            }else{
                split[2] = Math.abs(split[2]);
            }
            console.log(split[0],split[1],split[2]);
            translation = split[0]+" "+split[1]+" "+(split[2]);
            console.log(translation);

            var xmlhttp;
            if (window.XMLHttpRequest) {
                xmlhttp = new XMLHttpRequest();
            } else {
                xmlhttp = new XDomainRequest();
            }

            // remove spaces from the url
            meshSrc=meshSrc.replace(/\s+/g, '');

             xmlhttp.onreadystatechange = function() {
                if (xmlhttp.readyState==4 && xmlhttp.status==200) {
                    var meshNameArray = [];
                    $.get(meshSrc, function(xml){
                        $('data', xml).each(function(i){
                            console.log($(this).attr('id'));
                            var meshName = $(this).attr('id');
                            if (meshName && meshName.indexOf('submesh')>=0){
                                console.log(meshName.indexOf('submesh'));                    
                                meshNameArray.push(meshSrc+"#"+meshName);
                            }
                        });
                    console.log(meshNameArray);
                    addMeshtoHtml(meshNameArray, translation);
                    });
                }
            }
            xmlhttp.open("GET",meshSrc,false);
            xmlhttp.send();
        }
    }


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
        // console.log("setCameraPosition: "+parseFloat(currentTerrainElevRefPoint+camHeightOffset));

        var camera_player = document.getElementById("camera_player-camera");
        camera_player.setAttribute("orientation", "0 -1 -0.11 2.6");
        camera_player.setAttribute("position", 
                                   "0 "+
                                   parseFloat(currentTerrainElevRefPoint+camHeightOffset)+" 0");
    }

    function addMeshtoHtml(meshSrcArray, translation){
        console.log("addMeshtoHtml:  "+meshSrcArray);
        var IdName = "foo"+Math.floor(Math.random()*111);

        var transformation = "<transform id=\""+IdName+"transform"+"\" rotation=\"0.0 0.0 0.0 0.0\" translation=\""+translation+"\"></transform>";
        console.log(transformation);
        $("#defs").append(transformation);
        
        var newGroup = "<group id=\""+IdName+"\" shader=\"#phong\" transform=\"#"+IdName+"transform\">";

        for (i=0;i<meshSrcArray.length;i++){
            var meshString = "<mesh src=\""+meshSrcArray[i]+"\"/>";
            newGroup+=meshString;
        }
        newGroup+=("</group>");
        // console.log(newGroup);

        $("#MaxScene").append(newGroup); 

        // setCameraPosition();
        stopSpinner();
    }

    function addXml3DContent(xml3dData, layerName, textureUrl, transformX, transformY) {
        console.log("addXml3DContent");
        //console.log(xml3dData);

        var newGroup = document.createElement('group');
        var IdName = layerName+transformX+transformY;
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

        $('#defs').append(layerShader);

        var transformation = document.createElement('transform');
        transformation.setAttribute('id',IdName+"transform");
        transformation.setAttribute('rotation','0.0 0.0 0.0 0.0');
        // transformation.setAttribute('translation','0 0 0');
        transformation.setAttribute('translation',(transformX*blocklengthX)+' 0 '+((-transformY*blocklengthY)));

        $("#defs").append(transformation);

        $(newGroup).append(xml3dData);
        $("#MaxScene").append(newGroup);

        if (newLayer) {
            // getTerrainElevationRefPoint();
            setCameraPosition(); 
            newLayer = false;
        }
        stopSpinner();
    }
    

this.calculateCurrentPosLayerBlock = function(currentX, currentY){
        // console.log("calculateCurrentPosLayerBlock");
        // console.log("calculateCurrentPosLayerBlock:currentX, currentY "+currentX, currentY);    
       
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
        X4_1 = parseFloat(4*blocklengthX);
        X5_1 = parseFloat(5*blocklengthX);
        Y1_0 = 0;
        Y1_1 = parseFloat(-blocklengthY);
        Y2_1 = parseFloat(-(2*blocklengthY));
        Y3_1 = parseFloat(-(3*blocklengthY));
        Y4_1 = parseFloat(-(4*blocklengthY));
        Y5_1 = parseFloat(-(5*blocklengthY));

        var col=0, row=0, MinX=0, MinY=0, MaxX=0, MaxY = 0;
        var offsetX = parseFloat(blocklengthX/5);
        var offsetY = parseFloat(blocklengthY/5);
        // var transformX=0, transformY=0;
        // console.log("blocklenght X,Y "+blocklengthX, blocklengthY);
        // console.log("offset: "+offset);
        // console.log("currentX+offset: "+parseInt(currentX+offset));
        // console.log("currentY-offsetY: "+currentY-offsetY);

        if (currentX+offsetX < X1_1){
            // console.log("currentX <= X1");
            col = 0;
            MinX = X1_0;
            MaxX = X1_1;            
        }else if (currentX+offsetX < X2_1){
            // console.log("currentX <= X2");
            col = 1;
            MinX = X1_1;
            MaxX = X2_1;            
        }else if (currentX+offsetX < X3_1){
            // console.log("currentX <= X3");
            col = 2;
            MinX = X2_1;
            MaxX = X3_1;            
        }else if (currentX+offsetX < X4_1){
            // console.log("currentX <= X3");
            col = 3;
            MinX = X3_1;
            MaxX = X4_1;            
        }
        else if (currentX+offsetX < X5_1){
            // console.log("currentX <= X3");
            col = 4;
            MinX = X4_1;
            MaxX = X5_1;            
        }

        if (currentY-blocklengthY > Y1_1){
            // console.log("currentY <= Y1");
            row = 0;
            MinY = Y1_0;
            MaxY = Y1_1;
        }else if (currentY-blocklengthY > Y2_1){
            // console.log("currentY <= Y2");
            row = 1;
            MinY = Y1_1;
            MaxY = Y2_1;
        }else if (currentY-blocklengthY > Y3_1){
            // console.log("currentY <= Y3");
            row = 2;
            MinY = Y2_1;
            MaxY = Y3_1;
        } else if (currentY-blocklengthY > Y4_1){
            // console.log("currentY <= Y3");
            row = 3;
            MinY = Y3_1;
            MaxY = Y4_1;
        }else if (currentY-blocklengthY > Y5_1){
            // console.log("currentY <= Y3");
            row = 4;
            MinY = Y4_1;
            MaxY = Y5_1;
        }

        // checkIfLayerBlockIsLoaded(currentLayerName, row, col);

        // if (LayerblockArray[row][col] == 0){
        for (i=0; i<layerToBeLoaded.length;i++){
            if (checkIfLayerBlockIsLoaded(layerToBeLoaded[i], row, col)===false){    
                console.log("load new block. row:"+row+", col: "+col );   
                console.log("load new block. min:"+MinX+":"+MinY+", Max: "+MaxX+":"+MaxY ); 
                console.log("load new block. (Math.abs(MinY)+LayerMinY):"+(Math.abs(MinY)+LayerMinY) );

                getElements(layerToBeLoaded[i],
                            MinX+LayerMinX, (Math.abs(MinY)+LayerMinY)+0,
                            MaxX+LayerMinX, (Math.abs(MaxY)+LayerMinY),
                            currentLayerCRS, col, row );
            }else{
                // console.log('dont load new block' );
            }    
        }
        
    }

}());


