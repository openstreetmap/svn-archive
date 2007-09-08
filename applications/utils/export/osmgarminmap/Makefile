
CGPSMAPPER=../bin/cgpsmapper-static
XSLTRANSFORM=xsltproc

map.img:	map.mp
	$(CGPSMAPPER) $<

map.mp:		map.mpl
	./mpl2mp.pl $< >$@

map.mpl:	map.mpx mpx2mp.xsl feature-list.xml
	$(XSLTRANSFORM) feature-list.xml >$@

map.mpx:	data.osm osm2mpx.xml osm2mpx.xsl
	$(XSLTRANSFORM) osm2mpx.xml >$@

