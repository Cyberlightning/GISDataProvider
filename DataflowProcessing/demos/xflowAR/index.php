<?php
header('Content-type: application/xhtml+xml'); 
echo '<?xml version="1.0" encoding="UTF-8"?>';

if(true){
	$width = 640;
	$height = 480;
}
else{
	$width = 320;
	$height = 240;
}

$branch = isset($_GET['master']) ? 'master' : 'dev';

?>

<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <title>CYBER-AR</title>
    <script type="text/javascript" src="../../deps/xml3d.old.js"></script>
    <script type="text/javascript" src="../../deps/alvar.js"></script>
    <script type="text/javascript" src="../../deps/alvar-xflow.js"></script>
    <script type="text/javascript" src="../../deps/phongvs.js"></script>
    <script type="text/javascript">
    	<?php 
    	echo "const WEBCAM_WIDTH = $width;"; 
    	echo "const WEBCAM_HEIGHT = $height;";
    	?>
    </script>
    <script type="text/javascript" src="../../deps/scripts.js"></script>
    <style>
	body{
	font: 62.5% "Trebuchet MS", sans-serif;
	margin: 30px;
	background-color:#1F001F;
	}
    </style>
</head>

<body>
<div align="left" style="width:100%;height:100px">
<img src="logo.png" alt=""/>
</div>
    <div style="position: relative; width: <?php echo $width; ?>px; height: <?php echo $height; ?>px; margin: 0 auto" >
	    <video id="background" autoplay="true" width="<?php echo $width; ?>" height="<?php echo $height; ?>"
	           style="position: absolute; top: 0; width: <?php echo $width; ?>px; height: <?php echo $height; ?>px;"></video>
	           
	    <xml3d xmlns="http://www.xml3d.org/2009/xml3d"
	          style="border: 1px solid black; background: rgba(0,0,0,0); position: absolute; top: 0; width: <?php echo $width; ?>px; height: <?php echo $height; ?>px;">
	
	        <data id="arBase" 
	        compute="basicMarkerTransforms, imageMarkerTransforms, 
	        basicMarkerVisibilities, imageMarkerVisibilities, perspective 
	        = alvar-mobile-xflow.detect(arvideo, basicMarkers, imageMarkers, allowedImageMarkerErrors, flip)">
				<bool name="flip">false</bool>
				<int name="allowedImageMarkerErrors"></int>
				<int name="imageMarkers"></int>
				<int name="basicMarkers">38, 39</int>
				<texture name="arvideo">
					<video id="webcam"></video>
				</texture>
			</data>

	        <!-- Viewpoint with connection to AR data -->
	        <view id="View" perspective="#arBase" />

	        <!-- Object 1: -->
	        <!-- Extract visibility and transformation -->
	        <data id="obj1AR" compute="transform = alvar-mobile-xflow.selectTransform(index, basicMarkerTransforms)">
	          <data compute="visibility = xflow.selectBool(index, basicMarkerVisibilities)">
	            <int name="index">0</int>
	            <data src="#arBase"/>
	          </data>
	        </data>
	        <!-- Shader -->
	        <shader id="obj1Shader" script="urn:xml3d:shader:phongvs">
	          <float3 name="diffuseColor">1.0 0.4 0.2</float3>
	          <float name="ambientIntensity">0.2</float>
	          <!-- Take visibility from AR -->
	          <data filter="keep(visibility)" src="#obj1AR" />
	        </shader>
		<!-- Group taking transformation from AR data -->
		<group transform="#obj1AR" shader="#obj1Shader" >
		  <group style="transform: rotateX(90deg) scale(0.02,0.02,0.02) translate3d(0px, 1px, 0px)" >
		    <mesh type="triangles" src="res/teapot.xml#mesh" ></mesh>
		  </group>
		</group>

	        <!-- Object 2: -->
	        <!-- Extract visibility and transformation -->
	        <data id="obj2AR" compute="transform = alvar-mobile-xflow.selectTransform(index, basicMarkerTransforms)">
	          <data compute="visibility = xflow.selectBool(index, basicMarkerVisibilities)">
	            <int name="index">1</int>
	            <data src="#arBase"/>
	          </data>
	        </data>
	        <!-- Shader -->
	        <shader id="obj2Shader" script="urn:xml3d:shader:phongvs">
	          <float3 name="diffuseColor">0.0 0.4 0.2</float3>
	          <float name="ambientIntensity">0.2</float>
	          <!-- Take visibility from AR -->
	          <data filter="keep(visibility)" src="#obj2AR" />
	        </shader>
		<!-- Group taking transformation from AR data -->
		<group transform="#obj2AR" shader="#obj2Shader" >
		  <group style="transform: rotateX(90deg) scale(0.02,0.02,0.02) translate3d(0px, 1px, 0px)" >
		    <mesh type="triangles" src="res/teapot.xml#mesh" ></mesh>
		  </group>
		</group>

	        <!-- Lights -->

	        <lightshader id="light1" script="urn:xml3d:lightshader:point">
	          <float3 name="intensity">1 1 1</float3>
	          <float3 name="attenuation">1 0 0</float3>
	          <bool name="castShadow">true</bool>
	        </lightshader>

			<group style="transform: translate3d(0px, 10px, 0px)">
	          <light shader="#light1" />
	        </group>

			<group style="transform: translate3d(5px, 10px, 0px)">
	          <light shader="#light1" />
	        </group>
	
	    </xml3d>
    </div>
    <div class="markers" >
      <p><a href="markers.xhtml" target="_blank" >Print Markers</a></p>
    </div>
<div align="right" style="background-color:#4C334C;clear:both;color:white;font-size: 9">Copyright &#169; 2013 Cyberlightning Ltd.</div>
</body>
</html>
