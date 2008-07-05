#!/usr/bin/python
#---------------------------------------------------------------------------
# Controls the view being displayed on the map
#---------------------------------------------------------------------------
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
from base_module import ranaModule
from time import time
from tilenames import *

def getModule(m,d):
  return(mapView(m,d))

class mapView(ranaModule):
  """Controls the view being displayed on the map"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.updateTime = 0
    
  def handleMessage(self, message):
    z = self.get('z', 15)
    if(message == 'zoomIn'):
      self.set('z', z + 1)
    elif(message == 'zoomOut'):
      self.set('z', max(z - 1, 8))
  
  def dragEvent(self,startX,startY,dx,dy,x,y):
    if(not self.get("centred",True)):
      m = self.m.get('projection', None)
      if(m):
        m.nudge(dx,dy)
        self.set('needRedraw', True)

  def update(self):
    # Run scheduledUpdate every second
    if(self.get("centred",True) or self.get('neverBeenCentred', True)):
      t = time()
      dt = t - self.updateTime
      if(dt > 2):
        self.updateTime = t
        pos = self.get('pos', None)
        if(pos != None):
          self.set('map_centre', pos)
          self.setCentre(pos)
          self.set('needRedraw', True)
          self.set('neverBeenCentred', False)

  def setCentre(self,pos):
    proj = self.m.get('projection', None)
    if(proj == None):
      return;
    if(pos == None):
      return
    
    (lat,lon) = pos

    z = int(self.get('z', 15))
    x,y = latlon2xy(lat,lon,z)

    if(not self.d.has_key('viewport')):
      return
    (sx,sy,sw,sh) = self.get('viewport')
    proj.setView(sx,sy,sw,sh)
    proj.recentre(lat,lon,z)
    proj.setZoom(z)
