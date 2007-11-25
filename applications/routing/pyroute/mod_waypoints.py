#!/usr/bin/python
#----------------------------------------------------------------
# Waypoint handler for pyroute
#
#------------------------------------------------------
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
#------------------------------------------------------
from xml.sax import make_parser, handler
from mod_base import dataSource, dataGroup, dataItem
import os

class waypointsModule(dataSource, handler.ContentHandler):
  def __init__(self, modules, filename = None):
    dataSource.__init__(self, modules)
    self.storeFields = ('name','sym')
    self.groups.append(dataGroup("Waypoints"))
    self.filename = None
    self.nextNumericWpt = 1
    if(filename):
      if(os.path.exists(filename)):
        self.load(filename)
        self.filename = filename

  def load(self, filename):
    self.inField = False
    parser = make_parser()
    parser.setContentHandler(self)
    parser.parse(filename)
  
  def startElement(self, name, attrs):
    if name == "wpt":
      self.parsedWaypoint = { \
        'lat': float(attrs.get('lat')),
        'lon': float(attrs.get('lon'))}
    if name in self.storeFields:
      self.fieldText = ''
      self.inField = True
      
  def endElement(self, name):
    if self.inField:
      if name in self.storeFields:
        self.parsedWaypoint[name] = self.fieldText
      self.inField = False
    if(name == "wpt"):
      self.storeWaypoint(self.parsedWaypoint)
        
  def characters(self, text):
    if self.inField:
      self.fieldText = self.fieldText + text
    
  def storeWaypoint(self, waypoint):
    x = dataItem(waypoint['lat'], waypoint['lon'])
    x.title = waypoint['name']
    self.groups[0].items.append(x)
    try:
      if(int(x.title) >= self.nextNumericWpt):
        self.nextNumericWpt = int(x.title) + 1
    except ValueError:
      pass

  def storeNumberedWaypoint(self, waypoint):
    waypoint['name'] = self.nextNumber()
    self.storeWaypoint(waypoint)

  def save(self):
    # Default filename if none was loaded
    if(self.filename == None):
      self.filename = "data/waypoints.gpx"
    self.saveAs(self.filename)
  
  def saveAs(self,filename):
    if(filename == None):
      return
    f = open(filename, "w")
    f.write('<?xml version="1.0"?>\n')
    f.write('<gpx version="1.0" creator="pyroute">\n')
    for w in self.groups[0].items:
      f.write('<wpt lat="%f" lon="%f">\n' % (w.lat, w.lon))
      f.write('  <name>%s</name>\n' % w.title)
      #f.write('  <cmt>%s</cmt>\n' % w.title)
      #f.write('  <desc>%s</desc>\n' % w.title)
      f.write('  <sym>%s</sym>\n' % 'flag')
      f.write('</wpt>\n')
    f.write('</gpx>\n')
    f.close()
  
  def nextNumber(self):
    return(str(self.nextNumericWpt))
  
if __name__ == "__main__":
  wpt = waypointsModule(None,"data/waypoints.gpx")
  wpt.report()
  wpt.saveAs("data/new.gpx")
