#!/usr/bin/python
#-----------------------------------------------------------------------------
# Projection code (lat/long to screen conversions)
#
# Usage: 
#   * Derive all pyroute modules fom this class
#   * Put "pyrouteModule.__init__(self,modules)" in your init function
#   * Instantiate using modules.append(yourmodule(modules))
#      where "modules" is the list of all pyroute modules
#
# Features:
#   * Get and set data from the global table
#   * Send messages or trigger events
#   * Access other modules (using "self.m['modulename']")
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
#----------------------------------------------------------------------------
class pyrouteModule:
  def __init__(self, modules):
    self.m  = modules
  def get(self, name, default=None):
    return(self.m['data'].getData(name, default))
  def set(self, name, value):
    return(self.m['data'].setData(name, value))
  def action(self, message):
    self.m['events'].handleEvent(message)
  def ownPos(self):
    return(self.m['position'].get())
  # Meta-info
  def getStatus(self):
    return("")