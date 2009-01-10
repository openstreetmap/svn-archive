#!/usr/bin/python
#----------------------------------------------------------------------
# Allow using UK postcodes (freethepostcode.org) in the map
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
try:
  import urllib
  urllib_available = True
except ImportError:
  urllib_available = False
import math

def distance(lat1,lon1,lat2,lon2,square=False):
  dlat = lat2 - lat1
  dlon = lon2 - lon1
  dlon *= math.cos(lat1)
  sq = dlat*dlat+dlon*dlon
  if(square):
    return(sq)
  return(math.sqrt(sq))

class postcodes(RanaModule):
  """Displays the title screen"""
  def __init__(self, m, d, name):
    RanaModule.__init__(self,m,d,name)
    self.postcodes = []
    
  def startup(self):
    self.registerPage('postcodes')
    self.load_postcodes()
  
  def filename(self):
    return("data/postcodes.txt")
  
  def load_postcodes(self):
    f = open(self.filename(), "r")
    count = 0
    for line in f:
      count += 1
      if(line[0] == '#'):
        continue
      (lat,lon,major,minor) = line.split()
      self.postcodes.append((float(lat),float(lon),major,minor))
    f.close()
    self.log("Loaded %d postcodes from %d lines" % (len(self.postcodes), count))
  
  
  def postcode_at(self, lat1, lon1):
    print "Searching %f,%f" % (lat1,lon1)
    min_dist = None
    result = None
    for p in self.postcodes:
      (lat,lon,major,minor) = p
      d = distance(lat,lon,lat1,lon1,True)
      if(min_dist == None or d < min_dist):
        result = major
        min_dist = d
    print "found %s" % str(result)
    return(result)
      
  def status(self, blocks, blocksize, filesize):
    percent = 100.0 * float(blocks*blocksize) / float(filesize)
    self.download_btn.set_label("%1.0f%%" % percent)
  
  def download(self, widget, button):
    self.download_btn.set_label("Connecting...")
    url = self.get("postcode_url", "http://www.freethepostcode.org/currentlist")
    urllib.urlretrieve(url, self.filename(), self.status)
    self.download_btn.set_label("Done")
    self.load_postcodes()
  
  def update(self):
    if(self.nearest_btn and self.status_btn):
      if(self.get("pos_valid", False)):
        (lat,lon,alt) = self.get("pos")
        postcode = self.postcode_at(lat,lon)
        if(postcode != None):
          print "Setting %s" % postcode
          self.nearest_btn.set_text("In " + postcode)
          self.status_btn.set_label("OK")
        else:
          self.nearest_btn.set_text("Unknown postcode")
          self.status_btn.set_label("Not found")

  def page_postcodes(self):
    self.status_btn = gtk.Label("Searching...")
    self.nearest_btn = gtk.Label("")

    self.download_btn = gtk.Button(None)
    if(urllib_available):
      self.download_btn.set_label("Download")
      self.download_btn.connect("clicked", self.download, self.download_btn)
      buttons.append(self.download_btn)
    return(create_menu(self, 'menu', [self.status_btn, self.download_btn,self.nearest_btn]))
  
  