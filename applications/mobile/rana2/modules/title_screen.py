#!/usr/bin/python
#----------------------------------------------------------------------
# Title screen for Rana
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
from _menu import *
import gtk
import pango # fonts

class title_screen(RanaModule):
  """Displays the title screen"""
  def __init__(self, m, d, name):
    RanaModule.__init__(self,m,d,name)
    
  def startup(self):
    self.registerPage('title')
    self.gpsStatusLabel = None

  def label(self, text, size=None):
    bigTitles = False
    widget = gtk.Label(text)
    widget.set_justify(gtk.JUSTIFY_CENTER)
    if(size and bigTitles):
      widget.modify_font(pango.FontDescription('Sans %d' % size))
    widget.set_line_wrap(True)
    return(widget)

  def page_title(self):
    box = gtk.VBox()

    box.add(self.label("Rana\nExplore your world", 20))

    box.add(self.label("This program is Free Software.\nGNU GPL v3 or later\nNo warranty"))

    box.add(self.label("All maps are Free Geodata.\nwww.openstreetmap.org\nJoin in the project and help to map your locality\nCreative Commons CC-BY-SA 2.0"))

    self.gpsStatusLabel = self.label("Testing GPS...")
    box.add(self.gpsStatusLabel)
    
    box.add(page_button(self, "Show map", "map"))
    return(box)

  def gpsStatus(self):
    if(not self.get("pos_valid", False)):
      return("No GPS connected")
    pos = self.get("pos", None)
    if(not pos):
      return("Error in stored position")
    (lat,lon,alt) = pos
    return("GPS status: OK, %1.5f, %1.5f" % (lat,lon))

  def update(self):
    self.gpsStatusLabel.set_text(self.gpsStatus())

