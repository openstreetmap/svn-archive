#!/bin/sh

export planet_file=planet-2006-07-a.osm.bz2
export planet_dir=~/.gpsdrive/MIRROR/osm

mkdir -p $planet_dir

echo "Check $planet_file"
wget -nd -P "$planet_dir" -nv  --mirror http://www.ostertag.name/osm/planet/$planet_file

check "gshhs_h.b"
wget -nd -P "Data" -nv  --mirror http://www.ngdc.noaa.gov/mgg/shorelines/data/gshhs/version1.2/gshhs_h.b.gz 

if [ "Data/gshhs_h.b.gz" -nt "Data/gshhs_h.b" ] ; then
    gunzip -dc Data/gshhs_h.b.gz >Data/gshhs_h.b
fi


if [ "$planet_dir/$planet_file" -nt "Data/osm.txt" ] ; then
    echo "Have to create Data/osm.txt"
    time perl planet_osm2txt.pl "$planet_dir/$planet_file"
else 
    echo "osm.txt seems up to date"
fi

time perl osm-pdf-atlas.pl Config/config.txt
