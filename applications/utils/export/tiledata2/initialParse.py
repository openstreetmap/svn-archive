#!/usr/bin/python
# -*- coding: UTF-8 -*-
import sqlite3
import sys
import os
from xml.sax import make_parser, handler
import xml
from tilenames import *
import mobileEncoding

z = 12

class dbStore:
  def __init__(self, c):
    self.c = c
    self.nodes = []
    self.countNodes = 0
    self.countWays = 0
    self.countPoi = 0
    self.nodeErrors = 0
    self.unsavedNodes = False
    
  def storeNode(self,id,lat,lon):
    (x,y) = tileXY(lat,lon,z)
    tile = "%05d_%05d" % (x,y)
    self.unsavedNodes = True
    self.nodes.append((id,lat,lon, tile))
    if(len(self.nodes) == 10000):
      self.saveNodes()
      
  def saveNodes(self):
      self.countNodes += len(self.nodes)
      self.c['meta'].executemany("insert into nodes values (?, ?, ?, ?)", self.nodes)
      self.c['meta_db'].commit()
      print "Done %dk nodes" % (self.countNodes / 1000)
      self.nodes = []
      self.unsavedNodes = False
  
  def lookupNode(self, id):
    self.c['meta'].execute('select * from nodes where id=?', (id,))
    return(self.c['meta'].fetchone())
    
  def storeWay(self,wid,nodes, tags):
    if(self.unsavedNodes):
      self.saveNodes()
    waynodes = []
    tilesTouched = {}
    for nid in nodes:
      node = self.lookupNode(nid)
      if(not node):
        self.nodeErrors += 1
      else:
        (nid2,lat,lon,tile) = node
        tilesTouched[tile] = True
        waynodes.append((nid,lat,lon))
    data = []
    for tile in tilesTouched.keys():
      data.append((tile, wid))
    self.c['meta'].executemany("insert into wayloc values (?, ?)", data)
    self.c['meta_db'].commit()

    data = sqlite3.Binary(mobileEncoding.storeWay(waynodes, tags))
    self.c['way'].execute("insert into ways values(?,?)", (wid,data))
    self.c['way_db'].commit()
    if(self.countWays % 10000 == 0):
      print "Done %dk ways" % (self.countWays / 1000)
    self.countWays += 1

  def storePoi(self,id,tags, latlon):
    data = sqlite3.Binary(mobileEncoding.storePOI(id,tags,latlon))

    self.c['poi'].execute("insert into poi values(?,?)", (id,data))
    self.c['poi_db'].commit()
    self.countPoi += 1

  
class osmParser(handler.ContentHandler):
  def __init__(self, filename,db):
    self.db = db
    self.useless = ('',
      'created_by',
      'source',
      'editor',
      'ele',
      'time',
      'editor',
      'author',
      'hdop',
      'pdop',
      'sat',
      'speed',
      'fix',
      'course',
      'converted_by',
      'attribution',
      'upload_tag',
      'history')
        
    try:
      parser = make_parser()
      parser.setContentHandler(self)
      parser.parse(filename)
    except xml.sax._exceptions.SAXParseException:
      print "Error loading %s" % filename
    print "Done\n%d nodes\n%d ways\n%d POIs\n%d nodes not found"% (
      self.db.countNodes,
      self.db.countWays,
      self.db.countPoi,
      self.db.nodeErrors)
  def startElement(self, name, attrs):
    """Handle XML elements"""
    if name in('node','way','relation'):
      self.tags = {}
      self.isInteresting = False
      self.waynodes = []
      if name == 'node':
        id = int(attrs.get('id'))
        self.nodeID = id
        lat = float(attrs.get('lat'))
        lon = float(attrs.get('lon'))
        self.nodepos = (lat,lon)
        self.db.storeNode(id,lat,lon)
      elif name == 'way':
        id = int(attrs.get('id'))
        self.wayID = id
    elif name == 'nd':
      """Nodes within a way -- add them to a list"""
      node = int(attrs.get('ref'))
      self.waynodes.append(node)
    elif name == 'tag':
      """Tags - store them in a hash"""
      k,v = (attrs.get('k'), attrs.get('v'))
      
      # Test if a tag is interesting enough to make it worth
      # storing this node as a special "point of interest"
      if not k in self.useless:
        if(k[0:5] != "tiger"):
          self.tags[k] = v
          self.isInteresting = True
  
  def endElement(self, name):
    if name == 'way':
      self.db.storeWay(self.wayID,self.waynodes,self.tags)
      
    elif name == 'node':
      if(self.isInteresting):
        self.db.storePoi(self.nodeID,self.tags, self.nodepos)



def insertNodes(c, nodes):
  c.executemany("insert into nodes values (?, ?, ?, ?)", nodes)
  conn.commit()


if(__name__ == "__main__"):

  conn = {}
  conn['meta_db'] = sqlite3.connect('data/meta.dat')
  conn['poi_db'] = sqlite3.connect('data/poi.dat')
  conn['poi_db'].text_factory = str
  conn['way_db'] = sqlite3.connect('data/way.dat')
  conn['way_db'].text_factory = str

  conn['meta'] = conn['meta_db'].cursor()
  conn['poi'] = conn['poi_db'].cursor()
  conn['way'] = conn['way_db'].cursor()

  if(len(sys.argv) > 1 and sys.argv[1] == "print"):
    print "Printing POIs:"
    conn['poi'].execute('select * from poi')
    #a = conn['poi'].fetchall()
    for poi in conn['poi']:
      (id,data) = poi
      print "----------------------"
      mobileEncoding.printPOI(data)
    
  
  elif(len(sys.argv) > 1 and sys.argv[1] == "find"):
    db = dbStore(conn)
    print db.lookupNode(100)
    print db.lookupNode(-100)
    

  else:
  
    if(1):
      conn['poi'].execute("drop table if exists poi")
      conn['poi'].execute("create table poi (id int primary key, data blob);")
      conn['poi_db'].commit()

      conn['way'].execute("drop table if exists ways")
      conn['way'].execute("create table ways (id int primary key, data blob);")
      conn['way_db'].commit()
      
      
      conn['meta'].execute("drop table if exists nodes")
      conn['meta'].execute("""create table nodes (id int primary key, lat real, lon real, tile text(20))""")
      
      conn['meta'].execute("drop table if exists wayloc")
      conn['meta'].execute("""create table wayloc (tile text(20), way int)""")
      conn['meta_db'].commit()

    db = dbStore(conn)
    a = osmParser(sys.stdin, db)
    