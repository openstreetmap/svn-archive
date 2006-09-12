#!/bin/sh

# ---------------------- GSHHS
gshhs=gshhs_h
echo "check $gshhs.b"
mkdir -p ~/osm/GSHHS/
wget -nd -P ~/osm/GSHHS/ -nv  --mirror http://www.ngdc.noaa.gov/mgg/shorelines/data/gshhs/version1.2/$gshhs.b.gz 

if [ ~/osm/GHHS/$gshhs.b.gz" -nt ~/osm/GHHS/$gshhs.b" ] ; then
    echo "Unpack ~/osm/GSHHS/$gshhs.b.gz"
    gunzip -dc ~/osm/GSHHS/$gshhs.b.gz >~/osm/GSHHS/$gshhs.b
fi


for place_config in Config/places-*.txt; do
    csv_area=${place_config#*places-}
    csv_area=${csv_area%.txt}

    echo $csv_area | grep africa && continue
    echo $csv_area | grep australia && continue
    echo $csv_area | grep world && continue
    echo $csv_area | grep england && continue

#    echo $csv_area | grep germany || continue

    echo $csv_area
    perl ../osm2csv/osm2csv.pl "$planet_file" --area=${csv_area} -v --update-only


    places_name=${place_config/*\places-/}
    places_name=${places_name%.txt}
    pdf_name=~/osm/Results/osm_atlas-${places_name}.pdf
    if [ \( -s "$pdf_name" \) -a \( "$pdf_name" -nt "~/osm/planet/csv/osm-${csv_area}.csv" \) ] ; then
	echo "Up to Date: " $places_name $pdf_name
	continue
    fi
    echo "Updating: " $places_name $pdf_name
    time perl osm-pdf-atlas.pl -d  \
	--config=Config/config.txt \
	--Places=$place_config
done
