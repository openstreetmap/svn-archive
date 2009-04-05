#!/usr/bin/python
# -*- coding: utf-8 -*-

import xml.etree.cElementTree as ET
import sets
import sys
import codecs
import optparse
import gzip
import osgeo.ogr
import osgeo.osr
from shapely.geometry import LineString
from shapely.geometry import Point
from shapely.geometry import Polygon
from shapely.wkt import loads
import string
from xml import sax
from operator import itemgetter
from roadmatchertools import RoadMatchHandler
from roadmatchertools import NameMatchHandler
from roadmatchertools import expandStreetType

# Allow enforcing of required arguements
# code from http://www.python.org/doc/2.3/lib/optparse-extending-examples.html
class OptionParser (optparse.OptionParser):

    def check_required (self, opt):
      option = self.get_option(opt)

      # Assumes the option's 'default' is set to None!
      if getattr(self.values, option.dest) is None:
          self.error("%s option not supplied" % option)

# Format the output in a prettier style.
# code from the elementtree web site
def indent(elem, level=0):
    i = "\n" + level*"  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        for e in elem:
            indent(e, level+1)
            if not e.tail or not e.tail.strip():
                e.tail = i + "  "
        if not e.tail or not e.tail.strip():
            e.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i

# Convert a string to an int if possible
def convertStr(s):
  try:
    return int(s)
  except ValueError:
    return s

# Namespaces
nrn_ns = "{http://www.geobase.ca/nrn}"
ogc_ns = "{http://www.opengis.net/gml}"
# Mapping of NRN road types to OSM highway types
highway = {}
highway["Freeway"] = "trunk"
highway["Expressway / Highway"] = "primary"
highway["Arterial"] = "secondary"
highway["Collector"] = "tertiary"
highway["Local / Street"] = "residential"
highway["Local / Strata"] = "unclassified"
highway["Local / Unknown"] = "unclassified"
highway["Alleyway / Lane"] = "service"
highway["Ramp"] = "unclassified"
highway["Service Lane"] = "service"
highway["Resource / Recreation"] = "unclassified"
highway["Service Lane"] = "service"

highway["Rapid Transit"] = "unclassified"
highway["Winter"] = "unclassified"


    

TagList=set(('nrn:nid',
            'nrn:ferrySegmentId',
            'nrn:roadSegmentId',
            'nrn:functionalRoadClass',
            'nrn:numberOfLanes',
            'nrn:pavementStatus',
            'nrn:routeNameEnglish1',
            'nrn:routeNameFrench1',
            'nrn:routeNumber1',
            'nrn:datasetName',
            'nrn:right_OfficialPlaceName',
            'nrn:right_OfficialStreetNameConcat',
            'nrn:structureType',
            'nrn:acquisitionTechnique',
            'nrn:junctionType'))

CRS83=osgeo.osr.SpatialReference()
CRS83.ImportFromEPSG(4617)
WGS84=osgeo.osr.SpatialReference()
WGS84.ImportFromEPSG(4326)
transform=osgeo.osr.CoordinateTransformation(CRS83,WGS84)

import string
import sys
from xml import sax
from operator import itemgetter

class addrHandler(sax.ContentHandler):
  count = 0
  
  string = None
  
  addressRange = False
  streetName = False
  
  streets = {}
  
  waiting = False
  
  nid = None
  
  def __init__(self):
    printStr( "Starting to process street address information..." )
  
  def counter(self):
    if self.count % 5000 == 0:
      printStr( self.count )
    self.count += 1  
  
  def startDocument(self):
    return
   
  def endDocument(self):
    return
    
  def startElement(self, name, attributes):
    printStr( "Start of "+name )
    if name == 'nrn:AddressRange':       
      self.counter()
      
    if name == 'nrn:AlternateNameLink':
      self.counter()
    
    if name == 'nrn:StreetPlaceNames':
      self.streetName = True
    
    if name == 'nrn:nid':
      self.waiting = True

  
  def endElement(self,name):
    if name == 'nrn:AddressRange':
      self.waiting = False
      
    if self.streetName == True:
      if name == 'nrn:nid':
        self.nid = self.string
        printStr( "Found nid = "+self.nid )
        self.waiting = False
        
    if name == 'nrn:StreetPlaceNames':
      self.streetName = False
    
    printStr( "End of "+name )
    
  def characters(self,string):      
    if self.waiting == True:
      string = unicode(string)    
      if self.string == None:
        self.string = string
      else :
        self.string = self.string + string


        

        
