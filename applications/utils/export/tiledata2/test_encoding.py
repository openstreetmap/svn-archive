#!/usr/bin/python
# -*- coding: UTF-8 -*-
import sqlite3
import sys
import os
from tilenames import *
import mobileEncoding


if(__name__ == "__main__"):
  print sys.getdefaultencoding()

  conn = sqlite3.connect('test.dat')
  conn.text_factory = str

  c = conn.cursor()
  c.execute("drop table if exists test")
  c.execute("create table test (id int, data blob)")
  
  data = mobileEncoding.storeWay(((1,51,10),(2,52,20),(3,53,30),(4,54,50),(5,55,50)), {"highway":"motorway","oneway":"true"})
  #mobileEncoding.printWay(data)
  
  c.execute("insert into test values(?, ?)", (123, sqlite3.Binary(data),))
  conn.commit()

  c.execute('select * from test')
  row = c.fetchone()
  (wid,data2) = row

  print "Saved %d, retrieved %d bytes" % (len(data), len(data2))
  print wid
  if(data != data2):
    print "Data is different"
  
  mobileEncoding.printWay(data2)
      
    
