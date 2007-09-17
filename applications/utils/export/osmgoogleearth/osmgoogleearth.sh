#!/bin/bash

if [[ -r ./osmgoogleearth.conf ]]; then
  echo "reading config"
else
  echo "config \"osmgoogleearth.conf\" not found"
  exit 1;
fi

. ./osmgoogleearth.conf
 
## check necessary config parameters

if [[ -z "$make" ]]; then
  make="make"
fi

## print some debug info:

echo "configured fetch url: $url"

for place in $locations; do
  bbox=$(eval echo \$${place}_bbox)
  echo  "Fetching: $place, $bbox"
  rm -f map.kmz data.osm
  wget -q -O data.osm "${url}${bbox}"
  make 
  mv map.kmz ${place}.kmz
  echo "created ${place}.kmz"
done

