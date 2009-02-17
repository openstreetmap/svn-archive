#!/usr/bin/python
import sys, time
from osmparser import osmparser     # We eventually want to mod this to stream out the XML to file, bypassing ET.
import xml.etree.cElementTree as ET

class ParseXML:
    namespace = '{http://www.naptan.org.uk/}'
    tagmap = {}
    featurecounter = 0
    feature = None      # The OSM feature we're building
    document = ET.Element("osm", {"version": "0.5"})    # OSM document we're creating
    
    def cleannode(self, elem):
        """Cleans namespace from element node
        eg: elem.tag = '{http://www.naptan.org.uk/}AtcoCode'
            is transformed to 'AtcoCode'
        """
        return elem.tag.replace(self.namespace, '')
        
    def node2tag(self, elem):
        """Lookup an OSM tag mapping in tagmap for a given node name"""
        try:
            key = self.tagmap[self.cleannode(elem)]
            value = elem.text
            return (key, value)
        except:
            return None
            
    def newfeature(self):
        self.featurecounter += 1
        return osmparser.OSMObj(type='node', id='-%d' % self.featurecounter)
            
    def getosmxml(self):
        return ET.tostring(self.document)

class ParseNaptan(ParseXML):
    tagmap = {
        'AtcoCode': 'naptan:AtcoCode'
    }
    STOPPOINTS = 1
    STOPAREAS = 2
    
    def __init__(self):
        self.group = 0
        
    def startElement(self, elem):
        """Event handler for when the XML parser encounters an opening tag.
        Should be used for maintaining state, data retrieval should be done on
        encountering an endElement"""
        node = self.cleannode(elem)
        if node == 'StopPoints':
            self.group = self.STOPPOINTS
        # The StopAreas node also appears in a StopPoint as well as a grouping node.
        elif node == 'StopAreas' and self.group != self.STOPPOINTS:
            self.group = self.STOPAREAS
        elif node == 'StopPoint':
            self.feature = self.newfeature()
    
    def endElement(self, elem):
        node = self.cleannode(elem)
        if node == 'StopPoints':
            self.group = 0
        elif node == 'StopAreas' and self.group != self.STOPPOINTS:
            self.group = 0
        elif node == 'StopPoint':
            self.feature.toxml(parent=self.document, as_string=False, indent=False)
            self.feature = None
        elif node == 'Latitude' and self.group == self.STOPPOINTS:
            self.feature.loc[1] = elem.text
        elif node == 'Longitude' and self.group == self.STOPPOINTS:
            self.feature.loc[0] = elem.text
        else:
            tag = self.node2tag(elem)
            if tag:
                k, v = tag
                self.feature.tags[k] = v
    
class ParseNPTG(ParseXML):
    def __init__(self):
        pass
    
    def startElement(self, elem):
        pass
    
    def endElement(self, elem):
        pass

def treeparse(f):
    it = iter(ET.iterparse(f, events=('start', 'end')))
    event, root = it.next()
    
    if root.tag == ParseNaptan.namespace + 'NaPTAN':
        parser = ParseNaptan()
    elif root.tag == ParseNPTG.namespace + 'NationalPublicTransportGazetteer':
        parser = ParseNPTG()
    else:
        raise Exception, "Unknown XML type"
    
    count = 0
    for event, elem in it:
        if event == 'start':
            parser.startElement(elem)
        elif event == 'end':
            parser.endElement(elem)
            elem.clear()
            
    return parser.getosmxml()
    
if __name__ == "__main__":
    if not len(sys.argv) == 2:
        raise Exception, "Input file required"
    filename = sys.argv[1]
    time1 = time.time()
    f = open(sys.argv[1])
    xml = treeparse(f)
    time2 = time.time()
    print "Time: %f" % (time2 - time1)
    
    osmfile = '%s.osm' % (filename.rpartition('.')[0])
    
    outfile = open(osmfile, 'w')
    outfile.write(xml)
    outfile.close()
