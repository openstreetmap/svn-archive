# -*- coding: utf-8 -*-
# by kay - create color texture map for ground

import sys
import commands
import psycopg2
import csv
import re
from numpy import *

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

def pov_globals(f):
    globsettings = """
global_settings {{
    assumed_gamma 1.5
    noise_generator 2
}}
"""
    f.write(globsettings.format())

def pov_camera(f,bbox):
    polygonstring = bbox[0][0]
    polygonstring = polygonstring[9:] # cut off the "POLYGON(("
    polygonstring = polygonstring[:-2] # cut off the "))"
    #print polygonstring
    points = polygonstring.split(',')

    numpoints = len(points)

    for i,point in enumerate(points):
        latlon = point.split(' ')
        if (i==0):
            left=float(latlon[0])
            bottom=float(latlon[1])
        if (i==2):
            right=float(latlon[0])
            top=float(latlon[1])

    #print bottom,left,top,right
    zoom = 1.5
    zoom = 1100.0/zoom
    f.write("""
camera {{
   orthographic
   location <0, 10000, 0>
   sky <0, 1, 0>
   direction <0, 0, 1>
   right <1.3333*{2}, 0, 0>
   up <0, 1*{2}*cos(radians({3})), 0>
   look_at <0, 0, 0>
   rotate <-{3},0,0>
   scale <1,1,1>
   translate <{0},0,{1}>
}}
""".format(avg(left,right),avg(bottom,top),zoom,10))
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

    #f.write("""light_source {{ <{0}, 50000, {1}>, rgb <0.5, 0.5, 0.5> }}\n""".format(avg(left*1.5,right*0.5),avg(bottom*1.5,top*0.5)))
    #f.write("""light_source {{ <{0}, 5000, {1}>, rgb <0.5, 0.5, 0.5> }}\n""".format(avg(left*0.5,right*1.5),avg(bottom*1.5,top*0.5)))
    #f.write("""light_source {{ <300000+{0}, 1000000, -1000000+{1}>, rgb <1, 1, 1> }}\n""".format(avg(left,right),avg(bottom,top)))

def create_landuse_texture(conn,curs,options):
    f_landuse = open('/home/kayd/workspace/Parking/osray/scene-osray-landuse-texture.pov', 'w')

    thebbox = options['bbox']
    prefix = options['prefix']

    latlon= 'ST_Y(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),ST_X(ST_Transform(ST_line_interpolate_point(way,0.5),4326))'
    coords= "ST_Y(ST_line_interpolate_point(way,0.5)) as py,ST_X(ST_line_interpolate_point(way,0.5)) as px,ST_Y(ST_line_interpolate_point(way,0.49)) as qy,ST_X(ST_line_interpolate_point(way,0.49)) as qx,ST_Y(ST_line_interpolate_point(way,0.51)) as ry,ST_X(ST_line_interpolate_point(way,0.51)) as rx"
    FlW = "FROM "+prefix+"_line WHERE"
    FpW = "FROM "+prefix+"_polygon WHERE"

    googbox = "transform(SetSRID('BOX3D("+thebbox+")'::box3d,4326),900913)"

    #curs.execute("SELECT ST_AsText(transform(SetSRID('BOX3D(9.92498 49.78816,9.93955 49.8002)'::box3d,4326),900913)) AS geom")
    curs.execute("SELECT ST_AsText("+googbox+") AS geom")

    bbox = curs.fetchall()
    pov_globals(f_landuse)
    pov_camera(f_landuse,bbox)

    landuses = []
    for landusetype in landusetypes.iterkeys():
        curs.execute("SELECT osm_id,landuse,ST_AsText(\"way\") AS geom "+FpW+" \"way\" && "+googbox+" and landuse='"+landusetype+"' LIMIT 1700;")
        landuses += curs.fetchall()

        for landuse in landuses:
            pov_landuse(f_landuse,landuse)

    f_landuse.close()

    image_dimension_parameters = "-W"+str(options['width'])+" -H"+str(options['height'])
    result = commands.getstatusoutput('povray -Iscene-osray-landuse-texture.pov -UV '+image_dimension_parameters+' +Q9 +A')
    # print result[1]

