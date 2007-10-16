#!/usr/bin/env python
# GPX Party renderer
# Takes a directory structure full of GPX file tracklogs, 
# and renders them all to a series of PNG images that can
# be encoded into a video
# 
# Copyright 2007, Oliver White
# Licensed as GNU GPL version3 or at your option any later version
import cairo
import math
import sys
import getopt
import colorsys
import urllib
from xml.sax import saxutils
from xml.dom import minidom
from UserDict import UserDict
from time import *
from xml.sax import make_parser
import os
from os.path import join, getsize
deg2rad = 0.0174532925
M_PI = 3.1415926535

class Palette:
  def __init__(self, size):
    """Create a palette of visually-unique colours. 
    size = how many colours to create"""
    size = max(size,1.0)
    self.dh = 1.0/(size + 1.0)
    self.s = 0.8
    self.v = 0.9
    self.reset()
  def reset(self):
    self.h = 0.0
  def get(self):
    """Get the next available colour"""
    colour = colorsys.hsv_to_rgb(self.h, self.s, self.v)
    self.h = self.h + self.dh
    if(self.h > 1.0):
      self.h = 0.0
    return(colour)

class CityPlotter(saxutils.DefaultHandler):
  def __init__(self,surface,extents):
    self.extents = extents
    self.surface = surface
  def drawCities(self,proj,gazeteer):
    """Load and plot city locations"""
    if gazeteer == "none" or gazeteer == "":
      return
    self.loadCities(proj, gazeteer)
    self.renderCities(proj)
  def loadCities(self,proj,gazeteer):
    """Load city data from the network (osmxapi), or from a file"""
    self.attr = {}
    self.cities = []
    parser = make_parser()
    parser.setContentHandler(self)
    if gazeteer == "osmxapi":
      URL =  "http://www.informationfreeway.org/api/0.5/node[%s][bbox=%f,%f,%f,%f]" % ("place=city|town|village", proj.W, proj.S, proj.E, proj.N)
      print "Downloading gazeteer"
      sock = urllib.urlopen(URL)
      parser.parse(sock)
      sock.close
    else:
      print "Loading gazeteer"
      parser.parse(gazeteer)
    print " - Loaded %d placenames" % len(self.cities)
  def startElement(self, name, attrs):
    """Store node positions and tag values"""
    if(name == 'node'):
      self.attr['lat'] = float(attrs.get('lat', None))
      self.attr['lon'] = float(attrs.get('lon', None))
    if(name == 'tag'):
      self.attr[attrs.get('k', None)] = attrs.get('v', None)
  def endElement(self, name):
    """When each node is completely read, store it and reset the tag list for the next node"""
    if(name == 'node'):
      if self.attr.get('place') in ("city","town","village"):
        self.cities.append(self.attr)
      self.attr = {}
  def listCities(self):
    """Dumps list of city locations to stdout"""
    for c in self.cities:
      print "%s: %f,%f %s" % (c.get('name',''), c.get('lat'), c.get('lon'), c.get('place',''))
  def renderCities(self,proj):
    """Draws cities onto the map"""
    for c in self.cities:
      ctx = cairo.Context(surface)
      ctx.set_source_rgb(1.0,1.0,1.0)
      x = proj.xpos(c.get('lon'))
      y = proj.ypos(c.get('lat'))
      ctx.move_to(x, y)
      ctx.show_text(c.get('name',''))
      ctx.stroke()

class Projection:
  def __init__(self,N,E,S,W):
    self.N = N
    self.E = E
    self.S = S
    self.W = W
    self.dLat = N - S
    self.dLon = E - W
    self.ratio = self.dLon / self.dLat
  def setOutput(self,width,height):
    self.width = width
    self.height = height
  def xpos(self,lon):
    return(self.width * (lon - self.W) / self.dLon)
  def ypos(self,lat):
    return(self.height * (1 - (lat - self.S) / self.dLat))
  def debug(self):
    """Display the map extents"""
    print " - Lat %f to %f, Long %f to %f" % (self.S,self.N,self.W,self.E)
    print " - Ratio: %f" % self.ratio
    
