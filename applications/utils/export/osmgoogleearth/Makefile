
XSLTRANSFORM=xsltproc

map.kmz:	map.kml OSMCopyright.png icons/*
	zip $@ $^

map.kml:	data.osm osm2kml.xml osm2kml.xsl
	$(XSLTRANSFORM) osm2kml.xml >$@

