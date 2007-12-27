#!/bin/sh

wget \
     -q  \
     --directory-prefix=/var/www/planet.openstreetmap.de/ \
     --level=1 \
     --include-directories=POIs \
     -A .zip \
     --no-host-directories \
     --mirror \
     http://christeck.de/POIs/index.html
