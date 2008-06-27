#!/usr/bin/python
#----------------------------------------------------------------------------
# Allows searching by place name
#----------------------------------------------------------------------------
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
import cairo
from datetime import *
import math

def getModule(m,d):
  return(placenames(m,d))

class placenames(ranaModule):
  """Lookup nearest town or village"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.places = {'v':[],'c':[],'t':[]} # village, city, town
    self.load("places.txt")
    self.lastpos = (0,0)

  def load(self, filename):
    file = open(filename,"r")

    for line in file:
      line = line.strip()
      (lat,lon,id,type,name) = line.split("\t")
      if(type in ('t','c','v')):
        item = (float(lat),float(lon),name)
        self.places[type].append(item)
        
  def lookupPlace(self, lat, lon, type, radiusKm):
    kmToDeg = 360.0 / 40041.0
    radius = radiusKm * kmToDeg
    limitSq = radius * radius
    nearest = None
    nearestDist = limitSq
    
    for place in self.places[type]:
      (plat,plon,name) = place
      dx = plon - lon
      dy = plat - lat
      dist = dx * dx + dy * dy
      if(dist < nearestDist):
        nearestDist = dist
        nearest = name
    if(0):
      if(nearest != None):
        print "Found %s at %1.4f km" % (nearest, math.sqrt(nearestDist) / kmToDeg)
    return(nearest)
  
  def lookup(self, lat, lon):
    # Search small to large - i.e. if you're near a village,
    # return that rather than the nearby city.
    # Use limits, so we don't claim to be 'near' a town > 30km away
    for lookup in (('v',10.0), ('t', 30.0), ('c', 150.0)):
      (type, rangefilter) = lookup
      place = self.lookupPlace(lat,lon,type, rangefilter)
      if(place != None):
        return(place)
    return(None)
  
  def update(self):
    """If requested, lookup the nearest place name"""
    if(not self.get('lookup_place', False)):
      return
    pos = self.get('pos', None)
    if(pos != None):
      if(pos != self.lastpos):
        self.set('nearest_place', self.lookup(pos[0], pos[1]))
        self.lastpos = pos

  def handleMessage(self, message):
    pass
      
if(__name__ == "__main__"):
  a = placenames({},{})
  print a.lookup(51.3,-0.5)