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
    
  def drawText(self,cr,text,x,y,w,h,border=0):
    if(not text):
      return
    # Put a border around the area
    if(border != 0):
      x += w * border
      y += h * border
      w *= (1-2*border)
      h *= (1-2*border)
    
    if(0): # optional choose a font
      self.cr.select_font_face(
        'Verdana',
        cairo.FONT_SLANT_NORMAL,
        cairo.FONT_WEIGHT_BOLD)

    # Pick a font size to fit
    test_fontsize = 60
    cr.set_font_size(test_fontsize)
    _s1, _s2, textwidth, textheight, _s3, _s4 = cr.text_extents(text)
    ratio = max(textwidth / w, textheight / h)

    # Draw text
    cr.set_font_size(test_fontsize / ratio)
    cr.move_to(x, y + h)
    cr.show_text(text)
        
  def drawButton(self, cr, x1, y1, w, h, text='', icon='generic', action=''):
    """Draw a clickable button, with icon image and text"""
    
    # Draw icon
    if(icon != None):
      m = self.m.get('icons', None)
      if(m != None):
        m.draw(cr,icon,x1,y1,w,h)

    # Draw text
    cr.set_source_rgb(0, 0, 0.3)
    self.drawText(cr, text, x1, y1+0.5*h, w, 0.4*h, 0.15)

    # Make clickable
    if(action != None):
      m = self.m.get('clickHandler', None)
      if(m != None):
        m.registerXYWH(x1,y1,w,h, action)

  def drawMenu(self, cr):
    """Draw menus"""
    menuName = self.get('menu', None)
    if(menuName == None):
      return

    # Find the menu
    menu = self.menus.get(menuName, None)
    if(menu == None):
      print "Menu %s doesn't exist, returning to main screen" % menuName
      self.set('menu', None)
      self.set('needRedraw', True)
      return

    # Find the screen
    (x1,y1,w,h) = self.get('viewport')

    # Decide how to layout the menu
    cols = 3
    rows = 4
    
    dx = w / cols
    dy = h / rows

    # for each item in the menu
    id = 0
    for y in range(rows):
      for x in range(cols):
        item = menu.get(id, None)
        if(item == None):
          return
        
        # Draw it
        (text, icon, action) = item
        self.drawButton(cr, x1+x*dx, y1+y*dy, dx, dy, text, icon, action)
        id += 1

  def clearMenu(self, menu, cancelButton='set:menu:None'):
    self.menus[menu] = {}
    if(cancelButton != None):
      self.addItem(menu,'','up', cancelButton, 0)

  def addItem(self, menu, text, icon=None, action=None, pos=None):
    i = 0
    while(pos == None):
      if(self.menus[menu].get(i, None) == None):
        pos = i
      i += 1
      if(i > 20):
        print "Menu full, can't add %s" % text
    self.menus[menu][pos] = (text, icon, action)
    
  def setupTransportMenu(self):
    self.clearMenu('transport')
    for(label, mode) in { \
      'Bike':'cycle',
      'Walk':'foot',
      'MTB':'cycle',
      'Car':'car',
      'Hike':'foot',
      'FastBike':'cycle',
      'Train':'train',
      'HGV':'hgv'}.items():
      self.addItem(
        'transport',                       # menu
        label,                             # label
        label.lower(),                     # icon
        'set:mode:'+mode+"|set:menu:None") # action

  def setupSearchMenus(self):
    f = open("data/search_menu.txt", "r")
    self.clearMenu('search')
    sectionID = None
    for line in f:
      if(line[0:3] == '== '):
        section = line[3:].strip()
        sectionID = 'search_'+section.lower()
        self.addItem('search', section, section.lower(), 'set:menu:'+sectionID)
        self.clearMenu(sectionID)
      else:
        details = line.strip()
        if(details and sectionID):
          (name,filter) = details.split('|')
          self.addItem(sectionID, name, name, '')
    f.close()
  
  def setupGeneralMenus(self):
    self.clearMenu('main')
    self.addItem('main', 'map', 'generic', 'set:menu:layers')
    self.addItem('main', 'centre', 'centre', 'toggle:centred')
    self.addItem('main', 'search', 'business', 'set:menu:search')
    self.addItem('main', 'view', 'view', 'set:menu:view')
    self.addItem('main', 'options', 'options', 'set:menu:options')
    self.addItem('main', 'mode', 'transport', 'set:menu:transport')
    self.setupTransportMenu()
    self.setupSearchMenus()
    
    
if(__name__ == "__main__"):
  a = menus({},{'viewport':(0,0,600,800)})
  #a.drawMapOverlay(None)
  a.setupSearchMenus()
  