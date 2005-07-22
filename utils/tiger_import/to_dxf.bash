#!/bin/bash

# Manhattan (New York County):
URL=http://www2.census.gov/geo/tiger/tiger2004fe/NY/tgr36061.zip

# Chicago (Cook County, huge):
#URL=http://www2.census.gov/geo/tiger/tiger2004fe/IL/tgr17031.zip

# Montana (Wibaux County, moderately small):
#URL=http://www2.census.gov/geo/tiger/tiger2004fe/MT/tgr30109.zip

# county in American Samoa (very small):
#URL=http://www2.census.gov/geo/tiger/tiger2004fe/AS/tgr60020.zip

./to_dxf.rb $URL >>county.dxf </dev/null

