#!/usr/bin/env python
import pygtk
pygtk.require('2.0')
import gobject
import gtk
import sys
from gtk import gdk
from loadOsm import *
from route import *
from tilenames import *
import cairo
import urllib
import os

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
    self.S = lat - scale
    self.N = lat + scale
    self.W = lon - scale
    self.E = lon + scale
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
    #routeStart, routeEnd = (107318,107999) # short
    #routeStart, routeEnd = (21533912, 109833) # across top
    routeStart, routeEnd = (21544863, 108898) # crescent to aldwych
    result, self.route = self.router.doRoute(routeStart, routeEnd, self.transport)
    print "Done routing (transport: %s), result: %s" % (self.transport, result)
    self.unknownTags = []
    self.im = cairo.ImageSurface.create_from_png('image2.png')
    self.images = {}
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
    cr.save()
    cr.translate(bbox[0],bbox[1])
    cr.scale((bbox[2] - bbox[0]) / 256.0, (bbox[3] - bbox[1]) / 256.0)
    cr.set_source_surface(self.images[name],0,0)
    cr.paint()
    cr.restore()
    
  def draw(self, cr):
    # Map as image
    z = 14
    view_x1,view_y1 = latlon2xy(self.projection.N,self.projection.W,z)
    view_x2,view_y2 = latlon2xy(self.projection.S,self.projection.E,z)
    #print "X: %1.3f - %1.3f. y: %1.3f - %1.3f" % (view_x1,view_x2,view_y1,view_y2)
    for x in range(int(floor(view_x1)), int(ceil(view_x2))):
      for y in range(int(floor(view_y1)), int(ceil(view_y2))):
        S,W,N,E = tileEdges(x,y,z) 
        x1,y1 = self.projection.ll2xy(N,W)
        x2,y2 = self.projection.ll2xy(S,E)
        self.loadImage(x,y,z)
        self.drawImage(cr,(x,y,z),(x1,y1,x2,y2))
    
    # The map
    if 0:
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
      cr.set_source_rgba(0.0, 1.0, 0.0, 0.5)
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
