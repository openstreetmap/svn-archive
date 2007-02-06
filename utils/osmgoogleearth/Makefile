
XSLTRANSFORM=xsltproc

map.kmz:	map.kml OSMGoogleEarthCopyright.png	icons/*
	zip $@ $^

map.kml:	data.osm osm2kml.xml osm2kml.xsl
	$(XSLTRANSFORM) osm2kml.xml >$@

