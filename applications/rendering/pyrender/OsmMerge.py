#!/usr/bin/python
#----------------------------------------------------------------------------
# Merge multiple OSM files (in memory) and save to another OSM file
#----------------------------------------------------------------------------
# Copyright 2008, Oliver White
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#---------------------------------------------------------------------------
import codecs
from parseOsm import *
from xml.sax.saxutils import quoteattr

def OsmMerge(dest, sources):
  """Merge multiple OSM files together
  
  Usage: 
    OsmMerge('output_filename.osm', 
      ['input_file_1.osm', 
       'input_file_2.osm',
       'input_file_3.osm'])
  """
  
  ways = {}
  poi = []
  node_tags = {}
  nodes = {}
  
  divisor = float(2 ** 31)
  
  # Trawl through the source files, putting everything into memory
  for source in sources:
    osm = parseOsm(source)
    
    for p in osm.poi:
      node_tags[p['id']] = p['t']

    for id,n in osm.nodes.items():
      nodes[id] = n
    for id,w in osm.ways.items():
      ways[id] = w

  # Store the result as an OSM XML file
  f=codecs.open(dest, mode='w', encoding='utf-8')
  f.write('<?xml version="1.0" encoding="UTF-8"?>\n')
  f.write('<osm version="0.5" generator="OsmMerge">\n')
  
  if(0):
	  for n,data in nodes.items():
	    (lat,lon) = nodes[n]
	    f.write('<node id="%d" lat="%f" lon="%f">' % (n,lat,lon))
	    tags = node_tags.get(n, None)
	    if(tags):
	      for k,v in tags.items():
	        f.write('\n<tag k=%s v=%s/>' % (quoteattr(k),quoteattr(v)))
	    f.write("</node>\n")
    
  for id,way in ways.items():
    f.write('<way id="%d">' % id)
    for k,v in way['t'].items():
      f.write('\n<tag k=%s v=%s/>' % (quoteattr(k),quoteattr(v)))

    (count,x1,y1) = (0,0,0)
    for n in way['n']:
      lat = n['lon']
      lon = n['lat']
      if(0 and count > 0):
        dx = lon - x1
        dy = lat - y1
        dd = dx + dx + dy + dy
        print "Dist2 = %f" % dd
      (x1,y1,count) = (lon,lat,count+1)
      f.write("\n<nd id='%d' x='%1.0f' y='%1.0f'/>" % (n['id'], n['lon'] * divisor, n['lat'] * divisor))
    f.write("</way>\n")
  
  f.write("</osm>\n")
  f.close()
  
