#!/usr/bin/python
##
##  Author: Bryce Nesbitt, June 2011
##  Licence: Public Domain, no rights reserved
##
##  osmsync - imports external data into OpenStreetMap,
##  then monitors the data over time.  Nodes added,
##  removed or edited in the original source are flagged
##  for a matching change in OSM.
##
##  Typical attributes mastered from the source:
##      name, website, description
##      existence of feature
##      initial location
##
##  Typical tags mastered in osm:
##      location as adjusted by osm mappers
##      all other tags
##
##  See also
##      http://api06.dev.openstreetmap.org/     - the sandbox test server
##
##  An example use case: Car Sharing locations, a published
##  by a car sharing reservation system.
##
import sys, re, urllib, urllib2
import csv, demjson
import xml.sax.saxutils
import argparse
from   pprint     import pprint
from   xml.etree  import ElementTree

class osmsync:
    """ Class to aid in creation of robust and repeatable imports into OSM.
    Supports "diffing" current osm with the external source, and producing a JOSM
    compatible file to reconcile differences.  By overloading methods you can 
    perform just about any type of merge or mirroring action.  The resulting scripts
    are meant to be run from a cron job, allowing monitoring the import
    over time. """

    http_headers    = { 'User-Agent' : 'osmsync/0.1 (OpenStreetMap merge utility; http://www.obviously.com/)' }
    xapi_url        = "http://open.mapquestapi.com/xapi/api/0.6/"
    options         = None
    output_buffer   = ""

    def __init__(self):
        return


    def fetch_source(self, sourcedata):
        assert "You must override fetch_source to get some useful external data"
        return {},[]


    def fetch_osm(self, osmdata):
        osmnodes        = {}

        if osmdata.find('://') > 0: # URL or local file?
            req     = urllib2.Request(osmdata, headers=self.http_headers)
            tree    = ElementTree.parse(urllib2.urlopen(req))
        else:
            tree    = ElementTree.parse(osmdata)

        for node in tree.iter('node'):
            osmnode = {}
            osmnode['tag'] = {}
            osmnode['id']       = node.attrib.get('id')
            osmnode['lat']      = node.attrib.get('lat')
            osmnode['lon']      = node.attrib.get('lon')
            osmnode['version']  = node.attrib.get('version')
            osmnode['user']     = node.attrib.get('user')
            for tag in node.iter('tag'):
                osmnode['tag'][tag.attrib.get('k')] = xml.sax.saxutils.escape(tag.attrib.get('v'))
            osmnodes[osmnode['id']] = osmnode

        return osmnodes

    #   Record an action (e.g. add, delete) for later output
    def record_action(self, osmnode, action):
        if( action == "none" ):
            return
        
        # attributes
        temp = "\t<node action='{}' ".format(action);
        for attribute,value in osmnode.items():
            if(attribute == 'tag'):
                continue
            temp += "{}='{}' ".format(attribute,value)
        temp += ">\n".format(action);
        self.output_buffer += temp

        # tags
        if( action != "delete" ):
            for key,value in osmnode['tag'].items():
                assert value, 'Empty value in key={}'.format(key)
                value = str(value)
                value = value.replace("'", "&apos;")
                self.output_buffer += "\t\t<tag k='{}' v='{}'/>\n".format(key,value)
        self.output_buffer += "\t</node>\n"

    # Find the osm node that matches each node in the source data
    # By defualt this uses "source:pkey"
    def map_primary_keys(self, sourcenodes, osmnodes):
        mapping = {}
        if osmnodes:
            for k,node in osmnodes.items():   
               if( 'source:pkey' in node['tag'] ):
                    mapping [node['tag']['source:pkey']]=k
        return mapping 


    ##  Perform the diff, and record any actions needed to syncronize the sources.
    ##  Warning: modifies osmnodes, removing matched nodes
    def merge_sort(self, osmnodes, sourcenodes, source2osmmap, source_is_master_for):
        count_match  = 0
        count_add    = 0
        count_del    = 0
        count_modify = 0
        id_counter   = -1

        for pkey,sourcenode in sourcenodes.items():   
            modified = False
            pkey = str(pkey)
            if( source2osmmap.has_key(pkey) ):


                osmnode = osmnodes[source2osmmap[pkey]]

                # Now we have primary key matched pairs ready for inspection
                # Here the source rules: if osm is different it gets overwritten
                for tag in source_is_master_for:
                    if( str(sourcenode['tag'].get(tag)) != str(osmnode['tag'].get(tag)) ):
                        if self.options.debug or self.options.verbose:
                            sys.stderr.write("Source {} {}='{}'\n". format(pkey,tag,sourcenode['tag'].get(tag)))
                            if osmnode['tag'].get(tag):
                                sys.stderr.write("Osm    {} {}='{}'\n". format(pkey,tag,osmnode   ['tag'].get(tag)))
                        osmnode['tag'][tag] = sourcenode['tag'].get(tag)
                        modified = True

                if modified:
                    count_modify += 1
                    self.record_action(osmnode, "modify")
                    if self.options.debug or self.options.verbose:
                        sys.stderr.write("Osm    {} {}='{}'\n". format(pkey,'userid', osmnode.get('user')))
                        sys.stderr.write("\n");
                else:
                    count_match += 1
                osmnodes.pop(source2osmmap[pkey])       # Remove any osm nodes we've delt with

            else:
                count_add += 1
                sourcenode['id'] = id_counter
                id_counter -= 1
                self.record_action(sourcenode, "add")
                if self.options.debug or self.options.verbose:
                    sys.stderr.write("Add pkey={} description={}\n".format(pkey,sourcenode['tag'].get('description')))

        for k,osmnode in osmnodes.items():   
            count_del += 1
            self.record_action(osmnode, "delete")    # Delete any leftovers
            if self.options.debug or self.options.verbose:
                sys.stderr.write("Delete osmid={} with obsolete source pkey={} name={}\n".
                                 format(osmnode['id'],
                                        osmnode['tag'].get('source:pkey'),
                                        osmnode['tag'].get('name')))

        if self.options.debug or self.options.verbose:
            sys.stderr.write("match={0} add={1} del={2} modify={3}\n".
                            format(count_match,count_add,count_del,count_modify))

        return

    #   Output our buffer.  Hard coded here to JOSM format, but should be extended
    #   to support other formats.
    #   http://wiki.openstreetmap.org/wiki/JOSM_file_format
    def output(self, changeSetTags):
        print('<?xml version="1.0" encoding="UTF-8"?>')
        print('<osm version="0.6" generator="osmsync">')
        print('<changeset>')
        for k,v in changeSetTags.items():
            print('\t<tag k="{}" v="{}"/>'.format(k,v))
        print('</changeset>')
        print(self.output_buffer + '</osm>')
        return

    #   Main executive, handles command line arguments
    #   and control flow
    def run(self, description, ext_url, osm_url):
        osmnodees       = {}
        sourcenodes     = {}
        source2osmmap   = {}

        ##  Parse arguments.  Allow, for testing purposes, forcing the data
        ##  to come from a different source or a local disk file
        ##  using a "file:///" style URL
        parser = argparse.ArgumentParser(description=description,
                        epilog="Please use local files for data during testing (file:// url)")
        parser.add_argument("--ext_url", default=ext_url,
                            help="Override external source ")   # + ext_url)    # creates runtime error
        parser.add_argument("--osm_url", default=osm_url,
                            help="Override osm query ")         # + osm_url)    # creates runtime error
       #parser.add_argument("--format", default=xxxxx,
       #                    help="Set output format [JOSM|osmChange|API]")
        parser.add_argument("--debug",    action='store_true')
        parser.add_argument("--quiet",  action='store_true')
        self.options = parser.parse_args()

        self.options.verbose = not self.options.quiet

        ##  Fetch data from both sources, then merge them
        ##  Print out results at each stage for debugging purposes, depsite
        ##  the code clutter
        sourcenodes, source_is_master_for = self.fetch_source(self.options.ext_url)
        if self.options.debug or self.options.verbose:
            sys.stderr.write("{} source nodes loaded from ".format(len(sourcenodes)))
            sys.stderr.write(self.options.ext_url+"\n")
            sys.stderr.write("source is master for tags: " + ",".join(source_is_master_for) + "\n")
        if self.options.debug:
            print("\nLoaded source data from: " + self.options.ext_url)
            pprint(sourcenodes)

        osmnodes = self.fetch_osm(self.options.osm_url)
        if self.options.debug or self.options.verbose:
            sys.stderr.write("{} osm nodes loaded from ".format(len(osmnodes)))
            sys.stderr.write(self.options.osm_url+"\n")
        if self.options.debug:
            print("\nLoaded osm data from: " + self.options.osm_url)
            pprint(osmnodes)

        source2osmmap = self.map_primary_keys(sourcenodes,osmnodes)
        if self.options.debug:
            print("\nPrimary key mapping:")
            pprint(source2osmmap)

        self.merge_sort(osmnodes, sourcenodes, source2osmmap, source_is_master_for)
        if self.options.debug:
            print("\nLeftover osm nodes after merge:")
            pprint(osmnodes)

        return
