# -*- coding: utf-8 -*-
# by kay

import sys,time,string,logging
#import psycopg2
from osmdb import OSMDB
from geom import bbox
from optparse import OptionParser

class JoinDB (OSMDB):

    highway_types={'residential':'r','trunk':'r','trunk_link':'r','primary':'r','primary_link':'r','secondary':'r','secondary_link':'r',
        'tertiary':'r','tertiary_link':'r','living_street':'r','road':'r','service':'r','unclassified':'r','motorway':'r','motorway_link':'r',
        'cycleway':'m','pedestrian':'m','footway':'m','path':'m','raceway':'m','construction':'m','proposed':'m','bridleway':'m','steps':'m','byway':'m','platform':'m','trail':'m',
        'private':'bug','abandoned':'bug','turning_circle':'bug','ter':'bug','undefined':'bug','unsurfaced':'bug','cycleway; footway':'bug','unbuilt':'bug','rest_area':'bug','residential; tertiary; residential':'bug','emergency_access_point':'bug','racetrack':'bug','disused':'bug','minor':'bug','secondary;tertiary':'bug','private road':'bug','residential;steps':'bug',
        'track':'t'}

    def get_highways_segments(self):
        st=time.time()
        sel="select name,string_agg(text(osm_id),',') "+self.FlW+" highway is not Null and \"way\" && "+self.googbox+" and name is not Null group by name"
        result=self.select(sel)
        t=time.time()-st
        logging.debug("{t}s: {sel}".format(t=t,sel=sel))
        highways=[]
        for hw,osmids in result:
            ids=osmids.split(',')
            highways.append([hw,ids])

    def _escape_quote(self,name):
        return name.replace("'","''")

    def find_same_named_highways(self,highway,bbox):
        """ Finds - within the small bbox - the highways with the same name. Returns dictionary with osm_id as key. """
        st=time.time()
        # print "select osm_id,highway,name,ST_AsText(\"way\") AS geom {FlW} highway is not Null and \"way\" && '{bbox}'::BOX2D and name='{name}'".format(FlW=self.FlW,bbox=bbox,name=highway['name'])
        sel="select osm_id,highway,name,ST_AsText(\"way\") AS geom {FlW} highway is not Null and \"way\" && '{bbox}'::BOX2D and name='{name}'".format(FlW=self.FlW,bbox=bbox,name=self._escape_quote(highway['name']))
        rs=self.select(sel)
        t=time.time()-st
        logging.debug("{t:.2f}s: {sel}".format(t=t,sel=sel))
        highways = {}
        for res in rs:
            highway = {}
            highway['osm_id']=res[0]
            highway['highway']=res[1]
            highway['name']=res[2]
            highway['geom']=res[3]
            highways[highway['osm_id']]=highway
        return highways


    def get_next_deleted_highway(self):
        """ Gets the next deleted highway (osm_id) """
        select = "select osm_id from planet_line_join_deleted_segments limit 1"
        highway_osm_id=self.select_one(select)
        return highway_osm_id

    def get_next_pending_highway(self,bboxobj=None):
        """ Gets the next unhandled highway (osm_id+dict) """
        st=time.time()
        if bboxobj!=None:
            bbox_condition_sql = '"way" && {b} and '.format(b=bboxobj.get_bbox_sql())
        else:
            bbox_condition_sql = ''
        select = "select osm_id,highway,name,ST_AsText(\"way\") AS geom {FlW} jrhandled is False and highway is not Null and {bbox} name is not Null order by osm_id limit 1".format(FlW=self.FlW,bbox=bbox_condition_sql)
        result=self.select(select)
        t=time.time()-st
        logging.debug("{t:.2f}s: Get Next Pending Highway: {sel}".format(t=t,sel=select))
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

        w=the_joined_way
        if len(w)>19:
            w=w[:8]+"..."+w[-8:]
        logging.debug("-> Found {n} highway segments in {i} iterations. Joined way is {w}".format(n=len(collated_highways),i=i,w=w))
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
        # just a quick test if highway is valid. (I had instances of highway=')
        if highway not in self.highway_types:
            if highway=="'":
                logging.warn('Illegal highway type for way ({id}): "{ht}" - using "fixme" instead.'.format(id=id,ht=highway))
                highway="fixme"
            else:
                logging.warn('Illegal highway type for way ({id}): "{ht}" - using "fixme" instead.'.format(id=id,ht=highway))
                highway="fixme"
