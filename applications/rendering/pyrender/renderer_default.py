#!/usr/bin/python
#-----------------------------------------------------------------------------
# Basic cairo renderer, that can plot OSM ways as coloured lines
#-----------------------------------------------------------------------------
# Copyright 2008, authors:
# * Oliver White
# * Sebastian Spaeth
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
#----------------------------------------------------------------------------
from render_cairo_base import OsmRenderBase

roadColours = {
  #tag=value:'r,g,b,width' with r,g,b between [0,1]
  'highway=motorway':'0.5,0.5,1,9',
  'highway=primary':'1,0.5,0.5,6',
  'highway=secondary':'1,0.5,0,4',
  'highway=residential':'0.25,0.25,0.25,2',
  'highway=unclassified':'0.25,0.25,0.25,2',
  'railway=rail':'0,0,0,1',
  'waterway=river':'1,0.2,0.2,4',
  }

def wayStyle(tags):
  for (ident, style) in roadColours.items():
    (tag,value) = ident.split('=')
    if(tags.get(tag,'default') == value):
      return(style)
  return(None)  


class RenderClass(OsmRenderBase):
  
  # Specify the background for new tiles
  def imageBackgroundColour(self):
    return(1,0.9,0.8,0.5)  #r,g,b,a background color
  
  # Draw a tile
  def draw(self):
    # Ways
    for w in self.osm.ways:
      style = wayStyle(w['t'])
      
      if(style != None):
        
        (r,g,b, width) = style.split(",")
        self.ctx.set_source_rgb(float(r),float(g),float(b))
        self.ctx.set_line_width(int(width))
        count = 0
        for n in w['n']: 
          # need to lookup that node's lat/long from the osm.nodes dictionary
          (lat,lon) = self.osm.nodes[n]
          
          # project that into image coordinates
          (x,y) = self.proj.project(lat,lon)
          
          # draw lines on the image
          if(count == 0):
            self.ctx.move_to(x, y)
          else:
            self.ctx.line_to(x, y)
          count = count + 1
        self.ctx.stroke()
    
    # POIs
    if(0):
      for poi in self.osm.poi:
        n = poi['id']
        (lat,lon) = self.osm.nodes[n]
        (x,y) = self.proj.project(lat,lon)
        s = 1
        self.drawContext.rectangle((x-s,y-s,x+s,y+s),fill='blue')



#-----------------------------------------------------------------
# Test suite - call this file from the command-line to generate a
# sample image
if(__name__ == '__main__'):
  a = RenderClass()
  filename = "sample_"+__file__+".png"
  a.RenderTile(17,65385,43658, filename)
  print "------------------------------------"
  print "Saved image to " + filename
