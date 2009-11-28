#!/usr/bin/python
# -*- coding: UTF-8 -*-
#
# Mapscript WMS server with default request parameters for josm
# 
# (c) 2009 Sven Geggus <sven-osm@geggus.net>
#
# Released under the GNU GENERAL PUBLIC LICENSE
# http://www.gnu.org/copyleft/gpl.html
#
#
#

debug = 0
mapfile="/var/www/wms4osm/osmwms.map"

josmdefaults={'SERVICE': 'WMS', 'VERSION': '1.1.1', 'FORMAT':'image/png', 'REQUEST':'GetMap'}

import os,crypt,mapscript,sys,math,string

from mod_python import apache
from mod_python.util import parse_qsl

# make our WMS also usable as TMS
def TileToMeters(tx, ty, zoom):
  initialResolution = 20037508.342789244 * 2.0 / 256.0
  originShift = 20037508.342789244
  tileSize = 256.0
  zoom2 = (2.0**zoom)
  res = initialResolution / zoom2
  mx = (res*tileSize*(tx+1))-originShift
  my = (res*tileSize*(zoom2-ty))-originShift
  return mx, my

# this will give the BBox Parameter from x,y,z
def TileToBBox(x,y,z):
  x1,y1=TileToMeters(x-1,y+1,z)
  x2,y2=TileToMeters(x,y,z) 
  return x1,y1,x2,y2

# generate a valid XML ServiceException message text
# from a simple textual error message
def SException(req,msg,code=""):
  req.content_type = 'application/vnd.ogc.se_xml' 
  xml = '<ServiceExceptionReport '
  xml += 'xmlns=\"http://www.opengis.net/ogc\" '
  xml += 'xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance" '
  xml += 'xsi:schemaLocation=\"http://www.opengis.net/ogc '
  xml += 'http://schemas.opengis.net/wms/1.1.1/OGC-exception.xsd\">\n'
  if code == "":
    xml += '  <ServiceException>\n  ' + msg
  else:
    xml += '  <ServiceException code=\"' + code +'\">\n  ' + msg
  xml += '\n  </ServiceException>\n</ServiceExceptionReport>\n'
  return xml

# "main"
def index(req):

  global debug

  # read apaceh environment variables
  req.add_common_vars()
  req.content_type = "text/plain"

  url = "http://%s%s" % \
        (req.subprocess_env['SERVER_NAME'],req.subprocess_env['REQUEST_URI'])

  os.chdir(req.subprocess_env['DOCUMENT_ROOT'])
  
  querystr = req.subprocess_env['QUERY_STRING']
  uagent = ''
  uagent = req.subprocess_env['HTTP_USER_AGENT']
  
  qupper=querystr.upper()
  
  # MAP=something in request is always an error  
  if "MAP=" in qupper:
    return SException(req,'Invalid argument "MAP=..." in request')
    
  # if tile=something is given we are in TMS mode
  
    
  # parse QUERY_STRING
  query = parse_qsl(querystr)
  query = dict(query)
  
  msreq = mapscript.OWSRequest()

  query_layers=''
  tilemode=0
  # ad QUERY items into mapscript request object
  for key, value in query.items():
    key=key.upper()
    if (key == "DEBUG"):
      debug = 1
    if (key == "LAYERS"):
      query_layers=value
    if key != "TILE":
      msreq.setParameter(key,value)
    else:
      xyz=value.split(',')
      if (len(xyz) != 3):
        return SException(req,'Invalid argument "TILE=%s" in request, must be TILE=x,y,z' % value)
      try:
        x=int(xyz[0])
      except:
        return SException(req,'Invalid argument "TILE=%s" in request, must be TILE=x,y,z' % value)
      try:
        y=int(xyz[1])
      except:
        return SException(req,'Invalid argument "TILE=%s" in request, must be TILE=x,y,z' % value)
      try:
        z=int(xyz[2])
      except:
        return SException(req,'Invalid argument "TILE=%s" in request, must be TILE=x,y,z' % value)                                                                                                                                                      
      tilemode=1

  # we are in tilemode, lets add the required keys
  if (tilemode):
    msreq.setParameter("srs","EPSG:900913")
    msreq.setParameter("bbox","%f,%f,%f,%f" % TileToBBox(x,y,z))
    msreq.setParameter("width","256")
    msreq.setParameter("height","256")

  # add default QUERY items for use with JOSM
  # this will allow for easy to remember WMS Request-URLs
  if ("JOSM" in uagent) or (tilemode):
    for d in josmdefaults:
      if d not in qupper:
        msreq.setParameter(d,josmdefaults[d])

  map = mapscript.mapObj(mapfile)
  map.setMetaData("wms_onlineresource",url.split("?")[0])
  
  # write a mapfile for debugging purposes
  if (debug):
    os.umask(022)
    map.save("/tmp/topomap.map")

  # write mapserver results to buffer
  mapscript.msIO_installStdoutToBuffer()
  try:
    res = map.OWSDispatch(msreq)
  except:
     return SException(req,str(sys.exc_info()[1]))

  # adjust content-type
  req.content_type = mapscript.msIO_stripStdoutBufferContentType()

  # write image data to stdout
  return mapscript.msIO_getStdoutBufferBytes()

