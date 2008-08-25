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
from base_module import ranaModule
import cairo
from listable_menu import listable_menu

def getModule(m,d):
  return(menus(m,d))

class menus(ranaModule):
  """Handle menus"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.menus = {}
    self.lists = {}
    self.listOffset = 0
    self.setupGeneralMenus()

  def drawMapOverlay(self, cr):
    """Draw an overlay on top of the map, showing various information
    about position etc."""
    (x,y,w,h) = self.get('viewport')

    dx = w / 5.0
    dy = dx
    
    self.drawButton(cr, x+dx, y, dx, dy, '', "zoom_out", "mapView:zoomOut")
    self.drawButton(cr, x, y, dx, dy, '', "hint", "set:menu:main")
    self.drawButton(cr, x, y+dy, dx, dy, '', "zoom_in", "mapView:zoomIn")

    m = self.m.get('clickHandler', None)
    if(m != None):
      m.registerDraggable(x,y,x+w,y+h, "mapView")

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

  def resetMenu(self, menu=None):
    print "Menu knows menu changed"
    self.listOffset = 0

  def dragEvent(self,startX,startY,dx,dy,x,y):
    menuName = self.get('menu', None)
    if(menuName == None):
      return
    list = self.lists.get(menuName, None)
    if(list != None):
      self.listOffset += dy
      print "Drag in menu + %f = %f" % (dy,self.listOffset)
    
  def drawMenu(self, cr, menuName):
    """Draw menus"""

    # Find the screen
    if not self.d.has_key('viewport'):
      return
    (x1,y1,w,h) = self.get('viewport', None)

    list = self.lists.get(menuName, None)
    if(list != None):
      m = self.m.get(list, None)
      if(m != None):
        listHelper = listable_menu(cr,x1,y1,w,h, self.m.get('clickHandler', None), self.listOffset)
        m.drawList(cr, menuName, listHelper)
      return
    
    # Find the menu
    menu = self.menus.get(menuName, None)
    if(menu == None):
      if(0):
        print "Menu %s doesn't exist, returning to main screen" % menuName
        self.set('menu', None)
        self.set('needRedraw', True)
      return

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

  def register(self, menu, type, module):
    """Register a menu as being handled by some other module"""
    if(type == 'list'):
      self.lists[menu] = module
    else:
      print "Can't register \"%s\" menu - unknown type" % type
    
  def clearMenu(self, menu, cancelButton='set:menu:main'):
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

  def setupMaplayerMenus(self):
    self.clearMenu('layers')
    m = self.m.get('mapTiles', None)
    if(m):
      layers = m.layers()
      for (name, layer) in layers.items():
        self.addItem(
          'layers',
          layer.get('label',name),
          name,
          'set:layer:'+name+'|set:menu:None')
    
  def setupTransportMenu(self):
    """Create menus for routing modes"""
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
    """Create a load of menus that are just filters for OSM tags"""
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
          self.addItem(sectionID, name, name.lower(), '')
    f.close()

    
  def setupPoiMenu(self):
    self.clearMenu('poi', "set:menu:None")
    self.addItem('poi', 'Route', 'generic', 'route:route')
    #self.addItem('poi', 'Direct', 'generic', 'nav:direct_to')
    #self.addItem('poi', 'Map', 'generic', '')
    #self.addItem('poi', 'Waypoint', 'generic', '')
    
  def setupDataSubMenu(self):
    self.clearMenu('data2', "set:menu:data")
    self.addItem('data2', '5 km', 'generic', 'set:downloadSize:4|mapData:download|set:menu:None')
    self.addItem('data2', '10 km', 'generic', 'set:downloadSize:8|mapData:download|set:menu:None')
    self.addItem('data2', '20 km', 'generic', 'set:downloadSize:16|mapData:download|set:menu:None')
    self.addItem('data2', '40 km', 'generic', 'set:downloadSize:32|mapData:download|set:menu:None')
    self.addItem('data2', '80 km', 'generic', 'set:downloadSize:64|mapData:download|set:menu:None')
    self.addItem('data2', 'Fill disk', 'generic', 'set:downloadSize:0|mapData:download|set:menu:None')
    
  def setupDataMenu(self):
    self.clearMenu('data', "set:menu:Main")
    self.addItem('data', 'Around here', 'generic', 'set:downloadType:data|set:downloadArea:here|set:menu:data2')
    self.addItem('data', 'Around route', 'generic', 'set:downloadType:data|set:downloadArea:route|set:menu:data2')
    self.setupDataSubMenu()

  def setupGeneralMenus(self):
    self.clearMenu('main', "set:menu:None")
    self.addItem('main', 'map', 'generic', 'set:menu:layers')
    self.addItem('main', 'centre', 'centre', 'toggle:centred|set:menu:None')
    self.addItem('main', 'places', 'generic', 'set:menu:places')
    self.addItem('main', 'search', 'business', 'set:menu:search')
    self.addItem('main', 'view', 'view', 'set:menu:view')
    self.addItem('main', 'options', 'options', 'set:menu:options')
    self.addItem('main', 'download', 'generic', 'set:menu:data')
    self.addItem('main', 'mode', 'transport', 'set:menu:transport')
    self.setupTransportMenu()
    self.setupSearchMenus()
    self.setupMaplayerMenus()
    self.setupPoiMenu()
    self.setupDataMenu()
    self.clearMenu('options', "set:menu:main") # will be filled by mod_options
    self.lists['places'] = 'placenames'
  
  def firstTime(self):
    self.set("menu",None)
    
if(__name__ == "__main__"):
  a = menus({},{'viewport':(0,0,600,800)})
  #a.drawMapOverlay(None)
  a.setupSearchMenus()
  