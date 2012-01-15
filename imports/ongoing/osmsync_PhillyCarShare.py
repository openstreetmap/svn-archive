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



class osmsync_metavera(osmsync):
    """ Mirror merge Metavera car share reservation system data into OSM """

    # 
    #   [{"id":"1","descr":"33 W 56th Street",
    #   "latitude":"40.763199","longitude":"-73.976380","vehicle_types":"4,15,"},
    #
    def fetch_source(self, sourcedata):
        sourcenodes = {}

        req = urllib2.Request(sourcedata, headers=self.http_headers);
        body = urllib2.urlopen(req).read()

        pods = demjson.decode(body)

        for pod in pods:
            pkey    = pod['id']
            commas  = pod['vehicle_types'].strip().count(',')


            osmnode = {}
            osmnode['tag']                  = {}
            osmnode['lat']                  = pod['latitude']
            osmnode['lon']                  = pod['longitude']
            osmnode['tag']['amenity']       = 'car_sharing'
            osmnode['tag']['operator']      = 'PhillyCarShare'
            osmnode['tag']['capacity']      = str(commas - 1)
            osmnode['tag']['name']          = xml.sax.saxutils.escape(pod['descr'].strip())
           #osmnode['tag']['website']       = pod['url'].strip()
           #osmnode['tag']['description']   = pod['addr'] .strip()+ " with " + ccs_pod['vstring'].strip()
            osmnode['tag']['source']        = 'osmsync:PhillyCarShare'
            osmnode['tag']['source:pkey']   = 'pcs:' + pkey

            if float(osmnode['lat']) <  39 or float(osmnode['lat']) >  41 or \
               float(osmnode['lon']) > -74 or float(osmnode['lon']) < -76:
                if self.options.debug or self.options.verbose:
                        sys.stderr.write('Skipping out of area pod #' + pkey + '\n')
                        #pprint(osmnode)
                        continue

            sourcenodes[pkey] = osmnode

        source_is_master_for=['name','website','description','amenity','operator','source','capacity']
        return(sourcenodes, source_is_master_for)


    #   Override a few keys
    def record_action(self, osmnode, action):
        return(osmsync.record_action(self, osmnode, action))


#############################################################################################
#############################################################################################
changeSetTags = {
    'source'            : 'osmsync:PhillyCarShare',
    'source:website'    : 'https://reservations.phillycarshare.org/',
    'conflation_key'    : 'source:pkey',
    'note'              : 'Prepared for human review by osmsync: '+
                          'reads the car share reservation system and suggests matching updates for osm.'
    }
myfetch = osmsync_metavera()
myfetch.run(description='Metavera car sharing location mirror script',
            ext_url='https://reservations.phillycarshare.org/maps/api/lots.php',
            osm_url=osmsync.xapi_url + urllib.quote("node[source=osmsync:PhillyCarShare]"),
            )
myfetch.output(changeSetTags)
