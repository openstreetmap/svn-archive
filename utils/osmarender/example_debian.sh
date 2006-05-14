#!/bin/sh
sudo apt-get install xalan
xalan -in osm-map-features.xml -out data.svg
