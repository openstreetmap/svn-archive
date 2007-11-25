#!/usr/bin/python
#----------------------------------------------------------------
# Waypoint handler for pyroute
#
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
from waypoints import Waypoints
from mod_base import dataSource, dataGroup, dataItem
import os

class waypointsModule(dataSource, Waypoints):
  def __init__(self, modules, filename = None):
    dataSource.__init__(self, modules)
    Waypoints.__init__(self)
    self.groups.append(dataGroup("Waypoints"))
    if(filename):
      if(os.path.exists(filename)):
        self.load(filename)

  def storeWaypoint(self, waypoint):
    #print "Module!!!"
    #self.waypoints.append(waypoint)
    x = dataItem(waypoint['lat'], waypoint['lon'])
    x.title = waypoint['name']
    self.groups[0].items.append(x)


if __name__ == "__main__":
  wpt = waypointsModule(None,"data/waypoints.gpx")
  wpt.report()
