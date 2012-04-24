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

import re,cgi,mapscript,os,pyproj

# this will generate an extent in WGS84 projection
# regardless of the actual layer projection
def gen4326Extent(ms_extent,ms_srs):
  ms_srs=ms_srs.lower()
  if ms_srs == "espg:4326":
    return ms_extent
  else:
    x0,y0,x1,y1=eval('['+ms_extent.replace(' ',',')+']')
    proj4326 = pyproj.Proj(init='epsg:4326')
    projlayer = pyproj.Proj({'init':ms_srs})
    lon0,lat0 = pyproj.transform(projlayer,proj4326,x0,y0)
    lon1,lat1 = pyproj.transform(projlayer,proj4326,x1,y1)
    return "%f %f %f %f" % (lon0,lat0,lon1,lat1)

def genHTML(template,layername,bounds,zoom):
  templ = open(template,'r')
  tdata = templ.read()
  templ.close()

  p = re.compile('%LAYERNAME%')
  tdata = p.sub(layername,tdata)
  
  p = re.compile('%BOUNDS%')
  tdata = p.sub(bounds,tdata)
  
  p = re.compile('numZoomLevels:[0-9]+')
  zoom = 'numZoomLevels:' + zoom
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
   bounds=gen4326Extent(layer.metadata.get('wms_extent'),layer.metadata.get('wms_srs')).replace(' ',',')
  
   zoom = layer.metadata.get('zoomlevels')
   if zoom is None:
    zoom = '19'
    
   output=genHTML(template,layername,bounds,zoom)       

  response_headers = [('Content-type', 'text/html'),
                      ('Content-Length', str(len(output)))]
                       
  start_response(status, response_headers)
  return [output]
                                           