# -*- coding: utf-8 -*-
# by kay - basic functions

from osray_db import *
from osray_geom import *

highwaytypes = { # highwaytype : [color,lane_width_factor]
    'motorway':['<1,0,0>',1.3],
    'motorway_link':['<1,0,0>',1.3],
    'trunk':['<0.9,1,0.9>',1.1],
    'trunk_link':['<0.9,1,0.9>',1.1],
    'primary':['<1,1,0.9>',1.0],
    'primary_link':['<1,1,0.9>',1.0],
    'secondary':['<1,0.9,0.9>',1.0],
    'secondary_link':['<1,0.9,0.9>',1.0],
    'tertiary':['<1,0.9,0.8>',1.0],
    'residential':['<0.9,0.9,0.9>',0.8],
    'living_street':['<0.8,0.8,0.9>',0.8],
    'unclassified':['<0.8,0.8,0.8>',0.8],
    'service':['<0.8,0.8,0.8>',0.6],
    'pedestrian':['<0.8,0.9,0.8>',0.8],
    'footway':['<0.8,0.9,0.8>',0.3],
    'steps':['<0.6,0.7,0.6>',0.3],
    'cycleway':['<0.8,0.8,0.9>',0.3],
    'track':['<0.7,0.7,0.6>',0.9],
    'bus_stop':['<1,0,0>',0.9],
    'bus_station':['<1,0,0>',0.9],
    'road':['<1,0,0>',0.9],
    'path':['<0.8,0.9,0.8>',0.5]
    }


def pov_declare_highway_textures(f):
    declare_highway_texture = """
#declare texture_highway_{highwaytype} =
    texture {{
        pigment {{
            color rgb {color}
        }}
        finish {{
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }}
    }}
"""
    for highwaytype in highwaytypes.keys():
        highwayparams = highwaytypes.get(highwaytype)
        color = highwayparams[0]
        f.write(declare_highway_texture.format(color=color,highwaytype=highwaytype))
    # texture for the highway casing
    f.write("""
#declare texture_highway_casing =
    texture {{
        pigment {{
            color rgb {color}
        }}
        finish {{
            specular 0.05
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }}
    }}
""".format(color='<0.3,0.3,0.3>'))


def draw_lines(f,points,height,streetwidth,highwaytype):
    numpoints = len(points)
    f.write("object {")
    f.write("union {")
    #f.write("sphere_sweep {{ linear_spline, {0},\n".format(numpoints+0))

    latlon_prev = None
    for i,point in enumerate(points):
        latlon = point.split(' ')
        f.write(" sphere {{ <{x}, 0, {y}>,{d} }}\n".format(x=latlon[0],y=latlon[1],d=streetwidth))
        if(latlon_prev != None):
            f.write(" cylinder {{ <{x1}, 0, {y1}>,<{x2}, 0, {y2}>,{d} }}\n".format(x1=latlon[0],y1=latlon[1],x2=latlon_prev[0],y2=latlon_prev[1],d=streetwidth))
        latlon_prev = latlon
    f.write("}") # union ends
    f.write("scale <1, 0.05, 1>")
    f.write("translate <0, {z}, 0>".format(z=height))
    #f.write("  } ") #end of intersection
    f.write("""texture {{ texture_highway_{highwaytype} }}
}}
\n""".format(highwaytype=highwaytype))
    
def draw_lines_sweep(f,points,height,streetwidth,highwaytype):
    numpoints = len(points)
    #f.write("intersection { object { boundbox } ")
    f.write("sphere_sweep {{ linear_spline, {0},\n".format(numpoints+0))

    for i,point in enumerate(points):
        latlon = point.split(' ')
        #if (i==0):
        #    f.write("  <{0}, {3}, {1}>,{2}\n".format(latlon[0],latlon[1],streetwidth,height))
        f.write("  <{x}, 0, {y}>,{d}\n".format(x=latlon[0],y=latlon[1],d=streetwidth))
        #if (i==numpoints-1):
        #   f.write("  <{0}, {3}, {1}>,{2}\n".format(latlon[0],latlon[1],streetwidth,height))

    #print highwayparams[0],highwayparams[1],streetwidth
    f.write("  tolerance 1")
    f.write("  scale <1, 0.05, 1>")
    f.write("  translate <0, {z}, 0>".format(z=height))
    #f.write("  } ") #end of intersection
    f.write("""
    texture {{ texture_highway_{highwaytype} }}
}}
\n""".format(highwaytype=highwaytype))
    
