#!/usr/bin/python
#----------------------------------------------------------------
# Generates map tiles showing a route from stored CSV file
#----------------------------------------------------------------
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
#----------------------------------------------------------------
from tile_base import TileBase

class TileRoute(TileBase):
  def __init__(self, filename):
    self.filename = filename
  def draw(self):
    self.ctx.set_source_rgb(0, 0, 0)
    f = open(self.filename, 'r')
    if(f):
      first = True
      for line in f:
        (id,lat,lon) = map(float, line.split(','))
        (x,y) = self.proj.project(lat,lon)
        #print "%f,%f -> %f,%f" % (lat,lon,x,y)
        if(first):
          self.ctx.move_to(x,y)
          first = False
        else:
          self.ctx.line_to(x,y)
      f.close()
      self.ctx.stroke()
