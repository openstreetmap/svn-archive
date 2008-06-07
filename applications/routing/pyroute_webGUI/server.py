#!/usr/bin/python
#----------------------------------------------------------------------------
# 
#----------------------------------------------------------------------------
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
from BaseHTTPServer import *
import re
import sys
import math
import os
import pickle

from tile_route import TileRoute

sys.path.append("../pyroutelib2")
import routeAsCSV

class tileServer(BaseHTTPRequestHandler):
  def __init__(self, request, client_address, server):
    self.settings = None
    self.re_tile = re.compile('/tile/(\w+)/(\d+)/(\d+)/(\d+)\.png')
    self.re_route = re.compile('/route\?start=(.*?)%2C(.*?)&end=(.*?)%2C(.*?)&type=(.*?)')
    BaseHTTPRequestHandler.__init__(self, request, client_address, server)

  def getNextRoute(self):
    for id in range(1,10000):
      filename = "route/%d.txt" % id
      if(not os.path.exists(filename)):
        return(filename,id)

  def settingsFilename(self):
    return("settings.txt")
  
  def getSettings(self):
    """Load self.settings from disk, or return existing copy, or create blank copy"""
    if(not self.settings):
      try:
        self.settings = {}
        f = open(self.settingsFilename(), "r")
        self.settings = pickle.load(f)
        f.close()
      except IOError:
        print "Couldn't load settings from disk"
        
        
  def saveSettings(self):
    """save self.settings to disk"""
    f = open(self.settingsFilename(), "w")
    pickle.dump(self.settings, f)
    f.close()
    
  def log_message(self, format, *args):
    pass  # Kill logging to stderr
  
  def return_file(self, filename, contentType='text/HTML'):
    # Serve a real file from disk (for indexes etc.)
    f = open(filename, "r")
    self.send_response(200)
    self.send_header('Content-type',contentType) # TODO: more headers
    self.end_headers()
    self.wfile.write(f.read())
    f.close()
  def sendError(self,code,text):
    self.send_response(code)
    self.send_header('Content-type','text/HTML')
    self.end_headers()
    self.wfile.write("<html><title>Error %d</title></head><body><h1>%d: %s</h1></body></html>" % (code,code,text))

  def do_GET(self):
    # Filter-out unwanted addresses
    # (TODO: you should be able to run the server on your phone and connect
    # to it from your desktop, so we need some other authentication here)
    (host,port) = self.client_address
    if(not host in ("127.0.0.1")):
      print "Ignored connection from %s" % host
      self.sendError(403, "Access this from the machine running the server")
      return
    
    # Specific files to serve:
    if(self.path[0:5]=='/map/' or self.path=='/'):
      self.return_file('slippy.html')
      return

    # See if a tile was requested
    match_tile = self.re_tile.search(self.path)
    match_route = self.re_route.search(self.path)
    
    if self.path=='/counter':
      self.send_response(200)
      self.send_header('Content-type','text/plain')
      self.end_headers()
      
      self.getSettings()
      self.settings['test'] = self.settings.get('test',0) + 1
      self.wfile.write(self.settings['test'])
      self.saveSettings()
    
    elif(match_tile):
      (layer,z,x,y) = match_tile.groups()
      z = int(z)
      x = int(x)
      y = int(y)
      
      # Render the tile
      print 'Request for %d,%d at zoom %d, layer %s' % (x,y,z,layer)


      a = TileRoute("route/latest.csv")
      pngData = a.RenderTile(z,x,y,None)

      # Return the tile as a PNG
      self.send_response(200)
      self.send_header('Content-type','image/PNG')
      self.end_headers()
      self.wfile.write(pngData)
    
    elif(match_route):
      (x1,y1,x2,y2,type) = match_route.groups()
      (x1,y1,x2,y2) = map(float, (x1,y1,x2,y2))
      dx = x2 - x1
      dy = y2 - y1
      dist = math.sqrt(dx * dx + dy * dy)
      print "Routing from %f,%f to %f,%f = distance %f" % (x1,y1,x2,y2,dist)
      (filename,id) = self.getNextRoute()
      if(filename):
        routeAsCSV.routeToCSVFile(
          x1,y1,x2,y2,
          "cycle",
          filename)
        self.send_response(200)
        self.send_header('Content-type','text/HTML')
        self.end_headers()
        self.wfile.write("<p>Your route is available on the <a href='/map/?route=%d'>map</a></p>" % id)
      else:
        self.sendError(500, "Can't find a good name to call this route")
    else:
      print "404: %s" % self.path
      self.sendError(404, "Unrecognised URL")

try:
  server = HTTPServer(('',1280), tileServer)
  server.serve_forever()
except KeyboardInterrupt:
  server.socket.close()
  sys.exit()

