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
    self.transport = 'cycle'
    self.data = LoadOsm(osmDataFile, True)
    self.projection = Projection()
    self.projection.recentre(51.524,-0.129, 0.02)
    self.router = Router(self.data)
    result, self.route = self.router.doRoute(107318,107999,self.transport)
    print "Done routing (transport: %s), result: %s" % (self.transport, result)
    self.unknownTags = []
  def move(self,dx,dy):
    self.projection.nudge(-dx,dy,1.0/self.rect.width)
    self.window.invalidate_rect((0,0,self.rect.width,self.rect.height),False)
  def nodeXY(self,node):
    node = self.data.nodes[node]
    return(self.projection.ll2xy(node[0], node[1]))
  def setupDrawStyle(self, cr, wayType):
    styles = { \
      "primary": {'rgb':(0.5, 0.0, 0.0),'w':3},
      "secondary": {'rgb':(0.5, 0.25, 0.0),'w':2},
      "unclassified": {'rgb':(0.8, 0.5, 0.5)},
      "service": {'rgb':(0.5, 0.5, 0.5),'w':0.5},
      "footway": {'rgb':(0.0, 0.5, 0.5)},
      "cycleway": {'rgb':(0.0, 0.5, 0.8)},
      "river": {'rgb':(0.5, 0.5, 1.0),'w':2},
      "rail": {'rgb':(0.3, 0.3, 0.3)}
      }
    
    try:
      style = styles[wayType]
    except KeyError:
      if not wayType in self.unknownTags:
        print "Unknown %s" % wayType
        self.unknownTags.append(wayType)
      return(False)
    
    rgb = style['rgb']
    cr.set_source_rgb(rgb[0], rgb[1], rgb[2])
    
    try:
      width = style['w']
    except KeyError:
      width = 1
    cr.set_line_width(width)
    
    return(True)
    
  def draw(self, cr):
    # The map
    for way in self.data.ways:
      if(self.setupDrawStyle(cr, way['t'])):
        firstTime = True
        for node in way['n']:
          x,y = self.nodeXY(node)
          if firstTime:
            cr.move_to(x,y)
            firstTime = False
          else:
            cr.line_to(x,y)
        cr.stroke()

    # The route
    if(len(self.route) > 1):
      cr.set_source_rgba(1.0, 0.0, 0.0, 0.5)
      cr.set_line_width(8)
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
    """Drag-handler"""
    self.mapWidget.move(event.x - self.dragx, event.y - self.dragy)
    self.dragx = event.x
    self.dragy = event.y
  def moved(self, event):
    pass

if __name__ == "__main__":
  program = GuiBase(sys.argv[1])
