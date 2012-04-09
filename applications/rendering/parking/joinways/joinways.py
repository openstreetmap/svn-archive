# -*- coding: utf-8 -*-
# by kay - basic functions

### config for Toolserver
#DSN = 'dbname=osm_mapnik host=sql-mapnik'
### config for devserver
#DSN = 'dbname=hstore'
### config for Crite
#DSN = 'dbname=gis host=crite'

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
    def dummy(self):
        print "dummy"
def main_approach(options):
    bbox = options['bbox']
    DSN = options['dsn']
    print bbox
    osmdb = OSMDB(DSN)
    #highways = getHighwaysInBbox(DSN,bbox)
    osmdb.dummy()

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-b", "--bbox", dest="bbox", help="bounding box to restrict to", default="")
    parser.add_option("-d", "--dsn", dest="dsn", help="DSN, default is 'dbname=gis host=crite'", default="dbname=gis host=crite")
    (options, args) = parser.parse_args()
    print options
    main(options.__dict__)
    sys.exit(0)
