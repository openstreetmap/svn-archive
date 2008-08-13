#!/usr/bin/python
# -*- coding: UTF-8 -*-
import sys
import os
import tilenames
import struct
import enums

class converter:
  def __init__(self):
    self.ways = []
    self.layers = {}
    self.enums = enums.makeEnums()
      
  def storeWay2(self, way, style):
    layer = int(way['t'].get('layer', 0))
    if(not self.layers.has_key(layer)):
      self.layers[layer] = []

    self.layers[layer].append(way)
  
  def storeWay(self, way):
    for style in self.enums.equiv:
      if(style['fn'](way['t'])):
        style2 = self.enums.enums[style['equiv']]
        self.storeWay2(way, style2)
    for name, style in self.enums.enums.items():
      if(style['fn'](way['t'])):
        self.storeWay2(way, style)
    
  def loadPacked(self, filename):
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
      wayID = struct.unpack("I", f.read(4))[0]
      numNodes = struct.unpack("I", f.read(4))[0]
      way = {'n':[],'t':{},'id':wayID}
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
      self.storeWay(way)

if(__name__ == "__main__"):
  a = converter()
  a.loadPacked("simple/255_170/2046_1361.dat")
  print a.layers.keys()