#!/bin/sh

./extractdata.pl Z_36_2.ASC 51.3426 -2.4146 51.4089 -2.3171 > extract.txt
./mkcntr.pl extract.txt 10 > contours.osm
./merge.pl contours.osm data.osm > map.osm

rm tmp.cnt tmp
#rm extract.txt contours.osm
