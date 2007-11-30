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

class lib_gpx:
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
      
  def saveAs(self,filename):
		file = open(filename,"w")
		file.write("<?xml version=\"1.0\"?>\n")
		file.write('<gpx>\n')
		file.write('<trk>\n')
		file.write('<name>Sketch</name>\n')
		for l in self.lines:
			file.write('<trkseg>\n')
			for p in l:
				file.write('<trkpt lat="%f" lon="%f"/>\n'%(p[0], p[1]))
			file.write('</trkseg>\n')
		file.write('</trk>\n')
		file.write('</gpx>\n')

		file.close()
    