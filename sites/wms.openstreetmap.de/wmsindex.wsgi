#!/usr/bin/python
# -*- coding: UTF-8 -*-
#
# generate index page for wms.openstreetmap.de dynamically
#
# from information provided by templates and mapfile information
#
# this way for adding new layers the mapfile is the only place we need to
# make changes. No redundant information anymore :)
#
# this is not a very elegant script (intermixes content and code)
# but serves its purpose
#
# (c) 2010 Sven geggus <sven-osm@geggus.net>
#
# GNU GPL Version 3 or later
# http://www.gnu.org/copyleft/gpl.html

header_template='/osm/wms/templates/index_header.html'
footer_template='/osm/wms/templates/index_footer.html'
wmsurl="http://wms.openstreetmap.de/wms"
tmsurl="http://wms.openstreetmap.de/tms"
mapurl="http://wms.openstreetmap.de/slippymap"
# zoomlevel for potlatch URL
zoom=17

mapfile='/osm/wms/osmwms.map'

import re,cgi,mapscript,os

def application(env, start_response):
  status = '200 OK'
  map = mapscript.mapObj(mapfile)

  templ = open(header_template,'r')
  index_html = templ.read()
  templ.close()
  
  for i in range(0,map.numlayers):
   layer = map.getLayer(i)
   extent = layer.metadata.get('wms_extent')
   if (layer.name != 'copyright'):
    index_html += "<hr />\n"
    index_html += '<table border="0" width="100%">\n'
    index_html += '<tr><td width="30%%"><b>Name:</b></td><td>%s</td></tr>' % layer.name
    wiki = layer.metadata.get('wiki')
    if wiki is not None:
     index_html += '<tr><td width="30%%"><b>Wiki:</b></td><td><a href="%s">%s</a></td></tr>' % (wiki,wiki)
    index_html += '<tr><td width="30%%"><b>Source:</b></td><td>%s</td></tr>' % layer.metadata.get('copyright')
    index_html += '<tr><td width="30%%"><b>Content:</b></td><td>%s</td></tr>' % layer.metadata.get('wms_title')
    index_html += '<tr><td width="30%%"><b>Bounding Box:</b></td><td>%s</td></tr>' % extent
    index_html += '<tr><td width="30%%"><b>WMS URL (for JOSM/Merkaartor):</b></td><td><b>%s?layers=%s&</b></td></tr>' % (wmsurl,layer.name)
    index_html += '<tr><td width="30%%"><b>Interactive map:</b></td><td><a href="%s/%s">%s/%s</a></td></tr>' % (mapurl,layer.name,mapurl,layer.name)
    latlon=extent.split()
    lon1=float(latlon[0])
    lat1=float(latlon[1])
    lon2=float(latlon[2])
    lat2=float(latlon[3])
    lat=lat1+(lat2-lat1)/2
    lon=lon1+(lon2-lon1)/2
    index_html += '<tr><td width="30%%"><b>Online Editor URL:</b></td><td><a href="http://www.openstreetmap.org/edit?lat=%f&lon=%f&zoom=%s&tileurl=%s/%s/!/!/!.png">Potlatch %s</a></td></tr>' \
    % (lat,lon,zoom,tmsurl,layer.name,layer.name)
    index_html += '</table>\n'
  
  templ = open(footer_template,'r')
  index_html += templ.read()
  templ.close()

  response_headers = [('Content-type', 'text/html'),
                      ('Content-Length', str(len(index_html)))]
                       
  start_response(status, response_headers)
  return [index_html]


                                                 