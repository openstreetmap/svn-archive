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
        #print "select osm_id,name "+self.FlW+" \"way\" && "+self.googbox+""
        result=self.select("select name,string_agg(text(osm_id),',') "+self.FlW+" highway is not Null and \"way\" && "+self.googbox+" and name is not Null group by name")
        highways=[]
        for hw,osmids in result:
            ids=osmids.split(',')
            highways.append([hw,ids])

    def _escape_quote(self,name):
        return name.replace("'","''")

    def find_same_named_highways(self,highway,bbox):
        """ Finds - within the small bbox - the highways with the same name. Returns dictionary with osm_id as key. """
        # print "select osm_id,highway,name,ST_AsText(\"way\") AS geom {FlW} highway is not Null and \"way\" && '{bbox}'::BOX2D and name='{name}'".format(FlW=self.FlW,bbox=bbox,name=highway['name'])
        rs=self.select("select osm_id,highway,name,ST_AsText(\"way\") AS geom {FlW} highway is not Null and \"way\" && '{bbox}'::BOX2D and name='{name}'".format(FlW=self.FlW,bbox=bbox,name=self._escape_quote(highway['name'])))
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
        #print "Get Next Pending Highway: sql={s}".format(s=select)
        result=self.select(select)
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
        """ check and collect iteratively if a same-named highway is in an expanded bbox around the current highway """
        old_bbox=""
        collated_highways={}
        collated_highways[highway['osm_id']]=highway
        current_bbox=self.get_expanded_bbox(highway['geom'],10.0)

        i=0
        while current_bbox != old_bbox:
            old_bbox = current_bbox
            collated_highways.update(self.find_same_named_highways(highway,current_bbox))

            the_joined_way=self.get_joined_ways(collated_highways.keys())
            current_bbox=self.get_expanded_bbox(the_joined_way,10.0)
            i+=1

        #print "-> Found {n} highway segments in {i} iterations. Joined way is {w}".format(n=len(collated_highways),i=i,w=the_joined_way)
        return collated_highways,the_joined_way

    def get_expanded_bbox(self,geom,meter):
        """ returns a bbox expanded by meter """
        return self.select_one("select ST_Expand(cast(ST_Extent('{geom}') as BOX2D),{meter})".format(geom=geom,meter=meter))

    def get_joined_ways(self,segment_ids):
        """ Get a joined way (likely a LINESTRING, but possibly a MULTILINESTRING) for the set of osm_ids (int) given """
        segment_ids_as_strings=map(lambda osmid: str(osmid),segment_ids)
        return self.select_one("select ST_Linemerge(ST_Collect(way)) {FlW} osm_id in ({seglist})".format(FlW=self.FlW,seglist=string.join(segment_ids_as_strings,',')))

    def _insert_joined_highway(self,id,name,highway,way):
        """ adds the joined highway (it may be a MULTILINE feature) to the jr tables. returns (just for info) the number of written ways (>1 if a MULTILINESTRING) """
        if self._which_geometry_is_it(way)=="LINESTRING":
            #print "inserting a simple way"
            self.insert("insert into planet_line_join (join_id, name, highway, way) values ('"+id+"','"+self._escape_quote(name)+"','"+highway+"',SetSrid('"+way+"'::Text,4326))")
            return 1
        else:
            #print "inserting a MULTILINE way"
            ways = self._split_multiline_way(way)
            for one_way in ways:
                self.insert("insert into planet_line_join (join_id, name, highway, way) values ('"+id+"','"+self._escape_quote(name)+"','"+highway+"',SetSrid('"+one_way+"'::Text,4326))")
            return len(ways)


    def _insert_segment_into_joinmap(self,join_id,segment_id):
        """ adds a segment to the jr tables """
        self.insert("insert into planet_line_joinmap (join_id, segment_id) values ('{jid}','{sid}')".format(jid=join_id,sid=segment_id))

    def _mark_segment_as_handled(self,segment_id):
        """ Mark the given segment (by osm_id) as handled in the jr tables """
        self.update("update planet_line set jrhandled=True where osm_id={oid}".format(oid=segment_id))


    def _which_geometry_is_it(self,geom):
        """ Returns the WKT type of the geom, e.g. LINESTRING or MULTILINESTRING """
        itisa = self.select_one("select ST_AsText(ST_SetSRID('"+geom+"'::Text,4326))")
        itisa = itisa.split('(')[0]
        # print "whatisit-result = "+itisa
        return itisa

    def _split_multiline_way(self,multilineway):
        """ split MULTILINESTRING multilineway into array of ways """
        ways=[]
        i=1
        while True:
            way = self.select_one("select ST_GeometryN(ST_SetSRID('{way}'::Text,4326),{i})".format(way=multilineway,i=i))
            #print "way[{i}]={w}".format(i=i,w=way)
            if way==None:
                break
            ways.append(way)
            i += 1
        return ways



    def add_join_highway(self,highway,joinset,joinway):
        """ Add the highway into the jr tables, handle all flagging """
        join_id = highway['osm_id']
        #print "*** Adding '{name}' ({id}) to planet_line_join".format(name=highway['name'],id=join_id)
        numjoins = self._insert_joined_highway(str(join_id),highway['name'],highway['highway'],joinway)
        #print "(joinset={j})".format(j=joinset)
        for segment_id in joinset.keys():
            #print "  * segment is {s}".format(s=joinset[segment_id])
            self._insert_segment_into_joinmap(join_id,segment_id)
            self._mark_segment_as_handled(segment_id)
        return numjoins

    def clear_planet_line_join(self,bboxobj=None):
        print "*** clearing jr tables and flags"
        self.delete("delete from planet_line_join")
        self.delete("delete from planet_line_joinmap")
        if bboxobj!=None:
            bbox_condition_sql = '"way" && {b} and '.format(b=bboxobj.get_bbox_sql())
        else:
            bbox_condition_sql = ''
        update = "update planet_line set jrhandled=False where {bbox} jrhandled is True".format(bbox=bbox_condition_sql)
        self.update(update)


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
        #print "Found {i}. pending highway '{name}'".format(i=i,name=highway['name'])
        joinset,joinway=osmdb.collate_highways(highway)
        # print "  Found connected highways '{hws}'".format(hws=joinset)
        numjoins = osmdb.add_join_highway(highway,joinset,joinway)
        if i%100==0:
            osmdb.commit()
        print "Joined {i}. Highway '{name}': {segs} segments -> {numjoins} joined segments".format(i=i,name=highway['name'],segs=len(joinset),numjoins=numjoins)
    osmdb.commit()
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