##
# A class that has the ability to accumlate characeters passed to the
# characeters() method.
# The class can then convert them to a geometry array (appendCoordinates)
#
##
class GeomParser:    

    def __init__(self):
         self.coords=[]
         self.cstring=None
         self.string=None
         self.waiting=False
         self.waitingCoord=False
         
    def characters(self,string):
        string = unicode(string)
        if self.waiting == True:
            if self.string == None:
                self.string = string
            else :
                self.string = self.string + string
        if self.waitingCoord == True:
            if self.cstring == None:
                self.cstring = string
            else :
                self.cstring = self.cstring + string
    # Populates self.coords with a array of coordinates from the text
    # stored in self.cstring
    #
    def appendCoordinates(self):    
        self.coords=[]
        for coordset in self.cstring.split(' '):
          coord = coordset.split(',')
          if len(coord) == 2:
            if len(coord[0]) > 0 and len(coord[1] ) > 0:
              #print "Transform %s %s" % (coord[0],coord[1])
              coord_t=transform.TransformPoint(float(coord[0]),float(coord[1]),0.0)
              lon = coord_t[0]
              lat = coord_t[1]
              self.createNode(lon,lat)
              #Append coordinates to coords array
              self.coords.append([lon,lat])
            else: 
              printStr( "Could not add node due to empty coordinate" )
              printStr( "set = " + coordset )
              printStr( "str = " + self.cstring )
        self.cstring = None

        self.waitingCoord = False
        self.waiting = True
    #
    # Returns the latitude of the first point
    def startLat(self):
          return self.coords[0][1]
    def startLon(self):
          return self.coords[0][0]
    def endLat(self):
          return self.coords[-1][1]
    def endLon(self):
          return self.coords[-1][0]
    def createNode(self,lon,lat):
          return
    
