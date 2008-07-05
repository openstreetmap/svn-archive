#!/usr/bin/python
#---------------------------------------------------------------------------
# Base class for any module which displays points of interest
#---------------------------------------------------------------------------
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

class poiModule(ranaModule):
  
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.poi = {}

  def drawList(self, cr, menuName, listHelper):
    if(menuName[0:4] == "poi:"):
      type = menuName[4:]
      items = [a[0] for a in self.poi.get(type, [])]
    else:
      items = self.poi.keys()
    
    
    
    for i in range(0, min(listHelper.numItems, len(items))):
      text = items[i]
      listHelper.write(i, "%d: %s"% (i,text))
      listHelper.makeClickable(i, "set:menu:poi:%s" % text)

    