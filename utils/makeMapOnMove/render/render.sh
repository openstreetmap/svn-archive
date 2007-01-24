cp ../data.osm ./
nice xmlstarlet tr osmarender.xsl osm-map-features.xml > output.svg
nice inkscape -D -w 400 -b FFFFFF -e output.png2 output.svg
rm output.png
mv output.png2 output.png
