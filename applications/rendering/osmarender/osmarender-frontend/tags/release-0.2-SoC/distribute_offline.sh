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

cp -r gr cmyk.js CSS_parse.js data.xml LICENSE osmarender_frontend.css osmarender_frontend.js osmarender_frontend.html osmarender.xsl osm-map-features-z12.xml osm-map-features-z13.xml osm-map-features-z14.xml osm-map-features-z15.xml osm-map-features-z16.xml osm-map-features-z17.xml README_OFFLINE rome_centre.xml somewhere_in_london.xml zurich_google.xml osmadistribute/

cd osmadistribute

mv README_OFFLINE README

echo "*** OSMARENDER FRONTEND *** Removing .svn hidden files, if any"

rm -rf `find . -type d -name .svn`

echo "*** OSMARENDER FRONTEND *** Setting HTML file for Dojo"

sed -i "s/dojotoolkit\//osmarenderfrontend_dojo\//g" osmarender_frontend.html

sed -i "s/osmarenderfrontend_dojo\/dojo\/dojo.js/osmarenderfrontend_dojo\/dojo\/dojo.js/g" osmarender_frontend.html

sed -i '/<script type="text\/javascript" src="osmarenderfrontend_dojo\/dojo\/dojo.js/ a\ \t\t<script type="text\/javascript" src="osmarenderfrontend_dojo/dojo/osmafrontend.js"><\/script>' osmarender_frontend.html

sed -i '/<script type="text\/javascript" src="osmarenderfrontend_dojo\/dojo\/dojo.js/ a\ \t\t<script type="text\/javascript" src="osmarenderfrontend_dojo/dojo/juice.js"><\/script>' osmarender_frontend.html

#sed -i '/<script type="text\/javascript" src="osmarenderfrontend_dojo\/dojo\/dojo.js/ a\ \t\t<script type="text\/javascript" src="osmarenderfrontend_dojo/dojo/my_dojo.js"><\/script>' osmarender_frontend.html

echo "*** OSMARENDER FRONTEND *** Changing all permissions"
#Thanks to http://erlug.linux.it/pipermail/erlug/2005-12/msg00081.html

FILE_MOD="644"
DIR_MOD="755"

ls | while read file; do
    if [ -d "$file" ]; then
        echo " Entering directory '$file'"
        
        chmod u+rwx "$file" || { echo " No permission to enter
'$file'" 
                                 continue; }
        cd "$file"; $0 $@; cd -
        
        chmod $DIR_MOD "$file" 2> /dev/null && echo " Directory '$file'
set."
    elif [ -f "$file" ]; then
        echo -n "  $file ... "
        chmod $FILE_MOD "$file" && echo " [ OK ]"
    else
        echo " Ignoring '$file', probably a link."
    fi
done

echo "*** OSMARENDER FRONTEND *** Deleting unnecessary dojo files"
#TODO: seems that, once packed, only these files are needed. Need to verify in the future
# for "dojo" dir
# dojo.js
# osmafrontend.js
# /resources
# /nls
#
# for "dijit" dir
# /themes
# /nls
#
# for "dojox" dir, probably only strictly needed resources.

#Thanks to http://trac.dojotoolkit.org/browser/util/trunk/buildscripts/build_mini.sh

cd osmarenderfrontend_dojo

echo "*** OSMARENDER FRONTEND *** Deleting dojo and dojox tests and demos"
for i in dojo/dojox/*
do
	if [ -d $i ]; then
		rm -rf $i/tests/
		rm -rf $i/demos/
	fi
done

echo "*** OSMARENDER FRONTEND *** Deleting unnecessary dojo tests, dijit files and util"
rm -rf dijit/tests
rm -rf dijit/demos
rm -rf dijit/bench
rm -rf dojo/tests
rm -rf dojo/tests.js
rm -rf util

echo "*** OSMARENDER FRONTEND *** Removing dijit templates"

rm -rf dijit/templates
rm -rf dijit/form/templates
rm -rf dijit/layout/templates

echo "*** OSMARENDER FRONTEND *** Removing uncompressed files"

find . -name *.uncompressed.js -exec rm '{}' ';'

echo "*** OSMARENDER FRONTEND *** Moving compressed files to the outside directory"

mv cmyk ..
mv osmarender_frontend ..
mv juice ..

cd ..

#cd osmarender_frontend
#rm -rf widgets
#cd ..

echo "*** OSMARENDER FRONTEND *** Zipping OsmarenderFrontend"

zip -qr9 osmarender_frontend_offline.zip *

mv osmarender_frontend_offline.zip ../

cd ..

echo "*** OSMARENDER FRONTEND *** Deleting temp files"

rm -fr osmadistribute/

echo "*** OSMARENDER FRONTEND *** Finished"
