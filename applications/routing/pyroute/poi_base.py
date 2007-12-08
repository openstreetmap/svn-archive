import geometry
from base import pyrouteModule
import listable

class poi:
  def __init__(self,lat,lon):
    self.lat = lat
    self.lon = lon
    self.title = 'Untitled at %1.3f, %1.3f' % (lat,lon)
  def formatText(self):
    return(self.title)
  def distanceFrom(self,pos):
    a = (pos['lat'], pos['lon'])
    b = (self.lat,self.lon)
    self.d = geometry.distance(a,b)
    return(self.d)
    
  def __cmp__(self, other):
    return cmp(self.d, other.d)

class poiGroup(listable.listable):
  def __init__(self,name):
    self.items = []
    self.name = name
    self.sortpos = {'valid':False}
    
  # Functions to implement the listable interface
  def numItems(self):
    return(len(self.items))
  def getItemText(self,n):
    return(self.items[n].title)
  def isLocation(self,n):
    return(True)
  def getItemLatLon(self,n):
    item = self.items[n]
    return(item.lat,item.lon)

  def sort(self,pos):
    
    if(pos == self.sortpos):
      return
    self.sortpos = pos
    print "Sorting"

    if(pos['valid']):
      for i in self.items:
        i.d = i.distanceFrom(pos)

    self.items.sort()
    if(0):
      for i in self.items:
        print "%1.3f" % i.d

    
class poiModule(pyrouteModule):
  def __init__(self, modules):
    pyrouteModule.__init__(self, modules)
    self.groups = []
    self.draw = True

  def sort(self, pos):
    for g in self.groups:
      g.sort(pos)
      
  def report(self):
    for g in self.groups:
      print "=== %s ===" % g.name
      for i in g.items:
        print "%1.3f, %1.3f = %s" % (i.lat, i.lon, i.title)
