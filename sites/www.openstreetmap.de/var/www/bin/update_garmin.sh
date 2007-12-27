#!/bin/sh

wget \
     -q \
     --directory-prefix=/var/www/planet.openstreetmap.de/garmin \
     --cut-dirs=1 \
     --recursive \
     --level=3 \
     --no-host-directories \
     --mirror \
     http://www.smash-net.org/openstreetmap/latest/


find /var/www/planet.openstreetmap.de/garmin/ -name "index.html\?*" -print0 | xargs -0 rm
rm  /var/www/planet.openstreetmap.de/garmin/*.gif
