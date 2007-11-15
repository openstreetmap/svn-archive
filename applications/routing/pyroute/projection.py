
class Projection:
  def __init__(self):
    self.xyValid = 0
    self.llValid = 0
  def isValid(self):
    return(self.xyValid and self.llValid)
  def setView(self,x,y,w,h):
    self.w = w / 2
    self.h = h / 2
    self.xc = x + self.w
    self.yc = y + self.h
    self.xyValid = 1
  def recentre(self,lat,lon,scale):
    self.lat = lat
    self.lon = lon
    self.scale = scale  # TODO: scale and scaleCosLat
    self.findEdges()
    self.llValid = 1
  def findEdges(self):
    """(S,W,E,N) are derived from (lat,lon,scale)"""
    self.S = self.lat - self.scale
    self.N = self.lat + self.scale
    self.W = self.lon - self.scale
    self.E = self.lon + self.scale
  def nudge(self,dx,dy,scale):
    self.lat = self.lat + dy * scale * self.scale
    self.lon = self.lon + dx * scale * self.scale
    self.findEdges()
  def nudgeZoom(self,amount):
    self.scale = self.scale * (1 + amount)
    self.findEdges()    
  def ll2xy(self,lat,lon):
    px = (lon - self.lon) / self.scale
    py = (lat - self.lat) / self.scale
    x = self.xc + self.w * px
    y = self.yc - self.h * py
    return(x,y)
  def xy2ll(self,x,y):
    px = (x - self.xc) / self.w
    py = (y - self.yc) / self.h
    lon = self.lon + px * self.scale
    lat = self.lat - py * self.scale
    return(lat,lon)
  def onscreen(self,x,y):
    return(x >= 0 and x < 2 * self.w and y >= 0 and y < 2 * self.h)
  def relXY(self,x,y):
    return(x/(2*self.w), y/(2*self.h))
    
