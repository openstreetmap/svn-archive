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
  
  def update(self):
    pass
  
  def handleMessage(self, message):
    if(message == 'nextField'):
      self.mode += 1
      if(self.mode >= len(self.modes)):
        self.mode = 0
      self.onModeChange()

  def drawButton(self, cr, x1, y1, w, h, action):
    r = 0.5 * min(w,h) - 6
    cr.set_line_width(3)

    cr.arc(x1+0.5*w, y1+0.5*h, r, 0, 2.0*pi)
    cr.set_source_rgb(0,0,1)
    cr.stroke()

    m = self.m.get('clickHandler', None)
    if(m != None):
      m.registerXYWH(x1,y1,w,h, action)

  def drawMapOverlay(self, cr):
    """Draw an overlay on top of the map, showing various information
    about position etc."""
    (x,y,w,h) = self.get('viewport')

    dx = w / 5.0
    dy = dx
    
    self.drawButton(cr, x+dx, y, dx, dy, "mapView:zoomOut")
    self.drawButton(cr, x, y, dx, dy, "menu:show")
    self.drawButton(cr, x, y+dy, dx, dy, "mapView:zoomOut")
