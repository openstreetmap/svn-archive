#!/usr/bin/python
import re, sys, time
from osmparser import osmparser     # We eventually want to mod this to stream out the XML to file, bypassing ET.
import xml.etree.cElementTree as ET

class ParseXML:
    namespace = '{http://www.naptan.org.uk/}'
    defaulttags = {'source': 'naptan_import', 'created_by': 'naptan2osm', 'naptan:unverified': 'yes'}
    tagprefix = ''
    tagmap = {}         # The basic tag mappings that are a simple one to one mapping
    
    # Tuple containing XML (parent) nodes that are required to determine a child node's meaning
    watchnodes = ()
    parentnodes = {}    # Dict noting parent nodes that may need to be checked to determine child's meaning
    
    nodecounter = 0  # OSM feature id incrementor
    relationcounter = 0
    feature = None      # The OSM feature we're building
    outfile = None
    
    def __init__(self, of):
        self.parentnodes = dict.fromkeys(self.watchnodes, False)
        self.outfile = of
        
    def startElement(self, elem):
        node = self.cleannode(elem)
        # Keep our watched parent nodes list updated
        if node in self.watchnodes:
            self.parentnodes[node] = True
            
    def endElement(self, elem):
        node = self.cleannode(elem)
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
            
        value = elem.text.strip(' -')
        if not value:
            return None
        
        if not isinstance(keys, tuple):
            keys = (keys,)
        for key in keys:
            if not key:
                key = '%s:%s%s' % (self.tagprefix, midfix or '', node)
            if key in self.feature.tags:
                self.feature.tags[key] = '%s;%s' % (self.feature.tags[key], value)
            else:
                self.feature.tags[key] = value
            
    def newfeature(self):
        self.nodecounter += 1
        feature = osmparser.OSMObj(type='node', id=-self.nodecounter)
        feature.tags = self.defaulttags.copy()
        return feature
        
    def newrelation(self):
        self.relationcounter += 1
        feature = osmparser.OSMObj(type='relation', id=-self.relationcounter)
        feature.tags = self.defaulttags.copy()
        return feature
        
    def cancelfeature(self):
        self.feature = None



class ParseNaptan(ParseXML):
    tagprefix = 'naptan'
    tagmap = {
        'AtcoCode': '',             # Blank entries automatically form tags of tagprefix:nodename
        'NaptanCode': ('', 'ref'),   # Tuples can be used if a node needs to be mapped to multiple tags
        'CommonName': ('', 'name'),
        'ShortCommonName': '',
        'Landmark': '',
        'Street': '',
        'Crossing': '',
        'Indicator': '',            # For when the ref-parsing code below fails
        'Notes': '',
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
    defaultareatags = {'type': 'site', 'site': 'stop_area'}
    watchnodes = ('StopPoints', 'StopArea', 'AlternativeDescriptors', 'StopClassification')
    
    # dict of form {stoparearef: [osmstoppointid, osmstoparearaid, ...]}
    stopareamap = {}
    # Some ParentStopAreas are in other datasets (eg national), make a note so we can link back.
    # TODO: mechanism for pulling in PSAs that are already imported.
    missingparentarea = []
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
                self.feature = self.newrelation()
                self.feature.tags.update(self.defaultareatags)
            else:
                elem.clear()
    
    def endElement(self, elem):
        node = self.cleannode(elem)
        # If there's no feature, we're probably ignoring this element, or the tree its in.
        if self.feature:
            
            if node == 'StopPoint':
                # That's all for this point, wrap up the feature's xml ready for the next one
                self.outfile.write(self.feature.toxml(needs_parent=False, indent=False))
                self.outfile.write("\n")
                self.feature = None
                self.pointcount += 1
            
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
                        re.compile('^(?:(?:BAY)|(?:ENTRANCE)|(?:STAND)|(?:STOP)|(?:STANCE) )([A-Z0-9\-& ]{1,7})'),
                        # Unfortunately, still matches 2 letter words (such as 'on') used as Indicators in Surrey
                        re.compile('^([A-Z0-9]{1,2}[0-9]?)$')
                    )
                    for regex in regexes:
                        matches = regex.match(indicator)
                        if matches:
                            v = matches.group(1).strip(' -')
                            if v:
                                self.feature.tags['local_ref'] = v
                                break
                # Also pull into naptan:Indicator
                self.node2tag(elem)
                
            #elif self.parentnodes['AlternativeDescriptors']:
                #self.node2tag(elem, self.tagmap_altdescriptors, 'Alt')
            elif node == 'StopType':
                # This is a 'legacy' node according to the schema, but it should still work
                st = elem.text
                if st == 'BCT' or st == 'BCQ' or st == 'BCS':
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
            
            elif node == 'StopAreaRef':
                # Store this info to add the feature to the relation when we're parsing the StopArea
                if not elem.text in self.stopareamap:
                    # This StopArea has not yet been referenced
                    self.stopareamap[elem.text] = [self.feature,]
                else:
                    # This StopArea has already been referenced
                    self.stopareamap[elem.text].append(self.feature)
            
            ###
            # StopArea stuff
            ###
            elif node == 'StopArea':
                # Don't bother with StopAreas with no StopPoints present
                #if not self.feature.members:
                    #self.feature = None
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
                if elem.text in self.features:
                    # The parent StopArea's feature has already been created
                    self.features[elem.text].members.append(self.feature)
                elif not elem.text in self.stopareamap:
                    # The parent StopArea has not yet been referenced
                    self.stopareamap[elem.text] = [self.feature,]
                else:
                    # The parent StopArea has been referenced but not created
                    self.stopareamap[elem.text].append(self.feature)
                
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
        for feature in self.features.values():
            self.outfile.write(feature.toxml(needs_parent=False, indent=False))
            self.outfile.write("\n")
        self.features = None
        
        print "Parsed %s StopPoints and %s StopAreas" % (self.nodecounter, self.relationcounter)
        print "Output %s StopPoints and %s StopAreas" % (self.pointcount, areacount)
        print self.stopareamap


class ParseNPTG(ParseXML):
    
    def startElement(self, elem):
        pass
    
    def endElement(self, elem):
        pass

def treeparse(f, of=sys.stdout):
    of.write('<osm version="0.5">')

    it = iter(ET.iterparse(f, events=('start', 'end')))
    event, root = it.next()
    
    if root.tag == ParseNaptan.namespace + 'NaPTAN':
        parser = ParseNaptan(of=of)
    elif root.tag == ParseNPTG.namespace + 'NationalPublicTransportGazetteer':
        parser = ParseNPTG(of=of)
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

    of.write('</osm>')
    
if __name__ == "__main__":
    if not len(sys.argv) == 2:
        raise Exception, "Input file required"
    
    filename = sys.argv[1]
    infile = open(filename)
    osmfile = '%s.osm' % (filename.rpartition('.')[0])
    outfile = open(osmfile, 'w')
    
    xml = treeparse(infile, outfile)
    
    outfile.close()
