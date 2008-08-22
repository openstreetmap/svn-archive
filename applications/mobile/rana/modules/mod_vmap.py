#!/usr/bin/python
#----------------------------------------------------------------------------
# Display map vectors
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
from base_module import ranaModule
import cairo
import os
import sys
import urllib
from tilenames import *
from vmap_load import vmapData

def getModule(m,d):
  return(vmap(m,d))

class vmap(ranaModule):
  """Display map vectors"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.tiles = {}

  def firstTime(self):
    self.loadEnums()
    self.setupStyles()

  def getTile(self,x,y,z):
    ourZ = 14
    if(z < ourZ):
      return(False)
    while(z > ourZ):
      x = int(x/2)
      y = int(y/2)
      z -= 1
    key = "%05d_%05d" % (x,y)
    if(not self.tiles.has_key(key)):
      self.tiles[key] = vmapData(self.getFilename(x,y))
      print "Loading %s" % (self.getFilename(x,y),)
    return(self.tiles[key])

  def getBaseDir(self):
    return(self.get("vmapTileDir", "../tiledata3/output"))
    
  def getFilename(self,x,y):
    if(1):
      xdir = int(x / 64)
      ydir = int(y / 64)
      filename = "%s/%d_%d/%d_%d.bin" % (self.getBaseDir(),xdir,ydir,x,y)
    else:
      filename = "%s/%d_%d.bin" % (self.getBaseDir(),x,y)
    return(filename)

  def setupStyles(self):
    self.highways = {
      'motorway':     ((0.5, 0.5, 1.0), 6, {'shields':True}),
      'trunk':        ((0.0, 0.8, 0.0), 6, {'shields':True}),
      'primary':      ((0.8, 0.0, 0.0), 6, {'shields':True}),
      'secondary':    ((0.8, 0.8, 0.0), 6, {'shields':True}),
      'tertiary':     ((0.8, 0.8, 0.0), 6, {}),
      'unclassified': ((0.5, 0.5, 0.5), 4, {}),
      'service':      ((0.5, 0.5, 0.5), 2, {}),
      'footway':      ((1.0, 0.5, 0.5), 2, {'dashed':True}),
      'cycleway':     ((0.5, 1.0, 0.5), 2, {'dashed':True}),
      }

  def loadEnums(self):
    filename = "%s/enums.txt" % self.getBaseDir()
    f = open(filename, "r")
    self.enums = {}
    for line in f:
      k,v = line.rstrip().split("\t")
      k = int(k)
      print "%d = '%s'" % (k,v)
      self.enums[k] = v
    
  def setStyle(self, style, cr):
    styleDef = self.highways.get(style, None)
    if(not styleDef):
      return(False)
    (colour,width,options) = styleDef
    (r,g,b) = colour
    cr.set_source_rgb(r,g,b)
    cr.set_line_width(width)
    return(True)
    
  def drawTile(self,cr,tx,ty,tz,proj):
    mapData = self.getTile(tx,ty,tz)
    if(mapData):
      #print mapData.ways
      for wayID, way in mapData.ways.items():
        if(not self.waysDrawn.get(wayID, False)): # if not drawn already as part of another tile
          if(self.setStyle(self.enums[way['style']], cr)):
            count = 0
            for node in way['n']:
              (lat,lon,nid) = node
              x,y = proj.ll2xy(lat,lon)
              if(count == 0):
                cr.move_to(x,y)
              else:
                cr.line_to(x,y)
              count += 1
            cr.stroke()
          # Note: way['N'] and way['r'] are name and ref respectively
          self.waysDrawn[wayID] = True
    else:
      print "No map data"

  def drawMap(self, cr):
    (sx,sy,sw,sh) = self.get('viewport')
    proj = self.m.get('projection', None)
    if(not proj or not proj.isValid()):
      return
    
    z = int(self.get('z', 15))

    # Render each 'tile' in view
    self.waysDrawn = {}
    for x in range(int(floor(proj.px1)), int(ceil(proj.px2))):
      for y in range(int(floor(proj.py1)), int(ceil(proj.py2))):
        self.drawTile(cr,x,y,z,proj)
   
  def update(self):
    pass

if(__name__ == "__main__"):
  a = vmap({},{"vmapTileDir":"../../tiledata3/output"})
  a.loadEnums()