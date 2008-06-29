#!/usr/bin/python
#----------------------------------------------------------------------------
# Button overlays for zoom and menu
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
import cairo
from math import pi

def getModule(m,d):
  return(buttons(m,d))

class buttons(ranaModule):
  """Overlay info on the map"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
  
  def drawButton(self, cr, x1, y1, w, h, icon, action):
    m = self.m.get('icons', None)
    if(m == None):
      return
    m.draw(cr,icon,x1,y1,w,h)

    m = self.m.get('clickHandler', None)
    if(m != None):
      m.registerXYWH(x1,y1,w,h, action)

  def drawMapOverlay(self, cr):
    """Draw an overlay on top of the map, showing various information
    about position etc."""
    (x,y,w,h) = self.get('viewport')

    dx = w / 5.0
    dy = dx
    
    self.drawButton(cr, x+dx, y, dx, dy, "zoom_out", "mapView:zoomOut")
    self.drawButton(cr, x, y, dx, dy, "blank", "set:menu:main")
    self.drawButton(cr, x, y+dy, dx, dy, "zoom_in", "mapView:zoomIn")

    m = self.m.get('clickHandler', None)
    if(m != None):
      m.registerDraggable(x,y,x+w,y+h, "mapView")
    