#!/usr/bin/python
#-----------------------------------------------------------------------------
# Handles geometry on the earth's surface (e.g. bearing/distance)
#
# Usage: 
#   (library code)
#-----------------------------------------------------------------------------
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
#-----------------------------------------------------------------------------
from math import *

def bearing(a,b):
    dlat = radians(b[0] - a[0])
    dlon = radians(b[1] - a[1])

    dlon = dlon * cos(radians(a[0]))
    
    return(degrees(atan2(dlon, dlat)))

def distance(a,b):
  lat1 = radians(a[0])
  lon1 = radians(a[1])
  lat2 = radians(b[0])
  lon2 = radians(b[1])
  d = acos(sin(lat1)*sin(lat2) + cos(lat1)*cos(lat2) * cos(lon2-lon1)) * 6371;
  return(d)

if(__name__ == "__main__"):
  a = (51.477,-0.4856)
  b = (51.477,-0.4328)

  print bearing(a,b)
  print distance(a,b)
