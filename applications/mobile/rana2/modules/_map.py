#!/usr/bin/python
#----------------------------------------------------------------------
# GTK widget to display a map on screen
#----------------------------------------------------------------------
# Copyright 2007-2009, Oliver White
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
#----------------------------------------------------------------------
import gtk
from _cairo_widget import *

class MapWidget(cairo_widget):
  def __init__(self, d):
    cairo_widget.__init__(self, d)

  def draw(self, cr):
    cr.set_source_rgb(1,1,0)
    cr.rectangle(0,0,self.rect.width,self.rect.height)
    cr.fill()
    
    cr.set_source_rgb(0,1,0)
    cr.rectangle(10,10,self.rect.width-20,self.rect.height-20)
    cr.fill()
    
    map_modules = []
    for m in map_modules:
      m.beforeDraw()
    for m in map_modules:
      m.drawMap(cr)
    for m in map_modules:
      m.drawMapOverlay(cr)

