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
  ('highway=trunk','0,0,0,10'),    # primary casing
  ('highway=trunk','1,0.5,0.5,6'), # primary core
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

waterColours = [
  ('natural=water','0.1,0.1,0.8,0'),
  ('waterway=river','0.1,0.1,0.8,8'),
  ('waterway=riverbank','0.1,0.1,0.8,3'),
  ('waterway=canal','0.1,0.1,0.8,6'),
  ('waterway=aqueduct','0.1,0.1,0.8,6'),
  ('waterway=drain','0.1,0.1,0.8,2'),
  ('waterway=ditch','0.1,0.1,0.8,2'),
  ('natural=coastline','0.1,0.1,0.8,6'),
  ]

powerColours = [
  ('power=line','0,0,0,8'),
  ('power=sub_station','0,0,0,0'),
  ('power=substation','1,0,0,0'), # debug colour to show misspelling!
  ]

buildingColours = [
  ('building=*','0,0,0,0'),
  ]
  
def HTMLColorToRGB(colorstring):
    """ convert #RRGGBB to an (R, G, B) tuple """
    colorstring = colorstring.strip()
    if colorstring[0] == '#': colorstring = colorstring[1:]
    if len(colorstring) != 6:
        raise ValueError, "input #%s is not in #RRGGBB format" % colorstring
    r, g, b = colorstring[:2], colorstring[2:4], colorstring[4:]
    r, g, b = [int(n, 16) for n in (r, g, b)]
    r, g, b = [float(n) / 256.0 for n in (r, g, b)]
    return (r, g, b)


def convert(colorstring):
  (r,g,b) = HTMLColorToRGB(colorstring)
  text = "%1.2f,%1.2f,%1.2f,%d" % (r,g,b,6)
  return(text)
  
metroColours = [
  ('line=Bakerloo', convert('996633')),
  ('line=Central', convert('CC3333')),
  ('line=Circle', convert('FFCC00')),
  ('line=District', convert('006633')),
  ('line=EastLondon', convert('FF9900')),
  ('line=HammersmithAndCity', convert('CC9999')),
  ('line=Jubilee', convert('868F98')),
  ('line=Metropolitan', convert('660066')),
  ('line=Northern', convert('000000')),
  ('line=Piccadilly', convert('000099')),
  ('line=Victoria', convert('0099CC')),
  ('line=WaterlooAndCity', convert('66CCCC')),
  ('line=Tramlink', convert('808080')),
  ('line=DLR', convert('8080FF')),
  ('railway=rail', '0,0,0,1'),
  ('railway=station', '0,0,0,0'),
  ]

cycleColours = [
  #tag=value:'r,g,b,width' with r,g,b between [0,1]
  #a width of 0 means filling an area with a color
  ('highway=footway','0.4,0,0,2'),
  ('highway=cycleway','0,0.8,0,5'),
  ('highway=bridleway','0,0.8,0,5'),
  ('highway=residential','0.6,0.6,0.6,5'),
  ('highway=unclassified','0.6,0.6,0.6,5'),
  ('highway=tertiary','0.6,0.6,0.6,6'),
  ('highway=secondary','0.6,0.6,0.6,6'),
  ('highway=primary','0.6,0.6,0.6,7'),
  ('highway=trunk','0.6,0.6,0.6,8'),
  ('highway=motorway','0.6,0.6,0.6,9'),
  ('cycleway=lane','0,0.8,0,3'),
  ('cycleway=track','0,0.8,0,3'),
  ('cycleway=opposite_lane','0,0.8,0,3'),
  ('cycleway=opposite_track','0,0.8,0,3'),
  ('cycleway=opposite','0,0.8,0,3'),
  ('ncn_ref=*','1,0,0,5'),
  ('rcn_ref=*','1,0,0.3,5'),
  ('ncn=*','1,0,0,5'),
  ('rcn=*','1,0,0.3,5'),
  ]

landuseColours = [
  ('landuse=*', '0.6,0.6,0.6,0'),
  ]

boundaryColours = [
  ('boundary=*', '0.6,0,0.6,2'),
  ]

nonameColours = [
  ('name=~', '0.8,0,0,3'),
  ]
   
unfinishedColours = [
  ('todo=*', '0.8,0,0,3'),
  ('to_do=*', '0.8,0,0,3'),
  ('fixme=*', '0.8,0,0,3'),
  ('fix_me=*', '0.8,0,0,3'),
  ('note=*', '0,0.8,0,3'),
  ('notes=*', '0,0.8,0,3'),
  ]

stylesheets = {
  'underground':metroColours,
  'default':roadColours,
  'water':waterColours,
  'power':powerColours,
  'cycle':cycleColours,
  'landuse':landuseColours,
  'boundaries':boundaryColours,
  'buildings':buildingColours,
  'noname':nonameColours,
  'unfinished':unfinishedColours,
  };

def wayStyles(layer, tags):
  styles = []
  stylesheet = stylesheets.get(layer, None)
  if(stylesheet == None):
    return([])
  
  for (ident, style) in stylesheet: #roadColours:
    (tag,value) = ident.split('=')
    if(tags.get(tag,'default') == value):
      styles.append(style)
    elif((value == '*') and (tags.get(tag, None) != None)):
      styles.append(style)
    elif((value == '~') and (tags.get(tag, None) == None)):
      styles.append(style)
  if(not styles):
    styles.append('0.8,0.8,0.8,1') # default/debug
  return(styles)  

# TODO: these need to be static images, if we use them at all
backgrounds = {
  'parchment': (1.0,0.95,0.85,1),
  'white': (1,1,1,1),
  'black': (0,0,0,1),
  'blue': (0.8,0.8,1,1),
  'green': (0.8,1,0.8,1),
  }

class RenderClass(OsmRenderBase):
  
  # Specify the background for new tiles
  def imageBackgroundColour(self, mapLayer=None):
    return(backgrounds.get(mapLayer, (0,0,0,0)))
  
  # Draw a tile
  def draw(self, mapLayer):
    if(backgrounds.get(mapLayer, None) != None):
      return
    
    # Ways
    for w in self.osm.ways.values():
      layeradd = 0
      for style in wayStyles(mapLayer, w['t']):
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
