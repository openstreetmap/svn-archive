#!/usr/bin/python
#----------------------------------------------------------------------------
# Replay a GPX file as position data
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
from base_module import ranaModule
import re
from time import *

def getModule(m,d):
  return(replayGpx(m,d))

class replayGpx(ranaModule):
  """Replay a GPX"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.nodes = []
    self.pos = 0
    self.numNodes = 0
    self.updateTime = 0
    self.replayPeriod = 1 # second

  def load(self, filename):
    self.nodes = []
    file = open(filename, 'r')
    if(file):
      regexp = re.compile("<trkpt lat=['\"](.*?)['\"] lon=['\"](.*?)['\"]")
      for text in file:
        matches = regexp.match(text)
        if(matches):
          lat = float(matches.group(1))
          lon = float(matches.group(2))
          self.nodes.append([lat,lon])
      file.close()
      self.numNodes = len(self.nodes)
    else:
      print "No file"

  def dump(self):
    print "%d nodes:"%len(self.nodes)
    for n in self.nodes:
      print "%1.4f, %1.4f" % (n[0], n[1])

  def scheduledUpdate(self):
    if(self.numNodes < 1):
      return
    self.pos += 1
    if(self.pos >= self.numNodes):
      self.pos = 0
    (lat,lon) = self.nodes[self.pos]
    self.set('pos', (lat,lon))
    self.set('pos_source', 'replay')
  
  def update(self):
    # Run scheduledUpdate every second
    t = time()
    dt = t - self.updateTime
    if(dt > self.replayPeriod):
      self.updateTime = t
      self.scheduledUpdate()

if(__name__ == "__main__"):
  d = {}
  a = replayGpx({}, d)
  a.load('../../Waypoints/WaddingtonNG1/backhome.gpx')

  while(1):
    a.update()
    (lat,lon) = d.get("pos")
    print "%f, %f" % (lat,lon)

