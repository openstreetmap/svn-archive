#!/usr/bin/python
#-----------------------------------------------------------------------------
# 
#
# Usage: 
# 
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
from base import pyrouteModule
import os
import cairo
from lib_gpx import lib_gpx
from xml.sax import make_parser, handler

class tracklog(pyrouteModule, lib_gpx, handler.ContentHandler):
  """  """
  def __init__(self, modules):
    lib_gpx.__init__(self)
    pyrouteModule.__init__(self, modules)
    

  def load(self, filename):
    if(not os.path.exists(filename)):
      print "No such tracklog \"%s\"" % filename
      return
    self.inField = False
    parser = make_parser()
    parser.setContentHandler(self)
    parser.parse(filename)
  
  def startElement(self, name, attrs):
    if name == 'trk':
      pass
    if name == 'trkseg':
      self.latest = []
      self.lines.append(self.latest)
      pass
    if name == "trkpt":
			lat = float(attrs.get('lat'))
			lon = float(attrs.get('lon'))
			self.latest.append([lat,lon])
