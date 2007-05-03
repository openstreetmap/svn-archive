# Makefile.am for scripts/perl_lib

SUBDIRS =      pdf-atlas planet.osm
DIST_SUBDIRS = $(SUBDIRS)

bin_SCRIPTS= \
	utils/import/osmtrackfilter/osmtrackfilter.pl
	planet-mirror/planet-mirror.pl \
	utils/osm-extract/osm-subset.pl \
	simplify/simplify.pl \
	utils/export/osm2csv/osm2csv.pl \
	utils/planet.osm/C/UTF8sanitizer

UTF8sanitizerdir = $(bindir)
UTF8sanitizer_PROGRAMS = UTF8sanitizer
UTF8sanitizer_SOURCES = utils/planet.osm/C/UTF8sanitizer.c

EXTRA_DIST = $(bin_SCRIPTS) \
	$(PRGS) \
	utils/export/osm2csv/create_all.sh \
	utils/import/csv2osm/csv2osm.pl\
	utils/import/coast_import/coast_upload.pl
	utils/import/coast_utils/import/coast_upload.pl\
	utils/import/coast_utils/import/osm.pm\
	utils/import/srtm2osm/merge.pl\
	utils/import/srtm2osm/mkcntr.pl\
	utils/import/srtm2osm/extractdata.pl\
	utils/import/osmtrackfilter/osmtrackfilter.pl\
	utils/export/osm2singleline_per_tag/osm2singleline_per_tag.pl\
	utils/export/CityKML/run.pl\
	utils/export/CityKML/download_specific.pl\
	utils/export/CityKML/download.pl\
	utils/export/CityKML/package.pl\
	utils/export/osm2csv/osm2csv.pl\
	utils/export/osmgarminmap/feature-list-csv2xml.pl\
	utils/export/osm2kml/osm2kml.py\
	utils/osm-extract/osm-subset.pl\
	utils/filter/osm-length/osm-length-2.pl\
	utils/filter/simplify/simplify.pl\
	utils/planet.osm/perl/planetosm-to-db.pl\
	utils/planet.osm/perl/reorder-ways.pl\
	utils/planet.osm/perl/check_osm.pl\
	utils/planet.osm/perl/planetcount.pl\
	utils/planet.osm/python/utf8osmchecker.py
