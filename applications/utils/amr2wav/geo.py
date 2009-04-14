#!/usr/bin/python
#---------------------------------------------------------------------------
# Geographic calculations
#---------------------------------------------------------------------------
# Copyright 2007-2008, Oliver White
#                2009, Graham Jones (minor corrections)
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
from math import *

def distance(lat1,lon1,lat2,lon2):
  """Distance between two points in km"""
  R = 6371.0
  dLat = radians(lat2-lat1)
  dLon = radians(lon2-lon1)
  a = sin(dLat/2.0) * sin(dLat/2.0) + \
          cos(radians(lat1)) * cos(radians(lat2)) * \
          sin(dLon/2.0) * sin(dLon/2.0)
  # GJ Added error correction for rounding errors.
  if a>1: a=1
  c = 2 * atan2(sqrt(a), sqrt(1.0-a))
  return(R * c)

def bearing(lat1,lon1,lat2,lon2):
  """Bearing from one point to another in degrees (0-360)"""
  dLat = lat2-lat1
  dLon = lon2-lon1
  y = sin(radians(dLon)) * cos(radians(lat2))
  x = cos(radians(lat1)) * sin(radians(lat2)) - \
          sin(radians(lat1)) * cos(radians(lat2)) * cos(radians(dLon))
  bearing = degrees(atan2(y, x))
  if(bearing < 0.0):
    bearing += 360.0
  return(bearing)


if(__name__ == "__main__"):
  print distance(51,-1,52,1)
  print bearing(51,-1,52,1)
