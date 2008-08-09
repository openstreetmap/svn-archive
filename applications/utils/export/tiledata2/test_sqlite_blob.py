#!/usr/bin/python
# -*- coding: UTF-8 -*-
import sqlite3
import sys
import os
from struct import pack

if(__name__ == "__main__"):
  print sys.getdefaultencoding()
  
  conn = sqlite3.connect('test.dat')

  c = conn.cursor()
  c.execute("drop table if exists test2")
  c.execute("create table test2 (data blob)")
  
  data = pack('hhl', 1, 2, 3) #"メイ"
  print "Storing %d bytes" % len(data)

  bin = sqlite3.Binary(data)
  c.execute("insert into test2 values(?)", (bin,))
  conn.commit()

  c.execute('select * from test2')
  row = c.fetchone()
  data2 = row[0]
  
  print "Retrieved %d bytes" % len(data2)

  print data == data2
  
      
    
