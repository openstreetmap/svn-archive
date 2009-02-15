#!/usr/bin/python
import sys
import xml.etree.cElementTree as ET

class ParseXML:
    namespace = '{http://www.naptan.org.uk/}'
    tagmap = {}
    def cleantag(self, elem):
        """Cleans namespace from element tag
        eg: elem.tag = '{http://www.naptan.org.uk/}AtcoCode'
            is transformed to 'AtcoCode'
        """
        return elem.tag.replace(self.namespace, '')
        
    def node2tag(self, elem):
        """Lookup an OSM tag mapping in tagmap for a given node name"""
        try:
            key = self.tagmap[self.cleantag(elem)]
            value = elem.text
            return (key, value)
        except:
            pass

class ParseNaptan(ParseXML):
    tagmap = {'AtcoCode': 'naptan:AtcoCode'}
    STOPPOINTS = 1
    STOPAREAS = 2
    def __init__(self):
        self.group = 0
        
    def startElement(self, elem):
        """Event handler for when the XML parser encounters an opening tag.
        Should be used for maintaining state, data retrieval should be done on
        encountering an endElement"""
        tag = self.cleantag(elem)
        if tag == 'StopPoints':
            self.group = self.STOPPOINTS
        # The StopAreas node also appears in a StopPoint as well as a grouping node.
        elif tag == 'StopAreas' and self.group != self.STOPPOINTS:
            self.group = self.STOPAREAS
        elif tag == '':
            pass
    
    def endElement(self, elem):
        print self.node2tag(elem)
        
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
    
    for event, elem in it:
        if event == 'start':
            parser.startElement(elem)
        elif event == 'end':
            parser.endElement(elem)
            elem.clear()
    
if __name__ == "__main__":
    f = open(sys.argv[1])
    treeparse(f)