#                raise Exception('Illegal highway type for way ({id}): "{ht}"'.format(id=id,ht=highway))
        if self._which_geometry_is_it(way)=="LINESTRING":
            #print "inserting a simple way"
            #print "insert into planet_line_join (join_id, name, highway, way) values ('"+id+"','"+self._escape_quote(name)+"','"+highway+"',SetSrid('"+way+"'::Text,4326))"
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
        logging.warn("*** clearing jr tables and flags")
        self.delete("delete from planet_line_join")
        self.delete("delete from planet_line_joinmap")
        if bboxobj!=None:
            bbox_condition_sql = '"way" && {b} and '.format(b=bboxobj.get_bbox_sql())
        else:
            bbox_condition_sql = ''
        update = "update planet_line set jrhandled=False where {bbox} jrhandled is True".format(bbox=bbox_condition_sql)
        self.update(update)

    def find_joinway_by_segment(self,segment_id):
        """ Find the join_id of a highway segment. None if none found """
        select="select join_id from planet_line_joinmap where segment_id={seg}".format(seg=segment_id)
        return self.select_one(select)

    def flush_deleted_segment(self,segment_id):
        """ Removes a "deleted highway" from the deleted_segments table. """
        delete="delete from planet_line_join_deleted_segments where osm_id={seg}".format(seg=segment_id)
        self.delete(delete)

    def flush_deleted_segments(self,segment_list):
        """ Removes a list of "deleted highways" from the deleted_segments table. """
        segmentlistsql=self.sql_list_of_ids(segment_list)
        delete="delete from planet_line_join_deleted_segments where osm_id in {seg}".format(seg=segmentlistsql)
        self.delete(delete)

    def get_segments_of_joinway(self,joinway_id):
        select="select segment_id from planet_line_joinmap where join_id={jid}".format(jid=joinway_id)
        segments=self.select_list(select)
        return segments

    def get_name_of_joinway(self,joinway_id):
        select="select name from planet_line_join where join_id={jid}".format(jid=joinway_id)
        return self.select_one(select)

    def mark_segments_unhandled(self,dirtylist):
        dirtylistsql=self.sql_list_of_ids(dirtylist)
        update="update planet_line set jrhandled=False where osm_id in {l}".format(l=dirtylistsql)
        self.update(update)

    def remove_joinway(self,joinway_id):
        delete="delete from planet_line_join where join_id={j}".format(j=joinway_id)
        self.delete(delete)
        delete="delete from planet_line_joinmap where join_id={j}".format(j=joinway_id)
        self.delete(delete)

    def area_of_joinway(self,joinway):
        bbox_of_joinway=self.get_expanded_bbox(joinway,0.0)
        xs,ys,xe,ye=self.coords_from_bbox(bbox_of_joinway)
        dx=abs(xe-xs)
        dy=abs(ye-ys)
        area=dx*dy/1000000.0
        return area

"""
'Kittelstra\xc3\x9fe', '36717484,36717485,5627159'

create table planet_line_join (join_id integer , name text, highway text);
select AddGeometryColumn('planet_line_join', 'way', 4326, 'LINESTRING', 2 );

"""


