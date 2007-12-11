#! /bin/sh

HERE=$(dirname $0)
HERE=/home/osm/maps4glopus


#
#
#
fetch_map() {

    coords=$1
    where=$(echo $2 | tr "[:upper:]" "[:lower:]")_$(echo $1 | sed -e 's|,|_|g')
    neighbors=${3:-2}
    size=${4:-2}

    echo $*

    [ ! -d ${HERE}/logs ] && mkdir ${HERE}/logs

    rm -rf ${HERE}/map

    ${HERE}/../taho/taho.pl -tilename=${coords} -size=${size} -neighbormaps=${neighbors} > ${HERE}/logs/taho-${where}.log

    [ ! -d ${HERE}/map.full ] && mkdir ${HERE}/map.full
    cp -arl ${HERE}/map/*.png ${HERE}/map.full
    cp -arl ${HERE}/map/*.kal ${HERE}/map.full

    rm -rf ${HERE}/map.${where}
    mkdir ${HERE}/map.${where}
    mv ${HERE}/map/*.png  ${HERE}/map.${where}
    mv ${HERE}/map/*.kal  ${HERE}/map.${where}
    rm -rf ${HERE}/map.${where}/*.log

    find ${HERE}/map.${where} -name \*.kal -print0 | xargs -I '{}' -0 cat '{}' | \
	cat > ${HERE}/map.full/kals.${where}.txt

    GMF_GENERATE=${HERE}/gmf-generate
    GMF_GENERATE="wine /home/hakan/Archive/2007/2007-11/geotools-dev-bin-2007-11-11/mingw/gmf-generate.exe"
    find ${HERE}/map.${where} -name \*.kal -print0 | xargs -I '{}' -0 cat '{}' | \
 	${GMF_GENERATE} ${HERE}/map.${where} ${HERE}/map.full/${where}.gmf > ${HERE}/logs/gmf-generate-${where}.log

    [ ! -d ${HERE}/map.kml ] && mkdir ${HERE}/map.kml
    GMF_INDEX=${HERE}/gmf-index
    GMF_INDEX="wine /home/hakan/Archive/2007/2007-11/geotools-dev-bin-2007-11-11/mingw/gmf-index.exe"
    ${GMF_INDEX} ${HERE}/map.full/${where}.gmf >  ${HERE}/map.${where}/zzz-${where}.kml
    cp ${HERE}/map.${where}/zzz-${where}.kml ${HERE}/map.kml/${where}.kml

    rm -f ${HERE}/map.${where}/*.kal

    # rm -rf ${HERE}/map
}



#
#
#
cd ${HERE}

rm -rf ${HERE}/map.full

# rm -rf ${HERE}/map.kml


#
#
#

# 2177 / 1421 / 12 / Germering
fetch_map 14,8709,5686 Germering
fetch_map 14,8714,5686 Germering
fetch_map 14,8709,5691 Gauting

# 2179 / 1421 / 12 / Germering
fetch_map 12,2179,1421 Muenchen
fetch_map 14,8719,5686 Muenchen




#
#  Re-create full gmf file
#
where=full.de

rm -rf ${HERE}/tostick/map.tiles/de

mkdir -p ${HERE}/tostick/map.tiles/de
cp ${HERE}/map.full/*.kal ${HERE}/tostick/map.tiles/de
cp ${HERE}/map.full/*.png ${HERE}/tostick/map.tiles/de

# mkdir ${HERE}/tostick/map.gmf
# cp ${HERE}/map.full/*.gmf ${HERE}/tostick/map.gmf

mkdir ${HERE}/tostick/glopus
cp /home/archive/export/GPS-Tracks/Waypoints.asc  ${HERE}/tostick/glopus

chmod a+w ${HERE}/tostick/map.tiles/de



#########
#
#
#
#


rm -rf ${HERE}/map.full


## fetch_map 12,2370,1595 Marmaris     # (1596, lieber y-1, damit Marmaris am unteren Rand bleibt)

## fetch_map 12,2385,1604 Kas          # (1605, lieber y-1, damit Kas am unteren Rand bleibt)

## fetch_map 12,2397,1594 Antalya      # (1595, lieber y-1, damit Antalya am unteren Rand bleibt)

fetch_map 12,2406,1598 Manavgat     # (2405,1597, lieber x+1, y+1 damit es im Zentrum bleibt)

# Degirmen Lokantasi, 12,2409,1596 
fetch_map 12,2409,1596 Degirmen
fetch_map 14,9638,6385 Degirmen

fetch_map 12,2412,1600 Alanya
fetch_map 12,2409,1599 Alanya 0
fetch_map 12,2408,1599 Alanya 0

# 2392 / 1583 / 12 / Burdur
## fetch_map 12,2392,1583 Burdur

# 2395 / 1583 / 12 / Isparta
## fetch_map 12,2395,1583 Isparta

# 2402 / 1595 / 12 / Melek Ciftligi
## fetch_map 12,2401,1595 Melek_Ciftligi # (2402,1595, lieber x-1 damit es nicht mit Manavgat kollodiert)
# fetch_map 13,4804,3190 Melek_Ciftligi

# 2392 / 1580 / 12 / Keciborlu
## fetch_map 12,2392,1580 Keciborlu
# fetch_map 13,4785,3161 Keciborlu
# fetch_map 14,9571,6322 Keciborlu

#
#  Re-create full gmf file
#
where=full.tr

rm -rf ${HERE}/tostick/map.tiles/tr

mkdir -p ${HERE}/tostick/map.tiles/tr
cp ${HERE}/map.full/*.kal ${HERE}/tostick/map.tiles/tr
cp ${HERE}/map.full/*.png ${HERE}/tostick/map.tiles/tr

# cp ${HERE}/map.full/*.gmf ${HERE}/tostick/map.gmf

mkdir ${HERE}/tostick/glopus
cp /home/archive/export/GPS-Tracks/Waypoints.asc  ${HERE}/tostick/glopus

chmod a+w ${HERE}/tostick/map.tiles/tr



#
#
#
mkdir ${HERE}/tostick/map.gmf

chmod a+w ${HERE}/tostick/map.gmf

