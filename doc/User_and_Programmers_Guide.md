# GIS Data Provider - User and Programmers Guide


## Introduction

This document describes how to implement web client which is capable to
query GIS data from GeoServer in XML3D format. GeoServer is able to
create XML3D objects and pass them to the client.

Before starting to implement web client software it is recommended that
installation part from the [GIS Data Provider - Installation and Administration Guide](installation_and_administration_guide.md) is successfully completed.

## Background and Detail

This User and Programmers Guide relates to the GIS Data Provider GE
which is part of the [Advanced Middleware and Web User Interfaces chapter](http://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/Summary_of_FIWARE_Open_Specifications_R4#Advanced_Web_User_Interfaces_Chapter "Advanced_Web_User_Interfaces_Chapter").

For more background information on this GE, also refer to its [Open Specification](http://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/FIWARE.OpenSpecification.WebUI.GISDataProvider_R4 "FIWARE.OpenSpecification.WebUI.GISDataProvider") and [FIWARE Advanced Web UI Architecture](http://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/Advanced_Web_UI_Architecture). Here you can find [Detailed API documentation](http://docs.gisdataprovider.apiary.io/#).

## User guide

Reference GIS client is part of the release. 3D rendering is done with
XML3D, therefore web browser needs to support it. XML3D is based on
WebGL and JavaScript, any browser that support these two technologies
should work. Most tested browsers are:

-   Chrome on Windows, Mac OS X and Android
-   Firefox on Windows, Mac OS X and Android
-   Opera on Android

### Setup GeoServer with test data

To be able to query GIS data in XML3D format from GeoServer GIS data itself needs to be uploaded to server. One way to upload test data to GeoServer is by uploading **ESRI Shapefile** to server. This practice was used during implementation of GIS Data Provider GE. Shapefile needs to contain geometry objects with elevation data, otherwise GIS web client is unable to display 3d terrain. Elevation data is used as Z-axis when XML3D objects are generated in GeoServer. When layer as shapefile is uploaded to GeoServer, mandatory filetypes are `.shp, .dbf, .shx`.

Compared to data efficiency between data stored to shapefile or PosGIS DB, reading data from PostGIS performs significantly better. Detailed guidance how to setup PostGIS with provided test data is described in the [GIS Data Provider - Installation and Administration Guide](installation_and_administration_guide.md).

Third option is to use image based elevation data formats (DEM models). These data sets can only be requested through WMS service and therefore request parameters varies in some parts but data in the response is identical with W3DS request. At the moment in **WMS service** only **Octet-Stream** response is supported for terrain.

### 3D GIS data generation

GIS Data Provider GE test data was based on the National Land Survey of
Finland elevation data. Source data was in *-xyz*-format, which contains
elevation data with spatial information. With this source information it
is possible to generate shapefile consisting polygon structure with
elevation data. 2D presentation of the converted shapefile with
elevation points is flat grid, in 3D presentation each grid points are
in same elevation level as in the real world. Therefore this grid with
elevation data can be used as source for terrain presentation.

### Data generation from \*.xyz file format

Here is a small sample from source dataset:

    Easting    Northing    Elevation
    --------------------------------
    398000.000 7542000.000 279.950
    398010.000 7542000.000 279.388
    398020.000 7542000.000 278.818
    398000.000 7541990.000 281.002
    398010.000 7541990.000 280.436
    398020.000 7541990.000 279.847
    398000.000 7541980.000 282.096
    398010.000 7541980.000 281.539
    398020.000 7541980.000 280.944

That data produces following 2D grid:

|       |              0                  |                1                   |               2                 |
|-------|---------------------------------|------------------------------------|---------------------------------|
| **0** | 398000.000 7542000.000 279.950  |   398010.000 7542000.000 279.388   |  398020.000 7542000.000 278.818 |
| **1** | 398000.000 7541990.000 281.002  |   398010.000 7541990.000 280.436   |  398020.000 7541990.000 279.847 |
| **2** | 398000.000 7541980.000 282.096  |   398010.000 7541980.000 281.539   |  398020.000 7541980.000 280.944 |



From that grid we can create polygons representing terrain surface.

WKT polygons generated from that dataset should look like this:

    POLYGONZ((398000.000 7542000.000 279.950, 398010.000 7542000.000 279.388, 398010.000 7541990.000 280.436, 398000.000 7541990.000 281.002, 398000.000 7542000.000 279.950))
    POLYGONZ((398010.000 7542000.000 279.388, 398020.000 7542000.000 278.818, 398020.000 7541990.000 279.847, 398010.000 7541990.000 280.436, 398010.000 7542000.000 279.388))
    POLYGONZ((398000.000 7541990.000 281.002, 398010.000 7541990.000 280.436, 398010.000 7541980.000 281.539, 398000.000 7541980.000 282.096, 398000.000 7541990.000 281.002))
    POLYGONZ((398010.000 7541990.000 280.436, 398020.000 7541990.000 279.847, 398020.000 7541980.000 280.944, 398010.000 7541980.000 281.539, 398010.000 7541990.000 280.436))

NOTE: It is said in WKT specification that polygons needs to be closed,
meaning that start and end points are the same.

Each polygon should be written into a PostGIS database or into a shape
file.

-   Shapefile writing can be done for example with python library called
    pyshp
    ([https://github.com/GeospatialPython/pyshp](https://github.com/GeospatialPython/pyshp "https://github.com/GeospatialPython/pyshp")).
-   PostGIS can be used for example with psycopg2 library
    ([http://initd.org/psycopg/](http://initd.org/psycopg/ "http://initd.org/psycopg/")).

Depending on selected storage format choose corresponding publishing
steps for data publishing into a W3DS layer with GeoServer.

[Official GeoServer guide how to publish a shapefile](http://docs.geoserver.org/stable/en/user/gettingstarted/shapefile-quickstart/index.html)

[Official GeoServer guide how to publish a PostGIS table](http://docs.geoserver.org/stable/en/user/gettingstarted/postgis-quickstart/index.html)

### Uploading XML3D objects reference information to PostGIS

Single XML3D objects can be placed to 3D GIS environment based on the
object location information. Location information can be stored as
PointZ presentation to PostGIS.

GeoServer supports reading XML3D reference objects from the PostGIS in
following format:

| ID | name <BR>  text | geom <BR>  geometry(PointZ)                                        | mesh_ref <BR> text                            |
|----|-----------------|--------------------------------------------------------------------|-----------------------------------------------|
| 1  | Cabin1          | 01010000A0E70B0000295C8F02367D5241B81E854B81F649410000000000208C40 | http://reference_to_xml3d_file/xml3d_file.xml | 
| 2  | Cabin2          | 01010000A0E70B0000713D0AB7497E5241C3F528DC09E249410000000000208C40 | http://reference_to_xml3d_file/xml3d_file.xml | 
| 3  | Cabin3          | 01010000A0E70B000048E17A04A888524114AE47A1CEEE49410000000000208C40 | http://reference_to_xml3d_file/xml3d_file.xml | 


Geometry conversion to PostGIS can be done with `psql ST\_GeomFromText` -command.

# Programmers guide

Guide for programmers utilizing the GE.

GIS data provider release contains [example implementations of the GIS web
client](https://github.com/Cyberlightning/GISDataProvider). Package contains own version for requesting XMl3D terrain objects from the geoserver
and version for requesting elevation data in octet stream format form the server and dynamically generating XML3D objetc based on received data.

## Needed javascript libraries

For enabling XML3D content in web page at least `xml3d.js` needs to be
included.

     <script src="http://www.xml3d.org/xml3d/script/xml3d.js"></script>

If camera handlding is used, include camera.js.

     <script src="http://www.xml3d.org/xml3d/script/tools/camera.js"></script>

In order to activate camera moving by keys, useKeys needs to be defined inside `<xml3d>`.

     <bool id="useKeys">true</bool>

Camera handling in 3D GIS environment works following way:

-   `a` and `d` -keys move camera left and right
-   `w` and `s` -keys move camera up and down
-   Camera orientation can be changed by pressing right mouse button
    down and moving mouse

## Get GeoServer capabilities

GeoServer capabilities can be queried with following syntax :

	http://hostname:port/path?SERVICE=W3DS&ACCEPTVERSIONS=0.3.0,0.4.0&request=GetCapabilities

The response to a GetCapabilities request is an XML document containing service metadata about the server, including specific information about layer properties and how to access data from the server.

Example query from test client:

     http://localhost:8080/geoserver/ows?service=w3ds&version=0.4.0&request=GetCapabilities

Example of layer details in GetCapabilities response:

     <w3ds:Layer>
       <ows:Title>V4132E</ows:Title>
       <ows:Abstract/>
       <ows:Identifier>fiware:V4132E</ows:Identifier>
       <ows:BoundingBox crs="EPSG:3047">
           <ows:LowerCorner>368000.0 7542000.0</ows:LowerCorner>
           <ows:UpperCorner>374010.0 7548000.0</ows:UpperCorner>
       </ows:BoundingBox>
       <ows:OutputFormat>model/x3d+xml</ows:OutputFormat>
       <ows:OutputFormat>model/xml3d+xml</ows:OutputFormat>
       <ows:OutputFormat>text/html</ows:OutputFormat>
       <w3ds:DefaultCRS>EPSG:3047</w3ds:DefaultCRS>
       <w3ds:Queriable>true</w3ds:Queriable>
       <w3ds:Tiled>false</w3ds:Tiled>
       <w3ds:Style>
           <ows:Title>A default style</ows:Title>
           <ows:Abstract>A sample style that just prints out a green line</ows:Abstract>
           <ows:Identifier>line</ows:Identifier>
       <w3ds:IsDefault>true</w3ds:IsDefault>
       </w3ds:Style>
     </w3ds:Layer>

-   `<ows:Identifier\>` contains layer name which can be used for
    querying specific layer data.
-   `<ows:BoundingBox\>` contains information of the total bounding
    box area where layer data is located. Layer data can be requested
    inside total bounding box. There is also information which
    Coordinate Reference System (CRS) layer uses.
-   `<model/xml3d+xml\>` indicates that layer output can be requested
    as in XML3D format.
-   `<w3ds:Queriable\>` states if layer is queriable for client. In
    case queriable value is false client is not able to request layer
    data.

## Query XML3D objects from GeoServer

**important:** First of all it is important to explain how GeoServer
returns XML3D data: XML3D GIS data queries are always done with real
spatial location with in CRS which layer supports. Layer queries needs
to contain at least *W3DS service definition, layer CRS, layer name and
layer bounding box*. **GeoServer will** process data query and **return
result always located to origin (0,0)**. For this reason client needs to
be aware internally which part of the whole layer area is drawn and to
where GIS camera is directed. In a short client needs to implement
internal scene manager. Without scene manager all layers returned by
GeoServer are put to same origin.

XML3D object query can be done based on the GetCapabilities response
data. XML3D layer which has
`<ows:OutputFormat\>model/xml3d+xml\</ows:OutputFormat\>` and
`<w3ds:Queriable\>true\</w3ds:Queriable\>` can be queried from the
server. It is advised that layer queries should be done with in layer
bounding box. There's no actual harm to extend query outside of the
bounding box, just no data to return from that area.

XML3D object query syntax: 

    geoserver/w3ds?version="version"&service=w3ds&request="request type"&crs="layer CRS"&format="format for XML3D response"&layers="layer name"&boundingbox="query area for GIS data"

Example how the partial layer data is queried, whole layer area is 248000,7668000 260010,7680000. 

    localhost:8080/geoserver/w3ds?version=0.4&service=w3ds&request=GetScene&crs=EPSG:3047&format=model/xml3d+xml&layers=fiware:terrain&boundingbox=248000,7668000,252003,7672000

GeoServer returns XML3D object definitions or references to XML3D
definition files.

### Level Of Details (LOD) usage in object query

Level Of Details (LOD) is integer value starting from 1 and ending to 10. Smaller than 10 LOD levels are generated so that original source data is filtered in GeoServer based on LOD level so that generated 3D terrain data has less details compared to original. LOD level 10 means that detail levels are not reduced at all from the source data.

Level Of Detail is defined in the GeoServer query by providing `LOD` -parameter with relevant LOD number. Example how to add define LOD level 4 `&LOD=4` in the GeoServer query:

    130.206.80.182:8080/geoserver/w3ds?version=0.4&service=w3ds&request=GetScene&crs=EPSG:3047&format=model/xml3d+xml&layers=testbed:fiware_test_terrain&boundingbox=373969.9375,7547970,375183.9625,7549182&LOD=4


### Returned XML3D data object definition

When Geoserver returns XML3D object definition it contains following
information:

    <group xmlns="http://www.w3.org/1999/xhtml" 
           id="outputGeometryCollection" class="NodeClassName">
        <mesh type="triangles">
            <int name="index">...</int>
            <float3 name="position">...</float3>
            <float3 name="normal">...</float3>
            <float2 name="texcoord">...</float2>
        </mesh>
    </group>

Above information is according to [XML3D API
specification](/plugins/mediawiki/wiki/fiware/index.php/FIWARE.OpenSpecification.Details.MiWi.3D-UI "FIWARE.OpenSpecification.Details.MiWi.3D-UI")
and it needs to be placed inside `<xml3d>...</xml3d>` -tags. Web
client needs to create light shader for the loaded XML3D content.

### Adding texture

Raster data (f.ex. ortophotos) is used as terrain texture and official
GeoServer guide how to work with rasters can be found from here:

-   [http://docs.geoserver.org/stable/en/user/data/raster/index.html](http://docs.geoserver.org/stable/en/user/data/raster/index.html "http://docs.geoserver.org/stable/en/user/data/raster/index.html")

Texture for returned XML3D terrain object can be added by requesting
graphics with same bounding box as requested terrain XML3D model was
requested.

Texture query syntax: 

    geoserver/wms?service=WMS&version=1.1.0&request=GetMap&layers="texture layer name"&styles=&bbox="query area for texture"&width="texture width"&height="texture height"&srs="texture layer crs"&format="image format"

Example for requesting terrain texture: 

    dev.cyberlightning.com:8080/geoserver/fiware/wms?service=WMS&version=1.1.0&request=GetMap&layers=fiware:NorthernFinland_texture&styles=&bbox=356000,7530000,372003.3333333333,7546000&width=1024&height=1024&srs=EPSG:404000&format=image%2Fpng

One option to store terrain texture to GeoServer is by using GeoTIFF. In
this case Layer bounding box needs to be precisely correct so that
GeoServer is able to return correct area of the bitmap when requested.

### Returned XML3D data definition file reference

GeoServer is capable to include external XML3D object definition references in to the returned XML3D GIS data query. Chapter *Uploading XML3D objects reference information to PostGIS* describes how data should be uploaded to PostGIS. GeoServer will query database based on the requested bounding box and returns all found points. Each point has reference to external XML3D file. Each point are translated to coordinates inside requested bounding box, so client scene manager needs to be aware where returned building objects should be placed in the 3D GIS presentation.

Example of returned XML3D information with external reference:

    <group xmlns="http://www.w3.org/1999/xhtml" id="fiware:building_coordinates" class="" style="transform: translate3d(1102,900.0,-0.0)">
     <mesh src="http://localhost:8989/a416a634c021.json "></mesh>
    </group>

## Adding XML3D objects in json format to html page

XML3D objects stored to the json can be injected to client web page or
generate new web page where returned XML3D objects are inserted. XML3D
declarations received from the GeoServer needs to be injected inside
`\<xml3d\>` -tags.

Example of index.html-template where new XML3D objects are injected. In
GIS data provider demo all XML3D elements are injected inside
*id="MaxScene"* -group.

index.html before XML3D data request: 

    <body>
        <xml3d id="xml3dContent" xmlns="http://www.xml3d.org/2009/xml3d">
           <defs id="defs" xmlns="http://www.xml3d.org/2009/xml3d">
               <transform id="t_node-camera_player" 
                          rotation="0.0 0.0 0.0 0.0" translation="0 0 0"/>
               <lightshader id="light1" script="urn:xml3d:lightshader:directional" >
                 <float3 name="intensity" >2 2 2</float3>
                 <bool name="castShadow">true</bool>
                 <float3 name="direction">1 -0.5 1.0</float3>
               </lightshader>
               <light shader="#light1" />
               <bool id="useKeys">true</bool>
           </defs>
           <group xmlns="http://www.xml3d.org/2009/xml3d" id="MaxScene">
             <group id="node-camera_player" 
                    transform="#t_node-camera_player" lightshader="#light1">
                 <view fieldOfView="0.7" id="camera_player-camera"/>
             </group>
           </group>
        </xml3d>
    </body>

index.html after XML3D data request: 

     <body>
        <xml3d id="xml3dContent" xmlns="http://www.xml3d.org/2009/xml3d">
           <defs id="defs" xmlns="http://www.xml3d.org/2009/xml3d">
               <transform id="t_node-camera_player" rotation="0.0 0.0 0.0 0.0" 
                          translation="0 0 0"/>
               <lightshader id="light1" script="urn:xml3d:lightshader:directional" >
                 <float3 name="intensity" >2 2 2</float3>
                 <bool name="castShadow">true</bool>
                 <float3 name="direction">1 -0.5 1.0</float3>
               </lightshader>
               <light shader="#light1" />
               <bool id="useKeys">true</bool>
           </defs>
           <group xmlns="http://www.xml3d.org/2009/xml3d" id="MaxScene">
             <group id="node-camera_player" transform="#t_node-camera_player" 
                    lightshader="#light1">
                 <view fieldOfView="0.7" id="camera_player-camera"/>
             </group>
             <group xmlns="http://www.w3.org/1999/xhtml" id="fiware:building_coordinates" 
                    class="" style="transform: translate3d(1102,900.0,-0.0)">
                <mesh src="http://localhost:8989/a416a634c021.json "></mesh>
             </group>
           </group>
       </xml3d>
     </body>


## Using GIS GE Asset instance in web client


It is possible to query XML3D terrain data so that returned terrain data
will be in xml-file. This response is possible to be referenced directly
from DOM. Benefit of using reference to XML3D object file instead of
having whole XML3D object definition in the DOM tree is that this way
DOM tree will remain more clean and easier to maintain.

GIS GE returns terrain data in asset instance format when
**Application/xml** is used as requested data format. Client can utilize
this functionality so that it generates correct URL for the asset
instance request and places this URL to the DOM as \<model\> *src*
content. Below is the example of the asset instance request,

    http://localhost:8080/geoserver/w3ds?version=0.4&service=w3ds&request=GetScene&crs=EPSG:3047&format=Application/xml&layers=fiware:postgis\_terrain&boundingbox=373969.9375,7547970,375183.9625,7549182&LOD=4\#asset

returns terrain data in xml format. When requesting asset data from GIS
GE, returned terrain data has id='asset', this needs to be used in the
generated URL as you can see from the example. More details about asset
instancing can be get from
[http://www.XML3D.org](http://www.XML3D.org "http://www.XML3D.org") In
the example `fiware:postgis\_terrain00transform` and
`fiware:postgis\_terrain00shader` are the client generated definitions
to be used with received asset.

Web Client terrain data asset instance example: 
     
     <xml3d xmlns="http://www.xml3d.org/2009/xml3d" id="xml3dContent" activeView="#camera_player-camera" width="1920" height="943">
       <defs xmlns="http://www.xml3d.org/2009/xml3d" id="defs">
         <transform id="t_node-camera_player" rotation="0.0 0.0 0.0 0.0" translation="0 0 0"/>
         <shader id="phong" script="urn:xml3d:shader:phong">
           <float3 name="diffuseColor">1 0.5 0</float3>
           <float name="ambientIntensity">0.5</float>
         </shader>
         <lightshader id="light1" script="urn:xml3d:lightshader:directional">
           <float3 id="light1_intensity" name="intensity">2 2 2</float3>
           <bool name="castShadow">false</bool>
           <float3 name="direction">1 -0.5 1.0</float3>
         </lightshader>
         <light shader="#light1"/>
         <bool id="useKeys">true</bool>
         <transform xmlns="http://www.w3.org/1999/xhtml" id="layerguideTransform" rotation="0.0 0.0 0.0 0.0" translation="0 0 606"></transform>
         <shader xmlns="http://www.w3.org/1999/xhtml" id="fiware:postgis_terrain00shader" script="urn:xml3d:shader:phong">
           <float3 name="diffuseColor">1.0  1.0  1.0</float3>
           <float name="ambientIntensity">0.1</float>
           <texture name="diffuseTexture">
             <img src="http://localhost:8080/geoserver/fiware/wms?service=WMS&version=1.1.0&request=GetMap&layers=fiware:terrain_texture_raster&styles=&amp;
              bbox=373969.9375,7547970,375183.9625,7549182&width=512&height=512&srs=EPSG:404000&format=image%2Fjpeg" />
           </texture>
         </shader>
         <transform xmlns="http://www.w3.org/1999/xhtml" id="fiware:postgis_terrain00transform" rotation="0.0 0.0 0.0 0.0" translation="0 0 0"></transform>
       </defs>
       <group xmlns="http://www.xml3d.org/2009/xml3d" id="MaxScene">
         ...      
         <model xmlns="http://www.w3.org/1999/xhtml" id="fiware:postgis_terrain00" src="http://localhost:8080/geoserver/w3ds?version=0.4&service=w3ds&request=GetScene&crs=EPSG:3047&format=Application/xml&amp;
       layers=fiware:postgis_terrain&boundingbox=373969.9375,7547970,375183.9625,7549182&LOD=4#asset" transform="#fiware:postgis_terrain00transform" shader="#fiware:postgis_terrain00shader"></model>
       </group>
     </xml3d>

## Requesting terrain data in octet-stream format

Terrain data can be requested also in the octet-stream format by using
**application/octet-stream** format in the request. Detailed explanation
of the returned octet-stream format can be found in the [GIS GE Open API
Specification](http://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/GIS_Data_Provider_Open_API_Specification#Representation_Format "http://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/GIS_Data_Provider_Open_API_Specification#Representation_Format").

When terrain data is requested in octet-stream format, GIS GE returns
binary data from the requested bounding box area. Binary data contains
only information how many elevation points there is within bounding box
and what are the elevation values for these points. Client itself needs
to create 3D presentation based on the received octet-stream data.
Benefit of using octet-stream request is that client is able to freely
use any available rendering engine.

Octet-Steam can be requested from two server modules based on source
data format and those requests differs in some parts.

### World Wind Format Module

This module can handle image based data (DEM Models) and it is
recommended way to store and use elevation data.

Sample request

    http://localhost:8080/geoserver/wms?version=1.1.0&service=WMS&request=GetMap&srs=EPSG:4326&format=application/octet-stream&bbox=24.55704,60.18754,24.57507,60.19652&layers=fiware:imageterrain&width=100&height=100

Static required parameters are:

-   wms?version=1.1.0
-   request=GetMap
-   format=application/octet-stream

Dynamic required parameters are:

-   srs
-   bbox
-   layers
-   width (defines how many points response contains in horizontal
    direction)
-   height (defines how many points response contains in vertical
    direction)

### W3DS Module

This module can handle elevation data stored to database as polygons or
as points.

Sample request

    localhost:8080/geoserver/w3ds?version=0.4&service=w3ds&request=GetScene&crs=EPSG:3047&format=application/octet-stream&layers=fiware:polygonterrain&boundingbox=248000,7668000,252003,7672000&width=100&height=100

Static required parameters are:

-   w3ds?version=0.4
-   request=GetScene
-   format=application/octet-stream

Dynamic required parameters are:

-   crs
-   boundingbox
-   layers

Dynamic optional parameters are:

-   width (defines how many points response contains in horizontal
    direction)
-   height (defines how many points response contains in vertical
    direction)
-   lod