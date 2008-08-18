#!/usr/bin/python
#----------------------------------------------------------------------------
# Library for handling "simple-packed" OpenStreetMap vector data
#
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
import sys
import os
import struct
import tilenames

class vmapData:
  def __init__(self, filename=None):
    """Load an OSM XML file into memory"""
    self.ways = {}
    if(filename != None):
      self.load(filename)

  def load(self, filename):
    """Load an OSM XML file into memory"""
    if(not os.path.exists(filename)):
      print "File doesn't exist: '%s'" % filename
      return([])
    f = file(filename, "rb")

    size = os.path.getsize(filename)
    while(f.tell() < size):
      way = {'n':[]};
      
      wayID = struct.unpack("I", f.read(4))[0]
            
      numNodes = struct.unpack("I", f.read(4))[0]
      for n in range(numNodes):
        (x,y,nid) = struct.unpack("III", f.read(3*4))
        (lat,lon) = tilenames.xy2latlon(x,y,31)
        way['n'].append((lat,lon,nid))
      way['style'] = struct.unpack("I", f.read(4))[0]
      way['layer'] = struct.unpack("b", f.read(1))[0]

      tagSize = struct.unpack("H", f.read(2))[0]

      c = 0
      while(c < tagSize):
        k = f.read(1)
        s = struct.unpack("H", f.read(2))[0]
        v = f.read(s)
        way[k] = v
        #print " - %s = %s" % (k, v)
        c += 3 + s
        
        
      #name = self.lookForField(f, 'N')
      #ref = self.lookForField(f, 'r')

      #print "%d: Way %d: style %d, %d nodes." % (f.tell(), wayID, way['style'], numNodes)
      self.ways[wayID] = way

  def lookForField(self, file, tag):
    char = file.read(1)
    if(char != tag):
      file.seek(-1, os.SEEK_CUR)
      return(None)
    len = struct.unpack("H", file.read(2))[0]
    string = file.read(len)
    return(string)
    
    
if(__name__ == "__main__"):

  a = vmapData()
  filename = "../../tiledata3/output//127_85/8185_5447.bin"
  #print "Loading %d bytes" % os.path.getsize(filename)
  a.load(filename)
  print str(a.ways)