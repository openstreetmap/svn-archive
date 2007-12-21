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

if [ -n "$1" ]; then
  locations="$*"
fi

for place in $locations; do
  bbox=$(eval echo \$${place}_bbox)
  if [ -z "$bbox" ]; then 
    echo "warning: no data for bbox at $place";
  fi
  echo  "Fetching: $place, $bbox"
  wget -q -O ${place}.osm "${url}${bbox}"
  rm -f map.kmz data.osm
  perl conv05/05to04.pl < ${place}.osm > data.osm
  make 
  mv map.kmz ${place}.kmz
  echo "created ${place}.kmz"
done

