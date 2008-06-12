#!/usr/bin/python
#----------------------------------------------------------------------------
# Map tile server
#
# Features:
#   * Serves slippy-map tile images
#   * Uses OsmRender module to render them
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

# Choose which rendering engine you want to use here
import renderer_default as RenderModule

class tileServer(BaseHTTPRequestHandler):
  def __init__(self, request, client_address, server):
    self.re = re.compile('/(\w+)/(\d+)/(\d+)/(\d+)\.png')
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
    # split query strings off and store separately
    if self.path.find('?') != -1: 
      (self.path, self.query_string) = self.path.split('?', 1)

    # Specific files to serve:
    if self.path=='/':
      self.return_file('slippy.html')
      return

    # See if a tile was requested
    match = self.re.search(self.path)
    if(match):
      (layer,z,x,y) = match.groups()
      z = int(z)
      x = int(x)
      y = int(y)
      
      # Render the tile
      print 'Request for %d,%d at zoom %d, layer %s' % (x,y,z,layer)
      renderer = RenderModule.RenderClass()
      pngData = renderer.RenderTile(z,x,y, None)
      
      if(pngData == None):
        print "Not found"
        self.send_response(404)
        return
      
      # Return the tile as a PNG
      self.send_response(200)
      self.send_header('Content-type','image/PNG')
      self.end_headers()
      self.wfile.write(pngData)
    else:
      self.send_response(404)

try:
  server = HTTPServer(('',1280), tileServer)
  server.serve_forever()
except KeyboardInterrupt:
  server.socket.close()
  sys.exit()

