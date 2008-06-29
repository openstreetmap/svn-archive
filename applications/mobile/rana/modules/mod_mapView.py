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
from module_base import ranaModule
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
    #print "Map move from %1.2f,%1.2f by %1.2f,%1.2f to %1.2f,%1.2f" % (startX,startY,dx,dy,x,y)
    pass

  def update(self):
    # Run scheduledUpdate every second
    if(not self.get('centreOnce', False)):
      t = time()
      dt = t - self.updateTime
      if(dt > 2):
        self.updateTime = t
        pos = self.get('pos', False)
        if(pos != False):
          self.set('map_centre', pos)
          self.setCentre(pos)
          self.set('needRedraw', True)
          self.set('centreOnce', True)

  def setCentre(self,pos):
    proj = self.m.get('projection', None)
    if(proj == None):
      return;

    (lat,lon) = pos

    z = int(self.get('z', 15))
    x,y = latlon2xy(lat,lon,z)

    (sx,sy,sw,sh) = self.get('viewport')
    proj.setView(sx,sy,sw,sh)
    proj.recentre(lat,lon,z)
    proj.setZoom(z)
