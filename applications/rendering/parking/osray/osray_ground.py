# -*- coding: utf-8 -*-
# by kay - create color texture map for ground

import sys
import commands
import psycopg2
import csv
import re
from numpy import *
from osray_db import *
from osray_geom import *

landusetypes = {
    'residential':['<1,0.95,0.9>'],

    'retail':['<1,1,0.9>'],
    'commercial':['<1,1,0.9>'],
    'industrial':['<1,1,0.8>'],

    'hospital':['<1,0.8,0.8>'],
    'landfill':['<0.6,0.7,0.4>'],
    
    'recreation_ground':['<0.8,0.9,0.8>'],
    'city_green':['<0.8,0.9,0.8>'],
    'village_green':['<0.8,0.9,0.8>'],
    'meadow':['<0.8,0.9,0.8>'],
    'grass':['<0.8,0.9,0.8>'],

    'cemetery':['<0.8,0.85,0.8>'],

    'farm':['<0.8,0.9,0.6>'],
    'farmland':['<0.8,0.9,0.7>'],
    'farmyard':['<0.8,0.9,0.7>'],
    'vineyard':['<0.7,0.9,0.7>'],

    'allotments':['<0.6,0.7,0.6>'],

    'forest':['<0.7,0.8,0.7>'],
    'wood':['<0.7,0.8,0.7>']
    }
"""
basin (4356)
quarry (2410)
construction (1649)
reservoir (1475)
greenfield (1206)
landfill (1038)
brownfield (1036)
orchard (956)
railway (768)
military (685)
wood (491)
garages (275)
piste (89)
mine_spoils (77)
greenhouse_horticulture (74)
scrub (65)
water (54)
Sand (41)
building (39)
park (36)
resis (34)
plaza (31)
civil (31)
garden (28)
mining (22)
yes (22)
mixed (22)
common (21)
paddock (15)
fields (15)
Gehege (13)
conservation (13)
sport (12)
green (12)
wasteland (12)
"""

def pov_landuse(f,landuse):
    landusetype = landuse['landuse']
    landuseparams = landusetypes.get(landusetype)

    polygon = landuse['coords']

    numpoints = len(polygon)
    f.write("prism {{ linear_spline  0, 0.01, {0},\n".format(numpoints))
    f.write("/* osm_id={0} */\n".format(landuse['osm_id']))

    for i,point in enumerate(polygon):
        x,y = point
        if (i!=numpoints-1):
            f.write("  <{x}, {y}>,\n".format(x=x,y=y))
        else:
            f.write("  <{x}, {y}>\n".format(x=x,y=y))

    color = landuseparams[0]

    f.write("""
    texture {{
        pigment {{
            color rgb {0}
        }}
        finish {{
            ambient 1
            /*specular 0.5
            roughness 0.05
            reflection 0.5*/
        }}
    }}
}}
\n""".format(color))

#-----------------------------------------------------------------------------

leisuretypes = {
    'park':['<0.5,1,0.5>'],

    'unknown':['<0,1,1>']
    }

def pov_leisure(f,leisure):
    leisuretype = leisure['leisure']
    if leisuretype in leisuretypes:
        leisureparams = leisuretypes.get(leisuretype)
    else:
        leisureparams = leisuretypes.get('unknown')

    polygon = leisure['coords']

    numpoints = len(polygon)
    f.write("prism {{ linear_spline  0, 0.02, {0},\n".format(numpoints))
    f.write("/* osm_id={0} */\n".format(leisure['osm_id']))

    for i,point in enumerate(polygon):
        x,y = point
        if (i!=numpoints-1):
            f.write("  <{x}, {y}>,\n".format(x=x,y=y))
        else:
            f.write("  <{x}, {y}>\n".format(x=x,y=y))

    color = leisureparams[0]

    f.write("""
    texture {{
        pigment {{
            color rgb {0}
        }}
        finish {{
            ambient 1
            /*specular 0.5
            roughness 0.05
            reflection 0.5*/
        }}
    }}
}}
\n""".format(color))
    f.write(""" text {{
    ttf "timrom.ttf" "{lt}" 1, 0
    pigment {{ Red }}
    translate <-{center},0,0>
    rotate <90, 0, 0>
    scale <18,0,18>
    translate <{x},0.3,{y}>
  }}
\n""".format(lt=leisuretype,x=x,y=y,center=0.25*len(leisuretype)))

#-----------------------------------------------------------------------------

