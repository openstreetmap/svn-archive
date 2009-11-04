from loadOsm import LoadOsm
from base import pyrouteModule
import os
import urllib

class osmData(pyrouteModule):
  def __init__(self, modules):
    pyrouteModule.__init__(self,modules)
    self.data = LoadOsm(None)
  
  def defaultFilename(self):
    return("data/routing.osm")
  
  def loadDefault(self):
    self.load(self.defaultFilename())
    
  def load(self,filename):
    print "Loading OSM data from %s"%filename
    self.data.loadOsm(filename)
  
  def download(self,params):
    ownpos = self.get('ownpos')
    if(not ownpos['valid']):
      print "Can only download around own position"
      return

    lat = ownpos['lat']
    lon = ownpos['lon']
    size = 0.2
    
    url = "http://informationfreeway.org/api/0.5/way[bbox=%1.4f,%1.4f,%1.4f,%1.4f][highway|railway|waterway=*]" % (lon-size,lat-size,lon+size,lat+size)
    
    print "downloading %s" % url
    
    filename = self.defaultFilename()
    
    if(os.path.exists(filename)):
      print "Removing existing file"
      os.remove(filename)
      
    urllib.urlretrieve(url, filename)
    
    print "Finished downloading"
    self.load(filename)
