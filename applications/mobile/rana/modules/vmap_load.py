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
      return([])
    f = file(filename, "rb")

    fileID = struct.unpack("I", f.read(4))[0]
    if(fileID != 1280):
      print "File version not recognised"
      return
    numWays = struct.unpack("I", f.read(4))[0]
    for w in range(numWays):
      way = {'n':[],'t':{}}
      wayID = struct.unpack("I", f.read(4))[0]
      numNodes = struct.unpack("I", f.read(4))[0]
      for n in range(numNodes):
        (x,y,nid) = struct.unpack("III", f.read(3*4))
        (lat,lon) = tilenames.xy2latlon(x,y,31)
        way['n'].append((lat,lon,nid))
      numTags = struct.unpack("I", f.read(4))[0]
      for t in range(numTags):
        (lenK,lenV) = struct.unpack("HH", f.read(2*2))
        k = f.read(lenK)
        v = f.read(lenV)
        way['t'][k] = v
      self.ways[wayID] = way

if(__name__ == "__main__"):
  a = vmapData()
  a.load("../../TileData2/simple/128_84/16384_10877.dat")
  print str(a.ways)