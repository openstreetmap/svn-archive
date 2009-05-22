#!/usr/bin/env python2.5
"""Parse OSM files. """

import xml.parsers.expat
import sys
import psyco
psyco.full()

class OsmData:
    """Ultra-lightweight parser that keeps just the bare minimum of data."""
    ignoredElements = ("osm", "bounds", "member", "relation", "bound")
    wayTags = ['highway']

    def __init__(self):
        self.parser = xml.parsers.expat.ParserCreate()
        self.parser.StartElementHandler = self.startElement
        self.currentWay = None
        self.nodes = {}
        self.ways = []
        self.elementcount = 0

    def parseFile(self, filename):
        f = open(filename, 'r')
        self.parse(f)
        f.close()

    def parse(self, filehandle):
        self.elementcount = 0
        self.parser.ParseFile(filehandle)

    def startElement(self, name, attr):
        self.elementcount += 1
        if (self.elementcount & 65535) == 0:
            print self.elementcount
            
        if name == "tag":
            if self.currentWay is not None:
                if attr['k'] in self.wayTags:
                    self.ways.append(self.currentWay)
            return

        if name == "node":
            self.nodes[int(attr['id'])] = (float(attr['lat']), float(attr['lon']))
            return

        if name == "way":
            self.currentWay = {'id': attr['id'], 'nodes': []}
            return

        if name == "nd":
            self.currentWay['nodes'].append(int(attr['ref']))
            return

        if name in self.ignoredElements:
            return

        print "Unknown Element", name, attr

if __name__ == '__main__':
    data = OsmData()
    data.parse(sys.stdin)
    print len(data.nodes), len(data.ways)
    #print data.nodes
    #print data.ways