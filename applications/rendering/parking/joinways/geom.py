# -*- coding: utf-8 -*-
# by kay drangmeister

import psycopg2
from numpy import *
from osray_geom import *

LIMIT = 'LIMIT 5000'

class bbox:

    bbox = None
    clientsrs='4326'
    dbsrs='900913'

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
        #self.googbox = "transform(SetSRID('BOX3D("+thebbox+")'::box3d,4326),900913)"
        #self.curs.execute("SELECT ST_AsText("+self.googbox+") AS geom")
        #self.bbox = self.curs.fetchall()
        #self.get_bounds()
        self.clientsrs = options.get('srs','4326')
        if self.clientsrs=='4326':
            self.init_bbox_4326(options['bbox'])
        elif self.clientsrs=='3857':
            self.init_bbox_3857(options['bbox'])
        elif self.clientsrs=='900913':
            self.init_bbox_3857(options['bbox'])


    def init_bbox_srs(self,bbox,srs):
        self.bbox = "transform(SetSRID('BOX3D("+bbox+")'::box3d,"+srs+"),900913)"
#        self.curs.execute("SELECT ST_AsText("+self.googbox+") AS geom")
#        self.bbox = self.curs.fetchall()
#        self.get_bounds()

    def init_bbox_4326(self,bbox):
        self.init_bbox_srs(bbox, '4326')

    def init_bbox_3857(self,bbox): # 900913 Projection
        self.init_bbox_srs(bbox, '3857')
        #self.googbox = "'BOX3D("+bbox+")'::box3d"
        #self.curs.execute("SELECT ST_AsText("+self.googbox+") AS geom")
        #self.bbox = self.curs.fetchall()
        #self.get_bounds()

    def get_bbox_sql(self):
       return self.bbox
