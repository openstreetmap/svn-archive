#!/bin/sh

# This script configures the environment to use the OSM db
# so that the gpx-import program can find it.

setting () {
  S_N=GPX_$1
  shift
  eval "${S_N}='$*'"
  export ${S_N}
}

# General settings
setting SLEEP_TIME 1

# Paths (can be relative from invocation path if appropriate)
setting PATH_TRACES /home/osm/traces
setting PATH_IMAGES /home/osm/images
setting PATH_TEMPLATES templates/

# MySQL connection
setting MYSQL_HOST localhost
setting MYSQL_USER openstreetmap
setting MYSQL_DB openstreetmap
setting MYSQL_PASS openstreetmap

# Optional debug statements
#setting INTERPOLATE_STDOUT 1

# Run the commandline

exec "$@"

