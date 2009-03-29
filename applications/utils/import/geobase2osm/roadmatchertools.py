# -*- coding: utf-8 -*-
#
# Tools for using RoadMatcher produced result files
# in importing GeoBase data to OSM.
#
#
#
#
import string
import sets
from xml import sax


#
#
# A map of type abbrivations to
# type names used in the StatsCan
# naming data.
typeNameMap={ 'EXPY':'Expressway',
              'PINES':'Pines',
              'ABBEY':'Abbey',
              'EXTEN':'Extension',
              'PLACE':'Place',
              'ACCESS':'Access',
              'FARM': 'Farm',
              'PL':'Place',
              'ACRES':'Acres',
              'FIELD':'Field',
              'PLAT':'Plateau',
              'ALLÉE': 'Allée',	
              'FOREST':'Forest',          
              'PLAZA':'Plaza',
              'ALLEY':'Alley',	          
              'FWY':'Freeway',
              'PT':	'Point',
              'AUT':'Autoroute',
              'FRONT': 'Front',	
              'PVT':''	,
              'AV': 'Avenue',
              'GDNS': 'Gardens',
              'PROM':'Promenade',
              'AVE':'Avenue',              
              'GATE':'Gate',
              'QUAY':'Quay',
              'BAY':'Bay',
              'GLADE':'Glade',
              'GLEN':'Glen',
              'RG' : 'Range',
              'BEND':'Bend',	
              'GREEN':'Green',
              'REACH' :'Reach',
              'BLVD':'Boulevard',
              'GRNDS':'Grounds',
              'RIDGE':'Ridge',
              'GROVE':'Grove',
              'RTOFWY':'',
              'BROOK':'Brook',
              'HARBR':'Harbour',
              'RISE':'Rise',
              'BYPASS':'By-pass',
              'HAVEN':'Haven',
              'RD':'Road',
              'BYWAY':'Byway',
              'HEATH':'Heath',
              'RDPT':'Rond Point',
              'CAMPUS':'Campus',
              'HTS':'Heights',
              'ROUTE':'Route',
              'CAPE':'Cape',
              'HGHLDS':'Highlands',
              'RTE':'Route',
              'CAR':'Carre',
              'HWY':'Highway',	
              'ROW':'Row',
              'CERCLE':'Cercle',
              'HILL':'Hill',
              'RUE': 'Rue',
              'CHASE':'Chase',  
              'HOLLOW':'Hollow',
              'RLE':'Ruelle',
              'CH': 'Chemin',
              'IMP':'Impasse',
              'RUIS':'Ruisseau',
              'CIR':'Circle',
              'ISLAND':'Island',
              'RUN':'Run',
              'CIRCT':'Circuit',
              'KEY':'Key',
              'SECTN':'Section',
              'CLOSE':'Close',
              'KNOLL':'Knoll',
              'SENT':'Sentier',
              'COMMON':'Common',
              'LANDING':'Landing',
              'SIDERD':'Sideroad',
              'CONC':'Concession' ,             
              'LANE':'Lane',
              'SQ':'Square',
              'CRNRS':'Corners',
              'LANEWY':'Laneway',
              'ST':'Street',
              'CÔTE':'Côte',
              'LMTS':'Limits',
              'STROLL':'Stroll',
              'COUR':'Cour',
              'LINE':'Line',
              'SUBDIV':'Subdivision',
              'CRT':'Court',
              'LINK':'Link',
              'TERR':'Terrace',
              'COVE':'Cove',
              'LKOUT':'Lookout',
              'TSSE':'Terrasse',
              'CRES':'Crescent',
              'LOOP':'Loop',
              'TLINE':'Townline',
              'CROFT':'Croft',
              'MALL':'Mall',
              'TRACE':'Trace',
              'CROIS':'Croissant',
              'MANOR':'Manor',
              'TRAIL':'Trail',
              'CROSS':'Crossing',
              'MAZE':'Maze',
              'TRNABT':'Turnabout',
              'CRSSRD':'Crossroads',
              'MEADOW':'Meadow',
              'VAIL':'Vale',
              'CDS':'Cul-de-sac',
              'MEWS':'Mews',
              'VIEW':'View',
              'CTR':'Center',
              'MONTÉE':'Montée',
              'VILLGE':'Village',
              'DALE':'Dale',
              'MOUNT':'Mount',
              'VILLAS':'Villas',
              'DELL':'Dell',
              'ORCH':'Orchard',
              'VISTA':'Vista',
              'DIVERS':'Diversion',
              'PARADE':'Parade',
              'VOIE':'Voie',
              'DOWNS':'Downs',
              'PARC':'Parc',
              'WALK':'Walk',
              'DR':'Drive',
              'PK':'Park',
              'WAY':'Way',
              'ÉCH':'Échangeur',	
              'PKY':'Parkway',
              'WHARF':'Wharf',
              'END':'End',
              'PASS':'Passage',
              'WOOD':'Wood',
              'ESPL':'Esplanade',
              'PATH':'Path',
              'WYND':'Wynd',
              'ESTATE':'Estates',
              'PTWAY':'Pathway'
              }	  	 
