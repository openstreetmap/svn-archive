#!/usr/bin/python
# -------------------------------------------------------------------
# Converts the bare "lat,lon" tracklogs stored by rana into GPX files
#
# Oliver White, 2008.  This file may be treated as public-domain
# -------------------------------------------------------------------
import sys
from time import strftime, gmtime

t = 0 # fake timestamps in file, to allow OSM upload without revealing dates or speeds

print """<?xml version="1.0"?>
<gpx
 version="1.0"
creator="Converted from tangogps log"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xmlns="http://www.topografix.com/GPX/1/0"
xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd">
<time>1970-01-01T00:00:00Z</time>
<trk>
  <name>ACTIVE LOG</name>
  <trkseg>"""

f = open(sys.argv[1], "r")
for line in f:
  (lat,lon) = [float(i) for i in line.split(",")]

  tstr = strftime("%Y-%m-%dT%H:%M:%SZ", gmtime(t))
  t += 1
  
  print "<trkpt lat=\"%f\" lon=\"%f\">\n<time>%s</time>\n</trkpt>" % (lat, lon, tstr)

print "</trkseg>\n</trk>\n</gpx>\n"
 
