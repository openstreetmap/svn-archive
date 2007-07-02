#!/bin/bash


# ---------------------- GSHHS
gshhs=gshhs_h
echo "check $gshhs.b"
mkdir -p ~/osm/GSHHS/
#wget -nd -P ~/osm/GSHHS/ -nv  --mirror http://www.ngdc.noaa.gov/mgg/shorelines/data/gshhs/version1.2/$gshhs.b.gz 
wget -nd -P ~/osm/GSHHS/ -nv  --mirror http://www.ngdc.noaa.gov/mgg/shorelines/data/gshhs/oldversions/$gshhs.b.gz 

if [ ~/osm/GHHS/$gshhs.b.gz" -nt ~/osm/GHHS/$gshhs.b" ] ; then
    echo "Unpack ~/osm/GSHHS/$gshhs.b.gz"
    gunzip -dc ~/osm/GSHHS/$gshhs.b.gz >~/osm/GSHHS/$gshhs.b
fi


for place_config in \
    Config/places-germany.txt \
    Config/places-bavaria*.txt \
    Config/places-turkey*.txt \
    Config/places-UK.txt \
    Config/places-france.txt \
    Config/places-france.txt \
    Config/places-*.txt; do

    csv_area=${place_config#*places-}
    csv_area=${csv_area%.txt}

    case $csv_area in
	*germany*)
	    csv_area="germany"
	    ;;
	*bavari*)
	    csv_area="germany"
	    ;;
        *munich*)
	    csv_area="germany"
	    ;;
	*UK*)
	    csv_area="uk"
	    ;;
	*iow*)
	    csv_area="uk"
	    ;;
	*france*)
	    csv_area="france"
	    ;;
	*)
	    ;;
    esac
    osm_csv=$HOME/osm/planet/csv/osm-${csv_area}.csv


    echo ""
    echo "------------------------------------------------------------------"
    echo "place: '$place_config' with area: '$csv_area'"
    echo ""

    if  echo $csv_area | grep \
	-e world  \
	-e europe \
	;then 
	echo "Ignoring $place_config with $vsc_area"
	continue
    fi

    OSM2CSV="../../utils/export/osm2csv/osm2csv.pl"

    if [ `perl $OSM2CSV   --list-areas | grep $csv_area ` ]; then
	echo "--> Update csv File for Area '$csv_area'"
	osm2csv_cmd="perl $OSM2CSV --area=${csv_area} --update-only -v -v -v -v"
	echo "$osm2csv_cmd"
	$osm2csv_cmd
    fi
    if [ ! -s $osm_csv ] ; then
	echo "Cannot find apropriate csv File ($osm_csv)"
	continue
	osm_csv=~/osm/planet/csv/osm.csv
    fi
    
    places_name=${place_config/*\places-/}
    places_name=${places_name%.txt}
    pdf_name=~/osm/pdf-atlas/osm_atlas-${places_name}.pdf
    #echo "CSV File: $pdf_name $osm_csv"
    echo "------------------------------------------------------------------"
    echo "Updating: " $places_name $pdf_name
    create_atlas_cmd="./pdf-atlas.pl -v -v -d --config=Config/config.txt  --Data=$osm_csv --Places=$place_config"
    echo "perl $create_atlas_cmd"
    echo "     ..."
    time perl $create_atlas_cmd
done
