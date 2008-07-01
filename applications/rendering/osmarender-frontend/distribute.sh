#!/bin/sh

echo "*** OSMARENDER FRONTEND *** Distributing Osmarender Frontend"

cd dojotoolkit/util/buildscripts

echo "*** OSMARENDER FRONTEND *** Building custom dojo distribution"

./build.sh profile=../../../../osmarenderfrontend action=release optimize=shrinksafe layerOptimize=shrinksafe releaseDir=../../../osmarenderfrontend_

cd ../../../

echo "*** OSMARENDER FRONTEND *** Removing past temp files and past .zip file, if any"

rm -fr osmadistribute

rm -f osmarenderfrontend.zip

mkdir osmadistribute

mv osmarenderfrontend_dojo osmadistribute/

echo "*** OSMARENDER FRONTEND *** Copying files to distribute"

cp -r gr cmyk.js CSS_parse.js data.osm LICENSE osmarender_frontend.css osmarender_frontend.js osmarender_frontend.html osmarender.xsl osm-map-features-z12.xml osm-map-features-z13.xml osm-map-features-z14.xml osm-map-features-z15.xml osm-map-features-z16.xml osm-map-features-z17.xml rome_centre.osm somewhere_in_london.osm osmadistribute/

cd osmadistribute

echo "*** OSMARENDER FRONTEND *** Removing .svn hidden files, if any"

rm -rf `find . -type d -name .svn`

echo "*** OSMARENDER FRONTEND *** Setting HTML file"

sed -i "s/dojotoolkit\//osmarenderfrontend_dojo\//g" osmarender_frontend.html

sed -i "s/osmarenderfrontend_dojo\/dojo\/dojo.js/osmarenderfrontend_dojo\/dojo\/dojo.js/g" osmarender_frontend.html

echo "*** OSMARENDER FRONTEND *** Zipping OsmarenderFrontend"

zip -qr9 osmarender_frontend.zip *

mv osmarender_frontend.zip ../

cd ..

echo "*** OSMARENDER FRONTEND *** Deleting temp files"

rm -fr osmadistribute/

echo "*** OSMARENDER FRONTEND *** Finished"
