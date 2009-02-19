#!/usr/bin/python
import re, sys, time
from osmparser import osmparser     # We eventually want to mod this to stream out the XML to file, bypassing ET.
import xml.etree.cElementTree as ET

class ParseXML:
    namespace = '{http://www.naptan.org.uk/}'
    tagprefix = ''
    tagmap = {}         # The basic tag mappings that are a simple one to one mapping
    
    # Tuple containing XML (parent) nodes that are required to determine a child node's meaning
    watchnodes = ()
    parentnodes = {}    # Dict noting parent nodes that may need to be checked to determine child's meaning
    
    featurecounter = 0  # OSM feature id incrementor
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
    
    def cleannode(self, elem):
        """Cleans namespace from element node
        eg: elem.tag = '{http://www.naptan.org.uk/}AtcoCode'
            is transformed to 'AtcoCode'
        """
        return elem.tag.replace(self.namespace, '')
        
    def node2tag(self, elem, tagmap=None):
        """Lookup (and set) an OSM tag mapping in tagmap for a given node name"""
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
                    key = '%s:%s' % (self.tagprefix, node)
                if key in self.feature.tags:
                    self.feature.tags[key] = '%s;%s' % (self.feature.tags[key], value)
                else:
                    self.feature.tags[key] = value
            
    def newfeature(self):
        self.featurecounter += 1
        return osmparser.OSMObj(type='node', id='-%d' % self.featurecounter)




class ParseNaptan(ParseXML):
    tagprefix = 'naptan'
    tagmap = {
        'AtcoCode': '',             # Blank entries automatically form tags of tagprefix:nodename
        'NaptanCode': ('','ref'),   # Tuples can be used if a node needs to be mapped to multiple tags
        'CommonName': 'name',
        'ShortCommonName': '',
        'Landmark': '',
        'Street': '',
        'Crossing': '',
        'Indicator': '',            # For when the ref-parsing code below fails
        'Notes': '',
    }
    tagmap_altdescriptors = {
        
    }
    watchnodes = ('StopPoints', 'StopAreas', 'AlternativeDescriptors')
        
    def startElement(self, elem):
        """Event handler for when the XML parser encounters an opening tag.
        Should be used for maintaining state, data retrieval should be done on
        encountering an endElement"""

        # Updates the parent nodes list
        ParseXML.startElement(self, elem)

        node = self.cleannode(elem)
        if node == 'StopPoint':
            self.feature = self.newfeature()
    
    def endElement(self, elem):
        node = self.cleannode(elem)
        
        if node == 'StopPoint':
            # That's all for this point, wrap up the feature's xml ready for the next one
            self.outfile.write(self.feature.toxml(needs_parent=False, indent=False))
            self.feature = None
        
        elif node == 'Latitude' and self.parentnodes['StopPoints']:
            self.feature.loc[1] = elem.text
        elif node == 'Longitude' and self.parentnodes['StopPoints']:
            self.feature.loc[0] = elem.text
        elif node == 'Indicator' and not self.parentnodes['AlternativeDescriptors']:
            v = ''
            if not elem.text in ('In', 'No', 'Nr', 'On'):
                indicator = elem.text.upper()
                regexes = (
                    # Mostly produced with reference to London and Surrey data
                    re.compile('^(?:(?:BAY)|(?:ENTRANCE)|(?:STAND)|(?:STOP) )([A-Z0-9\-& ]{1,7})'),
                    # Unfortunately, still matches 2 letter words (such as 'on') used as Indicators in Surrey
                    re.compile('^([A-Z0-9]{1,2}[0-9]?)$')
                )
                for regex in regexes:
                    matches = regex.match(indicator)
                    if matches:
                        v = matches.group(1).strip(' -')
                        if v:
                            self.feature.tags['ref'] = v
                            break
            if not v:
                self.node2tag(elem)
            
        elif self.parentnodes['AlternativeDescriptors']:
            self.node2tag(elem, self.tagmap_altdescriptors)
        elif node == 'StopType':
            # This is a 'legacy' node according to the schema, but it should still work
            stoptype = elem.text
            if stoptype == 'BCT' or stoptype == 'BCQ' or stoptype == 'BCS':
                self.feature.tags['highway'] = 'bus_stop'
            elif stoptype == 'BST':       # check exactly what NaPTAN means by "busCoachStationAccessArea"
                self.feature.tag['amenity'] = 'bus_station'
        else:
            # Fallthrough for simple tag mappings
            self.node2tag(elem)
        
        # Keep our parent nodes dict updated.
        ParseXML.endElement(self, elem)






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

    of.write('</osm>')
    
if __name__ == "__main__":
    if not len(sys.argv) == 2:
        raise Exception, "Input file required"
    
    filename = sys.argv[1]
    infile = open(filename)
    osmfile = '%s.osm' % (filename.rpartition('.')[0])
    outfile = open(osmfile, 'w')
    
    time1 = time.time()
    xml = treeparse(infile, outfile)
    time2 = time.time()
    print >>sys.stderr, "Time: %f" % (time2 - time1)
    
    outfile.close()
