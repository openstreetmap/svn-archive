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
highway["Local / Unknown"] = "unclassified"
highway["Alleyway / Lane"] = "service"
highway["Ramp"] = "unclassified"
highway["Service Lane"] = "service"
highway["Resource / Recreation"] = "unclassified"
highway["Local / Strata"] = "unclassified"

highway["Rapid Transit"] = ""
highway["Winter"] = ""

attribution = 'Geobase.ca NRN'
date = ''

import string
import sys
from xml import sax
from operator import itemgetter

class gmlHandler(sax.ContentHandler):

  count = 0

  attribution = "Geobase"
  source = "Geobase Import 2008"

  roadSegment = None
  way = None
  nodes = None
  
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
    
    # A blocked passage (dead end)
    if name == "nrn:BlockedPassage":
      self.counter()
    
    # A road 'way'
    if name == "nrn:RoadSegment":
      self.roadSegment = True
      self.way = ET.Element("way",visible="true",id=str(self.nodeid))
      self.nodeid -= 1

      self.counter()

    if self.roadSegment == True:
      if name == 'nrn:nid':
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
    if self.roadSegment == True:
      if name == 'nrn:nid':
        self.way.append(ET.Element('tag', k="nrn:nid",v=self.string))
        #print self.string
      
      if name == 'nrn:roadSegmentId':
        self.way.append(ET.Element('tag', k="nrn:roadSegmentId",v=self.string))
        
      if name == 'nrn:functionalRoadClass':
        #print self.string
        self.way.append(ET.Element('tag', k="highway",v=highway[self.string.strip() ]))
              
      if name == 'nrn:numberOfLanes' :
        self.way.append(ET.Element('tag', k="lanes",v=self.string))
              
      if name == 'nrn:pavementStatus' and self.string!='Paved' :
        self.way.append(ET.Element('tag', k="surface",v=self.string))
              
      if name == 'nrn:routeNameEnglish1' and self.string!='None' :
        self.way.append(ET.Element('tag', k="name",v=self.string))
            
      if name == 'nrn:routeNameFrench1' and self.string!='None':
        self.way.append(ET.Element('tag', k="name:fr",v=self.string))
              
      if name == 'nrn:routeNumber1' and self.string!='None':
        self.way.append(ET.Element('tag', k="ref",v=self.string))
            
      if name == 'nrn:datasetName':
        self.way.append(ET.Element('tag', k="nrn:datasetName",v=self.string))
            
      if name == 'nrn:structureType':
        if self.string == 'Bridge' or self.string == 'Bridge Covered' or self.string == 'Bridge moveable' or self.string == 'Bridge unknown':
          # Found a bridge
          self.way.append(ET.Element('tag', k="bridge",v="yes"))
          self.way.append(ET.Element('tag', k="layer",v="1"))
          
        elif self.string == 'Tunnel':
          # Found a tunnel              
          self.way.append(ET.Element('tag', k="tunnel",v="yes"))
          self.way.append(ET.Element('tag', k="layer",v="-1"))
  
      self.string = None
  
      if name == 'gml:coordinates':
        for set in self.cstring.split(' '):
          
          coord = set.split(',')
          
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
              print "set = " + set
              print "str = " + self.cstring
        
        self.cstring = None
      
        self.waitingCoord = False
        self.waiting = True
  
    if name=='nrn:RoadSegment':
    
      # add the default tags
      self.way.append(ET.Element('tag', k='attribution',v=self.attribution))
      self.way.append(ET.Element('tag', k='source',v=self.source))
    
      # add this way to the main document
      self.osm.append(self.way)
      
      # Reset the tracking variables
      self.way = None
      self.roadSegment = False
    
    self.depth = self.depth - 1
    return

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

  sax.parse(open(options.geomfile), handler)


  # Format the code by default
  if options.indent:
    print "Formatting output"
    indent(handler.osm)
  
  print "Saving to '" + options.outputfile + "'"
  
  f = open(options.outputfile, 'w')
  f.write(ET.tostring(handler.osm))

