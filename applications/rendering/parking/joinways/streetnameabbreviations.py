# -*- coding: utf-8 -*-
# by kay

import sys,time,re,string,logging
from optparse import OptionParser
from osmdb import OSMDB

endabbreviations1 = {}
endabbreviations2 = {}
begabbreviations1 = {}
begabbreviations2 = {}

# english/american
endabbreviations1.update({'Avenue':'Ave','Boulevard':'Blvd','Circle':'Cir','Close':'Cl','Court':'Ct','Crescent':'Cr','Drive':'Dr','Lane':'Ln','Parkway':'Pkwy','Place':'Pl.','Road':'Rd','Street':'St','Terrace':'Ter','Way':'Wy'})
endabbreviations1.update({'West':'W','East':'E','South':'S','North':'N','Southwest':'SW','Southeast':'SE','Northwest':'NW','Northeast':'NE'})
endabbreviations2.update({'Avenue':'A','Boulevard':'Bd','Close':'C','Lane':'L','Street':'S','Road':'R','Parkway':'Pky','Rd':'R','St':'S','Ln':'L'})
begabbreviations1.update({'West':'W','East':'E','South':'S','North':'N','Southwest':'SW','Southeast':'SE','Northwest':'NW','Northeast':'NE'})

#german
endabbreviations1.update({'Allee':'Al.','allee':'al.','Straße':'Str.','straße':'str.','Weg':'W.','weg':'w.','Gasse':'G.','gasse':'g.','Platz':'Pl.','platz':'pl.','Brücke':'Br.','brücke':'br.'})
endabbreviations1.update({'berg':'bg.'})
endabbreviations2.update({'Straße':'S','straße':'s.'})
begabbreviations1.update({'Straße der':'Str. d.','Straße des':'Str. d.','Weg':'W.','Gasse':'G.','An der':'A. d.','An dem':'A. d.','Auf der':'A. d.','Auf dem':'A. d.','Am ':'A ','In den ':'I. d. ','Zum ':'Z. '})
begabbreviations2.update({'Straße der':'S. d.','Straße des':'S. d.',})

#swiss
endabbreviations1.update({'Strasse':'Str.','strasse':'str.'})
endabbreviations2.update({'Strasse':'S','strasse':'s.'})

#dutch
endabbreviations1.update({'Straat':'Str.','straat':'str.'})
endabbreviations2.update({'Straat':'S','straat':'s.'})

#french
begabbreviations1.update({'Chemin':'Ch.','Impasse':'Imp.','Place':'Pl.','Rue':'R.','rue':'r.'})
begabbreviations2.update({'Impasse':'I','Rue':'R','rue':'r'})

#spanish
begabbreviations1.update({'Avenida':'Av.','avenida':'av.','Camino':'Cno.','Rua':'R.','rua':'r.','Calle':'C.','calle':'c.'})
begabbreviations2.update({'Rua':'R','rua':'r','Calle':'C','calle':'c'})

#catalan
begabbreviations1.update({'Carrer':'C.','carrer':'c.'})

#italian
begabbreviations1.update({'Via':'V.','via':'v.'})
begabbreviations2.update({'Via':'V','via':'v'})

#svenska
endabbreviations1.update({'gatan':'g.','gata':'g.','vägen':'v.','väg':'v.'})

#norsk
endabbreviations1.update({'gaten':'g.','gate':'g.','veien':'v.','vei':'v.'})






class NameDB (OSMDB):

    def _escape_quote(self,name):
        return name.replace("'","''")

    def get_unabbreviated_highways(self,num):
        """ Finds - within the small bbox - the highways with the same name. Returns dictionary with osm_id as key. """
        st=time.time()
        self.FljW = "FROM "+self.prefix+"_line_join WHERE"
        sel="select distinct join_id,name {FljW} abbravailable is Null limit {lim}".format(FljW=self.FljW,lim=num)
        rs=self.select(sel)
        t=time.time()-st
        logging.debug("{t:.2f}s: {sel}".format(t=t,sel=sel))
        highways = {}
        for res in rs:
            highway = {}
            highway['join_id']=res[0]
            highway['name']=res[1]
            #print "x="+str(highway)
            highways[highway['join_id']]=highway
        return highways

    def _escape_quote(self,name):
        if name==None:
            return None
        return name.replace("'","''")

    def _quote_or_null(self,name):
        if name==None:
            return 'Null'
        return "'"+name.replace("'","''")+"'"

    def set_abbreviated_highways(self,join_id,name,a1,a2,a3):
        aa='true'
        if a1==None:
            aa='false'
        a1=self._quote_or_null(a1)
        a2=self._quote_or_null(a2)
        a3=self._quote_or_null(a3)
        upd="update {prefix}_line_join set abbr1={a1}, abbr2={a2}, abbr3={a3}, abbravailable={aa} where join_id={jid}".format(prefix=self.prefix,jid=join_id,a1=a1,a2=a2,a3=a3,aa=aa)
        self.update(upd)
        self.commit
        return









