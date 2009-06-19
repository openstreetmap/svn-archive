#!/usr/bin/python

import sys, os, re, stat, time

# INDEX,TAG,DATE,TIME,LATITUDE N/S,LONGITUDE E/W,HEIGHT,SPEED,HEADING,VOX
# 1@@@@@,T,090313,163804,39.951613N,075.160065W,149@@,0@@@,0@@,@@@@@@@@@
    
model = [
"""<?xml version="1.0" encoding="UTF-8"?>
<gpx
  version="1.1"
  creator="makegpx"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns="http://www.topografix.com/GPX/1/1"
  xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<metadata>
    <time>%s</time>
</metadata>
  <trk>
""","""    <trkseg>
""","""      <trkpt lat="%s" lon="%s"><time>%s</time></trkpt>
""","""      <trkpt lat="%s" lon="%s"><desc>%s</desc><time>%s</time></trkpt>
""","""    </trkseg>
""","""  </trk>
""","""  <wpt lat="%s" lon="%s">
    <desc>%s</desc><link href="%s"><text>%s</text></link>
  </wpt>
""","""</gpx>
"""]
    
def convertone(fn, outfn):
    
    waypoints = []
    inf = open(fn)
    outf = open(outfn, "w")
    mtime = os.stat(fn)[stat.ST_MTIME]
    filetime = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.localtime(mtime))
    outf.write(model[0] % filetime)
    outf.write(model[1])
    for line in inf:
        fields = line.split(",")
        if fields[0] == "INDEX":
            continue
        lat = fields[4]
        lon = fields[5]
        trkptdate = "20%s-%s-%sT%s:%s:%sZ" % (fields[2][0:2],fields[2][2:4],fields[2][4:6],fields[3][0:2],fields[3][2:4],fields[3][4:6])
        if lat[-1] == "N":
            lat = lat[:-1]
        else:
            lat = "-" + lat[:-1]
        if lon[-1] == "E":
            lon = lon[:-1]
        else:
            lon = "-" + lon[:-1]
        outf.write(model[2] % (lat, lon, trkptdate))
        if not locals().has_key('oldlat'):
            oldlat = lat
            oldlon = lon
        if fields[1] == 'V':
            audiofn = fields[9].replace("\000", "").rstrip()
            waypoints.append((oldlat, oldlon, trkptdate, audiofn+".wav"))
        elif fields[1] == 'C':
            wp = fields[0].replace("\000", "").rstrip()
            waypoints.append((lat, lon, trkptdate, wp))
        oldlat = lat
        oldlon = lon
    outf.write(model[4])
    outf.write(model[5])
    for wp in waypoints:
        args = list(wp[:])
        args.append(wp[-1])
        outf.write(model[6] % tuple(args))
    outf.write(model[7])
    
for fn in sys.argv[1:]:
    print fn
    convertone(fn, fn+".gpx")

