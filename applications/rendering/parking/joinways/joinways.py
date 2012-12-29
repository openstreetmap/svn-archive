# -*- coding: utf-8 -*-
# by kay

import sys,time,logging,random
#import psycopg2
from osmdb import OSMDB
from geom import bbox
from optparse import OptionParser
from streetnameabbreviations import NameDB
import streetnameabbreviations
from collections import deque

class loopdetect():
    queue = deque(['a','b','c','d'])
    def add_id(self,join_id):
        self.queue.append(join_id)
        if len(self.queue)>4:
            self.queue.popleft()
    def check_for_duplicates(self):
        if(self.queue[3]==self.queue[1] and self.queue[2]==self.queue[0]):
            return True
        return False

class JoinDB (OSMDB):

    highway_types={'residential':'r','trunk':'r','trunk_link':'r','primary':'r','primary_link':'r','secondary':'r','secondary_link':'r',
        'tertiary':'r','tertiary_link':'r','living_street':'r','road':'r','service':'r','unclassified':'r','motorway':'r','motorway_link':'r',
        'cycleway':'m','pedestrian':'m','footway':'m','path':'m','raceway':'m','construction':'m','proposed':'m','bridleway':'m','steps':'m','byway':'m','platform':'m','trail':'m',
        'private':'bug','abandoned':'bug','turning_circle':'bug','ter':'bug','undefined':'bug','unsurfaced':'bug','cycleway; footway':'bug','unbuilt':'bug','rest_area':'bug','residential; tertiary; residential':'bug','emergency_access_point':'bug','racetrack':'bug','disused':'bug','minor':'bug','secondary;tertiary':'bug','private road':'bug','residential;steps':'bug',
        'track':'t'}

    '''
     no longer used? remove!
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
    '''
    loop_detect_queue = loopdetect()

    def __init__(self,dsn,maxobjects=0):
        self.maxobjects = maxobjects
        OSMDB.__init__(self, dsn)

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

    def get_next_pending_highway(self):
        """ Gets the next unhandled highway (osm_id+dict) """
        st=time.time()
        if self.globalboundingbox!=None:
            bbox_condition_sql = '"way" && {b} and '.format(b=self.get_globalboundingbox().get_bbox_sql())
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
        # FIXME: better use a buffer around the street in order to find attached ones. BBoxes give problems e.g. for the Bernstrasse here:
        # http://www.openstreetmap.org/?way=131047597,131047602,131047604,124904118,124904119,106915385,123729605,123729606,117751125,131048677,88232929,116526818,131048676,38302181,38302185,27822320,86651386,42348030,42348031
        expand_by_this_many_meters = random.uniform(15.0,50.0) # earlier value 10.0 had issues with unnamed roundabouts (ping pong loops with competing joinways), fixed values as well. Trying with random range.
        old_bbox=""
        collated_highways={}
        collated_highways[highway['osm_id']]=highway
        current_bbox=self.get_expanded_bbox(highway['geom'],expand_by_this_many_meters)

        i=0
        while current_bbox != old_bbox:
            old_bbox = current_bbox
            collated_highways.update(self.find_same_named_highways(highway,current_bbox))
            # FIXME: wieso wird hier immer der Joinway berechnet während der expand-phase? Das genügt doch nachher einmal.
            the_joined_way=self.get_joined_ways(collated_highways.keys())
            current_bbox=self.get_expanded_bbox(the_joined_way,expand_by_this_many_meters)
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
        return self.select_one("select ST_Linemerge(ST_Collect(way)) {FlW} osm_id in {seglist}".format(FlW=self.FlW,seglist=self.sql_list_of_ids(segment_ids)))

    def _insert_joined_highway(self,join_id,name,highway,way):
        """ adds the joined highway (it may be a MULTILINE feature) to the jr tables. returns (just for info) the number of written ways (>1 if a MULTILINESTRING) """
        # just a quick test if highway is valid. (I had instances of highway=')
        if highway not in self.highway_types:
            if highway=="'":
                logging.warn('Illegal highway type for way ({join_id}): "{ht}" - using "fixme" instead.'.format(join_id=join_id,ht=highway))
                highway="fixme"
            else:
                logging.warn('Illegal highway type for way ({join_id}): "{ht}" - using "fixme" instead.'.format(join_id=join_id,ht=highway))
                highway="fixme"
