#!/usr/bin/python

"""
This script is designed to convert and NaPTAN XMLv2 data into a suitable OSM form.
"""

__author__ = "Thomas Wood (Edgemaster) <grand.edgemaster@gmail.com>"
__version__ = "$Id$"

import re, sys, time
from osmparser import osmparser     # We eventually want to mod this to stream out the XML to file, bypassing ET.
try:
    import xml.etree.cElementTree as ET
except ImportError:
    import cElementTree as ET
try:
    import cPickle as pickle
except ImportError:
    import pickle

class Feature(osmparser.OSMObj):
    filtered = False

class ParseXML:
    namespace = '{http://www.naptan.org.uk/}'
    defaulttags = {'naptan:verified': 'no', 'source': 'naptan_import'}
    tagprefix = ''
    tagmap = {}         # The basic tag mappings that are a simple one to one mapping
    
    # Tuple containing XML (parent) nodes that are required to determine a child node's meaning
    watchnodes = ()
    parentnodes = {}    # Dict noting parent nodes that may need to be checked to determine child's meaning
    
    nodecounter = 0     # OSM feature id incrementor
    relationcounter = 0
    feature = None      # The OSM feature we're building
    output = None

    filter = []       # Filtering for a particular element in the naptan
    passive = False
    
    def __init__(self, output, options):
        self.parentnodes = dict.fromkeys(self.watchnodes, False)
        self.output = output
        self.options = options
        if options.filter:
            self.filter = filt.upper().split(':')
        
    def startElement(self, elem):
        node = self.cleannode(elem)
        # Keep our watched parent nodes list updated
        if node in self.watchnodes:
            self.parentnodes[node] = True
            
    def endElement(self, elem):
        node = self.cleannode(elem)
        try:
            if node.upper() == self.filter[0]:
                if elem.text.upper() == self.filter[1]:
                    if self.feature:
                        self.feature.filtered = True
        except IndexError:
            pass
        if node in self.watchnodes:
            self.parentnodes[node] = False
            
    def finish(self):
        pass
    
    def cleannode(self, elem):
        """Cleans namespace from element node
        eg: elem.tag = '{http://www.naptan.org.uk/}AtcoCode'
            is transformed to 'AtcoCode'
        """
        return elem.tag.replace(self.namespace, '')
        
    def node2tag(self, elem, tagmap=None, midfix=None):
        """Lookup (and set) an OSM tag mapping in tagmap for a given node name.
        
        elem: elem to map
        tagmap: dict mapping elem.type to osm tag(s)
        midfix: suffix to the tagprefix, prefix to the element type if copied across
        """
        if not tagmap:
            tagmap = self.tagmap
        node = self.cleannode(elem)
        
        try:
            keys = tagmap[node]
        except KeyError:
            return None
            
        value = elem.text.strip(' -*')
        if not value:
            return None
        
        if not isinstance(keys, tuple):
            keys = (keys,)
        for key in keys:
            if not key:
                key = '%s:%s%s' % (self.tagprefix, midfix or '', node)
            if key in self.feature.tags:
                value = '%s;%s' % (self.feature.tags[key], value)

            if len(value) > 255:
                print "Tag value over 255 chars in %s for:\n%s=%s\n" % (self.feature, key, value)
                value = value[0:254]

            self.feature.tags[key] = value

            # Only do the first tag (most often empty, so copies naptan:value)
            if self.options.passive:
                break

    def addtomap(self, text, feature, map, features=[]):
        """Add feature to given pre-relation mappings.

        text {String}:  Key to search by
        feature:        Object to add (Point or Area)
        map:            Mapping sorted by parent StopAreaCode of features to their parent StopArea
        features:       Dict of created StopAreas to check against first
        """
        if text in features:
            # The parent StopArea's feature has already been created
            features[text].members.append(feature)
        elif not text in map:
            # The parent StopArea has not yet been referenced
            map[text] = [feature,]
        else:
            # The parent StopArea has been referenced but not created
            map[text].append(feature)
            
    def newfeature(self):
        self.nodecounter += 1
        feature = Feature(type='node', id=-self.nodecounter)
        feature.tags = self.defaulttags.copy()
        return feature
        
    def newrelation(self, tags={}):
        self.relationcounter += 1
        feature = Feature(type='relation', id=-self.relationcounter)
        feature.tags = self.defaulttags.copy()
        feature.tags.update(tags)
        feature.filtered = True         # No filtering yet on relations
        return feature
        
    def cancelfeature(self):
        self.feature = None



