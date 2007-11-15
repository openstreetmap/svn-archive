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
import gps
import os
import socket
from time import sleep

class geoPosition:
  def __init__(self):
    self.mode = "none"
    try:
      self.gpsd = gps.gps()
      self.mode = "gpsd"
      return
    except socket.error:
      pass
    
    self.filename = "pos.txt"
    if(os.path.exists(self.filename)):
      self.mode = "file"
  
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
  
  def get_gpsd(self):
    self.gps.query('mosy')
    # a = altitude, d = date/time, m=mode,
    # o=postion/fix, s=status, y=satellites    
    return((True, self.gpsd.fix.latitude, self.gpsd.fix.longitude))
  
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
  pos = geoPosition()
  print pos.get()