#!/bin/bash

sudo apt-get install libgdal-dev libpq-dev \
postgresql postgresql-server-dev-9.3 postgresql-contrib postgresql-client-common \
libgeos-dev libproj0 libproj-dev libgdal-dev libgdal1h libgeos-c1 libgeos-dev python-qgis python-qgis-common

if test -f postgis-2.1.0.tar.gz; then
	echo "postgis source already downloaded"
else 
	wget http://download.osgeo.org/postgis/source/postgis-2.1.0.tar.gz
fi

tar xzf postgis-2.1.0.tar.gz
cd postgis-2.1.0
./configure
make -j4
sudo make install
