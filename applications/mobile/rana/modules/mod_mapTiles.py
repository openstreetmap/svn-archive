#!/usr/bin/python
#----------------------------------------------------------------------------
# Display map tile images
#----------------------------------------------------------------------------
# Copyright 2007-2008, Oliver White
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
from module_base import ranaModule
from threading import Thread
import cairo
import os
import urllib

def getModule(m,d):
  return(mapTiles(m,d))

class mapTiles(ranaModule):
  """Display map images"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.images = {}
    self.threads = {}
  
  def drawMap(self, cr):
    (x,y,w,h) = self.get('viewport')
    # TODO: draw correct images
    self.loadImage(16308, 10871, 15, 'default')
    self.drawImage(cr, "default_15_16308_10871", 100,100)
    
  def update(self):
    # TODO: detect images finished downloading, and request update
    pass
  
  def drawImage(self,cr, name, x,y):
    """Draw a tile image"""
    
    # If it's not in memory, then stop here
    if not name in self.images.keys():
      print "Not loaded"
      return
    
    # TODO: Move the cairo projection onto the area where we want to draw the image
    cr.save()
    cr.translate(x,y)
    
    # Display the image
    cr.set_source_surface(self.images[name],0,0)
    cr.paint()
    
    # Return the cairo projection to what it was
    cr.restore()
    
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
    filename = "cache/images/%s.png" % name
    if os.path.exists(filename):
      self.images[name]  = cairo.ImageSurface.create_from_png(filename)
      return
    
    # Image not found anywhere - resort to downloading it
    if(1):
      self.threads[name] = tileLoader(x,y,z,layer,filename)
      self.threads[name].start()
    else:
      downloadTile(x,y,z,layer,filename)
    self.set("needRedraw", True)
    
  def imageName(self,x,y,z,layer):
    """Get a unique name for a tile image 
    (suitable for use as part of filenames, dictionary keys, etc)"""
    return("%s_%d_%d_%d" % (layer,z,x,y))



def downloadTile(x,y,z,layer,filename):
  """Downloads an image"""
  url = 'http://tah.openstreetmap.org/Tiles/tile/%d/%d/%d.png' % (z,x,y)
  urllib.urlretrieve(url, filename)
  
class tileLoader(Thread):
  """Downloads an image (in a thread)"""
  def __init__(self, x,y,z,layer,filename):
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
