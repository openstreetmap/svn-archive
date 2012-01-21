#! /bin/bash
# run in the current workspace, assuming that "Parking" and "mapnik" are checked out projects.
MAPNIKSRC=../../mapnik
DESTDIR=/tmp/deploy
TMPDIR=/tmp/parking-mapnik
#-------------------------------------------------------

MAPNIKPATCHED=$TMPDIR/mapnik
# cleanup
rm -rf $DESTDIR
rm -rf $TMPDIR
# copy mapnik to temp
mkdir -p $MAPNIKPATCHED
cp -r $MAPNIKSRC/* $MAPNIKPATCHED
# patch mapnik to have local settings
cp ./mapnik-patch/datasource-settings.xml.inc.trunk $MAPNIKPATCHED/inc/datasource-settings.xml.inc
cp ./mapnik-patch/fontset-settings.xml.inc.trunk $MAPNIKPATCHED/inc/fontset-settings.xml.inc
cp ./mapnik-patch/settings.xml.inc.trunk $MAPNIKPATCHED/inc/settings.xml.inc
# call converter
python mapnik_to_bw.py -s $MAPNIKPATCHED -f osm.xml -d $DESTDIR
