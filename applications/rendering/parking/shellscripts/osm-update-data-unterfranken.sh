#!/bin/sh
TEMPDIR=/tmp
OSMFILE=unterfranken
OSMPATH=europe/germany/bayern
#rm $TEMPDIR/$OSMFILE.osm.bz2
wget -N http://download.geofabrik.de/osm/$OSMPATH/$OSMFILE.osm.bz2 -P $TEMPDIR
time ~kayd/Install/osm2pgsql/osm2pgsql --slim -d gis $TEMPDIR/$OSMFILE.osm.bz2 -S ~kayd/Install/osm2pgsql/parking.style -v

