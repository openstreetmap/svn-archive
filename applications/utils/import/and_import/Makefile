# This Makefile is intended to automate the process of uploading the AND
# data. For now it only handles the cutting out process, but the importing
# will come at some stage.

# Where the planet file is
PLANET=$(HOME)/osm/planet-070815.osm
# The osmosis jar file
# Note: version must be at least from 21 August 2007 (0.8.0)
OSMOSIS=/usr/local/bin/osmosis.jar

all: data/nld-delete.osm data/nld-leftover.osm

# Data directory must exist
data:
	mkdir -p data
	
# Download Ante's NLD boundary
data/nldgrens.osm: data
	wget -q --mirror http://people.vrijschrift.org/~ante/osm/nld-simplified6.osm -O $@
	
# Convert the boundary to a polygon file and handle exclusions
data/nldgrens.polygon: data/nldgrens.osm 
	./osm2polygon.pl data/nldgrens.osm > $@

# Make a polygon file for the aras to be extracted	
data/extract.polygon: protected_areas
	@echo -e '1\n  0 0\n 90 0\n 90 90\n 0 90\n 0 0\nEND' > $@
	./protected2polygon.pl >> $@
	
# Extract NLD from the planet file
data/nld.osm: data/nldgrens.polygon $(PLANET)
	../../osm-extract/polygons/extract-polygon.pl -v -i $(PLANET) -o $@ -p data/nldgrens.polygon

# Remove protected areas from NLD file
data/nld-unprotected.osm: data/extract.polygon data/nld.osm
	../../osm-extract/polygons/extract-polygon.pl -v -i data/nld.osm -o $@ -p data/extract.polygon

# From the extracted data we determine what we actually want to delete
data/nld-delete.osm: data/nld-unprotected.osm planetosm-deleteby-tags.pl
	./planetosm-deleteby-tags.pl --output=osmchange data/nld-unprotected.osm > $@

data/nld-leftover.osm: $(OSMOSIS) data/nld.osm data/nld-delete.osm
	java -jar $(OSMOSIS) --read-xml file="data/nld.osm" --read-xml-change file="data/nld-delete.osm" outPipe.0=change --sort-change inPipe.0=change outPipe.0=change2 --apply-change inPipe.1=change2 --write-xml file="data/nld-leftover.osm"
	
# Nicity so that if the user forgets to update the location of their planet
# file they get a nice error message
$(PLANET):
	@echo "======================================================================="
	@echo "ERROR: Planet file not found: $(PLANET)"
	@echo "Please enter the location of the planet file at the top of the Makefile"
	@exit 1
# Same for osmosis
$(OSMOSIS):
	@echo "======================================================================="
	@echo "ERROR: Osmosis not found: $(OSMOSIS)"
	@echo "To generate nld-leftover.osm we use osmosis"
	@echo "It is available here: http://www.bretth.com/osmosis/osmosis-latest.zip"
	@echo "Please specify the location of the jar file in the Makefile"
	@exit 1
