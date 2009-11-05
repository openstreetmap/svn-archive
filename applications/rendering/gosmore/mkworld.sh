#!/bin/bash
# Convert the planet into all the pak file
# Requirements: Osmosis will require around 4GB of RAM

if ! [ -e cities1000.txt ]
then wget http://download.geonames.org/export/dump/cities1000.zip
     unzip cities1000.zip
fi                                
if ! [ -e lowres.osm ]
then gcc -O2 geonames2osm.c -o geonames2osm
     sort -nr -t$'\t' -k 15 cities1000.txt | ./geonames2osm >lowres.osm
fi
for n in 0*.pnm
do convert $n ${n:0:16}.png
done
bash density.sh
for n in 0*.osm.bz2
do (bzcat $n | egrep -v '</osm'
    cat lowres.osm
    egrep -v '?xml|<osmCha' countries.osm | sed -e 's|/osmChange|/osm|') |
      ./gosmore rebuild
    mv gosmore.pak ${n:0:16}.pak
    # TODO : Two pass build
done