def pov_highway(f,highway):
    f.write("/* osm_id={0} */\n".format(highway[0]))
    highwaytype = highway[1]
    #highwaytype = 'secondary'
    highwayparams = highwaytypes.get(highwaytype)
    linestring = highway[2]
    linestring = linestring[11:] # cut off the "LINESTRING("
    linestring = linestring[:-1] # cut off the ")"
    points = linestring.split(',')

    oneway = False
    if(highway[5]=='yes'):
        oneway=True
    
    lanes = highway[3]
    if lanes!=None:
        try:
            lanefactor=float(lanes)
        except ValueError:
            lanes = None
    if lanes==None:
        lanefactor=2.0 # 2 lanes seems to be default
        if oneway:
            lanefactor=1.2 # except for one-ways (make them a bit wider than 2 lanes/2)

    lanewidth = 2.5 # m
    streetwidth = lanewidth * lanefactor

    layer = highway[4]
    if layer==None:
        layer='0'
    layer = int(layer)
    if layer<0:
        layer=0 # FIXME
    layerheight = 4.0*layer # 4 m per layer
# draw road
    draw_lines(f,points,layerheight,streetwidth*highwayparams[1],highwaytype)
# draw casing
    draw_lines(f,points,layerheight-0.05*lanefactor,1.2*streetwidth*highwayparams[1],'casing')

def pov_highway_area(f,highway):
    highwaytype = highway[1]
    #highwaytype = 'secondary'
    highwayparams = highwaytypes.get(highwaytype)

    linestring = highway[2]
    linestring = linestring[8:] # cut off the "POLYGON("
    linestring = linestring[:-1] # cut off the ")"

    heightstring = highway[3]
    #print 'hs="{0}"'.format(heightstring)
    height = 0.1

    amenity = highway[4]
    #print amenity
    
    points = linestring.split(',')#.strip('(').strip(')')
    #print points

    numpoints = len(points)
    #f.write("polygon {{ {0},\n".format(numpoints))
    f.write("prism {{ linear_spline 0, 0.01, {0},\n".format(numpoints))
    f.write("/* osm_id={0} */\n".format(highway[0]))

    for i,point in enumerate(points):
        latlon = point.split(' ')
        if (i==0):
            firstpoint="<{0}, {1}>\n".format(latlon[0],latlon[1])
        if (i!=numpoints-1):
            f.write("  <{0}, {1}>,\n".format(latlon[0].strip('(').strip(')'),latlon[1].strip('(').strip(')')))
        else:
            f.write("  <{0}, {1}>\n".format(latlon[0].strip('(').strip(')'),latlon[1].strip('(').strip(')')))
    #f.write(firstpoint)

    color = highwayparams[0]
    #color = "<0,1,0>"

    #if height != 10.0:
        #print 'height:', height
        #color = '<0,1,0>'
    f.write("""
    texture {{
        pigment {{
            color rgb {0}
        }}
        finish {{
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }}
    }}
    translate <0, {1}, 0>
}}
\n""".format(color,height))
    # 
    # draw the casing
    #
    height = 0.08
    linestring = highway[5]
    linestring = linestring[8:] # cut off the "POLYGON("
    linestring = linestring[:-1] # cut off the ")"
    points = linestring.split(',')#.strip('(').strip(')')
    numpoints = len(points)
    f.write("prism {{ linear_spline 0, 0.01, {0},\n".format(numpoints))
    f.write("/* casing of osm_id={0} */\n".format(highway[0]))

    for i,point in enumerate(points):
        latlon = point.split(' ')
        if (i==0):
            firstpoint="<{0}, {1}>\n".format(latlon[0],latlon[1])
        if (i!=numpoints-1):
            f.write("  <{0}, {1}>,\n".format(latlon[0].strip('(').strip(')'),latlon[1].strip('(').strip(')')))
        else:
            f.write("  <{0}, {1}>\n".format(latlon[0].strip('(').strip(')'),latlon[1].strip('(').strip(')')))

    color = highwayparams[0]
    f.write("""
    texture {{
        pigment {{
            color rgb <0.3,0.3,0.3>
        }}
        finish {{
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }}
    }}
    translate <0, {height}, 0>
}}
\n""".format(height=height))


def render_highways(f,osraydb):
    pov_declare_highway_textures(f)

    highways = []
    for highwaytype in highwaytypes.iterkeys():
        highways += osraydb.select_highways(highwaytype)

    for highway in highways:
        pov_highway(f,highway)
        #pass
    
    highway_areas = []
    for highwaytype in highwaytypes.iterkeys():
        highway_areas += osraydb.select_highway_areas(highwaytype)
    
    for highway in highway_areas:
        pov_highway_area(f,highway)
