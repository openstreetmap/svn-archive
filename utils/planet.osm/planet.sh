#!/bin/bash
cd /home/steve/bin
./planet.rb > planet-`date +%y%m%d`.osm
bzip2 planet-`date +%y%m%d`.osm
chmod a+r planet-`date +%y%m%d`.osm.bz2
mv planet-`date +%y%m%d`.osm.bz2 /var/www/planet.openstreetmap.org/
