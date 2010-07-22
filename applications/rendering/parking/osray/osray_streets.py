# -*- coding: utf-8 -*-
# by kay - basic functions

from osray_db import *
from osray_geom import *
from osray_network import *

Radiosity = True

highwaytypes = { # highwaytype : [color,lane_width_factor]
    'motorway':['<1,0.2,0>',1.3],
    'motorway_link':['<1,0.2,0>',1.3],
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
    'steps':['<0.5,0.65,0.5>',0.3],
    'cycleway':['<0.8,0.8,0.9>',0.3],
    'track':['<0.7,0.7,0.6>',0.8],
    'bus_stop':['<1,0,0>',0.9],
    'bus_station':['<1,0,0>',0.9],
    'road':['<1,0,0>',0.9],
    'path':['<0.8,0.9,0.8>',0.5],
    'unknown':['<1.0,0,1.0>',1.0]
    }


def pov_declare_highway_textures(f):
    if Radiosity:
        declare_highway_texture = """
#declare texture_highway_{highwaytype} =
    texture {{
        pigment {{
            color rgb {color}
        }}
        finish {{
            diffuse 0.7
            ambient 0
        }}
    }}
"""
    else:
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
    f.write(declare_highway_texture.format(color='<0.4,0.4,0.4>',highwaytype='casing'))


def draw_lines(f,line,height,streetwidth,highwaytype):
    numpoints = len(line)
    f.write("object {")
    f.write("union {")
    #f.write("sphere_sweep {{ linear_spline, {0},\n".format(numpoints+0))

    x_prev = None
    for i,point in enumerate(line):
        x,y = point
        f.write(" sphere {{ <{x}, 0, {y}>,{d} }}\n".format(x=x,y=y,d=streetwidth))
        if(x_prev != None):
            f.write(" cylinder {{ <{x1}, 0, {y1}>,<{x2}, 0, {y2}>,{d} }}\n".format(x1=x,y1=y,x2=x_prev,y2=y_prev,d=streetwidth))
        x_prev=x
        y_prev=y
    f.write("}") # union ends
    f.write("scale <1, 0.05, 1>")
    f.write("translate <0, {z}, 0>".format(z=height))
    #f.write("  } ") #end of intersection
    f.write("""texture {{ texture_highway_{highwaytype} }}
}}
\n""".format(highwaytype=highwaytype))
    
def draw_way(f,line,height_offset,streetwidth,highwaytype,og):
    squeeze = 0.05
    #FIXME (make casing very thin, otherwise it draws over ways, if road is not horizontally aligned)
    #if height_offset>0:
    #    squeeze = 0.0005
    numpoints = len(line)
    f.write("object {")
    f.write("union {")
    #f.write("sphere_sweep {{ linear_spline, {0},\n".format(numpoints+0))

    x_prev = None
    for i,point in enumerate(line):
        x,y = point
        z = og.get_height(point)
        f.write(" sphere {{ <{x}, {zs}, {y}>,{d} }}\n".format(x=x,y=y,zs=(z/squeeze),d=streetwidth))
        if(x_prev != None):
            f.write(" cylinder {{ <{x1}, {z1s}, {y1}>,<{x2}, {z2s}, {y2}>,{d} }}\n".format(x1=x,y1=y,z1s=(z/squeeze),x2=x_prev,y2=y_prev,z2s=(z_prev/squeeze),d=streetwidth))
        x_prev=x
        y_prev=y
        z_prev=z
    f.write("}") # union ends
    f.write("scale <1, {s}, 1>".format(s=squeeze))
    f.write("translate <0, {z}, 0>".format(z=height_offset))
    #f.write("  } ") #end of intersection
    f.write("""texture {{ texture_highway_{highwaytype} }}
}}
\n""".format(highwaytype=highwaytype))
    
def calculate_lanefactor(lanes,lanesfw,lanesbw,oneway):
    if lanes==None:#
        # no lanes specified: look if lanes:forward is specified
        if lanesfw!=None and lanesbw!=None:
            return parse_float_safely(lanesfw,1.0) + parse_float_safely(lanesbw,1.0) 
        # none specified: use defaults
        lanefactor = 2.0 # 2 lanes seems to be default
        if parse_yes_no_safely(oneway,False)==True:
            lanefactor = 1.2 # except for one-ways (make it a bit wider than 2 lanes/2)
        return lanefactor
    return parse_float_safely(lanes,2.0)

def pov_highway(f,highway,og):
    f.write("/* osm_id={0} */\n".format(highway['osm_id']))
    highwaytype = highway['highway']

    if highwaytype in highwaytypes:
        highwayparams = highwaytypes.get(highwaytype)
    else:
        highwayparams = highwaytypes.get('unknown')
        print "### highway with id={id} has unknown type {typ}.".format(id=highway['osm_id'],typ=highwaytype)
        highwaytype = 'unknown'

    line = highway['coords']

#    oneway = False
#    if(highway['oneway']=='yes'):
#        oneway=True

    lanefactor = calculate_lanefactor(highway['lanes'],highway['lanesfw'],highway['lanesbw'],highway['oneway'])
    lanewidth = 2.5 # m
    streetwidth = lanewidth * lanefactor

    layer = parse_int_safely(highway['layer'],default=0)
    if layer<0:
        layer=0 # FIXME
    layerheight = 4.0*layer # 4 m per layer
## draw road
#    draw_lines(f,line,layerheight,streetwidth*highwayparams[1],highwaytype)
## draw casing
#    draw_lines(f,line,layerheight-0.05*lanefactor,1.2*streetwidth*highwayparams[1],'casing')
    draw_way(f,line,0.1*lanefactor,streetwidth*highwayparams[1],highwaytype,og)
    draw_way(f,line,-0.1*lanefactor,1.2*streetwidth*highwayparams[1],'casing',og)

