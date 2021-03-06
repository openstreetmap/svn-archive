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

def pov_text(f,x,y,txt):
    f.write("""text {{
    ttf "timrom.ttf" "{txt}" 1, 0
    pigment {{ rgbt <0.5,0.5,0.5,0.5> }}
    translate <-{center},0,0>
    rotate <90, 0, 0>
    scale <18,0,18>
    translate <{x},0.3,{y}>
}}
\n""".format(txt=txt,x=x,y=y,center=0.25*len(txt)))

landusetypes = {
    'residential':['<1,0.9,0.8>'],

    'retail':['<1,1,0.9>'],
    'commercial':['<1,1,0.9>'],
    'industrial':['<1,1,0.7>'],

    'hospital':['<1,0.8,0.8>'],
    'landfill':['<0.6,0.7,0.4>'],
    'brownfield':['<0.7,0.6,0.3>'],'construction':['<0.7,0.6,0.3>'],
    
    'recreation_ground':['<0.8,0.9,0.8>'],
    'city_green':['<0.7,0.9,0.7>'],
    'village_green':['<0.7,0.9,0.7>'],
    'meadow':['<0.7,0.9,0.7>'],
    'grass':['<0.7,0.9,0.6>'],

    'cemetery':['<0.8,0.85,0.8>'],
    'monastery':['<0.8,0.75,0.5>'],'monestery':['<0.8,0.75,0.5>'],

    'farm':['<0.8,0.9,0.6>'],
    'farmland':['<0.8,0.9,0.7>'],
    'farmyard':['<0.8,0.9,0.7>'],
    'vineyard':['<0.5,0.9,0.6>'],

    'allotments':['<0.6,0.7,0.6>'],

    'forest':['<0.5,0.8,0.5>'],
    'wood':['<0.5,0.8,0.5>'],

    'basin':['<0.5,0.5,0.8>'],

    'unknown':['<1,0,1>']
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
    if landusetype in landusetypes:
        landuseparams = landusetypes.get(landusetype)
    else:
        landuseparams = landusetypes.get('unknown')
        print "### unknown landuse '{l}' found in osm_id {id}".format(l=landusetype,id=landuse['osm_id']) 

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
    if landusetype in landusetypes:
        pass
    else:
        pov_text(f,x,y,landusetype)

#-----------------------------------------------------------------------------

leisuretypes = {
    'park':['<0.7,1,0.6>'],'common':['<0.7,1,0.6>'],
    'pitch':['<0.3,1,0.8>'],'sports_centre':['<0.4,1,0.8>'],'stadium':['<0.4,1,0.8>'],
    'track':['<1,0.7,0.6>'],
    'ice_rink':['<1,1,1>'],
    'playground':['<0.2,0.9,0.9>'],'miniature_golf':['<0.2,0.9,0.9>'],
    'garden':['<1,1,0.4>'],
    'unknown':['<1,0,1>']
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
    if leisuretype in leisuretypes:
        pass
    else:
        pov_text(f,x,y,leisuretype)

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
    
    #print "map size x = ",map_size_x
    #print "map size y = ",map_size_y
    
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

def create_ground_texture(osraydb,options,texturename):
    f_ground = open(texturename, 'w')

    pov_globals(f_ground)
    pov_camera(f_ground,osraydb)

    landuses = osraydb.select_landuse_areas()
    for landuse in landuses:
        pov_landuse(f_ground,landuse)

    leisures = osraydb.select_leisure_areas()
    for leisure in leisures:
        pov_leisure(f_ground,leisure)

    waterways = []
    for waterwaytype in waterwaytypes.iterkeys():
        waterways += osraydb.select_waterway(waterwaytype)
    for waterway in waterways:
        pov_waterway(f_ground,waterway)

    waters = osraydb.select_naturalwater()
    for water in waters:
        pov_water(f_ground,water)

    f_ground.close()

    print "--> povray texture"
    image_dimension_parameters = "-W"+str(options['width'])+" -H"+str(options['height'])
    result = commands.getstatusoutput('povray -Iscene-osray-landuse-texture.pov -UV '+image_dimension_parameters+' +Q4 +A')
    print "--> povray texture=",result[1]

