#!/bin/sh

areas=`./osm2csv.pl --list-areas`

for a in $areas ; do
    echo "=================================================="
    echo "Trying Area $a"
  ./osm2csv.pl -v --area=$a 
done
