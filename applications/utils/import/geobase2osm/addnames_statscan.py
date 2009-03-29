#!/usr/bin/python
# -*- coding: utf-8 -*-
#
# This script will read in a .osm file and scan it for
# ways with no names.
#
# If the way has a geobase:uuid it will search the
# result file generated from RoadMatcher matching GeoBase roads
# against StatsCan roads.  If a match is found it will add the
# StatsCan road name to the way.
#
# The resulting OSM file is then generated.
#
# This script parses the entire OSM input file into memory.
# Large OSM files will require large amounts of RAM.
# 
#
import xml.etree.cElementTree as ET
import sets
import sys
import optparse
import gzip
import string
from xml import sax
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



usage="usage: %prog -i input.osm -n Result.names.gml -o outputfile.osm"
parser = OptionParser(usage)
parser.add_option("-i", "--input", dest="inputfile", help="read data from OSM file")
parser.add_option("-n", "--names", dest="namesfile", help="")
parser.add_option("-o", "--output", dest="outputfile", help="store data to OUTPUTFILE")

(options, args) = parser.parse_args()

nameMatchHandler = NameMatchHandler()
sax.parse(open(options.namesfile),nameMatchHandler)

osm_tree = ET.parse(options.inputfile)
#
# Walk through all OSM ways checking for ones with no name
#
for way in osm_tree.findall('way'):
    named=False
    uuid=None
    for tag in way.findall('tag'):
        if tag.attrib['k'] == 'name':
            named=True
        elif tag.attrib['k'] == 'geobase:uuid':
            uuid=tag.attrib['v']

    if named == False and uuid != None:
        #Apply StatsCan Name        
        if nameMatchHandler.nameHash.has_key(uuid):
            
            nameTags=nameMatchHandler.nameHash[uuid]
            name = nameTags['NAME']
            if nameTags['TYPE'] != None:
                name = name + expandStreetType(nameTags['TYPE'])
            if nameTags['DIRECTION'] != None:
                name = name + ' ' + nameTags['DIRECTION']
            way.append(ET.Element('tag', k='name', v=name))
            way.append(ET.Element('tag',k='statscan:rbuid',v=nameTags['RB_UID']))
        #has key
    #not named
#for

osm_tree.write(options.outputfile)