#
# This class represents an OSM way.
# It gets built up from geobase road segments & ferry connections
# 
class Way(GeomParser):
    #A list of references to the OSM nodes
    #that make up this way.
    #Note: Multiple ways can share nodes.
    
    #The XML celement for the way.    
    attribution = "GeoBase®"
    source = "Geobase_Import_2009"
    
    def __init__(self,id,type,boundary,nameHash):
          GeomParser.__init__(self)
          self.nodeid=id         
          self.boundary=boundary
          self.nameHash=nameHash
          self.osm_nodes=[]
          self.way_element=None
          self.string=None
          self.completed=False
          self.tags={}
          self.way_element = ET.Element("way",visible="true",id=str(self.nodeid))
          self.nodeid-=1
          self.placeName=None
          self.outOfBounds=False
          
    def isCompleted(self):
        return self.completed
    def isSkipped(self):
        return self.outOfBounds
    def startElement(self,name,attributes):

        if name in TagList:
            self.waiting=True
            self.string=None
            
        elif  name == 'gml:coordinates':
            self.string = None
            self.waitingCoord = True

    
 


    def endElement(self,name):
        # Prepare a cleaned and stripped text string
        text = self.string

        if text != None:
            text = self.string.strip()
            
        if name == 'nrn:functionalRoadClass':
             self.tags['highway'] = highway[text ]

        elif name == 'nrn:numberOfLanes' :
            self.tags['lanes'] = text

        elif name == 'nrn:pavementStatus' and text!='Paved' :
            self.tags['surface'] = text.lower()

        elif name == 'nrn:datasetName':
            self.tags['geobase:datasetName'] = "NRN:" + text
            self.dataset=text
        elif name == 'nrn:acquisitionTechnique':
              self.tags['geobase:acquisitionTechnique']=text      
        elif name == 'nrn:structureType':
            if text == 'Bridge' or text == 'Bridge Covered' or text == 'Bridge moveable' or text == 'Bridge unknown':
              # Found a bridge
              self.tags['bridge'] = 'yes'
              self.tags['layer'] = '1'

            elif text == 'Tunnel':
              # Found a tunnel              
              self.tags['tunnel'] = 'yes'
              self.tags['layer'] = '-1'
        elif name == 'nrn:right_OfficialPlaceName' and text != 'Unknown':
            self.placeName=text
            # Features common to both roads and ferries

        elif name == 'nrn:nid':
            self.tags['geobase:uuid'] = text

        elif name == 'nrn:routeNumber1' and text!="None" and text != None:
            self.tags['ref'] = set([stringToInteger(text)])
        elif name=='nrn:right_OfficialStreetNameConcat' and text != "None" and text!="-"and text != "Unknown" :
              self.tags['name']=text
        elif name == 'nrn:ferrySegmentId':
            self.tags['nrn:ferrySegmentId'] = text
        elif name == 'gml:coordinates':
            self.appendCoordinates()
        elif name == 'nrn:routeNameEnglish1' and text!="None":
            self.tags['alt_name:en'] = set([text])
            self.tags['geobase:routeName1:en']=text


            if name == 'nrn:routeNumber2' and text!="None" and text != None:
                  if self.tags.has_key('ref'):
                      self.tags['ref'].add(stringToInteger(text))

            elif name == 'nrn:routeNumber3' and text!="None" and text != None:
                  if self.tags.has_key('ref'):
                      self.tags['ref'].add(stringToInteger(text))

            elif name == 'nrn:routeNumber4' and text!="None" and text != None:
                  if self.tags.has_key('ref'):
                      self.tags['ref'].add(stringToInteger(text))


            elif name == 'nrn:routeNameEnglish2' and text!="None":
                  if self.tags.has_key('name'):
                        self.tags['alt_name:en'].add(text)
                  self.tags['geobase:routeName2:en']=text
            elif name == 'nrn:routeNameEnglish3' and text!="None":
                  if self.tags.has_key('name'):
                        self.tags['alt_name:en'].add(text)
                  self.tags['geobase:routeName3:en']=text
            elif name == 'nrn:routeNameEnglish4' and text!="None":
                  if self.tags.has_key('name'):
                        self.tags['alt_name:en'].add(text)
                  self.tags['geobase:routeName4:en']=text
            elif name == 'nrn:routeNameFrench1' and text!="None":
                  if self.tags.has_key('name'):
                        self.tags['alt_name:fr'] = set([text])
                  self.tags['geobase:routeName1:fr']=text

            elif name == 'nrn:routeNameFrench2' and text!="None":
                  if self.tags.has_key('name:fr'):
                        self.tags['alt_name:fr'].add(text)
                  self.tags['geobase:routeName2:fr']=text
            elif name == 'nrn:routeNameFrench3' and text!="None":
                  if self.tags.has_key('name:fr'):
                        self.tags['alt_name:fr'].add(text)
                  self.tags['geobase:routeName3:fr']=text
            elif name == 'nrn:routeNameFrench4' and text!="None":
                  if self.tags.has_key('name:fr'):
                        self.tags['alt_name:fr'].add(text)
                  self.tags['geobase:routeName4:fr']=text

        if name == 'nrn:RoadSegment' or name=='nrn:FerryConnectionSegment':
               self.completeWay()
        

    def completeWay(self):
          self.completed=True  
          #Post Processing.
          #Exclude nodes that are not in the bounding region.
          shape=LineString(self.coords)
          #print "Checking Intersection on %s"  % str(self.coords)      
          if (self.boundary<>None and  self.boundary.intersects(shape)==False ):
              #Mark this object as excluded + incomplete
              self.outOfBounds=True
              return
                 
          self.localize()          
          # Catch all transcanada/yellowhead highway segments that we may have missed  
          if self.tags.has_key('alt_name:en'):
            if ('TransCanada Highway' in self.tags['alt_name:en'] or 'Yellowhead Highway' in self.tags['alt_name:en']) and self.tags['highway'] != 'motorway':
              self.tags['highway'] = 'trunk'
              if 'TransCanada Highway' in self.tags['alt_name:en']:
                self.tags['nat_name:en']='TransCanada Highway'
                self.tags['nat_name:fr']='Route Transcanadienne'
              if 'Yellowhead Highway' in self.tags['alt_name:en']:
                self.tags['nat_name:en']='Yellowhead Highway'
           # Convert the sets back to semicolon separated strings      
          if self.tags.has_key('ref'):      
              self.tags['ref'] = setToString(self.tags['ref']);
          if self.tags.has_key('name')==False:
              if self.dataset <> 'Quebec' and self.tags.has_key('alt_name:en'):
                  self.tags['name']=setToString(self.tags['alt_name:en'])
              elif self.tags.has_key('alt_name:fr'):
                  self.tags['name']=setToString(self.tags['alt_name:fr'])
              if self.tags.has_key('alt_name:en'):
                  self.tags['alt_name:en'] = setToString(self.tags['alt_name:en'])
          if self.tags.has_key('alt_name:en'):
                self.tags['alt_name:en'] = setToString(self.tags['alt_name:en'])
          if self.tags.has_key('alt_name:fr'):
                self.tags['alt_name:fr'] = setToString(self.tags['alt_name:fr'])
          if self.placeName != None:
                self.placeName=self.placeName + ',' + self.dataset
          else:          
              self.placeName=self.dataset
              if self.placeName != None:
                  #Only set is in, if we at least have a province.
                  self.placeName=self.placeName + ',Canada'
          self.tags['is_in']=self.placeName
                # Convert the tags to xml nodes
                
          #
          # If no name is defined, check nameHash
          # to see if one is available in the external
          # dataset.
          if not  self.tags.has_key('name'):
              uuid=self.tags['geobase:uuid']              
              if  self.nameHash.has_key(uuid) :
                  nameTags=self.nameHash[uuid]
                  if nameTags['NAME'] != None:
                      self.tags['name'] = nameTags['NAME']
                      if nameTags['TYPE'] != None:
                          self.tags['name']=self.tags['name'] + expandStreetType(nameTags['TYPE'])
                      if nameTags['DIRECTION'] != None:
                          self.tags['name']=self.tags['name']+ ' '+ nameTags['DIRECTION']
                      if nameTags['RB_UID'] != None:
                          self.tags['statscan:rbuid'] = nameTags['RB_UID']
                      
                          
                  #TODO filter out Unknown etc...
          
          for key, value in self.tags.iteritems():
              self.way_element.append(ET.Element('tag', k=key,v=value))
          self.way_element.append(ET.Element('tag',k='attribution',v=self.attribution))
          self.way_element.append(ET.Element('tag',k='source',v=self.source))
          self.nid=self.tags['geobase:uuid']
          self.tags = None

          
    def localize(self):
        if self.dataset == 'Newfoundland and Labrador':
            pass

        if self.dataset == 'Nova Scotia':
            self.localizeNS()
            
        if self.dataset == 'Prince Edward Island':
            self.localizePEI()            
        if self.dataset == 'New Brunswick':
            self.localizeNB()

        if self.dataset == 'Quebec':
            self.localizeQC()

        if self.dataset == 'Manitoba':
            self.localizeMB()
        if self.dataset == 'Saskatchewan':
            self.localizeSK()

        if self.dataset == 'Alberta':
            self.localizeAB()
        if self.dataset == 'British Columbia':
            self.localizeBC()
        if self.dataset == 'Yukon Territory':
              self.localizeYT()
              
        if self.dataset == 'Northwest Territories':
              self.localizeNT()        
        if self.dataset == 'Nunavut':
            pass

    def localizeNS(self):
        if self.tags.has_key('ref'):        
            if self.tags.has_key('highway'):            
                for ref in self.tags['ref']:
                  if ref >= 100 and ref <= 200:
                    self.tags['highway'] = 'primary'
                  if ref >= 1 and ref <= 100:
                    self.tags['highway'] = 'secondary'
                  # Tag Transcanada as trunk
                  if ref == 1:
                      self.tags['highway'] = 'trunk'
                      
    def localizePEI(self):
        if self.tags.has_key('ref'):        
          if self.tags.has_key('highway'):          
            for ref in self.tags['ref']:
              # Tag Transcanada as trunk
              if ref == 1:
                self.tags['highway'] = 'trunk'
                
    def localizeNB(self):
        if self.tags.has_key('ref'):        
          if self.tags.has_key('highway'):            
            for ref in self.tags['ref']:           
              if ref >= 1 and ref < 100:
                self.tags['highway'] = 'primary'                
              if ref >= 100 and ref < 200:
                self.tags['highway'] = 'secondary'                
              if ref >= 200 and ref <= 1000:
                self.tags['highway'] = 'tertiary'
                
    def localizeQC(self):
 
        if self.tags.has_key('ref'):          
          if self.tags.has_key('highway'):          
            old = self.tags['highway']
            for ref in self.tags['ref']:            
              # Set all autoroute's to motorway
              if ref >= 1 and ref <= 100:
                self.tags['highway'] = 'motorway'
                
              if ref >= 400 and ref <= 1000:
                self.tags['highway'] = 'motorway'
              
              if ref >= 101 and ref <= 199:
                self.tags['highway'] = 'primary'
                
              if ref >= 200 and ref <= 399:
                self.tags['highway'] = 'secondary'

    def localizeON(self):               
        if self.dataset == 'Ontario':
            if self.tags.has_key('ref'):          
                if self.tags.has_key('highway'):
                    for ref in self.tags['ref']:
                        # Primary (1-101)
                        if ref <= 101:
                            self.tags['highway'] = 'primary'
                            # Secondary (102 and up)
                        elif ref > 101:
                            self.tags['highway'] = 'secondary'
            
                            # Tag Transcanada/Yellowhead as trunk
                        if ref == 17 or ref == 11 or ref == 69 or ref == 12 or ref == 7 or ref == 115 or ref == 71:
                            self.tags['highway'] = 'trunk'
                
                        # 400-series as motorway
                        if ref >= 400 and ref < 500:
                            self.tags['highway'] = 'motorway'
    
    def localizeMB(self):
        if self.tags.has_key('ref'):          
            if self.tags.has_key('highway'):          
                for ref in self.tags['ref']:            
                    # Primary (1-101)
                    if ref <= 101:
                        self.tags['highway'] = 'primary'
              
                    # Secondary (102 and up)
                    elif ref > 101:
                        self.tags['highway'] = 'secondary'
            
                        # Tag Transcanada/Yellowhead as trunk
                    elif ref == 1 or ref == 16 or ref == 75 or ref == 100:
                        self.tags['highway'] = 'trunk'
    def localizeSK(self):
        if self.tags.has_key('ref'):        
          if self.tags.has_key('highway'):            
            # Tag Transcanada/Yellowhead as trunk
            for ref in self.tags['ref']:
            
              if ref >= 1 and ref <= 199:
                self.tags['highway'] = 'primary'
                
              if ref >= 200 and ref <= 600:
                self.tags['highway'] = 'secondary'
              
              if ref > 600 and ref <= 1000:
                self.tags['highway'] = 'tertiary'
            
              if ref == 1 or ref == 16 or ref == 7 or ref == 1 or ref == 6 or ref == 39:
                self.tags['highway'] = 'trunk'
                
    def localizeAB(self):
      if self.tags.has_key('ref'):        
          if self.tags.has_key('highway'):          
            for ref in self.tags['ref']:              
              # Primary (5-100)
              if ref >= 5 and ref <= 100:
                self.tags['highway'] = 'primary'
              
              # Secondary (500-899, 901, 940)
              if ref >= 500 and ref < 900:
                self.tags['highway'] = 'secondary'
              
              # Tag Transcanada/Yellowhead as trunk
              if ref == 1 or ref == 2 or ref == 3 or ref == 4 or ref == 16 or ref == 35 or ref == 43 or ref == 49 or ref == 201 or ref == 216:
                self.tags['highway'] = 'trunk'

    def localizeBC(self):
      if self.tags.has_key('ref'):        
          if self.tags['highway'] == 'primary' or self.tags['highway'] == 'secondary':          
            for ref in self.tags['ref']:            
              # Tag Transcanada/Yellowhead as trunk 
              if ref == 1 or ref == 2 or ref == 3 or ref ==5 or ref == 16 or ref == 17 or ref == 97 or ref == 99:
                self.tags['highway'] = 'trunk'
                
    def localizeYK(self):
      if self.tags.has_key('ref'):        
          if self.tags.has_key('highway'):  
            # Tag Transcanada/Yellowhead as trunk
            for ref in self.tags['ref']:
              if ref == 1 or ref == 2:
                self.tags['highway'] = 'trunk'
    def localizeNT(self):
      if self.tags.has_key('ref'):          
          if self.tags.has_key('highway'):            
            # Tag Transcanada/Yellowhead as trunk
            for ref in self.tags['ref']:
              if ref == 1 or ref == 2 or ref == 3:
                self.tags['highway'] = 'trunk'

    #Returns a node object (a XML celement for the OSM node)
    #that is part of the current way and located at the coordinates
    #specified.
    def nodeAt(self,coords):
          for n in self.osm_nodes:
                if n.get('lat')==coords[1] and n.get('lon')==coords[0]:
                      return n
          return None
    #Replaces the reference to nodeToReplace with
    #a reference to the id of nodeToKeep.
    #nodeToReplace then becomes orphaned.
    def replaceNode(self,nodeToReplace,nodeToKeep):
          #Find the nd ref for the node.
          for nd in self.way_element.getiterator('nd'):
                if nd.get('ref')==nodeToReplace.get('id'):
                      #found
                      print "Replacing node " + nd.get('ref') + ' with ' +nodeToKeep.get('id')
                      nd.set('ref',nodeToKeep.get('id'))
                      #Remote from the list
                      self.osm_nodes.remove(nodeToReplace)
                      self.osm_nodes.append(nodeToKeep)
                      break
    def createNode(self,lon,lat):
          node = ET.Element("node", visible='true', id=str(self.nodeid), lat=str(lat), lon=str(lon))
          
          # Add default tags
          # Add a reference to this node to the current way
          self.way_element.append(ET.Element('nd', ref=str(self.nodeid)))
          self.osm_nodes.append(node)
          self.nodeid -= 1



