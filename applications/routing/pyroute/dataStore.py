#!/usr/bin/python
#-----------------------------------------------------------------------------
# Global data table 
#
# Usage: 
#   (library code for pyroute GUI, not for direct use)
#
# Types of data stored:
#   * Options and settings
#   * Data (e.g. current position)
#   * Event-related data (e.g. position of last click)
#
# Not stored here:
#   * Routes (internal within the routing module)
#   * POIs and map data
#
# TODO:
#   * Event handling code needs to be moved into a module
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
from base import pyrouteModule

class DataStore(pyrouteModule):
    def __init__(self, modules):
        pyrouteModule.__init__(self,modules)
        self.options = {}
    
    def getData(self,name,default=None):
        return(self.options.get(name,default))
    
    def setData(self,name,value):
        self.options[name] = value
