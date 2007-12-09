#!/usr/bin/python
#----------------------------------------------------------------
#
#------------------------------------------------------
# Usage: 
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
import sys
import os
from xml.sax import make_parser, handler
import xml
from struct import *

class BinaryOsm(handler.ContentHandler):
  
  def __init__(self):
    pass
    
  def encode(self, filename, output):
    self.nextKID = 3
    self.nextVID = 1
    self.tags = {}
    self.values = {}
    
    if(not os.path.exists(filename)):
      print "No such data file %s" % filename
      return
    try:
      self.out = open(output, "wb")
      parser = make_parser()
      parser.setContentHandler(self)
      parser.parse(filename)
      self.out.write("X")
      self.out.close()
    except xml.sax._exceptions.SAXParseException:
      print "Error loading %s" % filename

  def startElement(self, name, attrs):
    """Handle XML elements"""
    if(name =='node'):
      self.meta = { \
        'id':int(attrs.get('id')),
        'lon':float(attrs.get('lat')),
        'lat':float(attrs.get('lon'))
        }
      self.tags = {}
    elif(name == 'way'):
      self.meta = {'id':int(attrs.get('id'))}
      self.tags = {}
      self.waynodes = []
    elif(name == 'relation'):
      self.tags = {}
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
    writeTags = False
    if(name =='node'):
      data = 'N' + pack("L", self.meta['id']) + self.encodeLL(self.meta['lat'], self.meta['lon'])
      self.out.write(data)
      writeTags = True
    
    elif(name == 'way'):
      data = 'W' + pack("L", self.meta['id'])
      self.out.write(data)
      writeTags = True
      
    if(writeTags):
      n = len(self.tags.keys())
      if(n > 255):
        # TODO:
        print "Error: more than 255 tags on an item"
        return
      self.out.write(pack('B', n))
      for k,v in self.tags.items():
        self.encodeTag(k)
        volatile = k in ('name','ref','ncn_ref','note','notes','description','ele','time')
        self.encodeTag(v,volatile)

  def encodeTag(self,text,volatile=False):
    if(not volatile):
      try:
        ID = self.values[text]
        print "Using %d to represent %s" % (ID,text)
        self.out.write(pack('H', ID))
      except KeyError:
        print "Storing %s as %d" % (text,self.nextKID)
        self.values[text] = self.nextKID
        self.out.write(pack('HHB', 1, self.nextKID, len(text)))
        self.out.write(text)
        self.nextKID = self.nextKID + 1
    else:
      self.out.write(pack('HB', 0, len(text)))
      self.out.write(text)
      #print "Storing simple %s" % (text)

  def encodeLL(self,lat,lon):
    pLat = (lat + 90.0) / 180.0 
    pLon = (lon + 180.0) / 360.0 
    iLat = self.encodeP(pLat)
    iLon = self.encodeP(pLon)
    return(pack("II", iLat, iLon))
    
  def encodeP(self,p):
    i = int(p * 4294967296.0)
    return(i)
    
# Parse the supplied OSM file
if __name__ == "__main__":
  print "Loading data..."
  Binary = BinaryOsm()
  Binary.encode(sys.argv[1], sys.argv[2])