def pov_highway_area(f,highway):
    highwaytype = highway['highway']
    highwayparams = highwaytypes.get(highwaytype)

    polygon = highway['coords']

    heightstring = highway['height']
    height = 0.1 #FIXME

    amenity = highway['amenity']
    
    # 
    # draw the casing
    #
    numpoints = len(polygon)
    #f.write("polygon {{ {0},\n".format(numpoints))
    f.write("prism {{ linear_spline 0, 0.01, {0},\n".format(numpoints))
    f.write("/* osm_id={0} */\n".format(highway['osm_id']))

    for i,point in enumerate(polygon):
        x,y = point
        if (i!=numpoints-1):
            f.write("  <{x}, {y}>,\n".format(x=x,y=y))
        else:
            f.write("  <{x}, {y}>\n".format(x=x,y=y))

    color = highwayparams[0]

    f.write("""
    texture {{ texture_highway_{highwaytype} }}
    translate <0, {height}, 0>
}}
\n""".format(highwaytype=highwaytype,height=height))
    # 
    # draw the casing
    #
    height = height-0.02
    polygon = highway['buffercoords']
    numpoints = len(polygon)
    f.write("prism {{ linear_spline 0, 0.01, {0},\n".format(numpoints))
    f.write("/* casing of osm_id={0} */\n".format(highway['osm_id']))

    for i,point in enumerate(polygon):
        x,y = point
        if (i!=numpoints-1):
            f.write("  <{x}, {y}>,\n".format(x=x,y=y))
        else:
            f.write("  <{x}, {y}>\n".format(x=x,y=y))

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

# ---------------------------------------------------------------------------

declarations = """
/* ********* barriers *********** */

#macro barrier_bollard ()
object {
  union{
    sphere { <0, 0.9,0>,0.2 }
    cylinder { <0, 0, 0>,<0, 0.9, 0>,0.2 }
  }
  texture {
    pigment {
      color rgb <1,0.1,0.1>
    }
    finish {
      diffuse 0.7
      ambient 0
    }
  }
}
#end
#macro barrier_unknown ()
object {
  union{
    sphere { <0, 0.9,0>,0.2 }
    cylinder { <0, 0, 0>,<0, 0.9, 0>,0.2 }
  }
  texture {
    pigment {
      color rgb <1,0.1,1>
    }
    finish {
      diffuse 0.7
      ambient 0
    }
  }
}
#end
"""

XXX_barriertypes = { # barriertype : [color,diameter,height]
    'bollard':['<1,0.2,0.2>',0.2,0.9],
    'unknown':['<1,0,1>',0.3,0.5]
    }

def pov_XXX_barrier(f,barrier,og):
    f.write("/* osm_id={0} */\n".format(barrier['osm_id']))
    barriertype = barrier['barrier']

    if barriertype in barriertypes:
        barrierparams = barriertypes.get(barriertype)
    else:
        barrierparams = barriertypes.get('unknown')
    point = barrier['coords']
    x,y = point
    z = og.get_height(point)

    color = barrierparams[0]
    diameter = barrierparams[1] # FIXME diameter =2*r
    tallness = barrierparams[2]

    f.write("object {")
    f.write("union {")
    f.write(" cylinder {{ <{x1}, {z1}, {y1}>,<{x2}, {z2}, {y2}>,{d} }}\n".format(x1=x,y1=y,z1=z,x2=x,y2=y,z2=(z+tallness),d=diameter))
    f.write(" sphere {{ <{x}, {z}, {y}>,{d} }}\n".format(x=x,y=y,z=(z+tallness),d=diameter))
    f.write("}") # union ends
    f.write("""texture {{
        pigment {{
            color rgb {col}
        }}
        finish {{
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }}
    }}
}}
\n""".format(col=color))

barriertypes = { # barriertype : [color,diameter,height]
    'bollard':['bollard'],
    'unknown':['unknown']
    }

def pov_barrier(f,barrier,og):
    f.write("/* barrier osm_id={0} */\n".format(barrier['osm_id']))
    barriertype = barrier['barrier']

    if barriertype in barriertypes:
        barrierparams = barriertypes.get(barriertype)
    else:
        barrierparams = barriertypes.get('unknown')
    point = barrier['coords']
    x,y = point
    #print "trying barrier with points ", point
    try:
        z = og.get_height(point)
    except KeyError:
        # this happens if a barrier is not part of a way
        z = 0
        print "### barrier {id} is not part of a way".format(id=barrier['osm_id'])

    f.write("object {{ barrier_{typ} () scale <5,5,5> translate <{x},{z},{y}> }}\n".format(x=x,y=y,z=z,typ=barrierparams[0]))

def render_highways(f,osraydb,options):
    Radiosity = options['radiosity']
    f.write(declarations)

    og = osrayNetwork()
    pov_declare_highway_textures(f)

    highways = osraydb.select_highways()

    for highway in highways:
        og.add_highway(highway['coords'],parse_int_safely(highway['layer'],default=0))

    og.calculate_height_on_nodes()
    #print og.G.nodes()
    
    for highway in highways:
        pov_highway(f,highway,og)
        #pass

    highway_areas = []
    for highwaytype in highwaytypes.iterkeys():
        highway_areas += osraydb.select_highway_areas(highwaytype)
    
    for highway in highway_areas:
        pov_highway_area(f,highway)

    barriers = osraydb.select_barriers()
    for barrier in barriers:
        pov_barrier(f,barrier,og)