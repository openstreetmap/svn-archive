#!/usr/bin/python
#----------------------------------------------------------------------
# Display GPS receiver status
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

NUM_GPS_CHARTS = 12

class gps_status(RanaModule):
  """Displays the title screen"""
  def __init__(self, m, d, name):
    RanaModule.__init__(self,m,d,name)
  def startup(self):
    self.registerPage('gps')
    self.status_widgets = {}

  def update(self):
    for i in range(NUM_GPS_CHARTS):
      if(i < self.get("sat_count",0)):
        (label1,bar,label2) = self.status_widgets[i]
        (id, used, el, az, snr) = self.get("sat_%d" % i)
        label1.set_text(used and "OK" or "")
        label2.set_text("%02u" % int(id))
        bar.set_fraction(snr / 100.0)
      else:
        label1.set_text("")
        label2.set_text("")
        bar.set_fraction(0)
    q = self.get("sat_quality", None)
    if(q):
      (pdop, hdop, vdop) = q
      #text = "P = %s\nD = %s\nV=%s" % [self.evaluate_dop(a) for a in q]
      text = self.evaluate_dop(pdop)
      self.status.set_text(text)
  
  def evaluate_dop(self, dop):
    if(dop < 1): 
      return("Excellent signal")
    if(dop < 3): 
      return("Very good signal")
    if(dop < 6):
      return("Good signal")
    if(dop < 8):
      return("Moderate signal")
    if(dop < 20):
      return("Low-quality signal")
    else:
      return("Very poor signal")

  def page_gps(self):
    table = gtk.Table(rows=4, columns=3, homogeneous=True)
  
    table.attach(parent_button(self, "modes"), 0,1, 0,1, gtk.FILL|gtk.EXPAND, gtk.FILL|gtk.EXPAND)
    
    self.status = gtk.Label("Getting status...")
    table.attach(self.status, 1,3, 0,1, gtk.FILL|gtk.EXPAND, gtk.FILL|gtk.EXPAND)
    
    box = gtk.HBox(True)
    for i in range(NUM_GPS_CHARTS):
      vbox = gtk.VBox(False)
      bar = gtk.ProgressBar(adjustment=None)
      bar.set_fraction(0)
      bar.set_orientation(gtk.PROGRESS_BOTTOM_TO_TOP)
      label1 = gtk.Label("")
      label2 = gtk.Label("")
      vbox.pack_start(label1, False)
      vbox.pack_start(bar, True, True, 3)
      vbox.pack_start(label2, False)
      box.pack_start(vbox, True, True, 3)
      self.status_widgets[i] = (label1, bar, label2)
      
    table.attach(box, 0,3, 1,4, gtk.FILL|gtk.EXPAND, gtk.FILL|gtk.EXPAND)
    
    return(table)
