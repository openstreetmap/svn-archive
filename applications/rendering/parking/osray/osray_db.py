# -*- coding: utf-8 -*-
# by kay drangmeister

import psycopg2
from numpy import *

LIMIT = 'LIMIT 10000'

class OsrayDB:

    bbox = None

    def get_bounds(self):
        polygonstring = self.bbox[0][0]
        polygonstring = polygonstring[9:] # cut off the "POLYGON(("
        polygonstring = polygonstring[:-2] # cut off the "))"
        points = polygonstring.split(',')

        numpoints = len(points)
        for i,point in enumerate(points):
            latlon = point.split(' ')
            if (i==0):
                self.left=float(latlon[0])
                self.bottom=float(latlon[1])
            if (i==2):
                self.right=float(latlon[0])
                self.top=float(latlon[1])
                    
        print "Bounds [b l t r] = ",self.bottom,self.left,self.top,self.right

    def __init__(self,options):
        DSN = options['dsn']
        #thebbox = options['bbox']
        prefix = options['prefix']

        print "Opening connection using dsn:", DSN
        self.conn = psycopg2.connect(DSN)
        print "Encoding for this connection is", self.conn.encoding
        self.curs = self.conn.cursor()

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

        self.latlon= 'ST_Y(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),ST_X(ST_Transform(ST_line_interpolate_point(way,0.5),4326))'
        self.coords= "ST_Y(ST_line_interpolate_point(way,0.5)) as py,ST_X(ST_line_interpolate_point(way,0.5)) as px,ST_Y(ST_line_interpolate_point(way,0.49)) as qy,ST_X(ST_line_interpolate_point(way,0.49)) as qx,ST_Y(ST_line_interpolate_point(way,0.51)) as ry,ST_X(ST_line_interpolate_point(way,0.51)) as rx"
        self.FlW = "FROM "+prefix+"_line WHERE"
        self.FpW = "FROM "+prefix+"_polygon WHERE"
    
        #self.googbox = "transform(SetSRID('BOX3D("+thebbox+")'::box3d,4326),900913)"
        #self.curs.execute("SELECT ST_AsText("+self.googbox+") AS geom")
        #self.bbox = self.curs.fetchall()
        #self.get_bounds()
        srs = options['srs']
        if srs==None:
            srs = '4326'
        if srs=='4326':
            self.init_bbox_4326(options['bbox'])
        elif srs=='3857':
            self.init_bbox_3857(options['bbox'])
        elif srs=='900913':
            self.init_bbox_3857(options['bbox'])


    def init_bbox_srs(self,bbox,srs):
        self.googbox = "transform(SetSRID('BOX3D("+bbox+")'::box3d,"+srs+"),900913)"
        self.curs.execute("SELECT ST_AsText("+self.googbox+") AS geom")
        self.bbox = self.curs.fetchall()
        self.get_bounds()

    def init_bbox_4326(self,bbox):
        self.init_bbox_srs(bbox, '4326')

    def init_bbox_3857(self,bbox): # 900913 Projection
        self.init_bbox_srs(bbox, '3857')
        #self.googbox = "'BOX3D("+bbox+")'::box3d"
        #self.curs.execute("SELECT ST_AsText("+self.googbox+") AS geom")
        #self.bbox = self.curs.fetchall()
        #self.get_bounds()

    def select_highways(self,highwaytype):
        self.curs.execute("SELECT osm_id,highway,ST_AsText(\"way\") AS geom, tags->'lanes' as lanes, tags->'layer' as layer, tags->'oneway' as oneway "+self.FlW+" \"way\" && "+self.googbox+" and highway='"+highwaytype+"' "+LIMIT+";")
        return self.curs.fetchall()

    def select_highway_areas(self,highwaytype):
        self.curs.execute("SELECT osm_id,highway,ST_AsText(\"way\") AS geom, tags->'height' as height, amenity, ST_AsText(buffer(\"way\",1)) AS geombuffer  "+self.FpW+" \"way\" && "+self.googbox+" and highway='"+highwaytype+"' "+LIMIT+";")
        return self.curs.fetchall()

    def select_buildings(self,buildingtype):
        self.curs.execute("SELECT osm_id,building,ST_AsText(\"way\") AS geom, tags->'height' as height,amenity "+self.FpW+" \"way\" && "+self.googbox+" and building='"+buildingtype+"' "+LIMIT+";")
        return self.curs.fetchall()

    def select_landuse(self,landusetype):
        print "SELECT osm_id,landuse,ST_AsText(\"way\") AS geom "+self.FpW+" \"way\" && "+self.googbox+" and landuse='"+landusetype+"' "+LIMIT+";"
        self.curs.execute("SELECT osm_id,landuse,ST_AsText(\"way\") AS geom "+self.FpW+" \"way\" && "+self.googbox+" and landuse='"+landusetype+"' "+LIMIT+";")
        return self.curs.fetchall()

    def select_waterway(self,waterwaytype):
        self.curs.execute("SELECT osm_id,waterway,ST_AsText(\"way\") AS geom "+self.FpW+" \"way\" && "+self.googbox+" and waterway='"+waterwaytype+"' "+LIMIT+";")
        return self.curs.fetchall()

    def shutdown(self):
        self.conn.rollback()
