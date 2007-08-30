# This Makefile is intended to automate the process of uploading the AND
# data. For now it only handles the cutting out process, but the importing
# will come at some stage.
#
# Created by: Martijn van Oosterhout <kleptog@svana.org> August 2007
# Licence: public domain

# Where the planet file is (may be compressed)
PLANET=$(HOME)/osm/planet-070823.osm.bz2
# The osmosis jar file
# Note: version must be at least from 21 August 2007 (0.8.0)
OSMOSIS=/usr/local/bin/osmosis.jar
# If this is set it will attempt to do the AND data conversion also
AND_DATA=$(HOME)/osm/AND/GRD-NSHP-NLD-L6-2006H1-V2.0_OSM/Data/2007H1/NLD/020/
# If you have this installed it will be used to cut the memory usage of 2AND
# (without this you cannot run it on a 1GB machine)
TCMALLOC=/usr/local/lib/libtcmalloc.so.0

all: data/nld-delete.osm data/nld-leftover.osm data/and-unprotected.osm

# Data directory must exist
data:
	mkdir -p data
	
# Download Ante's NLD boundary
data/nldgrens.osm: data
	wget -q --mirror http://people.vrijschrift.org/~ante/osm/nld-simplified6.osm -O $@
	
# Convert the boundary to a polygon file and handle exclusions
data/nldgrens.polygon: data/nldgrens.osm 
	./osm2polygon.pl data/nldgrens.osm > $@
data/nld-groot.polygon: nld-groot.osm 
	./osm2polygon.pl nld-groot.osm > $@

# Make a polygon file for the aras to be extracted	
data/extract.polygon: protected_areas
	@echo -e '1\n  0 0\n 90 0\n 90 90\n 0 90\n 0 0\nEND' > $@
	./protected2polygon.pl >> $@
	
# Extract NLD-groot from the planet file
data/nld-groot.osm: data/nld-groot.polygon $(PLANET)
	../../osm-extract/polygons/extract-polygon.pl -v -i $(PLANET) -o $@ -p data/nld-groot.polygon -r data/remains-groot.txt
# Extract NLD from the NLD-groot file
data/nld.osm: data/nldgrens.polygon data/nld-groot.osm ../../osm-extract/polygons/extract-polygon.pl
	../../osm-extract/polygons/extract-polygon.pl -v -i data/nld-groot.osm -o $@ -p data/nldgrens.polygon -r data/remains-grens.txt
# Remove protected areas from NLD file
data/nld-unprotected.osm: data/extract.polygon data/nld.osm ../../osm-extract/polygons/extract-polygon.pl
	../../osm-extract/polygons/extract-polygon.pl -v -i data/nld.osm -o $@ -p data/extract.polygon -r data/remains-protected.txt

# From the extracted data we determine what we actually want to delete
data/nld-delete.osm: data/nld-unprotected.osm planetosm-deleteby-tags.pl
	cat data/remains-groot.txt data/remains-grens.txt data/remains-protected.txt >data/remains.txt
	./planetosm-deleteby-tags.pl --output=osmchange --remains=data/remains.txt data/nld-unprotected.osm > $@

data/nld-leftover.osm: $(OSMOSIS) data/nld-groot.osm data/nld-delete.osm
	java -jar $(OSMOSIS) --read-xml file="data/nld-groot.osm" --read-xml-change file="data/nld-delete.osm" outPipe.0=change \
	                   --sort-change inPipe.0=change outPipe.0=change2 --apply-change inPipe.1=change2 \
	                   --write-xml file="data/nld-leftover.osm"
	
ifneq ($(AND_DATA),)
###################################
#### AND data conversion stuff ####
###################################
data/AND2osm.osm: ../and2osm/2AND $(AND_DATA)/020_a.dbf $(AND_DATA)/and_nodes.dbf
	make -C ../and2osm 2AND
	if [ -e $(TCMALLOC) ] ; then export LD_PRELOAD=$(TCMALLOC) ; fi ; \
		../and2osm/2AND -s -C $(AND_DATA)
	ln -sf $(AND_DATA)/AND2osm.osm data/AND2osm.osm
	
$(AND_DATA)/and_nodes.dbf: ../and2osm/mknodedb
	make -C ../and2osm mknodedb
	../and2osm/mknodedb -C $(AND_DATA)

data/and-unprotected.osm: data/AND2osm.osm data/extract.polygon 
	../../osm-extract/polygons/extract-polygon.pl -v -i data/AND2osm.osm -o $@ -p data/extract.polygon
	
$(AND_DATA)/020_a.dbf:
	@echo "======================================================================="
	@echo "ERROR: AND data not found: $(AND_DATA)"
	@echo "Please enter the location of the AND data at the top of the makefile, "
	@echo "or comment the variable out if you do not wish to do those steps. The "
	@echo "location you want is the location of dbf files."
	@exit 1
else
data/and-unprotected.osm:
	@echo "======================================================================="
	@echo "ERROR: AND data not found: $(AND_DATA)"
	@echo "Please enter the location of the AND data at the top of the makefile, "
	@exit 1
endif

load_nld-groot.osm: nld-groot.osm
	java -classpath /usr/share/java/mysql-connector-java.jar:/usr/local/bin/osmosis.jar com.bretth.osmosis.core.Osmosis \
		--read-xml file="data/nld-groot.osm" --write-mysql host="localhost" database="osm" user="kleptog"
	
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

clean:
	rm -f data/*
ifneq ($(AND_DATA),)
	make -C ../and2osm clean
endif
