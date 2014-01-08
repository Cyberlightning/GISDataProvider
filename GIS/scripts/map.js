var xmlDocW3DS;
var spinner;
var baseUrl = "http://localhost:9090/geoserver/";

(function() {
    var layerNames = [];

    var selectedTerrainTextureName = null;
    var selectedTerrainTextureCRS = null;

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

    // var baseUrl = "http://localhost:9090/geoserver/";
    // var baseUrl = "http://dev.cyberlightning.com:9091/geoserver/";
    var oldCoordinates = null;

    function parseServerCapabilities(response) {
        // console.log(response);

        xmlDocW3DS = new DOMParser().parseFromString(response,'text/xml');
        var x = xmlDocW3DS.getElementsByTagNameNS("http://www.opengis.net/w3ds/0.4.0", "Layer");


        $('#checkboxdiv').append(' | ');
        for (i=0;i<x.length;i++)
            { 
            var checkboxtext = x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Identifier")[0].childNodes[0].nodeValue;
            var checkboxvalue = x[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Title")[0].childNodes[0].nodeValue;
            $('#checkboxdiv').append(
               $(document.createElement('input')).attr({
                   id:    checkboxvalue
                  ,name:  checkboxtext
                  ,value: checkboxvalue
                  ,type:  'checkbox'
               })
            );
            $('#checkboxdiv').append(
               $(document.createElement('label')).text(checkboxtext));
            $('#checkboxdiv').append(' | ');

            layerNames.push(checkboxvalue);
        }
    }

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
                parseServerCapabilities(xmlhttp.responseText);
            }
        }

        xmlhttp.open("GET", baseUrl + "ows?service=w3ds&version=0.4.0&request=GetCapabilities" , true);
        xmlhttp.send();
    }

    // Traps selection list click event and launch layer detail fetching funtion
     $(function() {
        $("#SelecLayersButton").click(function(e) {
            console.log("SelecLayersButton clicked");
            // console.log("Selection list item: "+this.options[this.selectedIndex].value);
            
            var selectedLayers = [];
            console.log(selectedLayers.length);

            for (i=0; i<layerNames.length; i++){
                console.log(layerNames[i]);
                if($('#'+layerNames[i]).is(':checked')){
                    console.log(layerNames[i]+" is checked");
                    newLayer = true;
                    selectedLayers.push(layerNames[i]);
                    
                }
            }
            // Send selected layer information to scenemanager for further processing
            console.log(selectedLayers.length);
            if (selectedLayers.length > 0){
                getLayerDetails(baseUrl, selectedLayers, selectedTerrainTextureName, selectedTerrainTextureCRS);
            }            

            // Unfocus button to prevent accidental buttons pressing
            $(this).blur();

            e.preventDefault(); // if desired...
        });
      });

    $(function() {
        $("#selectTexture").change(function(){
           // alert( this.options[this.selectedIndex].id )
           // alert($("#selectTexture option:selected").text());
           selectedTerrainTextureName = $("#selectTexture option:selected").text();
           selectedTerrainTextureCRS = $("#selectTexture option:selected").val();
           console.log("select_texture: "+selectedTerrainTextureName, selectedTerrainTextureCRS);
        });
    });

     // user selected resolution for terrain texture
     $(function() {
        $("#selectTextureRes").click(function(e) {
            console.log("Selection list item: "+this.options[this.selectedIndex].value);
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

     // gets user selected value for grid division
    $(function() {
        $("#selectGridRowColNumber").click(function(e) {
            console.log("Selection list item: "+this.options[this.selectedIndex].value);
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
     

    // Traps camera movement, used for analyzing when new layer data should be requested
    $("#camera_player-camera").bind("DOMAttrModified", function() {
        // console.log("#camera_player-camera).bind(DOMAttrModified");
        // check flag if new layer is loaded, because in this case camera height needs to be adjusted 
        // and that operation tricks this function unneseccary. We want to see only camera movements after new layer is initialized
        // if (newLayer){
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
        // }
    })


    function init(){
        getGeoserverCapabilities();
        initTexttureSelection();
        initGridBlockSelection();
        initTextureSelection();
    }

    window.onload = init();

    
}());

function startSpinner(){

    $("#loading").show();
    // spinner(spinOpts).spin();
    
};

function stopSpinner(){
    // spinner.spin();
    $("#loading").hide(true);
};

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

    for (i=64;i<=4096;i=i*2){ 
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
};

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
                option.text = "Select layer texture";
                option.value = "select_layer_texture";
                try {
                    combo.add(option, null); //Standard 
                } catch(error) {
                    combo.add(option); // IE only
                }

                var xmlDoc = new DOMParser().parseFromString(xmlhttp.responseText,'text/xml');
                var x = xmlDoc.getElementsByTagNameNS("http://www.opengis.net/wms", "Layer");

                for (i=0;i<x.length;i++)
                    { 
                    var textureName = x[i].getElementsByTagNameNS("http://www.opengis.net/wms", "Name")[0].childNodes[0].nodeValue;
                    var textureCRS = x[i].getElementsByTagNameNS("http://www.opengis.net/wms", "CRS")[0].childNodes[0].nodeValue;
                    if (textureName.indexOf('texture')!=-1 && textureCRS.indexOf('AUTO')==-1){                        
                        console.log(textureName);
                        console.log(textureCRS);
                        var combo = document.getElementById("selectTexture");
                        var option = document.createElement("option");
                        option.text = textureName;
                        option.value = textureCRS;
                        try {
                            combo.add(option, null); //Standard 
                        } catch(error) {
                            combo.add(option); // IE only
                        }
                    }
                }
            }
        }

        xmlhttp.open("GET", baseUrl + "ows?service=wms&version=1.3.0&request=GetCapabilities" , true);
        xmlhttp.send();

};

function initGridBlockSelection(){
    var combo = document.getElementById('selectGridRowColNumber');
    var option = document.createElement('option');
    option.text = "Select grid block division";
    option.value = "select_grid_block_division";
    try {
        combo.add(option, null); //Standard 
    } catch(error) {
        combo.add(option); // IE only
    }

    for (i=5;i<=25;i=i+5){ 
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
};