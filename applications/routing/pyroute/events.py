#!/usr/bin/python
#-----------------------------------------------------------------------------# Event handler.  Messages (as text) get sent here, telling pyroute
# to do various things (e.g. save a waypoint, open a menu, route to
# some point)
#
# Usage: 
#   (library code)
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

class pyrouteEvents(pyrouteModule):
  def __init__(self, modules):
      pyrouteModule.__init__(self,modules)
  
  def handle_menu(self,params):
    self.set('menu', params)
  
  def handle_mode(self,params):
    self.set('mode', params)
  
  def handle_option(self,params):
    method, name = params.split(':',1)
    if(method == 'toggle'):
      self.set(name, not self.get(name))
    elif(method == 'add'):
      name,add = name.split(':')
      self.set(name, self.get(name,0) + float(add))
  
  def handle_route(self,params):
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
      self.set('needRedraw', True)

  def handle_ownpos(self,params):
    lat,lon = self.get('clicked')
    self.set('ownpos', {'lat':lat,'lon':lon,'valid':True})
    print "Set ownpos to %f,%f" % (lat,lon)
    self.set('positionUpdated', True)

  def handle_direct(self,params):
    start = self.get('ownpos')
    transport = self.get('mode')
    self.m['route'].setStartLL(start['lat'], start['lon'], transport)
    if(params == 'clicked'):
      lat,lon = self.get('clicked')
    else:
      lat, lon = [float(ll) for ll in params.split(':',1)]
    self.m['route'].setEndLL(lat,lon,transport)
    self.m['route'].setMode('direct')
    self.m['route'].update()
    self.set('needRedraw', True)

  def handle_download(self,params):
    centre = self.get('ownpos')
    if(not centre['valid']):
      print "Need to set your own position before downloading"
    else:
      sizeToDownload = 0.1
      self.globals.modules['osmdata'].download( \
        centre['lat'],
        centre['lon'],
        sizeToDownload)

  def handleEvent(self,event):
    closeMenuAfterwards = False
    
    # Events prefixed with a + means "...and then close the menu"
    if(event[0] == '+'):
      event = event[1:]
      closeMenuAfterwards = True
    
    action,params = event.split(':',1)
    
    print "Handling event %s" % event
    functionName = "handle_%s" % action
    
    try:
      function = getattr(self, functionName)
    except AttributeError:
      print "Error: %s not defined" % functionName
      self.set('menu',None)
    else:
      function(params)

    print " - done handling %s" % event
    
    # Having handled the event, optionally return to the map
    if(closeMenuAfterwards):
      self.closeAllMenus()
  
  def closeAllMenus(self):
    self.set('menu',None)
