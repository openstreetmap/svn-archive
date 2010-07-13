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
            diffuse 0.8
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
    buildingtype = building[2]
    #buildingtype = 'secondary'
    buildingparams = buildingtypes.get(buildingtype)

    linestring = building[1]
    linestring = linestring[8:] # cut off the "POLYGON("
    linestring = linestring[:-1] # cut off the ")"

    heightstring = building[4] # building:height
    if (heightstring==None):
        heightstring = building[3]
        if (heightstring==None):
            height = 10.0
        else:
            height = parse_length_in_meters(heightstring,0.01)
    else:
        height = parse_length_in_meters(heightstring,0.01)

    amenity = building[5]
    #print amenity

    points = linestring.split(',')#.strip('(').strip(')')
    #print points

    numpoints = len(points)
    f.write("prism {{ linear_spline  0, 1, {0},\n".format(numpoints))
    f.write("/* osm_id={0} */\n".format(building[0]))

    for i,point in enumerate(points):
        latlon = point.split(' ')
        if (i==0):
            firstpoint="<{0}, {1}>\n".format(latlon[0],latlon[1])
        if (i!=numpoints-1):
            f.write("  <{0}, {1}>,\n".format(latlon[0].strip('(').strip(')'),latlon[1].strip('(').strip(')')))
        else:
            f.write("  <{0}, {1}>\n".format(latlon[0].strip('(').strip(')'),latlon[1].strip('(').strip(')')))
    #f.write(firstpoint)

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
    Radiosity = options['Radiosity']
    pov_declare_building_textures(f)
    buildings = []
    for buildingtype in buildingtypes.iterkeys():
        buildings += osraydb.select_buildings(buildingtype)
    
    for building in buildings:
        pov_building(f,building)