class Junction(GeomParser):
      def __init__(self,boundary,nodeid):
            GeomParser.__init__(self)
            self.boundary=boundary
            self.nodeid=nodeid
            self.completed=False
            self.skipped=True
            self.intersection=False
            
      def startElement(self,name,attributes):
            if  name == 'gml:coordinates':
                  self.string = None
                  self.waitingCoord = True                  
            else:
                  self.waiting=True
                  self.string=None

      def endElement(self,name):
            if name == 'gml:coordinates':
                 self.appendCoordinates()
                 self.waitingCoord=False
            elif name=='nrn:junctionType':
                 if self.string=='Intersection' or self.string=='Ferry':
                    self.intersection=True                               
            elif name == 'nrn:Junction':
                 self.completed=True
                 coord_t=(self.startLon(),self.startLat())
                 shape=Point(coord_t[0],coord_t[1])
                 if self.boundary==None or  self.boundary.intersects(shape):
                      self.skipped=False

            self.waiting=False
            self.string=None
                  
      def isCompleted(self):
            return self.completed
      def isIntersection(self):
            return self.intersection
      def isSkipped(self):
            return self.skipped
     
class geomHandler(sax.ContentHandler):

  count = 0
  boundary=None  
  current_way=None  
  nodeid = -1000
  junctionList=[]
  currentJunction=None

  #
  # An index that maps the lon_lat of a point to any Way objects that
  # either start or end at that coordinate pair
  way_index={}
  
  ways=[]
  
  
  def counter(self):
    if self.count % 5000 == 0:
      printStr( self.count )
    self.count += 1

  def __init__(self,nameHash):
    printStr( "Starting to process GML..." )
    self.depth = 0
    self.nameHash=nameHash
    return

  def startDocument(self):
    self.depth = self.depth + 1
    return
      
  def endDocument(self):
    printStr( "Finished processing." )
          
  def startElement(self, name, attributes):
    # A blocked passage (barrier across road)
    if name == "nrn:BlockedPassage" or name== "nrn:TollPoint":
      self.counter()
      
    # A junction of two or more roads, a dead end road, a ferry route and a road, or the edge of a road on a dataset boundary
    if name == "nrn:Junction":
      self.currentJunction=Junction(self.boundary,self.nodeid)
      self.intersection=False
      self.tags={}
      self.counter()
    
    # A ferry connection
    elif name == 'nrn:FerryConnectionSegment':
      self.ferrySegment = True
      self.current_way=Way(self.nodeid,'nrn:FerryConnectionSegment',self.boundary,self.nameHash)      
      
      self.counter()
      
    # A road 'way'
    elif name == "nrn:RoadSegment":
      self.current_way=Way(self.nodeid,'nrn:RoadSegment',self.boundary,self.nameHash)   
      self.counter()
    
    elif self.current_way != None:
        self.current_way.startElement(name,attributes)
    elif self.currentJunction!=None:
          self.currentJunction.startElement(name,attributes)
    self.depth = self.depth + 1
    return

 

  def endElement(self, name):
    name = unicode(name)
    if self.current_way <> None:
        self.current_way.endElement(name)
        if self.current_way.isCompleted():
            if not self.current_way.isSkipped():
                self.ways.append(self.current_way)
                self.nodeid=self.current_way.nodeid
                for c in self.current_way.coords:
                      
                      key=str(c[0])+'_'+str(c[1])
                      if self.way_index.has_key(key):
                            self.way_index[key].append(self.current_way)
                      else:
                            self.way_index[key]=[self.current_way]
            self.current_way=None
    elif self.currentJunction<>None:
          self.currentJunction.endElement(name)
          if self.currentJunction.isCompleted():
                if self.currentJunction.isIntersection() and self.currentJunction.isSkipped()==False:
                      self.junctionList.append(self.currentJunction)
                self.currentJunction=None              

    self.depth = self.depth - 1
    return

  def mergeNodes(self,coords):
        #Get the list of nodes + the ways that terminate at those nodes
        #merge them into a common node and update the other ways.
        key=(str(coords[0])+'_'+str(coords[1]))
        if self.way_index.has_key(key) :
              way_list = self.way_index[key]
              keep = way_list[0].nodeAt(coords)
              keep_id = keep.get('id')
              for w in way_list[0:]:
                   dup_node = w.nodeAt(coords)
                   w.replaceNode(dup_node,keep)
  
        return



        
  def characters(self,string):
      if self.current_way != None:
          self.current_way.characters(string)
      elif self.currentJunction != None:
            self.currentJunction.characters(string)

