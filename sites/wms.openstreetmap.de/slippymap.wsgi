#!/usr/bin/python
# -*- coding: UTF-8 -*-
#
# Show a Slippymap of the Layer requested by the URL
#
# (c) 2010 Sven geggus <sven-osm@geggus.net>
#
# GNU GPL Version 3 or later
# http://www.gnu.org/copyleft/gpl.html

template='/osm/wms/templates/slippymap.html'
mapfile='/osm/wms/osmwms.map'

import re,cgi,mapscript,os

def genHTML(template,layername,bounds,zoom):
  templ = open(template,'r')
  tdata = templ.read()
  templ.close()

  p = re.compile('%LAYERNAME%')
  tdata = p.sub(layername,tdata)
  
  p = re.compile('%BOUNDS%')
  tdata = p.sub(bounds,tdata)
  
  p = re.compile('numZoomLevels:[0-9]+')
  zoom = 'numZoomLevels:' + zoom;
  tdata = p.sub(zoom,tdata)
  return tdata
  
def application(environ, start_response):
  status = '200 OK'
  map = mapscript.mapObj(mapfile)
  layername=os.path.basename(environ['PATH_INFO'])
  layer=map.getLayerByName(layername)
  
  if layer is None:
   output = ('<html><body>&quot;<b>%s</b>&quot; is not a valid layername!</html></body>' % layername)
  else:
   bounds=layer.metadata.get('wms_extent').replace(' ',',')
   try:
     zoom=layer.metadata.get('zoomlevels')
   except:
     zoom=19
   output=genHTML(template,layername,bounds,zoom)       

  response_headers = [('Content-type', 'text/html'),
                      ('Content-Length', str(len(output)))]
                       
  start_response(status, response_headers)
  return [output]
                                           