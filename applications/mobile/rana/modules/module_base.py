#!/usr/bin/python
#----------------------------------------------------------------------------
# Base class for Rana modules
#----------------------------------------------------------------------------
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
#---------------------------------------------------------------------------
class ranaModule:
  def __init__(self, modules={}, data={}):
    self.m = modules
    self.d = data
    self.status = ''
    
  def module_exists(self, module):
    """Test whether a named module is loaded"""
    return(self.m.get(module, None) != None)
  
  def get(self, name, default=None):
    """Get an item of data"""
    return(self.d.get(name, default))
  
  def set(self, name, value):
    """Set an item of data"""
    if(self.module_exists('watchlist')):
      self.m['watchlist'].notify(name, value)
    self.d[name] = value
    
  # Overridable
  def getStatus(self):
    return(self.status)
  def update(self):
    pass