class ParseNaptan(ParseXML):
    tagprefix = 'naptan'
    tagmap = {
        'AtcoCode': '',             # Blank entries automatically form tags of tagprefix:nodename
        'NaptanCode': '',           # Tuples can be used if a node needs to be mapped to multiple tags
        'CommonName': ('', 'name'), # Only the first will be used in passive mode.
        'ShortCommonName': '',
        'Landmark': '',
        'Street': '',
        'Crossing': '',
        'Indicator': '',            # For when the ref-parsing code below fails
        'Notes': '',
        'CompassPoint': 'naptan:Bearing',   # Slightly hacky
        'PlusbusZoneRef': '',
        # StopArea tagging begins here
        'StopAreaCode': '',
        'StopAreaType': ''
    }
    tagmap_altdescriptors = {
        # When copied across, will have Alt prepended to name (after naptan:)
        'CommonName': ('', 'alt_name'),
        'ShortCommonName': '',
        'Landmark': '',
        'Street': '',
        'Crossing': '',
        'Indicator': ''
    }
    defaultareatags = {'public_transport': 'stop_place'}
    watchnodes = ('StopPoints', 'StopArea', 'AlternativeDescriptors', 'StopClassification')
    
    # dict of form {stoparearef: [osmstoppointid, osmstoparearaid, ...]}
    stopareamap = {}
    # Used when storing StopAreas which could require further modification
    features = {}
    
    pointcount = 0
        
    def startElement(self, elem):
        """Event handler for when the XML parser encounters an opening tag.
        Should be used for maintaining state, data retrieval should be done on
        encountering an endElement"""

        # Updates the parent nodes list
        ParseXML.startElement(self, elem)

        node = self.cleannode(elem)
        if node == 'StopPoint':
            if elem.attrib['Status'] == 'active':
                self.feature = self.newfeature()
            else:
                elem.clear()
        elif node == 'StopArea':
            if elem.attrib['Status'] == 'active':
                self.feature = self.newrelation(self.defaultareatags)
            else:
                elem.clear()
    
    def endElement(self, elem):
        node = self.cleannode(elem)
        # If there's no feature, we're probably ignoring this element, or the tree its in.
        if self.feature:
            ###
            # StopPoint OUTPUT
            ###
            if node == 'StopPoint':
                if (not self.filter) or (self.filter and self.feature.filtered):
                    # That's all for this point, wrap up the feature's xml ready for the next one
                    self.output.write(self.feature)
                    self.pointcount += 1
                self.feature = None
            
            elif node == 'Latitude' and self.parentnodes['StopPoints'] and not self.parentnodes['StopClassification']:
                self.feature.loc[1] = elem.text
            elif node == 'Longitude' and self.parentnodes['StopPoints'] and not self.parentnodes['StopClassification']:
                self.feature.loc[0] = elem.text
            elif node == 'Indicator' and not self.parentnodes['AlternativeDescriptors']:
                v = ''
                if not elem.text in ('In', 'No', 'Nr', 'On'):
                    indicator = elem.text.upper()
                    regexes = (
                        # Mostly produced with reference to London and Surrey data
                        re.compile('^(?:(?:BAY)|(?:ENTRANCE)|(?:STAND)|(?:STOP)|(?:STANCE) )([A-Z0-9\-& ]+)'),
                        # Unfortunately, still matches 2 letter words (such as 'on') used as Indicators in Surrey
                        re.compile('^([A-Z0-9]{1,2}[0-9]?)$')
                    )
                    for regex in regexes:
                        matches = regex.match(indicator)
                        if matches:
                            v = matches.group(1).strip(' -*')
                            if v:
                                self.feature.tags['local_ref'] = v
                                break
                # Also pull into naptan:Indicator
                self.node2tag(elem)
                
            elif self.parentnodes['AlternativeDescriptors']:
                self.node2tag(elem, self.tagmap_altdescriptors, 'Alt')
            elif node == 'StopType':
                # This is a 'legacy' node according to the schema, but it should still work
                st = elem.text
                if st == 'BCT' or st == 'BCQ' or st == 'BCS':
                    if not self.options.passive:
                        self.feature.tags['highway'] = 'bus_stop'
                #elif st == 'BST':       # check exactly what NaPTAN means by "busCoachStationAccessArea"
                    #self.feature.tag['amenity'] = 'bus_station'
                elif st == 'TXR' or st == 'STR':
                    self.feature.tags['amenity'] = 'taxi'
                else:
                    # We don't want any other types of points imported at all yet
                    self.cancelfeature()
            elif node == 'BusStopType':
                bst = elem.text
                # We don't yet support Hail and Ride or 'Flexible' stop point areas.
                if bst == 'HAR' or bst == 'FLX':
                    self.cancelfeature()
                elif bst == 'CUS':
                    self.feature.tags['naptan:BusStopType'] = 'CUS'
                    if self.feature.tags['highway']:
                      del self.feature.tags['highway']
            
            elif node == 'StopAreaRef':
                self.addtomap(elem.text, self.feature, self.stopareamap)

            ###
            # StopArea stuff
            ###
            elif node == 'StopArea':
                if self.feature:
                    atcocode = self.feature.tags['naptan:StopAreaCode']
                    self.features[atcocode] = self.feature
                    self.feature = None
                    
            elif node == 'StopAreaCode':
                if elem.text in self.stopareamap:
                    # Add the Points we stored earlier to the newly created relation
                    self.feature.members.extend(self.stopareamap[elem.text])
                    # Delete from the dict, so we can store completely unreferenced objects
                    del self.stopareamap[elem.text]
                self.node2tag(elem)
                
            elif node == 'ParentStopAreaRef':
                self.addtomap(elem.text, self.feature, self.stopareamap, self.features)
                
            elif node == 'Name' and self.parentnodes['StopArea']:
                self.feature.tags['name'] = elem.text
            
            else:
                # Fallthrough for ALL simple tag mappings
                self.node2tag(elem)

        # (endif self.feature)
        
        # Keep our parent nodes dict updated.
        ParseXML.endElement(self, elem)
        
    def finish(self):
        # Write out any other features
        areacount = len(self.features)
        
        for idx in self.features.copy():
            # Ignore relations with no members
            #if len(self.features[idx].members) > 1:
                self.output.write(self.features[idx])
                del self.features[idx]
            #else:
            #    areacount -= 1

        pickles = open("%s.emptyarea.pkl" % (self.options.filenamebase), 'wb')
        pickle.dump(self.features, pickles)
        pickles.close()
        
        pickles = open("%s.noparentarea.pkl" % (self.options.filenamebase), 'wb')
        pickle.dump(self.stopareamap, pickles)
        pickles.close()
        
        print "Parsed %d StopPoints and %d StopAreas" % (self.nodecounter, self.relationcounter)
        print "Output %d StopPoints and %d StopAreas" % (self.pointcount, areacount)
        print "Pickled %d missing parental StopArea references and %d empty (or 1 member) StopAreas" % (len(self.stopareamap), len(self.features))


