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
from lib_gpx import lib_gpx

class tracklog(pyrouteModule, lib_gpx):
  """  """
  def __init__(self, modules):
    lib_gpx.__init__(self)
    pyrouteModule.__init__(self, modules)
    self.active = []
    self.lines.append(self.active)
    self.pointsSinceSave = 0
    
  def addPoint(self,lat,lon):
    self.active.append([lat,lon])
    self.pointsSinceSave = self.pointsSinceSave + 1

  def checkIfNeedsSaving(self, forceSave = False):
    if(self.pointsSinceSave > 20 or forceSave):
      self.save()
      
  def save(self):
    self.saveAs("data/tracklogs.gpx", "tracklogs")
    self.pointsSinceSave = 0
    