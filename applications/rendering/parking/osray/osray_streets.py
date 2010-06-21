# -*- coding: utf-8 -*-
# by kay - basic functions

### config for Trunk
DSN = 'dbname=gis'

import sys
import commands
import psycopg2
import csv
import re
from numpy import *

highwaytypes = {
    'trunk':['<0.9,1,0.9>',1.0],
    'trunk_link':['<0.9,1,0.9>',1.0],
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
    'cycleway':['<0.8,0.8,0.9>',0.3],
    'path':['<0.8,0.9,0.8>',0.5]
    }
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

def avg(a,b): return (a+b)/2.0

def shift_by_meters(lat, lon, brng, d):
    R = 6371000.0 # earth's radius in m
    lat=math.radians(lat)
    lon=math.radians(lon)
    lat2 = math.asin( math.sin(lat)*math.cos(d/R) + 
                      math.cos(lat)*math.sin(d/R)*math.cos(brng))
    lon2 = lon + math.atan2(math.sin(brng)*math.sin(d/R)*math.cos(lat), 
                             math.cos(d/R)-math.sin(lat)*math.sin(lat2))
    lat2=math.degrees(lat2)
    lon2=math.degrees(lon2)
    return [lat2,lon2]

def calc_bearing(x1,y1,x2,y2,side):
    Q = complex(x1,y1)
    R = complex(x2,y2)
    v = R-Q
    if side=='left':
        v=v*complex(0,1);
    elif side=='right':
        v=v*complex(0,-1);
    else:
        raise TypeError('side must be left or right')
    #v=v*(1/abs(v)) # normalize
    angl = angle(v) # angle (radians) (0°=right, and counterclockwise)
    bearing = math.pi/2.0-angl # (0°=up, and clockwise)
    return bearing

def pov_highway(f,highway):
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
    if lanes==None:
        lanefactor=2.0 # 2 lanes seems to be default
        if oneway:
            lanefactor=1.2 # except for one-ways (make them a bit wider than 2 lanes/2)
    else:
        lanefactor=float(lanes)
    lanewidth = 2.5 # m
    streetwidth = lanewidth * lanefactor

    layer = highway[4]
    if layer==None:
        layer='0'
    layer = int(layer)
    if layer<0:
        layer=0 # FIXME
    layerheight = 4.0*layer # 4 m per layer
    layerheight = layerheight / 0.05 # counteract the scale statement

    numpoints = len(points)

# draw road
    f.write("sphere_sweep {{ linear_spline, {0},\n".format(numpoints+2))
    f.write("/* osm_id={0} */\n".format(highway[0]))

    for i,point in enumerate(points):
        latlon = point.split(' ')
        if (i==0):
            f.write("  <{0}, {3}, {1}>,{2}\n".format(latlon[0],latlon[1],streetwidth*highwayparams[1],layerheight))
        f.write("  <{0}, {3}, {1}>,{2}\n".format(latlon[0],latlon[1],streetwidth*highwayparams[1],layerheight))
        if (i==numpoints-1):
           f.write("  <{0}, {3}, {1}>,{2}\n".format(latlon[0],latlon[1],streetwidth*highwayparams[1],layerheight))

    print highwayparams[0],highwayparams[1],streetwidth
    f.write("""  tolerance 1
    texture {{
        pigment {{
            color rgb {0}
        }}
        finish {{
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }}
    }}
    scale <1, 0.05, 1>
}}
\n""".format(highwayparams[0]))
# draw casing
    f.write("sphere_sweep {{ linear_spline, {0},\n".format(numpoints+2))
    f.write("/* osm_id={0} */\n".format(highway[0]))

    for i,point in enumerate(points):
        latlon = point.split(' ')
        if (i==0):
            f.write("  <{0}, {3}, {1}>,{2}\n".format(latlon[0],latlon[1],1.2*streetwidth*highwayparams[1],layerheight))
        f.write("  <{0}, {3}, {1}>,{2}\n".format(latlon[0],latlon[1],1.2*streetwidth*highwayparams[1],layerheight))
        if (i==numpoints-1):
           f.write("  <{0}, {3}, {1}>,{2}\n".format(latlon[0],latlon[1],1.2*streetwidth*highwayparams[1],layerheight))

    print highwayparams[0],highwayparams[1],streetwidth
    f.write("""  tolerance 1
    texture {{
        pigment {{
            color rgb <0.2,0.2,0.2>
        }}
        finish {{
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }}
    }}
    scale <1, 0.05, 1>
    translate <0, -0.1, 0>
}}
\n""".format(highwayparams[0]))

def parse_length_in_meters(length,default):
    units={
           'm':1.0, 'metres':1.0, 'meters':1.0, 'metre':1.0, 'meter':1.0,
           'km':1000.0,
           'ft':0.3,
           'yd':1.1,
           }
    parsed = re.split('(\d*[,.]?\d+)',length)
    if len(parsed)!=3:
        print "### unparsable length '{0}', parsed: {1}".format(length,parsed)
        return default
    prefix = parsed[0].strip()
    if prefix!='':
        print "### unknown prefix '{0}' in length '{1}'".format(prefix,length)
        return default
    unit = parsed[2].strip().lower()
    if unit=='': unit='m' # defaults to m
    factor = units.get(unit,0.0)
    if factor==0.0:
        print "### unknown unit '{0}' in length '{1}'".format(unit,length)
    meter = float(parsed[1])*factor
    return meter

def pov_building(f,building):
    buildingtype = building[1]
    #buildingtype = 'secondary'
    buildingparams = buildingtypes.get(buildingtype)

    linestring = building[2]
    linestring = linestring[8:] # cut off the "POLYGON("
    linestring = linestring[:-1] # cut off the ")"

    heightstring = building[3]
    print 'hs="{0}"'.format(heightstring)
    if (heightstring==None):
        height = 10.0
    else:
        height = parse_length_in_meters(heightstring,0.01)

    amenity = building[4]
    print amenity
    
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

    if height != 10.0:
        print 'height:', height
        #color = '<0,1,0>'
    f.write("""
    texture {{
        pigment {{
            color rgb {0}
        }}
        finish {{
            specular 0.5
            roughness 0.05
            /*reflection 0.5*/
        }}
    }}
    scale <1, {1}, 1>
}}
\n""".format(color,height))

