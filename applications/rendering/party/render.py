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
import getopt
from xml.sax import saxutils
from UserDict import UserDict
from xml.sax import make_parser
from math import sqrt
import os
import random 
from os.path import join, getsize
deg2rad = 0.0174532925
M_PI = 3.1415926535

def qnth(sample, n): # http://en.literateprograms.org/Nth_element_%28Python%29
    pivot = sample[0]
    below = [s for s in sample if s < pivot]
    above = [s for s in sample if s > pivot]
    i, j = len(below), len(sample)-len(above)
    
    if n < i:      return qnth(below, n)
    elif n >= j:   return qnth(above, n-j)
    else:          return pivot
    
def median(a,k):
  return(qnth(a,k))

if(0): # test accuracy of median()
  print median([10,100,9,90,50,5], 0)
  sys.exit();
  
if(0): # test speed of median()
  a=[]
  for i in range(50000):
    a.append(random.random())
  print median(a, 40000)
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
    if(self.countPoints == 0):
      print "No GPX files found"
      sys.exit()
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
  def calculate(self, radius):
    # Calculate mean and standard deviation
    self.calculateCentre()
    self.calculateExtents(radius)
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

  def calculateCentre(self):
    sumLat = 0
    sumLon = 0
    for x in self.points.values():
      for y in x:
        sumLat = sumLat + y[0]
        sumLon = sumLon + y[1]
    self.lat = sumLat / self.countPoints
    self.lon = sumLon / self.countPoints
    
  def calculateExtents(self,radius):
    c = 40000.0 # circumference of earth, km
    self.sdLat = (radius / (c / M_PI)) / deg2rad
    self.sdLon = self.sdLat / math.cos(self.lat * deg2rad)
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

# Handle command-line options
opts, args = getopt.getopt(sys.argv[1:], "hs:d:r:", ["help=", "size=", "dir=", "radius="])
# Defauts:
directory = "./"
size = 600
radius = 10 # km
# Options:
for o, a in opts:
  if o in ("-h", "--help"):
    print "Usage: render.py -d [directory] -s [size,pixel] -r [radius,km]"
    sys.exit()
  if o in ("-d", "--dir"):
    directory = a
  if o in ("-s", "--size"):
    size = int(a)
  if o in ("-r", "--radius"):
    radius = float(a)

print "Loading data"
Thingy.walkDir(directory)
print "Calculating extents"
Thingy.calculate(radius)
if(not Thingy.valid):
  print "Couldn't calculate extents"
  sys.exit()
width = size
height = int(width / Thingy.ratio)
print "Creating image %d x %d" % (width,height)

surface = cairo.ImageSurface(cairo.FORMAT_ARGB32, width, height)
Thingy.createImage(width, height, surface)
Thingy.drawTracklogs()

surface.write_to_png("output.png")

