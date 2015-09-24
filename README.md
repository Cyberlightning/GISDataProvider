# GIS Data Provider GE

The GIS Data Provider GE offers access to 3D GIS data via geo-location queries which can be used by any application to render content in a virtual real world scenario.

## Guides
[GIS Data Provider - Installation and Administration Guide](doc/installation_and_administration_guide.md)

[GIS Data Provider - User and Programmers Guide](doc/User_and_Programmers_Guide.md)

## Content
* GIS_Client_assetInstancing:
  * Reference client for asset instance usage
* GIS_Client_octet-stream
  * Contains GIS GE reference client for requesting and using 3D terrain objects based on octet-stream -data.
* GIS_Client_DEM
  * It is possible to query elevation information from the DEM files,
  * This example demonstrates how octet-stream response got from  DEM file can be utilized for creating terrain.
  * Latitude / Longitude support
* GIS_Client_xml3d:
  * GIS GE reference client for XML3D response type.
  * Metric coordinates supported (TM35)
* Demos/RvI_GIS_demo:
  * compiled jar package of the RealVirtualInteraction GE, can be used with GIS_Client_xml3d for demonstrating RvI visualization.

---
**This project is part of FIWARE.**
[https://www.fiware.org/](https://www.fiware.org/)


FIWARE catalogue: [GIS Data Provider](http://catalogue.fiware.org/enablers/gis-data-provider-geoserver3d)

Dockerhub: [GIS Data Provider](https://hub.docker.com/r/juhahyva/rvi/)
