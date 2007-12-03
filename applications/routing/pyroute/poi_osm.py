#!/usr/bin/python
#----------------------------------------------------------------
# OSM POI handler for pyroute
#
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
from xml.sax import make_parser, handler
from poi_base import *
import os
import urllib

class osmPoiModule(poiModule, handler.ContentHandler):
  def __init__(self, modules):
    poiModule.__init__(self, modules)
    self.draw = False
    self.loadPOIs("all", "amenity|shop=*")
    
  def loadPOIs(self,name,search):
    self.loadingGroup = poiGroup(name)
    self.groups.append(self.loadingGroup)
    
    print "Loading %s (%s)" % (name,search)
    filename = "data/poi_%s.osm" % name
    url = "http://www.informationfreeway.org/api/0.5/node[%s][%s]" % (search, self.bbox())
    
    if(not os.path.exists(filename)):
      urllib.urlretrieve(url, filename)
    self.load(filename)
        
  def bbox(self):
    # TODO: based on location!
    return "bbox=-6,48,2.5,61"
  
  def load(self, filename):
    if(not os.path.exists(filename)):
      print "Can't load %s"%filename
      return
    self.inNode = False
    parser = make_parser()
    parser.setContentHandler(self)
    parser.parse(filename)
  
  def startElement(self, name, attrs):
    if name == "node":
      self.currentNode = { \
        'lat': float(attrs.get('lat')),
        'lon': float(attrs.get('lon'))}
      self.inNode = True
    if name == "tag" and self.inNode:
      self.currentNode[attrs.get('k')] = attrs.get('v')
  
  def endElement(self, name):
    if(name == "node"):
      self.storeNode(self.currentNode)
      self.inNode = False

  def storeNode(self, n):
    x = poi(n['lat'], n['lon'])
    x.title = n.get('amenity','') + ': ' + n.get('name', '?')
    self.loadingGroup.items.append(x)

  def save(self):
    # Default filename if none was loaded
    if(self.filename == None):
      self.filename = "data/poi.osm"
    self.saveAs(self.filename)
  
  def saveAs(self,filename):
    if(filename == None):
      return
    pass
  

if __name__ == "__main__":
  nodes = osmPoiModule(None)
  nodes.report()
