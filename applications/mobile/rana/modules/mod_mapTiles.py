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
import sys
import urllib
from tilenames import *
sys.path.append("modules/pyrender")
import renderer_default as RenderModule

def getModule(m,d):
  return(mapTiles(m,d))

maplayers = {
  'pyrender':
    {
    'label':'pyrender',
    'pyrender':True,
    'type':'png'
    },
  'osma':
    {
    'label':'OSM T@h',
    'tiles':'http://tah.openstreetmap.org/Tiles/tile/',
    'type':'png'
    },
  'mapnik':
    {
    'label':'Mapnik',
    'tiles':'http://tile.openstreetmap.org/',
    'type':'png'
    },
  'aerial':
    {
    'label':'Aerialmap',
    'tiles':'http://oam1.hypercube.telascience.org/tiles/1.0.0/openaerialmap-900913/',
    'type':'jpg'
    },
  'cycle':
    {
    'label':'Cycle map',
    'tiles':'http://thunderflames.org/tiles/cycle/',
    'type':'png'
    },
  'localhost':
    {
    'label':'Localhost',
    'tiles':'http://localhost:1280/default/',
    'type':'png'
    }
  };
  
class mapTiles(ranaModule):
  """Display map images"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.images = {}
    self.threads = {}
  
  def drawMapOverlay(self, cr):
    """Draw an "own position" marker (TODO: probably should be in different module)"""

    # Where are we?
    pos = self.get('pos', None)
    if(pos == None):
      return
    (lat,lon) = pos

    # Where is the map?
    proj = self.m.get('projection', None)
    if(proj == None):
      return
    if(not proj.isValid()):
      return

    # Where are we on the map?
    x1,y1 = proj.ll2xy(lat,lon)

    # What are we?
    angle = self.get('bearing', 0)

    # Draw yellow/black triangle showing us
    cr.set_source_rgb(1.0, 1.0, 0.0)
    cr.save()
    cr.translate(x1,y1)
    cr.rotate(radians(angle))
    cr.move_to(-10, 15)
    cr.line_to( 10, 15)
    cr.line_to(  0, -15)
    cr.fill()
    cr.set_source_rgb(0.0, 0.0, 0.0)
    cr.set_line_width(3)
    cr.move_to(-10, 15)
    cr.line_to( 10, 15)
    cr.line_to(  0, -15)
    cr.close_path()
    cr.stroke()
    cr.restore()

  def drawMap(self, cr):
    """Draw map tile images"""
    (sx,sy,sw,sh) = self.get('viewport')
    
    z = int(self.get('z', 15))

    proj = self.m.get('projection', None)
    if(proj == None):
      return
    if(not proj.isValid()):
      return

    layer = self.get('layer','pyrender')
    
    # Cover the whole map view with tiles
    for x in range(int(floor(proj.px1)), int(ceil(proj.px2))):
      for y in range(int(floor(proj.py1)), int(ceil(proj.py2))):
        
        # Convert corner to screen coordinates
        x1,y1 = proj.pxpy2xy(x,y)

        # Try to load and display images
        name = self.loadImage(x,y,z,layer)
        if(name != None):
          self.drawImage(cr,name,x1,y1)

  def update(self):
    # TODO: detect images finished downloading, and request update
    pass
  
  def drawImage(self,cr, name, x,y):
    """Draw a tile image"""
    
    # If it's not in memory, then stop here
    if not name in self.images.keys():
      print "Not loaded"
      return
    
    # Move the cairo projection onto the area where we want to draw the image
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
      return(name)
    
    # Second, is it already in the process of being downloaded?
    if name in self.threads.keys():
      if(not self.threads[name].finished):
        return(None)
    
    # Third, is it in the disk cache?  (including ones recently-downloaded)
    layerInfo = maplayers.get(layer, None)
    if(layerInfo == None):
      return

    layerType = layerInfo.get('type','png')
    filename = "cache/images/%s.%s" % (name, layerType)
    if(os.path.exists(filename)):
      if(layerType == 'jpg'):
        self.images[name]  = cairo.ImageSurface.create_from_jpeg(filename)
      else:
        self.images[name]  = cairo.ImageSurface.create_from_png(filename)
      return(name)
    
    # Image not found anywhere - resort to downloading it
    if(self.get('threadedDownload',True)):
      self.threads[name] = tileLoader(x,y,z,layer,filename)
      self.threads[name].start()
      return(None)
    else:
      downloadTile(x,y,z,layer,filename)
      return(name)

  def imageName(self,x,y,z,layer):
    """Get a unique name for a tile image 
    (suitable for use as part of filenames, dictionary keys, etc)"""
    return("%s_%d_%d_%d" % (layer,z,x,y))

  def layers(self):
    return(maplayers)

def downloadTile(x,y,z,layer,filename):
  """Downloads an image"""
  layerDetails = maplayers.get(layer, None)
  if(layerDetails == None):
    return

  if(layerDetails.get('pyrender',False)):
    # Generate from local data
    renderer = RenderModule.RenderClass()
    renderer.RenderTile(z,x,y, 'default', filename) # TODO: pyrender layers
  else:
    # Download from network
    url = '%s/%d/%d/%d.%s' % (
      layerDetails['tiles'],
      z,x,y,
      layerDetails.get('type','png'))
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
