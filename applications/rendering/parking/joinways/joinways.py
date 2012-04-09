# -*- coding: utf-8 -*-
# by kay

import sys
import psycopg2
from optparse import OptionParser

class OSMDB:
    """ OSM Database """
    DSN = None
    conn = None
    curs = None

    def __init__( self, dsn = None ):
        self.DSN = dsn
        self.conn = psycopg2.connect(self.DSN)
        print "Encoding for this connection is", self.conn.encoding
        self.curs = self.conn.cursor()
    def __del__(self):
        print "Closing connection"
        self.conn.rollback()
        self.conn.close()
    def dummy(self,bbox):
        #self.curs.execute("SELECT osm_id,"+latlon+",\"parking:condition:"+side+":maxstay\","+coords+",'"+side+"' "+FW+" \"parking:condition:"+side+":maxstay\" is not NULL and \"parking:condition:"+side+"\"='disc'")
        result=[]
        #self.curs.execute("select osm_id,name from planet_line where \"way\" && SetSRID('BOX3D(1101474.25471931 6406603.879863935,1114223.324055468 6415715.307134068)'::box3d, 900913)")
        print "result for bbox("+bbox+")"
        self.curs.execute("select osm_id,name from planet_line where \"way\" && SetSRID('BOX3D("+bbox+")'::box3d, 4326)")
        result += self.curs.fetchall()
        print "resultlen="+len(result)

def main(options):
    bbox = options['bbox']
    DSN = options['dsn']
    print bbox
    osmdb = OSMDB(DSN)
    #highways = getHighwaysInBbox(DSN,bbox)
    osmdb.dummy(bbox)

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-b", "--bbox", dest="bbox", help="bounding box to restrict to", default="")
    parser.add_option("-d", "--dsn", dest="dsn", help="DSN, default is 'dbname=gis host=crite'", default="dbname=gis host=crite")
    (options, args) = parser.parse_args()
    print options
    main(options.__dict__)
    sys.exit(0)
