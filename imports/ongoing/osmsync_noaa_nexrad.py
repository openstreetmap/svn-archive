#!/usr/bin/python
##
##  Author: Bryce Nesbitt, June 2011
##  Licence: Public Domain, no rights reserved
##
##  osmsync module to import NOAA NEXRAD radar stations
##
##  See also:
##      http://wiki.openstreetmap.org/wiki/Potential_Datasources
##      http://wiki.openstreetmap.org/wiki/Man_made
##
##  Future work:
##        Add to changeset 'source:website=http://www.ncdc.noaa.gov/oa/radar/nexrad.kmz'
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

class osmsync_noaa_nexrad(osmsync):

    #  Sample NOAA data:
    #  <wsr>
    #    <name>KABR</name>
    #    <description><![CDATA[SITE: KABR<BR>LOCATION: ABERDEEN<BR>...]]></description>
    #    <Point>
    #      <coordinates>-98.413,45.45600000000001,0</coordinates>
    #    </Point>
    #  </wsr>
    def fetch_source(self, sourcedata):

        # Load the NOAA data, decompress, and parse the XML
        sourcenodes = {}
        req      = urllib2.Request(sourcedata, headers=self.http_headers);
        response = urllib2.urlopen(req)

        compressed_stream = StringIO( response.read () )
        plaintext_stream = zipfile.ZipFile(compressed_stream,'r')
        tree    = ElementTree.parse(plaintext_stream.open('doc.kml'))
    
        for site in tree.iter('{http://earth.google.com/kml/2.0}wsr'):
            pkey            = site.find("{http://earth.google.com/kml/2.0}name").text.strip()
            description     = site.find("{http://earth.google.com/kml/2.0}description").text.strip()
            point           = site.find("{http://earth.google.com/kml/2.0}Point")
            lon,lat,ele     = point.find("{http://earth.google.com/kml/2.0}coordinates").text.split(',')
            ele             = (float(ele)*12*2.54)/100     # feet->inches->centimeters->meters

            description     = description.replace('<BR>',"\n")
            description     = description.replace('<![CDATA[',"")
            description     = description.replace(']]',"")

            node                             = {}
            node['tag']                      = {}
            node['id']                       = pkey
            node['lat']                      = lat
            node['lon']                      = lon
           #node['tag']['ele']               = ele
            node['tag']['name']              = pkey
            node['tag']['source:pkey']       = pkey
            node['tag']['man_made']          = 'monitoring_station'
            node['tag']['monitoring_station']= 'radar'
            node['tag']['radar']             = 'NEXRAD'
           #node['tag']['note']              = description
            node['tag']['operator']          = 'NOAA'
            node['tag']['website']           = "http://www.ncdc.noaa.gov/nexradinv/chooseday.jsp?id=" + pkey
            sourcenodes[pkey] = node

        source_is_master_for=['operator','website','description','note','radar','man_made','source:pkey','monitoring_station','name']
        return(sourcenodes, source_is_master_for)

    #   Map source primary keys to osm primary keys
    def map_primary_keys(self, sourcenodes, osmnodes):
        mapping = {}
        if osmnodes:
            for osmid,node in osmnodes.items():
                pkey = node['tag'].get('name')
                mapping[pkey]=osmid
        return mapping

    #   Override a few keys as each node is written out
    def record_action(self, osmnode, action):
        osmnode['tag']['source']         = 'osmsync:noaa:nexrad'
        if 'note' in osmnode['tag']:
            del osmnode['tag']['note']
        if 'radar_transponder' in osmnode['tag']:
            del osmnode['tag']['radar_transponder']
        return(osmsync.record_action(self, osmnode, action))

#######################################################################################
changeSetTags = {
    'source'            : 'osmsync:noaa:nexrad',
    'source:website'    : 'http://www.ncdc.noaa.gov/oa/radar/nexrad.kmz',
    'conflation_key'    : 'source:pkey',
    'note'              : 'Prepared for human review by osmsync: '+
                          'official NOAA NEXRAD weather radar stations'
    }
myfetch = osmsync_noaa_nexrad()
myfetch.run(description='Mirror NOAA NEXRAD Weather Radar Stations into OpenStreetMap',
            ext_url='http://www.ncdc.noaa.gov/oa/radar/nexrad.kmz',
            osm_url=osmsync.xapi_url + urllib.quote('node[radar_transponder=NEXRAD]')
            )
myfetch.output(changeSetTags)
