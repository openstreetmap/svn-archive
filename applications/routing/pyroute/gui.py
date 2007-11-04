#!/usr/bin/env python
import pygtk
pygtk.require('2.0')
import gobject
import gtk
import sys
from gtk import gdk
from loadOsm import *
from route import *
import cairo

def update(mapWidget):
  return(True)

class Projection:
  def __init__(self):
    pass
  def setView(self,x,y,w,h):
    self.w = w / 2
    self.h = h / 2
    self.xc = x + self.w
    self.yc = y + self.h
  def recentre(self,lat,lon,scale):
    self.lat = lat
    self.lon = lon
    self.scale = scale  # TODO: scale and scaleCosLat
  def nudge(self,dx,dy,scale):
    self.lat = self.lat + dy * scale * self.scale
    self.lon = self.lon + dx * scale * self.scale
  def ll2xy(self,lat,lon):
    px = (lon - self.lon) / self.scale
    py = (lat - self.lat) / self.scale
    x = self.xc + self.w * px
    y = self.yc - self.h * py
    return(x,y)
  def xy2ll(self,x,y):
    pass
    
class MapWidget(gtk.Widget):
  __gsignals__ = { \
    'realize': 'override',
    'expose-event' : 'override',
    'size-allocate': 'override',
    'size-request': 'override'}
  def __init__(self, osmDataFile):
    gtk.Widget.__init__(self)
    self.draw_gc = None
    self.timer = gobject.timeout_add(200, update, self)
    self.data = LoadOsm(osmDataFile)
    self.projection = Projection()
    self.projection.recentre(51.524,-0.129, 0.02)
    self.router = Router(self.data)
    result, self.route = self.router.doRoute(107318,107999)
    print "Done routing, result: %s" % result
    
  def move(self,dx,dy):
    self.projection.nudge(-dx,dy,1.0/self.rect.width)
    self.window.invalidate_rect((0,0,self.rect.width,self.rect.height),False)
  def nodeXY(self,node):
    node = self.data.nodes[node]
    return(self.projection.ll2xy(node[0], node[1]))
  def draw(self, cr):
    # Just nodes:
    cr.set_source_rgb(0.0, 0.0, 0.0)
    cr.set_line_cap(cairo.LINE_CAP_ROUND)
    for k,v in self.data.nodes.items():
      x,y = self.projection.ll2xy(v[0],v[1])
      cr.move_to(x,y)
      cr.line_to(x,y)
      cr.stroke()
    
    if(len(self.route) > 1):
      x,y = self.nodeXY(self.route[0])
      cr.move_to(x,y)
      for i in self.route:
        x,y = self.nodeXY(i)
        cr.line_to(x,y)
      cr.stroke()
      
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
    self.projection.setView( \
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
  def __init__(self, osmDataFile):
    # Create the window
    win = gtk.Window()
    win.set_title('map')
    win.connect('delete-event', gtk.main_quit)
    win.resize(600,800)
    win.move(50, gtk.gdk.screen_height() - 850)
    
    # Events
    event_box = gtk.EventBox()
    event_box.connect("button_press_event", lambda w,e: self.pressed(e))
    event_box.connect("button_release_event", lambda w,e: self.released(e))
    event_box.connect("motion_notify_event", lambda w,e: self.moved(e))
    win.add(event_box)
    
    # Create the map
    self.mapWidget = MapWidget(osmDataFile)
    event_box.add(self.mapWidget)
    
    # Finalise the window
    win.show_all()
    gtk.main()
    
  def pressed(self, event):
    self.dragx = event.x
    self.dragy = event.y
  def released(self, event):
    pass
  def moved(self, event):
    """Drag-handler"""
    self.mapWidget.move(event.x - self.dragx, event.y - self.dragy)
    self.dragx = event.x
    self.dragy = event.y

if __name__ == "__main__":
  program = GuiBase(sys.argv[1])
