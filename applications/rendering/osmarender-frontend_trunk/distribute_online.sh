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

echo "*** OSMARENDER FRONTEND *** Setting HTML file for server distribution"

sed -i "s/of_server=\"\"/of_server=\"http:\/\/dev.openstreetmap.org\/~Merio\/osmarender_frontend\/\"/g" osmarender_frontend.html

sed -i "s/cmyk_server=\"\"/cmyk_server=\"http:\/\/dev.openstreetmap.org\/~Merio\/osmarender_frontend\/\"/g" osmarender_frontend.html

echo "*** OSMARENDER FRONTEND *** Setting HTML file to not display input fields for custom files"

sed -i "s/<option value=\"\">No one, I'm writing my file name<\/option>//g" osmarender_frontend.html

sed -i "s/<label id=\"label_write_osm_file\" for=\"osm_file_name_written\">/<label id=\"label_write_osm_file\" for=\"osm_file_name_written\" style=\"display:none;\">/g" osmarender_frontend.html

sed -i "s/<input id=\"osm_file_name_written\" type=\"text\" value=\"data.osm\" \/>/<input id=\"osm_file_name_written\" type=\"text\" value=\"data.osm\"  style=\"display:none;\"\/>/g" osmarender_frontend.html

sed -i "s/<label id=\"label_rules_file_name_written\" for=\"rules_file_name_written\">/<label id=\"label_rules_file_name_written\" for=\"rules_file_name_written\" style=\"display:none;\">/g" osmarender_frontend.html

sed -i "s/<input id=\"rules_file_name_written\" type=\"text\" value=\"osm-map-features-z13.xml\" \/>/<input id=\"rules_file_name_written\" type=\"text\" value=\"osm-map-features-z13.xml\" style=\"display:none;\" \/>/g" osmarender_frontend.html

###
echo "*** OSMARENDER FRONTEND *** Setting HTML file for OSM file to become XML file"
#otherwise document.load() doesn't work in online version
#TODO: find automatically local osm files

sed -i "s/data.osm/data.xml/g" osmarender_frontend.html
sed -i "s/somewhere_in_london.osm/somewhere_in_london.xml/g" osmarender_frontend.html
sed -i "s/rome_centre.osm/rome_centre.xml/g" osmarender_frontend.html

echo "*** OSMARENDER FRONTEND *** Changing .OSM file names to .XML"
mv data.osm data.xml
mv somewhere_in_london.osm somewhere_in_london.xml
mv rome_centre.osm rome_centre.xml
####

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

zip -qr9 osmarender_frontend_online.zip *

mv osmarender_frontend_online.zip ../

cd ..

echo "*** OSMARENDER FRONTEND *** Deleting temp files"

rm -fr osmadistribute/

echo "*** OSMARENDER FRONTEND *** Finished"