def stringToInteger(string):
  try:
    string = int(string)    
  except TypeError:
    pass
  except ValueError:
    pass
  
  return string

# Convert a set to a semicolon separated string
def setToString(tagset):
  
  if len(tagset) == 0:
    return ""
  
  elif len(tagset) == 1:
    string = tagset.pop()
    
    if isinstance(string, basestring ):
      return string
    else: 
      return str(string)
    
  else:
    
    string = tagset.pop()
    
    if isinstance(string,basestring):
      pass
    else:
      string = str(string)
    
    for x in tagset:
      if x != "None":
        if isinstance(x, basestring ):
          string = string + ";" + x
        else: 
          string = string + ";" + str(x)

    return string

suppressoutput = False


#
# Some datasets such as ontario
# have multile roadSegments with the same nid
# that describe a single road broken into
# multiple segments.  This really should
# be different nids connected with a junction.
#
# Here we will connect the nids
#
def connectDuplicateNid(handler):
       
    #For any ways that have a common node point
    for location in handler.way_index:
          waylist=handler.way_index[location]
          for idx  in range(len(waylist)):
                w1 = waylist[idx]
                for w2 in waylist[idx+1:]:
                      if w1.nid==w2.nid:
                            #Match, now find common node.
                            for c1 in w1.coords:
                                  c1_s=(str(c1[0]),str(c1[1]))
                                  if w2.nodeAt(c1_s)!=None:
                                        handler.mergeNodes(c1_s)



