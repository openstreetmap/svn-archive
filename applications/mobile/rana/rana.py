#!/usr/bin/python
#----------------------------------------------------------------------------
# Rana main GUI.  Displays maps, for use on a mobile device
#
# Controls:
#   * click on the overlay text to change fields displayed
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
#----------------------------------------------------------------------------
import pygtk
pygtk.require('2.0')
import gobject
import gtk
import sys
import cairo
import os
from math import sqrt
from time import clock
from gtk import gdk


def update1(mapWidget):
  mapWidget.update()
  return(True)

def update2(mapWidget):
  mapWidget.checkForRedraw()
  return(True)

class MapWidget(gtk.Widget):
  __gsignals__ = { \
    'realize': 'override',
    'expose-event' : 'override',
    'size-allocate': 'override',
    'size-request': 'override'
    }
  def __init__(self):
    gtk.Widget.__init__(self)
    self.draw_gc = None
    self.timer1 = gobject.timeout_add(100, update1, self)
    self.timer2 = gobject.timeout_add(10, update2, self)
    self.d = {} # List of data
    self.m = {} # List of modules
    self.loadModules('modules')
    
  def loadModules(self, module_path):
    """Load all modules from the specified directory"""
    sys.path.append(module_path)
    print "importing modules:"
    for f in os.listdir(module_path):
      if(f[0:4] == 'mod_' and f[-3:] == '.py'):
        name = f[4:-3]
        filename = "%s\\%s" % ( module_path, f[0:-3]) # with directory name, but without .py extension
        a = __import__(f[0:-3])
        self.m[name] = a.getModule(self.m,self.d)
        self.m[name].moduleName = name
        print " * %s: %s" % (name, self.m[name].__doc__)

    print "Loaded all modules, initialising"
    for m in self.m.values():
      m.firstTime()
      
  def beforeDie(self):
    print "Shutting-down modules"
    for m in self.m.values():
      m.shutdown()
  
  def update(self):
    for m in self.m.values():
      m.update()
    self.checkForRedraw()
  
  def checkForRedraw(self):
    if(self.d.get("needRedraw", False)):
      self.forceRedraw()

  def mousedown(self,x,y):
    pass
    
  def click(self, x, y):
    m = self.m.get("clickHandler",None)
    if(m != None):
      m.handleClick(x,y)
      self.update()
      
  def handleDrag(self,x,y,dx,dy,startX,startY):
    m = self.m.get("clickHandler",None)
    if(m != None):
      m.handleDrag(startX,startY,dx,dy,x,y)
      self.update()
        
  def forceRedraw(self):
    """Make the window trigger a draw event.  
    TODO: consider replacing this if porting pyroute to another platform"""
    self.d['needRedraw'] = False
    try:
      self.window.invalidate_rect((0,0,self.rect.width,self.rect.height),False)
    except AttributeError:
      pass
    
  def draw(self, cr):
    start = clock()
    for m in self.m.values():
      m.beforeDraw()

    menuName = self.d.get('menu', None)
    if(menuName != None):
      for m in self.m.values():
        m.drawMenu(cr, menuName)
    else:
      # Draw the base map, the map overlays, and the screen overlays
      for m in self.m.values():
        m.drawMap(cr)
      for m in self.m.values():
        m.drawMapOverlay(cr)
      for m in self.m.values():
        m.drawScreenOverlay(cr)

    #print "Redraw took %1.2f ms" % (1000 * (clock() - start))
    
  def do_realize(self):
    self.set_flags(self.flags() | gtk.REALIZED)
    self.window = gdk.Window( \
      self.get_parent_window(),
      width = self.allocation.width,
      height = self.allocation.height,
      window_type = gdk.WINDOW_CHILD,
      wclass = gdk.INPUT_OUTPUT,
      event_mask = self.get_events() | gdk.EXPOSURE_MASK)
    self.window.set_user_data(self)
    self.style.attach(self.window)
    self.style.set_background(self.window, gtk.STATE_NORMAL)
    self.window.move_resize(*self.allocation)
  def do_size_request(self, allocation):
    pass
  def do_size_allocate(self, allocation):
    self.allocation = allocation
    if self.flags() & gtk.REALIZED:
      self.window.move_resize(*allocation)
  def _expose_cairo(self, event, cr):
    self.rect = self.allocation
    self.d['viewport'] = (self.rect.x, self.rect.y, self.rect.width, self.rect.height)
    #self.modules['projection'].setView( \
    #  self.rect.x, 
    #  self.rect.y, 
    #  self.rect.width, 
    #  self.rect.height)
    
    if(1): # optional set clipping in cairo
      cr.rectangle(
        self.rect.x,
        self.rect.y,
        self.rect.width,
        self.rect.height)
      cr.clip()
    
    self.draw(cr)
  def do_expose_event(self, event):
    self.chain(event)
    cr = self.window.cairo_create()
    return self._expose_cairo(event, cr)

class GuiBase:
  """Wrapper class for a GUI interface"""
  def __init__(self, device):
    # Create the window
    win = gtk.Window()
    win.set_title('Rana')
    win.connect('delete-event', gtk.main_quit)

    if(device == 'eee'): # test for use with asus eee
      win.resize(800,600)
      win.move(gtk.gdk.screen_width() - 900, 50)
    elif(device == 'n95'): # test for use with nokia 95
      win.resize(480,640)
      win.move(gtk.gdk.screen_width() - 500, 50)
    else: # test for use with neo freerunner
      win.resize(480,640)
      win.move(gtk.gdk.screen_width() - 500, 50)
    
    # Events
    event_box = gtk.EventBox()
    event_box.connect("button_press_event", lambda w,e: self.pressed(e))
    event_box.connect("button_release_event", lambda w,e: self.released(e))
    event_box.connect("motion_notify_event", lambda w,e: self.moved(e))
    win.add(event_box)
    
    # Create the map
    self.mapWidget = MapWidget()
    event_box.add(self.mapWidget)
    
    # Finalise the window
    win.show_all()
    gtk.main()
    self.mapWidget.beforeDie()
    
  def pressed(self, event):
    self.dragstartx = event.x
    self.dragstarty = event.y
    #print dir(event)
    #print "Pressed button %d at %1.0f, %1.0f" % (event.button, event.x, event.y)
    
    self.dragx = event.x
    self.dragy = event.y
    self.mapWidget.mousedown(event.x,event.y)
    
  def moved(self, event):
    """Drag-handler"""

    self.mapWidget.handleDrag( \
      event.x,
      event.y,
      event.x - self.dragx, 
      event.y - self.dragy,
      self.dragstartx,
      self.dragstarty)

    self.dragx = event.x
    self.dragy = event.y
  def released(self, event):
    dx = event.x - self.dragstartx
    dy = event.y - self.dragstarty
    distSq = dx * dx + dy * dy
    # Adjust this to the length^2 of a gerfingerpoken on your touchscreen (1024 is for Freerunner, since it's very high resolution)
    if distSq < 1024:
      self.mapWidget.click(event.x, event.y)


if __name__ == "__main__":
  program = GuiBase('neo')

 