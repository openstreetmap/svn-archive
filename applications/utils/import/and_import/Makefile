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
	
# Convert the boundary to a polygon file and handle exclusions
data/nldpolygons.txt: data/nldgrens.osm protected_areas
	./osm2polygon.pl data/nldgrens.osm > $@
	./protected2polygon.pl >> $@
	
# Extract NLD from the planet file
data/nld.osm: data/nldpolygons.txt $(PLANET)
	../../osm-extract/polygons/extract-polygon.pl -v -i $(PLANET) -o $@ -p $<

# From the extracted data we determine what we actually want to delete
data/nld-delete.osm: data/nld.osm planetosm-deleteby-tags.pl
	./planetosm-deleteby-tags.pl --output=osmchange data/nld.osm > $@

# Nicity so that if the user forgets to update the location of their planet
# file they get a nice error message
$(PLANET):
	@echo "======================================================================="
	@echo "ERROR: Planet file not found: $(PLANET)"
	@echo "Please enter the location of the planet file at the top of the Makefile"
	@exit 1
