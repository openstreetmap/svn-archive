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
import pymedia.video.vcodec as vcodec
import pygame
import array

deg2rad = 0.0174532925
M_PI = 3.1415926535

class videoThingy():
  def __init__(self, width, height):
    params= { \
      'type': 0,
      'gop_size': 12,
      'frame_rate_base': 125,
      'max_b_frames': 0,
      'height': height,
      'width': width,
      'frame_rate': 2997,
      'deinterlace': 0,
      'bitrate': 2700000,
      'id': vcodec.getCodecID( "mpeg2video")
    }
    self.width = width
    self.height = height
    filename = "out_m.mpg"
    print "Outputting to %s at %d x %d" %( filename,width,height)
    self.fw= open(filename, 'wb' )
    self.e= vcodec.Encoder( params )
  
  def addFrame(self, surface, repeat = 1):
    # surface = cairo.ImageSurface.create_from_png("map.png")
    buf = surface.get_data_as_rgba()
    w = surface.get_width()
    h = surface.get_height()
    #s= pygame.image.load("map.png")
    
    s= pygame.image.frombuffer(buf, (self.width,self.height), "RGBA")
    ss= pygame.image.tostring(s, "RGB")

    bmpFrame= vcodec.VFrame( vcodec.formats.PIX_FMT_RGB24, s.get_size(), (ss,None,None))
    yuvFrame= bmpFrame.convert( vcodec.formats.PIX_FMT_YUV420P )
    
    for i in range(repeat):
      d= self.e.encode( yuvFrame )
      self.fw.write( d.data )
      
  def finish(self):
    self.fw.close()

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
    self.frame = 1
  def finish(self):
    self.video.finish()
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
  def setCentre(self,lat,lon):
    self.lat = lat
    self.lon = lon
    print "Centred on %f, %f" % (lat,lon)
  def calculateCentre(self):
    """Calculate the centre point of the map"""
    sumLat = 0
    sumLon = 0
    for x in self.points.values():
      for y in x:
        sumLat = sumLat + y[0]
        sumLon = sumLon + y[1]
    self.setCentre(sumLat / self.countPoints, sumLon / self.countPoints)
  def calculateExtents(self,radius):
    """Calculate the width and height of the map"""
    c = 40000.0 # circumference of earth, km
    self.sdLat = (radius / (c / M_PI)) / deg2rad
    self.sdLon = self.sdLat / math.cos(self.lat * deg2rad)
    self.proj = Projection(
      self.lat + self.sdLat,
      self.lon + self.sdLon,
      self.lat - self.sdLat,
      self.lon - self.sdLon)
    self.proj.debug()
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
    #self.filenameFormat = "gfx/img_%05d.png"
    self.video = videoThingy(fullwidth, height)
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
  def drawTracklogs(self):
    """Draw all tracklogs from memory onto the map"""
    
    # Get a list of timestamps in the GPX
    timeList = self.validTimes.keys()
    timeList.sort()
    
    # Setup drawing
    self.palette = Palette(self.count)
    ctx = cairo.Context(surface)
    ctx.set_line_cap(cairo.LINE_CAP_ROUND)
    
    # Divide time into frames
    secondsPerFrame = 60
    frameTimes = range(int(timeList[0]), int(timeList[-1]), secondsPerFrame)
    numFrames = len(frameTimes)
    lastT = 0
    count = 1
    pointsDrawn = 0
    currentPositions = {}
    # For each timeslot (not all of these will become frames in the video)
    for t in frameTimes:
      somethingToDraw = 0
      if lastT == 0:
        somethingToDraw = 1
      else:
        for ti in range(lastT+1, t+1):
          if self.validTimes.has_key(ti):
            somethingToDraw = 1

      if somethingToDraw:
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
          self.video.addFrame(surface2)
          print "t: %03.1f%%, %d points" % (100.0 * count / numFrames, pointsDrawnThisTimestep)
      lastT = t
      count = count + 1
    self.pause(self.surface2, 50)
    self.fadeToMapImage()

  def drawCities(self, gazeteer):
    Cities = CityPlotter(self.surface, self.extents)
    Cities.drawCities(self.proj, gazeteer)
  
  def pause(self, surface, frames):
    print "Pausing..."
    self.video.addFrame(surface, frames)
    
  def fadeToMapImage(self):
    URL =  "http://dev.openstreetmap.org/~ojw/bbox/?W=%f&S=%f&E=%f&N=%f&width=%d&height=%d" % (self.proj.W, self.proj.S, self.proj.E, self.proj.N, self.width, self.height)
    print "Downloading map: "
    print URL
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

      self.video.addFrame(surface2, 3)
      
    self.pause(self.surface2, 200)

  def drawTitle(self):
    self.drawBorder()
    ctx = cairo.Context(self.surface)
    page = TitlePage(0.5 * self.width, self.height, ctx)
    page.text("OpenStreetMap", 55, 0.28)
    page.text("Surrey Hills Mapping Party", 30, 0.46)
    page.text("October 2006", 30, 0.52)
    page.text("Creative Commons CC-BY-SA 2.0", 25, 0.85)
    
    print "Title..."
    self.video.addFrame(self.surface, 70)

  def drawCredits(self):
    self.drawBorder()
    ctx = cairo.Context(self.surface)
    page = TitlePage(0.5 * self.width, self.height, ctx)
    page.text("www.OpenStreetMap.org", 30, 0.35)
    page.text("Creative Commons CC-BY-SA 2.0", 25, 0.85)
    print "Credits..."
    self.video.addFrame(self.surface, 70)

