# supplies position info from the GPS daemon
#---------------------------------------------------------------------------
# Copyright 2007-2008, Oliver White
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
#---------------------------------------------------------------------------
from base_module import ranaModule
import sys
import os
import socket
from time import *
import re

def getModule(m,d):
  return(gpsd2(m,d))

class gpsd2(ranaModule):
  """Supplies position info from GPSD"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.tt = 0
    self.position_regexp = re.compile('P=(.*?)\s*$')
    self.connected = False
    try:
      self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      self.s.connect(("127.0.0.1", 2947))
      self.connected = True
    except socket.error:
      self.status = "No GPSD running"
 
  def socket_cmd(self, cmd):
    self.s.send("%s\r\n" % cmd)
    result = self.s.recv(8192)
    #print "Reply: %s" % result
    expect = 'GPSD,' + cmd.upper() + '='
    if(result[0:len(expect)] != expect):
      print "Fail: received %s after sending %s" % (result, cmd)
      return(None)
    remainder = result[len(expect):]
    if(remainder[0:1] == '?'):
      print "Fail: Unknown data in " + cmd
      return(None)
    return(remainder)
    
  def test_socket(self):
    for i in ('i','p','p','p','p'):
      print "%s = %s" % (i, self.socket_cmd(i))
      sleep(1)
      
  def gpsStatus(self):
    return(self.socket_cmd("M"))
  
  def satellites(self):
    list = self.socket_cmd('y')
    if(not list):
      return
    parts = list.split(':')
    (spare1,spare2,count) = parts[0].split(' ')
    count = int(count)
    self.set("gps_num_sats", count)
    for i in range(count):
      (prn,el,az,db,used) = [int(a) for a in parts[i+1].split(' ')]
      self.set("gps_sat_%d"%i, (db,used,prn))
      #print "%d: %d, %d, %d, %d, %d" % (i,prn,el,az,db,used)

  def quality(self):
    result = self.socket_cmd('q')
    if(result):
      (count,dd,dx,dy) = result.split(' ')
      count = int(count)
      (dx,dy,dd) = [float(a) for a in (dx,dy,dd)]
      print "%d sats, quality %f, %f, %f" % (count,dd,dx,dy)

  def update(self):
    dt = time() - self.tt
    if(dt < 2):
      return
    self.tt = time()

    if(not self.connected):
      self.status = "Not connected"
    else:
      result = self.socket_cmd("p")
      if(not result):
        self.status = "Unknown"
      else:
        lat,lon = [float(ll) for ll in result.split(' ')]
        self.status = "OK"
        self.set('pos', (lat,lon))
        self.set('pos_source', 'GPSD')
        self.set('needRedraw', True)
        self.satellites()

  
if __name__ == "__main__":
  d = {}
  a = gpsd2({},d)
  print a.gpsStatus()
  #print a.quality()
  print a.satellites()

  if(0):
    for i in range(2):
      a.update()
      print "%s: %s" %(a.status, d.get('pos', None))
      sleep(2)

