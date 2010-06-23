# -*- coding: utf-8 -*-
# by kay drangmeister

#import sys
#import commands
import psycopg2
#import csv
#import re
from numpy import *
#from osray_ground import *
#from osray_streets import *
#from optparse import OptionParser

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

def get_bounds():
    polygonstring = bbox[0][0]
    polygonstring = polygonstring[9:] # cut off the "POLYGON(("
    polygonstring = polygonstring[:-2] # cut off the "))"
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


def setup(options):
    DSN = options['dsn']
    thebbox = options['bbox']
    prefix = options['prefix']

    print "Opening connection using dsn:", DSN
    conn = psycopg2.connect(DSN)
    print "Encoding for this connection is", conn.encoding
    curs = conn.cursor()

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
    
    googbox = "transform(SetSRID('BOX3D("+thebbox+")'::box3d,4326),900913)"
    curs.execute("SELECT ST_AsText("+googbox+") AS geom")
    bbox = curs.fetchall()
    get_bounds()

def select_highways(highwaytype):
    curs.execute("SELECT osm_id,highway,ST_AsText(\"way\") AS geom, tags->'lanes' as lanes, tags->'layer' as layer, tags->'oneway' as oneway "+FlW+" \"way\" && "+googbox+" and highway='"+highwaytype+"' LIMIT 10000;")
    return curs.fetchall()

def select_buildings(buildingtype):
    curs.execute("SELECT osm_id,building,ST_AsText(\"way\") AS geom, tags->'height' as height,amenity "+FpW+" \"way\" && "+googbox+" and building='"+buildingtype+"' LIMIT 10000;")
    return curs.fetchall()

def shutdown():
    conn.rollback()
