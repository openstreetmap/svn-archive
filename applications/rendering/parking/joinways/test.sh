#! /bin/bash

# Würzburg komplett bis IKEA
#BBOX=9.911,49.774,9.9929,49.8282
BBOX=9.9315,49.78324,9.97245,49.81036

JOINWAYS=/home/osm/workspace/parking/joinways/joinways.py
$JOINWAYS --bbox $BBOX

# now, render a sample image

exit 0
blablubb

# style directory
STYLES=/home/osm/styles-deploy
# image target directory
IMAGES=/home/osm/wuerzburg-render-film

# Mozart
# BBOX=9.944475,49.796291,9.948402,49.799359

# Würzburg komplett bis IKEA
#BBOX=9.911,49.774,9.9929,49.8282
BBOX=9.9315,49.78324,9.97245,49.81036

# Size = Full HD
SIZE=1920x1080

# Timestamp
NOW=`date +%FT%H-%M-%S`

# Render command
RENDER=/home/osm/bin/render

#$RENDER --bbox $BBOX --size $SIZE --style $STYLES/bw-mapnik/osm-bw.xml --file $IMAGES/map-bw-$NOW
#$RENDER --bbox $BBOX --size $SIZE --style $STYLES/bw-noicons/osm-bw-noicons.xml --file $IMAGES/map-bw-noicons-$NOW
#$RENDER --bbox $BBOX --size $SIZE --style $STYLES/parktrans/osm-parktrans.xml --file $IMAGES/map-parktrans-$NOW
$RENDER --bbox $BBOX --size $SIZE --style $STYLES/parking/osm-parking.xml --file $IMAGES/map-parking-$NOW
$RENDER --bbox $BBOX --size $SIZE --style $STYLES/wifi/osm-wifi.xml --file $IMAGES/map-wifi-$NOW
$RENDER --bbox $BBOX --size $SIZE --style $STYLES/approach/osm-approach.xml --file $IMAGES/map-approach-$NOW
