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
    self.lines = ['hello', 'world']
    self.oldlines = ['','']
    self.field = 0
    
  def gatherData(self):
    self.lines = []

    pos = self.get('pos', None)
    if(pos == None):
      self.lines.append('No position')
    else:
      self.lines.append('%1.4f, %1.4f' % pos)
      self.lines.append("Position from: %s" % self.get('pos_source', 'unknown'))
      self.lines.append("Field %d" % self.field)

  def update(self):
    # Fill-in the lines
    self.gatherData()
    
    # Then detect changes to the line text, and ask for redraw if necessary
    if(len(self.lines) != len(self.oldlines)):
      self.set('needRedraw', True)
    else:
      for i in range(len(self.lines)):
        if(self.lines[i] != self.oldlines[i]):
          self.set('needRedraw', True)
    self.oldlines = self.lines

  def handleMessage(self, message):
    if(message == 'nextField'):
      self.field += 1
      
  def drawMap(self, cr):
    (x,y,w,h) = self.get('viewport')

    # Bottom of screen:
    dy = h * 0.13
    border = 10

    y2 = y + h
    y1 = y2 - dy
    x1 = x

    # Clicking on the rectangle should toggle which field we display
    m = self.m.get('clickHandler', None)
    if(m != None):
      m.registerXYWH(x1,y1,w,dy, "infoOverlay:nextField")

    numlines = len(self.lines)
    linespacing = (dy / numlines)
    fontsize = linespacing * 0.8

    # Draw a background
    cr.set_source_rgb(0,0,0)
    cr.rectangle(x1,y1,w,dy)
    cr.fill()

    cr.set_source_rgb(1.0, 1.0, 0.0)

    liney = y1
    for text in self.lines:
      # Check that font size is small enough to display width of text
      fontx = w / len(text)
      if(fontx < fontsize):
        cr.set_font_size(fontx)
      else:
        cr.set_font_size(fontsize)

      # Draw the text
      cr.move_to(x1+border,liney + (0.9 * linespacing))
      cr.show_text(str(text))
      cr.stroke()
      liney += linespacing

