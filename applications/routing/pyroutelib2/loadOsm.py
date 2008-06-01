#!/usr/bin/python
#----------------------------------------------------------------
# load OSM data file into memory
#
#------------------------------------------------------
# Copyright 2007, Oliver White
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#------------------------------------------------------
# Changelog:
#  2007-11-04  OJW  Modified from pyroute.py
#  2007-11-05  OJW  Multiple forms of transport
#------------------------------------------------------
import sys
import os
import tiledata
import tilenames
import re

execfile("weights.py")

class LoadOsm:
  """Parse an OSM file looking for routing information, and do routing with it"""
  def __init__(self, transport, storeMap = 0):
    """Initialise an OSM-file parser"""
    self.routing = {}
    self.routeTypes = ('cycle','car','train','foot','horse')
    self.routeableNodes = {}
    for routeType in self.routeTypes:
      self.routing[routeType] = {}
      self.routeableNodes[routeType] = {}
    self.nodes = {}
    self.ways = []
    self.storeMap = storeMap
    self.transport = transport
  
  def getArea(self, lat, lon):
    z = tiledata.DownloadLevel()
    (x,y) = tilenames.tileXY(lat, lon, z)
    print "Downloading %d,%d at z%d" % (x,y,z)
    filename = tiledata.GetOsmTileData(z,x,y)
    return(self.loadOsm(filename))

  def loadOsm(self, filename):
    if(not os.path.exists(filename)):
      print "No such data file %s" % filename
      return(False)
    fp = open(filename, "r")
    re_way = re.compile("<way id='(\d+)'>\s*$")
    re_nd = re.compile("\s+<nd id='(\d+)' x='(\d+)' y='(\d+)' />\s*$")
    re_tag = re.compile("\s+<tag k='(.*)' v='(.*)' />\s*$")
    re_endway = re.compile("</way>$")
    in_way = 0

    way_tags = {}
    way_nodes = []

    for line in fp:
      result_way = re_way.match(line)
      result_endway = re_endway.match(line)
      if(result_way):
        in_way = True
        way_tags = {}
        way_nodes = []
        way_id = int(result_way.group(1))
      elif(result_endway):
        in_way = False
        self.storeWay(way_id, way_tags, way_nodes)
      elif(in_way):
        result_nd = re_nd.match(line)
        if(result_nd):
          way_nodes.append([
            int(result_nd.group(1)),
            int(result_nd.group(2)),
            int(result_nd.group(3))])
        else:
          result_tag = re_tag.match(line)
          if(result_tag):
            way_tags[result_tag.group(1)] = result_tag.group(2)
    return(True)
  
  def storeWay(self, wayID, tags, nodes):
    highway = self.equivalent(tags.get('highway', ''))
    railway = self.equivalent(tags.get('railway', ''))
    oneway = tags.get('oneway', '')
    reversible = not oneway in('yes','true','1')

    # Calculate what vehicles can use this route
    access = {}
    access['cycle'] = highway in ('primary','secondary','tertiary','unclassified','minor','cycleway','residential', 'track','service')
    access['car'] = highway in ('motorway','trunk','primary','secondary','tertiary','unclassified','minor','residential', 'service')
    access['train'] = railway in('rail','light_rail','subway')
    access['foot'] = access['cycle'] or highway in('footway','steps')
    access['horse'] = highway in ('track','unclassified','bridleway')

    # Store routing information
    last = -1
    for i_obj in nodes:
      i = i_obj[0]
      if last != -1:
        for routeType in self.routeTypes:
          if(access[routeType]):
            weight = getWeight(routeType, highway)
            self.addLink(last, i, routeType, weight)
            if reversible or routeType == 'foot':
              self.addLink(i, last, routeType, weight)
      last = i

    # Store map information
    if(self.storeMap):
      wayType = self.WayType(tags)
      if(wayType):
        self.ways.append({ \
          't':wayType,
          'n':nodes})

  def addLink(self,fr,to, routeType, weight=1):
    """Add a routeable edge to the scenario"""
    self.routeablefrom(fr,routeType)
    try:
      if to in self.routing[routeType][fr].keys():
        return
      self.routing[routeType][fr][to] = weight
    except KeyError:
      self.routing[routeType][fr] = {to: weight}

  def WayType(self, tags):
    # Look for a variety of tags (priority order - first one found is used)
    for key in ('highway','railway','waterway','natural'):
      value = tags.get('highway', '')
      if value:
        return(self.equivalent(value))
    return('')
  def equivalent(self,tag):
    """Simplifies a bunch of tags to nearly-equivalent ones"""
    equivalent = { \
      "primary_link":"primary",
      "trunk":"primary",
      "trunk_link":"primary",
      "secondary_link":"secondary",
      "tertiary":"secondary",
      "tertiary_link":"secondary",
      "residential":"unclassified",
      "minor":"unclassified",
      "steps":"footway",
      "driveway":"service",
      "pedestrian":"footway",
      "bridleway":"cycleway",
      "track":"cycleway",
      "arcade":"footway",
      "canal":"river",
      "riverbank":"river",
      "lake":"river",
      "light_rail":"railway"
      }
    try:
      return(equivalent[tag])
    except KeyError:
      return(tag)
    
  def routeablefrom(self,fr,routeType):
    self.routeableNodes[routeType][fr] = 1

  def findNode(self,lat,lon,routeType):
    """Find the nearest node to a point.
    Filters for nodes which have a route leading from them"""
    maxDist = 1000
    nodeFound = None
    for id in self.routeableNodes[routeType].keys():
      if id not in self.nodes:
        print "Ignoring undefined node %s" % id
        continue
      n = self.nodes[id]
      dlat = n[0] - lat
      dlon = n[1] - lon
      dist = dlat * dlat + dlon * dlon
      if(dist < maxDist):
        maxDist = dist
        nodeFound = id
    return(nodeFound)
      
  def report(self):
    """Display some info about the loaded data"""
    report = "Loaded %d nodes,\n" % len(self.nodes.keys())
    report = report + "%d ways, and...\n" % len(self.ways)
    for routeType in self.routeTypes:
      report = report + " %d %s routes\n" % ( \
        len(self.routing[routeType].keys()),
        routeType)
    return(report)

# Parse the supplied OSM file
if __name__ == "__main__":
  data = LoadOsm("cycle", True)
  print data.getArea(52.55291,-1.81824)
  print data.report()