def main(options):
    bboxstr = options['bbox']
    DSN = options['dsn']
    maxobjects = int(options['maxobjects'])
    if bboxstr!='':
        bboxobj = bbox({'bbox':bboxstr,'srs':'4326'})
        logging.info(bboxobj)
        logging.info(bboxobj.get_bbox_sql())
    else:
        bboxobj = None
        logging.info("No bbox used")

    osmdb = JoinDB(DSN)

    if options['command']=='clear':
	osmdb.clear_planet_line_join()

    #
    # handle deleted highway segments -> mark all joined highway's segments as unhandled in order to have them re-handled
    #
    delestarttime=time.time()
    i=0
    j=0
    while True:
        segment_id=osmdb.get_next_deleted_highway()
        if segment_id==None:
            break
        #print "[] Handling deleted segment {seg}".format(seg=segment_id)
        joinway_id=osmdb.find_joinway_by_segment(segment_id)
        if joinway_id==None:  # deleted segment was not in joined highway -> ignore (FIXME: and warn)
            #print "   [] was not a joinway. Ignoring and flushing."
            osmdb.flush_deleted_segment(segment_id)
            ### FIXME: zur sicherheit in planet_line als dirty markieren (falls vorhanden), oder besser: ASSERT ERROR falls in planet_line vorhanden und jrhandled=True
            continue
        dirtylist=osmdb.get_segments_of_joinway(joinway_id)
        name_of_joinway=osmdb.get_name_of_joinway(joinway_id)
        #print "   [] '{jwname}': list of segments to mark: {l}".format(jwname=name_of_joinway,l=dirtylist)
        # dirty segments must be removed: * from the deleted_segments table, * from the joinmap, * from the join table
        # all of those must fail gracefully if an entry is not there (anymore).
        osmdb.mark_segments_unhandled(dirtylist)
        osmdb.flush_deleted_segments(dirtylist)
        osmdb.remove_joinway(joinway_id)
        i+=1
        j+=len(dirtylist)
        logging.info("Deleted {i}. ({id}) '{jwname}' ({l} segments).".format(i=i,id=segment_id,jwname=name_of_joinway,l=dirtylist))
        if i%100==0:
            osmdb.commit()
        if maxobjects>0 and i>maxobjects:
            break

    osmdb.commit()
    logging.info("Found {i} deleted segments and marked {j} highways as dirty".format(i=i,j=j))
    dele=i
    deletime=time.time()-delestarttime
    delerate=dele/deletime

    #
    # handle unhandled (or dirty) highway segments
    #
    ### FIXME: was passiert, wenn zu einem Segment eins angehängt wird, vorher müssten die alten segmente rausgelöscht werden.
    addstarttime=time.time()
    i=0
    while True:
        ts=time.time()
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
        t=time.time()-ts
        area=osmdb.area_of_joinway(joinway)
        logging.info("Joined {i}. ({id}) '{name}' ({t:.2f}s): {segs} segments -> {numjoins} joined segments [{area:.2f}km²,{tpa:.3f}s/km²]".format(i=i,id=highway['osm_id'],name=highway['name'],t=t,segs=len(joinset),numjoins=numjoins,area=area,tpa=(t/area)))
        if maxobjects>0 and i>maxobjects:
            break
    osmdb.commit()
    add=i
    addtime=time.time()-addstarttime
    addrate=add/addtime

    logging.info("Terminated adding {i} highways".format(i=i))
    logging.warn("Joinways ended: {dele} highways deleted in {deletime:.0f} seconds ({delerate:.2f} d/s), {add} highways added in {addtime:.0f} seconds ({addrate:.2f} a/s)".format(dele=dele,deletime=deletime,delerate=delerate,add=add,addtime=addtime,addrate=addrate))

if __name__ == '__main__':
    logging.basicConfig(format='%(asctime)s %(levelname)s: %(message)s',filename='/home/osm/bin/diffs/logs/joinways2.log',level=logging.INFO)
    parser = OptionParser()
    parser.add_option("-c", "--command", dest="command", help="The command to execute. Default is update. Possible values are update, install, clear", default="update")
    parser.add_option("-b", "--bbox", dest="bbox", help="bounding box to restrict to", default="")
    parser.add_option("-d", "--dsn", dest="dsn", help="DSN, default is 'dbname=gis host=crite'", default="dbname=gis host=crite")
    parser.add_option("-m", "--maxobjects", dest="maxobjects", help="maximum number of objects to treat, default is 50000. Set to 0 for unlimited.", default="50000")
    (options, args) = parser.parse_args()
    logging.debug(options)
    main(options.__dict__)
    sys.exit(0)
