#!/usr/bin/python
#----------------------------------------------------------------------------
# Library for handling OpenStreetMap data
#----------------------------------------------------------------------------
# Copyright 2008-2009, Oliver White
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
import sys
import os
from xml.sax import make_parser, handler
import xml
import pickle
import gzip

class osm_file(handler.ContentHandler):
  def __init__(self):
    """Load an OSM XML file into memory"""
    self.nodes = {}
    self.data = {
      'ways': {},
      'poi': []
      }
    self.tags_seen = {}
  
  def load(self, filename):
    if(filename[-4:] == ".osm"):
      self.load_osm(filename)
    elif(filename[-3:] == ".gz"):
      self.load_dat(filename)
    else:
      print "Unrecognised filetype: %s" % filename

  def load_dat(self, filename):
    f = gzip.open(filename, "r")
    self.data = pickle.load(f)
    f.close()
      
  def save(self, filename):
    f = gzip.open(filename, "w")
    pickle.dump(self.data, f)
    f.close()
    
  def load_osm(self, filename):
    """Load an OSM XML file into memory"""
    if(not os.path.exists(filename)):
      print "No such file %s" % filename
      return
    try:
      parser = make_parser()
      parser.setContentHandler(self)
      parser.parse(filename)
    except xml.sax._exceptions.SAXParseException:
      print "Error loading %s" % filename
    self.nodes = {}

  def startElement(self, name, attrs):
    """Handle XML elements"""
    if name in('node','way','relation'):
      self.tags = {}
      self.isInteresting = False
      self.waynodes = []
      if name == 'node':
        """Nodes need to be stored"""
        id = int(attrs.get('id'))
        self.nodeID = id
        lat = float(attrs.get('lat'))
        lon = float(attrs.get('lon'))
        self.nodes[id] = (lat,lon)
      elif name == 'way':
        id = int(attrs.get('id'))
        self.wayID = id
    elif name == 'nd':
      """Nodes within a way -- add them to a list"""
      nid = int(attrs.get('ref'))
      (lat,lon) = self.nodes[nid]
      self.waynodes.append((nid, lat, lon))

    elif name == 'tag':
      """Tags - store them in a hash"""
      k,v = (attrs.get('k'), attrs.get('v'))
      
      # Discard unwanted tags
      if(k in ('created_by', 'source')
        or k[0:6] == "tiger:"
        or k[0:7] == "source:"):
        pass
      else:
        self.tags[k] = v
        self.isInteresting = True
        self.tags_seen[k] = 1
  
  def endElement(self, name):
    if name == 'way':
      self.data['ways'][self.wayID] = ({'t':self.tags, 'n':self.waynodes})
    elif name == 'node':
      if(self.isInteresting):
        self.data['poi'].append({'t':self.tags, 'id':self.nodeID})

