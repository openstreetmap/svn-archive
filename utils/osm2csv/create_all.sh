#!/bin/bash
# This script creates all stripe_*.csv and the concatenates 
# them all. This way we don't need this much of memory for the task

areas=`./osm2csv.pl --list-areas`

dir=`dirname $0 `
for a in $areas ; do
    echo $a | grep 'stripe_' || continue
    echo "=================================================="
    echo "Trying Area $a"
  $dir/osm2csv.pl -v --area=$a 
done
cat ~/osm/planet/csv/stripe_*.csv | sort -u > ~/osm/planet/csv/osm.csv
