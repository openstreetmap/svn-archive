# This Makefile is intended to automate the process of uploading the AND
# data. For now it only handles the cutting out process, but the importing
# will come at some stage.

# Where the planet file is
PLANET=$(HOME)/osm/planet-070815.osm

all: data/nld-delete.osm

# Data directory must exist
data:
	mkdir -p data
	
# Download Ante's NLD boundary
data/nldgrens.osm: data
	wget -q --mirror http://people.vrijschrift.org/~ante/osm/nld-simplified6.osm -O $@
	
# Convert the boundary to a polygon file
data/nldpolygons.txt: data/nldgrens.osm
	perl -w osm2polygon.pl  data/nldgrens.osm > $@
	
# Extract NLD from the planet file
data/nld.osm: data/nldpolygons.txt $(PLANET)
	../../osm-extract/polygons/extract-polygon.pl -v -i $(PLANET) -o $@ -p $<

data/nld-delete.osm: data/nld.osm
	./planetosm-deleteby-tags.pl data/nld.osm > $@
	 
	