#! /bin/bash
# run in the current workspace, assuming that "Parking" and "mapnik" are checked out projects.
MAPNIKSRC=../../mapnik
PARKINGSRC=.
DESTDIR=/tmp/deploy
TMPDIR=/tmp/parking-mapnik
#-------------------------------------------------------

MAPNIKPATCHED=$TMPDIR/mapnik
PARKINGPATCHED=$TMPDIR/parking
# cleanup
rm -rf $DESTDIR
rm -rf $TMPDIR
# copy mapnik to temp
mkdir -p $MAPNIKPATCHED
cp -r $MAPNIKSRC/* $MAPNIKPATCHED
# patch mapnik to have local settings
cp ./mapnik-patch/datasource-settings.xml.inc.toolserver $MAPNIKPATCHED/inc/datasource-settings.xml.inc
cp ./mapnik-patch/fontset-settings.xml.inc.toolserver $MAPNIKPATCHED/inc/fontset-settings.xml.inc
cp ./mapnik-patch/settings.xml.inc.toolserver $MAPNIKPATCHED/inc/settings.xml.inc

# copy parking to temp
mkdir -p $PARKINGPATCHED
cp -r $PARKINGSRC/colorents* $PARKINGPATCHED
#TODO ^^^^ remove it ###
cp -r $PARKINGSRC/osm-park* $PARKINGPATCHED
cp -r $PARKINGSRC/parking-inc $PARKINGPATCHED
mv $PARKINGPATCHED/parking-inc $PARKINGPATCHED/inc
cp -r $PARKINGSRC/parking-symbols-src $PARKINGPATCHED
#mv $PARKINGPATCHED/parking-symbols-src $PARKINGPATCHED/parking-symbols
cp -r $MAPNIKSRC/symbols $PARKINGPATCHED

# patch parking to have local settings
cp ./mapnik-patch/datasource-settings.xml.inc.toolserver $PARKINGPATCHED/inc/datasource-settings.xml.inc
cp ./mapnik-patch/fontset-settings.xml.inc.toolserver $PARKINGPATCHED/inc/fontset-settings.xml.inc
cp ./mapnik-patch/settings.xml.inc.toolserver $PARKINGPATCHED/inc/settings.xml.inc

#TODO are these ^^^^ settings the same as with mapnik??? ###

# call converter
python generate_parking_layer_xml.py -s $PARKINGPATCHED -f osm-parktrans-src.xml -d $DESTDIR
#python mapnik_to_bw.py -s $MAPNIKPATCHED -f osm.xml -d $DESTDIR
