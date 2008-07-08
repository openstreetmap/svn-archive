#!/usr/bin/python
#----------------------------------------------------------------------------
# Handles downloading of map data
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
from tilenames import *

import sys
if(__name__ == '__main__'):
  sys.path.append('pyroutelib2')
else:
  sys.path.append('modules/pyroutelib2')

import tiledata

def getModule(m,d):
  return(mapData(m,d))

class mapData(ranaModule):
  """Handle downloading of map data"""
  
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)

  def listTiles(self, route):
    """List all tiles touched by a polyline"""
    tiles = {}
    for pos in route:
      (lat,lon) = pos
      (tx,ty) = tileXY(lat, lon, 15)
      tile = "%d,%d" % (tx,ty)
      if(not tiles.has_key(tile)):
        tiles[tile] = True
    return(tiles.keys())

  def addToQueue(self, tilesToDownload):
    for tile in tilesToDownload:
      (x,y,z) = tile
      # for now, just get them without updating screen
      tiledata.GetOsmTileData(z,x,y)

  def handleMessage(self, message):
    if(message == "download"):
      size = int(self.get("downloadSize", 4))

      type = self.get("downloadType")
      if(type != "data"):
        print "Error: mod_mapData can't download %s" % type
        return
      
      location = self.get("downloadArea", "here") # here or route

      # Which zoom level are map tiles stored at
      z = tiledata.DownloadLevel()

      if(location == "here"):
        
        # Find which tile we're on
        pos = self.get("pos",None)
        if(pos != None):
          (lat,lon) = pos
          (x,y) = latlon2xy(lat,lon,z)

          self.addToQueue(self.spiral(x,y,z,size))

  def expand(self, tileset, amount=1):
    """Given a list of tiles, expand the coverage around those tiles"""
    tiles = {}
    tileset = [[int(b) for b in a.split(",")] for a in tileset]
    for tile in tileset:
      (x,y) = tile
      for dx in range(-amount, amount+1):
        for dy in range(-amount, amount+1):
          tiles["%d,%d" % (x+dx,y+dy)] = True
    return(tiles.keys())

  def spiral(self, x, y, z, distance):
    class spiraller:
      def __init__(self,x,y,z):
        self.x = x
        self.y = y
        self.z = z
        self.tiles = [(x,y,z)]
      def moveX(self,dx, direction):
        for i in range(dx):
          self.x += direction
          self.touch(self.x, self.y, self.z)
      def moveY(self,dy, direction):
        for i in range(dy):
          self.y += direction
          self.touch(self.x, self.y, self.z)
      def touch(self,x,y,z):
        self.tiles.append((x,y,z))
        
    s =spiraller(x,y,z)
    for d in range(1,distance+1):
      s.moveX(1, 1) # 1 right
      s.moveY(d*2-1, -1) # d*2-1 up
      s.moveX(d*2, -1)   # d*2 left
      s.moveY(d*2, 1)    # d*2 down
      s.moveX(d*2, 1)    # d*2 right
    return(s.tiles)

  def update(self):
    pass

  def tilesetToSvg(self, tileset, filename):
    f = open(filename, "w")
    f.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
    f.write("<svg\n   xmlns:svg=\"http://www.w3.org/2000/svg\"\n   xmlns=\"http://www.w3.org/2000/svg\"\n   version=\"1.0\"\n   width=\"1000\"\n   height=\"1000\"   id=\"svg2\">\n")
    f.write("  <g id=\"layer1\">\n")
    for tile in tileset:
      (x,y) = [int(a) for a in tile.split(",")]
      f.write("<rect width=\"1\" height=\"1\" x=\"%d\" y=\"%d\" style=\"fill:#800000;stroke:#000000;stroke-width:0.05;\" id=\"rect2160\" />\n" % (x,y))
    f.write("</g>\n")
    f.write("</svg>\n")


if(__name__ == "__main__"):
  from sample_route import *
  route = getSampleRoute()
  a = mapData({}, {})

  for d in range(1,10):
    # Create a spiral of tiles, radius d
    tileset = a.spiral(100,100, d)
    print "Spiral of width %d = %d locations" %(d,len(tileset))

    # Convert array to dictionary
    keys = {}
    count = 0
    for tile in tileset:
      key = "%d,%d" % tile
      keys[key] = " " * (5-len(str(count))) + str(count)
      count += 1

    # Print grid of values to a textfile
    f = open("tiles_%d.txt" % d, "w")
    for y in range(100 - d - 2, 100 + d + 2):
      for x in range(100 - d - 2, 100 + d + 2):
        key = "%d,%d" % (x,y)
        val = keys.get(key, " ")
        f.write("%s\t" % val)
      f.write("\n")
    f.close()
        
  # Load a sample route, and try expanding it
  if(1):
    tileset = a.listTiles(route)
    from time import time
    print "Route covers %d tiles" % len(tileset)
    for i in range(0,20):
      start = time()
      result = a.expand(tileset,i)
      dt = time() - start
      print " expand by %d to get %d tiles (took %1.3f sec)" % (i,len(result), dt)

      a.tilesetToSvg(result, "expand_%d.svg" % i)
