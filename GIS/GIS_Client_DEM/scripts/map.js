var xmlDocW3DS;
var xmlDocWMS;
var spinner;
//var baseUrl = "http://130.206.81.238:8080/geoserver/";
//var baseUrl = "http://localhost:8080/geoserver/";
var spinnerCounter = 0;

var ip = location.host;
var baseUrl = "http://"+ip+"/geoserver/";

var oldCoordinates = null;

(function() {
    var layerNames = [];

    var selectedTerrainLayer = null;
    var selectedTerrainLayerDetails = null;

    var spinOpts = {
          lines: 30, // The number of lines to draw
          length: 20, // The length of each line
          width: 4, // The line thickness
          radius: 30, // The radius of the inner circle
          rotate: 0, // The rotation offset
          color: '#000', // #rgb or #rrggbb
          speed: 2, // Rounds per second
          trail: 60, // Afterglow percentage
          shadow: true, // Whether to render a shadow
          hwaccel: false, // Whether to use hardware acceleration
          className: 'spinner', // The CSS class to assign to the spinner
          zIndex: 2e9, // The z-index (defaults to 2000000000)
          top: 'auto', // Top position relative to parent in px
          left: 'auto' // Left position relative to parent in px
        };
    
    spinner = new Spinner(spinOpts).spin();
    $("#loading").append(spinner.el);

    
    function getGeoserverCapabilities() {
        // console.log("getGeoserverCapabilities");
        var xmlhttp;
        if (window.XMLHttpRequest) {
            xmlhttp = new XMLHttpRequest();
        } else {
            xmlhttp = new XDomainRequest();
        }

        // Set callback function
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState==4 && xmlhttp.status==200) {
                // console.log(xmlhttp.responseText);
                // parseServerCapabilities(xmlhttp.responseText);
            }
        };

        xmlhttp.open("GET", baseUrl + "ows?service=w3ds&version=0.4.0&request=GetCapabilities", true);
        xmlhttp.send();
    }

    // Traps selection list click event and launch layer detail fetching funtion
     $(function() {
        $("#SelectLayersButton").click(function(e) {
            console.log("SelectLayersButton clicked");
            if(selectedTerrainLayer !== null){                
                var selectedObjectLayers = [];
                console.log(selectedObjectLayers.length);

                // Check which 3D object layers are selected
                for (i=0; i<layerNames.length; i++){
                    console.log(layerNames[i]);
                    console.log(document.getElementById(layerNames[i]).value);
                    // console.log($('#'+layerNames[i]).name);
                        if ($('#'+layerNames[i]).is(':checked')){
                        console.log(layerNames[i]+" is checked");
                        selectedObjectLayers.push(layerNames[i]);                        
                    }
                }

                newLayer = true;
                getDemLayerDetails(selectedTerrainLayer, selectedTerrainLayerDetails);

                $(this).blur();
                e.preventDefault();
            }else{
                alert("Scene initialization values needs to be set first. At least layer selection needs to be done.");
            }
            
        });
      });

    // Terrain texture selection handling
    $(function() {
        $("#selectTexture").change(function(){
           var selectedTerrainTextureName = $("#selectTexture option:selected").text();
           var selectedTerrainTextureCRS = $("#selectTexture option:selected").val();
           setTextureInfo(selectedTerrainTextureName, selectedTerrainTextureCRS);

           // console.log("select_texture: "+selectedTerrainTextureName, selectedTerrainTextureCRS);
        });
    });

     // User selected resolution for terrain texture
     $(function() {
        $("#selectTextureRes").change(function(e) {
            // console.log("Selection list item: "+this.options[this.selectedIndex].value);
            e.preventDefault();
            if (this.options[this.selectedIndex].value === 'select_texture_resolution'){
                // Select layer-option pressed, do nothing
                console.log("select_texture_resolution");
            }
            else{
                setTextureResolution(this.options[this.selectedIndex].text);
            }
        });
      });

     

    $(function() {
        $("#selectOctetstreamResolution").change(function(e) {
            // console.log("Selection list item: "+this.options[this.selectedIndex].value);
            e.preventDefault();
            if (this.options[this.selectedIndex].value === 'Select_oct_res'){
                // Select layer-option pressed, do nothing
                console.log("Select_oct_res");
            }
            else{
                setOctet_streamResolution(this.options[this.selectedIndex].text);
            }
        });
      });
     

     // gets user selected value for grid division
    $(function() {
        $("#selectGridRowColNumber").change(function(e) {
            // console.log("Selection list item: "+this.options[this.selectedIndex].value);
            e.preventDefault(); // if desired...
            if (this.options[this.selectedIndex].value === 'select_grid_block_division'){
                // Select layer-option pressed, do nothing
                console.log("select_grid_block_division");
            }
            else{
                setGridRowCol(this.options[this.selectedIndex].text);
            }
        });
      });

    // handles terrain layer selection
    $(function() {
        $("#select_Layer").change(function(e) {
            // console.log("Selection list item: "+this.options[this.selectedIndex].text);
            e.preventDefault(); // if desired...
            if (this.options[this.selectedIndex].value === 'select_terrain_layer'){
                // Select layer-option pressed, do nothing
                console.log("select_terrain_layer");
            }
            else{
                selectedTerrainLayer = this.options[this.selectedIndex].text;
                selectedTerrainLayerDetails = this.options[this.selectedIndex].value;
            }
        });
      });

   
    function init(){
        getGeoserverCapabilities();
        initTexttureSelection();
        initTextureSelection();
        initOctetResSelection();
    }

    window.onload = init();
    
}());


