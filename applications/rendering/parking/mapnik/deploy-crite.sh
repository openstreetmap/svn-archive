#! /bin/bash
# run in the current workspace, assuming that "Parking" and "mapnik" are checked out projects.
MAPNIKSRC=../../mapnik
PARKINGSRC=.
TARGETSRV=crite
DESTDIR=/tmp/deploy-neu-$TARGETSRV
TMPDIR=/tmp/tmp-kay-mapnik

python setup_target_style.py -m $MAPNIKSRC -t $TMPDIR -d $DESTDIR -v $TARGETSRV
