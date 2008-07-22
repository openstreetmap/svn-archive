#!/bin/sh
echo Creating temporary directory '"osmpnms/"' and converting icons to pnm
mkdir osmpnms
cd osmpnms
# Create dummy to force GCD to (1,1)
echo 'P3
1 1
255
255'>fix.pnm
for n in `find /usr/share/map-icons/ -iname "*.png"`
do 
  A=${n%%.png}
  B=${A//\//_}
  pngtopnm -background \#11EE22 $n |pnmdepth 255 >"${B:21}.pnm"
done
# These make nice POIs, but are not needed to render OSM maps :
rm -f *geocach* {classic,square}.{big,small}_{people,waypoint,wlan}* \
  {svg_tn,japan_tn}_{people,waypoint,wlan}* svg_* *_misc_no_icon.png

echo Creating the montage and removing the temporary directory
ulimit -n 2048
../../netpbm-10.26.46/editor/pnmmontage -data ../icons.csv *.pnm>../icons.pnm
cd ..
rm -rf osmpnms
ppmtobmp icons.pnm >icons.bmp
