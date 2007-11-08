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
from time import sleep

class geoPosition:
  def __init__(self, filename):
    self.filename = filename
  def get(self):
    file = open(self.filename, 'r')
    text = file.readline(50)
    file.close()
    lat,lon = [float(i) for i in text.rstrip().split(",")]
    return(lat,lon)
    
if __name__ == "__main__":
  pos = geoPosition(sys.argv[1])
  print pos.get()