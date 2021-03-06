# -*- coding: utf-8 -*-
# by kay - basic functions

from osray_db import *
from osray_geom import *

Radiosity = True

buildingtypes = {
    'yes':['<1,0.7,0.7>',1.0],
    'office':['<1,0.6,0.5>',1.0],
    'apartments':['<0.8,1,0.6>',1.0],
    'garages':['<0.8,0.8,0.8>',1.0],
    }
amenitybuildingtypes = {
    'shop':['<1,0.5,0.6>',1.0],
    'public_building':['<1,0.9,0.9>',1.0],
    'place_of_worship':['<1,1,0.5>',1.0],
    'restaurant':['<0.9,0.8,0.3>',1.0],'fast_food':['<0.9,0.8,0.3>',1.0],
    'hospital':['<1,0.6,0.6>',1.0],
    'theatre':['<1,0.6,0.8>',1.0],
    'university':['<0.6,1,0.8>',1.0],
    'school':['<0.8,1,0.9>',1.0],
    'parking':['<0.6,0.6,1>',1.0],
    'unknown':['<1.0,0,1.0>',1.0]
    }


def pov_declare_building_textures(f):
    if Radiosity:
        declare_building_texture = """
#declare texture_building_{buildingtype} =
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
        declare_building_texture = """
#declare texture_building_{buildingtype} =
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
    for buildingtype in buildingtypes.keys():
        buildingparams = buildingtypes.get(buildingtype)
        color = buildingparams[0]
        f.write(declare_building_texture.format(color=color,buildingtype=buildingtype))
    for buildingtype in amenitybuildingtypes.keys():
        buildingparams = amenitybuildingtypes.get(buildingtype)
        color = buildingparams[0]
        f.write(declare_building_texture.format(color=color,buildingtype=buildingtype))

def get_building_texture(building,amenity,shop,cladding):
    buildingparams = buildingtypes.get(building)
    texture = building
    if shop!=None:
        texture = 'shop'
        if amenity!=None:
            if amenity!='shop':
                texture = 'unknown'
                print "### amenity is not shop ({am}), but shop is declared ({sh}) for building {id}"
        return texture
    if amenity!=None:
        if amenitybuildingtypes.has_key(amenity): # if amenity is known, use that color
            texture = amenity
        else:
            texture = 'unknown'
    if cladding!=None:
        if claddingtypes.has_key(cladding): # if cladding is known, use that color
            texture = cladding
    return texture

def pov_building(f,building):
    buildingtype = building['building']
    buildingparams = buildingtypes.get(buildingtype)

    polygon = building['coords']

    heightstring = building['bheight'] # building:height
    if (heightstring==None):
        heightstring = building['height']

    if (heightstring==None):
        height = 10.0
    else:
        height = parse_length_in_meters(heightstring,0.01)

    amenity = building['amenity']
    shop = building['shop']
        
    numpoints = len(polygon)
    f.write("prism {{ linear_spline  0, 1, {0},\n".format(numpoints))
    f.write("/* osm_id={0} */\n".format(building['osm_id']))

    for i,point in enumerate(polygon):
        x,y = point
        if (i!=numpoints-1):
            f.write("  <{x}, {y}>,\n".format(x=x,y=y))
        else:
            f.write("  <{x}, {y}>\n".format(x=x,y=y))

    color = buildingparams[0]
    if amenitybuildingtypes.has_key(amenity): # if amenity is known, use that color
        amenitybuildingparams = amenitybuildingtypes.get(amenity)
        color = amenitybuildingparams[0]

    texture = get_building_texture(buildingtype,amenity,shop,None)
    f.write("""texture {{ texture_building_{texture} }}""".format(texture=texture))
    f.write("""
    scale <1, {height}, 1>
}}
\n""".format(height=height))

#---------------------------------------------------------------------
barriertypes = { # barrier: type, default-height
    'yes':['<0.8,0.8,0.8>',1.5],'wall':['<0.8,0.8,0.8>',1.5],
    'city_wall':['<1,1,0.8>',9.0],
    'retaining_wall':['<0.8,0.8,0.6>',9.0],
    'fence':['<0.7,0.7,0.2>',2.0],
    'wire_fence':['<0.7,0.7,0.2>',2.5],
    'hedge':['<0.3,0.8,0.4>',2.0],
    'unknown':['<1,0.1,1>',15]
    }

def pov_declare_barrier_textures(f):
    if Radiosity:
        declare_barrier_texture = """
#declare texture_barrier_{barriertype} =
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
        declare_barrier_texture = """
#declare texture_barrier_{barriertype} =
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
    for barriertype in barriertypes.keys():
        barrierparams = barriertypes.get(barriertype)
        color = barrierparams[0]
        f.write(declare_barrier_texture.format(color=color,barriertype=barriertype))

def pov_barrier(f,barrier):
    barriertype = barrier['barrier']
    if barriertype in barriertypes:
        barrierparams = barriertypes.get(barriertype)
        texture = barriertype
    else:
        barrierparams = barriertypes.get('unknown')
        texture = 'unknown'

    #print "barrier ",barriertype," -> ",texture
    polygon = barrier['coords']

    heightstring = barrier['height']

    if (heightstring==None):
        height = barrierparams[1]
    else:
        height = parse_length_in_meters(heightstring,25)

    numpoints = len(polygon)
    f.write("prism {{ linear_spline  0, 1, {0},\n".format(numpoints))
    f.write("/* barrier osm_id={0} */\n".format(barrier['osm_id']))

    for i,point in enumerate(polygon):
        x,y = point
        if (i!=numpoints-1):
            f.write("  <{x}, {y}>,\n".format(x=x,y=y))
        else:
            f.write("  <{x}, {y}>\n".format(x=x,y=y))

    f.write("""texture {{ texture_barrier_{texture} }}""".format(texture=texture))
    f.write("""
    scale <1, {height}, 1>
}}
\n""".format(height=height))
#--------------------------------------------------------------------
def render_buidings(f,osraydb,options):
    Radiosity = options['radiosity']
    pov_declare_building_textures(f)
    buildings = []
    for buildingtype in buildingtypes.iterkeys():
        buildings += osraydb.select_buildings(buildingtype)
    
    for building in buildings:
        pov_building(f,building)

    pov_declare_barrier_textures(f)
    barriers = osraydb.select_barrier_lines()

    for barrier in barriers:
        pov_barrier(f,barrier)

