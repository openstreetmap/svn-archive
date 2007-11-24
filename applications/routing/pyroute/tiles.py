#!/usr/bin/python
#-----------------------------------------------------------------------------
# Tile image handler (download, cache, and display tiles) 
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
from base import pyrouteModule
from tilenames import *
import urllib
import os
import cairo
from threading import Thread

class tileLoader(Thread):
  def __init__(self, x,y,z,filename):
    self.url = tileURL(x,y,z)
    self.finished = 0
    self.filename = filename
    Thread.__init__(self)
    self.run()
    
  def run(self):
    print "downloading %s" % self.filename
    urllib.urlretrieve(self.url, self.filename)
    print "done %s" % self.filename
    self.finished = 1
    
class tileHandler(pyrouteModule):
  def __init__(self, modules):
    pyrouteModule.__init__(self, modules)
    self.images = {}
    self.threads = {}
    
  def imageName(self,x,y,z):
    return("%d_%d_%d" % (z,x,y))
  
  def startDownloading(self,x,y,z,filename):
    name = self.imageName(x,y,z)
    
  def loadImage(self,x,y,z):
    """Check that an image is loaded, and try to load it if not"""
    
    # First: is the image already in memory?
    name = self.imageName(x,y,z)
    if name in self.images.keys():
      return
    
    # Second, is it already in the process of being downloaded?
    if name in self.threads.keys():
      if(not self.threads[name].finished):
        return
    
    # Third, is it in the disk cache?  (including ones recently-downloaded)
    filename = "cache/%s.png" % name
    if os.path.exists(filename):
      print "loading %s from disk" % name
      self.images[name]  = cairo.ImageSurface.create_from_png(filename)
      return
    
    # Image not found anywhere - resort to downloading it
    print "Starting to download %s" % name
    self.threads[name] = tileLoader(x,y,z,filename)
    
    
  def drawImage(self,cr, tile, bbox):
    name = self.imageName(tile[0],tile[1],tile[2])
    if not name in self.images.keys():
      return
    cr.save()
    cr.translate(bbox[0],bbox[1])
    cr.scale((bbox[2] - bbox[0]) / 256.0, (bbox[3] - bbox[1]) / 256.0)
    cr.set_source_surface(self.images[name],0,0)
    cr.paint()
    cr.restore()
    
  def zoomFromScale(self,scale):
    if(scale > 0.046):
      return(10)
    if(scale > 0.0085):
      return(13)
    if(scale > 0.0026):
      return(15)
    return(17)
    
  def tileZoom(self):
    return(self.zoomFromScale(self.m['projection'].scale))
  
  def draw(self, cr):
    proj = self.m['projection']
    z = self.tileZoom()
    view_x1,view_y1 = latlon2xy(proj.N, proj.W, z)
    view_x2,view_y2 = latlon2xy(proj.S, proj.E, z)
    for x in range(int(floor(view_x1)), int(ceil(view_x2))):
      for y in range(int(floor(view_y1)), int(ceil(view_y2))):
        S,W,N,E = tileEdges(x,y,z) 
        x1,y1 = proj.ll2xy(N,W)
        x2,y2 = proj.ll2xy(S,E)
        self.loadImage(x,y,z)
        self.drawImage(cr,(x,y,z),(x1,y1,x2,y2))
