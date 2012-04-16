# -*- coding: utf-8 -*-
# by kay drangmeister

class bbox:

    bbox = None
    clientsrs='4326'
    dbsrs='900913'

    def box_coords_ccc2bcb(self,box_coords):
        """ converts box coordinates from format "b,l,t,r" (3 commas) to format "b l,t r" (blank comma blank) """
        bxarray=box_coords.split(",")
        return "{b} {l},{t} {r}".format(b=bxarray[0],l=bxarray[1],t=bxarray[2],r=bxarray[3])
        

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
        """ parameters: bbox coords in ccc format, srs as string """
        #self.googbox = "transform(SetSRID('BOX3D("+thebbox+")'::box3d,4326),900913)"
        #self.curs.execute("SELECT ST_AsText("+self.googbox+") AS geom")
        #self.bbox = self.curs.fetchall()
        #self.get_bounds()
        self.clientsrs = options.get('srs','4326')
        box_coords_ccc=options['bbox']
        print "box_coords_ccc={bc}".format(bc=box_coords_ccc)
        box_coords_bcb=self.box_coords_ccc2bcb(box_coords_ccc)
        if self.clientsrs=='4326':
            self.init_bbox_4326(box_coords_bcb)
        elif self.clientsrs=='3857':
            self.init_bbox_3857(box_coords_bcb)
        elif self.clientsrs=='900913':
            self.init_bbox_3857(box_coords_bcb)


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
