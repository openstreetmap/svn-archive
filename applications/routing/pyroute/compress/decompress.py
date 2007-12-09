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
from struct import *

class BinaryOsm:
  def __init__(self):
    self.values = {}
    pass
    
  def decode(self, filename, output):
    if(not os.path.exists(filename)):
      print "No such data file %s" % filename
      return
    Output = open(output, "w")
    Input = open(filename, "rb")
    self.readBinary(Input, Output)
    Input.close()
    Output.close()

  def readBinary(self, Input, Output):
    while(True):
      type = Input.read(1)
      if(type == 'N'):
        self.readNode(Input, Output)
      elif(type == 'W'):
        self.readWay(Input, Output)
      elif(type == 'X'):
        print "Reached end of data!!!"
        return
      else:
        print "Unknown type"

  def readNode(self, Input, Output):
    nid = unpack("L", Input.read(4))[0]
    lat,lon = self.decodeLL(Input.read(8))
    #print "Node %u at (%f,%f)"%(nid,lat,lon)
    self.readTags(Input, Output)
    
  def readWay(self, Input, Output):
    id = Input.read(8)
    self.readTags(Input, Output)

  def readTags(self, Input, Output):
    n = unpack('B', Input.read(1))[0]
    if(n == 0):
      return
    print " - %d tags" % n
    for i in range(n):
      k = self.readKV(Input,Output)
      v = self.readKV(Input,Output)
      print "%s = %s" % (k,v)

  def readKV(self, Input, Output):
    ID = unpack('H', Input.read(2))[0]
    if(ID == 0):
      # one-off
      lenX = unpack('B', Input.read(1))[0]
      return(Input.read(lenX))
    
    if(ID == 1):
      # New + store
      NewID = unpack('H', Input.read(2))[0]
      lenX = unpack('B', Input.read(1))[0]
      X = Input.read(lenX)
      self.values[NewID] = X
      return(X)
      
    else:
      # ID that we should already know
      return(self.values[ID])
    
    
  def decodeLL(self,data):
    iLat,iLon = unpack("II", data)
    pLat = self.decodeP(iLat)
    pLon = self.decodeP(iLon)
    lat = pLat * 180.0 - 90.0
    lon = pLon * 360.0 - 180.0
    return(lat,lon)
    
  def decodeP(self,i):
    p = float(i) / 4294967296.0
    return(p)
    
# Parse the supplied OSM file
if __name__ == "__main__":
  print "Loading data..."
  Binary = BinaryOsm()
  Binary.decode(sys.argv[1], sys.argv[2])