class TracklogInfo(saxutils.DefaultHandler):
  def __init__(self):
    self.count = 0
    self.countPoints = 0
    self.points = {}
    self.currentFile = ''
    self.validTimes = {}
  def walkDir(self, directory):
    """Load a directory-structure full of GPX files into memory"""
    for root, dirs, files in os.walk(directory):
      for file in files:
        if(file.endswith(".gpx")):
          print " * %s" % file
          self.currentFile = file
          fullFilename = join(root, file)
          self.points[self.currentFile] = []
          self.inTime = 0
          self.inTrackpoint = 0
          parser = make_parser()
          parser.setContentHandler(self)
          parser.parse(fullFilename)
          self.count = self.count + 1
    self.currentFile = ''
    if(self.countPoints == 0):
      print "No GPX files found"
      sys.exit()
    print "Read %d points in %d files" % (self.countPoints,self.count)
  def startElement(self, name, attrs):
    """Handle tracklog points found in the GPX files"""
    if(name == 'trkpt'):
      self.pLat = float(attrs.get('lat', None))
      self.pLon = float(attrs.get('lon', None))
      self.inTrackpoint = 1
    if(name == 'time'):
      if(self.inTrackpoint == 1):
        self.inTime = 1
        self.timeText = ''
  def endElement(self,name):
    if(name == 'time' and self.inTime):
      self.inTime = 0
      # Parses <time>2006-10-28T10:06:03Z</time>
      time = mktime(strptime(self.timeText, "%Y-%m-%dT%H:%M:%SZ"))
      self.validTimes[time] = 1;
      self.points[self.currentFile].append((self.pLat,self.pLon,time));
      self.countPoints = self.countPoints + 1
  def characters(self, content):
    if(self.inTime == 1):
      self.timeText = self.timeText + content
    
  def valid(self):
    """Test whether the lat/long extents of the map are sane"""
    if(self.ratio == 0.0):
      return(0)
    if(self.dLat <= 0.0 or self.dLon <= 0):
      return(0)
    return(1)
  def calculate(self, radius):
    """Calculate (somehow*) the extents of the map"""
    self.calculateCentre()
    self.calculateExtents(radius)
    # then use that to calculate extents
    self.proj = Projection(
      self.lat + self.sdLat,
      self.lon + self.sdLon,
      self.lat - self.sdLat,
      self.lon - self.sdLon)
    self.proj.debug()
  def calculateCentre(self):
    """Calculate the centre point of the map"""
    sumLat = 0
    sumLon = 0
    for x in self.points.values():
      for y in x:
        sumLat = sumLat + y[0]
        sumLon = sumLon + y[1]
    self.lat = sumLat / self.countPoints
    self.lon = sumLon / self.countPoints
  def calculateExtents(self,radius):
    """Calculate the width and height of the map"""
    c = 40000.0 # circumference of earth, km
    self.sdLat = (radius / (c / M_PI)) / deg2rad
    self.sdLon = self.sdLat / math.cos(self.lat * deg2rad)
    pass
  def createImage(self,fullwidth,width1,height,surface,surface2):
    """Supply a cairo drawing surface for the maps"""
    self.fullwidth = fullwidth
    self.width = width1
    self.height = height
    self.proj.setOutput(width1,height)
    self.extents = [0,0,width1,height]
    self.surface = surface
    self.surface2 = surface2
    self.drawBorder()
    self.keyY = self.height - 20
  def drawBorder(self):
    """Draw a border around the 'map' portion of the image"""
    ctx = cairo.Context(surface)
    ctx.set_source_rgb(1.0,1.0,1.0)
    ctx.rectangle(0,0,self.fullwidth,self.height)
    ctx.fill()
    border=5
    ctx = cairo.Context(surface)
    ctx.set_source_rgb(0,0,0)
    ctx.rectangle(border,border,self.width-2*border, self.height-2*border)
    ctx.fill()
    self.extents = [border,border,self.width-border, self.height-border]
  def drawKey(self, ctx, colour, name):
    """Add a label showing which colour represents which tracklog"""
    x = self.width + 10
    y = self.keyY
    ctx.arc(x, y, 4, 0, 2*M_PI)
    ctx.fill()
    ctx.move_to(x + 10, y+4)
    ctx.set_source_rgb(0,0,0)
    ctx.show_text(name)
    ctx.stroke()
    self.keyY = self.keyY - 20
    pass
  def inImage(self,x,y):
    """Test whether an x,y coordinate is within the map drawing area"""
    if(x < self.extents[0] or y < self.extents[1]):
      return(0)
    if(x > self.extents[2] or y > self.extents[3]):
      return(0)
    return(1)
  def drawTracklogs(self, pointsize):
    """Draw all tracklogs from memory onto the map"""
    
    # Get a list of timestamps in the GPX
    timeList = self.validTimes.keys()
    timeList.sort()
    
    # Setup drawing
    self.palette = Palette(self.count)
    ctx = cairo.Context(surface)
    # CAIRO_LINE_CAP_ROUND
    ctx.set_line_cap(cairo.LINE_CAP_ROUND)
    
    # Divide time into frames
    secondsPerFrame = 500
    frameTimes = range(int(timeList[0]), int(timeList[-1]), secondsPerFrame)
    numFrames = len(frameTimes)
    lastT = 0
    frame = 1
    count = 1
    pointsDrawn = 0
    currentPositions = {}
    # For each timeslot (not all of these will become frames in the video)
    for t in frameTimes:
      self.palette.reset()
      pointsDrawnThisTimestep = 0
      # For each file
      for name,a in self.points.items():
        colour = self.palette.get()
        ctx.set_source_rgb(colour[0],colour[1],colour[2])
        # For each trackpoint which occurs within this timeslot
        for b in a:
          if(b[2] <= t and b[2] > lastT):
            x = self.proj.xpos(b[1])
            y = self.proj.ypos(b[0])
            if(self.inImage(x,y)):
              if(1):
                ctx.move_to(x,y)
                ctx.line_to(x,y)
                ctx.stroke()
              else:
                ctx.arc(x, y, pointsize, 0, 2*M_PI)
              currentPositions[name] = (x,y)
              ctx.fill()
              pointsDrawnThisTimestep = pointsDrawnThisTimestep + 1
              pointsDrawn = pointsDrawn + 1
        # On the first frame, draw a key for each track 
        # (this remains on the image when subsequent trackpoints are drawn)
        if(count == 1):
          self.drawKey(ctx, colour, name)
      # If anything changed in this frame, then add it to the video
      if(pointsDrawnThisTimestep > 0):
        ctx2 = cairo.Context(surface2)
        ctx2.set_source_surface(surface, 0, 0);
        ctx2.set_operator(cairo.OPERATOR_SOURCE);
        ctx2.paint();
        ctx2.set_source_rgb(1.0, 1.0, 0.0)
        for name,pos in currentPositions.items():
          ctx2.arc(pos[0], pos[1], pointsize*5, 0, 2*M_PI)
          ctx2.fill()
        filename = "gfx/img_%05d.png" % frame
        frame = frame + 1
        surface2.write_to_png(filename)
      print "t: %03.1f%%, %d points" % (100.0 * count / numFrames, pointsDrawnThisTimestep)
      count = count + 1
      lastT = t
    self.fadeToMapImage("gfx/img_%05d.png", frame)

  def drawCities(self, gazeteer):
    Cities = CityPlotter(self.surface, self.extents)
    Cities.drawCities(self.proj, gazeteer)
  
  def fadeToMapImage(self, filenameFormat, frame):
    URL =  "http://dev.openstreetmap.org/~ojw/bbox/?W=%f&S=%f&E=%f&N=%f&width=%d&height=%d" % (self.proj.W, self.proj.S, self.proj.E, self.proj.N, self.width, self.height)
    print "Downloading map: ", URL
    sock = urllib.urlopen(URL)
    out = open("map.png","w")
    out.write(sock.read())
    out.close()
    sock.close
    
    mapSurface = cairo.ImageSurface.create_from_png("map.png")
    
    w = mapSurface.get_width()
    h = mapSurface.get_height()
    print "Size %d x %d" % (w,h)
  
    for alphaPercent in range(0, 101, 1):
      alpha = float(alphaPercent) / 100.0
      print "Fading map: %1.0f%%" % alphaPercent
      
      # Copy 
      ctx2 = cairo.Context(surface2)
      ctx2.set_source_surface(surface, 0, 0);
      ctx2.set_operator(cairo.OPERATOR_SOURCE);
      ctx2.paint();
    
      # Overlay
      ctx2.set_source_surface(mapSurface, 0, 0);
      ctx2.paint_with_alpha(alpha)
      
      filename = filenameFormat % frame
      self.surface2.write_to_png(filename)
      frame = frame + 1


