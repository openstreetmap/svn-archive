# GPX Party renderer
# Takes a directory structure full of GPX file tracklogs, 
# and renders them all to a single image
# 
# Copyright 2007, Oliver White
# Licensed as GNU GPL version3 or at your option any later version
# 
# Usage: python render.py
import cairo
import math
import sys
from xml.sax import saxutils
from UserDict import UserDict
from xml.sax import make_parser
from math import sqrt
import os
from os.path import join, getsize
deg2rad = 0.0174532925
M_PI = 3.1415926535

class TracklogInfo(saxutils.DefaultHandler):
  def __init__(self):
    self.count = 0
    self.points = {}
    self.currentFile = ''
  def walkDir(self, directory):
    for root, dirs, files in os.walk(directory):
      for file in files:
        if(file.endswith(".gpx")):
          self.currentFile = join(root, file)
          self.points[self.currentFile] = []
          parser = make_parser()
          parser.setContentHandler(self)
          parser.parse(self.currentFile)
          self.count = self.count + 1
    self.currentFile = ''
  def startElement(self, name, attrs):
    if(name == 'trkpt'):
      lat = float(attrs.get('lat', None))
      lon = float(attrs.get('lon', None))
      self.points[self.currentFile].append((lat,lon));
  def calculate(self, numSD):
    # Calculate mean and standard deviation
    self.calculateMean()
    self.calculateSD()
    # then use that to calculate extents
    self.N = self.lat + numSD * self.sdLat
    self.E = self.lon + numSD * self.sdLon
    self.S = self.lat - numSD * self.sdLat
    self.W = self.lon - numSD * self.sdLon
    self.dLat = self.N - self.S
    self.dLon = self.E - self.W
    self.ratio = self.dLon / self.dLat
    self.debug()

  def calculateMean(self):
    sumLat = 0
    sumLon = 0
    self.countPoints = 0
    for x in self.points.values():
      for y in x:
        self.countPoints = self.countPoints + 1
        sumLat = sumLat + y[0]
        sumLon = sumLon + y[1]
    self.lat = sumLat / self.countPoints
    self.lon = sumLon / self.countPoints
  def calculateSD(self):
    sumDLatSq = 0;
    sumDLonSq = 0;
    for x in self.points.values():
      for y in x:
        dLat = y[0] - self.lat
        dLon = y[1] - self.lon
        sumDLatSq = sumDLatSq + dLat * dLat
        sumDLonSq = sumDLonSq + dLon * dLon
    self.sdLat = sqrt(sumDLatSq)
    self.sdLon = sqrt(sumDLonSq)
  def debug(self):
    print "Pos: %f, %f +- %f, %f" % (self.lat,self.lon, self.sdLat,self.sdLon)
    print "Lat %f to %f, Long %f to %f" % (self.S,self.N,self.W,self.E)
    print "Ratio: %f" % self.ratio
    pass
  def createImage(self,width,height,surface):
    self.width = width
    self.height = height
    self.surface = surface
    self.drawBorder()
  def drawBorder(self):
    border=5
    ctx = cairo.Context(surface)
    ctx.set_source_rgb(0,0,0)
    ctx.rectangle(border,border,self.width-2*border, self.height-2*border)
    ctx.stroke()
  def valid(self):
    return(1)
  def drawTracklogs(self):
    for a in self.points.values():
      print "*"
      for b in a:
        ctx = cairo.Context(surface)
        x = self.xpos(b[1])
        y = self.ypos(b[0])
        ctx.arc(x, y, 1.0, 0, 2*M_PI);
        ctx.fill();
    pass
  def xpos(self,lon):
    return(self.width * (lon - self.W) / self.dLon)
  def ypos(self,lat):
    return(self.height * (lat - self.S) / self.dLat)
      
      
Thingy = TracklogInfo()
directory = "test/"
print "Loading data"
Thingy.walkDir(directory)
print "Calculating extents"
Thingy.calculate(1.0)
if(not Thingy.valid):
  print "Couldn't calculate extents"
  sys.exit()
width = 1000
height = int(width / Thingy.ratio)
print "Creating image %d x %d" % (width,height)

surface = cairo.ImageSurface(cairo.FORMAT_ARGB32, width, height)
Thingy.createImage(width, height, surface)
Thingy.drawTracklogs()

surface.write_to_png("output.png")

