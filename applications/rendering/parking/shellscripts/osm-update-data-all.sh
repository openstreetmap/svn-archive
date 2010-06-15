#!/bin/sh
TEMPDIR=/tmp
OSMFILE=bayern
OSMPATH=europe/germany
OSMFILE=unterfranken
OSMPATH=europe/germany/bayern
wget -N http://download.geofabrik.de/osm/$OSMPATH/$OSMFILE.osm.bz2 -P $TEMPDIR

time ~kayd/Install/osm2pgsql/osm2pgsql --hstore --slim -d gis $TEMPDIR/$OSMFILE.osm.bz2 -S ~kayd/Install/osm2pgsql/parking.style -v

OSMFILE=brazil
OSMPATH=south-america
wget -N http://download.geofabrik.de/osm/$OSMPATH/$OSMFILE.osm.bz2 -P $TEMPDIR

time ~kayd/Install/osm2pgsql/osm2pgsql -a --hstore --slim -d gis $TEMPDIR/$OSMFILE.osm.bz2 -S ~kayd/Install/osm2pgsql/parking.style -v

OSMFILE=finland
OSMPATH=europe
wget -N http://download.geofabrik.de/osm/$OSMPATH/$OSMFILE.osm.bz2 -P $TEMPDIR

time ~kayd/Install/osm2pgsql/osm2pgsql -a --hstore --slim -d gis $TEMPDIR/$OSMFILE.osm.bz2 -S ~kayd/Install/osm2pgsql/parking.style -v

