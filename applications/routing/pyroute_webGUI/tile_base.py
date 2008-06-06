#!/usr/bin/python
#----------------------------------------------------------------------------
# Base class for rendering tile-style images
#----------------------------------------------------------------------------
# Copyright 2008, authors:
# * Sebastian Spaeth
# * Oliver White
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
import StringIO
import cairo
from tilenames import *

class proj:
  """Simple projection class for geographic data.  Converts lat/long to pixel position"""
  def __init__(self, tx,ty,tz, to):
    """Setup a projection.  
    tx,ty,tz is the slippy map tile number.  
    to is the (width,height) of an image to render onto
    """ 
    (S,W,N,E) = tileEdges(tx,ty,tz)
    self.to = to
    self.S = S
    self.N = N
    self.E = E
    self.W = W
    self.dLat = N - S
    self.dLon = E - W
    self.x1 = 0
    self.y1 = 0
    self.x2 = to[0]
    self.y2 = to[1]
    self.dx = self.x2 - self.x1
    self.dy = self.y2 - self.y1
  def project(self,lat,lon):
    """Project lat/long (in degrees) into pixel position on the tile"""
    pLat = (lat - self.S) / self.dLat
    pLon = (lon - self.W) / self.dLon
    x = self.x1 + pLon * self.dx
    y = self.y2 - pLat * self.dy
    return(x,y)

class TileBase:
  def draw(self):
    pass # Override this function
    
  def RenderTile(self, z,x,y, outputFile):
    
    # Create the image
    im = cairo.ImageSurface (cairo.FORMAT_ARGB32, 256, 256)
    self.proj = proj(x,y,z,(256,256))  # create a projection for this tile
    self.ctx = cairo.Context(im)  # create a drawing context

    # Call some overloaded draw function from a derived class
    self.draw()

    # Either save the result to a file
    if(outputFile):
      im.write_to_png(outputFile) # Output to PNG
      return
    else:
      # Or return it in a string
      f = StringIO.StringIO()
      im.write_to_png(f)
      data = f.getvalue()
      return(data)

if(__name__ == '__main__'):
  # Test suite: render a tile in hersham, and save it to a file
  a = OsmRenderBase()
  a.RenderTile(17,65385,43658, "sample2.png")
