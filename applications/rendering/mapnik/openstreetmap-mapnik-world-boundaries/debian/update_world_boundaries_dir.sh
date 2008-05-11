#!/bin/bash

instal_dir=debian/openstreetmap-mapnik-world-boundaries/usr/share/mapnik/world_boundaries

# remove old files
rm -rf debian/openstreetmap-mapnik-world-boundaries/usr/share/mapnik/world_boundaries

mkdir -p mirror

if ! echo "$@" | grep "no-mirror" ; then

    unset http_proxy

    # (ca. 51MB)
    wget \
	-v \
	--no-host-directories \
	--no-directories \
	--directory-prefix=mirror \
	--mirror \
	http://tile.openstreetmap.org/world_boundaries-spherical.tgz

    # (~200MB)
    wget \
	-v \
	--mirror \
	--no-directories \
	--no-host-directories \
	--directory-prefix=mirror \
	http://hypercube.telascience.org/~kleptog/processed_p.zip
fi

mkdir -p ${instal_dir}/world_boundaries

echo "Unpack world_boundaries-spherical"
tar -xvz \
    --directory=${instal_dir}/ \
    -f mirror/world_boundaries-spherical.tgz

echo "Unpack processed_p.zip"
unzip mirror/processed_p.zip -d ${instal_dir}/world_boundaries

mv ${instal_dir}/world_boundaries/coastlines/*  ${instal_dir}/world_boundaries/
rmdir  ${instal_dir}/world_boundaries/coastlines


echo "Add local stuff"
cp mirror/local/world_bnd* ${instal_dir}/world_boundaries/

if false; then
    # Old Method copying from local installation
    cp /usr/share/mapnik/world_boundaries/* \
	${instal_dir}/world_boundaries/
fi