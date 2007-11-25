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
    def __init__(self, globals, modules):
        pyrouteModule.__init__(self,modules)
        self.options = {}
        self.globals = globals # TODO: remove
        
    def handleEvent(self,event):
        closeMenuAfterwards = False
        
        # Events prefixed with a + means "...and then close the menu"
        if(event[0] == '+'):
          event = event[1:]
          closeMenuAfterwards = True
          
        action,params = event.split(':',1)
        print "Handling event %s" % event

        if(action == 'menu'):
            self.set('menu', params)
        elif(action == 'mode'):
            self.set('mode', params)
            
        elif(action == 'option'):
            method, name = params.split(':',1)
            if(method == 'toggle'):
                self.set(name, not self.get(name))
            elif(method == 'add'):
                name,add = name.split(':')
                self.set(name, self.get(name,0) + float(add))
        elif(action == 'route'):
          ownpos = self.get('ownpos')
          
          if(not ownpos['valid']):
            print "Can't route, don't know start position"
          else:
            if(params == 'clicked'):
              lat,lon = self.get('clicked')
            else:
              lat, lon = [float(ll) for ll in params.split(':',1)]
              
            transport = self.get('mode')
            
            router = self.m['route']
            router.setStartLL(ownpos['lat'], ownpos['lon'], transport)
            router.setEndLL(lat,lon,transport)
            router.setMode('route')
            router.update()
            self.globals.forceRedraw()
        
        elif(action == 'ownpos'):
          lat,lon = self.get('clicked')
          self.set('ownpos', {'lat':lat,'lon':lon,'valid':True})
          print "Set ownpos to %f,%f" % (lat,lon)
          self.globals.handleUpdatedPosition()

        elif(action == 'direct'):
          start = self.get('ownpos')
          transport = self.get('mode')
          self.globals.modules['route'].setStartLL(start['lat'], start['lon'], transport)
          if(params == 'clicked'):
            lat,lon = self.get('clicked')
          else:
            lat, lon = [float(ll) for ll in params.split(':',1)]
          self.globals.modules['route'].setEndLL(lat,lon,transport)
          self.globals.modules['route'].setMode('direct')
          self.globals.modules['route'].update()
          self.globals.forceRedraw()

        elif(action == 'download'):
          centre = self.get('ownpos')
          if(not centre['valid']):
            print "Need to set your own position before downloading"
          else:
            sizeToDownload = 0.1
            self.globals.modules['osmdata'].download( \
              centre['lat'],
              centre['lon'],
              sizeToDownload)

        # Having handled the event, optionally return to the map
        if(closeMenuAfterwards):
          self.closeAllMenus()
    
    def closeAllMenus(self):
        self.set('menu',None)
        
    def getData(self,name,default=None):
        return(self.options.get(name,default))
    
    def setData(self,name,value):
        self.options[name] = value
