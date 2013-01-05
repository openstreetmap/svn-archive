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
endabbreviations1.update({'Avenue':'Ave','Alley':'Aly','Boulevard':'Blvd','Bridge':'Br','Circle':'Cir','Close':'Cl','Court':'Ct','Cove':'Cv','Crescent':'Cr','Drive':'Dr','Expressway':'Expy','Garden':'Gdn','Gardens':'Gdns','Highway':'Hwy','Lane':'Ln','Park':'Pk','Parade':'Pde','Parkway':'Pkwy','Place':'Pl.','Road':'Rd','Street':'St','Square':'Sq','Terrace':'Ter','Trail':'Trl','View':'Vw','Way':'Wy'})
endabbreviations1.update({'West':'W','East':'E','South':'S','North':'N','Southwest':'SW','Southeast':'SE','Northwest':'NW','Northeast':'NE'})
endabbreviations2.update({'Avenue':'A','Boulevard':'Bd','Close':'C','Lane':'L','Road':'R','Parkway':'Pky','Rd':'R','Ln':'L'})
begabbreviations1.update({'West':'W','East':'E','South':'S','North':'N','Southwest':'SW','Southeast':'SE','Northwest':'NW','Northeast':'NE'})
begabbreviations1.update({'Upper ':'Up ','Lower':'Lr '})

#german
endabbreviations1.update({'Allee':'Al.','allee':'al.','Straße':'Str.','straße':'str.','Weg':'W.','weg':'w.','Gasse':'G.','gasse':'g.','Hof':'H.','Platz':'Pl.','platz':'pl.','promenade':'prm.','Brücke':'Br.','brücke':'br.','Ring':'Rg.','Steige':'Stg.'})
endabbreviations1.update({'bach':'b.','berg':'bg.','burg':'bg.','dorf':'d.','feld':'fd.','heim':'hm.','hof':'h.','graben':'gr.','park':'pk.','ring':'rg.','stein':'st.','gässle':'g.','gässchen':'g.'})
endabbreviations2.update({'Straße':'S','straße':'s.'})
begabbreviations1.update({'Straße der':'Str. d.','Straße des':'Str. d.','Weg':'W.','Gasse':'G.','An der':'A. d.','An dem':'A. d.','Auf der':'A. d.','Auf dem':'A. d.','Am ':'A ','In den ':'I. d. ','Vor dem ':'V. d. ','Zum ':'Z. ','Zur ':'Z. '})
begabbreviations1.update({'Unterer ':'U. ','Untere ':'U. ','Unteres ':'U. ','Oberer ':'Ob. ','Obere ':'Ob. ','Oberes ':'Ob. '})
begabbreviations2.update({'Straße der':'S. d.','Straße des':'S. d.','Am ':'','Zum ':'','Zur ':''})

#swiss
endabbreviations1.update({'Strasse':'Str.','strasse':'str.'})
endabbreviations2.update({'Strasse':'S','strasse':'s.'})

#dutch
endabbreviations1.update({'Laan':'Ln.','laan':'ln.','Plaats':'Pl.','plaats':'pl.','Plein':'Pln.','plein':'pln.','Straat':'Str.','straat':'str.','vej':'v.'})
endabbreviations1.update({'Oost':'O.','Noord':'N.','Zuid':'Z.','West':'W.'})
endabbreviations2.update({'Straat':'S','straat':'s.'})

#french
begabbreviations1.update({'Avenue':'Av.','Allée du ':'Al. d. ','Allée':'Al.','Boulevard':'Bl.','Chemin':'Ch.','Impasse':'Imp.','Place':'Pl.','Promenade':'Prmd.','Route de la ':'Rt. d. l. ','Rue':'R.','rue':'r.'})
begabbreviations2.update({'Impasse':'I','Rue de la ':'R d l ','rue':'r'})

#spanish
begabbreviations1.update({'Avenida':'Av.','avenida':'av.','Camino':'Cno.','Plaza de ':'Pl. d. ','Plaza':'Pl.','Rua':'R.','rua':'r.','Rúa':'R.','rúa':'r.','Calle':'C.','calle':'c.'})
begabbreviations2.update({'Rua':'R','rua':'r','Rúa':'R','rúa':'r','Calle':'C','calle':'c'})

#catalan
begabbreviations1.update({'Carrer':'C.','carrer':'c.'})

