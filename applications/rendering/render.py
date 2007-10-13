# OJW 2007  GPLv3/later
import cairo
import math
import sys
from xml.sax import saxutils
from UserDict import UserDict
from xml.sax import make_parser
import os
from os.path import join, getsize
deg2rad = 0.0174532925
M_PI = 3.1415926535

class MapObject:
  def __init__(self,width,height,surface, Outline):
    self.width = width
    self.height = height
    self.surface = surface
    self.outline = Outline
    self.ctx = cairo.Context(surface)
    self.ctx.set_font_size(30)
    self.ctx.set_source_rgb(0,0,0)

  def drawBorder(self):
    border=5
    self.ctx.rectangle(border,border,self.width-2*border, self.height-2*border)
    self.ctx.stroke()
  
  def point(self,x,y):
    self.ctx.arc(x, y, 5.0, 0, 2*M_PI);
    self.ctx.fill();
  def pointLL(self,lat,lon):
    x = self.outline.xpos(lon) * self.width
    y = self.outline.ypos(lat) * self.height
    #print "%f,%f -> %f,%f" % (lat,lon,x,y)
    self.ctx.arc(x, y, 1.0, 0, 2*M_PI);
    self.ctx.fill();
    
  def finish(self):
    self.ctx.stroke()

class TracklogInfo(MapObject, saxutils.DefaultHandler):
  def __init__(self, directory):
    self.W = 180;
    self.E = -180;
    self.N = -90;
    self.S = 90;
    self.count = 0
    for root, dirs, files in os.walk(directory):
      for file in files:
        if(file.endswith(".gpx")):
          filename = join(root, file)
          parser = make_parser()
          parser.setContentHandler(self)
          parser.parse(filename)
          self.count = self.count + 1
    print "Lat %f to %f, Long %f to %f" % (self.S,self.N,self.W,self.E)
    self.dLat = self.N - self.S
    self.dLon = self.E - self.W
    self.ratio = self.dLon / self.dLat

  def startElement(self, name, attrs):
    if(name == 'trkpt'):
      lat = float(attrs.get('lat', None))
      lon = float(attrs.get('lon', None))
      self.S = min(lat, self.S)
      self.N = max(lat, self.N)
      self.W = min(lon, self.W)
      self.E = max(lon, self.E)
  def valid(self):
    if(self.W >= self.E):
      return(0)
    if(self.S >= self.N):
      return(0)
    return(1)

  def xpos(self,lon):
    return((lon - self.W) / self.dLon)
  def ypos(self,lat):
    return((lat - self.S) / self.dLat)
      
      
class Tracklog(MapObject, saxutils.DefaultHandler):
  def draw(self, file):
    parser = make_parser()
    parser.setContentHandler(self)
    parser.parse(file)
    
  def startElement(self, name, attrs):
    if(name == 'trkpt'):
      lat = attrs.get('lat', None)
      lon = attrs.get('lon', None)
      self.pointLL(float(lat),float(lon))

class TracklogDir(Tracklog):
  def drawDir(self, directory):
    count = 0
    for root, dirs, files in os.walk(directory):
      for file in files:
        if(file.endswith(".gpx")):
          filename = join(root, file)
          print " %d/%d:%s" % (count, self.outline.count, filename)
          self.draw(filename)
          count = count + 1

# Find out the extents of the data
directory = "eg/"
print "Calculating extents"
Outline = TracklogInfo(directory)
if(not Outline.valid):
  print "Couldn't calculate extents"
  sys.exit()
  
width = 10000
height = int(width / Outline.ratio)
print "Creating image %d x %d" % (width,height)

surface = cairo.ImageSurface(cairo.FORMAT_ARGB32, width, height)
Tracklog = TracklogDir(width, height, surface, Outline)
Tracklog.drawDir(directory)
Tracklog.drawBorder()

surface.write_to_png("output.png")

