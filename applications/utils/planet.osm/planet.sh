#!/bin/bash
set -e
cur_date=`date +%y%m%d`
cur_planet=planet-${cur_date}.osm
cur_relation=relations-${cur_date}.osm
planet_dir=/var/www/planet.openstreetmap.org/

export PATH='/usr/local/bin:/usr/bin:/bin:/usr/bin/X11'

cd ${planet_dir}

planetdump | gzip -9 > .${cur_planet}.gz.new
planetdump --relations | gzip -9 > .${cur_relation}.gz.new
mv .${cur_planet}.gz.new ${cur_planet}.gz
mv .${cur_relation}.gz.new ${cur_relation}.gz
md5sum ${cur_planet}.gz > ${cur_planet}.gz.md5
md5sum ${cur_relation}.gz > ${cur_relation}.gz.md5

gzip -dc ${cur_planet}.gz | bzip2 -2 > .${cur_planet}.bz2.new
gzip -dc ${cur_relation}.gz | bzip2 -2 > .${cur_relation}.bz2.new
mv .${cur_planet}.bz2.new  ${cur_planet}.bz2
mv .${cur_relation}.bz2.new  ${cur_relation}.bz2
md5sum ${cur_planet}.bz2 > ${cur_planet}.bz2.md5
md5sum ${cur_relation}.bz2 > ${cur_relation}.bz2.md5

#7zr a -t7z -m0=lzma -mx=9 -mfb=64 -md=32m -ms=on  planet-`date +%y%m%d.osm`.7z  ${cur_planet}
#chmod a+r ${cur_planet}.7z
#mv ${cur_planet}.7z ${planet_dir}


#link planet latest to the new file
ln -fs ${cur_planet}.bz2 planet-latest.osm.bz2
ln -fs ${cur_relation}.bz2 relations-latest.osm.bz2
# mangle md5 files for 'latest' ones
rm -f planet-latest.osm.bz2.md5 relations-latest.osm.bz2.md5
sed -e "s/${cur_planet}.bz2/planet-latest.osm.bz2/" ${cur_planet}.bz2.md5 > planet-latest.osm.bz2.md5
sed -e "s/${cur_relation}.bz2/relations-latest.osm.bz2/" ${cur_relation}.bz2.md5 > relations-latest.osm.bz2.md5

#next, create the planet diff from last week to this week

CURPLANETDATE=`echo ${cur_planet}|sed -e 's/\w*-\([0-9]*\).*/\1/'`
PREVPLANETDATE=`ls *.osm.bz2|grep -B1 ${cur_planet}|head -n1|sed -e 's/\w*-\([0-9]*\).*/\1/'`
PREVPLANETBZ=planet-${PREVPLANETDATE}.osm.bz2
TMPFILE=`mktemp`

planetdiff ${PREVPLANETBZ} ${cur_planet}.bz2 | bzip2 -2 > ${TMPFILE}
chmod +r ${TMPFILE}
mv ${TMPFILE} ${planet_dir}/planet-${PREVPLANETDATE}-${CURPLANETDATE}.diff.xml.bz2

rm ${cur_planet}.gz
rm ${cur_relation}.gz
rm ${cur_planet}.gz.md5
rm ${cur_relation}.gz.md5
