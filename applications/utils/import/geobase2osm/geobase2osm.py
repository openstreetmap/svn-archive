import cElementTree as ET
import sys
import codecs
import optparse

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


def main():
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
  
  nrn = ET.parse(options.geomfile)
  
  nodeid = -1
  wayid = -1
  
  print "Transforming GML to OSM"
  
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
