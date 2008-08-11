#!/usr/bin/python
# -*- coding: UTF-8 -*-
import sqlite3
import sys
import os
import tilenames
import mobileEncoding
import tiledata_pb2
import struct

def packMapData(mapData, wayID):
  data = struct.pack("I", 1280)
  data += struct.pack("I", len(mapData.way))
  for w in mapData.way:
    data += struct.pack("I", w.id)
    data += struct.pack("I", len(w.node))
    for n in w.node:
      (x,y) = tilenames.tileXY(n.lat,n.lon,31)
      data += struct.pack("III", x, y, n.id)
    data += struct.pack("I", len(w.tag))
    for t in w.tag:
      data += struct.pack("HH", len(t.k), len(t.v))
      data += t.k
      data += t.v
  return(data)

if(__name__ == "__main__"):

  conn = {}
  metadb = sqlite3.connect('data/meta.dat')
  waydb = sqlite3.connect('data/way.dat')

  print "Connected"
  c1 = metadb.cursor()
  c2 = metadb.cursor()
  cw = waydb.cursor()
  waydb.text_factory = str

  outdir = "simple"
  if(not os.path.exists(outdir)):
    os.mkdir(outdir)

  grouping = pow(2, 12-9)
  
  c1.execute('select * from wayloc')
  tilesDone = {}
  dirMade = {}
  for wayloc in c1:
    (tile,wid) = wayloc
    
    if(not tilesDone.get(tile, False)):
      tilesDone[tile] = True
      (tx,ty) = [int(i) for i in tile.split("_")]
     
      mapData = tiledata_pb2.Osm()

      c2.execute('select * from wayloc where tile=?', (tile,))
      ways = c2.fetchall()
      for wayloc in ways:
        (tile,wid) = wayloc
        
        cw.execute('select * from ways where id=?', (wid,))
        wayRow = cw.fetchone()

        (wid2,data) = wayRow
        
        way = mapData.way.add()
        way.ParseFromString(data)
        way.id = wid

      mapString = packMapData(mapData, wid)

      dtx = int(tx / grouping)
      dty = int(ty / grouping)
      directory = "simple/%d_%d" % (dtx, dty)
      if(not dirMade.get(directory, False) and not os.path.exists(directory)):
        os.mkdir(directory)
      dirMade[directory] = True
      
      filename = "%s/%d_%d.dat" % (directory,tx,ty)
      f = open(filename, "wb")
      f.write(mapString)
      f.close()
      
      print "%s: %d bytes" % (filename, len(mapString))
      