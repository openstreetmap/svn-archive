# -*- coding: utf-8 -*-
# by kay - basic functions

from osray_db import *
from osray_geom import *

Radiosity = True

declarations = """
#macro tree_broad_leafed (x,y,height)
#local wipfel = sphere {
  <0,1,0>,0.5
  texture {
    pigment {
      color rgb <0.6,1.0,0.6>
    }
    finish {
      diffuse 0.8
      ambient 0
    }
  }   
} // end of sphere
#local stamm = cylinder {
  <0,0,0>,<0,1,0>,1.8
  texture {
    pigment {
      color rgb <0.6,0.6,0.1>
    }
    finish {
      diffuse 0.8
      ambient 0
    }
  }   
} // end of cylinder

union{
  object{wipfel scale <height,height,height> translate <x,0,y>}
  object{stamm scale <height,height,height> translate <x,0,y>}
 } // end of tree
#end
"""
def pov_tree(f,tree):
    treetype = tree['type']

    pointstring = tree['way']
    pointstring = linestring[6:] # cut off the "POINT("
    pointstring = linestring[:-1] # cut off the ")"

    heightstring = tree['height'] # height
    if (heightstring==None):
        height = 9.0
    else:
        height = parse_length_in_meters(heightstring,0.01)

    f.write("/* tree osm_id={0} */\n".format(tree['osm_id']))

    latlon = pointstring.split(' ')

    firstpoint="<{0}, {1}>\n".format(latlon[0],latlon[1])

    f.write("object {{ tree ({x},{y},{height}) }}\n".format(x=latlon[0],y=latlon[1],height=height))
    


def render_furniture(f,osraydb,options):
    Radiosity = options['radiosity']
    f.write(declarations)

    trees = osraydb.select_trees()

    for tree in trees:
        pov_tree(f,tree)

