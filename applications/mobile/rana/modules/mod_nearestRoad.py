#!/usr/bin/python
#---------------------------------------------------------------------------
# Lookup the nearest road
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

# OK, we need to somehow import modules from pyrender directory here.
# replace this with location of your pyrender modules
import sys
sys.path.append('../pyrender')
import whatAmIOn

def getModule(m,d):
  return(nearestRoad(m,d))

class nearestRoad(ranaModule):
  """Handles messages"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.lastpos = [0,0]
  
  def update(self):
    """If requested, lookup the nearest road name"""
    if(not self.get('lookup_road', False)):
      return
    pos = self.get('pos', None)
    if(pos != None):
      if(pos != self.lastpos):
        self.set('nearest_road', whatAmIOn.describe(pos[0], pos[1]))
        self.lastpos = pos

if(__name__ == '__main__'):
  d = {'lookup_road':True, 'pos':[51.678935, -0.826256]}
  a = nearestRoad({},d)
  a.update()
  print d.get('nearest_road')
  