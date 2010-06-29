# -*- coding: utf-8 -*-
# by kay drangmeister

import sys
import commands
import psycopg2
import csv
import re
from numpy import *
from osray_db import *
from osray_ground import *
from osray_streets import *
from optparse import OptionParser

def pov_globals(f):
    globsettings = """
#include "colors.inc"
global_settings {{
    assumed_gamma 2.0
    noise_generator 2
    ambient_light rgb <0.9,0.9,1>
/*
    radiosity {{
        count 1000
        error_bound 0.7
        recursion_limit 6
        pretrace_end 0.002
    }}
*/
}}
"""
    f.write(globsettings.format())

def pov_camera(f,osraydb,options):

    bottom = float(osraydb.bottom)
    left = float(osraydb.left)
    top = float(osraydb.top)
    right = float(osraydb.right)
    map_size_x = right-left
    map_size_y = top-bottom
    
    print "map size x = ",map_size_x
    print "map size y = ",map_size_y
    
    image_aspect = float(options['width'])/float(options['height'])
    map_aspect = map_size_x/map_size_y

    isometric_angle = 20 # in °
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
""".format(middle_x=avg(left,right),middle_z=avg(bottom,top),zoom=zoom,angle=isometric_angle,aspect=map_aspect))
    f.write("""
/* ground */
box {{
    <{0}, -0.5, {1}>, <{2}, -0.2, {3}>
    texture {{
        pigment {{
            /* color rgb <1, 1, 1> */
            image_map {{
                png "scene-osray-landuse-texture.png"
                map_type 0
                interpolate 2
            }}
        }}
        rotate <90,0,0>
        scale <{4},1,{5}>
        translate <{0},0,{1}>
    }}
    finish {{
        specular 0.5
        roughness 0.05
        ambient 0.2
        /*reflection 0.5*/
    }}
}}
/* sky */
sky_sphere {{
    pigment {{
        gradient y
        color_map {{
            [ 0.5 color CornflowerBlue ]
            [ 1.0 color MidnightBlue ]
        }}
        scale 20
        translate -10
    }}
}}
""".format(left,bottom,right,top,map_size_x,map_size_y))

    #f.write("""light_source {{ <{0}, 50000, {1}>, rgb <0.5, 0.5, 0.5> }}\n""".format(avg(left*1.5,right*0.5),avg(bottom*1.5,top*0.5)))
    #f.write("""light_source {{ <{0}, 5000, {1}>, rgb <0.5, 0.5, 0.5> }}\n""".format(avg(left*0.5,right*1.5),avg(bottom*1.5,top*0.5)))
    #f.write("""light_source {{ <300000+{0}, 1000000, -1000000+{1}>, rgb <1, 1, 1> fade_power 0 }}\n""".format(avg(left,right),avg(bottom,top)))
    f.write("""
light_source {{
    <0, 1000000,0>,
    rgb <1, 1, 0.9>
    area_light <100000, 0, 0>, <0, 0, 100000>, 8, 8
    adaptive 1
    circular
    rotate <-45,-10,0>
    translate <{0},0,{1}>
}}
\n""".format(avg(left,right),avg(bottom,top)))


def main(options):
    print "In main(). Options: ",options

    create_textures = not(options['quick'])
    osraydb = OsrayDB(options)

    f = open('/home/kayd/workspace/Parking/osray/scene-osray.pov', 'w')

    pov_globals(f)
    pov_camera(f,osraydb,options)
    
    if create_textures:
        create_landuse_texture(osraydb,options,'/home/kayd/workspace/Parking/osray/scene-osray-landuse-texture.pov')
        #pass
    
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
    
    buildings = []
    for buildingtype in buildingtypes.iterkeys():
        buildings += osraydb.select_buildings(buildingtype)
    
    for building in buildings:
        pov_building(f,building)
    
    f.close()
    
    image_dimension_parameters = "-W"+str(options['width'])+" -H"+str(options['height'])
    result = commands.getstatusoutput('povray -Iscene-osray.pov -UV '+image_dimension_parameters+' +Q9 +A')
    print result[1]
    osraydb.shutdown()


if __name__ == "__main__":
    parser = OptionParser()
    parser.add_option("-d", "--dsn", dest="dsn", help="database name and host (e.g. 'dbname=gis')", default="dbname=gis")
    parser.add_option("-p", "--prefix", dest="prefix", help="database table prefix (e.g. 'planet_osm')", default="planet_osm")
    parser.add_option("-q", "--quick", action="store_true", dest="quick", default=False, help="don't generate textures, but use existing ones")
    parser.add_option("-b", "--bbox", dest="bbox", help="the bounding box as string ('9.94861 49.79293,9.96912 49.80629')", default="9.94861 49.79293,9.96912 49.80629")
    parser.add_option("-H", "--height", dest="height", help="image height in pixels", default="768", type='int')
    parser.add_option("-W", "--width", dest="width", help="image width in pixels", default="1024", type='int')
    parser.add_option("-Q", "--high-quality", action="store_true", dest="hq", default=False, help="high quality rendering")
    (options, args) = parser.parse_args()
    #if options.a and options.b:
    #    parser.error("options -a and -b are mutually exclusive")
    print options
    main(options.__dict__)
    sys.exit(0)