// Start function for spinner start.
// Every time function is called "spinnerCounter" counter is increased.
function startSpinner(){    
    if (spinnerCounter === 0){
        $("#loading").show();
        $("#loadingOctetStream").show();
        // $("#loadingOctetStream").style.visibility='visible';
    }
    spinnerCounter += 1;
    console.log("startSpinner()"+spinnerCounter);
    
}

// Stop function for stopping spinner.
// Every time function is called "spinnerCounter" counter is decreased. 
// Spinner is stopped when counter value is "0"
function stopSpinner(){
    spinnerCounter -= 1;
    if (spinnerCounter === 0){
        $("#loading").hide(true);
        $("#loadingOctetStream").hide(true);
    }
    console.log("stopSpinner()"+spinnerCounter);
    
}

function initTexttureSelection(){
    var combo = document.getElementById('selectTextureRes');
    var option = document.createElement('option');
    option.text = "Select texture resolution";
    option.value = "select_texture_resolution";
    try {
        combo.add(option, null); //Standard 
    } catch(error) {
        combo.add(option); // IE only
    }

    for (i=64;i<=1024;i=i*2){ 
        var combo = document.getElementById("selectTextureRes");
        var option = document.createElement("option");
        option.text = i;
        option.value = i;
        try {
            combo.add(option, null); //Standard 
        } catch(error) {
            combo.add(option); // IE only
        }
    }
    $("#selectTextureRes").val(getTextureResolution());
}

