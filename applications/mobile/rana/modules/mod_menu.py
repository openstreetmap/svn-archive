#!/usr/bin/python
#----------------------------------------------------------------------------
# Handle menus
#----------------------------------------------------------------------------
# Copyright 2008, Oliver White
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
  return(menus(m,d))

class menus(ranaModule):
  """Handle menus"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.menus = {}

  def update(self):
    pass

  def handleMessage(self, message):
    if(message == 'close'):
      self.set('menu', None)
    else:
      self.set('menu', message)
    print "Set menu to %s" % message
    
  def drawButton(self, cr, x1, y1, w, h, icon, action):
    m = self.m.get('icons', None)
    if(m == None):
      return
    m.draw(cr,icon,x1,y1,w,h)

    m = self.m.get('clickHandler', None)
    if(m != None):
      m.registerXYWH(x1,y1,w,h, action)
      
  def drawMenuItem(self, cr, menu, x, y, w, h):
    self.drawButton(cr, x,y,w,h,'default', 'menu:close')
  
  def drawMenu(self, cr):
    """Draw menus"""
    menu = self.get('menu', None)
    if(menu == None):
      return
    
    (x1,y1,w,h) = self.get('viewport')

    cols = 3
    rows = 4
    
    dx = w / cols
    dy = h / rows

    id = 0
    for x in range(cols):
      for y in range(rows):
        self.drawMenuItem(cr, menu, x1+x*dx, y1+y*dy, dx, dy)
        id += 1

    return
      
    # Clicking on the rectangle should toggle which field we display
    m = self.m.get('clickHandler', None)
    if(m != None):
      m.registerXYWH(x1,y1,w,dy, "infoOverlay:nextField")
    # Draw a background
    cr.set_source_rgb(0,0,0)
    cr.rectangle(x1,y1,w,dy)
    cr.fill()

    numlines = len(self.lines)
    if(numlines < 1):
      return
    linespacing = (dy / numlines)
    fontsize = linespacing * 0.8

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

if(__name__ == "__main__"):
  a = menus({},{'viewport':(0,0,600,800)})
  a.drawMapOverlay(None)