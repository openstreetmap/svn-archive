#!/usr/bin/python
#-----------------------------------------------------------------------------
# Projection code (lat/long to screen conversions)
#
# Usage: 
#   (library code for pyroute GUI, not for direct use)
#-----------------------------------------------------------------------------
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
#-----------------------------------------------------------------------------
from tilenames import *

class Projection:
  def __init__(self):
    self.xyValid = False
    self.llValid = False
    self.needsEdgeFind = False
  def isValid(self):
    return(self.xyValid and self.llValid)
  def setView(self,x,y,w,h):
    self.w = w
    self.h = h
    self.xc = x + self.w
    self.yc = y + self.h
    self.xyValid = True
    if(self.needsEdgeFind):
      self.findEdges()
    
  def recentre(self,lat,lon,zoom = None):
    print "centering on %1.3f, %1.3f" % (lat,lon)
    self.lat = lat
    self.lon = lon
    if(zoom != None):
      self.implementNewZoom(zoom)
    else:
      self.findEdges()
    self.llValid = True
    
  def setZoom(self, value, isAdjustment=False):
    if(isAdjustment):
      self.implementNewZoom(self.zoom + value)
    else:
      self.implementNewZoom(value)
  
  def limitZoom(self):
    if(self.zoom < 6):
      self.zoom = 6
    if(self.zoom > 17):
      self.zoom = 17
      
  def implementNewZoom(self, zoom):
    self.zoom = int(zoom)
    print "zoom set to %d" % zoom
    self.limitZoom()
    self.findEdges()
  
  def findEdges(self):
    """(S,W,E,N) are derived from (lat,lon,scale)"""
    if(not self.xyValid):
      print "Can't find edges"
      self.needsEdgeFind = True
      return
    self.px, self.py = latlon2xy(self.lat,self.lon,self.zoom)
    
    print "Centred on %1.1f,%1.1f" % (self.px,self.py)
    unitSize = tileSizePixels()
    
    self.px1 = self.px - 0.5 * self.w / unitSize
    self.px2 = self.px + 0.5 * self.w / unitSize
    self.py1 = self.py - 0.5 * self.h / unitSize
    self.py2 = self.py + 0.5 * self.h / unitSize
    
    self.pdx = self.px2 - self.px1
    self.pdy = self.py2 - self.py1
    
    print "Viewing %1.1f - %1.1f, %1.1f - %1.1f" % \
      (self.px1,
      self.px2,
      self.py1,
      self.py2);
    
    self.N,self.W = xy2latlon(self.px1, self.py1, self.zoom)
    self.S,self.E = xy2latlon(self.px2, self.py2, self.zoom)
    
    print "Lat %1.1f - %1.1f, Lon %1.1f - %1.1f" % \
      (self.S,
      self.N,
      self.W,
      self.E)
      
    self.needsEdgeFind = False
  
  def pxpy2xy(self,px,py):
    x = self.w * (px - self.px1) / self.pdx
    y = self.h * (py - self.py1) / self.pdy
    return(x,y)
  
  def nudge(self,dx,dy):
    unitSize = tileSizePixels()
    newXC = self.px - dx / unitSize
    newYC = self.py - dy / unitSize
    
    if(1):
      self.lat,self.lon = xy2latlon(newXC,newYC, self.zoom)
    else:
      self.lat,self.lon = self.xy2ll(newXC,newYC) 
    self.findEdges()

  def ll2xy(self,lat,lon):
    px,py = latlon2xy(lat,lon,self.zoom)
    #print "%1.3f,%1.3f -> %1.1f,%1.1f" % (lat,lon,px,py)
    unitSize = tileSizePixels()
    x = (px - self.px1) * unitSize
    y = (py - self.py1) * unitSize
    return(x,y)
  
  def xy2ll(self,x,y):
    unitSize = tileSizePixels()
    px = self.px1 + x / unitSize
    py = self.py1 + y / unitSize
    lat,lon = xy2latlon(px, py, self.zoom)
    return(lat,lon)
  
  def onscreen(self,x,y):
    return(x >= 0 and x < self.w and y >= 0 and y < self.h)
  
  def relXY(self,x,y):
    return(x/self.w, y/self.h)
    