class TitlePage():
  def __init__(self,xc,height,context):
    self.context = context
    self.height = height
    self.xc = xc
    self.context.set_source_rgb(1.0, 1.0, 1.0)
    self.context.select_font_face( \
      "FreeSerif", 
      cairo.FONT_SLANT_NORMAL, 
      cairo.FONT_WEIGHT_BOLD)

  def text(self,text,size, yp):
    self.context.set_font_size(size)
    x_bearing, y_bearing, width, height = \
      self.context.text_extents(text)[:4]
    self.context.move_to( \
      self.xc - width / 2 - x_bearing, 
      yp * self.height - height / 2 - y_bearing)
    self.context.show_text(text)    

# Handle command-line options
opts, args = getopt.getopt(sys.argv[1:], "hs:d:r:p:g:", ["help", "size=", "dir=", "radius=","gazeteer=","pos="])
# Defauts:
directory = "./"
size = 600
radius = 10 # km
pointsize = 1 # mm
specifyCentre = 0
gazeteer = "osmxapi" # can change to a filename
# Options:
for o, a in opts:
  if o in ("-h", "--help"):
    print "Usage: render.py -d [directory] -s [size,pixel] -r [radius,km] -g [gazeteer file] --pos=[lat],[lon]"
    sys.exit()
  if o in ("-d", "--dir"):
    directory = a
  if o in ("-s", "--size"):
    size = int(a)
  if o in ("-r", "--radius"):
    radius = float(a)
  if o in ("-g", "--gazeteer"):
    gazeteer = a
  if o in ("--pos"):
    lat, lon = [float(x) for x in a.split(",")]
    specifyCentre = 1
    print "Lat: %f\nLon: %f" % (lat,lon)

TracklogPlotter = TracklogInfo()
print "Loading data"
TracklogPlotter.walkDir(directory)

print "Calculating extents"
if specifyCentre:
  TracklogPlotter.setCentre(lat,lon)
else:
  TracklogPlotter.calculateCentre()
TracklogPlotter.calculateExtents(radius)
  
if(not TracklogPlotter.valid):
  print "Couldn't calculate extents"
  sys.exit()

width = size
height = size
fullwidth = width + 120
print "Creating image %d x %d px" % (fullwidth,height)

surface = cairo.ImageSurface(cairo.FORMAT_ARGB32, fullwidth, height)
surface2 = cairo.ImageSurface(cairo.FORMAT_ARGB32, fullwidth, height)
TracklogPlotter.createImage(fullwidth, width, height, surface, surface2)

TracklogPlotter.drawTitle()

print "Plotting tracklogs"
TracklogPlotter.drawBorder()
TracklogPlotter.drawCities(gazeteer)

TracklogPlotter.drawTracklogs()

TracklogPlotter.drawCredits()

TracklogPlotter.finish()