# Makefile.am for scripts
SUBDIRS =      \
	export \
	import \
	little-osm \
	osm-data \
	osm-extract \
	perl_lib \
	planet.osm \
	xsltrans \
	maplint \
	filter

DIST_SUBDIRS = $(SUBDIRS)

bin_SCRIPTS= \
	export/osm2csv/osm2csv.pl \
	filter/simplify/simplify.pl \
	import/osmtrackfilter/osmtrackfilter.pl \
	osm-extract/planetosm-excerpt-area.pl \
	osm-extract/planetosm-excerpt-tags.pl \
	osm-extract/polygons/extract-germany.pl \
	osm-extract/polygons/extract-polygon.pl \
	planet-mirror/planet-mirror.pl \
	export/osm2csv/osm2csv.pl

EXTRA_DIST = $(bin_SCRIPTS) \
	export/CityKML/download.pl \
	export/CityKML/download_specific.pl \
	export/CityKML/package.pl \
	export/CityKML/run.pl \
	export/osm2csv/osm2csv.pl \
	export/osm2kml/osm2kml.py \
	export/osm2shp/osm2shp.cpp \
	export/osm2singleline_per_tag/osm2singleline_per_tag.pl \
	export/osmgarminmap/curlstuff.cpp \
	export/osmgarminmap/curlstuff.h \
	export/osmgarminmap/feature-list-csv2xml.pl \
	export/osmgarminmap/feature-list.csv \
	export/osmgarminmap/osm2garmin.cpp \
	filter/osm-length/osm-length-2.pl \
	filter/simplify/simplify.pl \
	filter/wayclean/wayclean.cpp \
	gps-tracks/gpx-batch-upload/gpx-batch-upload.pl \
	import/airport_import/import_apt.pl \
	import/coast_import/coast_upload.pl \
	import/coast_import/osm.pm \
	import/csv2osm/config.sample.complex \
	import/csv2osm/csv2osm.pl \
	import/dafif2osm/dafif2osm.pl \
	import/osmtrackfilter/osmtrackfilter.pl \
	import/srtm2osm/extractdata.pl \
	import/srtm2osm/merge.pl \
	import/srtm2osm/mkcntr.pl \
	maplint/README \
	maplint/Makefile \
	maplint/html/*.html \
	maplint/html/style.css \
	maplint/lib/*.xsl \
	maplint/tests/base/*.xml \
	maplint/tests/main/*.xml \
	maplint/tests/strict/*.xml \
	osm-extract/osm-subset.pl \
	osm-extract/planetosm-excerpt-area.pl \
	osm-extract/planetosm-excerpt-tags.pl \
	osm-extract/polygons/extract-germany.pl \
	osm-extract/polygons/extract-polygon.pl \
	osmapitest/*.rb \
	planet-mirror/planet-mirror.pl \
	planet.osm/java/speller/words.cfg \
	planet.osm/perl/check_osm.pl \
	planet.osm/perl/planetcount.pl \
	planet.osm/perl/planetosm-to-db.pl \
	planet.osm/perl/reorder-ways.pl \
	planet.osm/python/utf8osmchecker.py \
	planetdiff/*.txt \
	planetdiff/*.xml \
	planetdiff/*.h \
	planetdiff/*.c \
	sanitize/sanitize.pl \
	serverStatus/ServerStatusToWiki.pl \
	srtmshp/Canvas.cpp \
	srtmshp/Canvas.h \
	srtmshp/EarthPoint.h \
	srtmshp/Map.h \
	srtmshp/*.cpp \
	srtmshp/SRTMConGen.h \
	srtmshp/SRTMDataGrid.h \
	srtmshp/SRTMGeneral.h \
	where_are_they/geo_helper.py \
	where_are_they/mini_osm.py \
	where_are_they/osm_io_helper.py \
	where_are_they/test_mini_osm.py \
	where_are_they/where_am_i.py \
	where_are_they/where_is_it.py \
	osm-activity/osm-activity.pl \
	osm-activity/README.txt \
	maplint/bin/maplint \
	maplint/*.xml \
	maplint/*.xsl \
	georss/*.js \
	georss/README \
	georss/*.html
