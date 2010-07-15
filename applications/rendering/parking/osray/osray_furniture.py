# -*- coding: utf-8 -*-
# by kay - basic functions

from osray_db import *
from osray_geom import *

Radiosity = True

declarations = """
#macro tree_broad_leafed (xp,yp,height)
#local wipfel = cone {
  <0,0.666,0>,0.333
  texture {
    pigment {
      color rgb <0.6,1.0,0.6>
    }
    finish {
      diffuse 0.7
      ambient 0
    }
  }   
} // end of sphere
#local stamm = cylinder {
  <0,0,0>,<0,0.4,0>,0.12
  texture {
    pigment {
      color rgb <0.4,0.3,0.1>
    }
    finish {
      diffuse 0.8
      ambient 0
    }
  }   
} // end of cylinder

union{
  object{wipfel scale <height,height,height> translate <xp,0,yp>}
  object{stamm scale <height,height,height> translate <xp,0,yp>}
 } // end of tree
#end

#macro tree_conifer (xp,yp,height)
#local wipfel = cone {
  <0,0.3,0>,0.5
  <0,1.0,0>,0.0
  texture {
    pigment {
      color rgb <0.5,0.8,0.6>
    }
    finish {
      diffuse 0.7
      ambient 0
    }
  }   
} // end of sphere
#local stamm = cylinder {
  <0,0,0>,<0,0.31,0>,0.12
  texture {
    pigment {
      color rgb <0.4,0.3,0.1>
    }
    finish {
      diffuse 0.8
      ambient 0
    }
  }   
} // end of cylinder

union{
  object{wipfel scale <height,height,height> translate <xp,0,yp>}
  object{stamm scale <height,height,height> translate <xp,0,yp>}
 } // end of tree
#end

"""
def pov_tree(f,tree):
    treetype = tree['type']
    x,y = tree['coords']
    heightstring = tree['height'] # height
    if (heightstring==None):
        height = 10.0
    else:
        height = parse_length_in_meters(heightstring,0.01)
    f.write("/* tree osm_id={0} */\n".format(tree['osm_id']))
    if treetype=='conifer':
        f.write("object {{ tree_conifer ({x},{y},{height}) }}\n".format(x=x,y=y,height=height))
    else:
        f.write("object {{ tree_broad_leafed ({x},{y},{height}) }}\n".format(x=x,y=y,height=height))
    
def render_furniture(f,osraydb,options):
    Radiosity = options['radiosity']
    f.write(declarations)

    trees = osraydb.select_trees()

    for tree in trees:
        pov_tree(f,tree)

