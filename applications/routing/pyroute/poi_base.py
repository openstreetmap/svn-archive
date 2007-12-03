import geometry
from base import pyrouteModule

class poi:
  def __init__(self,lat,lon):
    self.lat = lat
    self.lon = lon
    self.title = 'Untitled at %1.3f, %1.3f' % (lat,lon)
  def formatText(self):
    return(self.title)
  def formatPos(self, ownPos = None):
    if(ownPos and ownPos['valid']):
      a = (ownPos['lat'], ownPos['lon'])
      b = (self.lat,self.lon)
      return("%1.2fkm at %03.1f" % \
        (geometry.distance(a,b),
        geometry.bearing(a,b)))
    else:
      return("%f,%f" % (self.lat,self.lon))

class poiGroup:
  def __init__(self,name):
    self.items = []
    self.name = name

class poiModule(pyrouteModule):
  def __init__(self, modules):
    pyrouteModule.__init__(self, modules)
    self.groups = []
    self.draw = True

  def report(self):
    for g in self.groups:
      print "=== %s ===" % g.name
      for i in g.items:
        print "%1.3f, %1.3f = %s" % (i.lat, i.lon, i.title)
