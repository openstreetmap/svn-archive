#!/usr/bin/python
#-----------------------------------------------------------------------------
# Pyroute main GUI
#
# Usage: 
#   gui.py
#
# Controls:
#   * drag left/right along top of window to zoom in/out
#   * drag the map to move around
#   * click on the map for a position menu
#       * set your own position, if the GPS didn't already do that
#       * set that location as a destination
#       * route to that point
#   * click on the top-left of the window for the main menu
#       * download routing data around your position
#       * browse geoRSS, wikipedia, and OSM points of interest
#       * select your mode of transport for routing
#           * car, bike, foot currently supported
#       * toggle whether the map is centred on your GPS position
#-----------------------------------------------------------------------------
# Copyright 2007, Oliver White
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
#-----------------------------------------------------------------------------
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
from mod_geonames import geonames
from mod_waypoints import waypointsModule
from base import pyrouteModule
from tiles import tileHandler

def update(mapWidget):
  mapWidget.updatePosition();
  return(True)

class MapWidget(gtk.Widget, pyrouteModule):
  __gsignals__ = { \
    'realize': 'override',
    'expose-event' : 'override',
    'size-allocate': 'override',
    'size-request': 'override'}
  def __init__(self):
    gtk.Widget.__init__(self)
    self.draw_gc = None
    self.timer = gobject.timeout_add(30, update, self)
    
    self.modules = {'poi':{}}
    pyrouteModule.__init__(self, self.modules)
    
    #self.modules['poi']['rss'] = geoRss('Setup/feeds.txt')
    #self.modules['poi']['geonames'] = geonames()
    self.modules['poi']['waypoints'] = waypointsModule(self.modules, "data/waypoints.gpx")
    self.modules['overlay'] = guiOverlay(self.modules)
    self.modules['position'] = geoPosition()
    self.modules['tiles'] = tileHandler(self.modules)
    self.modules['data'] = DataStore(self, self.modules)
    self.set('mode','cycle')
    self.set('centred',False)
    self.modules['osmdata'] = LoadOsm(None)
    self.modules['projection'] = Projection()
    self.modules['projection'].recentre(51.2,-0.2, 0.1)
    self.modules['route'] = RouteOrDirect(self.modules['osmdata'])
    self.updatePosition()
    self.set('ownpos', {'valid':False})

    if(0): 
      self.modules['data'].reportModuleConnectivity()
      sys.exit()
    
  def updatePosition(self):
    """Try to get our position from GPS"""
    newpos = self.modules['position'].get()
    
    # If the latest position wasn't valid, then don't touch the old copy
    # (especially since we might have manually set the position)
    if(not newpos['valid']):
      return
    
    # TODO: if we set ownpos and then get a GPS signal, we should decide
    # here what to do
    self.set('ownpos', newpos)
    
    self.handleUpdatedPosition()
    
  def handleUpdatedPosition(self):
    pos = self.get('ownpos')
    if(not pos['valid']):
      return
      
    # If we've never actually decided where to display a map yet, do so now
    if(not self.modules['projection'].isValid()):
      print "Projection not yet valid, centering on ownpos"
      self.centreOnOwnPos()
      return
    
    # This code would let us recentre if we reach the edge of the screen - 
    # it's not currently in use
    x,y = self.modules['projection'].ll2xy(pos['lat'], pos['lon'])
    x,y = self.modules['projection'].relXY(x,y)
    border = 0.15
    outsideMap = (x < border or y < border or x > (1-border) or y > (1-border))
    
    # If map is locked to our position, then recentre it
    if(self.get('centred')):
      self.centreOnOwnPos()
    else:
      self.forceRedraw()
  
  def centreOnOwnPos(self):
    """Try to centre the map on our position"""
    pos = self.get('ownpos')
    if(pos['valid']):
      self.modules['projection'].recentre(pos['lat'], pos['lon'])
      self.forceRedraw()
  
  def click(self, x, y):
    """Handle clicking on the screen"""
    # Give the overlay a chance to handle all clicks first
    if(self.modules['overlay'].handleClick(x,y)):
      pass
    # If the overlay is fullscreen and it didn't respond, the click does
    # not fall-through to the map
    elif(self.modules['overlay'].fullscreen()):
      return
    # Map was clicked-on: store the lat/lon and go into the "clicked" menu
    else:
      lat, lon = self.modules['projection'].xy2ll(x,y)
      self.modules['data'].set('clicked', (lat,lon))
      self.modules['data'].set('menu','click')
    self.forceRedraw()
      
  def forceRedraw(self):
    """Make the window trigger a draw event.  
    TODO: consider replacing this if porting pyroute to another platform"""
    try:
      self.window.invalidate_rect((0,0,self.rect.width,self.rect.height),False)
    except AttributeError:
      pass
    
  def move(self,dx,dy):
    """Handle dragging the map"""
    
    # TODO: what happens when you drag inside menus?
    if(self.get('menu')):
      return
    
    if(self.get('centred') and self.get('ownpos')['valid']):
      return
    self.modules['projection'].nudge(-dx,dy,1.0/self.rect.width)
    self.forceRedraw()
  
  def zoom(self,dx):
    """Handle dragging left/right along top of the screen to zoom"""
    self.modules['projection'].nudgeZoom(-1 * dx / self.rect.width)
    self.forceRedraw()

  def draw(self, cr):
    start = clock()
    proj = self.modules['projection']
    
    # Don't draw the map if a menu/overlay is fullscreen
    if(not self.modules['overlay'].fullscreen()):
      
      # Map as image
      self.modules['tiles'].draw(cr)

      # The route
      if(self.modules['route'].valid()):
        cr.set_source_rgba(0.5, 0.0, 0.0, 0.5)
        cr.set_line_width(12)
        count = 0
        for i in self.modules['route'].route['route']:
          x,y = proj.ll2xy(i[0],i[1])
          if(count == 0):
            cr.move_to(x,y)
          else:
            cr.line_to(x,y)
          count = count + 1
        cr.stroke()
      
      # Each plugin can display on the map
      for name,source in self.modules['poi'].items():
        for group in source.groups:
          for item in group.items:
            x,y = proj.ll2xy(item.lat, item.lon)
            if(proj.onscreen(x,y)):
              #print " - %s at %s" % (item.title, item.point)
              
              cr.set_source_rgb(0.0, 0.4, 0.0)
              cr.arc(x,y,5, 0,2*3.1415)
              cr.fill()
              
              #cr.select_font_face('Verdana', cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
              cr.set_font_size(12)
              cr.move_to(x,y)
              cr.show_text(item.title)

      pos = self.get('ownpos')
      if(pos['valid']):
        # Us
        x,y = proj.ll2xy(pos['lat'], pos['lon'])
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
  def __init__(self):
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
    self.mapWidget = MapWidget()
    event_box.add(self.mapWidget)
    
    # Finalise the window
    win.show_all()
    gtk.main()
    
  def pressed(self, event):
    self.dragstartx = event.x
    self.dragstarty = event.y
    #print dir(event)
    #print "Pressed button %d at %1.0f, %1.0f" % (event.button, event.x, event.y)
    
    self.dragx = event.x
    self.dragy = event.y
  def moved(self, event):
    """Drag-handler"""

    if(self.dragstarty < 100):
      self.mapWidget.zoom(event.x - self.dragx)
    else:
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
  program = GuiBase()