#italian
begabbreviations1.update({'Piazza':'P.','Strada':'S.','Via':'V.','via':'v.','Vía':'V.','vía':'v.','Viale':'Vle.'})
begabbreviations2.update({'Via':'V','via':'v','Vía':'V','vía':'v','Viale':'V'})

#svenska
endabbreviations1.update({'gatan':'g.','gata':'g.','gränd':'gr.','gränden':'gr.','stråket':'str.','vägen':'v.','väg':'v.'})

#norsk
endabbreviations1.update({'gaten':'g.','gate':'g.','veien':'v.','vei':'v.'})

#danish
endabbreviations1.update({'vænget':'v.'})

#finnish
endabbreviations1.update({'aukio':'auk.','kaari':'kri','kaarenpolku':'k.p.','katu':'k.','kuja':'kj.','laituri':'lait.','niemi':'n.','polku':'p.','porras':'prs.','puisto':'ps.','ranta':'r.','raitti':'r.','rinne':'rn.','silta':'s.','tie':'t.','tori':'tr.','vierto':'v.'})
endabbreviations2.update({'aukio':'a.','kaari':'k','katu':'k','laituri':'l.','rinne':'r.'})
begabbreviations1.update({'Pohjoinen ':'Pohj. ','Eteläinen ':'Et. ','Itäinen ':'It. ','Läntinen ':'Länt. '})
begabbreviations2.update({'Pohjoinen ':'P. ','Eteläinen ':'E. ','Itäinen ':'I. ','Läntinen ':'L. '})

#russian
endabbreviations1.update({'аллея':'ал.','бульвар':'бул.','набережная':'наб.','переулок':'пер.','площадь':'пл.','проезд':'пр.','проспект':'просп.','шоссе':'ш.','тупик':'туп.','улица':'ул.'})

#ladinisch
begabbreviations1.update({'Streda ':'Str. '})

#errors
errorbegin = {'strada','via'}
errorend = {'bridge'}

