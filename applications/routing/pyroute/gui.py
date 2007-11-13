#!/usr/bin/env python
import pygtk
pygtk.require('2.0')
import gobject
import gtk
import sys
import cairo
import urllib
import os
from math import sqrt
from time import clock
from gtk import gdk

# Our modules:
from loadOsm import *
from routeOrDirect import *
from tilenames import *
from geoPosition import *
from projection import Projection
from overlay import *
from dataStore import *
from mod_geoRss import geoRss

def update(mapWidget):
  mapWidget.updatePosition();
  return(True)

class MapWidget(gtk.Widget):
  __gsignals__ = { \
    'realize': 'override',
    'expose-event' : 'override',
    'size-allocate': 'override',
    'size-request': 'override'}
  def __init__(self, osmDataFile, positionFile):
    gtk.Widget.__init__(self)
    self.draw_gc = None
    self.timer = gobject.timeout_add(30, update, self)
    self.images = {}
    
    self.modules = {'plugins':{}}
    self.modules['plugins']['rss'] = geoRss('Setup/feeds.txt')
    self.modules['overlay'] = guiOverlay(self.modules)
    self.modules['position'] = geoPosition(positionFile)
    self.modules['data'] = DataStore()
    self.modules['data'].setState('mode','cycle')
    self.modules['osmdata'] = LoadOsm(osmDataFile, True)
    self.modules['projection'] = Projection()
    self.modules['route'] = RouteOrDirect(self.modules['osmdata'])
    self.updatePosition()
    for name,mod in self.modules['plugins'].items():
      mod.callbacks(self.modules)
  def updatePosition(self):
    self.ownpos = self.modules['position'].get()
    if(not self.modules['projection'].isValid()):
      print "Projection not yet valid, centering on ownpos"
      self.centreOnOwnPos()
      return
    x,y = self.modules['projection'].ll2xy(self.ownpos[0], self.ownpos[1])
    x,y = self.modules['projection'].relXY(x,y)
    border = 0.15
    followMode = self.modules['data'].getOption("centred")
    outsideMap = (x < border or y < border or x > (1-border) or y > (1-border))
    if(followMode):
      self.centreOnOwnPos()
    else:
      self.forceRedraw()
  def centreOnOwnPos(self):
    self.modules['projection'].recentre(self.ownpos[0],self.ownpos[1], 0.01)
    self.forceRedraw()
    
    
  def click(self, x, y):
    if(self.modules['overlay'].handleClick(x,y)):
      return
    transport = self.modules['data'].getState('mode')
    self.modules['route'].setStartLL(self.ownpos[0],self.ownpos[1], transport)

    lat, lon = self.modules['projection'].xy2ll(x,y)
    self.modules['route'].setEndLL(lat,lon,transport)
    self.modules['route'].update()
    self.forceRedraw()
      
  def forceRedraw(self):
    try:
      self.window.invalidate_rect((0,0,self.rect.width,self.rect.height),False)
    except AttributeError:
      pass
  def move(self,dx,dy):
    self.modules['projection'].nudge(-dx,dy,1.0/self.rect.width)
    self.forceRedraw()
  def nodeXY(self,node):
    node = self.modules['osmdata'].nodes[node]
    return(self.modules['projection'].ll2xy(node[0], node[1]))

  def imageName(self,x,y,z):
    return("%d_%d_%d" % (z,x,y))
  def loadImage(self,x,y,z):
    name = self.imageName(x,y,z)
    if name in self.images.keys():
      return
    filename = "cache/%s.png" % name
    if not os.path.exists(filename):
      url = tileURL(x,y,z)
      urllib.urlretrieve(url, filename)
    self.images[name]  = cairo.ImageSurface.create_from_png(filename)
    
  def drawImage(self,cr, tile, bbox):
    name = self.imageName(tile[0],tile[1],tile[2])
    if not name in self.images.keys():
      return
    cr.save()
    cr.translate(bbox[0],bbox[1])
    cr.scale((bbox[2] - bbox[0]) / 256.0, (bbox[3] - bbox[1]) / 256.0)
    cr.set_source_surface(self.images[name],0,0)
    cr.paint()
    cr.restore()
    
  def draw(self, cr):
    start = clock()
    # Map as image
    if(not self.modules['overlay'].fullscreen()):
      z = 14
      view_x1,view_y1 = latlon2xy(self.modules['projection'].N,self.modules['projection'].W,z)
      view_x2,view_y2 = latlon2xy(self.modules['projection'].S,self.modules['projection'].E,z)
      for x in range(int(floor(view_x1)), int(ceil(view_x2))):
        for y in range(int(floor(view_y1)), int(ceil(view_y2))):
          S,W,N,E = tileEdges(x,y,z) 
          x1,y1 = self.modules['projection'].ll2xy(N,W)
          x2,y2 = self.modules['projection'].ll2xy(S,E)
          self.loadImage(x,y,z)
          self.drawImage(cr,(x,y,z),(x1,y1,x2,y2))

      
      # The route
      if(self.modules['route'].valid()):
        cr.set_source_rgba(0.5, 0.0, 0.0, 0.5)
        cr.set_line_width(12)
        count = 0
        for i in self.modules['route'].route['route']:
          x,y = self.modules['projection'].ll2xy(i[0],i[1])
          if(count == 0):
            cr.move_to(x,y)
          else:
            cr.line_to(x,y)
          count = count + 1
        cr.stroke()

      for name,source in self.modules['plugins'].items():
        for group in source.groups:
          for item in group.items:
            x,y = self.modules['projection'].ll2xy(item.lat, item.lon)
            if(self.modules['projection'].onscreen(x,y)):
              #print " - %s at %s" % (item.title, item.point)
              
              cr.set_source_rgb(0.0, 0.4, 0.0)
              cr.arc(x,y,5, 0,2*3.1415)
              cr.fill()
              
              #cr.select_font_face('Verdana', cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
              cr.set_font_size(12)
              cr.move_to(x,y)
              cr.show_text(item.title)

    
      # Us
      x,y = self.modules['projection'].ll2xy(self.ownpos[0],self.ownpos[1])
      cr.set_source_rgb(0.0, 0.0, 0.0)
      cr.arc(x,y,14, 0,2*3.1415)
      cr.fill()
      cr.set_source_rgb(1.0, 0.0, 0.0)
      cr.arc(x,y,10, 0,2*3.1415)
      cr.fill()
      
    # Overlay (menus etc)
    self.modules['overlay'].draw(cr, self.rect)
    
    end = clock()
    delay = end - start 
    #print "%1.1f ms" % (delay * 100)
    
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
    self.modules['projection'].setView( \
      self.rect.x, 
      self.rect.y, 
      self.rect.width, 
      self.rect.height)
    self.draw(cr)
  def do_expose_event(self, event):
    self.chain(event)
    cr = self.window.cairo_create()
    return self._expose_cairo(event, cr)

