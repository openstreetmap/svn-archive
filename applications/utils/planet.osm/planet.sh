#!/bin/bash
cur_date=`date +%y%m%d`
cur_planet=planet-${cur_date}.osm
planet_dir=/var/www/planet.openstreetmap.org/

export PATH='/usr/local/bin:/usr/bin:/bin:/usr/bin/X11'

cd ${planet_dir}

planetdump | gzip -9 > .${cur_planet}.gz.new
mv .${cur_planet}.gz.new ${cur_planet}.gz

gzip -dc ${cur_planet}.gz | bzip2 -2 > .${cur_planet}.bz2.new
mv .${cur_planet}.bz2.new  ${cur_planet}.bz2

#7zr a -t7z -m0=lzma -mx=9 -mfb=64 -md=32m -ms=on  planet-`date +%y%m%d.osm`.7z  ${cur_planet}
#chmod a+r ${cur_planet}.7z
#mv ${cur_planet}.7z ${planet_dir}


#link planet latest to the new file
ln -fs ${cur_planet}.bz2 planet-latest.osm.bz2

#next, create the planet diff from last week to this week

CURPLANETDATE=`echo ${cur_planet}|sed -e 's/\w*-\([0-9]*\).*/\1/'`
PREVPLANETDATE=`ls *.osm.bz2|grep -B1 ${cur_planet}|head -n1|sed -e 's/\w*-\([0-9]*\).*/\1/'`
PREVPLANETBZ=planet-${PREVPLANETDATE}.osm.bz2
TMPFILE=`mktemp`

planetdiff ${PREVPLANETBZ} ${cur_planet}.bz2 | bzip2 -2 > ${TMPFILE}
chmod +r ${TMPFILE}
mv ${TMPFILE} ${planet_dir}/planet-${PREVPLANETDATE}-${CURPLANETDATE}.diff.xml.bz2

rm ${cur_planet}.gz
