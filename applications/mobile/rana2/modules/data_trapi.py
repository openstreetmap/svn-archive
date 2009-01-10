#!/usr/bin/python
#----------------------------------------------------------------------
# Get OpenStreetMap data from Tiled Read-only API (TRAPI)
#----------------------------------------------------------------------
# Copyright 2008-2009, Oliver White
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
from _menu import *
from _tilenames import *
from _osm_file import *
import urllib
import gtk
import os
import sys
import gzip

TRAPI_Z_LEVEL = 14

class data_trapi(RanaModule):
  """Displays the title screen"""
  def __init__(self, m, d, name):
    RanaModule.__init__(self,m,d,name)
  
  def bbox_url(self, S,W,N,E):
    server = "http://api1.osm.absolight.net:8080"
    server = "http://91.189.201.75"
    #server = "http://80.167.103.102"
    server = "http://host1.openhost.dk"
    return("%s/api/0.5/map?bbox=%f,%f,%f,%f" % 
      (server, W,S,E,N))

  def progress_meter(self, count, block, total):
    print "%1.0f%%" % (100.0 * count * block / total)

  def download_tile(self, x,y, z, filename=None, overwrite=False):
    if(z != TRAPI_Z_LEVEL):
      return
    
    if(filename == None):
      filename = "tile_%d_%d_%d.osm" % (z,x,y)
    
    (S,W,N,E) = tileEdges(x,y,z)
    
    URL = self.bbox_url(S,W,N,E)

    if(os.path.exists(filename) and not overwrite):
      pass
    elif(os.path.exists(filename+".gz") and not overwrite):
      pass
    else:
      print "Downloading " + URL
      try:
        data = urllib.urlretrieve(URL, filename, self.progress_meter)
      except IOError:
        print "Download failed"
        return
      self.compress_file(filename)
    
  def compress_file(self, filename):
    zipFilename = filename + ".gz"
    f_in = open(filename, 'rb')
    f_out = gzip.open(zipFilename, 'wb')
    f_out.writelines(f_in)
    f_out.close()
    f_in.close()
    os.remove(filename)
  
  def download_area(self, S,N,W,E, directory):
    (x1,y1)= tileXY(N,W,TRAPI_Z_LEVEL)
    (x2,y2)= tileXY(S,E,TRAPI_Z_LEVEL)
    num_tiles = ((x2-x1) * (y2-y1))
    
    for x in range(x1,x2):
      for y in range(y1,y2):
        filename = os.path.join(directory, "tile_%d_%d_%d.osm" % (TRAPI_Z_LEVEL,x,y))
        download_tile(x,y,TRAPI_Z_LEVEL, filename, False)

if(__name__ == "__main__" and len(sys.argv) > 1):
  
  if(sys.argv[1] == "trapi"):
    a = data_trapi({},{},'')
    print a.download_tile(8173,5457,14, "test.osm", True)
    print download_area(50.9,52.2,-0.63,0.01, sys.argv[1])
  
  if(sys.argv[1] == "osm"):
    print "Loading test.osm"
    a = osm_file()
    a.load_osm("test.osm")
    #print a.data['poi']
    #print a.tags_seen.keys()
  
  if(sys.argv[1] == "osm_to_dat"):
    print "Converting test.osm to test.dat.gz"
    a = osm_file()
    a.load_osm("test.osm")
    a.save("test.dat.gz")
  
  if(sys.argv[1] == "gzip"):
    print "Loading test.dat.gz"
    a = osm_file()
    a.load("test.dat.gz")
    #print a.data["poi"]
