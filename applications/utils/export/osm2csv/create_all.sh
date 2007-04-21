#!/bin/bash
# This script creates all stripe_*.csv and the concatenates 
# them all. This way we don't need this much of memory for the task

dir=`dirname $0 `
areas=`$dir/osm2csv.pl --list-areas | grep -v -e africa -e stripe_`

for a in $areas ; do

    echo ""
    echo "=================================================="
    echo "Trying Area $a"
  $dir/osm2csv.pl -v --update-only --area=$a 
done
cat ~/osm/planet/csv/osm-stripe_*.csv | sort -u > ~/osm/planet/csv/osm.csv
