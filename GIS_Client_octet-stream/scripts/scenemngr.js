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
    var blocklengthX, blocklengthY = 0;

    // Amount of the grid blocks for dividing layer
    var layerBlockGridsplit = 5;
    var textureResolution = 512;
    var terrainTextureCRS = 0;
    var LodLevel = 10;
    var octet_stream_resolution = 120;
    // var selectedTerrainTextureName = null;
    // var selectedTerrainTextureCRS = null;

    //has to track which blocks of the layers are loaded
    var LayerBlockHash = new Object();

    var screenHeight = $(document).height();
    var screenWidth = $(document).width();

    var camHeightOffset = 500;
    var currentTerrainElevRefPoint = 0;

    // Currently loaded layer bounding box min and max values    
    var LayerMinX = null;
    var LayerMinY = null;
    var LayerMaxX = null;
    var LayerMaxY = null;

    this.setTextureResolution = function(resolution){
        textureResolution = resolution;
    }
    
    this.getTextureResolution = function(){
        return textureResolution;
    }

    this.setLODlevel = function(LODlevel){
        console.log("Change LOD level to "+LODlevel);
        LodLevel = LODlevel;
    }

    this.getLODlevel = function(){
        return LodLevel;
    }

    this.setOctet_streamResolution = function(resolution){
        console.log("Change octet stream resolution to "+resolution);
        octet_stream_resolution = resolution;
    }

    this.getCurrentOctet_streamResolution = function(){
        return octet_stream_resolution;
    }

    this.setTextureInfo = function(texture, textureCRS){
        TerrainTextureName = texture;
        TerrainTextureCRS = textureCRS;
    }

    // setter function for changing layerBlockGridsplit variable on runtime
    // NOTE: scene must be reloaded after changing this value
    this.setGridRowCol = function(gridsplit){
        layerBlockGridsplit = gridsplit;
    }

     // Fetches layer details from GeoServer and passes them to getElements()-function
    this.getLayerDetails = function(selectedLayer, selectedObjectLayers) {
        console.log("getLayerDetails(): "+selectedLayer, selectedObjectLayers);
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
        


        var MinX = LayerMinX;
        var MinY = LayerMinY;
        var MaxX = LayerMinX + blocklengthX;
        var MaxY = LayerMinY + blocklengthY;

        createLayerGuideBlock();

        for (i=0;i<layerToBeLoaded.length;i++){
            getElements(layerToBeLoaded[i],
            MinX, MinY, MaxX, MaxY, //custom size for layer min/max boundaries
            LayerBlockHash[layerToBeLoaded[i]+"_CRS"], 0, 0
            );

        }        
    }

    // Creates square below terrain to give guidance what is the maximum area for the terrain to be loaded
    function createLayerGuideBlock(){
        // var transform = "<transform id=\"layerguideTransform\" rotation=\"0.0 0.0 0.0 0.0\" translation=\"0 0 "+(blocklengthY/2)+"\"></transform>"
        var transform = "<transform id=\"layerguideTransform\" rotation=\"0.0 0.0 0.0 0.0\" translation=\""+(-(blocklengthX/4))+" 0 "+((blocklengthY/4))+"\"></transform>"
        $("#defs").append(transform);

        var layerBorder = "<group id=\"layerguide\" xmlns=\"http://www.xml3d.org/2009/xml3d\" shader=\"#phong\"  transform=\"#layerguideTransform\">";

        layerBorder += "<mesh type=\"triangles\" transform=\"#layerguideTransform\"> <int name=\"index\">0 1 2  1 2 3</int>";
        layerBorder += "<float3 name=\"position\"> 0 20 0 "+(blocklengthX*layerBlockGridsplit)+" 20 0 0 20 "+(-(blocklengthY*layerBlockGridsplit))+" "+(blocklengthX*layerBlockGridsplit)+" 20 "+(-(blocklengthY*layerBlockGridsplit))+"</float3>";
        layerBorder += "<float3 name=\"normal\">0 0 1  0 0 1  0 0 1  0 0 1</float3>";
        layerBorder += "<float2 name=\"texcoord\">0.0 0.0 1.0 0.0 0.0 1.0 1.0 1.0</float2>";
        layerBorder += "</mesh></group>";
        $("#MaxScene").append(layerBorder);
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
        // console.log("Identifier, LowerCorner, UpperCorner, DefaultCRS: "+Identifier, LowerCorner, UpperCorner, DefaultCRS);

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
        if (LayerMinX>parseFloat(lowerCornerSplit[0]) || LayerMinX===null ){
            LayerMinX = parseFloat(lowerCornerSplit[0]);
        }
        if (LayerMinY>parseFloat(lowerCornerSplit[1]) || LayerMinY===null){
            LayerMinY = parseFloat(lowerCornerSplit[1]);
        }
        if (LayerMaxX < parseFloat(higherCornerSplit[0]) || LayerMaxX ===null){
            LayerMaxX = parseFloat(higherCornerSplit[0]);
        }
        if (LayerMaxY < parseFloat(higherCornerSplit[1]) || LayerMaxX ===null){
            LayerMaxY = parseFloat(higherCornerSplit[1]);
        }
        // console.log("minmax arvot BB "+LayerMinX, LayerMinY, LayerMaxX, LayerMaxY);

        blocklengthX = parseFloat((LayerMaxX-LayerMinX)/layerBlockGridsplit);
        blocklengthY = parseFloat((LayerMaxY-LayerMinY)/layerBlockGridsplit);
    }


    // Parses data for GeoServer GIS request
    // 
    // layerName = Layer Name
    // lowerCornerX, lowerCornerY, higherCornerX, higherCornerY = BoundingBox lower and higher corners, area for GIS request
    // layerCRS = Layer CRS
    function getElements(layerName, lowerCornerX, lowerCornerY, higherCornerX, higherCornerY, layerCRS, transformX, transformY){
        // console.log("getElements(layerName, lowerCornerX, lowerCornerY, higherCornerX, higherCornerY, layerCRS, transformX, transformY): "+layerName, lowerCornerX, lowerCornerY, higherCornerX, higherCornerY, layerCRS, transformX, transformY);

        var service = "w3ds";
        var version = "0.4.0";
        var imageFormat = "jpeg";

        var xml3dobject = document.getElementById("xml3dContent");
        
        xml3dobject.setAttribute("width", screenWidth);
        xml3dobject.setAttribute("height", screenHeight);
        

        // external xml files contains all needed info, also textures. 
        // With other layers, e.g. terrain textures needs to be downloaded separately
        if ((TerrainTextureName != null) && (TerrainTextureCRS != null)){
            var texture = baseUrl+"fiware/wms?service=WMS&amp;version=1.1.0&amp;request=GetMap&amp;layers=" +
                                TerrainTextureName + 
                                "&amp;styles=&amp;bbox=" + 
                                lowerCornerX+","+
                                lowerCornerY+","+
                                higherCornerX+","+
                                higherCornerY+ 
                                "&amp;width="+textureResolution+"&amp;height="+textureResolution+"&amp;srs="+TerrainTextureCRS+"&amp;format=image%2F"+imageFormat;

            

        }
        // Assumption is that if layer contains external 3D object references, layer name contains "building_coordinates".
        if (layerName.indexOf("building_coordinates")>=0){
            var xml3dRequest = createGisXml3dRequest( baseUrl, 
                                            layerName, 
                                            lowerCornerX+","+
                                            lowerCornerY+","+
                                            higherCornerX+","+
                                            higherCornerY,
                                            layerCRS);
            httpRequest3dObjects(xml3dRequest, layerName, transformX, transformY, parseMeshSrc);
        }else{
            // console.log("getElements: "+lowerCornerX+", "+lowerCornerY+", "+higherCornerX+", "+higherCornerY);
            var octetStreamRequest = createGisOctetStreamRequest( baseUrl, 
                                            layerName, 
                                            lowerCornerX+","+
                                            lowerCornerY+","+
                                            higherCornerX+","+
                                            higherCornerY,
                                            layerCRS);
            httpRequest(octetStreamRequest, layerName, transformX, transformY, texture, addOctetstreamContent);
        }

        

    }

    function createGisXml3dRequest(baseUrl, layer, boundingbox, layerCRS) {
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

    function createGisOctetStreamRequest(baseUrl, layer, boundingbox, layerCRS) {
        // console.log("createGISRequest");
        var requestUrl;
        var service = "w3ds?version=0.4&service=w3ds";
        
        // Octet-stream specific format
        var format = "&format=application%2Foctet-stream";

        var crs = "&crs="+layerCRS;
        var request = "&request=GetScene";
        // var repType = "&responsetype=ArrayBuffer";
        var dataType = "&DataType=Binary";

        var octet_stream_resolution_complete = "&width="+octet_stream_resolution+"&height="+octet_stream_resolution;

        requestUrl = baseUrl + service + request + crs + format+"&layers="+layer+"&boundingbox="+boundingbox+octet_stream_resolution_complete;

        // If user hasn't defined LOD level, LOD level is not included to request at all
        if (LodLevel !== -1){
            requestUrl = requestUrl+"&LOD="+LodLevel;
        }
        // if (octet_stream_resolution !== -1){
        //     requestUrl = requestUrl+octet_stream_resolution_complete;
        // }
        
        // console.log(requestUrl);
        return requestUrl;


    }

    function httpRequest(requestUrl, layerName, transformX, transformY, texture, callback) {
        // console.log("httpRequest(): "+requestUrl, layerName, transformX, transformY, texture);
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
                // callback(xmlhttp.responseText, layerName, texture, transformX, transformY);
                callback(xmlhttp.response, layerName, texture, transformX, transformY);
            }
        }
        xmlhttp.open("GET", requestUrl , true);
        xmlhttp.responseType = "arraybuffer";
        xmlhttp.send();
    }
    
    function httpRequest3dObjects(requestUrl, layerName, transformX, transformY, callback) {
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
                callback(xmlhttp.responseText, transformX, transformY);
            }
        }
        xmlhttp.open("GET", requestUrl , true);
        xmlhttp.send();
    }

    // Parse XML3D defintion file to format which can be injected to DOM
    function parseMeshSrc(xml3dData, transformX, transformY){
            // console.log("parseMeshSrc(xml3dData): transformX, transformY: "+transformX, transformY);
            // console.log(xml3dData);
            var style, meshSrc;
        if ($(xml3dData).find("mesh").attr("src")!= undefined){
            
            startSpinner();

            meshSrc = $(xml3dData).find("mesh").attr("src");
            // console.log("mesh src found: "+$(xml3dData).find("mesh").attr("src"));            
            translation = $(xml3dData).find("group").attr("translation");
            // console.log('parseMeshSrc:translation: '+translation);
            // console.log('parseMeshSrc:blocklengthX & blocklengthY: '+blocklengthX, blocklengthY);

            //HOX: change translation according to used grid
            split = translation.split(' ');
            if (transformX>0){
                split[0] = parseFloat(split[0]) + parseFloat((transformX+1)*blocklengthX);
            }
            if(transformY>0){
                // GeoServer sends object coordinates so that y-axis is measured from the top level of the block.
                // scenemanager handles grids from down to up in y-axis, therefore y-axis transfomation is needed.
                var object_grid_location = (blocklengthY - parseFloat(split[2]));
                // console.log("object_grid_location "+object_grid_location)
                split[2] = (parseFloat(object_grid_location) - parseFloat((transformY)*blocklengthY));

            }else{
                split[2] = parseFloat(split[2])-blocklengthY;
            }
            translation = split[0]+" "+split[1]+" "+(split[2]);
            // console.log("translation: "+translation);

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


    // parse XML3D object and fetch first elevation point to be used as reference elevation for camera
    function getTerrainElevationRefPoint(){
        // console.log("getTerrainElevationRefPoint()");
        var meshObjects = document.getElementsByTagName("mesh");
        //Get elev data from last node
        elevRefpoint = document.getElementsByTagName("mesh")[meshObjects.length-1].childNodes[1].value[10];
        // console.log(meshObjects.length);
        // console.log("getTerrainElevationRefPoint: "+ elevRefpoint);
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
        camera_player.setAttribute("orientation", "0 -1 -0.11 2.6");
        camera_player.setAttribute("position", 
                                   "0 "+
                                   parseFloat(currentTerrainElevRefPoint+camHeightOffset)+" 0");
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

    var handleOctetStream = function(octetStreamData) {
        var returnData = [];

        console.log("handle octet-stream");
        var dv = new DataView(octetStreamData), dataOffset = 0;
        var L_INT32 = 4, L_FLOAT32 = 4;

        var s = dv.getInt32(dataOffset, false);
        dataOffset += L_INT32;
        var t = dv.getInt32(dataOffset, false);
        dataOffset += L_INT32;
        
        console.log("s:", s);
        console.log("t:", t);
        returnData.push(s);
        returnData.push(t);

        var ds = dv.getFloat32(dataOffset, false);
        dataOffset += L_FLOAT32;
        var dt = dv.getFloat32(dataOffset, false);
        dataOffset += L_FLOAT32;
        
        console.log("avg dist in s:", ds);
        console.log("avg dist in t:", dt);

        var c = new Float32Array(octetStreamData, dataOffset);
        
        // convert big endian to host endianess
        for (var i = 0; dataOffset < dv.byteLength; dataOffset += L_FLOAT32, i++) // {
            c[i] = dv.getFloat32(dataOffset, false);

        console.log(c);
        returnData.push(c);
        returnData.push(ds);
        returnData.push(dt);

        return returnData;
    };

    function addOctetstreamContent(octetstreamData, layerName, textureUrl, transformX, transformY) {
        //console.log("addOctetstreamContent(): "+layerName, textureUrl, transformX, transformY);
        //console.log("addOctetstreamContent(): "+octetstreamData);
        var octet_data = handleOctetStream(octetstreamData);
        // get rid of "fiware:" text from layername
        layerName = layerName.substring(7, layerName.length);

        var newGroup = document.createElement('group');
        var IdName = layerName+transformX+transformY;
        var xmlnsTagContent = 'http://www.xml3d.org/2009/xml3d';
        
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

        if (textureUrl!==undefined){
            var texture = "<texture name=\"diffuseTexture\">\n";
            texture += "<img src=\"" + textureUrl + "\"/>\n" + "</texture>";
            $(layerShader).append(texture);    
        }   

        var transformation = document.createElement('transform');
        transformation.setAttribute('id',IdName+"transform");
        transformation.setAttribute('rotation','0.0 0.0 0.0 0.0');
        transformation.setAttribute('scale', '607 180 606');
        transformation.setAttribute('translation',(transformX*blocklengthX)+' 0 '+((-transformY*blocklengthY)));
        // transformation.setAttribute('translation',(transformX*(octet_data[0]*octet_data[3]))+' 0 '+((transformY*(-octet_data[1]*octet_data[4]))));

        // <!-- Load terrain data: --> 
        var data_terrain = document.createElement('data');
        data_terrain.setAttribute('id', IdName+"_terrain_data");
        var gis_dimension = document.createElement('int');
        gis_dimension.setAttribute('id', IdName+"_gis_dimension");
        gis_dimension.setAttribute('name', IdName+"_dimension");
        $(gis_dimension).append('2');
        var gis_elevation = document.createElement('float');
        gis_elevation.setAttribute('id', IdName+"_gis_elevation");
        gis_elevation.setAttribute('name', IdName+"_elevation");
        $(gis_elevation).append('0 0 0 0');

        $(data_terrain).append(gis_dimension,gis_elevation);
        $('#defs').append(data_terrain);
        //console.log(octet_data[0]); 
        $("#"+IdName+"_gis_dimension").text(octet_data[0] + " " + octet_data[1]);
        $("#"+IdName+"_gis_elevation")[0].setScriptValue(octet_data[2]);

        // <!-- Generate Grid: -->  
        var generatedGrid = document.createElement('data');
        generatedGrid.setAttribute('id', IdName+"_generatedGrid");
        generatedGrid.setAttribute('compute', "(position, normal, texcoord, index) = xflow.mygrid(size)");
        var generatedGrid_data_src = document.createElement('data');
        generatedGrid_data_src.setAttribute('src', "#"+IdName+"_terrain_data");
        generatedGrid_data_src.setAttribute('filter', "rename({size: "+IdName+"_dimension"+"})");
        $(generatedGrid).append(generatedGrid_data_src);

        // <!-- Transform Grid by elevation data: -->
        var surface = document.createElement('data');
        surface.setAttribute('id', IdName+"_surface");
        surface.setAttribute('compute', "normal = xflow.vertexNormal(position, index)");
            var displace = document.createElement('data');
            displace.setAttribute('id', IdName+"_displace");
            displace.setAttribute('compute', "position = xflow.morph(position, scale, "+IdName+"_elevation)");
                var displaceFloat3 = document.createElement('float3');
                displaceFloat3.setAttribute('name','scale');        
                displaceFloat3.setAttribute('id','scale');
                $(displaceFloat3).append('0 0.0054 0');
                $(displace).append(displaceFloat3);
                var displacedata = document.createElement('data');
                displacedata.setAttribute('src','#'+IdName+'_generatedGrid');        
                $(displace).append(displacedata);
        $(surface).append(displace);


        //$('#defs').append(layerShader,transformation,data_terrain, generatedGrid, surface);
        $('#defs').append(layerShader,transformation,generatedGrid, surface);

        newGroup.setAttribute('id',IdName);
        newGroup.setAttribute('xmlns',xmlnsTagContent);
        newGroup.setAttribute('shader','#'+IdName+'shader');
        newGroup.setAttribute('transform','#'+IdName+'transform');
        var groupMesh = document.createElement('mesh');
        groupMesh.setAttribute('type', "triangles");
        var meshData = document.createElement('data');
        meshData.setAttribute('src', "#"+IdName+"_surface");
        $(groupMesh).append(meshData);
        $(newGroup).append(groupMesh);

        // $(newGroup).append(octetstreamData);


        $("#MaxScene").append(newGroup);

        if (newLayer) {
            // getTerrainElevationRefPoint();
            setCameraPosition(); 
            newLayer = false;
        }
        stopSpinner();
    }
    

this.calculateCurrentPosLayerBlock = function(currentX, currentY){
        // console.log("calculateCurrentPosLayerBlock(): "+currentX, currentY);
       
        var MinX, MinY, MaxX, MaxY;

        //       X1|X2|X3 
        //     ------------
        //   Y3||31|32|33||
        //   Y2||21|22|23||
        //     |+--------||
        //   Y1||11|12|13||
        //     ------------
        // First block loaded is 11. Upper left corner of the 1st block is the origo.
        
        var col=-1, row=-1, MinX=0, MinY=0, MaxX=0, MaxY = 0;
        var offsetX = parseFloat(blocklengthX/3);
        var offsetY = parseFloat(blocklengthY/3);

        // check northing block
        for (gridSplit=0;gridSplit<=layerBlockGridsplit;gridSplit++){
            if (currentX+offsetX < parseFloat(gridSplit*blocklengthX)){
                col = gridSplit-1;
                MinX = parseFloat((gridSplit-1)*blocklengthX);
                MaxX = parseFloat(gridSplit*blocklengthX); 
                break;           
            }
        }

        // check easting block
        for (gridSplit=0;gridSplit<=layerBlockGridsplit;gridSplit++){
            if (currentY-blocklengthY > parseFloat(-(gridSplit*blocklengthY))){
                row = gridSplit-1;
                MinY = parseFloat(-((gridSplit-1)*blocklengthY))
                MaxY = parseFloat(-(gridSplit*blocklengthY));
                break;
            }
        }

        if (col>=0 && row>=0){
            if (checkIfLayerBlockIsLoaded(layerToBeLoaded[0], row, col)===false){    
                // console.log("load new block. row:"+row+", col: "+col );   
                // console.log("load new block. min:"+MinX+":"+MinY+", Max: "+MaxX+":"+MaxY ); 

                for (i=0;i<layerToBeLoaded.length;i++){
                    getElements(layerToBeLoaded[i],
                            MinX+LayerMinX, (Math.abs(MinY)+LayerMinY)+0,
                            MaxX+LayerMinX, (Math.abs(MaxY)+LayerMinY),
                            // currentLayerCRS, col, row );
                            LayerBlockHash[layerToBeLoaded[i]+"_CRS"], col, row );
                }
            }else{
                // console.log('dont load new block' );
            }        
        }        
    }
}());


