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
  
  def handle_zoom(self,params):
    """zoom:[in|out|zoomlevel] - zooms to a quadtile level (17=max detail, <6=whole world)"""
    if(params == 'in'):
      self.m['projection'].setZoom(1, True)
    elif(params == 'out'):
      self.m['projection'].setZoom(-1, True)
    else:
      self.m['projection'].setZoom(int(params))
    self.set("needRedraw", True)

  def handle_menu(self,params):
    """menu:(name) - displays a menu. Use blank name to hide the menu"""
    self.set('menu', params)
  
  def handle_mode(self,params):
    """mode:(name) - sets the transport type in use (e.g. car, bike)"""
    self.set('mode', params)

  def handle_save(self,params):
    whichModule = params
    self.m[whichModule].save()
    
  def handle_option(self,params):
    """option:add|toggle:(name):(value)"""
    method, name = params.split(':',1)
    if(method == 'toggle'):
      self.set(name, not self.get(name))
    elif(method == 'add'):
      name,add = name.split(':')
      self.set(name, self.get(name,0) + float(add))
    elif(method == 'set'):
      name,value = name.split(':')
      self.set(name, value)
  
  def handle_set_colour(self,params):
    use,r,g,b = params.split(':')
    print "Setting %s to %s,%s,%s" % (use,r,g,b)
    self.set('%s_r' % (use), r)
    self.set('%s_g' % (use), g)
    self.set('%s_b' % (use), b)
    
  def handle_add_waypoint(self,params):
    if(params == 'clicked'):
      lat,lon = self.get('clicked')
      self.m['poi']['waypoints'].storeNumberedWaypoint({'lat':lat,'lon':lon})
      self.m['poi']['waypoints'].save()
    else:
      print "Not handled: dropping waypoint somewhere other than last click"
  
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
      self.m['osmdata'].download( \
        centre['lat'],
        centre['lon'],
        sizeToDownload)

  def handleEvent(self,event):
    closeMenuAfterwards = False
    
    # Events prefixed with a + means "...and then close the menu"
    if(event[0] == '+'):
      event = event[1:]
      closeMenuAfterwards = True
    
    # format is "eventname:parameters" (where parameters may be further split)
    action,params = event.split(':',1)
    
    # Look for an event-handler function, and call it
    print "Handling event %s" % event
    functionName = "handle_%s" % action
    try:
      function = getattr(self, functionName)
    except AttributeError:
      print "Error: %s not defined" % functionName
      self.set('menu',None)
    else:
      function(params)
    
    # Optionally return to the map
    if(closeMenuAfterwards):
      self.set('menu',None)
