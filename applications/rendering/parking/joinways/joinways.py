# -*- coding: utf-8 -*-
# by kay

import sys
import string
#import psycopg2
from osmdb import OSMDB
from geom import bbox
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

    def find_same_named_highways(self,highway,bbox):
        """ finds - within the small bbox - the highways with the same name. Returns dictionary with osm_id as key. """
#        print "select osm_id,highway,name,ST_AsText(\"way\") AS geom {FlW} highway is not Null and \"way\" && '{bbox}'::BOX2D and name='{name}'".format(FlW=self.FlW,bbox=bbox,name=highway['name'])
        self.curs.execute("select osm_id,highway,name,ST_AsText(\"way\") AS geom {FlW} highway is not Null and \"way\" && '{bbox}'::BOX2D and name='{name}'".format(FlW=self.FlW,bbox=bbox,name=highway['name']))
        rs = self.curs.fetchall()
        highways = {}
        for res in rs:
            highway = {}
            highway['osm_id']=res[0]
            highway['highway']=res[1]
            highway['name']=res[2]
            highway['geom']=res[3]
            highways[highway['osm_id']]=highway
        return highways


    def get_next_pending_highway(self,bboxobj=None):
        """ Gets the next unhandled highway (osm_id+dict) """
        if bboxobj!=None:
            bbox_condition_sql = '"way" && {b} and '.format(b=bboxobj.get_bbox_sql())
        else:
            bbox_condition_sql = ''
        select = "select osm_id,highway,name,ST_AsText(\"way\") AS geom {FlW} jrhandled is False and highway is not Null and {bbox} name is not Null limit 1".format(FlW=self.FlW,bbox=bbox_condition_sql)
        print "Get Next Pending Highway: sql={s}".format(s=select)
        self.curs.execute(select)
        result = self.curs.fetchall()
        if len(result)==0:
            return None
            # raise BaseException("No pending highway found (this should not be an assert)")
        res=result[0]
        highway = {}
        highway['osm_id']=res[0]
        highway['highway']=res[1]
        highway['name']=res[2]
        highway['geom']=res[3]
        return highway

    def collate_highways(self,highway):
        old_bbox=""
        collated_highways={}
        collated_highways[highway['osm_id']]=highway
#        print "  collated_highways_0={ch}".format(ch=collated_highways)

#        all_osm_ids_of_collated_highways=map(lambda osmid: str(osmid),collated_highways.keys())
#        current_geom=self.get_joined_ways(all_osm_ids_of_collated_highways)
#        current_bbox=self.get_expanded_bbox(current_geom,10.0)
        current_bbox=self.get_expanded_bbox(highway['geom'],10.0)
        print "    current_bbox={bb}".format(bb=current_bbox)

        i=0
        while current_bbox != old_bbox:
            old_bbox = current_bbox
#            print "current_bbox={bb}".format(bb=current_bbox)
            collated_highways.update(self.find_same_named_highways(highway,current_bbox))
#            print "  collated_highways_{i}={ch}".format(i=i,ch=collated_highways)
 
            all_osm_ids_of_collated_highways=map(lambda osmid: str(osmid),collated_highways.keys())
            the_joined_way=self.get_joined_ways(all_osm_ids_of_collated_highways)
#            print "    current_bbox={bb}".format(bb=current_bbox)
            current_bbox=self.get_expanded_bbox(the_joined_way,10.0)
            i+=1

        print "-> Found {n} highway segments in {i} iterations. Joined way is {w}".format(n=len(collated_highways),i=i,w=the_joined_way)
        return collated_highways,the_joined_way

    def get_expanded_bbox(self,geom,meter):
        """ returns a bbox expanded by meter """
        result=[]
        self.curs.execute("select st_expand(cast(st_extent('{geom}') as box2d),{meter})".format(geom=geom,meter=meter))
        result += self.curs.fetchall()
        return result[0][0]

    def get_joined_ways(self,segment_ids):
        result=[]
        self.curs.execute("select st_linemerge(st_collect(way)) "+self.FlW+" osm_id in ("+string.join(segment_ids,',')+")")
        result += self.curs.fetchall()
