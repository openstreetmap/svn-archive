# -*- coding: utf-8 -*-
# by kay - basic functions

### config for Toolserver
#DSN = 'dbname=osm_mapnik host=sql-mapnik'
#openlayertextfilename = '/home/kayd/parkingicons/parkingicons.txt'
### config for devserver
#DSN = 'dbname=hstore'
#openlayertextfilename = '/osm/parking/parkingicons/parkingicons.txt'

import sys
import psycopg2
import csv
from numpy import math,angle

if __name__ == '__main__':
    if len(sys.argv) == 3:
        DSN = sys.argv[1]
        openlayertextfilename = sys.argv[2]
    else:
        print "usage: {cmd} <db connection string> <output filename>".format(cmd=sys.argv[0])
        exit(0);

    print "Opening connection using dns:", DSN
    conn = psycopg2.connect(DSN)
    print "Encoding for this connection is", conn.encoding
    curs = conn.cursor()

    openlayertextfile = csv.writer(open(openlayertextfilename, 'w'), delimiter='\t',quotechar='"', quoting=csv.QUOTE_MINIMAL)
    openlayertextfile.writerow(['lat','lon','title','description','icon','iconSize','iconOffset'])

    latlon= "ST_Y(ST_Transform(way,4326)),ST_X(ST_Transform(way,4326))"
    FW = "FROM planet_point WHERE"

    pc_disc_maxstay = []
    curs.execute("SELECT osm_id,"+latlon+",(tags->'espresso') as \"espresso\",amenity,name "+FW+" (tags @> 'espresso=>yes')")
    pc_disc_maxstay += curs.fetchall()

    amenities={'shop':'Shop','cafe':'Cafè','restaurant':'Restaurant'}
    for pc_dm in pc_disc_maxstay:
        amenity = pc_dm[4]
        if amenity==None:
            amenity='?'
        amenity = amenities.get(amenity,'"'+amenity+'"')
        name = pc_dm[5]
        if name==None:
            name='<no name>'
        id = str(pc_dm[0])
        description_html='{am} <a href="http://www.openstreetmap.org/browse/node/{id}">{name}</a>'.format(am=amenity,name=name,id=id)
        openlayertextfile.writerow([pc_dm[1],pc_dm[2],'Espresso',description_html,'espressoicons/espresso16.png','16,16','-8,-8'])

    conn.rollback()
    sys.exit(0)
