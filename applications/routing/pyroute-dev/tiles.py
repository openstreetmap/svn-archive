﻿#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Tile image handler (download, cache, and display tiles) 

Usage: 
  (library code for pyroute GUI, not for direct use)
"""

__version__ = "$Rev$"
__license__ = """This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>."""
_debug = 0


from base import pyrouteModule
from tilenames import *
import urllib
import os
import cairo
from threading import Thread

def downloadTile(x,y,z,layer,filename):
  url = tileURL(x,y,z,layer)
  if(tileLayerExt(layer) != "png"):
    base,ext = os.path.splitext(filename)
    tempFilename = "%s.%s" % (base, tileLayerExt(layer))
    urllib.urlretrieve(url, tempFilename)
    sys = "convert %s %s" % (tempFilename, filename)
    print "Running \"%s\"" % sys
    os.system(sys)
    os.unlink(tempFilename)
  else:
    urllib.urlretrieve(url, filename)
  
class tileLoader(Thread):
  """Downloads images in a separate thread"""
  def __init__(self, x,y,z,layer,filename):
    """Download a tile image"""
    self.x = x
    self.y = y
    self.z = z
    self.layer = layer
    self.finished = 0
    self.filename = filename
    Thread.__init__(self)
    
  def run(self):
    downloadTile( \
      self.x,
      self.y,
      self.z,
      self.layer,
      self.filename)
      
    self.finished = 1

class tileHandler(pyrouteModule):
  """Loads and displays map tiles"""
  def __init__(self, modules):
    pyrouteModule.__init__(self, modules)
    self.images = {}
    self.threads = {}
    self.downloadInThread = False
    
  def __del__(self):
    print "Shutting-down tiles"
    for name,thread in self.threads.items():
      pass
  def imageName(self,x,y,z,layer):
    """Get a unique name for a tile image 
    (suitable for use as part of filenames, dictionary keys, etc)"""
    return("%s_%d_%d_%d" % (layer,z,x,y))
  
  def loadImage(self,x,y,z, layer):
    """Check that an image is loaded, and try to load it if not"""
    
    # First: is the image already in memory?
    name = self.imageName(x,y,z,layer)
    if name in self.images.keys():
      return
    
    # Second, is it already in the process of being downloaded?
    if name in self.threads.keys():
      if(not self.threads[name].finished):
        return
    
    # Third, is it in the disk cache?  (including ones recently-downloaded)
    filename = os.path.join(os.path.dirname(__file__), "cache", "%s.png" % name)
    if os.path.exists(filename):
      self.images[name]  = cairo.ImageSurface.create_from_png(filename)
      return
    
    # Image not found anywhere - resort to downloading it
    print "Downloading %s" % (name)
    if(self.downloadInThread):
      self.threads[name] = tileLoader(x,y,z,layer,filename)
      self.threads[name].start()
    else:
      downloadTile(x,y,z,layer,filename)
    self.set("needRedraw", True)
    
  def drawImage(self,cr, tile, bbox):
    """Draw a tile image"""
    name = self.imageName(tile[0],tile[1],tile[2], tile[3])
    # If it's not in memory, then stop here
    if not name in self.images.keys():
      return
    # Move the cairo projection onto the area where we want to draw the image
    cr.save()
    cr.translate(bbox[0],bbox[1])
    
    #cr.scale((bbox[2] - bbox[0]) / 256.0, (bbox[3] - bbox[1]) / 256.0)
    
    # Display the image
    cr.set_source_surface(self.images[name],0,0)
    cr.paint()
    
    # Return the cairo projection to what it was
    cr.restore()
    
  def draw(self, cr):
    """Draw all the map tiles that are in view"""
    proj = self.m['projection']
    layer = self.get("layer","tah")
    
    if(not proj.isValid()):
      return;
    # Pick a zoom level
    z = proj.zoom
    
    # Loop through all tiles
    for x in range(int(floor(proj.px1)), int(ceil(proj.px2))):
      for y in range(int(floor(proj.py1)), int(ceil(proj.py2))):
        
        
        if(1):
          # Find the edges of the tile as lat/long
          #S,W,N,E = tileEdges(x,y,z) 
          
          # Convert those edges to screen coordinates
          x1,y1 = proj.pxpy2xy(x,y)
          x2,y2 = proj.pxpy2xy(x+1,y+1)
          
          #print "%d,%d,%d -> %1.1f-%1.1f"%(x,y,z,x1,x2)
          
          # Check that the image is available in the memory cache
          # and start downloading it if it's not
          self.loadImage(x,y,z,layer)
          
          # Draw the image
          self.drawImage(cr,(x,y,z,layer),(x1,y1,x2,y2))
