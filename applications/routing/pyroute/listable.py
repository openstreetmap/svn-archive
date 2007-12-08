#!/usr/bin/python
#-----------------------------------------------------------------------------
# Interface for things which can be listed
#
# Usage: 
#   (base-class for pyroute GUI libraries)
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

class listable:
  """Base-class (interface) for anything that can be listed in a menu"""
  def numItems(self):
    """Return how many items are available"""
    return(0)
  
  def getItemText(self,item):
    """Return the label for any item number"""
    return("-")
  
  def isLocation(self,item):
    """Return true if the item represents a location"""
    return(False)
  
  def getItemLatLon(self,item):
    """If the item represents a location, return it's position"""
    return(0,0)
  
  def getItemClickable(self,item):
    """Return true if the item should have a button to click on"""
    return(False)
    
  def getItemAction(self,item):
    """If the item isn't a location, return what happens if it's selected"""
    return("")
