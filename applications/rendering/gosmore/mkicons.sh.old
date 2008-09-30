#!/bin/bash
# svn co http://svn.openstreetmap.org/applications/share/map-icons/square.big
# svn co http://svn.openstreetmap.org/applications/share/map-icons/classic.big
# svn co http://svn.openstreetmap.org/applications/share/map-icons/square.small
# svn co http://svn.openstreetmap.org/applications/share/map-icons/classic.small
echo Creating temporary directory '"osmpnms/"' and converting icons to pnm
mkdir osmpnms
cd osmpnms
# Create dummy to force GCD to (1,1)
echo 'P6
1 1
255 
0 0 0
'>fix.pnm
for n in `find ../ -iname "*.png"`
do 
  A=${n%%.png}
  B=${A//\//_}
  pngtopnm -background \#11EE22 $n |pnmdepth 255 >"${B:3}.pnm"
done
# These make nice POIs, but are not needed to render OSM maps :
rm -f *geocach* \
  {classic,square}.{big,small}_{people,waypoint,wlan,rendering}* \

#  {classic,square}.big_*

echo Creating the montage and removing the temporary directory
ulimit -n 2048
../../../netpbm-10.26.46/editor/pnmmontage -data ../icons.csv *.pnm>../icons.pnm
cd ..
rm -rf osmpnms
ppmtobmp icons.pnm >icons.bmp
# Suppress the icons Ulf is using to highlight errors
echo 'classic.big_misc_deprecated.pnm:1:1:1:1
square.big_misc_deprecated.pnm:1:1:1:1
classic.small_misc_deprecated.pnm:1:1:1:1
square.small_misc_deprecated.pnm:1:1:1:1
classic.big_misc_no_icon.pnm:1:1:1:1
square.big_misc_no_icon.pnm:1:1:1:1
classic.small_misc_no_icon.pnm:1:1:1:1
square.small_misc_no_icon.pnm:1:1:1:1' >>icons.csv
