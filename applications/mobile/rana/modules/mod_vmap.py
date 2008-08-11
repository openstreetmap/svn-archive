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
    self.setupStyles()
  
  def getTile(self,x,y,z):
    if(z < 15):
      return(False)
    while(z > 15):
      x = int(x/2)
      y = int(y/2)
      z -= 1
    key = "%05d_%05d" % (x,y)
    if(not self.tiles.has_key(key)):
      self.tiles[key] = vmapData(self.getFilename(x,y))
    return(self.tiles[key])
  
  def getFilename(self,x,y):
    xdir = int(x / 128)
    ydir = int(y / 128)
    base = self.get("vmapTileDir", "../TileData2/simple")
    filename = "%s/%d_%d/%d_%d.dat" % (base,xdir,ydir,x,y)
    return(filename)
    
  def setupStyles(self):
    self.highways = {
      'motorway':     ((0.5, 0.5, 1.0), 6, {'shields':True}),
      'trunk':        ((0.0, 1.0, 0.0), 6, {'shields':True}),
      'primary':      ((0.8, 0.0, 0.0), 6, {'shields':True}),
      'secondary':    ((0.8, 0.8, 0.0), 6, {'shields':True}),
      'tertiary':     ((0.8, 0.8, 0.0), 6, {}),
      'unclassified': ((0.5, 0.5, 0.5), 4, {}),
      'service':      ((0.5, 0.5, 0.5), 2, {}),
      
      'footway':      ((1.0, 0.5, 0.5), 2, {'dashed':True}),
      'cycleway':     ((0.5, 1.0, 0.5), 2, {'dashed':True}),
      }
    
    equivalences = {
      'residential':'unclassified',
      'bridleway':'cycleway',
      'byway':'cycleway',
      'footpath':'footway',
      }
    for n1,n2 in equivalences.items():
      self.highways[n1] = self.highways[n2]
      
    for x in('motorway', 'trunk', 'primary'):
      self.highways[x+"_link"] = self.highways[x]
    
  def style(self, tags):
    highway = self.highways.get(tags.get("highway", None), None)
    if(highway):
      layer = int(tags.get('layer',0))
      highway[2]['layer'] = layer
      casing = ((0,0,0), highway[1]+4, {'layer':layer-0.1})
      styleList = [casing, highway]
      return(styleList)

  def setStyle(self, style, cr):
    (colour,width,options) = style
    (r,g,b) = colour
    cr.set_source_rgb(r,g,b)
    cr.set_line_width(width)
    
  def drawTile(self,cr,tx,ty,tz,proj):
    mapData = self.getTile(tx,ty,tz)
    if(mapData):
      for wayID, way in mapData.ways.items():
        if(not self.waysDrawn.get(wayID, False)): # if not drawn already as part of another tile
          styleList = self.style(way['t'])
          if(styleList):
            for style in styleList:
              cr = self.getCtx(style[2]['layer'])
              self.setStyle(style, cr)
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
          self.waysDrawn[wayID] = True

  def drawMap(self, cr):
    (sx,sy,sw,sh) = self.get('viewport')
    proj = self.m.get('projection', None)
    if(not proj or not proj.isValid()):
      return
    
    z = int(self.get('z', 15))
    layer = self.get('layer','pyrender')
    self.layers = {}
    
    # Render each 'tile' in view
    self.waysDrawn = {}
    for x in range(int(floor(proj.px1)), int(ceil(proj.px2))):
      for y in range(int(floor(proj.py1)), int(ceil(proj.py2))):
        self.drawTile(cr,x,y,z,proj)
   
    layerIDs = self.layers.keys()
    layerIDs.sort()
    for num in layerIDs:
      layer = self.layers[num]
      cr.set_source_surface(layer[0], 0, 0)
      cr.set_operator(cairo.OPERATOR_OVER)
      cr.paint()
    self.layers = {}


  def getLayer(self, layernum):
    layerid = str(layernum)
    layer = self.layers.get(layerid, None)
    if(not layer):
      # Create the image
      (sx,sy,sw,sh) = self.get('viewport')
      im = cairo.ImageSurface(cairo.FORMAT_ARGB32, sw,sh)
      ctx = cairo.Context(im)  # create a drawing context
      layer = [im, ctx, layerid]
      self.layers[layerid] = layer
    return(layer)

  def getCtx(self, layernum):
    layer = self.getLayer(layernum)
    return(layer[1])
    
  def update(self):
    pass
