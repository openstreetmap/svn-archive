#!/usr/bin/python
#----------------------------------------------------------------
# report "current position" (currently accepts replayed GPX
# files from follow.py)
#------------------------------------------------------
# Usage: 
#   pos = geoPosition(filename_of_latlong_file)
#   lat,lon = pos.get()
#------------------------------------------------------
# Copyright 2007, Oliver White
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
#------------------------------------------------------
import sys
import os
import socket
from time import sleep
import re

class geoPosition:
  def __init__(self):
    self.mode = "none"
    try:
      self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      self.s.connect(("127.0.0.1", 2947))
      self.mode = "socket"
    except socket.error:
      print "No GPSD detected, position information will not be available"
      pass # leave mode at "none"
    
    self.filename = "pos.txt"
    if(os.path.exists(self.filename)):
      self.mode = "file"
      print "Using %s as position reference" % self.filename
  
  def get(self):
    try:
        function = getattr(self, 'get_%s' % self.mode)
    except AttributeError:
        print "Error: position mode %s not defined" % self.mode
        self.mode = 'none'
        return(0,0)
    return(function())
    
  def get_none(self):
    return((False,0,0))
  
  def socket_cmd(self, cmd):
    self.s.send("%s\r\n" % cmd)
    return self.s.recv(8192)
    
  def test_socket(self):
    for i in ('i','p','p','p','p'):
      print "%s = %s" % (i, self.socket_cmd(i))
      sleep(1)
    pos = self.socket_cmd("p")
    p = re.compile('P=(.*?)\s+(.*?)\s*')
    match = p.search(pos)
    print (pos, dir(match), match.group(1))
  
  def get_socket(self):
    pos = self.socket_cmd("p")
    p = re.compile('P=([0-9.]+?)\s+([0-9.]+?)\s*')
    match = p.search(pos)
    if(match != None):
      text1 = match.group(1)
      text2 = match.group(2)
      print "matched '%s', '%s'"%(text1,text2)
      return((True, float(text1), float(text2)))
    return((False, 0,0))
  
  def get_file(self):
    file = open("pos.txt", 'r')
    text = file.readline(50)
    file.close()
    try:
      lat,lon = [float(i) for i in text.rstrip().split(",")]
      return((True, lat,lon))
    except ValueError:
      return((False, 0,0))
    
if __name__ == "__main__":
  #print dir(socket)
  pos = geoPosition()
  print pos.get()