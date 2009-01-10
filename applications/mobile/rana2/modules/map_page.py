#!/usr/bin/python
#----------------------------------------------------------------------
# GTK layout for the map view
#----------------------------------------------------------------------
# Copyright 2008-2009, Oliver White
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
from _base import *
from _map import *
from _menu import page_button
import gtk

class map_page(RanaModule):
  """Displays the main map view"""
  def __init__(self, m, d, name):
    RanaModule.__init__(self,m,d,name)
    
  def startup(self):
    self.registerPage('map')

  def page_map(self):
    zoom_out = gtk.Button("-", gtk.STOCK_ZOOM_OUT)
    status_bar = page_button(self, None, "menu")
    zoom_in = gtk.Button("+", gtk.STOCK_ZOOM_IN)
    self.status = gtk.Label("text")
    status_bar.add(self.status)

    toolbar = gtk.HBox(False)
    toolbar.pack_start(zoom_out, False, True, 0)
    toolbar.pack_start(status_bar, True, True, 0)
    toolbar.pack_start(zoom_in, False, True, 0)

    map_box = gtk.EventBox()
    if(0):
      map_box.connect("button_press_event", lambda w,e: self.pressed(e))
      map_box.connect("button_release_event", lambda w,e: self.released(e))
      map_box.connect("motion_notify_event", lambda w,e: self.moved(e))
    map_widget = MapWidget(self.d)
    map_box.add(map_widget)

    layout = gtk.VBox(False)
    layout.pack_start(map_box, True, True, 0)
    layout.pack_start(toolbar, False, True, 0)
    return(layout)

  def update(self):
    self.status.set_text("hello world")
