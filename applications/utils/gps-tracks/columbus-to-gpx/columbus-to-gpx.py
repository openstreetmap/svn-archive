#!/usr/bin/python

import sys, os, re, stat, time

# INDEX,TAG,DATE,TIME,LATITUDE N/S,LONGITUDE E/W,HEIGHT,SPEED,HEADING,VOX
# 1@@@@@,T,090313,163804,39.951613N,075.160065W,149@@,0@@@,0@@,@@@@@@@@@
    
model = [
"""<?xml version="1.0" encoding="UTF-8"?>
<gpx
  version="1.0"
  creator="makegpx"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns="http://www.topografix.com/GPX/1/0"
  xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd">
<time>%s</time>
  <trk>
""","""    <trkseg>
""","""      <trkpt lat="%s" lon="%s"><time>%s</time></trkpt>
""","""      <trkpt lat="%s" lon="%s"><desc>%s</desc><time>%s</time></trkpt>
""","""    </trkseg>
""","""  </trk>
""","""  <wpt lat="%s" lon="%s"><desc>%s</desc><link>%s</link></wpt>
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
        outf.write(model[6] % wp)
    outf.write(model[7])
    
for fn in sys.argv[1:]:
    convertone(fn, fn+".gpx")

