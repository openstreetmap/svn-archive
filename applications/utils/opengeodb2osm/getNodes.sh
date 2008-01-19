#!/bin/sh
DATE=`date +%Y-%m-%d`                                                          
NODE="nodes$DATE.osm"
wget -d 'http://www.informationfreeway.org/api/0.5/node[place=*][bbox=0,40,20,56]' -O $NODE
gzip $NODE
perl places2sql.pl $NODE.gz |gzip >$NODE.mysql.gz