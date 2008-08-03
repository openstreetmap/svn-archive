#!/usr/bin/python
#----------------------------------------------------------------------------
# Log position as GPX
#----------------------------------------------------------------------------
# Copyright 2008, Oliver White
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
  return(tracklog(m,d))

class tracklog(ranaModule):
  """Record and display tracklogs"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.nodes = []
    self.updateTime = 0

  def load(self, filename):
    # TODO: share this with replayGpx
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
      self.set("centreOnce", True)
    else:
      print "No file"

  def scheduledUpdate(self):
    pos = self.get('pos', None)
    if(pos != None):
      self.nodes.append(pos)
      #(lat,lon) = pos
      #print "Logging %f, %f" % (lat,lon)
    

  def drawMapOverlay(self, cr):
    # Where is the map?
    proj = self.m.get('projection', None)
    if(proj == None):
      return
    if(not proj.isValid()):
      return

    # Draw all trackpoints as lines (TODO: optimisation)
    cr.set_source_rgb(0,0,0.5)
    count = 0
    for n in self.nodes:
      (lat,lon) = n
      (x,y) = proj.ll2xy(lat,lon)
      if(count == 0):
        cr.move_to(x,y)
      else:
        cr.line_to(x,y)
      count += 1
    cr.stroke()
  
  def update(self):
    # Run scheduledUpdate every second
    t = time()
    dt = t - self.updateTime
    if(dt > self.get("logPeriod", 2)):
      self.updateTime = t
      self.scheduledUpdate()


