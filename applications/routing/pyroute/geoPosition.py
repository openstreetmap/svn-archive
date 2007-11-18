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
        return({'valid':False,'message':"function for %s not defined" % self.mode})
    return(function())
    
  def get_none(self):
    return({'valid':False,'message':"no gps"})
  
  def socket_cmd(self, cmd):
    self.s.send("%s\r\n" % cmd)
    return self.s.recv(8192)
    
  def test_socket(self):
    for i in ('i','p','p','p','p'):
      print "%s = %s" % (i, self.socket_cmd(i))
      sleep(1)
    
  
  def get_socket(self):
    pos = self.socket_cmd("p")
    p = re.compile('P=(.*?)\s*$')
    match = p.search(pos)
    if(match == None):
      return({'valid':False,'message':"GPS returned %s" % pos})
    text = match.group(1)
    if(text == '?'):
      return({'valid':False,'message':"GPS returned P=?"})
    lat,lon = [float(ll) for ll in text.split(' ')]
    #print "matched '%s', '%s'"%(lat,lon)
    return({'valid':True, 'lat':lat, 'lon':lon, 'source':'GPS'})
  
  def get_file(self):
    file = open(self.filename, 'r')
    text = file.readline(50)
    file.close()
    try:
      lat,lon = [float(i) for i in text.rstrip().split(",")]
      return({'valid':True, 'lat':lat, 'lon':lon, 'source':'textfile'})
    except ValueError:
      return({'valid':False, 'message':'Invalid textfile'})
    
if __name__ == "__main__":
  #print dir(socket)
  pos = geoPosition()
  #print pos.test_socket()
  for i in range(5):
    print pos.get()
    sleep(1)