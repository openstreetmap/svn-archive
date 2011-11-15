#!/usr/bin/python
##
##  Author: Bryce Nesbitt, November 2011
##  Licence: Public Domain, no rights reserved
##
##  osmsync module for WeTap / http://findafountain.org/
##
##  See also:
##      http://wiki.openstreetmap.org/wiki/Tag:amenity%3Ddrinking_water
##
import sys
sys.path = ['../../applications/utils/import/osmsync/'] + sys.path
from osmsync import osmsync

import re, urllib, urllib2
import csv, demjson
import xml.sax.saxutils
import zipfile

from   pprint     import pprint
from   xml.etree  import ElementTree
from   StringIO   import StringIO

fafurl = 'http://findafountain.org/fountains/osm_fountains.xml'

class osmsync_findafountain(osmsync):

    #  Sample data:
    #   
    #   <node uid="355242" lat="51.5139" user="Peter Gleick" id="-1" lon="-0.112245">
    #   <tag v="drinking_water" k="amenity"/>
    #   <tag v="faf" k="source"/>
    #   <tag v="16" k="faf:id"/>
    #   <tag v="St Dunstan's-in-the-West" k="faf:title"/>
    #   <tag v="St Dunstan's-in-the-West Church" k="faf:note"/>
    #   <tag v="http://findafountain.org/system/28/original/
    #   uk-greater-london-london-corporation-of-london-st-dunstans-in-
    #       the-west-broken-fountain.jpg" k="faf:photo"/>
    #   <tag v="Broken Fountain" k="faf:category"/>
    #   <tag v="3" k="faf:category_id"/>
    #   </node>
    # 
    def fetch_source(self, sourcedata):

        # Load the NOAA data, decompress, and parse the XML
        sourcenodes = {}
#        req      = urllib2.Request(sourcedata, headers=self.http_headers);
#        response = urllib2.urlopen(req)
#
#        compressed_stream = StringIO( response.read () )
#        plaintext_stream = zipfile.ZipFile(compressed_stream,'r')
#        tree    = ElementTree.parse(response)
        tree    = ElementTree.parse("faf.xml")
    
        for node in tree.iter('node'):
            tags = {}
            for tag in node.iter('tag'):
                tags[tag.attrib.get('k')] = xml.sax.saxutils.escape(tag.attrib.get('v'))
            pkey = tags.get('faf:id')

            # Build output record
            out = {}
            out['lat']	= node.attrib.get('lat')
            out['lon']	= node.attrib.get('lon')
            out['tag']  = {}

            # 1=outdoor 2=indoor 3=broken
            category_id = tags.get('faf:category_id')
            if  ( category_id == '1' ):
                out['tag']['maintenance'] = 'ok'
            else:
                continue

            out['tag']['amenity']       = 'drinking_water'
            out['tag']['source']        = 'osmsync:findafountain'
            out['tag']['source:pkey']   = pkey
            out['tag']['name']          = tags.get('faf:title')
            out['tag']['description']   = tags.get('faf:note')
            out['tag']['image']         = tags.get('faf:photo')
            
            sourcenodes[pkey] = out

        source_is_master_for=['amenity']
        return(sourcenodes, source_is_master_for)

#######################################################################################
changeSetTags = {
    'source'            : 'osmsync:findafountain',
    'source:website'    : 'http://findafountain.org/',
    'conflation_key'    : 'source:pkey',
    'note'              : 'Prepared for human review by osmsync: '+
                          'Find-A-Fountain'
    }
myfetch = osmsync_findafountain()
myfetch.run(description='FindAFountain',
            ext_url='http://findafountain.org/fountains/osm_fountains.xml',
            osm_url=osmsync.xapi_url + urllib.quote('node[source=osmsync:findafountain]')
            )
myfetch.output(changeSetTags)