def findabbr(name,begabbrlist,endabbrlist):
    # try abbreviations at end, e.g. "... Street"
    for k in endabbrlist.iterkeys():
        if name.endswith(k):
            r = name[:-len(k)]+endabbrlist[k]
            #print "found end {e}".format(e=k)
            return r
    for k in begabbrlist.iterkeys():
        if name.startswith(k):
            r = begabbrlist[k]+name[len(k):]
            #print "found start {e}".format(e=k)
            return r
    return None

german_street_with_person_name = re.compile('^(.*)-(.*)-(.*)$')

def specialabbr(name):
    # german person based streets, e.g. "Johann-Strauß-Straße" -> "J.-Strauß-Str."
    #print string.count(name,'-')
    if string.count(name,'-')==2:
        matches = german_street_with_person_name.match(name)
        #print matches
        if matches!=None:
            groups = matches.groups()
            firstname = unicode(groups[0],'utf8')
            secondname = unicode(groups[1],'utf8')
            waytype=groups[2]
            #print waytype
            if waytype in endabbreviations1:
                # unicode("ÄBC",'utf8')[:1].encode('utf8')
                #result=firstname[:1]+'.-'+secondname+'-'+endabbreviations1[waytype]
                result=(firstname[:1]+u'.-'+secondname+u'-'+unicode(endabbreviations1[waytype],'utf8')).encode('utf8')
                print "### person-str: {i}->{o}".format(i=name,o=result)
                return result
    # US NSEW roads, e.g. "East 2nd Avenue"
    for k in begabbreviations1.iterkeys():
        if name.startswith(k):
            r = begabbreviations1[k]+name[len(k):]
            #print "found start {e}".format(e=k)
            for k in endabbreviations1.iterkeys():
                if r.endswith(k):
                    r = r[:-len(k)]+endabbreviations1[k]
                    #print "found end {e}".format(e=k)
                    return r
    return None
    
def getAbbreviations(name):
    abbr1 = findabbr(name,begabbreviations1,endabbreviations1)
#    if abbr1 == None:
  #      return (name,name,name)
    abbr2 = findabbr(name,begabbreviations2,endabbreviations2)
#    if abbr2 == None:
#        return (abbr1,abbr1,abbr1)
    abbr3 = specialabbr(name)
#    if abbr3 == None:
#        return (abbr1,abbr2,abbr2)
    # shift to left
    if abbr1==None:
        abbr1=abbr2
        abbr2=abbr3
        abbr3=None
    # second time, if still None
    if abbr1==None:
        abbr1=abbr2
        abbr2=abbr3
        abbr3=None
    # second time, if abbr2 was still None
    if abbr2==None:
        abbr2=abbr3
        abbr3=None
    return (abbr1,abbr2,abbr3)

def main(options):
    name = options['name']
    if name is not None:
        a1,a2,a3 = getAbbreviations(name)
        print "name='{n}', abbr1='{a1}', abbr2='{a2}', abbr3='{a3}'".format(n=name,a1=a1,a2=a2,a3=a3)
        exit(0)
    else:
        DSN = options['dsn']
        num = options['count']
        namedb = NameDB(DSN)
        highways=namedb.get_unabbreviated_highways(num)
        #print highways
        for highway in highways.itervalues():
            name=highway['name']
            join_id=highway['join_id']
            a1,a2,a3 = getAbbreviations(name)
            print "jid={jid}: name='{n}', abbr1='{a1}', abbr2='{a2}', abbr3='{a3}'".format(jid=join_id,n=name,a1=a1,a2=a2,a3=a3)
            if a1==None:
                print "***** no abbreviation found for {n}".format(n=name)
            namedb.set_abbreviated_highways(join_id,name,a1,a2,a3)
            
    return

"""
pAlbumYear = re.compile('^(.*) \((\d\d\d\d)\)$')
matches = pAlbumYear.match(album)
if matches!=None:
  groups = matches.groups()
  year = groups[1]
  album = groups[0]
  print "Year identified: "+year
"""

if __name__ == '__main__':
    logging.basicConfig(format='%(asctime)s %(levelname)s: %(message)s',filename='/home/osm/bin/diffs/logs/nameabbr.log',level=logging.INFO)
    parser = OptionParser()
    #parser.add_option("-n", "--name", dest="name", help="The name to abbreviate. Default is 'Main Street'.", default="Main Street")
    parser.add_option("-n", "--name", dest="name", help="The name to abbreviate.")
    parser.add_option("-d", "--dsn", dest="dsn", help="DSN, default is 'dbname=gis host=crite'", default="dbname=gis host=crite")
    parser.add_option("-c", "--count", dest="count", help="Number of lines to process. Default=10.", default="10")
    (options, args) = parser.parse_args()
    main(options.__dict__)
    sys.exit(0)
