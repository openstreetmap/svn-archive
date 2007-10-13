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
import random 
from os.path import join, getsize
deg2rad = 0.0174532925
M_PI = 3.1415926535

def median(a,k):
  num = len(a)
  for i in range(1,num):
    minIndex = i
    minValue = a[i]
    for j in range(i+1, num):
      if(a[j] < minValue):
        minIndex = j
        minValue = a[j]
    # swap a[i] and a[minIndex]
    temp = a[i]
    a[i] = a[minIndex]
    a[minIndex] = temp
  return a[k]

if(0):
  a=[]
  for i in range(5000):
    a.append(random.randint(1,10000))
  print median(a, 400)
  sys.exit();

class TracklogInfo(saxutils.DefaultHandler):
  def __init__(self):
    self.count = 0
    self.countPoints = 0
    self.points = {}
    self.currentFile = ''
  def walkDir(self, directory):
    for root, dirs, files in os.walk(directory):
      for file in files:
        if(file.endswith(".gpx")):
          print file
          self.currentFile = join(root, file)
          self.points[self.currentFile] = []
          parser = make_parser()
          parser.setContentHandler(self)
          parser.parse(self.currentFile)
          self.count = self.count + 1
    self.currentFile = ''
    print "Read %d points in %d files" % (self.countPoints,self.count)
  def startElement(self, name, attrs):
    if(name == 'trkpt'):
      lat = float(attrs.get('lat', None))
      lon = float(attrs.get('lon', None))
      self.points[self.currentFile].append((lat,lon));
      self.countPoints = self.countPoints + 1
  def valid(self):
    if(self.ratio == 0.0):
      return(0)
    if(self.dLat <= 0.0 or self.dLon <= 0):
      return(0)
    return(1)
  def calculate(self, amount):
    # Calculate mean and standard deviation
    self.calculateMean()
    self.calculateSD(amount)
    # then use that to calculate extents
    self.N = self.lat + self.sdLat
    self.E = self.lon + self.sdLon
    self.S = self.lat - self.sdLat
    self.W = self.lon - self.sdLon
    self.dLat = self.N - self.S
    self.dLon = self.E - self.W
    self.ratio = self.dLon / self.dLat
    self.debug()
    #sys.exit()

  def calculateMean(self):
    sumLat = 0
    sumLon = 0
    for x in self.points.values():
      for y in x:
        sumLat = sumLat + y[0]
        sumLon = sumLon + y[1]
    self.lat = sumLat / self.countPoints
    self.lon = sumLon / self.countPoints
    
  def calculateSD(self,amount):
    diffsLat = []
    diffsLon = []
    for x in self.points.values():
      for y in x:
        diffsLat.append(abs(y[0] - self.lat))
        diffsLon.append(abs(y[1] - self.lon))
    self.sdLat = median(diffsLat, int(self.countPoints * amount))
    self.sdLon = median(diffsLon, int(self.countPoints * amount))
    return
    diffsLat.sort();
    diffsLon.sort();
    self.sdLat = diffsLat[int(self.countPoints * amount)]
    self.sdLon = diffsLon[int(self.countPoints * amount)]
    print "SD = %f,%f" % (self.sdLat, self.sdLon)
    pass
  def debug(self):
    print "%d points" % self.countPoints
    print "Pos: %f, %f +- %f, %f" % (self.lat,self.lon, self.sdLat,self.sdLon)
    print "Lat %f to %f, Long %f to %f" % (self.S,self.N,self.W,self.E)
    print "Ratio: %f" % self.ratio
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
  def drawTracklogs(self):
    for a in self.points.values():
      for b in a:
        ctx = cairo.Context(surface)
        x = self.xpos(b[1])
        y = self.ypos(b[0])
        ctx.arc(x, y, 2.0, 0, 2*M_PI);
        ctx.fill();
    pass
  def xpos(self,lon):
    return(self.width * (lon - self.W) / self.dLon)
  def ypos(self,lat):
    return(self.height * (1 - (lat - self.S) / self.dLat))
      
      
Thingy = TracklogInfo()
directory = "eg/etienne"
print "Loading data"
Thingy.walkDir(directory)
print "Calculating extents"
Thingy.calculate(0.99)
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

