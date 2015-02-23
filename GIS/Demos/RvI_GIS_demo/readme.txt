Sun light change visualization in the GIS test client web page by using RvI framework.
--------------------------------------------------------------------------------------

RvI_GIS_simulation.jar is part of the terrain sunlight visualization demo. RvI_GIS_simulation.jar server is customized 
version of Real Virtual Interaction Server, server is modified so that when it is launched with "_simulate" switch,
it send automatically luminance value which all the clients who are listening this specific server are able to receive
and read. To make demonstration easier values are between 1.2 - 3.0. In the GIS test client this value is used to 
manipulate terrain shader intensity value. This way terrain changes similary than it would change when sun is rising and 
lowering.

More about RvI in the wiki: 
http://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/FIWARE.OpenSpecification.MiWi.RealVirtualInteraction

Following is described steps how to launch demonstration:
1. With terminal/command prompt navigate place where RvI_GIS_simulation.jar exists. 
2. Activate simulation with following command:
   java -jar RvI_GIS_simulation.jar -simulate
3. Now Real virtual interaction simulation is running.
4. Open Gis test client web page
5. In the GIS test client there is "Connect RealWorldInteraction"-button. By pressing the button client creates 
connection with RvI and if connection is succesfully, information text about succesfully connection is shown
in the test client web page. By opening web browser console it is possible to see incoming messages to gis test client.
If connection creation fails, check if needed ports are open and RvI simulation is running. Needed ports are 
- UDP port: 61616 (by default)
- UDP port: 61617 (only needed for testing and is by default one greater than set UDP port)
- TCP port: 44445 (by default)
- TCP port: 44446 (by default)
6. To see sunlight visualization on top of terrain, please select terrain to be loaded in the gis test client and ensure 
that RvI connection is still active. after terrain is loaded you should be able to see sun light changes on top of the 
terrain.