def printStr(s):
  #global suppressoutput
  #if suppressoutput == False:
  print s

def main():
    
  usage = "usage: %prog -i NRN_GEOM.gml [-a NRN_ADDR.gml] [-o outfilefile.osm] [--zip] [--pretty] [--quiet]"
  parser = OptionParser(usage)
  parser.add_option("-i", "--input", dest="geomfile", help="read data from GEOMFILE")
  parser.add_option("-a", "--addr", dest="addrfile", help="read optional data from ADDRFILE")
  parser.add_option("-o", "--output", dest="outputfile", default="geobase.osm", help="store data to OUTPUTFILE")
  parser.add_option("-z", "--zip", dest="compress", action="store_true", help="compress the output using gzip")
  parser.add_option("-p", "--pretty", dest="indent", action="store_true", help="stylize the output file")
  parser.add_option("-b", "--boundsfile", dest="boundsfile",help="Boundary file")
  parser.add_option("-q", "--quiet", dest="quiet", action="store_true", help="Enable quiet mode")
  parser.add_option("-e", "--roadmatch_standalone", dest="standAloneFile",help="RoadMatch File")
  parser.add_option("-n", "--roadmatch_names", dest="nameFile",help="RoadMatch FName File")
  (options, args) = parser.parse_args()

  global suppressoutput

  parser.check_required("-i")

  suppressoutput = options.quiet

  if suppressoutput == True:
    print "true"
  else:
    print "false"

  saxparser = sax.make_parser()
  boundary=None
  if options.boundsfile != None and len(options.boundsfile) > 0:
    boundsfile=open(options.boundsfile,"r")
    bounds=boundsfile.readline()
    
    boundary=loads(bounds)
  if options.standAloneFile != None and len(options.standAloneFile) >0:
    standAloneHandler = RoadMatchHandler()
    sax.parse(open(options.standAloneFile),standAloneHandler)
    print "Standalone List Size %d" % (len(standAloneHandler.standAloneList))


  nameMatchHandler=None
  if options.nameFile != None and len(options.nameFile) > 0:
      nameMatchHandler = NameMatchHandler()
      sax.parse(open(options.nameFile),nameMatchHandler)
  
  # If we were given the addr file parse it
  if options.addrfile != None and len(options.addrfile) > 0:
    printStr( "Preparing to read '"+options.addrfile+"'" )
    
    handler = addrHandler()
    
    sax.parse(open(options.addrfile), handler)
  
  # Then parse the geom file
  printStr( "Preparing to read '"+options.geomfile+"'" )
  nameHash={}
  if nameMatchHandler != None:
      nameHash = nameMatchHandler.nameHash
  handler = geomHandler(nameHash)
  handler.boundary=boundary
  sax.parse(open(options.geomfile), handler)
  printStr( "Parse of geomfile done" )

  for junct in handler.junctionList:
    #Find the 'nodes' at these coordinates and merge them IF
    # the junction is an intersection
    handler.mergeNodes((str(junct.startLon()),str(junct.startLat())))


  connectDuplicateNid(handler)
  
   #
   # If we ran with a standalone list, then we need to generate the standalone
   # output file.
   # The standalone output file contains just ways.
   # we need to find only the nodes referenced by the standalone list.
  
  complete_osm=ET.Element("osm", generator='geobase2osm', version='0.5')
  included_nodes=set()

             
  
  for w in handler.ways:
    for n in w.osm_nodes:
        if n not in included_nodes:
            included_nodes.add(n)
            complete_osm.append(n)
  for w in handler.ways:
      complete_osm.append(w.way_element)
  
  # Format the code by default
  if options.indent:
    printStr( "Formatting output" )
    for e in handler.ways:
        indent(e.way_element)
