#!/usr/bin/python
#---------------------------------------------------------------------------
# Supplies position info from the GPS daemon
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
from module_base import ranaModule
import sys
import os
import socket
from time import sleep
import re

def getModule(m,d):
  return(gpsd(m,d))

class gpsd(ranaModule):
  """Supplies position info from GPSD"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    position_regexp = re.compile('P=(.*?)\s*$')
    self.connected = False
    try:
      self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      self.s.connect(("127.0.0.1", 2947))
      self.connected = True
    except socket.error:
      self.status = "No GPSD running"
 
  def socket_cmd(self, cmd):
    self.s.send("%s\r\n" % cmd)
    return self.s.recv(8192)
    
  def test_socket(self):
    for i in ('i','p','p','p','p'):
      print "%s = %s" % (i, self.socket_cmd(i))
      sleep(1)
  
  def update(self):
    if(not self.connected):
      self.status = "Not connected"
      return
    posString = self.socket_cmd("p")
    match = position_regexp.search(posString)
    if(match == None):
      self.status = "Invalid message"
      return
    text = match.group(1)
    if(text == '?'):
      self.status = "GPS doesn't know position"
      return
    lat,lon = [float(ll) for ll in text.split(' ')]
    self.status = "OK"
    self.set('pos', (lat,lon))
    self.set('pos_source', 'file')
  
if __name__ == "__main__":
  d = {'pos_filename':'pos.txt'}
  a = gpsd({},d)
  for i in range(5):
    a.update()
    print "%s: %s" %(a.getStatus(), d.get('pos', None))
    sleep(3)