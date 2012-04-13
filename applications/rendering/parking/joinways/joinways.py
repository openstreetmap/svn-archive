# -*- coding: utf-8 -*-
# by kay

import sys
import string
#import psycopg2
from osmdb import OSMDB
from optparse import OptionParser

class JoinDB (OSMDB):

    def get_highways_segments(self):
        #self.curs.execute("SELECT osm_id,"+latlon+",\"parking:condition:"+side+":maxstay\","+coords+",'"+side+"' "+FW+" \"parking:condition:"+side+":maxstay\" is not NULL and \"parking:condition:"+side+"\"='disc'")
        result=[]
        #self.curs.execute("select osm_id,name from planet_line where \"way\" && SetSRID('BOX3D(1101474.25471931 6406603.879863935,1114223.324055468 6415715.307134068)'::box3d, 900913)")
        #print "result for bbox("+self.googbox+")"
        #print "select osm_id,name "+self.FlW+" \"way\" && "+self.googbox+""
        #self.curs.execute("select osm_id,name from planet_line where \"way\" && "+self.googbox+"")
        self.curs.execute("select name,string_agg(text(osm_id),',') "+self.FlW+" highway is not Null and \"way\" && "+self.googbox+" and name is not Null group by name")
        result += self.curs.fetchall()
        highways=[]
        for hw,osmids in result:
            ids=osmids.split(',')
            highways.append([hw,ids])

    def get_next_pending_highway(self):
        result=[]
        # FIXME: bbox is to be removed later
        self.curs.execute("select osm_id,highway,name,ST_AsText(\"way\") AS geom "+self.FlW+" jrhandled is False and highway is not Null and \"way\" && "+self.googbox+" and name is not Null limit 1")
        result += self.curs.fetchall()
        if len(result)==0:
            raise BaseException("No pending highway found (this should not be an assert)")
        res=result[0]
        highway = {}
        highway['osm_id']=res[0]
        highway['highway']=res[1]
        highway['name']=res[2]
        highway['coords']=WKT_to_line(res[3])
        return highway
    

    def get_joined_ways(self,segment_ids):
        result=[]
        self.curs.execute("select st_linemerge(st_collect(way)) "+self.FlW+" osm_id in ("+string.join(segment_ids,',')+");")
        result += self.curs.fetchall()
        print "jw-result = "+str(result)
        return result[0][0]

    def insert_joined_highway(self,id,name,highway,way):
        #self.curs.execute("SELECT osm_id,"+latlon+",\"parking:condition:"+side+":maxstay\","+coords+",'"+side+"' "+FW+" \"parking:condition:"+side+":maxstay\" is not NULL and \"parking:condition:"+side+"\"='disc'")
        result=[]
        #self.curs.execute("select osm_id,name from planet_line where \"way\" && SetSRID('BOX3D(1101474.25471931 6406603.879863935,1114223.324055468 6415715.307134068)'::box3d, 900913)")
        print "insert into planet_line_join (join_id, name, highway, way) values ('"+id+"','"+name+"','"+highway+"',SetSrid('"+way+"'::Text,4326));"
        self.curs.execute("insert into planet_line_join (join_id, name, highway, way) values ('"+id+"','"+name+"','"+highway+"',SetSrid('"+way+"'::Text,4326))")

    def what_is_it(self,geom):
        result=[]
        self.curs.execute("select astext(setsrid('"+geom+"'::Text,4326))")
        result += self.curs.fetchall()
        itisa=result[0][0]
        itisa=itisa.split('(')[0]
        print "whatisit-result = "+itisa
        return itisa
        
        

"""
'Kittelstra\xc3\x9fe', '36717484,36717485,5627159'

create table planet_line_join (join_id integer , name text, highway text);
select AddGeometryColumn('planet_line_join', 'way', 4326, 'LINESTRING', 2 );

"""

def main2(options):
    bbox = options['bbox']
    DSN = options['dsn']
    print bbox
    osmdb = JoinDB(DSN)
    #highways = getHighwaysInBbox(DSN,bbox)
    bxarray=bbox.split(",")
    bbox="{b} {l},{t} {r}".format(b=bxarray[0],l=bxarray[1],t=bxarray[2],r=bxarray[3])
    osmdb.set_bbox(bbox)
    highways=osmdb.get_highways_segments()
    
    for hw in highways:
        hwname = hw[0]
        hwsegments = hw[1]
        hwjoinedway = osmdb.get_joined_ways(hwsegments)
        print "* Highway "+hwname+": "+hwjoinedway
        itisa = osmdb.what_is_it(hwjoinedway)
        if itisa=='LINESTRING':
            osmdb.insert_joined_highway(str(0),hwname,"residential",hwjoinedway)
        else:
            print "not handled yet: "+hwname
#        break

def main(options):
    bbox = options['bbox']
    DSN = options['dsn']
    print bbox
    osmdb = JoinDB(DSN)
    #highways = getHighwaysInBbox(DSN,bbox)
    bxarray=bbox.split(",")
    bbox="{b} {l},{t} {r}".format(b=bxarray[0],l=bxarray[1],t=bxarray[2],r=bxarray[3])
    osmdb.set_bbox(bbox)
    highway=osmdb.get_next_pending_highway()
    print highway

#        break

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-c", "--command", dest="command", help="The command to execute. Default is update. Possible values are update, install, clear", default="update")
    parser.add_option("-b", "--bbox", dest="bbox", help="bounding box to restrict to", default="")
    parser.add_option("-d", "--dsn", dest="dsn", help="DSN, default is 'dbname=gis host=crite'", default="dbname=gis host=crite")
    (options, args) = parser.parse_args()
    print options
    main(options.__dict__)
    sys.exit(0)
