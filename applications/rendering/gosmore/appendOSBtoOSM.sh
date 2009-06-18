#!/bin/bash -u

# this is an example script for adding openstreetbugs to a osm pak file
# example usage: bzcat planet-extract.osm.bz2 \
#   | ./appendOSBtoOSM.sh 'http://openstreetbugs.schokokeks.org/api/0.1/getGPX?b=-27.60787&t=-27.28064&l=152.69114&r=153.30363&open=yes' \
#   | ./gosmore rebuild

url=$1

tempfolder=$(mktemp -d)

wget --quiet $url \
    -O $tempfolder/OSB.gpx

# convert to osm format using gpsbabel
gpsbabel -i GPX -f $tempfolder/OSB.gpx \
    -o osm,tagnd="openstreetbug:yes" -F $tempfolder/OSB.osm

# concatenate with existing osm file
egrep -v '^</osm' /dev/stdin
egrep -v '^<(osm|\?xml)' $tempfolder/OSB.osm

rm -r $tempfolder