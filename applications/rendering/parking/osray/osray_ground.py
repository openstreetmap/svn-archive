# -*- coding: utf-8 -*-
# by kay - create color texture map for ground

import sys
import commands
import psycopg2
import csv
import re
from numpy import *
from osray_db import *

def avg(a,b): return (a+b)/2.0

landusetypes = {
    'residential':['<0.9,1,0.9>'],
    'commercial':['<1,1,0.9>'],
    'hospital':['<1,0.8,0.8>'],
    'landfill':['<0.6,0.7,0.4>'],
    'recreation_ground':['<0.8,0.9,0.8>'],
    'city_green':['<0.8,0.9,0.8>'],
    'farm':['<0.8,0.9,0.8>'],
    'farmland':['<0.8,0.9,0.8>'],
    'wood':['<0.8,0.9,0.8>']
    }

def pov_landuse(f,landuse):
    landusetype = landuse[1]
    print "landusetype=",landusetype
    #landusetype = 'secondary'
    landuseparams = landusetypes.get(landusetype)

    linestring = landuse[2]
    linestring = linestring[8:] # cut off the "POLYGON("
    linestring = linestring[:-1] # cut off the ")"

    points = linestring.split(',')#.strip('(').strip(')')
    #print points

    numpoints = len(points)
    f.write("prism {{ linear_spline  0, 0.01, {0},\n".format(numpoints))
    f.write("/* osm_id={0} */\n".format(landuse[0]))

    for i,point in enumerate(points):
        latlon = point.split(' ')
        if (i==0):
            firstpoint="<{0}, {1}>\n".format(latlon[0],latlon[1])
        if (i!=numpoints-1):
            f.write("  <{0}, {1}>,\n".format(latlon[0].strip('(').strip(')'),latlon[1].strip('(').strip(')')))
        else:
            f.write("  <{0}, {1}>\n".format(latlon[0].strip('(').strip(')'),latlon[1].strip('(').strip(')')))
    #f.write(firstpoint)

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

waterwaytypes = {
    'riverbank':['<0.2,0.2,0.9>'],
    'dock':['<0.2,0.2,0.8>']
    }

def pov_waterway(f,waterway):
    waterwaytype = waterway[1]
    print "waterwaytype=",waterwaytype
    #waterwaytype = 'secondary'
    waterwayparams = waterwaytypes.get(waterwaytype)

    linestring = waterway[2]
    linestring = linestring[8:] # cut off the "POLYGON("
    linestring = linestring[:-1] # cut off the ")"

    points = linestring.split(',')#.strip('(').strip(')')
    #print points

    numpoints = len(points)
    f.write("prism {{ linear_spline  0, 0.01, {0},\n".format(numpoints))
    f.write("/* osm_id={0} */\n".format(waterway[0]))

    for i,point in enumerate(points):
        latlon = point.split(' ')
        if (i==0):
            firstpoint="<{0}, {1}>\n".format(latlon[0],latlon[1])
        if (i!=numpoints-1):
            f.write("  <{0}, {1}>,\n".format(latlon[0].strip('(').strip(')'),latlon[1].strip('(').strip(')')))
        else:
            f.write("  <{0}, {1}>\n".format(latlon[0].strip('(').strip(')'),latlon[1].strip('(').strip(')')))
    #f.write(firstpoint)

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

def pov_globals(f):
    globsettings = """
global_settings {{
    assumed_gamma 1.5
    noise_generator 2
}}
"""
    f.write(globsettings.format())

def pov_camera(f,osraydb):

    bottom = float(osraydb.bottom)
    left = float(osraydb.left)
    top = float(osraydb.top)
    right = float(osraydb.right)
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

    waterways = []
    for waterwaytype in waterwaytypes.iterkeys():
        waterways += osraydb.select_waterway(waterwaytype)
    for waterway in waterways:
        pov_waterway(f_landuse,waterway)

    f_landuse.close()

    image_dimension_parameters = "-W"+str(options['width'])+" -H"+str(options['height'])
    result = commands.getstatusoutput('povray -Iscene-osray-landuse-texture.pov -UV '+image_dimension_parameters+' +Q9 +A')
    # print result[1]

