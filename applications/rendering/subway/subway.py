#!/usr/bin/python
#----------------------------------------------------------------
# Subway
#
# Takes a map data file (OpenStreetMap format), and extracts a 
# graph of a network (e.g. a metro rail system) suitable for
# dragging the nodes around in a graph editor (e.g. yed) to create
# a diagram of the network
#----------------------------------------------------------------
# Copyright 2008, OJW. Licensed as GNU GPL v3 or later
#----------------------------------------------------------------
import math
import sys
import urllib
from xml.sax import saxutils
from xml.dom import minidom
from xml.sax import make_parser
import os


class grapher(saxutils.DefaultHandler):
  def __init__(self, filename):
    parser = make_parser()
    parser.setContentHandler(self)
    print "Loading %s"%filename
    self.stations = []
    self.railways = []
    self.attr = {}
    parser.parse(filename)
    self.report('graph.gml')
  def startElement(self, name, attrs):
    """Store node positions and tag values"""
    if(name == 'node'):
      self.attr = {}
      self.attr['id'] = int(attrs.get('id', 0))
      self.attr['lat'] = float(attrs.get('lat', None))
      self.attr['lon'] = float(attrs.get('lon', None))
    if(name == 'way'):
      self.attr = {}
      self.attr['id'] = int(attrs.get('id', 0))
      self.waynodes = []
    if(name == 'nd'):
      self.waynodes.append(int(attrs.get('ref', 0)))
    if(name == 'tag'):
      self.attr[attrs.get('k', None)] = attrs.get('v', None)
  def endElement(self, name):
    rail = self.attr.get('railway')
    if(name == 'node'):
      if rail in ('station', 'halt'):
        # print "Station %s \"%s\"" % (rail, self.attr.get('name',''))
        self.stations.append(self.attr)
    if(name == 'way'):
      self.attr['nodes'] = self.waynodes
      if rail in ('subway','rail','light_rail'):
        #print "way: %s" % rail
        self.railways.append(self.attr)
  def report(self, filename):
    f = open(filename, 'w')
    useful_nodes = {}
    
    names = {}
    f.write("graph\n[\n\tdirected\t1\n")
    
    # Write stations
    for i in self.stations:
      id = i['id']
      name  = i.get('name', '?')
      useful_nodes[id] = True
      names[id] = name

    # Write end-nodes
    for i in self.railways:
      n = i['nodes']
      first = n[0]
      last = n[-1]
      useful_nodes[first] = True
      useful_nodes[last] = True

    nodecount = 0
    node_ids = {}
    for n in useful_nodes.keys():
      name = names.get(n, '?')
      f.write("\tnode\n\t[\n")
      f.write("\t\tid\t%d\n\t\tlabel\t\"%s\"\n" % (nodecount, self.safe(name)))
  
      if(0):
        f.write("\t\tgraphics\n\t\t[\n")
        int_tags = {"x":100,"y":100,"w":30,"h":30}
        text_tags = {"type":"rectangle","fill":"#FFCC00","outline":"#000000"}
        for k,v in int_tags.items():
          f.write("\t\t\t%s\t%s\n"%(k,v))
        for k,v in text_tags.items():
          f.write("\t\t\t%s\t\"%s\"\n"%(k,v))
        f.write("\t\t]\n");

      f.write("\t]\n")
      node_ids[n] = nodecount
      nodecount = nodecount + 1
      
    for i in self.railways:
      id = i['id']
      name = i.get('name', '?')
      fr = None
      for n in i['nodes']:
        a = node_ids.get(n, None)
        if(a != None):
          if(fr != None):
            name1 = names.get(fr,'-')
            name2 = names.get(n,'-')
            #f.write("node_%d -- node_%d [label=\"%s\"];\n" % (fr,n, name))
            f.write("\tedge\n\t[\n\t\tsource\t%d\n\t\ttarget\t%d\n\t\tlabel \"%s\"\n\t]\n" % (fr,a,self.safe(name)))
          fr = a
    f.write("]\n")
    f.close()
  def safe(self, text):
    text = text.replace("&", " and ")
    return(text)
  
if(__name__ == "__main__"):
  data = grapher("subway.osm")
  