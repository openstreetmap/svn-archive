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

import os,crypt,mapscript,sys

from mod_python import apache
from mod_python.util import parse_qsl

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
    
  # parse QUERY_STRING
  query = parse_qsl(querystr)
  query = dict(query)
  
  msreq = mapscript.OWSRequest()

  query_layers=''
  # ad QUERY items into mapscript request object
  for key, value in query.items():
    key=key.upper()
    if (key == "DEBUG"):
      debug = 1
    if (key == "LAYERS"):
      query_layers=value
    msreq.setParameter(key,value)

  # add default QUERY items for use with JOSM
  # this will allow for easy to remember WMS Request-URLs
  if "JOSM" in uagent:
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