# Handle command-line options
opts, args = getopt.getopt(sys.argv[1:], "hs:d:r:p:g:", ["help", "size=", "dir=", "radius=","pointsize=","gazeteer="])
# Defauts:
directory = "./"
size = 600
radius = 10 # km
pointsize = 1 # mm
gazeteer = "osmxapi" # can change to a filename
# Options:
for o, a in opts:
  if o in ("-h", "--help"):
    print "Usage: render.py -d [directory] -s [size,pixel] -r [radius,km] -p [point size] -g [gazeteer file]"
    sys.exit()
  if o in ("-d", "--dir"):
    directory = a
  if o in ("-s", "--size"):
    size = int(a)
  if o in ("-r", "--radius"):
    radius = float(a)
  if o in ("-p", "--pointsize"):
    pointsize = float(a)
  if o in ("-g", "--gazeteer"):
    gazeteer = a

TracklogPlotter = TracklogInfo()
print "Loading data"
TracklogPlotter.walkDir(directory)
print "Calculating extents"
TracklogPlotter.calculate(radius)
if(not TracklogPlotter.valid):
  print "Couldn't calculate extents"
  sys.exit()
width = size
height = size
fullwidth = width + 120
print "Creating image %d x %d px" % (fullwidth,height)

surface = cairo.ImageSurface(cairo.FORMAT_RGB24, fullwidth, height)
surface2 = cairo.ImageSurface(cairo.FORMAT_RGB24, fullwidth, height)
TracklogPlotter.createImage(fullwidth, width, height, surface, surface2)

print "Plotting tracklogs"
TracklogPlotter.drawCities(gazeteer)

TracklogPlotter.drawTracklogs(pointsize)


