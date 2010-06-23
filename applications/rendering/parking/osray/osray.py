# -*- coding: utf-8 -*-
# by kay drangmeister

import sys
import commands
import psycopg2
import csv
import re
from numpy import *
from osray_ground import *
from osray_streets import *
from optparse import OptionParser


def avg(a,b): return (a+b)/2.0

def pov_globals(f):
    globsettings = """
global_settings {{
    assumed_gamma 1.5
    noise_generator 2
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

def pov_camera(f,bbox):
    polygonstring = bbox[0][0]
    polygonstring = polygonstring[9:] # cut off the "POLYGON(("
    polygonstring = polygonstring[:-2] # cut off the "))"
    print polygonstring
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

    print bottom,left,top,right
    zoom = 0.4
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
}}
""".format(left,bottom,right,top,(right-left)/1,(top-bottom)/1))

    #f.write("""light_source {{ <{0}, 50000, {1}>, rgb <0.5, 0.5, 0.5> }}\n""".format(avg(left*1.5,right*0.5),avg(bottom*1.5,top*0.5)))
    #f.write("""light_source {{ <{0}, 5000, {1}>, rgb <0.5, 0.5, 0.5> }}\n""".format(avg(left*0.5,right*1.5),avg(bottom*1.5,top*0.5)))
    f.write("""light_source {{ <300000+{0}, 1000000, -1000000+{1}>, rgb <1, 1, 1> }}\n""".format(avg(left,right),avg(bottom,top)))




if __name__ == "__main__":
    parser = OptionParser()
    parser.add_option("-d", "--dsn", dest="dsn", help="database name and host (e.g. 'dbname=gis')", default="dbname=gis")
    parser.add_option("-p", "--prefix", dest="prefix", help="database table prefix (e.g. 'planet_osm')", default="planet_osm")
    parser.add_option("-q", "--quick", action="store_true", dest="quick", default=False, help="don't generate textures, but use existing ones")
    parser.add_option("-b", "--bbox", dest="bbox", help="the bounding box as string ('9.94861 49.79293,9.96912 49.80629')", default="9.94861 49.79293,9.96912 49.80629")
    parser.add_option("-H", "--height", dest="height", help="image height in pixels", default="768", type='int')
    parser.add_option("-W", "--width", dest="width", help="image width in pixels", default="1024", type='int')
    (options, args) = parser.parse_args()
    #if options.a and options.b:
    #    parser.error("options -a and -b are mutually exclusive")
    print options
    main(options)
    sys.exit(0)

def main(options):
    DSN = options.dsn
    thebbox = options.bbox
    prefix = options.prefix
    create_textures = not(options.quick)

    print "Opening connection using dsn:", DSN
    conn = psycopg2.connect(DSN)
    print "Encoding for this connection is", conn.encoding
    curs = conn.cursor()

    f = open('/home/kayd/workspace/Parking/osray/scene-osray.pov', 'w')

    """
    SELECT ST_AsText(transform("way",4326)) AS geom
    FROM planet_osm_line
    WHERE "way" && transform(SetSRID('BOX3D(9.92498 49.78816,9.93955 49.8002)'::box3d,4326),900913)
    LIMIT 10;
    
    SELECT highway,ST_AsText(transform("way",4326)) AS geom
    FROM planet_osm_line
    WHERE "way" && transform(SetSRID('BOX3D(9.92498 49.78816,9.93955 49.8002)'::box3d,4326),900913)
    and highway='secondary' LIMIT 50;
    """
    
    latlon= 'ST_Y(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),ST_X(ST_Transform(ST_line_interpolate_point(way,0.5),4326))'
    coords= "ST_Y(ST_line_interpolate_point(way,0.5)) as py,ST_X(ST_line_interpolate_point(way,0.5)) as px,ST_Y(ST_line_interpolate_point(way,0.49)) as qy,ST_X(ST_line_interpolate_point(way,0.49)) as qx,ST_Y(ST_line_interpolate_point(way,0.51)) as ry,ST_X(ST_line_interpolate_point(way,0.51)) as rx"
    FlW = "FROM "+prefix+"_line WHERE"
    FpW = "FROM "+prefix+"_polygon WHERE"
    
    #thebbox = "9.94861 49.79293,9.96912 49.80629"
    googbox = "transform(SetSRID('BOX3D("+thebbox+")'::box3d,4326),900913)"
    
    #curs.execute("SELECT ST_AsText(transform(SetSRID('BOX3D(9.92498 49.78816,9.93955 49.8002)'::box3d,4326),900913)) AS geom")
    curs.execute("SELECT ST_AsText("+googbox+") AS geom")
    
    bbox = curs.fetchall()
    pov_globals(f)
    pov_camera(f,bbox)
    
    if create_textures:
        create_landuse_texture(conn,curs)
    
    highways = []
    for highwaytype in highwaytypes.iterkeys():
        curs.execute("SELECT osm_id,highway,ST_AsText(\"way\") AS geom, tags->'lanes' as lanes, tags->'layer' as layer, tags->'oneway' as oneway "+FlW+" \"way\" && "+googbox+" and highway='"+highwaytype+"' LIMIT 2000;")
        highways += curs.fetchall()
    
    for highway in highways:
        pov_highway(f,highway)
        #pass
    
    buildings = []
    for buildingtype in buildingtypes.iterkeys():
        curs.execute("SELECT osm_id,building,ST_AsText(\"way\") AS geom, tags->'height' as height,amenity "+FpW+" \"way\" && "+googbox+" and building='"+buildingtype+"' LIMIT 1700;")
        buildings += curs.fetchall()
    
    for building in buildings:
        pov_building(f,building)
    
    f.close()
    
    conn.rollback()

    image_dimension_parameters = "-W"+string(options.width)+" -H"+string(options.height)
    print commands.getstatusoutput('povray -Iscene-osray.pov -UV '+image_dimension_parameters+' +Q9 +A')
    
