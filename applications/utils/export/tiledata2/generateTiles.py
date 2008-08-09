#!/usr/bin/python
# -*- coding: UTF-8 -*-
import sqlite3
import sys
import os
from tilenames import *
import mobileEncoding
import tiledata_pb2

if(__name__ == "__main__"):

  conn = {}
  conn['meta_db'] = sqlite3.connect('data/db.dat')
  conn['way_db'] = sqlite3.connect('data/way.dat')
  conn['map_db'] = sqlite3.connect('data/map.dat')

  for db in ('meta','way','map'):
    conn[db] = conn[db+'_db'].cursor()
    conn[db+'_db'].text_factory = str

  conn['map'].execute("drop table if exists maps")
  conn['map'].execute("create table maps (tile text primary key, data blob)")
  
  conn['meta'].execute('select * from wayloc')
  tiles = {}
  for wayloc in conn['meta']:
    (tile,wid) = wayloc
    tiles[tile] = tiles.get(tile,0) + 1

  for tile,count in tiles.items():
    print "Generating %s" % tile
    
    mapData = tiledata_pb2.Osm()

    conn['meta'].execute('select * from wayloc where tile=?', (tile,))
    ways = conn['meta'].fetchall()
    for wayloc in ways:
      (tile,wid) = wayloc
      
      conn['way'].execute('select * from ways where id=?', (wid,))
      wayRow = conn['way'].fetchone()

      (wid2,data) = wayRow
      #mobileEncoding.printWay(data)
      way = mapData.way.add()
      way.ParseFromString(data)
      
    if(0):
      for w in mapData.way:
        mobileEncoding.printWayObject(w)

    mapString = mapData.SerializeToString()
    sqlMapString = sqlite3.Binary(mapString)
    
    print " - created %d (%d) bytes" % (len(mapString), len(sqlMapString))

    conn['map'].execute("insert into maps values(?,?)", (tile, sqlMapString))
    conn['map_db'].commit()
    

