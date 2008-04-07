#!/bin/sh
#

# Fink installed postgis location
[ -d /sw/share/doc/postgis82 ] && POSTGIS=/sw/share/doc/postgis82

# Linux installed postgis location
[ -d /usr/share/postgresql-8.1-postgis ] && POSTGIS=/usr/share/postgresql-8.1-postgis

#################
dropdb $1
createdb -E UTF-8 $1
createlang plpgsql $1
psql $1 <${POSTGIS}/lwpostgis.sql
psql $1 <${POSTGIS}/spatial_ref_sys.sql
psql $1 <osm_server.sql
psql $1 <spatial.sql
