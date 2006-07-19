#!/bin/sh

# check if xalan is installed
xalan -v || sudo apt-get install xalan

# check if librsvg2-bin is installed
rsvg-convert -v || sudo apt-get install librsvg2-bin

# get current planet.osm
export planet_file=planet-2006-07-a.osm.bz2
export planet_dir=~/.gpsdrive/MIRROR/osm

mkdir -p $planet_dir

echo "Check $planet_file"
wget -nd -P "$planet_dir" -nv  --mirror http://www.ostertag.name/osm/planet/$planet_file

ln -s "$planet_dir/$planet_file" data.osm


# Start rendering
xalan -in osm-map-features.xml -out data.svg

# svg --> png
rsvg-convert data.svg >data.png

