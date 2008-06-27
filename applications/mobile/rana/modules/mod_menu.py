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
    self.setupGeneralMenus()

  def drawButton(self, cr, x1, y1, w, h, icon='generic', action=''):
    # Draw icon
    if(icon != None):
      m = self.m.get('icons', None)
      if(m != None):
        m.draw(cr,icon,x1,y1,w,h)
    
    # Make clickable
    if(action != None):
      m = self.m.get('clickHandler', None)
      if(m != None):
        m.registerXYWH(x1,y1,w,h, action)
      
  def drawMenuItem(self, cr, menu, id, x, y, w, h):
    menu = self.menus.get(menu, None)
    if(menu == None):
      return
    item = menu.get(id, None)
    if(item == None):
      return
      
    (text, icon, action) = item
    self.drawButton(cr,x,y,w,h,icon,action)
  
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
        self.drawMenuItem(cr, menu, id, x1+x*dx, y1+y*dy, dx, dy)
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
  
  def clearMenu(self, menu, cancelButton='set:menu:None'):
    self.menus[menu] = {}
    if(cancelButton != None):
      self.addItem(menu,'','up', cancelButton, 0)

  def addItem(self, menu, text, icon=None, action=None, pos=None):
    self.menus[menu][pos] = (text, icon, action)
    
  def setupGeneralMenus(self):
    self.clearMenu('main')
    
    
    
if(__name__ == "__main__"):
  a = menus({},{'viewport':(0,0,600,800)})
  a.drawMapOverlay(None)