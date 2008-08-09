#!/usr/bin/python
# -*- coding: UTF-8 -*-
import sqlite3
import sys
import os
from tilenames import *
import mobileEncoding
import tiledata_pb2

def getMap(x,y, filename='data/map.dat'):
  tile = "%05d_%05d" % (x,y)
  conn = sqlite3.connect(filename)
  c = conn.cursor()
  conn.text_factory = str
  c.execute("select data from maps where tile=?", (tile,))
  row = c.fetchone()
  if(not row):
    return
  mapDataString = row[0]
  mapData = tiledata_pb2.Osm()
  mapData.ParseFromString(mapDataString)
  return(mapData)

def printMap(mapData):
  if(not mapData):
    print "No data"
  else:
    for w in mapData.way:
      mobileEncoding.printWayObject(w)

  
if(__name__ == "__main__"):
  printMap(getMap(16372,10891))

