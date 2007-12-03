#!/usr/bin/python
#-----------------------------------------------------------------------------
# Library for handling GPX files
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
import os
import cairo
from xml.sax import make_parser, handler

class lib_gpx(handler.ContentHandler):
  """  """
  def __init__(self):
    self.lines = []

  def draw(self,cr,proj):
    cr.set_line_cap(cairo.LINE_CAP_ROUND)
    cr.set_source_rgb(0.7,0.7,0)
    cr.set_line_width(5)

    for l in self.lines:
      first = 1
      for p in l:
        x,y = proj.ll2xy(p[0], p[1])
        if(first):
          cr.move_to(x,y)
          first = 0
        else:
  	      cr.line_to(x,y)
      cr.stroke()
      
  def saveAs(self,filename, title="untitled"):
		file = open(filename,"w")
		file.write("<?xml version=\"1.0\"?>\n")
		file.write('<gpx>\n')
		file.write('<trk>\n')
		file.write('<name>%s</name>\n' % title)
		for l in self.lines:
			file.write('<trkseg>\n')
			for p in l:
				file.write('<trkpt lat="%f" lon="%f"/>\n'%(p[0], p[1]))
			file.write('</trkseg>\n')
		file.write('</trk>\n')
		file.write('</gpx>\n')

		file.close()

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