#    for e in handler.osm_nodes:        
#        indent(e)
    
  if options.compress:
    printStr( "Saving to '" + options.outputfile + ".gz'" )
    f = gzip.GzipFile(options.outputfile+".gz", 'wb');
    f.write(ET.tostring(complete_osm))
    f.close()
  else:
    printStr( "Saving to '" + options.outputfile + "'" )
    f = open(options.outputfile, 'w')
    f.write(ET.tostring(complete_osm))
    f.close()

  complete_osm=None


  #Generate standalones  
  if options.standAloneFile != None:
    standAlone_nodes=sets.Set()
    for way in handler.ways:
          if way.nid in standAloneHandler.standAloneList:
                for node in way.osm_nodes:
                      standAlone_nodes.add(node)
    
    #Now For each XML node in handler.osm_nodes that is in the set, write it
    standAlone_osm=ET.Element("osm", generator='geobase2osm', version='0.5')
    excluded_osm=ET.Element("osm", generator='geobase2osm', version='0.5')
    excluded_nodes=sets.Set()
    for n in standAlone_nodes:
        standAlone_osm.append(n)
    print  "Scanning ways for standalones"
    for w in handler.ways:
          if w.nid in standAloneHandler.standAloneList:
                standAlone_osm.append(w.way_element)
          else:
            excluded_osm.append(w.way_element)
            for node in w.osm_nodes:
                    excluded_nodes.add(node)
    print "Scanning excluded nodes"
    for node in excluded_nodes:
        excluded_osm.insert(0,node)
    print "Writing standalone"
    f = open(options.outputfile+".standalone.osm",'w')
    f.write(ET.tostring(standAlone_osm))
    f.close();
    print "Writing excluded"
    f = open(options.outputfile+".excluded.osm",'w')
    f.write(ET.tostring(excluded_osm))
    f.close();
    print "Done"
 
if __name__ == "__main__":
   main()
