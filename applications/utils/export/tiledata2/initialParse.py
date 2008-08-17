#!/usr/bin/python
# -*- coding: UTF-8 -*-
import bsddb
import sys
import os
from xml.sax import make_parser, handler
import xml
import tilenames
import enums
import struct

z = 14

class average:
  def __init__(self):
    self.count = 0
    self.total = 0
    self.max = 0
  def add(self,num):
    self.count += 1
    self.total += num
    if(num > self.max):
      self.max = num
  def average(self):
    return(self.total/self.count)

class filePool:
  def __init__(self, maxfiles = 120):
    """Keep a pool of filehandles, reusing them and closing the
    ones which haven't been used for longest.
    Higher maxfiles mean it doesn't need to open files so often,
    so if the OS can afford all that many filehandles then great."""
    self.f = {}
    self.access = {}
    self.t = 0
    self.limit = maxfiles
    self.numOpens = 0
    self.average = average()
  def fp(self,filename, mode="a"):
    # record access-time
    self.access[filename] = self.t
    self.t += 1
    # if already open, return
    f = self.f.get(filename, None)
    if(f):
      return(f)
    #print "Opening %s" % filename
    # if too many open, close one
    if(len(self.f.keys()) >= self.limit):
      self.closeOne()
    # open new file
    f = open(filename, mode)
    self.f[filename] = f
    self.numOpens += 1
    return(f)
  def closeOne(self):
    minT = None
    oldestFilename = None
    for filename,t in self.access.items():
      if(minT == None or t < minT):
        oldestFilename = filename
        minT = t
    #print "T = %d, oldest = %d" % (self.t, minT)
    if(oldestFilename):
      self.close(oldestFilename)
  def close(self,filename):
    age = self.t - self.access[filename]
    self.average.add(age)
    #print "Closing %s, age %d" % (filename, age)
    self.f[filename].close()
    del(self.f[filename])
    del(self.access[filename])
  def closeAll(self):
    for k,v in self.f.items():
      self.close(k)
    print "Opened %d files, average life %d" % (self.numOpens, self.average.average())
      
class dbStore:
  def __init__(self, filename, nodesExist = False):
    self.db = bsddb.btopen(filename, 'c')
    self.nodesExist = nodesExist
    if(nodesExist):
      print "Assuming that nodes already exist in %s" % filename
    self.countNodes = 0
    self.countWays = 0
    self.countPoi = 0
    self.nodeErrors = 0
    self.promptFreq = 10000
    self.prompt = self.promptFreq
    self.setupEnums()
    self.average = average()
    self.files = filePool()
  def cleanup(self):
    self.files.closeAll()
  def setupEnums(self):
    self.enums = enums.makeEnums()
    self.enumIDs = {}
    count = 1
    f = open('enums.txt','w')
    for name in sorted(self.enums.enums.keys()):
      f.write("%d\t%s\n" % (count,name))
      self.enumIDs[name] = count
      count += 1
    f.close()
    
  def wayStyle(self, tags):
    styles = []
    for style in self.enums.equiv:
      if(style['fn'](tags)):
        style2 = self.enums.enums[style['equiv']]
        styles.append(style2)
    for name, style in self.enums.enums.items():
      if(style['fn'](tags)):
        styles.append(style)
    return(styles)
            
  def storeNode(self,id,lat,lon):
    """Store a node"""
    if(not self.nodesExist):
      (x,y) = tilenames.tileXY(lat,lon,z)
      self.db[str(id)] = "%f,%f,%d_%d"%(lat,lon,x,y)
      
      self.countNodes += 1
      self.prompt -= 1
      if(not self.prompt):
        self.prompt = self.promptFreq
        print "%1.3fM nodes" % (float(self.countNodes) / 1000000.0)

  def storeWay(self,wid,nodes, tags):
    # Progress/status
    if(not self.countWays):
      print "Starting ways"
      self.prompt = self.promptFreq
    self.countWays += 1
    self.prompt -= 1
    if(not self.prompt):
      self.prompt = self.promptFreq
      print "%1.3fM ways" % (float(self.countWays) / 1000000.0)
    
    # Lookup the nodes used in the way, find their lat/long,
    # and compile a list of tiles that the way 'touches'
    waynodes = []
    tilesTouched = {}
    for nid in nodes:
      try:
        text = self.db[str(nid)]
        if(not text):
          self.nodeErrors += 1
        else:
          (lat,lon,tile) = text.split(",")
          tilesTouched[tile] = True
          waynodes.append((nid,float(lat),float(lon)))
      except KeyError:
        self.nodeErrors += 1

    # Convert OSM tags into styles being used in output format
    styles = self.wayStyle(tags)
    #print ", ".join(["%s = %d" % (a['name'], self.enumIDs[a['name']]) for a in styles])

    data = ''
    for style in styles:
      data += self.packWay(wid, waynodes, style, tags)
    if(data):
      for tile in tilesTouched.keys():
        filename = self.filename(tile)
        f = self.files.fp(filename)
        if(f):
          f.write(data)
      self.average.add(len(data))

  def filename(self,tile):
    (x,y) = [int(i) for i in tile.split("_")]
    tx = int(x / 64)
    ty = int(y / 64)
    directory = "output/%d_%d/" % (tx,ty)
    if(not os.path.exists(directory)):
      os.mkdir(directory)
    return("%s/%s.bin" % (directory,tile))
    
  
  def packWay(self, id, nodes, style, tags):
    """Compress a way for storage"""
    data = ''
    
    data += struct.pack("I", id)
    
    # Nodes
    data += struct.pack("I", len(nodes))
    for n in nodes:
      (id,lat,lon) = n
      (x,y) = tilenames.tileXY(lat,lon,31)
      data += struct.pack("III", x, y, id)

    # Styles
    data += struct.pack("I", self.enumIDs[style['name']])

    # Layer
    try:
      layer = int(tags.get('layer',0))
    except ValueError:  # yeah thanks for whoever entered "=1" as a layer number...
      layer = 0
    data += struct.pack("b", layer)

    # Important tags
    packedTags = ''
    packedTags += self.packTag("N", tags, 'name')
    packedTags += self.packTag("r", tags, 'ref')
    packedTags += self.packTag("r", tags, 'ncn_ref')
    data += struct.pack("H", len(packedTags)) + packedTags

    return(data)
    
  def packTag(self, type, tags, tag):
    string = tags.get(tag, None)
    if(string):
      string = str(string)
      return(type + struct.pack("H", len(string)) + string)
    return('')

  def storePoi(self,id,tags, latlon):
    """Store an interesting node"""
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


if(__name__ == "__main__"):
  filename = "data/nodes.dat"
  db = dbStore(filename, os.path.exists(filename))
  a = osmParser(sys.stdin, db)
  db.cleanup()
  print "%d ways, average length %1.0f bytes, max %d" % (db.average.count, db.average.average(), db.average.max)