waterwaytypes = {
    'riverbank':['<0.2,0.2,0.9>'],
    'dock':['<0.2,0.2,0.8>']
    }

def pov_waterway(f,waterway):
    waterwaytype = waterway['waterway']
    waterwayparams = waterwaytypes.get(waterwaytype)

    polygon = waterway['coords']
    numpoints = len(polygon)
    f.write("prism {{ linear_spline  0, 0.01, {0},\n".format(numpoints))
    f.write("/* osm_id={0} */\n".format(waterway['osm_id']))

    for i,point in enumerate(polygon):
        x,y = point
        if (i!=numpoints-1):
            f.write("  <{x}, {y}>,\n".format(x=x,y=y))
        else:
            f.write("  <{x}, {y}>\n".format(x=x,y=y))

    color = waterwayparams[0]

    f.write("""
    texture {{
        pigment {{
            color rgb {0}
        }}
        finish {{
            ambient 1
            /*specular 0.5
            roughness 0.05
            reflection 0.5*/
        }}
    }}
}}
\n""".format(color))

def pov_water(f,water):
    polygon = water['coords']
    numpoints = len(polygon)
    f.write("prism {{ linear_spline  0, 0.01, {0},\n".format(numpoints))
    f.write("/* osm_id={0} */\n".format(water['osm_id']))

    for i,point in enumerate(polygon):
        x,y = point
        if (i!=numpoints-1):
            f.write("  <{x}, {y}>,\n".format(x=x,y=y))
        else:
            f.write("  <{x}, {y}>\n".format(x=x,y=y))

    color = waterwaytypes['riverbank'][0]

    f.write("""
    texture {{
        pigment {{
            color rgb {color}
        }}
        finish {{
            ambient 1
            /*specular 0.5
            roughness 0.05
            reflection 0.5*/
        }}
    }}
}}
\n""".format(color=color))

def pov_globals(f):
    globsettings = """
#include "colors.inc"
global_settings {{
    assumed_gamma 2.0
    noise_generator 2
}}
"""
    f.write(globsettings.format())

def pov_camera(f,osraydb):

    bottom = osraydb.bottom
    left = osraydb.left
    top = osraydb.top
    right = osraydb.right
    map_size_x = right-left
    map_size_y = top-bottom
    
    print "map size x = ",map_size_x
    print "map size y = ",map_size_y
    
    #image_aspect = float(options['width'])/float(options['height'])
    map_aspect = map_size_x/map_size_y

    zoom = 1.0
    zoom = map_size_y/zoom

    f.write("""
camera {{
   orthographic
   location <0, 10000, 0>
   sky <0, 1, 0>
   direction <0, 0, 1>
   right <{aspect}*{zoom}, 0, 0>
   up <0, 1*{zoom}*cos(radians({angle})), 0> /* this stretches in y to compensate for the rotate below */
   look_at <0, 0, 0>
   rotate <-{angle},0,0>
   scale <1,1,1>
   translate <{middle_x},0,{middle_z}>
}}
""".format(middle_x=avg(left,right),middle_z=avg(bottom,top),zoom=zoom,angle=10,aspect=map_aspect))
    f.write("""
/* ground */
box {{
    <{0}, -0.5, {1}>, <{2}, -0.0, {3}>
    pigment {{
        color rgb <1, 1, 1>
    }}
    finish {{
        ambient 1
    }}
}}
""".format(left,bottom,right,top))

def create_landuse_texture(osraydb,options,texturename):
    f_landuse = open(texturename, 'w')

    pov_globals(f_landuse)
    pov_camera(f_landuse,osraydb)

    landuses = []
    for landusetype in landusetypes.iterkeys():
        landuses += osraydb.select_landuse(landusetype)
    for landuse in landuses:
        pov_landuse(f_landuse,landuse)

    leisures = osraydb.select_leisure_areas()
    for leisure in leisures:
        pov_leisure(f_landuse,leisure)

    waterways = []
    for waterwaytype in waterwaytypes.iterkeys():
        waterways += osraydb.select_waterway(waterwaytype)
    for waterway in waterways:
        pov_waterway(f_landuse,waterway)

    waters = osraydb.select_naturalwater()
    for water in waters:
        pov_water(f_landuse,water)

    f_landuse.close()

    print "--> povray texture"
    image_dimension_parameters = "-W"+str(options['width'])+" -H"+str(options['height'])
    result = commands.getstatusoutput('povray -Iscene-osray-landuse-texture.pov -UV '+image_dimension_parameters+' +Q4 +A')
    print "--> povray texture=",result[1]

