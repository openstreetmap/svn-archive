#!/usr/bin/python
#-----------------------------------------------------------------------------# Merge multiple OSM files (in memory) and save to another OSM file
#-----------------------------------------------------------------------------# Copyright 2008, Oliver White
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
#----------------------------------------------------------------------------
from parseOsm import *

def OsmMerge(dest, sources):
  """Merge multiple OSM files together
  
  Usage: 
    OsmMerge('output_filename.osm', 
      ['input_file_1.osm', 
       'input_file_2.osm',
       'input_file_3.osm'])
  """
  
  ways = []
  poi = []
  node_tags = {}
  nodes = {}
  
  # Trawl through the source files, putting everything into memory
  for source in sources:
    osm = parseOsm(source)
    
    ways = ways + osm.ways
    for p in osm.poi:
      node_tags[p['id']] = p['t']

    for id,n in osm.nodes.items():
      nodes[id] = n

  # Store the result as an OSM XML file
  f=open(dest, 'w')
  f.write("<?xml version='1.0' encoding='UTF-8'?>\n")
  f.write("<osm version='0.5' generator='OsmMerge'>\n")
  
  for n,data in nodes.items():
    (lat,lon) = nodes[n]
    f.write("<node id='%d' lat='%f' lon='%f'>" % (n,lat,lon))
    tags = node_tags.get(n, None)
    if(tags):
      for k,v in tags.items():
        f.write("\n<tag k='%s' v='%s'/>" % (k,v))
    f.write("</node>\n")
    
  for w in ways:
    f.write("<way id='%d'>")
    for k,v in w['t'].items():
      f.write("\n<tag k='%s' v='%s'/>" % (k,v))
    for n in w['n']:
      f.write("\n<nd ref='%d'/>" % n)
    f.write("</way>\n")
  
  f.write("</osm>\n")
  f.close()
  
