 #!/usr/bin/python
# -*- coding: utf-8 -*-

import cElementTree as ET
import sys
import codecs
import optparse

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

import string
import sys
from xml import sax
from operator import itemgetter

class gmlHandler(sax.ContentHandler):

  count = 0

  attribution = "GeoBase®"
  source = "GeobaseImport2008"

  roadSegment = None
  ferrySegment = None
  
  way = None
  nodes = None
  
  tags = None
  
  nodeid = -1000
  
  waiting = False
  waitingCoord = False
  
  string = None
  cstring = None
  
  osm = ET.Element("osm", generator='geobase2osm', version='0.5')

  def counter(self):
    if self.count % 5000 == 0:
      print self.count
    self.count += 1

  def __init__(self):
    print "Starting to process GML..."
    self.depth = 0
    return

  def startDocument(self):
    self.depth = self.depth + 1
    return
      
  def endDocument(self):
    print "Finished processing."
          
  def startElement(self, name, attributes):
    
    # A blocked passage (barrier across road)
    if name == "nrn:BlockedPassage":
      self.counter()
      
    # A junction of two or more roads, a dead end road, a ferry route and a road, or the edge of a road on a dataset boundary
    if name == "nrn:Junction":
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
            
      if name == 'nrn:structureType':
        self.waiting = True
          
    if self.roadSegment == True or self.ferrySegment == True:      
      if name == 'gml:coordinates':
        self.string = None
        self.waiting = False
        self.waitingCoord = True
          
    self.depth = self.depth + 1
    return

  def characters(self,string):
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
  
    # Prepare a cleaned and stripped text string
    text = self.string
    
    if text != None:
        text = self.string.strip()
        
    if self.ferrySegment == True:      
        
      if name == 'nrn:ferrySegmentId':
        self.tags['nrn:ferrySegmentId'] = text
      
      self.tags['route'] = 'ferry'
      
      self.string = None
  
    if self.roadSegment == True:      
            
      if name == 'nrn:roadSegmentId':
        self.tags['nrn:roadSegmentId'] = text
        
      if name == 'nrn:functionalRoadClass':
        self.tags['highway'] = highway[text ]
              
      if name == 'nrn:numberOfLanes' :
        self.tags['lanes'] = text
              
      if name == 'nrn:pavementStatus' and text!='Paved' :
        self.tags['surface'] = text.lower()
      
      if name == 'nrn:datasetName':
        self.tags['nrn:datasetName'] = text
            
      if name == 'nrn:structureType':
        if text == 'Bridge' or text == 'Bridge Covered' or text == 'Bridge moveable' or text == 'Bridge unknown':
          # Found a bridge
          self.tags['bridge'] = 'yes'
          self.tags['layer'] = '1'
          
        elif text == 'Tunnel':
          # Found a tunnel              
          self.tags['tunnel'] = 'yes'
          self.tags['layer'] = '-1'
  
      self.string = None
  
    # Features common to both roads and ferries
    if self.roadSegment == True or self.ferrySegment == True:
    
      if name == 'nrn:nid':
        self.tags['nrn:nid'] = text
        
      if name == 'nrn:routeNumber1' and text!='None':
        self.tags['ref'] = set([text])
        
      if self.tags.has_key('ref'):
        if name == 'nrn:routeNumber2' and text!='None':
          self.tags['ref'].add(text)
          
        if name == 'nrn:routeNumber3' and text!='None':
          self.tags['ref'].add(text)
          
        if name == 'nrn:routeNumber4' and text!='None':
          self.tags['ref'].add(text)
          
      if name == 'nrn:routeNameEnglish1' and text!='None':
        self.tags['name'] = set([text])
        
      if self.tags.has_key('name'):
        if name == 'nrn:routeNameEnglish2' and text!='None':
          self.tags['name'].add(text)
          
        if name == 'nrn:routeNameEnglish3' and text!='None':
          self.tags['name'].add(text)
          
        if name == 'nrn:routeNameEnglish4' and text!='None':
          self.tags['name'].add(text)
      
      if name == 'nrn:routeNameFrench1' and text!='None':
        self.tags['name:fr'] = set([text])
        
      if self.tags.has_key('name:fr'):
        if name == 'nrn:routeNameFrench2' and text!='None':
          self.tags['name:fr'].add(text)
          
        if name == 'nrn:routeNameFrench3' and text!='None': 
          self.tags['name:fr'].add(text)
          
        if name == 'nrn:routeNameFrench4' and text!='None':
          self.tags['name:fr'].add(text)
      
      if name == 'gml:coordinates':
        for coordset in self.cstring.split(' '):
          
          coord = coordset.split(',')
          
          if len(coord) == 2:
            lon = coord[0]
            lat = coord[1]
            
            if len(lat) > 0 and len(lon) > 0:
              node = ET.Element("node", visible='true', id=str(self.nodeid), lat=lat, lon=lon)
            
              # Add default tags
              node.append(ET.Element('tag', k='attribution',v=self.attribution))
              node.append(ET.Element('tag', k='source',v=self.source))
            
              # Add a reference to this node to the current way
              self.way.append(ET.Element('nd', ref=str(self.nodeid)))
            
              # Add this node to the main document
              self.osm.append(node)
            
              self.nodeid -= 1
            else: 
              print "Could not add node due to empty coordinate"
              print "set = " + coordset
              print "str = " + self.cstring
        
        self.cstring = None
      
        self.waitingCoord = False
        self.waiting = True
  
    if name == 'nrn:FerryConnectionSegment':
      pass
  
    if name=='nrn:RoadSegment':
      
      if self.tags['nrn:datasetName'] == 'Newfoundland and Labrador':
        pass
        
      if self.tags['nrn:datasetName'] == 'Nova Scotia':
        pass
        
      if self.tags['nrn:datasetName'] == 'Prince Edward Island':
        if self.tags.has_key('ref'):
        
          if self.tags['highway'] == 'primary' or self.tags['highway'] == 'secondary' or self.tags['highway'] == 'tertiary':
            # Tag Transcanada/Yellowhead as trunk
            if 1 in self.tags['ref'] and ( self.tags['highway'] == 'primary' or self.tags['highway'] == 'secondary'):
              self.tags['highway'] = 'trunk'
            
      if self.tags['nrn:datasetName'] == 'New Brunswick':
        pass
        
      if self.tags['nrn:datasetName'] == 'Quebec':
        pass
        
      if self.tags['nrn:datasetName'] == 'Ontario':
        if self.tags.has_key('ref'):
          
          if self.tags['highway'] == 'primary' or self.tags['highway'] == 'secondary' or self.tags['highway'] == 'tertiary':
          
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
        
      if self.tags['nrn:datasetName'] == 'Manitoba':
        if self.tags.has_key('ref'):
          
          if self.tags['highway'] == 'primary' or self.tags['highway'] == 'secondary' or self.tags['highway'] == 'tertiary':
          
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
              
      if self.tags['nrn:datasetName'] == 'Saskatchewan':
        if self.tags.has_key('ref'):
        
          if self.tags['highway'] == 'primary' or self.tags['highway'] == 'secondary' or self.tags['highway'] == 'tertiary':
            
            # Tag Transcanada/Yellowhead as trunk
            for ref in self.tags['ref']:
              if ref == 1 or ref == 16:
                self.tags['highway'] = 'trunk'
                
      if self.tags['nrn:datasetName'] == 'Alberta':

        if self.tags.has_key('ref'):
        
          if self.tags['highway'] == 'primary' or self.tags['highway'] == 'secondary' or self.tags['highway'] == 'tertiary':
          
            for ref in self.tags['ref']:
              # Primary (4-100)
              if ref >= 4 and ref <= 100:
                self.tags['highway'] = 'primary'
              
              # Secondary (500-899, 901, 940)
              if ref >= 500 and ref < 900:
                self.tags['highway'] = 'secondary'
              
              # Tag Transcanada/Yellowhead as trunk
              if ref >= 1 and ref <= 3 or ref == 16 or ref == 201 or ref == 216:
                self.tags['highway'] = 'trunk'
                
      if self.tags['nrn:datasetName'] == 'British Columbia':
        if self.tags.has_key('ref'):
        
          if self.tags['highway'] == 'primary' or self.tags['highway'] == 'secondary':
          
            for ref in self.tags['ref']:
            
              # Tag Transcanada/Yellowhead as trunk 
              if ref == 1 or ref == 16:
                self.tags['highway'] = 'trunk'
      
      if self.tags['nrn:datasetName'] == 'Yukon Territory':
        pass
      
      if self.tags['nrn:datasetName'] == 'Northwest Territories':
        pass
        
      if self.tags['nrn:datasetName'] == 'Nunavut':
        pass
        
      # Catch all transcanada/yellowhead highway segments that we may have missed
      if self.tags.has_key('name'):
        if ('TransCanada Highway' in self.tags['name'] or 'Yellowhead Highway' in self.tags['name']) and self.tags['highway'] != 'motorway':
          self.tags['highway'] = 'trunk'
    
    if name == 'nrn:FerryConnectionSegment' or name=='nrn:RoadSegment':
    
      # Convert the sets back to semicolon separated strings      
      if self.tags.has_key('ref'):      
        self.tags['ref'] = setToString(self.tags['ref']);
      
      if self.tags.has_key('name'):
        self.tags['name'] = setToString(self.tags['name']);
      
      if self.tags.has_key('name:fr'):
        self.tags['name:fr'] = setToString(self.tags['name:fr']);
        
      # Convert the tags to xml nodes
      for key, value in self.tags.iteritems():
        self.way.append(ET.Element('tag', k=key,v=value))
        
      self.tags = None
    
      # add the default tags
      self.way.append(ET.Element('tag', k='attribution',v=self.attribution))
      self.way.append(ET.Element('tag', k='source',v=self.source))
    
      # add this way to the main document
      self.osm.append(self.way)
      
      # Reset the tracking variables
      self.way = None
      self.roadSegment = False
      self.ferrySegment = False
    
    self.depth = self.depth - 1
    return

