#!/usr/bin/python
#----------------------------------------------------------------------
# Base class for widgets which use cairo for drawing and have a 
# self.draw(cr) function that gets called regularly
#----------------------------------------------------------------------
# Copyright 2009, Oliver White
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
#----------------------------------------------------------------------
import gtk
import math

class cairo_widget(gtk.Widget):
  __gsignals__ = { \
    'realize': 'override',
    'expose-event' : 'override',
    'size-allocate': 'override',
    'size-request': 'override'
    }
  def __init__(self, d):
    gtk.Widget.__init__(self)
    self.d = d

  def update(self):
    self.forceRedraw()

  def forceRedraw(self):
    try:
      self.window.invalidate_rect((0,0,self.rect.width,self.rect.height),False)
    except AttributeError:
      pass
    
  def draw(self, cr):
    pass # override
  
  def background(self,r=0,g=0,b=0):
    self.cr.set_source_rgb(r,g,b)
    self.cr.rectangle(0,0,self.rect.width,self.rect.height)
    self.cr.fill()

  def do_realize(self):
    self.set_flags(self.flags() | gtk.REALIZED)
    self.window = gtk.gdk.Window( \
      self.get_parent_window(),
      width = self.allocation.width,
      height = self.allocation.height,
      window_type = gtk.gdk.WINDOW_CHILD,
      wclass = gtk.gdk.INPUT_OUTPUT,
      event_mask = self.get_events() | gtk.gdk.EXPOSURE_MASK)
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
    self.cr = cr
    self.draw(cr)
  def do_expose_event(self, event):
    self.chain(event)
    cr = self.window.cairo_create()
    return self._expose_cairo(event, cr)
