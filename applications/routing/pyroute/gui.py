#!/usr/bin/env python
import pygtk
pygtk.require('2.0')
import gobject
import gtk
from gtk import gdk
import loadOsm
import cairo

def update(mapWidget):
  return(True)

class Rect:
  def __init__(self,x,y,w,h):
    self.x = x
    self.y = y
    self.w = w
    self.h = h
    
class MapWidget(gtk.Widget):
  __gsignals__ = { \
    'realize': 'override',
    'expose-event' : 'override',
    'size-allocate': 'override',
    'size-request': 'override'}
  def __init__(self):
    gtk.Widget.__init__(self)
    self.draw_gc = None
    self.timer = gobject.timeout_add(200, update, self)
  def move(self,dx,dy):
    pass
  def draw(self, cr):
    cr.set_source_rgb(0,0,0)
    cr.move_to(self.rect.x,self.rect.y)
    cr.line_to(self.rect.x + self.rect.width,self.rect.y + self.rect.height)
    cr.stroke()
    pass
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
    self.draw(cr)
  def do_expose_event(self, event):
    self.chain(event)
    cr = self.window.cairo_create()
    return self._expose_cairo(event, cr)

class GuiBase:
  """Wrapper class for a GUI interface"""
  def __init__(self):
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
    self.mapWidget = MapWidget()
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
  program = GuiBase()
