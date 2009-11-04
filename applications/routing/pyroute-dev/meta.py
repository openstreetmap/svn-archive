#!/usr/bin/python
#-----------------------------------------------------------------------------# Meta-information (self-testing, self-diagnosis, etc.) for pyroute
#
# Usage: 
#   Library for pyroute
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
from base import pyrouteModule
from listable import *

class moduleInfo(pyrouteModule, listable):
  def __init__(self, modules):
    pyrouteModule.__init__(self, modules)
    
  def numItems(self):
    return(len(self.m))
  def getItemText(self,n):
    return("Module: \"%s\"" % (self.m.keys()[n]))
  def getItemStatus(self,n):
    module = self.m.values()[n]
    if("getStatus" in dir(module)):
      return module.getStatus();
    return("")
  def getItemClickable(self,n):
    return(True)
  def getItemAction(self,n):
    return("")
