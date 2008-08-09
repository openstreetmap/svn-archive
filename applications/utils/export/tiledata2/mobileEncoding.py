#!/usr/bin/python
# -*- coding: UTF-8 -*-
import tiledata_pb2
import tilenames

def printPOI(data):
  node = tiledata_pb2.Node()
  node.ParseFromString(data)
  if(node.HasField("lat") and node.HasField("lon")):
    print "%f,%f" % (node.lat, node.lon)
  if(node.HasField("x") and node.HasField("y")):
    (lat,lon) = tilenames.xy2latlon(node.x,node.y,31)
    print "%f,%f" % (lat, lon)
  for t in node.tag:
    print "* %s = %s" % (t.k, t.v)

def printWayObject(way):
  for n in way.node:
    print "n%d: %f, %f" % (n.id, n.lat, n.lon)
  for t in way.tag:
    print "* %s = %s" % (t.k, t.v)
  
def printWay(data):
  way = tiledata_pb2.Way()
  way.ParseFromString(data)
  printWayObject(way)

def storeWay(nodes, tags):
  way = tiledata_pb2.Way()
  
  for n in nodes:
    node = way.node.add()
    (nid,lat,lon) = n
    node.id = nid
    node.lat = lat
    node.lon = lon

  for k,v in tags.items():
    tag = way.tag.add()
    tag.k = k.encode("utf-8")
    tag.v = v.encode("utf-8")
  return(way.SerializeToString())

def storePOI(nid, tags, latlon, storeTags=True, storeXY=True):
  node = tiledata_pb2.Node()
 
  if(0):
    node.id = nid;

  if(storeXY):
    (x,y) = tilenames.tileXY(latlon[0],latlon[1],31)
    node.x = x
    node.y = y
  else:
    node.lat = latlon[0];
    node.lon = latlon[1];

  if(storeTags):
    for k,v in tags.items():
      tag = node.tag.add()
      tag.k = k.encode("utf-8")
      tag.v = v.encode("utf-8")
  return(node.SerializeToString())

if(__name__ == "__main__"):
  if(0):
    a = storePOI(1,{"highway":"motorway", "japanese":"メイ", "russian":"Предислови", "oneway":"true"},(51.5,-0.5))
    print "Done, %d bytes" % len(a)
    printPOI(a)

  b = storeWay(((1,51,10),(2,52,20),(3,53,30),(4,54,50),(5,55,50)), {"highway":"motorway","oneway":"true"})
  print "Done, %d bytes" % len(b)
  printWay(b)
  