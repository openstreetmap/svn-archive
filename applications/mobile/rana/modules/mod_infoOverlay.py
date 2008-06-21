#!/usr/bin/python
#----------------------------------------------------------------------------
# Overlay some information on the map screen
#----------------------------------------------------------------------------
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
#---------------------------------------------------------------------------
from module_base import ranaModule
import cairo

def getModule(m,d):
  return(infoOverlay(m,d))

class infoOverlay(ranaModule):
  """Overlay info on the map"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.lines = [['hello',''], ['world',''], ['line3',''], ['line4','']]
    
  def update(self):
    # Detect changes to the line text, and ask for redraw if necessary
    for line in self.lines:
      (value,oldvalue) = line
      if(value != oldvalue):
        self.set('needRedraw', True)
        line[1] = line[0] # copy new value to old

  def drawMap(self, cr):
    (x,y,w,h) = self.get('viewport')

    # Bottom of screen:
    dy = h * 0.13
    border = 10

    y2 = y + h
    y1 = y2 - dy
    x1 = x

    print "dy = %f" % dy
    numlines = len(self.lines)
    print "%d lines" % numlines
    linespacing = (dy / numlines)
    fontsize = linespacing * 0.8
    print "Line space %f" % linespacing
    print "Font size %f" % fontsize
    cr.set_source_rgb(0,0,0)
    cr.rectangle(x1,y1,w,dy)
    cr.fill()
    cr.set_source_rgb(1,0,0)
    cr.stroke()
    
    cr.set_source_rgb(1.0, 1.0, 0.0)
    cr.set_font_size(fontsize)

    liney = y1
    for line in self.lines:
      text = line[0]
      cr.move_to(x1+border,liney + (0.9 * linespacing))
      cr.show_text(text)
      cr.stroke()
      liney += linespacing