#                raise Exception('Illegal highway type for way ({join_id}): "{ht}"'.format(join_id=join_id,ht=highway))
        if self._which_geometry_is_it(way)=="LINESTRING":
            #print "inserting a simple way"
            #print "insert into planet_line_join (join_id, name, highway, way) values ('"+join_id+"','"+self._escape_quote(name)+"','"+highway+"',SetSrid('"+way+"'::Text,4326))"
            self.insert("insert into planet_line_join (join_id, name, highway, way) values ('"+join_id+"','"+self._escape_quote(name)+"','"+highway+"',SetSrid('"+way+"'::Text,4326))")
            return 1
        else:
            #print "inserting a MULTILINE way"
            ways = self._split_multiline_way(way)
            for one_way in ways:
                self.insert("insert into planet_line_join (join_id, name, highway, way) values ('"+join_id+"','"+self._escape_quote(name)+"','"+highway+"',SetSrid('"+one_way+"'::Text,4326))")
            return len(ways)


    def _insert_segment_into_joinmap(self,join_id,segment_id):
        """ adds a segment to the jr tables """
        self.insert("insert into planet_line_joinmap (join_id, segment_id) values ('{jid}','{sid}')".format(jid=join_id,sid=segment_id))

    def _mark_segment_as_handled(self,segment_id):
        """ Mark the given segment (by osm_id) as handled in the jr tables """
        self.update("update planet_line set jrhandled=True where osm_id={oid}".format(oid=segment_id))

    def _mark_segments_as_handled(self,segment_id_list):
        """ Mark the given segments (by osm_id) as handled in the jr tables """
        segment_id_sql_list = self.sql_list_of_ids(segment_id_list)
        self.update("update planet_line set jrhandled=True where osm_id in {oids}".format(oids=segment_id_sql_list))

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



    def detect_and_handle_merger(self,highway,joinset,joinway):
        """  if a segment adds to a previously existing joinroad (e.g. a side road is added) """
        join_id = highway['osm_id']
        segments = joinset.keys()
        select = "select distinct join_id from planet_line_joinmap where segment_id in {l}".format(l=self.sql_list_of_ids(segments))
        existing_joinways = self.select_list(select)
        l = len(existing_joinways)
        if l==0:
            return
        if l==1:
            # just a quick assertion: the segments should not be in the same joinway as existed before, as it should have been deleted by the delete handling first.
            if existing_joinways[0]==join_id:
                logging.error("***** programming error: segment set {l} will be inserted as joinway {jw} which existed (but should have been deleted by osm2pgsql)".format(l=self.sql_list_of_ids(segments),jw=join_id))
        # if l>0
        # remove/flush all existing joinways, and mark all their individual segments as unhandled.
        # note that the set of segments may be larger than the newly calculated joinway's segments (joinset parameter).
        logging.warn("Merger: Joinway {jw}'s segment set {l} is in the following (to be removed) joinways {jwl}".format(jw=join_id,l=self.sql_list_of_ids(segments),jwl=self.sql_list_of_ids(existing_joinways)))
        #
        # FIXME: temp workaround for endless loop:
        # build a queue and check that join_ids are not looping
        #
        self.loop_detect_queue.add_id(join_id)
        if self.loop_detect_queue.check_for_duplicates():
            logging.error("***** Loop detected: ignoring and adding duplicate way")
            return
        for existing_joinway in existing_joinways:
            self.unhandle_joinway(existing_joinway)
        return

    def add_join_highway(self,highway,joinset,joinway):
        """ Add the highway into the jr tables, handle all flagging """
        join_id = highway['osm_id']
        #print "*** Adding '{name}' ({id}) to planet_line_join".format(name=highway['name'],id=join_id)
        numjoins = self._insert_joined_highway(str(join_id),highway['name'],highway['highway'],joinway)
        #print "(joinset={j})".format(j=joinset)
        join = joinset.keys()
        for segment_id in join:
            #print "  * segment is {s}".format(s=joinset[segment_id])
            self._insert_segment_into_joinmap(join_id,segment_id)
        self._mark_segments_as_handled(join)
        return numjoins

    def assert_joinway_is_not_duplicated(self,highway,joinset,joinway):
        """ Check the highway in the jr tables """
        join_id = highway['osm_id']
        #print "*** Adding '{name}' ({id}) to planet_line_join".format(name=highway['name'],id=join_id)
        segments = joinset.keys()
        select = "select distinct join_id from planet_line_joinmap where segment_id in {l}".format(l=self.sql_list_of_ids(segments))
        joinways = self.select_list(select)
        if len(joinways)==1:
            return
        logging.error("***** programming error: segment set {l} has just been inserted as joinway {jw} but is part of joinways {jwl}".format(l=self.sql_list_of_ids(segments),jw=join_id,jwl=self.sql_list_of_ids(joinways)))

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

    def assert_segment_is_unrecorded(self,highway):
        """ Assert that the given highway (structure) is not in the database, i.e. it is either a new one or had been deleted in the step before.
        """
        segment_id = highway['osm_id']
        while True:
            select="select join_id from planet_line_joinmap where segment_id={sid}".format(sid=segment_id)
            jidlist = self.select_list(select)
            if not jidlist:
                break
            jid = jidlist[0]
            logging.warn("osm2pgsql problem: way segment {sid} was found in the joinmap table at joinway {jid} ({n} times).".format(sid=segment_id,jid=jid,n=len(jidlist)))
            #logging.warn("...removing it.")
            # this means mark all segments of the joinway as unhandled. This covers the case that a segment is being separated from a once joined set.
            self.unhandle_joinway(jid)
            #logging.warn("...done.")
        return

    def unhandle_joinway(self,joinway_id):
        """ removes a joinway and marks its segments as unhandled """
        dirtylist=self.get_segments_of_joinway(joinway_id)
        self.mark_segments_unhandled(dirtylist)
        self.remove_joinway(joinway_id)
        
    def remove_joinway(self,joinway_id):
        delete="delete from planet_line_join where join_id={j}".format(j=joinway_id)
        self.delete(delete)
        delete="delete from planet_line_joinmap where join_id={j}".format(j=joinway_id)
        self.delete(delete)

    def area_of_joinway(self,joinway):
        bbox_of_joinway=self.get_expanded_bbox(joinway,0.0)
        xs,ys,xe,ye=bbox.coords_from_bbox(bbox_of_joinway)
        dx=abs(xe-xs)
        dy=abs(ye-ys)
        area=dx*dy/1000000.0
        return area

    def handle_deleted_highway_segments(self):
        """ handle deleted highway segments -> mark all joined highway's segments as unhandled in order to have them re-handled """
        delestarttime=time.time()
        i=0
        j=0
        while True:
            segment_id=self.get_next_deleted_highway()
            if segment_id==None:
                break
            #print "[] Handling deleted segment {seg}".format(seg=segment_id)
            joinway_id=self.find_joinway_by_segment(segment_id)
            if joinway_id==None:  # deleted segment was not in joined highway -> ignore (FIXME: and warn)
                #print "   [] was not a joinway. Ignoring and flushing."
                self.flush_deleted_segment(segment_id)
                ### FIXME: zur sicherheit in planet_line als dirty markieren (falls vorhanden), oder besser: ASSERT ERROR falls in planet_line vorhanden und jrhandled=True
                continue
            dirtylist=self.get_segments_of_joinway(joinway_id)
            name_of_joinway=self.get_name_of_joinway(joinway_id)
            #print "   [] '{jwname}': list of segments to mark: {l}".format(jwname=name_of_joinway,l=dirtylist)
            # dirty segments must be removed: * from the deleted_segments table, * from the joinmap, * from the join table
            # all of those must fail gracefully if an entry is not there (anymore).
            self.mark_segments_unhandled(dirtylist)
            self.flush_deleted_segments(dirtylist)
            self.remove_joinway(joinway_id)
            i+=1
            j+=len(dirtylist)
            logging.info("Deleted {i}. ({id}) '{jwname}' ({l} segments).".format(i=i,id=segment_id,jwname=name_of_joinway,l=dirtylist))
            if i%100==0:
                self.commit()
            if self.maxobjects>0 and i>=self.maxobjects:
                break
        self.commit()
        logging.info("Found {i} deleted segments and marked {j} highways as dirty".format(i=i,j=j))
        dele=i
        deletime=time.time()-delestarttime
        return (dele,deletime)

    def handle_new_highway_segments(self):
        """ handle unhandled (or dirty) highway segments """
        ### FIXME: was passiert, wenn zu einem Segment eins angehängt wird, vorher müssten die alten segmente rausgelöscht werden.
        addstarttime=time.time()
        i=0
        while True:
            ts=time.time()
            # self.set_bbox(bbox)
            #logging.warn("handle_new_hw_segments (i={i})".format(i=i))
            highway=self.get_next_pending_highway()
            #print "The next pending highway was "
            #print highway
            if highway==None:
                break
            i+=1
            self.assert_segment_is_unrecorded(highway)
            #print "Found {i}. pending highway '{name}'".format(i=i,name=highway['name'])
            joinset,joinway=self.collate_highways(highway)
            # print "  Found connected highways '{hws}'".format(hws=joinset)
            self.detect_and_handle_merger(highway,joinset,joinway) # if a segment adds to a previously existing joinroad (e.g. a side road is added)
            numjoins = self.add_join_highway(highway,joinset,joinway)
            self.assert_joinway_is_not_duplicated(highway,joinset,joinway)
            if i%100==0:
                self.commit()
            t=time.time()-ts
            area=self.area_of_joinway(joinway)
            if area==0.0: # prevent a rare division by zero bug (maybe due to ways with a single node only)
                area=1
                logging.info("***** found zero-sized area: {i}. ({id}) '{name}' ({t:.2f}s): {segs} segments -> {numjoins} joined segments [{area:.2f}km²,{tpa:.3f}s/km²]".format(i=i,id=highway['osm_id'],name=highway['name'],t=t,segs=len(joinset),numjoins=numjoins,area=area,tpa=(t/area)))
            logging.info("Joined {i}. ({id}) '{name}' ({t:.2f}s): {segs} segments -> {numjoins} joined segments [{area:.2f}km²,{tpa:.3f}s/km²]".format(i=i,id=highway['osm_id'],name=highway['name'],t=t,segs=len(joinset),numjoins=numjoins,area=area,tpa=(t/area)))
            if self.maxobjects>0 and i>=self.maxobjects:
                break
        self.commit()
        add=i
        addtime=time.time()-addstarttime
        return (add,addtime)


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

    osmdb = JoinDB(DSN,maxobjects)
    if bboxobj != None:
        osmdb.set_globalboundingbox(bboxobj)

    if options['command']=='clear':
        osmdb.clear_planet_line_join()

    dele,deletime = osmdb.handle_deleted_highway_segments()
    delerate=dele/deletime
    add,addtime = osmdb.handle_new_highway_segments()
    addrate=add/addtime
    logging.warn("Joinways ended: {dele} highways deleted in {deletime:.0f} seconds ({delerate:.2f} d/s), {add} highways added in {addtime:.0f} seconds ({addrate:.2f} a/s)".format(dele=dele,deletime=deletime,delerate=delerate,add=add,addtime=addtime,addrate=addrate))

    # -----------
    # name abbreviations part here

    num = maxobjects
    namedb = NameDB(DSN)
    highways=namedb.get_unabbreviated_highways(num)
    logging.warn("Starting abbreviation of {l} highways".format(l=len(highways)))
    for highway in highways.itervalues():
        name=highway['name']
        join_id=highway['join_id']
        a1,a2,a3 = streetnameabbreviations.getAbbreviations(name)
        logging.info("jid={jid}: name='{n}', abbr1='{a1}', abbr2='{a2}', abbr3='{a3}'".format(jid=join_id,n=name,a1=a1,a2=a2,a3=a3))
        if a1==None:
            logging.warn("***** no abbreviation found for {n}".format(n=name))
        namedb.set_abbreviated_highways(join_id,name,a1,a2,a3)
    logging.warn("NameAbbreviate ended.")





if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-l", "--loglevel", dest="loglevel", help="Loglevel [INFO|WARN|DEBUG]. Default is INFO.", default="INFO")
    parser.add_option("-c", "--command", dest="command", help="The command to execute. Default is update. Possible values are update, install, clear", default="update")
    parser.add_option("-b", "--bbox", dest="bbox", help="bounding box to restrict to", default="")
    parser.add_option("-d", "--dsn", dest="dsn", help="DSN, default is 'dbname=gis host=crite'", default="dbname=gis host=crite")
    parser.add_option("-m", "--maxobjects", dest="maxobjects", help="maximum number of objects to treat, default is 50000. Set to 0 for unlimited.", default="50000")
    (options, args) = parser.parse_args()
    loglevel = options.__dict__['loglevel'].upper()
    if loglevel!=None:
        ll = {'INFO':logging.INFO, 'WARN':logging.WARN, 'DEBUG':logging.DEBUG}[loglevel]
    else:
        ll = logging.INFO
    logging.basicConfig(format='%(asctime)s %(levelname)s: %(message)s',filename='/home/osm/bin/diffs/logs/joinways2.log',level=ll)
    logging.debug(options)
    main(options.__dict__)
    sys.exit(0)
