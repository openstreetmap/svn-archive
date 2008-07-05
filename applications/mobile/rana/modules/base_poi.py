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
import math

class poiModule(ranaModule):
  
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)
    self.poi = {}
    self.needUpdate = False

  def addItem(self, type, name, lat, lon):
    item = {'name':name, 'lat':float(lat),'lon':float(lon)}
    self.poi[type].append(item)

  def updatePoi(self):
    """Update distances etc. to each POI"""
    if(not self.needUpdate):
      return
    
    pos = self.get("pos", None)
    if(pos == None):
      return(False)

    (ownlat, ownlon) = pos
    degToKm = 40041.0 / 360.0
    for type,items in self.poi.items():
      for item in items:
        dx = item['lon'] - ownlon
        dy = item['lat'] - ownlat
        # TODO: cos(lat)
        item['d'] = math.sqrt(dx * dx + dy * dy) * degToKm
        
    self.needUpdate = False
    
  def drawList(self, cr, menuName, listHelper):
    
    if(menuName[0:4] == "poi:"):
      type = menuName[4:]
      
      items = self.poi.get(type, [])
      items.sort(lambda x, y: int(x.get('d',1E+5)) - int(y.get('d',1E+5)))
      items = [a['name'] for a in items]
      submenu = False
    else:
      items = self.poi.keys()
      submenu = True
    
    for i in range(0, min(listHelper.numItems, len(items))):
      text = items[i]
      listHelper.write(i, "%d: %s"% (i,text))
      if(submenu):
        listHelper.makeClickable(i, "set:menu:poi:%s" % text)
      else:
        listHelper.makeClickable(i, "set:selected_type:poi|set:selected_name:%s|set:menu:poi" % text)
        

    