def main2():
  # Handle arguments
  
  usage = "usage: %prog -i NRN_GEOM.gml [-a NRN_ADDR.gml] [-o outfilefile.osm] [--pretty]"
  parser = OptionParser(usage)
  parser.add_option("-i", dest="geomfile", help="read data from GEOMFILE")
  parser.add_option("-a", dest="addrfile", help="read optional data from ADDRFILE")
  parser.add_option("-o", dest="outputfile", default="geobase.osm", help="store data to OUTPUTFILE")  
  parser.add_option("--pretty", action="store_true", dest="indent", help="stylize the output file")
    
  (options, args) = parser.parse_args()

  parser.check_required("-i")
  
  # Do the actual work
  print "Reading NRN data file '" + options.geomfile + "'"
  
  osm = ET.Element("osm", generator='nrn2osm', version='0.5')
  
  #nrn = ET.parse(options.geomfile)
  
  nodeid = -1
  wayid = -1
  
  print "Transforming GML to OSM"
  
  for event, nrn in ET.iterparse(options.geomfile,events=("start","end")):
    
    for feature in nrn.findall('{http://www.opengis.net/gml}featureMember'):
      for element in feature.findall(nrn_ns + 'RoadSegment'):
      
        way = ET.Element("way", id=str(wayid))
        wayid -= 1
  
        way_tags = {}
      
        for road in element.findall('*'):
          
          if road.text != 'None':
            if road.tag == nrn_ns + 'nid':
              way_tags['nrn:nid'] = road.text
              
            elif road.tag == nrn_ns + 'roadSegmentId' :
               way_tags['nrn:roadsegmentid'] = road.text
              
            elif road.tag == nrn_ns + 'functionalRoadClass':
              way_tags['highway'] = highway[road.text]
              
            elif road.tag == nrn_ns + 'numberOfLanes' :
              way_tags['lanes'] = road.text
              
            elif road.tag == nrn_ns + 'pavementStatus' :
              way_tags['surface'] = road.text
              
            elif road.tag == nrn_ns + 'routeNameEnglish1' :
              way_tags['name'] = road.text
            
            elif road.tag == nrn_ns + 'routeNameFrench1' :
              way_tags['name:fr'] = road.text
              
            elif road.tag == nrn_ns + 'routeNumber1':
              way_tags['ref'] = road.text
            
            elif road.tag == nrn_ns + 'datasetName':
              way_tags['nrn:datasetName'] = road.text
            
            elif road.tag == nrn_ns + 'structureType':
              if road.text == 'Bridge' or road.text == 'Bridge Covered' or road.text == 'Bridge moveable' or road.text == 'Bridge unknown':
                # Found a bridge
                way_tags['bridge'] = 'yes'
                
              elif road.text == 'Tunnel':
                # Found a tunnel              
                way_tags['tunnel'] = 'yes'
            
          for lineString in road.findall('*/*'):
            for set in lineString.text.split(' '):
              coord = set.split(',')
              
              # Example node:
              # <node id='-1' visible='true' lat='50.948611006929525' lon='-114.68740649854398' />          
              node = ET.Element("node", visible='true', id=str(nodeid), lat=str(coord[1]), lon=str(coord[0]))
              
              # Add the node reference to the way
              # <nd ref='-566' />
              way.append(ET.Element('nd', ref=str(nodeid)))
              
              # Add the source to the node
              # <tag k='source' v='123' />
              
              node.append(ET.Element('tag', k='attribution',v=attribution))
              node.append(ET.Element('tag', k='source',v='geobase_import_'+date))
              
              nodeid -= 1
              
              osm.append(node)
          
          # Apply the post process rules on the way
          # ---
          # These rules will modify fields to fit with the Canadian tagging guidelines. Some rules can be expressed nationally, others, such as highway
          # types may need to be done on a province by province basis
        
          if way_tags.has_key('ref') and way_tags.has_key('name'):
          
            if way_tags['nrn:datasetName'] == 'Prince Edward Island' or way_tags['nrn:datasetName'] == 'Newfoundland':
              
              if way_tags['ref'] == str(1) and way_tags['name'] == 'TransCanada Highway':
                way_tags['highway'] = 'trunk'
              
  
          if way_tags.has_key('ref'):     
            if way_tags['nrn:datasetName'] == 'Prince Edward Island' or way_tags['nrn:datasetName'] == 'Newfoundland':
              if convertStr(way_tags['ref']) > 1 and convertStr(way_tags['ref']) <= 4:
                way_tags['highway'] = 'primary'
                
              if convertStr(way_tags['ref']) > 4 and convertStr(way_tags['ref']) < 100:
                way_tags['highway'] = 'secondary'
                
            """
            elif way_tags['nrn:datasetName'] == 'Nova Scotia':
            
            elif way_tags['nrn:datasetName'] == 'Alberta':
            
            elif way_tags['nrn:datasetName'] == 'British Columbia':
            
            elif way_tags['nrn:datasetName'] == 'Saskatchewan':
            
            elif way_tags['nrn:datasetName'] == 'Manitoba':
            
            elif way_tags['nrn:datasetName'] == 'Newbrunswick':
             
            elif way_tags['nrn:datasetName'] == 'Quebec':
            
            elif way_tags['nrn:datasetName'] == 'Ontario':
            
            elif way_tags['nrn:datasetName'] == 'Newbrunswick':
            
            elif way_tags['nrn:datasetName'] == 'Yukon':
            
            elif way_tags['nrn:datasetName'] == 'Northwest Territories':
            
            elif way_tags['nrn:datasetName'] == 'Nunavut':
            
            """
  
        
        # Turn the dictionary of tags into their xml representation
        for key,value in way_tags.iteritems():
          way.append(ET.Element("tag",k=key,v=value))
        
        del(way_tags)        
          
        way.append(ET.Element("tag", k='attribution', v=attribution))
        way.append(ET.Element('tag', k='source',v='geobase_import_'+date))
              
        osm.append(way)
      
  # Format the code by default
  if options.indent:
    print "Formatting output"
    indent(osm)
  
  print "Saving to '" + options.outputfile + "'"
  
  f = open(options.outputfile, 'w')
  f.write(ET.tostring(osm))

if __name__ == "__main__":
    main()