function initTextureSelection(){
    var xmlhttp;
    if (window.XMLHttpRequest) {
        xmlhttp = new XMLHttpRequest();
    } else {
        xmlhttp = new XDomainRequest();
    }

    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState==4 && xmlhttp.status==200) {
            // console.log(xmlhttp.responseText);
            var combo = document.getElementById('selectTexture');
            var option = document.createElement('option');
            option.text = "Select texture";
            option.value = "select_layer_texture";
            try {
                combo.add(option, null); //Standard 
            } catch(error) {
                combo.add(option); // IE only
            }

            var comboDem = document.getElementById("select_Layer");
            var optionDem = document.createElement('option');
            if(comboDem.length==0){ //Selection list for DEM layer is empty, add default item
                optionDem.text = "Select terrain layer";
                optionDem.value = "select_terrain_layer";
                try {
                    comboDem.add(optionDem, null); //Standard 
                } catch(error) {
                    comboDem.add(optionDem); // IE only
                }
            }

            var xmlDocWMS = new DOMParser().parseFromString(xmlhttp.responseText,'text/xml');
            var x = xmlDocWMS.getElementsByTagNameNS("http://www.opengis.net/wms", "Layer");

            for (i=0;i<x.length;i++){ 
                var textureName = x[i].getElementsByTagNameNS("http://www.opengis.net/wms", "Name")[0].childNodes[0].nodeValue;
                var textureCRS = x[i].getElementsByTagNameNS("http://www.opengis.net/wms", "CRS")[0].childNodes[0].nodeValue;
                if (textureName.indexOf('texture')!=-1 && textureCRS.indexOf('AUTO')==-1){
                    console.log(textureName);
                    console.log(textureCRS);
                    
                    var option = document.createElement("option");                    
                    option.text = textureName;
                    option.value = textureCRS;
                    try {
                        combo.add(option, null); //Standard 
                    } catch(error) {
                        combo.add(option); // IE only
                    }
                }
                /* Read DEM files and add details of them to the option list */
                else if(textureName.indexOf('DEM')!=-1 && textureCRS.indexOf('AUTO')==-1){
                    var boundingBoxInfo = x[i].getElementsByTagNameNS("http://www.opengis.net/wms", "BoundingBox");
                    console.log(boundingBoxInfo.length);
                    ii=0;
                    while (ii<boundingBoxInfo.length){
                        if(boundingBoxInfo[ii].attributes['CRS'].value === textureCRS){
                            var DemDetails = (boundingBoxInfo[ii].attributes['CRS'].value+"; "+
                                            boundingBoxInfo[ii].attributes['minx'].value+" "+
                                            boundingBoxInfo[ii].attributes['miny'].value+", "+
                                            boundingBoxInfo[ii].attributes['maxx'].value+" "+
                                            boundingBoxInfo[ii].attributes['maxy'].value);

                            var comboDem = document.getElementById("select_Layer");
                            var optionDem = document.createElement("option");
                            optionDem.text = textureName;
                            optionDem.value = DemDetails;
                            try {
                                comboDem.add(optionDem, null); //Standard 
                            } catch(error) {
                                comboDem.add(optionDem); // IE only
                            }
                            console.log("DEM details added to optionlist: "+DemDetails);
                            }
                        ii++
                    }
                }
            }            
        }
    }

    xmlhttp.open("GET", baseUrl + "ows?service=wms&version=1.3.0&request=GetCapabilities" , true);
    xmlhttp.send();

}

function initGridBlockSelection(){
    var combo = document.getElementById('selectGridRowColNumber');
    var option = document.createElement('option');
    option.text = "Select grid";
    option.value = "select_grid_block_division";
    try {
        combo.add(option, null); //Standard 
    } catch(error) {
        combo.add(option); // IE only
    }

    for (i=5;i<=10;i=i+5){ 
        var combo = document.getElementById("selectGridRowColNumber");
        var option = document.createElement("option");
        option.text = i;
        option.value = i;
        try {
            combo.add(option, null); //Standard 
        } catch(error) {
            combo.add(option); // IE only
        }
    }
}


function initOctetResSelection(){
    var combo = document.getElementById('selectOctetstreamResolution');
    var option = document.createElement('option');
    option.text = "Select octet-stream resolution";
    option.value = "Select_oct_res";
    try {
        combo.add(option, null); //Standard 
    } catch(error) {
        combo.add(option); // IE only
    }

    for (i=70;i<=120;i=i+10){ 
        var combo = document.getElementById("selectOctetstreamResolution");
        var option = document.createElement("option");
        option.text = i;
        option.value = i;
        try {
            combo.add(option, null); //Standard 
        } catch(error) {
            combo.add(option); // IE only
        }
    }

    $("#selectOctetstreamResolution").val(getCurrentOctet_streamResolution());
}

// Traps camera movement, used for analyzing when new layer data should be requested
window.MutationObserver = window.MutationObserver
    || window.WebKitMutationObserver
    || window.MozMutationObserver;
// Find the element that you want to "watch"
var target = document.querySelector('#camera_player-camera'),

    observer = new MutationObserver(function(mutation) {
       //console.log(mutation[0].attributeName);
       if (mutation[0].attributeName == "position"){
        //console.log("position change");
        var cam = document.getElementById("camera_player-camera");
        var coordinates = cam.getAttribute("position");
        if (!oldCoordinates && coordinates !== null){
            oldCoordinates = coordinates;
        }
        // console.log("oldCoordinates: "+oldCoordinates);
        // console.log("coordinates: "+coordinates);
        if ((coordinates != null) && (coordinates !==oldCoordinates)) {
            var coordSplit = coordinates.split(" ");
            var currentX = parseFloat(coordSplit[0]);
            var currentY = parseFloat(coordSplit[2]);
            calculateCurrentPosLayerBlock(currentX, currentY);        
            }
       }
    }),
    config = {
        attributes: true 
    };
observer.observe(target, config);