"""


12:29:21 <kay_D> Hi alv
12:30:18 <kay_D> would you mind helping me a bit with road name abbreviations?
13:43:39 <alv> umm hi. which ones?
14:13:22 <kay_D> I am trying to handle abbriviations, like "Hauptstrasse"->"Hauptstr." in German. Do you have 
  finnish examples?
14:14:09 <alv> i wrote these already the wrong way, but you'll get them:
14:14:15 <alv> "t." = "tie" ("v."="väg" for swedish names), "k."="katu" ("g."="gatan" for swedish equivalent 
  name), "kj"="kuja" ("alley"). less frequent ones include "rn."="rinne" (literally "hillside"), "p." = 
  "polku" (literally "unmade footpath"). "ps"="puisto" (park), "r." ="raitti" (se: "str"="stråket"), 
  literally something akin to a main road in a village
14:16:01 <kay_D> .oO( damn encoding ) wait a sec.
14:16:31 <alv> one more for names in swedish  "gr" = "gränden"/"gränd" (alley)
14:19:00 <kay_D> ok, thanks a lot. I am currently running a conversion script for my DB, maybe it helps.
14:19:20 <kay_D> I'll send you a pointer to the result then :-) couple of days to go.
14:20:15 <alv> oh, a rare one: "porras" = "prs." (lit. "one step (of stairs)"). 
14:21:34 <kay_D> Have you seen I used Helsinki as a good example for road splitting/joining at SotM?
14:21:44 <alv> nope
14:22:29 <alv> I had just moved home, so didn't really follow/catch up the presentations
14:22:50 <kay_D> http://wiki.openstreetmap.org/wiki/State_Of_The_Map_2012/Saturday  <- choose the "commented 
  slides" pdf or ods.
14:23:37 <kay_D> You can gracefully skip the "how to tag parking lanes" part, you are the most expert that I 
  know :)
14:24:26 <kay_D> Hope everything went well with your move.
14:26:20 <alv> yes
14:26:40 <alv> i still have some drilling to do, like the last wall lamps
14:27:25 <kay_D> I have not placed lamps everywhere in my living room yet, and we live there for >3y now. :-(
14:27:29 <alv> this time it was a very short distance, like 110 meters door-to-door, including the lift
14:27:39 <kay_D> he he
14:28:07 <alv> much less stressfull, when you don't need to pack the whole apartment into a hgv
14:28:34 <kay_D> yes, my last move was 220m only, too.
14:28:47 <kay_D> but included lots of stairs.
14:31:31 <alv> that joining looks nice
14:32:31 <kay_D> Thanks. I think it is dearly needed for OSM rendering.
14:32:51 <alv> current mapnik looses the name on quite a lot of roads, even when we don't do overkill 
  accurate parking splits.
14:33:02 <kay_D> true
14:34:21 <kay_D> but "micromapping" is coming up for lots of places, especially bigger cities.
14:34:26 <alv> they seem to name new streets more and more with past persons' names, so new streets have 
  longer names than the old ones.
14:42:24 <kay_D> I included a special case abbreviation that abbreviates the first name: 
  "Alfred-Nobel-Strasse" -> "A.-Nobel-Str."
14:43:12 <kay_D> But that works only in german due to our habit of connecting nouns with hyphens. So if two 
  hyphens are present, I assume the first word being a first name.
14:43:53 <alv> in the beginning of a name, some roads in finland have "Pohjoinen " (northern) = "Pohj. " or 
  "P. "
14:44:05 <kay_D> If you can come up with a good identifying algorithm for finnish names, I could include that 
  too.
14:44:12 <alv> likewise "Eteläinen "="Et. " = "E. "
14:45:14 <kay_D> ok, I can handle "beginning" and "ending" abbreviations.
14:47:32 <alv> but sometimes it's for example "Pohjoisxxxxkatu", which is rarely abbreviated (like this 
  Pohjoisesplanadi http://osm.org/go/0xPLphyby--?m ) - i'm not even sure if i've ever seen "P.esplanadi", 
  because it's strange to have the lower case "e".
14:47:53 <alv> "northstreet" vs "northern street"
14:48:57 <kay_D> understood. Same in German. I cannot abbreviate if it's part of a longer word.
14:49:16 <kay_D> I mean I cannot abbreviate at the beginning.
14:50:40 <alv> i don't think there's a anything automatic that could work for names
14:52:21 <alv> I've seen, Urho Kekkosen katu = U. K. katu. but Birger Kaipiaisen katu = "Birger Kaip. katu", 
  or Katariina Saksilaisen katu -> "K. Saksilaisen katu".
14:52:51 <alv> just about all old named-after-person roads are with the surname only.
14:53:02 <alv> or just some version of the first name
14:54:03 <kay_D> why is in your second example the last name abbreviated?
14:54:30 <alv> don't ask me :)
14:54:32 <kay_D> or do you have (like japanese) sometimes the first name as the second word?
14:54:43 <alv> no
14:55:13 <kay_D> ok. can I just assume that if I see three words, last one is Katu, that the first word is a 
  first name? :-)
14:55:26 <alv> guess it could be easier to recognize on a map that way, vs. B. Kaipiaisen k.
14:55:38 <alv> i have no idea who he/she was.
14:56:30 <alv> likely, unless it's Etelä/Itä/Länsi/Pohjois/eteläinen/itäinen/läntinen/pohjoinen
14:56:50 <alv> if you get a list of them, i can look through.
14:57:02 <kay_D> Deal! :)
14:57:21 <alv> (maybe not right now, i have those lamps to drill, but later in the evening)
14:58:02 <kay_D> I am at work, too, will take a few days I guess :)
14:58:49 <kay_D> could you please do me a favour and copy our chat log into an email, send it to 
  kay@drangmeister.net, because I cannot see the special characters correctly in the chat window :-(
14:59:20 <kay_D> I tried with all sorts of encoding, but it did not work. Likely a chat server screws them.
14:59:40 <kay_D> (or ircII I use)
End of kay_D buffer    Wed Oct 24 15:01:06 2012
"""




class NameDB (OSMDB):

    def get_unabbreviated_highways(self,num):
        """ finds num highways with yet unabbreviated names """
        st=time.time()
        self.FljW = "FROM "+self.prefix+"_line_join WHERE"
        limit = "" if num==0 else " limit {lim}".format(lim=num)
        sel="select distinct join_id,name {FljW} abbravailable is Null{limit}".format(FljW=self.FljW,limit=limit)
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

    def set_abbreviated_highways(self,join_id,name,a1,a2,a3):
        aa='true' if a1!=None else 'false'
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
#        return (name,name,name)
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
