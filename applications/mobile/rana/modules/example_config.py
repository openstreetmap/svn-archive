#!/usr/bin/python
#----------------------------------------------------------------------------
# User configuration
#----------------------------------------------------------------------------
# Copyright 2007-2008, Oliver White
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

def getModule(m,d):
  return(config(m,d))

class config(ranaModule):
  """Handle configuration, options, and setup"""
  def __init__(self, m, d):
    ranaModule.__init__(self, m, d)

  def firstTime(self):
    # Option: load a GPX replay
    # m = self.m.get('replayGpx', None)
    # if(m != None):
    #   m.load('C:/home/OSM/Waddington/2007_05_07_bedfordshire_footpaths.gpx')
      
    # Option: set your start position
    # self.set("pos", (59.9441, 10.9403))
    # self.set("pos_source", "default")

    # Option: set the initial view
    # self.set("centreOn", "51.3,-0.5")
    
    # Option: set the map tiles
    # self.set('layer','pyrender') # osma, mapnik, etc - see mod_mapTiles for list

    # Option: whether to centre on your position
    # self.set('centred', False)

    pass