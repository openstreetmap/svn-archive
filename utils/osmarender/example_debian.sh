#!/bin/sh

# check if xalan is installed
xalan -v || sudo apt-get install xalan

# check if librsvg2-bin is installed
rsvg-convert -v || sudo apt-get install librsvg2-bin

download_url='http://www.openstreetmap.org/api/0.3/map?'
download_bbox='bbox=11.727117425387659,48.16020260299748,11.776499448945952,48.18914763168421'
if [ ! -s data.osm ] ; then
    curl --netrc -o data.osm "$download_url$download_bbox"
fi

# Start rendering
xalan -in osm-map-features.xml -out data.svg

# svg --> png
rsvg-convert data.svg >data.png

