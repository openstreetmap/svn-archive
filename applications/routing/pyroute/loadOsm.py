#!/usr/bin/python
#----------------------------------------------------------------
# load OSM data file into memory
#
#------------------------------------------------------
# Usage: 
#   data = LoadOsm(filename)
# or:
#   loadOsm.py filename.osm
#------------------------------------------------------
# Copyright 2007, Oliver White
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
#------------------------------------------------------
# Changelog:
#  2007-11-04  OJW  Modified from pyroute.py
#------------------------------------------------------
import sys
from xml.sax import make_parser, handler

class LoadOsm(handler.ContentHandler):
  """Parse an OSM file looking for routing information, and do routing with it"""
  def __init__(self, filename, storeMap = 0):
    """Initialise an OSM-file parser"""
    self.routing = {}
    self.nodes = {}
    self.storeMap = storeMap
    parser = make_parser()
    parser.setContentHandler(self)
    parser.parse(filename)
  def report(self):
    """Display some info about the loaded data"""
    return "Loaded %d nodes and %d routable sections" % ( \
      len(self.nodes.keys()), 
      len(self.routing.keys()))
  def startElement(self, name, attrs):
    """Handle XML elements"""
    if name in('node','way','relation'):
      self.tags = {}
      self.waynodes = []
      if name == 'node':
        """Nodes need to be stored"""
        id = int(attrs.get('id'))
        lat = float(attrs.get('lat'))
        lon = float(attrs.get('lon'))
        self.nodes[id] = (lat,lon)
    elif name == 'nd':
      """Nodes within a way -- add them to a list"""
      self.waynodes.append(int(attrs.get('ref')))
    elif name == 'tag':
      """Tags - store them in a hash"""
      k,v = (attrs.get('k'), attrs.get('v'))
      if not k in ('created_by'):
        self.tags[k] = v
  def endElement(self, name):
    """Handle ways in the OSM data"""
    if name == 'way':
      last = -1
      highway = self.tags.get('highway', '')
      railway = self.tags.get('railway', '')
      oneway = self.tags.get('oneway', '')
      reversible = not oneway in('yes','true','1')
      cyclable = highway in ('primary','secondary','tertiary','unclassified','minor','cycleway','residential', 'service')
      if cyclable:
        for i in self.waynodes:
          if last != -1:
            newLink = {'to':i}
            
            #print "%d -> %d & v.v." % (last, i)
            self.addLink(last, newLink)
            if reversible:
              self.addLink(i, {'to':last})
          last = i
  
  def addLink(self,fr,newLink):
    """Add a routeable edge to the scenario"""
    try:
      if newLink in self.routing[fr]:
        return
      self.routing[fr].append(newLink)
    except KeyError:
      self.routing[fr] = [newLink]
      
# Parse the supplied OSM file
if __name__ == "__main__":
  print "Loading data..."
  data = LoadOsm(sys.argv[1])
  print data.report()
