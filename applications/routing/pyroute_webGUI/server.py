#!/usr/bin/python
#----------------------------------------------------------------------------
# DEVELOPMENT CODE, DO NOT USE
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
sys.path.append("../pyroutelib2")
import routeAsGpx

class tileServer(BaseHTTPRequestHandler):
  def __init__(self, request, client_address, server):
    self.re_tile = re.compile('/(\w+)/(\d+)/(\d+)/(\d+)\.png')
    self.re_route = re.compile('/route\?start=(.*?)%2C(.*?)&end=(.*?)%2C(.*?)&type=(.*?)')
    BaseHTTPRequestHandler.__init__(self, request, client_address, server)

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

  def do_GET(self):
    # Specific files to serve:
    if self.path=='/':
      self.return_file('slippy.html')
      return

    # See if a tile was requested
    match_tile = self.re_tile.search(self.path)
    match_route = self.re_route.search(self.path)
    
    if(match_tile):
      (layer,z,x,y) = match_tile.groups()
      z = int(z)
      x = int(x)
      y = int(y)
      
      # Render the tile
      print 'Request for %d,%d at zoom %d, layer %s' % (x,y,z,layer)
      self.send_response(404)
      return
      
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
      gpx = routeAsGpx.routeToGpx(
        x1,y1,x2,y2,
        "cycle",
        "Route",
        "track")
      self.send_response(200)
      self.send_header('Content-type','text/plain')
      self.end_headers()
      self.wfile.write(gpx)
    else:
      print "404: %s" % self.path
      self.send_response(404)

try:
  server = HTTPServer(('',1280), tileServer)
  server.serve_forever()
except KeyboardInterrupt:
  server.socket.close()
  sys.exit()

