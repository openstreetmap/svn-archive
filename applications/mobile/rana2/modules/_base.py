#!/usr/bin/python
#----------------------------------------------------------------------
# Base-class for Rana modules
#----------------------------------------------------------------------
# Copyright 2008-2009, Oliver White
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
#----------------------------------------------------------------------
class RanaModule:
  def __init__(self,m,d,name):
    """Call this whenever a module is created
    m = module list
    d = data list
    name = module name"""
    self.m = m
    self.d = d
    self.name = name

  # ---------------------------------------------------
  # Overrideable functions
  # ---------------------------------------------------
  def startup(self):
    """On startup, after all other modules are loaded.  Use this to do any intialisation"""
    pass
  
  def shutdown(self): 
    """Before shutdown (don't rely on this to save important data!"""
    pass

  # ---------------------------------------------------
  # Utility functions
  # ---------------------------------------------------
  def log(self, message, priority=5):
    """Add message to logfile
    Priority:
    1: Show user and wait
    2: Show user temporarily
    3: Error, don't show
    4: Warning, don't show
    5: Debug
    """
    if(self.get('log')):
      print message
      
  def get(self,name,default=None):
    """Get an item of data
    Use this for all program state info"""
    return(self.d.get(name, default))
  
  def set(self,name,value):
    """Set an item of data.
    Use this for all program state info"""
    self.d[name] = value
  
  def registerPage(self, page):
    self.d['_pages'][page] = self.name

  def getFn(self, function):
    return(self.d['_functions'][function])

