#!/bin/bash

# Manhattan (New York County):
#     http://www2.census.gov/geo/tiger/tiger2004fe/NY/tgr36061.zip
# Chicago (Cook County):
#     http://www2.census.gov/geo/tiger/tiger2004fe/IL/tgr17031.zip

./tiger_to_dxf.rb http://www2.census.gov/geo/tiger/tiger2004fe/NY/tgr36061.zip >>county.dxf </dev/null

