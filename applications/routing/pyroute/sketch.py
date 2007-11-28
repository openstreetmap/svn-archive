#!/usr/bin/python
#-----------------------------------------------------------------------------
# 
#
# Usage: 
# 
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
import os
import cairo

class sketching(pyrouteModule):
  """  """
  def __init__(self, modules):
    pyrouteModule.__init__(self, modules)
    self.lines = []
    
  def startStroke(self,x,y):
    lat,lon = self.m['projection'].xy2ll(x,y)
    self.latest = [(lat,lon)]
    self.lines.append(self.latest)
    
  def moveTo(self,x,y):
    lat,lon = self.m['projection'].xy2ll(x,y)
    self.latest.append((lat,lon))
    self.set("needRedraw", 1)

  def draw(self,cr,proj):
    cr.set_line_cap(cairo.LINE_CAP_ROUND)
    cr.set_source_rgb(0.7,0.7,0)
    cr.set_line_width(5)

    for l in self.lines:
      first = 1
      for p in l:
        x,y = proj.ll2xy(p[0], p[1])
        if(first):
          cr.move_to(x,y)
          first = 0
        else:
  	      cr.line_to(x,y)
      cr.stroke()
      
      
  