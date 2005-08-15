#!/bin/bash

# the New York county TIGER file is at:
# http://www2.census.gov/geo/tiger/tiger2004fe/NY/tgr36061.zip

./to_osm.rb ny_county/TGR36061.RT1 ny_county/TGR36061.RT2 $1 >>to_osm.log 2>&1 </dev/null &