#
# Expand the StatsCan Street Type abbriviation
#
#
def expandStreetType(type):
      if typeNameMap.has_key(type):
          return ' ' + typeNameMap[type]
      else:
          return ''
#
#
# A base class for parsing RoadMatcher result data.
#
#
class RoadMatcherResult(sax.ContentHandler):
    def __init__(self):
        self.waiting=None
        self.string = None
        self.feature = False
        self.attribute = None
        self.nid = None
        self.count=0
        
    def counter(self):
        if self.count % 5000 == 0:
            print self.count
        self.count += 1  
  
    def startDocument(self):
        return
   
    def endDocument(self):
        return
    
    def startElement(self, name, attributes):
        if name == 'feature':
            self.feature=True
            self.nid=None
            self.src_state=None
            self.split=False
            self.counter()

        if name == 'property':
            self.attribute=attributes['name']
            self.waiting = True
            self.string=None
    
    def characters(self,string):
        if self.waiting == True:
            string = unicode(string)
            if self.string == None:
                self.string = string
            else :
                self.string = self.string + string


#
# A class for parsing the match results from RoadMatcher.
# This class looks roads that are StandAlone.
# The NID values are then added to the standAloneList set.
#
#
class RoadMatchHandler(RoadMatcherResult):
    def __init__(self):
        RoadMatcherResult.__init__(self)
        print "Starting to process Exclusion information..."
        self.split=None
        self.standAloneList=set()

    
    def endElement(self,name):
      
        if name=='property' and self.feature==True and self.attribute=='NID':
            self.waiting = False
            self.nid=self.string
            self.string=None
        elif name=='property' and self.feature==True and self.attribute=='SrcState':
            self.src_state=self.string
            self.string=None
            self.waiting=False
        elif name=='property':
            self.waiting=False
            if (self.attribute == 'SplitStart' or self.attribute=='SplitEnd') and self.string=='Y':
                self.split=True
            self.string=None
        if name=='feature' and self.nid != None:
            self.feature=False
            if self.src_state=='Standalone' and self.split==False:
                self.standAloneList.add(self.nid)



  
#
#
# A class used for parsing the roadmatcher results
# from matching the Geobase NRN data to the StatsCan data.
# This allows StatsCan names to be applied to GeoBase roads.
#
class NameMatchHandler(RoadMatcherResult):

    def __init__(self):
        RoadMatcherResult.__init__(self)
        self.tags={}
        self.nameHash={}

    def endElement(self,name):
        if name == 'property':
            if self.attribute == 'NID':
                self.nid=self.string
            elif self.attribute=='NAME':
                self.tags['NAME']=self.string
            elif self.attribute=='TYPE':
                self.tags['TYPE']=self.string
            elif self.attribute=='DIRECTION':
                self.tags['DIRECTION']=self.string
            elif self.attribute=='RB_UID':
                self.tags['RB_UID']=self.string
        elif name == 'feature':
                if self.nid != None:
                    self.nameHash[self.nid]=self.tags
                self.nid=None
                self.tags={}
        self.string=None
