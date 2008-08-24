#!/usr/bin/python
#----------------------------------------------------------------------------
# Handle option menus
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

def getModule(m,d):
  return(options(m,d))

class options(ranaModule):
  """Handle options"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.options = {}
    self.scroll = 0
  
  def addBoolOption(self, title, variable, category='misc',default=0):
    self.addOption(title,variable,(('off','off'),('on','on')),category,default)

  def addOption(self, title, variable, choices, category='misc', default=None):
    newOption = (title,variable, choices,category)
    if self.options.has_key(category):
      self.options[category].append(newOption)
    else:
      self.options[category] = [newOption,]

  def firstTime(self):
    """Create a load of options.  You can add your own options in here,
    or alternatively create them at runtime from your module's firstTime()
    function by calling addOption.  That would be best if your module is
    only occasionally used, this function is best if the option is likely
    to be needed in all installations"""
    self.addBoolOption("Centre map", "centre", "view")

    self.addOption("Network", "network",
      [("off","No use of network"),
       ("minimal", "Only for important data"),
       ("full", "Unlimited use of network")],
       "network")

    self.addBoolOption("Logging enabled", "logging", "logging", 'yes')
    options = []
    for i in (1,2,5,10,20,40,60):
      options.append(("%d sec" % i, i))
    self.addOption("Frequency", "log_period", options, "logging")

    self.addBoolOption("Vector maps", "vmap", "map", 'yes')
    self.addOption("Map images", "tiles",
      [("", "None"),
       ("", "")],
      "map", None)
    self.addBoolOption("Old tracklogs", "old_tracklogs", "map", 'no')
    self.addBoolOption("Latest tracklog", "tracklog", "map", 'yes')

    # Add all our categories to the "options" menu
    self.menuModule = self.m.get("menu", None)
    if(self.menuModule):
      for i in self.options.keys():
        self.menuModule.addItem('options', i, "opt_"+i, "set:menu:opt_"+i)

  def handleMessage(self, message):
    if(message == "up"):
      if(self.scroll > 0):
        self.scroll -= 1
    elif(message == "down"):
      self.scroll += 1
    
  def drawMenu(self, cr, menuName):
    """Draw menus"""
    if(menuName[0:4] != "opt_"):
      return
    menuName = menuName[4:]
    if(not self.options.has_key(menuName)):
      return
    
    # Find the screen
    if not self.d.has_key('viewport'):
      return
    (x1,y1,w,h) = self.get('viewport', None)

    dx = w / 3
    dy = h / 4
    
    if(self.menuModule):
      # Top row:
      # * parent menu
      self.menuModule.drawButton(cr, x1, y1, dx, dy, "", "up", "set:menu:options")
      # * scroll up
      self.menuModule.drawButton(cr, x1+dx, y1, dx, dy, "", "up_list", "options:up")
      # * scroll down
      self.menuModule.drawButton(cr, x1+2*dx, y1, dx, dy, "", "down_list", "options:down")

      options = self.options[menuName]

      # One option per row
      for row in (0,1,2):
        index = self.scroll + row
        numItems = len(options)
        if(0 <= index < numItems):
          (title,variable,choices,category) = options[index]
          # What's it set to currently?
          value = self.get(variable, None)

          # Lookup the description of the currently-selected choice.
          # (if any, use str(value) if it doesn't match any defined options)
          # Also lookup the _next_ choice in the list, because that's what
          # we will set the option to if it's clicked
          nextChoice = choices[0]
          valueDescription = str(value)
          useNext = False
          for c in choices:
            (cVal, cName) = c
            if(useNext):
              nextChoice = c
              useNext = False
            if(value == cVal):
              valueDescription = cName
              useNext = True

          # What should happen if this option is clicked -
          # set the associated option to the next value in sequence
          onClick = "set:%s:%s" % (variable, str(nextChoice[0]))
          onClick += "|set:needRedraw:1"

          y = y1 + (row+1) * dy
          
          # Draw background and make clickable
          self.menuModule.drawButton(cr,
            x1,
            y,
            w,
            dy,
            None,
            "3h", # background for a 3x1 icon
            onClick)

          border = 20

          # 1st line: option name
          self.showText(cr, title+":", x1+border, y+border, w-2*border)

          # 2nd line: current value
          self.showText(cr, valueDescription, x1 + 0.15 * w, y + 0.6 * dy, w * 0.85 - border)

          # in corner: row number
          self.showText(cr, "%d/%d" % (index+1, numItems), x1+0.85*w, y+border, w * 0.15 - border, 20)

            
  def showText(self,cr,text,x,y,widthLimit=None,fontsize=40):
    cr.set_font_size(fontsize)
    stats = cr.text_extents(text)
    (textwidth, textheight) = stats[2:4]

    if(widthLimit and textwidth > widthLimit):
      cr.set_font_size(fontsize * widthLimit / textwidth)
      stats = cr.text_extents(text)
      (textwidth, textheight) = stats[2:4]

    cr.move_to(x, y+textheight)
    cr.show_text(text)
  
if(__name__ == "__main__"):
  a = options({},{'viewport':(0,0,600,800)})
  a.firstTime()

  