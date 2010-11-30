#!/bin/bash
# Copyright 2010 Nic Roets as detailed in the README file.
# The Osm.org Routing demo daily update script
# The script runs as a cron job.

# make CFLAGS='-O2 -DRES_DIR=\"/usr/share/gosmore/\" -DHEADLESS -DONLY_ROUTING -DLD_CTRL'
OSMOSIS=$HOME/osmosis-0.38/bin/osmosis
FIRST_OSC=$(readlink $HOME/planet-latest.osm.bz2 |
                         sed 's/[^0-9]\|2$//g')
# A crude way of getting the date of the planet e.g. 100716
PLANET=$HOME/planet-$FIRST_OSC.osm.bz2
# List of change files from youngest to oldest
cd $HOME/daily
# Get into working directory

if [ g.o -nt lock ] ||
   ! wget -c http://planet.openstreetmap.org/daily/$(date -d 'now - 1 day' +%Y%m%d)-$(date +%Y%m%d).osc.gz ||
   ! gzip --test $(date -d 'now - 1 day' +%Y%m%d)-$(date +%Y%m%d).osc.gz
then exit 0
fi

cd $HOME/gosmore
OSC_LIST="ls -r $HOME/daily/*.gz "

# I tried a number of different things (in comments) before I had success.
# PARAM=$($OSC_LIST |grep -A 30 20${FIRST_OSC}- |
#     ( read first
#       echo --rxc $first
#       awk '{ print "--rxc ", $1, " --mc conflictResolutionMethod=version" }'))
# CMD="$OSMOSIS $PARAM --rx /dev/stdin --ac --wx -"

#----
# nice -n 20 $OSMOSIS $PARAM --wxc week.osc
# exit 0
# CMD="$OSMOSIS --rxc week.osc --rx /dev/stdin --ac --wx -"

#----
# PARAM=$($OSC_LIST |grep -A 30 20${FIRST_OSC}- |
#    ( read first
#       echo --rxc $first --rx /dev/stdin --ac
#       awk '{ print "--rxc ", $1, " --ac" }'))
# CMD="$OSMOSIS $PARAM --wx -"

#----
PARAM=$($OSC_LIST |grep -B 30 20${FIRST_OSC}- | awk '{ print "--rxc ", $1 }' )
AC=$($OSC_LIST    |grep -B 30 20${FIRST_OSC}- | awk '{ print "--ac " }')

CMD="$OSMOSIS $PARAM --rx /dev/stdin $AC --wx -"

echo $CMD
# exit

if nice -n 20 bzcat $PLANET | nice -n 20 $CMD |
   (cd relations; nice -n 20 ../gosmore sortRelations) |
   nice -n 20 ./bboxSplit -89 -179 89 -30 "./gosmore rebuild" g.o \
                         -89 -30 89 179 "cd e; ../gosmore rebuild" europe/g.o
then mv america/gosmore.pak am.pak
     mv europe/gosmore.pak eu.pak
     mv gosmore.pak america/
     mv e/gosmore.pak europe/
     mv relations/relations.tbl .
fi

sleep 2
touch lock
./lockMmap $(date +'24*3600 - (%s - 18*3600) %% (24*3600)' |bc) america/gosmore.pak europe/gosmore.pak