# Convert a set to a semicolon separated string
def setToString(tagset):
  
  if len(tagset) == 0:
    return ""
  
  elif len(tagset) == 1:
    string = tagset.pop()
    return string
    
  else:
    string = tagset.pop()
    
    for x in tagset:
      string = string + ";" + x

    return string

def main():

  usage = "usage: %prog -i NRN_GEOM.gml [-a NRN_ADDR.gml] [-o outfilefile.osm] [--pretty]"
  parser = OptionParser(usage)
  parser.add_option("-i", dest="geomfile", help="read data from GEOMFILE")
  parser.add_option("-a", dest="addrfile", help="read optional data from ADDRFILE")
  parser.add_option("-o", dest="outputfile", default="geobase.osm", help="store data to OUTPUTFILE")
  parser.add_option("--pretty", action="store_true", dest="indent", help="stylize the output file")
    
  (options, args) = parser.parse_args()

  parser.check_required("-i")

  saxparser = sax.make_parser()

  handler = gmlHandler()

  print "Preparing to read '"+options.geomfile+"'"

  sax.parse(open(options.geomfile), handler)


  # Format the code by default
  if options.indent:
    print "Formatting output"
    indent(handler.osm)
  
  print "Saving to '" + options.outputfile + "'"
  
  f = open(options.outputfile, 'w')
  f.write(ET.tostring(handler.osm))

if __name__ == "__main__":
    main()