#        print "jw-result = "+str(result)
        return result[0][0]

    def _insert_joined_highway(self,id,name,highway,way):
        """ adds the joined highway (it may be a MULTILINE feature) to the jr tables """
        #self.curs.execute("SELECT osm_id,"+latlon+",\"parking:condition:"+side+":maxstay\","+coords+",'"+side+"' "+FW+" \"parking:condition:"+side+":maxstay\" is not NULL and \"parking:condition:"+side+"\"='disc'")
        #result=[]
        #self.curs.execute("select osm_id,name from planet_line where \"way\" && SetSRID('BOX3D(1101474.25471931 6406603.879863935,1114223.324055468 6415715.307134068)'::box3d, 900913)")
        #print "insert into planet_line_join (join_id, name, highway, way) values ('"+id+"','"+name+"','"+highway+"',SetSrid('"+way+"'::Text,4326));"
        if self._which_geometry_is_it(way)=="LINESTRING":
            print "inserting a simple way"
            self.curs.execute("insert into planet_line_join (join_id, name, highway, way) values ('"+id+"','"+name+"','"+highway+"',SetSrid('"+way+"'::Text,4326))")
        else:
            print "inserting a MULTILINE way"
            ways = self._split_multiline_way(way)
            for one_way in ways:
                self.curs.execute("insert into planet_line_join (join_id, name, highway, way) values ('"+id+"','"+name+"','"+highway+"',SetSrid('"+one_way+"'::Text,4326))")


    def _insert_segment_into_joinmap(self,join_id,segment_id):
        """ adds a segment to the jr tables """
        self.curs.execute("insert into planet_line_joinmap (join_id, segment_id) values ('{jid}','{sid}')".format(jid=join_id,sid=segment_id))

    def _mark_segment_as_handled(self,segment_id):
        """ Mark the given segment (by osm_id) as handled in the jr tables """
        self.curs.execute("update planet_line set jrhandled=True where osm_id={oid}".format(oid=segment_id))


    def _which_geometry_is_it(self,geom):
        """ Returns the WKT type of the geom, e.g. LINESTRING or MULTILINESTRING """
        self.curs.execute("select astext(setsrid('"+geom+"'::Text,4326))")
        itisa = self.curs.fetchall()[0][0] # get first (and only) result
        itisa = itisa.split('(')[0]
        # print "whatisit-result = "+itisa
        return itisa

    def _split_multiline_way(self,multilineway):
        """ split MULTILINESTRING multilineway into array of ways """
        ways=[]
        i=1
        while True:
            self.curs.execute("select ST_GeometryN(ST_SetSRID('{way}'::Text,4326),{i})".format(way=multilineway,i=i))
            way = self.curs.fetchall()[0][0] # get first (and only) result
            print "way[{i}]={w}".format(i=i,w=way)
            if way==None:
                break
            ways.append(way)
            i += 1
        return ways



    def add_join_highway(self,highway,joinset,joinway):
        """ Add the highway into the jr tables, handle all flagging """
        join_id = highway['osm_id']
        print "*** Adding '{name}' ({id}) to planet_line_join".format(name=highway['name'],id=join_id)
        self._insert_joined_highway(str(join_id),highway['name'],highway['highway'],joinway)
        print "(joinset={j})".format(j=joinset)
        for segment_id in joinset.keys():
            print "  * segment is {s}".format(s=joinset[segment_id])
            self._insert_segment_into_joinmap(join_id,segment_id)
            self._mark_segment_as_handled(segment_id)

    def clear_planet_line_join(self,bboxobj=None):
        print "*** clearing jr tables and flags"
        self.curs.execute("delete from planet_line_join")
        self.curs.execute("delete from planet_line_joinmap")
        if bboxobj!=None:
            bbox_condition_sql = '"way" && {b} and '.format(b=bboxobj.get_bbox_sql())
        else:
            bbox_condition_sql = ''
        update = "update planet_line set jrhandled=False where {bbox} jrhandled is True".format(bbox=bbox_condition_sql)
        self.curs.execute(update)


"""
'Kittelstra\xc3\x9fe', '36717484,36717485,5627159'

create table planet_line_join (join_id integer , name text, highway text);
select AddGeometryColumn('planet_line_join', 'way', 4326, 'LINESTRING', 2 );

"""


def main(options):
    bboxstr = options['bbox']
    DSN = options['dsn']
    if bboxstr!='':
        bboxobj = bbox({'bbox':bboxstr,'srs':'4326'})
    else:
        bboxobj = None
    print bboxobj
    print bboxobj.get_bbox_sql()

    osmdb = JoinDB(DSN)

    if options['command']=='clear':
	osmdb.clear_planet_line_join()

    i=0
    while True:
        # osmdb.set_bbox(bbox)
        highway=osmdb.get_next_pending_highway(bboxobj)
        if highway==None:
            break
        i+=1
        print "Found {i}. pending highway '{name}'".format(i=i,name=highway['name'])
        joinset,joinway=osmdb.collate_highways(highway)
        # print "  Found connected highways '{hws}'".format(hws=joinset)
        osmdb.add_join_highway(highway,joinset,joinway)
    print "Terminated adding {i} highways".format(i=i)

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-c", "--command", dest="command", help="The command to execute. Default is update. Possible values are update, install, clear", default="update")
    parser.add_option("-b", "--bbox", dest="bbox", help="bounding box to restrict to", default="")
    parser.add_option("-d", "--dsn", dest="dsn", help="DSN, default is 'dbname=gis host=crite'", default="dbname=gis host=crite")
    (options, args) = parser.parse_args()
    print options
    main(options.__dict__)
    sys.exit(0)
