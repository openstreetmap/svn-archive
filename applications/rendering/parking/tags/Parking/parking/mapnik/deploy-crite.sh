#! /bin/bash
# run in the current workspace, assuming that "Parking" and "mapnik" are checked out projects.
MAPNIKSRC=../../mapnik
PARKINGSRC=.
TARGETSRV=crite
DESTDIR=/tmp/deploy-neu-$TARGETSRV
TMPDIR=/tmp/tmp-kay-mapnik

python setup_target_style.py -m $MAPNIKSRC -t $TMPDIR -d $DESTDIR -v $TARGETSRV

# convert to mapnik2 (using /usr/local/bin/upgrade_map_xml.py)

upgrade_map_xml.py $DESTDIR/parking/osm-parking.xml $DESTDIR/parking/osm-parking2.xml
upgrade_map_xml.py $DESTDIR/wifi/osm-wifi.xml $DESTDIR/wifi/osm-wifi2.xml
