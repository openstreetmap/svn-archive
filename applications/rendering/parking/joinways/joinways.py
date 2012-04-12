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
        print "result for bbox("+self.googbox+")"
        print "select osm_id,name "+self.FlW+" \"way\" && "+self.googbox+""
        #self.curs.execute("select osm_id,name from planet_line where \"way\" && "+self.googbox+"")
        self.curs.execute("select name,string_agg(text(osm_id),',') from planet_line where highway is not Null and \"way\" && "+self.googbox+" and name is not Null group by name")
        result += self.curs.fetchall()
        highways=[]
        for hw,osmids in result:
            ids=osmids.split(',')
            highways.append([hw,ids])
        
#        print "resultlen={l}".format(l=len(result))
#        print "result={r}".format(r=result)
        return highways

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
#        print "insert into planet_line_join (join_id, name, highway, way) values ("+id+","+name+","+highway+","+way+");"
        self.curs.execute("insert into planet_line_join (join_id, name, highway, way) values ('"+id+"','"+name+"','"+highway+"','"+way+"')")


"""
'Kittelstra\xc3\x9fe', '36717484,36717485,5627159'

create table planet_line_join (join_id integer , name text, highway text, way geometry);

"""

def main(options):
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
        osmdb.insert_joined_highway(str(0),hwname,"residential",hwjoinedway)
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
