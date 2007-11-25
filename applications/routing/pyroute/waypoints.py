#!/usr/bin/python
#----------------------------------------------------------------
# Waypoint handler for pyroute
#
#------------------------------------------------------
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
#------------------------------------------------------
import sys
import os
from xml.sax import make_parser, handler
from base import pyrouteModule

class Waypoints(handler.ContentHandler, pyrouteModule):
  def __init__(self, modules):
    pyrouteModule.__init__(self, modules)
    self.waypoints = []
    
  def load(self, filename):
    parser = make_parser()
    parser.setContentHandler(self)
    parser.parse(filename)
  
  def startElement(self, name, attrs):
    if name == "wpt":
      waypoint = { \
        'lat': float(attrs.get('lat')),
        'lon': float(attrs.get('lon'))
        }
      self.waypoints.append(waypoint)
  
  def endElement(self, name):
    pass
  def report(self):
    for w in self.waypoints:
      print "%1.3f, %1.3f" % (w['lat'], w['lon'])
    
    
# Parse the supplied OSM file
if __name__ == "__main__":
  wpt = Waypoints(None)
  wpt.load("data/waypoints.gpx")
  wpt.report()
