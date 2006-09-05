#!/bin/sh

export planet_file=planet-060903-a.osm.gz
export planet_dir=~/osm/planet

gshhs=gshhs_h
echo "check $gshhs.b"
mkdir -p ~/osm/GSHHS/
wget -nd -P ~/osm/GSHHS/ -nv  --mirror http://www.ngdc.noaa.gov/mgg/shorelines/data/gshhs/version1.2/$gshhs.b.gz 

if [ ~/osm/GHHS/$gshhs.b.gz" -nt ~/osm/GHHS/$gshhs.b" ] ; then
    echo "Unpack ~/osm/GSHHS/$gshhs.b.gz"
    gunzip -dc ~/osm/GSHHS/$gshhs.b.gz >~/osm/GSHHS/$gshhs.b
fi

perl ../planet-mirror/planet-mirror.pl

csv_area="europe"
if [ "$planet_dir/$planet_file" -nt "~/osm/csv/osm-${csv_area}.csv" ] ; then
    echo "Have to create  ~/osm/csv/osm.csv"
    time perl ../osm2csv.pl "$planet_dir/$planet_file"
else 
    echo "osm.csv seems up to date"
fi


for place_config in Config/places-*.txt; do
   places_name=${place_config/*\places-/}
   places_name=${places_name%.txt}
   pdf_name=~/osm/Results/osm_atlas-${places_name}.pdf
   if [ \( -s "$pdf_name" \) -a \( "$pdf_name" -nt "~/osm/csv/osm-${csv_area}.csv" \) ] ; then
	echo "Up to Date: " $places_name $pdf_name
	continue
   fi
   echo "Updating: " $places_name $pdf_name
   time perl osm-pdf-atlas.pl -d  \
			--config=Config/config.txt \
			--Places=$place_config
done
