#!/bin/bash

sudo apt-get install libgdal-dev libpq-dev \
postgresql postgresql-server-dev-all postgresql-contrib postgresql-client-common \
libgeos-dev libproj0 libgdal-dev libgdal1h libgeos-c1 libgeos-dev python-qgis python-qgis-common pgadmin3 libproj-dev build-essential

wget http://download.osgeo.org/postgis/source/postgis-2.1.0.tar.gz
tar xzf postgis-2.1.0.tar.gz
cd postgis-2.1.0
./configure
make -j4
sudo make install