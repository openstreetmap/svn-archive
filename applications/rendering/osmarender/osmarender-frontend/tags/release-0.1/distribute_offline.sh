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

echo "*** OSMARENDER FRONTEND *** Setting HTML file for Dojo"

sed -i "s/dojotoolkit\//osmarenderfrontend_dojo\//g" osmarender_frontend.html

sed -i "s/osmarenderfrontend_dojo\/dojo\/dojo.js/osmarenderfrontend_dojo\/dojo\/dojo.js/g" osmarender_frontend.html

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

echo "*** OSMARENDER FRONTEND *** Zipping OsmarenderFrontend"

zip -qr9 osmarender_frontend_offline.zip *

mv osmarender_frontend_offline.zip ../

cd ..

echo "*** OSMARENDER FRONTEND *** Deleting temp files"

rm -fr osmadistribute/

echo "*** OSMARENDER FRONTEND *** Finished"
