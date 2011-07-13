#! /bin/bash

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
RENDER=/home/project/o/s/m/osm/bin/render

#$RENDER --bbox $BBOX --style /home/kayd/deploy/bw-mapnik/osm-bw.xml --size $SIZE
#mv map.png map-bw-$NOW.png
#$RENDER --bbox $BBOX --style /home/kayd/deploy/bw-noicons/osm-bw-noicons.xml --size $SIZE
#mv map.png map-bw-noicons-$NOW.png
$RENDER --bbox $BBOX --style /home/kayd/deploy/parking/osm-parking.xml --size $SIZE
mv map.png map-parking-$NOW.png
$RENDER --bbox $BBOX --style /home/kayd/deploy/parktrans/osm-parktrans.xml --size $SIZE
mv map.png map-parktrans-$NOW.png
