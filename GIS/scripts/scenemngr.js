(function() {
    // var baseUrl;
    var TerrainTextureName = null;
    var TerrainTextureCRS = null;

    // Array which contains all layer names which data is loaded.
    // NOTE: Terrain layer needs to be 1st layer in the list, because 1st layer bounding box defines min & max area for bounding box
    var layerToBeLoaded = [];

    // Variables where each block dimensions are stored in the grid. 
    // NOTE: Terrain bounding box defines maximun and minimun values for area,
    // objects can be fetched only within max&min area.
    // var blocklengthX, blocklengthY = 0;

    // Amount of the grid blocks for dividing layer, this is automatically calculated based on the layer size
    var layerBlockGridsplit = 10;

    // Margin to identify how many border blocks to each direction is loaded. "-1" means that only neighbour blocks for center block are loaded.
    // 2 means that neighbours for neighbours of center block are loaeded etc.
    var borderBlockmargin = 2;
    var offset = 15; // Offset value to be used when distance blocks are removed from the view
    var textureResolution = 256;
    var terrainTextureCRS = 0;
    var LodLevel = 1;

    //has to track which blocks of the layers are loaded
    var LayerBlockHash = new Object();

    var preferredBlockLength = 500;
    var dynamicLayerBlockHash = new Object();
    var dynBlockSideLenghtX = 0;
    var dynBlockSideLenghtY = 0;

    var screenHeight = $(document).height();
    var screenWidth = $(document).width();

    var camHeightOffset = 1000;
    var camCenterX = 0;
    var camCenterY = 0;
    var currentTerrainElevRefPoint = 0;

    var centerBlockRow = null;
    var centerBlockCol = null;


    // Currently loaded layer bounding box min and max values    
    var LayerMinX = null;
    var LayerMinY = null;
    var LayerMaxX = null;
    var LayerMaxY = null;


    // Array where whole layer blocks are stored. block object is "layerBlock".
    var terrainGridArray = [];

    // block definition for storing each block's min-max coordinate value
    function layerBlock(){
        this.MinX = 0;
        this.MinY = 0;
        this.MaxX = 0;
        this.MaxY = 0;

        this.blockID = null;
        this.GridRow = null;
        this.GridCol = null;
        // following variables are used to store original coordinates of data
        this.crsMinX = 0;
        this.crsMinY = 0;
        this.crsMaxX = 0;
        this.crsMaxY = 0;
        this.blockVisible = false;
    }

    this.setTextureResolution = function(resolution){
        textureResolution = resolution;
    }

    this.setLODlevel = function(LODlevel){
        LodLevel = LODlevel;
    }

    this.setTextureInfo = function(texture, textureCRS){
        TerrainTextureName = texture;
        TerrainTextureCRS = textureCRS;
    }

    // "preferredBlockLength" defines lenght of the each block. 
    // Layer size is divided by this to get similar look and feel with different layer sizes
    function calculateGridLevel(){
        layerBlockGridsplit = ((parseFloat(LayerMaxX) - parseFloat(LayerMinX)) / preferredBlockLength).toFixed(0);
    }

    // Initialization function which divides layer in the small gridset
    function initTileMap(){
        dynBlockSideLenghtX = (parseFloat(LayerMaxX) - parseFloat(LayerMinX)) / layerBlockGridsplit;
        dynBlockSideLenghtY = (parseFloat(LayerMaxY) - parseFloat(LayerMinY)) / layerBlockGridsplit;

        console.log(dynBlockSideLenghtX, dynBlockSideLenghtY);
        console.log(LayerMinX, LayerMinY, LayerMaxX, LayerMaxY);

        for (row = 0; row<layerBlockGridsplit; row++){
            var colArray = [];
            for(col = 0; col<layerBlockGridsplit; col++){
                var block = new layerBlock();
                block.MinX = parseFloat((dynBlockSideLenghtX * col));
                block.MaxX = parseFloat((dynBlockSideLenghtX * (col + 1)));
                block.MinY = parseFloat(- (dynBlockSideLenghtY * row));
                block.MaxY = parseFloat(- (dynBlockSideLenghtY * (row + 1 )));
        
                block.crsMinX = parseFloat(LayerMinX) + parseFloat((dynBlockSideLenghtX * col));
                block.crsMaxX = parseFloat(LayerMinX) + parseFloat((dynBlockSideLenghtX * (col + 1)));
                block.crsMinY = parseFloat(LayerMinY) + parseFloat((dynBlockSideLenghtY * row));
                block.crsMaxY = parseFloat(LayerMinY) + parseFloat((dynBlockSideLenghtY * (row + 1 )));
                block.GridRow = row;
                block.GridCol = col;
                block.blockID = "Row"+row+"_col"+col;
                colArray.push(block);
            }
            terrainGridArray.push(colArray);
        }
        console.log(terrainGridArray);


    }

     // Fetches layer details from GeoServer and passes them to getElements()-function
    this.getLayerDetails = function(selectedLayer, selectedObjectLayers) {
        if (DEBUG){
            console.log("getLayerDetails(): "+selectedLayer, selectedObjectLayers);
        }

        var x = xmlDocW3DS.getElementsByTagNameNS("http://www.opengis.net/w3ds/0.4.0", "Layer");

        // First get details of the terrain layer
        for (i=0;i<x.length;i++) {
            if (selectedLayer === x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Identifier")[0].childNodes[0].nodeValue) {
                console.log(selectedLayer);
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
            
        // then get details of the layers which contains 3D object locations
        for (k=0; k<selectedObjectLayers.length;k++){
            for (i=0;i<x.length;i++) {
                console.log("selectedObjectLayers--- "+i);
                if (selectedObjectLayers[k] === x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Title")[0].childNodes[0].nodeValue) {
                    console.log("Add object layer "+selectedObjectLayers[k]);
                    initSceneMngr(x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Identifier")[0].childNodes[0].nodeValue,
                                  x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "LowerCorner")[0].childNodes[0].nodeValue,
                                  x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "UpperCorner")[0].childNodes[0].nodeValue,
                                  x[i].getElementsByTagNameNS("http://www.opengis.net/w3ds/0.4.0", "DefaultCRS")[0].childNodes[0].nodeValue
                                  );
                    layerToBeLoaded.push(x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Identifier")[0].childNodes[0].nodeValue);
                }
            }
        }

        calculateGridLevel();
        initTileMap();
        
        // TESTING, DOWNLOAD WHOLE LAYER ----------------->>>>>>>
        // for (row = 0; row<layerBlockGridsplit; row++){
        //     for(col = 0; col<layerBlockGridsplit; col++){
        //         getElements(layerToBeLoaded[0], row, col, LayerBlockHash[layerToBeLoaded[0]+"_CRS"]);
        //     }
        // }

        searchLocation();

        // createLayerGuideBlock();
    }
    this.searchLocation = function(coordX, coordY) {
        // var searchCol = 261149;
        // var searchRow = 7691020;
        var searchCol = null;
        var searchRow = null;
        if ((coordX===undefined)||coordY===undefined){
            // Set latitude based on selected layer
            if (layerToBeLoaded[0] === "fiware:postgis_oulu_terrain"){
                searchCol = 427982; //Oulu
                searchRow = 7210832; //Oulu
            } else if (layerToBeLoaded[0] === "fiware:postgis_finland"){
                searchCol = 376019.90625; //Pallas
                searchRow = 7549175.2890625; //Pallas
            } else if (layerToBeLoaded[0] === "fiware:postgis_helsinki_terrain"){
                searchCol = 385866; //Helsinki
                searchRow = 6672196; //Helsinki
            } else{
                // calculate camera position automatically
                searchCol = parseFloat((parseFloat(LayerMaxX)-parseFloat(LayerMinX))/2);
                searchRow = parseFloat((parseFloat(LayerMaxY)-parseFloat(LayerMinY))/2);
            }
           
       // coordX & coordY got, which means that function is launced via camera movement
        } else{
            searchRow = parseFloat(Math.abs(coordY)) + parseFloat(LayerMinY);
            searchCol = parseFloat(coordX) + parseFloat(LayerMinX);
        }

        
        console.log("dynBlockSideLenghtX: "+dynBlockSideLenghtX);
        var row = 0;
        var col = 0;

        for(i=(parseFloat(LayerMinX)+parseFloat(dynBlockSideLenghtX));i<searchCol;i+=parseFloat(dynBlockSideLenghtX)){
            // console.log("searchCol, i: "+col+" "+i);
            col +=1;
        }


        for(j=(parseFloat(LayerMinY)+parseFloat(dynBlockSideLenghtY));j<searchRow;j+=parseFloat(dynBlockSideLenghtY)){
            // console.log("searchRow, j: "+row+" "+j);
            row +=1;
        }
        //REMOVE ====>
        if ( camCenterY === 0){
            camCenterY = terrainGridArray[row][col].MinY;
            camCenterX = terrainGridArray[row][col].MinX;
        }
        
        //REMOVE <====

        if (!terrainGridArray[row][col].blockVisible){
            for (i=0;i<layerToBeLoaded.length;i++){
                getElements(layerToBeLoaded[i], (row), (col), LayerBlockHash[layerToBeLoaded[i]+"_CRS"]);
                terrainGridArray[row][col].blockVisible = true;
            }
        }
        checkBorderBlockVisibility(row, col);

        // centerBlockRow = row;
        // centerBlockCol = col;

        freeMemory(row, col);
        
    }
    function checkBorderBlockVisibility(row, col){
        var layerToBeLoadedLength = layerToBeLoaded.length;
        for (i=-borderBlockmargin;i<=borderBlockmargin;i++){
            for (k=-borderBlockmargin;k<=borderBlockmargin;k++){
                try{
                    if (!terrainGridArray[row-i][col-k].blockVisible){
                        for (z=0;z<layerToBeLoadedLength;z++){
                            getElements(layerToBeLoaded[z], (row-i), (col-k), LayerBlockHash[layerToBeLoaded[z]+"_CRS"]);
                            terrainGridArray[row-i][col-k].blockVisible = true;
                        }
                    }
                }
                catch(err){
                    //Handle errors here
                }        
            }
        }
    }

    function freeMemory(row, col){
        
        length = terrainGridArray.length-1;
        
        // TODO: optimization, it is not neseccary to go thru both top and down
        // first remove top row
        if ((row + offset)<terrainGridArray.length){
            var checkRow = row+offset;
            for (i=length;i>=0;i--){
                if (terrainGridArray[checkRow][i].blockVisible){
                    terrainGridArray[checkRow][i].blockVisible = false;

                    $('#'+terrainGridArray[checkRow][i].blockID+'shader').remove();
                    $('#'+terrainGridArray[checkRow][i].blockID).remove();
                    $('#'+terrainGridArray[checkRow][i].blockID+'transform').remove();
                }
            }      
        }  

        // then remove bottom row
        if (row >= offset){
            var checkRow = row-offset;
            for (i=length;i>=0;i--){
                if (terrainGridArray[checkRow][i].blockVisible){
                    terrainGridArray[checkRow][i].blockVisible = false;

                    $('#'+terrainGridArray[checkRow][i].blockID+'shader').remove();
                    $('#'+terrainGridArray[checkRow][i].blockID).remove();
                    $('#'+terrainGridArray[checkRow][i].blockID+'transform').remove();
                }
            }
        }

        // And then remove distance blocks from the visible block rows. Offset value used to evaluate if block should be displayed or not
        
            for (checkRow=row+offset;checkRow>=row-offset;checkRow--){
                for (checkCol=col-offset; checkCol>=0; checkCol--){
                    try{
                        if (terrainGridArray[checkRow][checkCol].blockVisible){
                            terrainGridArray[checkRow][checkCol].blockVisible = false;

                            $('#'+terrainGridArray[checkRow][checkCol].blockID+'shader').remove();
                            $('#'+terrainGridArray[checkRow][checkCol].blockID).remove();
                            $('#'+terrainGridArray[checkRow][checkCol].blockID+'transform').remove();
                        }
                    }
                    catch(err){
                        //Handle errors here
                    }        
                }                
                for (checkCol=terrainGridArray.length; checkCol>col+offset; checkCol--){
                    try{
                        if (terrainGridArray[checkRow][checkCol].blockVisible){
                            terrainGridArray[checkRow][checkCol].blockVisible = false;

                            $('#'+terrainGridArray[checkRow][checkCol].blockID+'shader').remove();
                            $('#'+terrainGridArray[checkRow][checkCol].blockID).remove();
                            $('#'+terrainGridArray[checkRow][checkCol].blockID+'transform').remove();
                        }
                    }
                    catch(err){
                        //Handle errors here
                    }        
                }                
            }


    }

    // Creates square below terrain to give guidance what is the maximum area for the terrain to be loaded
    function createLayerGuideBlock(){
        var transform = "<transform id=\"layerguideTransform\" rotation=\"0.0 0.0 0.0 0.0\" translation=\"0 0 0\"></transform>"
        $("#defs").append(transform);

        var layerBorder = "<group id=\"layerguide\" xmlns=\"http://www.xml3d.org/2009/xml3d\" shader=\"#phong\"  transform=\"#layerguideTransform\">";

        layerBorder += "<mesh type=\"triangles\" transform=\"#layerguideTransform\"> <int name=\"index\">0 1 2  1 2 3</int>";
        // layerBorder += "<float3 name=\"position\"> 0 20 0 "+(blocklengthX*layerBlockGridsplit)+" 20 0 0 20 "+(-(blocklengthY*layerBlockGridsplit))+" "+(blocklengthX*layerBlockGridsplit)+" 20 "+(-(blocklengthY*layerBlockGridsplit))+"</float3>";
        layerBorder += "<float3 name=\"position\"> 0 20 0 "+(dynBlockSideLenghtX*layerBlockGridsplit)+" 20 0 0 20 "+(-(dynBlockSideLenghtY*layerBlockGridsplit))+" "+(dynBlockSideLenghtX*layerBlockGridsplit)+" 20 "+(-(dynBlockSideLenghtY*layerBlockGridsplit))+"</float3>";
        layerBorder += "<float3 name=\"normal\">0 0 1  0 0 1  0 0 1  0 0 1</float3>";
        layerBorder += "<float2 name=\"texcoord\">0.0 0.0 1.0 0.0 0.0 1.0 1.0 1.0</float2>";
        layerBorder += "</mesh></group>";
        $("#xml3dContent").append(layerBorder);


    }

    

    function initLayerBlockArray(){
        // console.log("initLayerBlockArray()");
        var twoDimArray =[];
        for (var i=0;i<layerBlockGridsplit;i++){
            var data = [];
            for (var j=0;j<layerBlockGridsplit;j++){
                data.push(0);
            }
            twoDimArray.push(data);
        }
        // console.log(twoDimArray);

        //Set first block as 1 because it's data will be always downloaded during initialization
        twoDimArray[0][0]=1;
        return twoDimArray;
    }


    this.initSceneMngr = function(Identifier, LowerCorner, UpperCorner, DefaultCRS ){
        // console.log("Identifier, LowerCorner, UpperCorner, DefaultCRS: "+Identifier, LowerCorner, UpperCorner, DefaultCRS);

        var xml3dobject = document.getElementById("xml3dContent");
        xml3dobject.setAttribute("width", screenWidth);
        xml3dobject.setAttribute("height", screenHeight);


        // Check if layerblockhash for the layer already exists.
        var LayerblockHashDoesntExist = new Boolean();
        LayerblockHashDoesntExist = true;

        for (var k in LayerBlockHash) {
            if (LayerBlockHash.hasOwnProperty(k)) {
                console.log("initSceneMngr: "+k);
                if (k===Identifier){
                    // console.log('There is already layerBlockhash for terrain');
                    LayerblockHashDoesntExist = false;
                }
            }
        }

        if (LayerblockHashDoesntExist){
            // console.log("initSceneMngr: create NEW LayerBlockHash object");
            LayerBlockHash[Identifier] = initLayerBlockArray();  
        }else{
            // console.log("initSceneMngr: DONT create new LayerBlockHash object");
        }

        //Store layer CRS for later use
        LayerBlockHash[Identifier+"_CRS"] = DefaultCRS;
        // console.log(LayerBlockHash);

        // Check each layer bounding box (BB) values. 
        // Purpose is to adjust scene BB area during initializing phase so that scene BB covers all loaded layers area.
        var lowerCornerSplit = LowerCorner.split(" ");
        var higherCornerSplit = UpperCorner.split(" ");                
        if (LayerMinX > parseFloat(lowerCornerSplit[0]) || LayerMinX===null ){
            LayerMinX = parseFloat(lowerCornerSplit[0]);
        }
        if (LayerMinY > parseFloat(lowerCornerSplit[1]) || LayerMinY===null){
            LayerMinY = parseFloat(lowerCornerSplit[1]);
        }
        if (LayerMaxX < parseFloat(higherCornerSplit[0]) || LayerMaxX ===null){
            LayerMaxX = parseFloat(higherCornerSplit[0]);
        }
        if (LayerMaxY < parseFloat(higherCornerSplit[1]) || LayerMaxX ===null){
            LayerMaxY = parseFloat(higherCornerSplit[1]);
        }
        // console.log("minmax arvot BB "+LayerMinX, LayerMinY, LayerMaxX, LayerMaxY);

        // blocklengthX = parseFloat((LayerMaxX-LayerMinX)/layerBlockGridsplit);
        // blocklengthY = parseFloat((LayerMaxY-LayerMinY)/layerBlockGridsplit);
    }


    function getElements(layerName, row, col, layerCRS){
        if (DEBUG){
            console.log("getElements(layerName, row, col, layerCRS): "+layerName, row, col, layerCRS);
        }

        var imageFormat = "jpeg";

        var xml3drequest = createGISRequest( baseUrl, 
                                            layerName, 
                                            terrainGridArray[row][col].crsMinX+","+
                                            terrainGridArray[row][col].crsMinY+","+
                                            terrainGridArray[row][col].crsMaxX+","+
                                            terrainGridArray[row][col].crsMaxY,
                                            layerCRS);

        // external xml files contains all needed info, also textures. 
        // With other layers, e.g. terrain textures needs to be downloaded separately
        if ((TerrainTextureName != null) && (TerrainTextureCRS != null)){
            var texture = baseUrl+"fiware/wms?service=WMS&amp;version=1.1.0&amp;request=GetMap&amp;layers=" +
                                TerrainTextureName + 
                                "&amp;styles=&amp;bbox=" + 
                                terrainGridArray[row][col].crsMinX+","+
                                terrainGridArray[row][col].crsMinY+","+
                                terrainGridArray[row][col].crsMaxX+","+
                                terrainGridArray[row][col].crsMaxY+ 
                                "&amp;width="+textureResolution+"&amp;height="+textureResolution+"&amp;srs="+TerrainTextureCRS+"&amp;format=image%2F"+imageFormat;

            

        }
        // Assumption is that if layer contains external 3D object references, layer name contains "building_coordinates".
        if (layerName.indexOf("building_coordinates")>=0){
            httpRequest3dObjects(xml3drequest, layerName, row, col, parseMeshSrc);
        }else{
            httpRequest_dynamicGrid(xml3drequest, layerName, row, col, texture, addXml3DContent);
        }
    }

    function createGISRequest(baseUrl, layer, boundingbox, layerCRS) {
        // console.log("createGISRequest");
        var requestUrl;
        var service = "w3ds?version=0.4&service=w3ds";
        
        var format = "&format=model/xml3d+xml";

        var crs = "&crs="+layerCRS;
        var request = "&request=GetScene";

        // If user hasn't defined LOD level, LOD level is not included to request at all
        if (LodLevel !== -1){
            requestUrl = baseUrl + service + request + crs + format+"&layers="+layer+"&boundingbox="+boundingbox+"&LOD="+LodLevel;
        }else{
            requestUrl = baseUrl + service + request + crs + format+"&layers="+layer+"&boundingbox="+boundingbox;
        }
        
        // console.log(requestUrl);
        return requestUrl;
    }
    function httpRequest_dynamicGrid(requestUrl, layerName, row, col, texture, callback) {
        // console.log("httpRequest(): "+requestUrl, layerName, transformX, transformY, texture);
        startSpinner();

        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
            xmlhttp.layerName = layerName;
            xmlhttp.texture = texture;
            xmlhttp.row = row;
            xmlhttp.col = col;
        } else {
            xmlhttp = new XDomainRequest();
        }

        // Set callback function
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState==4){
                if(xmlhttp.status==200) {
                    callback(xmlhttp.responseText, xmlhttp.layerName, xmlhttp.texture, xmlhttp.row, xmlhttp.col);
                }
            } 
        }
        xmlhttp.open("GET", requestUrl , true);
        xmlhttp.send(null);
    }

    function httpRequest3dObjects(requestUrl, layerName, row, col, callback) {
        // console.log("httpRequest3dObjects(): "+requestUrl, layerName, transformX, transformY);
        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
        } else {
            xmlhttp = new XDomainRequest();
        }

        // Set callback function
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {       
                callback(xmlhttp.responseText,  row, col);
            }
        }
        xmlhttp.open("GET", requestUrl , true);
        xmlhttp.send();
    }

    // parse XML3D object and fetch first elevation point to be used as reference elevation for camera
    function getTerrainElevationRefPoint(){
        // console.log("getTerrainElevationRefPoint()");
        var meshObjects = document.getElementsByTagName("mesh");
        //Get elev data from last node
        // elevRefpoint = document.getElementsByTagName("mesh")[meshObjects.length-1].childNodes[1].value[10];
        elevRefpoint = document.getElementsByTagName("mesh");
        console.log(elevRefpoint.length);   
        if (elevRefpoint > 0){
            currentTerrainElevRefPoint = elevRefpoint;
        }
        else {
            currentTerrainElevRefPoint = 0;
        }        
    }


    // Used only when new layer is request
    function setCameraPosition(){
        // console.log("setCameraPosition");
        // Move camera to correct debugging position
        var camera_node = document.getElementById("t_node-camera_player");
        var camera_player = document.getElementById("camera_player-camera");
        camera_player.setAttribute("orientation", "-0.587460994720459 -0.6694765686988831 -0.45463240146636963 1.6601181509666858");

        // camera_player.setAttribute("orientation", "-0.05 -1.0 -0.1 1");
        // camera_player.setAttribute("orientation", "0 -1 -0.11 2.6");        
        
        camera_player.setAttribute("position", 
                                   camCenterX+" "+
                                   // parseFloat(currentTerrainElevRefPoint+camHeightOffset)+" "+ blocklengthY);
                                    parseFloat(currentTerrainElevRefPoint+camHeightOffset)+" "+camCenterY);
    }

    // Parse XML3D defintion file to format which can be injected to DOM
    function parseMeshSrc(xml3dData, row, col){
            // console.log("parseMeshSrc(xml3dData): transformX, transformY: "+transformX, transformY);
            // console.log(xml3dData);
            
        if ($(xml3dData).find("mesh").attr("src")!= undefined){
            
            startSpinner();
            // var n = $(xml3dData).length;

            for(i=0;i<$(xml3dData).length;i++){
                var meshSrc = $(xml3dData).find("mesh").eq(i).attr("src");
                translation = $(xml3dData).eq(i).attr('translation');
                //HOX: change translation according to used grid
                split = translation.split(' ');
                split[0] = parseFloat(split[0]) + parseFloat(terrainGridArray[row][col].MinX);
                split[2] = (-parseFloat(split[2]) + parseFloat(terrainGridArray[row][col].MinY));
                translation = split[0]+" "+split[1]+" "+split[2];

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
                                    // console.log(meshName.indexOf('submesh'));                    
                                    meshNameArray.push(meshSrc+"#"+meshName);
                                }
                            });
                        // console.log(meshNameArray);
                        addMeshtoHtml(meshNameArray, translation);
                        });
                    }
                }
                xmlhttp.open("GET",meshSrc,false);
                xmlhttp.send();
            }
        }
    }


    // injects XML3D objects (e.g buildings) to DOM 
    function addMeshtoHtml(meshSrcArray, translation){
        // console.log("addMeshtoHtml:  "+meshSrcArray);
        var IdName = "Object"+Math.floor(Math.random()*1111);

        var transformation = "<transform id=\""+IdName+"transform"+"\" rotation=\"0.0 0.0 0.0 0.0\" translation=\""+translation+"\"></transform>";
        // console.log(transformation);
        $("#defs").append(transformation);
        
        var newGroup = "<group id=\""+IdName+"\" shader=\"#phong\" transform=\"#"+IdName+"transform\">";

        for (i=0;i<meshSrcArray.length;i++){
            var meshString = "<mesh src=\""+meshSrcArray[i]+"\"/>";
            newGroup+=meshString;
        }
        newGroup+=("</group>");
        // console.log(newGroup);

        $("#MaxScene").append(newGroup); 

        stopSpinner();
    }

    function addXml3DContent(xml3dData, layerName, textureUrl, row, col) {
        //console.log("addXml3DContent(): "+layerName, textureUrl, transformX, transformY);
        //console.log("addXml3DContent(): "+xml3dData);

        // Check if returned XML3D object is empty. DON'T add empty XML3D object to DOM tree
        if (xml3dData!==""){
            var IdName = terrainGridArray[row][col].blockID;
            var xmlnsTagContent = 'http://www.xml3d.org/2009/xml3d';

            var xml3dGroupExists = $( "#"+IdName ).length;
            if (xml3dGroupExists === 0){
                var xml3dGroup = document.createElement('group');
                xml3dGroup.setAttribute('id',IdName);
                xml3dGroup.setAttribute('xmlns',xmlnsTagContent);
                xml3dGroup.setAttribute('shader','#'+IdName+'shader');
                xml3dGroup.setAttribute('transform','#'+IdName+'transform');
                $(xml3dGroup).append(xml3dData);
                $("#MaxScene").append(xml3dGroup);
            } else{
                $("#"+IdName).children().remove();
                $("#"+IdName).append(xml3dData);
            }

            // var shaderExists = $( "#"+IdName+"shader" ).length;
            var shaderExists = $( '#'+IdName+'shader' ).length;

            // Create layer specific shader
            if (shaderExists === 0){
                var layerShader = document.createElement('shader');
                layerShader.setAttribute('id',IdName+'shader');
                layerShader.setAttribute('class',IdName+'shader');
                layerShader.setAttribute('script','urn:xml3d:shader:phong');
                var layerFloat3 = document.createElement('float3');
                layerFloat3.setAttribute('name','diffuseColor');
                $(layerFloat3).append('1.0  1.0  1.0');
                $(layerShader).append(layerFloat3);
                var layerFloat = document.createElement('float');
                layerFloat.setAttribute('name','ambientIntensity');
                $(layerFloat).append('0.1')
                $(layerShader).append(layerFloat);

                $('#transforms').append(layerShader);

                if (textureUrl!==undefined){
                    var texture = "<texture name=\"diffuseTexture\">\n";
                    texture += "<img src=\"" + textureUrl + "\"/>\n" + "</texture>";
                    document.getElementById(IdName+"shader").innerHTML = texture;
                }   
                
            } else if (textureUrl!==undefined){
                // Change only texture
                var texture = "<texture name=\"diffuseTexture\">\n";
                texture += "<img src=\"" + textureUrl + "\"/>\n" + "</texture>";
                document.getElementById(IdName+"shader").innerHTML = texture;
                }   
            
            var transformationExists = $( "#"+IdName+"transform" ).length;
            if (transformationExists === 0 ){
                var transformation = document.createElement('transform');
                transformation.setAttribute('id',IdName+"transform");
                transformation.setAttribute('class',IdName+"transform");
                transformation.setAttribute('rotation','0.0 0.0 0.0 0.0');
                transformation.setAttribute('translation',terrainGridArray[row][col].MinX+' 0 '+terrainGridArray[row][col].MaxY);
                // console.log("addXml3DContent_dynamicGrid, transform: "+ gridBlockObject, dynamicLayerBlockHash[gridBlockObject].MinX, dynamicLayerBlockHash[gridBlockObject].MinY,dynamicLayerBlockHash[gridBlockObject].MaxYX, dynamicLayerBlockHash[gridBlockObject].MaxY);
                $('#transforms').append(transformation);
            }
            if (newLayer) {
                // getTerrainElevationRefPoint();
                setCameraPosition(); 
                newLayer = false;
            }
        }
        stopSpinner();
    }
}());


