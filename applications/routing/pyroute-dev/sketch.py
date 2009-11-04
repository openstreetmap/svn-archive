#!/usr/bin/python
#-----------------------------------------------------------------------------
# Sketch-on-map module for pyroute
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
from lib_gpx import lib_gpx

class sketching(pyrouteModule, lib_gpx):
  """  """
  def __init__(self, modules):
    lib_gpx.__init__(self)
    pyrouteModule.__init__(self, modules)
    
  def startStroke(self,x,y):
    lat,lon = self.m['projection'].xy2ll(x,y)
    self.latest = [(lat,lon)]
    self.lines.append(self.latest)
    
  def moveTo(self,x,y):
    lat,lon = self.m['projection'].xy2ll(x,y)
    self.latest.append((lat,lon))
    self.set("needRedraw", 1)

  def save(self):
      self.saveAs("data/sketches/latest.gpx")
      
