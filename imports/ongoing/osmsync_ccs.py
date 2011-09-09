#!/usr/bin/python
##
##  Author: Bryce Nesbitt, June 2011
##  Licence: Public Domain, no rights reserved
##
##  osmsync module to mirror City CarShare "pods" into osm 
##
##  See also:
##      http://wiki.openstreetmap.org/wiki/Tag:amenity%3Dcar_sharing
##
##  Future work:
##      Support capacity=* tag
##      Warn human about location discrepencies over 100 meters
##
import sys
sys.path = ['../../applications/utils/import/osmsync/'] + sys.path
from osmsync import osmsync

import sys, re, urllib, urllib2
import csv, demjson
import xml.sax.saxutils

from   pprint     import pprint
from   xml.etree  import ElementTree

class osmsync_ccs(osmsync):
    """ Mirror merge City CarShare reservation system data into OSM """

    #   Load all car sharing locations from CityCarShare
    #   This is done via a scrape from the map.  An xml dump
    #   previously existed, but was discontinued due to an
    #   unrelated change of backend systems.
    #
    #   The maps html file has handy almost-json format records as so:
    #    var pod_1 = {
    #        id     :   1,
    #        lat    :   37.783055,
    #        lon    :   -122.405573,
    #        name   :   "San Francisco/SOMA: 5th &amp; Mission",
    #        addr   :   "833 Mission St",
    #        url    :   "http://www.citycarshare.org/pod_1.do",
    #        vtype  :   ",9,,9,,17,",
    #        vstring:   "Scion xB,Scion xB,Honda Fit",
    #        gmark  :   null
    #        }
    #   I apologize it is my own fault that's almost-json not json.  Sorry.
    #
    def fetch_source(self, sourcedata):
        sourcenodes = {}

        req = urllib2.Request(sourcedata, headers=self.http_headers);
        body = urllib2.urlopen(req).read()

        pods_re = re.compile("var pod_\d* = ({.*?})", re.DOTALL)
        for m in pods_re.finditer(body):
            ccs_pod = demjson.decode(m.group(1))
            pkey    = ccs_pod['id']
            commas  = ccs_pod['vstring'].strip().count(',')

            osmnode = {}
            osmnode['tag']                  = {}
            osmnode['lat']                  = ccs_pod['lat']
            osmnode['lon']                  = ccs_pod['lon']
            osmnode['tag']['amenity']       = 'car_sharing'
            osmnode['tag']['operator']      = 'City CarShare'
            osmnode['tag']['capacity']      = str(commas + 1)
            osmnode['tag']['name']          = ccs_pod['name'].strip()
            osmnode['tag']['website']       = ccs_pod['url'].strip()
            osmnode['tag']['description']   = ccs_pod['addr'] .strip()+ " with " + ccs_pod['vstring'].strip()
            osmnode['tag']['source']        = 'osmsync:ccs'
            osmnode['tag']['source:pkey']   = pkey
            sourcenodes[pkey] = osmnode

        source_is_master_for=['name','website','description','amenity','operator','source','capacity']
        return(sourcenodes, source_is_master_for)


    #   Override a few keys
    def record_action(self, osmnode, action):
        if 'source:website' in osmnode['tag']:
            del osmnode['tag']['source:website']
        return(osmsync.record_action(self, osmnode, action))


#############################################################################################
#############################################################################################
changeSetTags = {
    'source'            : 'osmsync:ccs',
    'source:website'    : 'http://www.citycarshare.org/',
    'conflation_key'    : 'source:pkey',
    'note'              : 'Prepared for human review by osmsync: '+
                          'reads the car share reservation system and suggests matching updates for osm.'
    }
myfetch = osmsync_ccs()
myfetch.run(description='City CarShare pod location mirror script',
            ext_url='http://www.citycarshare.org/sfmap.do',
            osm_url=osmsync.xapi_url + urllib.quote("node[source=osmsync:ccs]"),
            )
myfetch.output(changeSetTags)
