#!/usr/bin/python
#---------------------------------------------------------------------------
# Calculate speed, time, etc. from position
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
import geo
from time import *

def getModule(m,d):
  return(stats(m,d))

class stats(ranaModule):
  """Handles messages"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.lastpos = [0,0]
    self.lastT = None
  
  def update(self):
    # Run scheduledUpdate every second
    t = time()
    if(self.lastT == None):
      self.scheduledUpdate(t, 0, True)
      self.lastT = t
    else:
      dt = t - self.lastT
      if(dt > 1):
        self.scheduledUpdate(t, dt)
        self.lastT = t
  
  def scheduledUpdate(self, t, dt, firstTime=False):
    """Called every dt seconds"""
    pos = self.get('pos', None)
    if(pos == None):
      return # TODO: zero any stats

    if(not firstTime):
      distance = geo.distance(
        self.lastpos[0],
        self.lastpos[1],
        pos[0],
        pos[1]) * 1000.0 # metres

      speed = distance / dt # km/sec
      speedMh = speed * 2.23693629  # miles/hour
      self.set('speed', speedMh)
    self.lastpos = pos

if(__name__ == '__main__'):
  d = {'pos':[51, -1]}
  a = stats({},d)
  a.update()
  d['pos'] = [52, 0]
  a.update()
  print d.get('speed')
  