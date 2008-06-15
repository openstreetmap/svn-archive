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

roadColours = [
  #tag=value:'r,g,b,width' with r,g,b between [0,1]
  #a width of 0 means filling an area with a color
  ('highway=motorway','0,0,0,13'),
  ('highway=motorway','0.5,0.5,1,9'),
  ('highway=motorway_link','0,0,0,13'),
  ('highway=motorway_link','0.5,0.5,1,9'),
  ('highway=primary','0,0,0,10'),    # primary casing
  ('highway=primary','1,0.5,0.5,6'), # primary core
  ('highway=secondary','0,0,0,8'),   # secondary casing
  ('highway=secondary','1,0.5,0,4'),
  ('highway=tertiary','0,0,0,6'),    # tertiary casing
  ('highway=tertiary','1,0.5,0,2'),
  ('highway=residential','0,0,0,4'),
  ('highway=residential','0.75,0.75,0.75,2'),
  ('highway=unclassified','0,0,0,4'),
  ('highway=unclassified','0.75,0.75,0.75,2'),
  ('railway=rail','0,0,0,1'),
  ('waterway=river','1,0.2,0.2,4'),
  ('landuse=forest','0.1,0.7,0.1,0'),
  ('natural=wood','0.1,0.7,0.1,0'),
  ('natural=water','0.1,0.1,0.8,0'),
  ]

def wayStyles(tags):
  styles = []
  for (ident, style) in roadColours:
    (tag,value) = ident.split('=')
    if(tags.get(tag,'default') == value):
      styles.append(style)
  if(not styles):
    styles.append('0.8,0.8,0.8,1') # default/debug
  return(styles)  


class RenderClass(OsmRenderBase):
  
  # Specify the background for new tiles
  def imageBackgroundColour(self):
    return(1,0.95,0.85,0.8)  #r,g,b,a background color
  
  # Draw a tile
  def draw(self):
    # Ways
    for w in self.osm.ways.values():
      layeradd = 0
      for style in wayStyles(w['t']):
        (r,g,b, width) = style.split(",")
        width = int(width)
        
        layer = float(w['t'].get('layer',0)) + layeradd
        ctx = self.getCtx(layer)
        layeradd = layeradd + 0.1
        
        ctx.set_source_rgb(float(r),float(g),float(b))
        ctx.set_line_width(width)
        count = 0
        for n in w['n']: 
          # project that into image coordinates
          (x,y) = self.proj.project(n['lat'], n['lon'])

          # draw lines on the image
          if(count == 0):
            ctx.move_to(x, y)
          else:
            ctx.line_to(x, y)
          count = count + 1
        if width > 0:ctx.stroke()
        else: ctx.fill()
    
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
  # a.RenderTile(15, 16372, 10895, filename) # london
  a.RenderTile(15, 16363, 10886, filename) # Multi-layer junction
  
  print "------------------------------------"
  print "Saved image to " + filename
