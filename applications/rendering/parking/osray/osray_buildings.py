# -*- coding: utf-8 -*-
# by kay - basic functions

from osray_db import *
from osray_geom import *

Radiosity = True

buildingtypes = {
    'yes':['<1,0.8,0.8>',1.0],
    'office':['<1,0.6,0.6>',1.0],
    'apartments':['<0.8,1,0.6>',1.0],
    'garages':['<0.8,0.8,0.8>',1.0],
    }
amenitybuildingtypes = {
    'shop':['<1,0.7,0.7>',1.0],
    'public_building':['<1,0.9,0.9>',1.0],
    'place_of_worship':['<1,1,0.6>',1.0],
    'hospital':['<1,0.6,0.6>',1.0],
    'theatre':['<1,0.6,1>',1.0],
    'university':['<0.6,1,0.8>',1.0],
    'parking':['<0.6,0.6,1>',1.0]
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

def get_building_texture(building,amenity,cladding):
    buildingparams = buildingtypes.get(building)
    texture = building
    if amenity!=None:
        if amenitybuildingtypes.has_key(amenity): # if amenity is known, use that color
            texture = amenity
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

    texture = get_building_texture(buildingtype,amenity,None)
    f.write("""texture {{ texture_building_{texture} }}""".format(texture=texture))
    f.write("""
    scale <1, {height}, 1>
}}
\n""".format(height=height))

def render_buidings(f,osraydb,options):
    Radiosity = options['radiosity']
    pov_declare_building_textures(f)
    buildings = []
    for buildingtype in buildingtypes.iterkeys():
        buildings += osraydb.select_buildings(buildingtype)
    
    for building in buildings:
        pov_building(f,building)

