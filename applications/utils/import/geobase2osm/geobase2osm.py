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

CRS83=osgeo.osr.SpatialReference()
CRS83.ImportFromEPSG(4617)
WGS84=osgeo.osr.SpatialReference()
WGS84.ImportFromEPSG(4326)
transform=osgeo.osr.CoordinateTransformation(CRS83,WGS84)
standAloneList={}

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
    string = unicode(string)
  
    if self.waiting == True:
      if self.string == None:
        self.string = string
      else :
        self.string = self.string + string

class RoadMatchHandler(sax.ContentHandler):
  count = 0
  
  string = None
    
  waiting = False
  feature = False
  attribute = None
  nid = None
  split=None
  
  def __init__(self):
    print "Starting to process Exclusion information..."
  
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
        standAloneList[self.nid]=1

  def characters(self,string):
    string = unicode(string)
  
    if self.waiting == True:
      if self.string == None:
        self.string = string
      else :
        self.string = self.string + string


class geomHandler(sax.ContentHandler):

  count = 0
  boundary=None
  attribution = "GeoBase®"
  source = "Geobase_Import_2009"

  roadSegment = None
  ferrySegment = None
  junctionPoint = None
  way = None
  nodes = None
  place=None
  tags = None
  coords = []
  cur_nodes=[]
  nodeid = -1000
  junctionList=[]
  waiting = False
  waitingCoord = False  
  string = None
  cstring = None
  way_map={}
  #A map of nodes keyed on the lat/lon
  node_map={}
  #A map of nodes keyed on id
  node_map_id={}
  nid=None
  osm_ways = sets.Set()
  osm_nodes= sets.Set()
  osm_standalone=sets.Set()
  dataset=None
  
  def counter(self):
    if self.count % 5000 == 0:
      printStr( self.count )
    self.count += 1

  def __init__(self):
    printStr( "Starting to process GML..." )
    self.depth = 0
    return

  def startDocument(self):
    self.depth = self.depth + 1
    return
      
  def endDocument(self):
    printStr( "Finished processing." )
          
  def startElement(self, name, attributes):
    #print name
    # A blocked passage (barrier across road)
    if name == "nrn:BlockedPassage":
      self.counter()
      
    # A junction of two or more roads, a dead end road, a ferry route and a road, or the edge of a road on a dataset boundary
    if name == "nrn:Junction":
      self.junctionPoint=True
      self.intersection=False
      self.tags={}
      self.counter()
      
    # A toll point (physical or virtual)
    if name == "nrn:TollPoint":
      self.counter()
    
    # A ferry connection
    if name == 'nrn:FerryConnectionSegment':
      self.ferrySegment = True
      self.way = ET.Element("way",visible="true",id=str(self.nodeid))
      self.tags = {}
      self.nodeid -= 1
      
      self.counter()
      
    # A road 'way'
    if name == "nrn:RoadSegment":
      self.roadSegment = True
      self.way = ET.Element("way",visible="true",id=str(self.nodeid))
      self.tags = {}
      self.nodeid -= 1

      self.counter()

    if self.ferrySegment == True or self.roadSegment == True:
      if name == 'nrn:nid':
        self.waiting = True
        
      if name == 'nrn:ferrySegmentId':
        self.waiting = True
      
      if name == 'nrn:roadSegmentId':
        self.waiting = True
        
      if name == 'nrn:functionalRoadClass':
        self.waiting = True
              
      if name == 'nrn:numberOfLanes' :
        self.waiting = True
              
      if name == 'nrn:pavementStatus' :
        self.waiting = True
              
      if name == 'nrn:routeNameEnglish1' :
        self.waiting = True
            
      if name == 'nrn:routeNameFrench1' :
        self.waiting = True
              
      if name == 'nrn:routeNumber1':
        self.waiting = True
            
      if name == 'nrn:datasetName':
        self.waiting = True
      if name == 'nrn:right_OfficialPlaceName':
        self.waiting=True
      if name == 'nrn:right_OfficialStreetNameConcat':
        self.waiting=True
      if name == 'nrn:structureType':
        self.waiting = True
      if name == 'nrn:acquisitionTechnique':
          self.waiting=True      
    if name == 'nrn:junctionType':
      self.waiting = True 
    if self.roadSegment == True or self.ferrySegment == True or self.junctionPoint==True:      
        if name == 'gml:coordinates':
            self.string = None
            self.waiting = False
            self.waitingCoord = True
          
    self.depth = self.depth + 1
    return

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

  def endElement(self, name):
    name = unicode(name)
    
    # Prepare a cleaned and stripped text string
    text = self.string
    
    if text != None:
        text = self.string.strip()
    if name == 'nrn:Junction' and self.junctionPoint == True and ( self.tags['junctionType']=='Intersection' or self.tags['junctionType']=='Ferry') :
      self.appendJunction()
    if self.ferrySegment == True:      
        
      if name == 'nrn:ferrySegmentId':
        self.tags['nrn:ferrySegmentId'] = text
      
      self.tags['route'] = 'ferry'
           
    
    if self.roadSegment == True:      
            
      #if name == 'nrn:roadSegmentId':
        #self.tags['nrn:roadSegmentId'] = text
        
      if name == 'nrn:functionalRoadClass':
        self.tags['highway'] = highway[text ]
              
      if name == 'nrn:numberOfLanes' :
        self.tags['lanes'] = text
              
      if name == 'nrn:pavementStatus' and text!='Paved' :
        self.tags['surface'] = text.lower()
      
      if name == 'nrn:datasetName':
        self.tags['geobase:datasetName'] = "NRN:" + text
        self.dataset=text
      if name == 'nrn:acquisitionTechnique':
          self.tags['geobase:acquisitionTechnique']=text      
      if name == 'nrn:structureType':
        if text == 'Bridge' or text == 'Bridge Covered' or text == 'Bridge moveable' or text == 'Bridge unknown':
          # Found a bridge
          self.tags['bridge'] = 'yes'
          self.tags['layer'] = '1'
          
        elif text == 'Tunnel':
          # Found a tunnel              
          self.tags['tunnel'] = 'yes'
          self.tags['layer'] = '-1'
      if name == 'nrn:right_OfficialPlaceName' and text != 'Unknown':
        self.placeName=text;

      
    if name == 'nrn:junctionType':
        self.tags['junctionType'] = text
        
    # Features common to both roads and ferries
    if self.roadSegment == True or self.ferrySegment == True :
    
      if name == 'nrn:nid':
        self.tags['geobase:uuid'] = text
        
      if name == 'nrn:routeNumber1' and text!="None" and text != None:
        self.tags['ref'] = set([stringToInteger(text)])
        
      if self.tags.has_key('ref'):
        if name == 'nrn:routeNumber2' and text!="None" and text != None:
          self.tags['ref'].add(stringToInteger(text))
          
        if name == 'nrn:routeNumber3' and text!="None" and text != None:
          self.tags['ref'].add(stringToInteger(text))
          
        if name == 'nrn:routeNumber4' and text!="None" and text != None:
          self.tags['ref'].add(stringToInteger(text))
      if name=='nrn:right_OfficialStreetNameConcat' and text != "None" and text!="-" and text != "Unknown" :
          self.tags['name']=text
      
      if name == 'nrn:routeNameEnglish1' and text!="None":
        self.tags['alt_name:en'] = set([text])
        self.tags['geobase:routeName1:en']=text
      if self.tags.has_key('name'):
        if name == 'nrn:routeNameEnglish2' and text!="None":
          self.tags['alt_name:en'].add(text)
          self.tags['geobase:routeName2:en']=text
        if name == 'nrn:routeNameEnglish3' and text!="None":
          self.tags['alt_name:en'].add(text)
          self.tags['geobase:routeName3:en']=text
        if name == 'nrn:routeNameEnglish4' and text!="None":
          self.tags['alt_name:en'].add(text)
          self.tags['geobase:routeName4:en']=text
      if name == 'nrn:routeNameFrench1' and text!="None":
        self.tags['alt_name:fr'] = set([text])
        self.tags['geobase:routeName1:fr']=text
      if self.tags.has_key('name:fr'):
        if name == 'nrn:routeNameFrench2' and text!="None":
          self.tags['alt_name:fr'].add(text)
          self.tags['geobase:routeName2:fr']=text
        if name == 'nrn:routeNameFrench3' and text!="None": 
          self.tags['alt_name:fr'].add(text)
          self.tags['geobase:routeName3:fr']=text
        if name == 'nrn:routeNameFrench4' and text!="None":
          self.tags['alt_name:fr'].add(text)
          self.tags['geobase:routeName4:fr']=text
    if self.roadSegment==True or self.ferrySegment==True or self.junctionPoint==True: 
      if name == 'gml:coordinates':
        self.appendCoordinates()
        
    self.string = None  
    if name == 'nrn:FerryConnectionSegment':
      pass
  
    if name=='nrn:RoadSegment':
      
      if self.dataset == 'Newfoundland and Labrador':
        pass
        
      if self.dataset == 'Nova Scotia':
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
        
      if self.dataset == 'Prince Edward Island':
        if self.tags.has_key('ref'):
        
          if self.tags.has_key('highway'):
          
            for ref in self.tags['ref']:

              # Tag Transcanada as trunk
              if ref == 1:
                self.tags['highway'] = 'trunk'
            
      if self.dataset == 'New Brunswick':
        if self.tags.has_key('ref'):
        
          if self.tags.has_key('highway'):
            
            for ref in self.tags['ref']:
            
              if ref >= 1 and ref < 100:
                self.tags['highway'] = 'primary'
                
              if ref >= 100 and ref < 200:
                self.tags['highway'] = 'secondary'
                
              if ref >= 200 and ref <= 1000:
                self.tags['highway'] = 'tertiary'
                
      if self.dataset == 'Quebec':

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
        
      if self.dataset == 'Ontario':
        if self.tags.has_key('ref'):
          
          if self.tags.has_key('highway'):
          
            for ref in self.tags['ref']:
              # Primary (1-101)
              if ref <= 101:
                self.tags['highway'] = 'primary'
                
              # Secondary (102 and up)
              if ref > 101:
                self.tags['highway'] = 'secondary'
            
              # Tag Transcanada/Yellowhead as trunk
              if ref == 17 or ref == 11 or ref == 69 or ref == 12 or ref == 7 or ref == 115 or ref == 71:
                self.tags['highway'] = 'trunk'
                
              # 400-series as motorway
              if ref >= 400 and ref < 500:
                self.tags['highway'] = 'motorway'
        
      if self.dataset == 'Manitoba':
        if self.tags.has_key('ref'):
          
          if self.tags.has_key('highway'):
          
            for ref in self.tags['ref']:
            
              # Primary (1-101)
              if ref <= 101:
                self.tags['highway'] = 'primary'
              
              # Secondary (102 and up)
              if ref > 101:
                self.tags['highway'] = 'secondary'
            
              # Tag Transcanada/Yellowhead as trunk
              if ref == 1 or ref == 16 or ref == 75 or ref == 100:
                self.tags['highway'] = 'trunk'
              
      if self.dataset == 'Saskatchewan':
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
                
      if self.dataset == 'Alberta':

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
                
      if self.dataset == 'British Columbia':
        if self.tags.has_key('ref'):
        
          if self.tags['highway'] == 'primary' or self.tags['highway'] == 'secondary':
          
            for ref in self.tags['ref']:
            
              # Tag Transcanada/Yellowhead as trunk 
              if ref == 1 or ref == 2 or ref == 3 or ref ==5 or ref == 16 or ref == 17 or ref == 97 or ref == 99:
                self.tags['highway'] = 'trunk'
      
      if self.dataset == 'Yukon Territory':
        if self.tags.has_key('ref'):
        
          if self.tags.has_key('highway'):
  
            # Tag Transcanada/Yellowhead as trunk
            for ref in self.tags['ref']:
              if ref == 1 or ref == 2:
                self.tags['highway'] = 'trunk'
      
      if self.dataset == 'Northwest Territories':
        if self.tags.has_key('ref'):
        
          if self.tags.has_key('highway'):
            
            # Tag Transcanada/Yellowhead as trunk
            for ref in self.tags['ref']:
              if ref == 1 or ref == 2 or ref == 3:
                self.tags['highway'] = 'trunk'
        
      if self.dataset == 'Nunavut':
        pass
        
      # Catch all transcanada/yellowhead highway segments that we may have missed  
      if self.tags.has_key('alt_name:en'):
        if ('TransCanada Highway' in self.tags['alt_name:en'] or 'Yellowhead Highway' in self.tags['alt_name:en']) and self.tags['highway'] != 'motorway':
          self.tags['highway'] = 'trunk'
          if 'TransCanada Highway' in self.tags['alt_name:en']:
            self.tags['nat_name:en']='TransCanada Highway'
            self.tags['nat_name:fr']='Route Transcanadienne'
          if 'Yellowhead Highway' in self.tags['alt_name:en']:
            self.tags['nat_name:en']='Yellowhead Highway'

    if name == 'nrn:FerryConnectionSegment' or name=='nrn:RoadSegment':
      shape=LineString(self.coords)
      #print "Checking Intersection on %s"  % str(self.coords)
      if (self.boundary==None or  self.boundary.intersects(shape) ):
        # Convert the sets back to semicolon separated strings      
        if self.tags.has_key('ref'):      
          self.tags['ref'] = setToString(self.tags['ref']);

       

        if self.tags.has_key('name')==False:
          if self.dataset <> 'Quebec' and self.tags.has_key('alt_name:en'):
            self.tags['name']=setToString(self.tags['alt_name:en'])
          elif self.tags.has_key('alt_name:fr'):
            self.tags['name']=setToString(self.tags['alt_name:fr'])
        if self.tags.has_key('alt_name:en'):
          self.tags['alt_name:en'] = setToString(self.tags['alt_name:en']);

        if self.tags.has_key('alt_name:fr'):
          self.tags['alt_name:fr'] = setToString(self.tags['alt_name:fr']);  
        if self.placeName != None:
          self.placeName=self.placeName + ',' + self.dataset
        else:          
          self.placeName=self.dataset
        if self.placeName != None:
          #Only set is in, if we at least have a province.
          self.placeName=self.placeName + ',Canada'
          self.tags['is_in']=self.placeName
          # Convert the tags to xml nodes
        for key, value in self.tags.iteritems():
          self.way.append(ET.Element('tag', k=key,v=value))
        self.nid=self.tags['geobase:uuid']
        self.tags = None
              
        # add the default tags
        self.appendWay()
      

                
      # Reset the tracking variables
      self.way = None
      self.roadSegment = False
      self.ferrySegment = False
      self.coords=[]
      self.cur_nodes=[]
      self.junctionPoint=False
      self.waitingCoord
      self.nid=None
      self.placeName=None
      self.dataset=None
    self.waiting = False
    self.depth = self.depth - 1
    return

  def mergeNodes(self,coords):
    #Get the list of nodes + the ways that terminate at those nodes
    #merge them into a common node and update the other ways.
    if self.node_map.has_key((coords[0],coords[1])) :
      node_list = self.node_map[(coords[0],coords[1])]
      keep = node_list[0]
      keep_id = keep.get('id')
      for n in node_list[1:]:
      #Find all ways using n, and update them to use keep
        for w in self.way_map[n.get('id')] :
          for elem in w.getiterator('nd'):
            if elem.get('ref')==n.get('id') and elem.get('ref') != keep_id:
              elem.set('ref',keep_id)
          #Now delete n, it should not be used anywhere
          self.osm_nodes.remove(n)
    return
  def appendJunction(self):
  # Check if junction is in bounds
  #coord_t=transform.TransformPoint(float(self.coords[0][0]),float(self.coords[0][1]),0.0)
    coord_t=(self.coords[0][0],self.coords[0][1])
    shape=Point(coord_t[0],coord_t[1])
    if self.boundary==None or  self.boundary.intersects(shape) :       
      #Record the junction coordinates for merging later
      #merging must happen after parsing because some RoadSegments
      #might be defined after the junction
      self.junctionList.append((self.coords[0][0],self.coords[0][1]))
    return


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
          if self.roadSegment==True or self.ferrySegment==True:
              node = ET.Element("node", visible='true', id=str(self.nodeid), lat=str(lat), lon=str(lon))

              # Add default tags
              #node.append(ET.Element('tag', k='attribution',v=self.attribution))
              #node.append(ET.Element('tag', k='source',v=self.source))

              # Add a reference to this node to the current way
              self.way.append(ET.Element('nd', ref=str(self.nodeid)))

              # Add this node to the main document
              self.cur_nodes.append(node)

              self.nodeid -= 1
          #Append coordinates to coords array
          self.coords.append([lon,lat])
        else: 
          printStr( "Could not add node due to empty coordinate" )
          printStr( "set = " + coordset )
          printStr( "str = " + self.cstring )
    self.cstring = None

    self.waitingCoord = False
    self.waiting = True

  def appendWay(self):
    self.way.append(ET.Element('tag', k='attribution',v=self.attribution))
    self.way.append(ET.Element('tag', k='source',v=self.source))   
    for n in self.cur_nodes:
      self.osm_nodes.add(n)
      lat_attr = n.get('lat')
      lon_attr = n.get('lon')
      if self.node_map.has_key((lon_attr,lat_attr)) :
        self.node_map[(lon_attr,lat_attr)].append(n)
      else:
        self.node_map[(lon_attr,lat_attr)]=[n]
      self.node_map_id[n.attrib['id']]=n
      if self.way_map.has_key(n.attrib['id']) :
        self.way_map[n.attrib['id']].append(self.way)
      else:
        self.way_map[n.attrib['id']]=[self.way]
    # add this way to the main document
    
    if  standAloneList.has_key(self.nid):
      self.osm_standalone.add(self.way)
    self.osm_ways.add(self.way)
      
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
    print "Standalone List Size %d" % (len(standAloneList))
  
  # If we were given the addr file parse it
  if options.addrfile != None and len(options.addrfile) > 0:
    printStr( "Preparing to read '"+options.addrfile+"'" )
    
    handler = addrHandler()
    
    sax.parse(open(options.addrfile), handler)
  
  # Then parse the geom file
  printStr( "Preparing to read '"+options.geomfile+"'" )
  
  handler = geomHandler()
  handler.boundary=boundary
  sax.parse(open(options.geomfile), handler)
  printStr( "Parse of geomfile done" )

  for junct in handler.junctionList:
    #Find the 'nodes' at these coordinates and merge them IF
    # the junction is an intersection
    handler.mergeNodes((str(junct[0]),str(junct[1])))

  #
  # If we ran with a standalone list, then we need to generate the standalone
  # output file.
  # The standalone output file contains just ways.
  # we need to find only the nodes referenced by the standalone list.
  
  complete_osm=ET.Element("osm", generator='geobase2osm', version='0.5')
  for n in handler.osm_nodes:
    complete_osm.append(n)
  for w in handler.osm_ways:
    complete_osm.append(w)
  
  
  # Format the code by default
  if options.indent:
    printStr( "Formatting output" )
    for e in handler.osm_ways:
        indent(e)
    for e in handler.osm_nodes:        
        indent(e)
    
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

  if options.standAloneFile != None:
    standalone_nodes=sets.Set()
    print "Size of Standalone ways:%d" % (len(handler.osm_standalone))
    for way in handler.osm_standalone:
      for node in way.getiterator('nd'):
        if node.get('ref') not in standalone_nodes:
          #Find the node with the ref
          standalone_nodes.add(node.get('ref'))

    #Now For each XML node in handler.osm_nodes that is in the set, write it
    standalone_osm=ET.Element("osm", generator='geobase2osm', version='0.5')
    excluded_osm=ET.Element("osm", generator='geobase2osm', version='0.5')
    excluded_nodes=sets.Set()
    for n in handler.osm_nodes:
      if n.get('id') in standalone_nodes:
        standalone_osm.append(n)
    print  "Scanning ways for standalones"
    for w in handler.osm_ways:
        if w in handler.osm_standalone:
            standalone_osm.append(w)
        else:
            excluded_osm.append(w)
            #Now Walk through excluded ways finding the excluded nodes
            #This might contain duplicate nodes with the standalone set
            #because junction nodes can be on a way in each set
            for node in w.getiterator('nd'):
                if node.get('ref') not in excluded_nodes:
                    excluded_nodes.add(node.get('ref'))
    print "Scanning excluded nodes"
    for nref in excluded_nodes:
        excluded_osm.insert(0,handler.node_map_id[nref])
    print "Writing standalone"
    f = open(options.outputfile+".standalone.osm",'w')
    f.write(ET.tostring(standalone_osm))
    f.close();
    print "Writing excluded"
    f = open(options.outputfile+".excluded.osm",'w')
    f.write(ET.tostring(excluded_osm))
    f.close();
    print "Done"
if __name__ == "__main__":
    main()