class GuiBase:
  """Wrapper class for a GUI interface"""
  def __init__(self, osmDataFile, positionFile):
    # Create the window
    win = gtk.Window()
    win.set_title('pyroute')
    win.connect('delete-event', gtk.main_quit)
    win.resize(430,600)
    win.move(50, gtk.gdk.screen_height() - 650)
    
    # Events
    event_box = gtk.EventBox()
    event_box.connect("button_press_event", lambda w,e: self.pressed(e))
    event_box.connect("button_release_event", lambda w,e: self.released(e))
    event_box.connect("motion_notify_event", lambda w,e: self.moved(e))
    win.add(event_box)
    
    # Create the map
    self.mapWidget = MapWidget(osmDataFile, positionFile)
    event_box.add(self.mapWidget)
    
    # Finalise the window
    win.show_all()
    gtk.main()
    
  def pressed(self, event):
    self.dragstartx = event.x
    self.dragstarty = event.y
    #print dir(event)
    #print "Pressed button %d" % event.button
    self.dragx = event.x
    self.dragy = event.y
  def moved(self, event):
    """Drag-handler"""
    self.mapWidget.move(event.x - self.dragx, event.y - self.dragy)
    self.dragx = event.x
    self.dragy = event.y
  def released(self, event):
    dx = event.x - self.dragstartx
    dy = event.y - self.dragstarty
    distSq = dx * dx + dy * dy
    if distSq < 4:
      self.mapWidget.click(event.x, event.y)


if __name__ == "__main__":
  program = GuiBase(sys.argv[1], sys.argv[2])