class ParseNPTG(ParseXML):
    
    def startElement(self, elem):
        pass
    
    def endElement(self, elem):
        pass

class OSMWriter:
    def __init__(self, outfile):
        self.of = outfile
        self.of.write('<osm version="0.6">')

    def write(self, osmobj):
        if self.of:
            self.of.write(osmobj.toxml(needs_parent=False, indent=False))
            self.of.write("\n")
        else:
            raise Exception, "Outfile already closed"

    def close(self):
        self.of.write('</osm>')
        self.of.close()
        self.of = None


class DebugWriter:
    def __init__(self, outfile):
        self.of = outfile
        self.objs = []
    
    def write(self, osmobj):
        self.objs.append(osmobj)

    def close(self):
        pickle.dump(self.objs, self.of)
        self.of.close()
        self.of = None

def treeparse(f, output, options):
    it = iter(ET.iterparse(f, events=('start', 'end')))
    event, root = it.next()

    if root.tag == ParseNaptan.namespace + 'NaPTAN':
        parser = ParseNaptan(output, options)
    elif root.tag == ParseNPTG.namespace + 'NationalPublicTransportGazetteer':
        parser = ParseNPTG(output, options)
    else:
        raise Exception, "Unknown XML type"
    
    count = 0
    for event, elem in it:
        if event == 'start':
            parser.startElement(elem)
        elif event == 'end':
            parser.endElement(elem)
            elem.clear()
            
    parser.finish()
    
if __name__ == "__main__":
    from optparse import OptionParser
    
    parser = OptionParser(usage="%prog [args] naptan.xml", version=__version__)
    parser.add_option("-f", "--filter", action="store", dest="filter",
    type="string", default=None, help="Only import Points matching FILTER. FILTER is in the form element:value")
    parser.add_option("-o", "--outdir", action="store", dest="outdir",
    type="string", default=".", help="Write OSM data into dir, defaults to .")
    parser.add_option("-p", "--passive", action="store_true", dest="passive",
    help="'Passive' conversion, don't tag the data visibly, require it to be reviewed before being useful.")
    parser.add_option("-d", "--debug-output", action="store_true", dest="debug",
    help="Produce pickled output for debug purposes rather than a .osm")
    (options, args) = parser.parse_args()
    if len(args) != 1:
        parser.error("Input file required")
    
    filename = args[0]
    infile = open(filename)

    options.filenamebase = "%s/%s" % (options.outdir, filename.rpartition('.')[0])
    
    options.outfile = '%s.osm' % (options.filenamebase)
    outfile = open(options.outfile, 'w')
    
    output = OSMWriter(outfile) if not options.debug else DebugWriter(outfile)
    xml = treeparse(infile, output, options)
    output.close()


