#!/usr/bin/python
# -*- coding: UTF-8 -*-
#
# zoom/x/y to wms redirector for geoimage.at 
#
# (c) 2011 Sven geggus <sven-osm@geggus.net>
#
# GNU GPL Version 3 or later
# http://www.gnu.org/copyleft/gpl.html

target_base='http://gis.lebensministerium.at/wmsgw/?key=%s&FORMAT=image/jpeg&VERSION=1.1.1&SERVICE=WMS&REQUEST=GetMap&Layers=Luftbild_MR,Luftbild_1m,Luftbild_8m,Satellitenbild_30m&SRS=EPSG:900913&WIDTH=256&HEIGHT=256&BBOX=%s'

import re

# calculate coordinates from tile names
def TileToMeters(tx, ty, zoom):
  initialResolution = 20037508.342789244 * 2.0 / 256.0
  originShift = 20037508.342789244
  tileSize = 256.0
  zoom2 = (2.0**zoom)
  res = initialResolution / zoom2
  mx = (res*tileSize*(tx+1))-originShift
  my = (res*tileSize*(zoom2-ty))-originShift
  return mx, my

# this will give the BBox Parameter for tile x,y,z
def TileToBBox(x,y,z):
  x1,y1=TileToMeters(x-1,y+1,z)
  x2,y2=TileToMeters(x,y,z) 
  return x1,y1,x2,y2

def application(environ, start_response):
	status = '200 OK'
	# our URI is in the form http://<our-server>/<key>/<z>/<x>/<y>.jpg
	# we are only interested in /<key>/<z>/<x>/<y>.jpg
	regex='/([0-9a-fxA-FX]+)/([0-9]+)/([0-9]+)/([0-9]+)\.jpg'
	uri = environ.get('REQUEST_URI', '')
	res=re.findall(regex,uri)
	
	if len(res) != 1:
	  out='ERROR, invalid Tile URL: %s\n' % uri
	  response_headers = [('Content-type', 'text/plain'),
                              ('Content-Length', str(len(out)))]
          start_response('200 OK', response_headers)
          return [out]
        else:
	  key=res[0][0]
	  z=res[0][1]
	  x=res[0][2]
	  y=res[0][3]
	  bbox="%f,%f,%f,%f" % TileToBBox(int(x),int(y),int(z))
	  target=target_base % (key,bbox)
	  start_response('301 Redirect', [('Location', target),])
	  return []
