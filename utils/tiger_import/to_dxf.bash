#!/bin/bash

# Manhattan (New York County):
URL=http://www2.census.gov/geo/tiger/tiger2004fe/NY/tgr36061.zip

# Chicago (Cook County):
#URL=http://www2.census.gov/geo/tiger/tiger2004fe/IL/tgr17031.zip

./to_dxf.rb $URL >>county.dxf </dev/null

