#!/usr/bin/python
#----------------------------------------------------------------------
# Supplies position info from the openmoko DBUS GPS interface
#----------------------------------------------------------------------
# Copyright 2007-2009, Oliver White
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
from _base import *
import gtk
import dbus
import time
import gobject
import math

def update(obj):
  obj.update()
  return(True)

class pos_gypsy(RanaModule):
  """Supplies position info from Gypsy (dbus GPS interface)"""
  def __init__(self, m, d, name):
    RanaModule.__init__(self,m,d,name)
    self.set("pos_valid", False)
    self.lastPos = (False,0,0,0,0)
    self.update_count = 0

  def startup(self):
    self.enableGps()
    self.dbus = dbus.SystemBus().get_object('org.freesmartphone.ogpsd', '/org/freedesktop/Gypsy')
    self.timer = gobject.timeout_add(1000, update, self)

  def enableGps(self):
    obj = dbus.SystemBus().get_object('org.freesmartphone.ousaged', '/org/freesmartphone/Usage')
    dbus.Interface(obj, 'org.freesmartphone.Usage').RequestResource('GPS')
  
  def isConnected(self):
    return(self.dbus.GetConnectionStatus(dbus_interface='org.freedesktop.Gypsy.Device'))

  def getPos(self):
    if(not self.isConnected()):
      return(False, "Not connected to gps",0,0,0)

    fixstatus = self.dbus.GetFixStatus(dbus_interface='org.freedesktop.Gypsy.Device')
    if (fixstatus == 1):
      return(False, "No fix",0,0,0)

    (fields, tstamp, lat, lon, alt) = self.dbus.GetPosition(dbus_interface='org.freedesktop.Gypsy.Position')
    return(True, "OK", float(lat),float(lon),float(alt))
  
  def update(self):
    (status, text, lat,lon,alt) = self.getPos()
    if(status):
      t = time.time()
      self.set("pos", (lat,lon,alt))
      self.set("pos_t", t)
      self.set("pos_valid", True)
      self.update_meta(lat,lon,alt,t)
    else:
      self.set("pos_valid", False)
      
    if(self.update_count % 3 == 0):
      self.update_satellite_info()
    self.update_count += 1
  
  def update_meta(self,lat2,lon2,alt2,t2):
    (valid,lat1,lon1,alt1,t1) = self.lastPos
    if(valid):
      dlat = lat2-lat1
      dlon = lon2-lon1
      dlon2 = dlon * math.cos(math.radians(lat2))
      dt = t2 - t1
      
      if(dt < 0.3 or dt > 5.0):
        self.set("heading", None)
        self.set("speed", None)
        
      else:
        angle = math.degrees(math.atan2(dlon2,dlat))
        self.set("heading", angle)
        
        distance = 40075.0 * 1000.0 * math.sqrt(dlon2*dlon2 +dlat*dlat) / 360.0 # metres
        speed = distance / dt # ms-1
        self.set("speed", speed)
        self.set("speed_mph", speed * 2.23693629)

    self.lastPos = (True, lat2,lon2,alt2,t2)
    
  def update_satellite_info(self):
    satellites = self.dbus.GetSatellites(dbus_interface='org.freedesktop.Gypsy.Satellite')
    count = 0
    for (satIndex, sat) in enumerate(satellites):
      self.set("sat_%d" % count, sat) # id, used, el, az, snr
      count += 1
    self.set("sat_count", count)
    
    (valid, pdop, hdop, vdop) = self.getQuality()
    if(valid):
      self.set("sat_quality", (pdop, hdop, vdop))
    
  def getQuality(self):
    if(not self.isConnected()):
      return((False, 0,0,0,0,0))
    (fields, pdop, hdop, vdop) = self.dbus.GetAccuracy(dbus_interface='org.freedesktop.Gypsy.Accuracy')

    return((True, pdop, hdop, vdop))
  
  def getSats(self):
    satellites = self.dbus.GetSatellites(dbus_interface='org.freedesktop.Gypsy.Satellite')
    text = ''
    for (satIndex, sat) in enumerate(satellites):
      text += '%d%s ' % (sat[0], sat[1] and '*' or '')
    return(text)

if(__name__ == "__main__"):
  a = pos_gypsy({},{},"")
  a.startup()
  print a.getPos()
  print a.getQuality()
  print a.getSats()
  