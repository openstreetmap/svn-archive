#!/usr/bin/env python
# -*- coding: utf-8 -*-
# -----------------------------------------------------------------------
# Takes a batch of images from a video, and matches them against a GPX
# tracklog to get georeferenced images
# -----------------------------------------------------------------------
# Copyright 2009, Oliver White
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# -----------------------------------------------------------------------
# Includes GPX and timestamp code from mapping-party renderer 
# (2007, Oliver White and others)
# http://svn.openstreetmap.org/applications/rendering/party/video.py
# -----------------------------------------------------------------------
import math
import sys
import urllib
from xml.sax import handler
from xml.dom import minidom
from UserDict import UserDict
from time import *
from xml.sax import make_parser
import re
import os
from os.path import join, getsize, splitext
import shutil


def parseTime(timeString):
  """ Parses <time>2006-10-28T10:06:03Z</time>"""
  time = None
  try:
    if timeString.endswith("Z"):
      try:
	time = mktime(strptime(timeString, "%Y-%m-%dT%H:%M:%SZ"))
      except ValueError:
	# FIXME: Assumes this was just a time with second fractions
	self.timeText = timeString[0:18] + "Z"
	time = mktime(strptime(timeString, "%Y-%m-%dT%H:%M:%SZ"))
    else: # not GMT
      time = mktime(strptime(timeString, "%Y-%m-%dT%H:%M:%S"))
  except (OverflowError, ValueError):
    print >>sys.stderr, "Rejected timestamp '%s'" % self.timeText
  return(time)

def formatT(t):
  """Format a floating-point time_t into text"""
  return(strftime("%Y-%m-%d %H:%M:%S", localtime(t)))

class gpxReader(handler.ContentHandler):
  """Reads a GPX file, and allows querying of 'where were we at time t?' """
  def __init__(self, filename=None):
    self.points = []
    self.validTimes = {}
    if(filename):
      self.readGpx(filename)

  def readGpx(self, filename):
    self.inTime = 0
    self.inTrackpoint = 0
    parser = make_parser()
    parser.setContentHandler(self)
    parser.parse(filename)

  def walkDir(self, directory):
    """Load multiple GPX files into memory"""
    for root, dirs, files in os.walk(directory):
      for file in files:
        if(file.endswith(".gpx")):
          fullFilename = join(root, file)
	  self.readGpx(fullFilename)
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
      time = parseTime(self.timeText)
      if(time):
        self.validTimes[time] = 1;
        self.points.append((self.pLat,self.pLon,time));
  def characters(self, content):
    if(self.inTime == 1):
      self.timeText = self.timeText + content
  def getPositionAt(self, t):
    #print "Searching %s = %f" % (formatT(t), t)

    (t1,lat1,lon1,valid) = (0,0,0,False)
    for p in self.points:
      (lat2,lon2,t2) = p
      #print " : %s = %f" % (formatT(t2), t2)
      if(valid):
	if((t >= t1) and (t <= t2) and ((t2-t1) < 3)):
	  return(lat2,lon2,"from %s < %s < %s" % (formatT(t1), formatT(t), formatT(t2)))
      (lat1,lon1,t1,valid) = (lat2,lon2,t2,True)

    return(0,0,None)
     

class imageLoader:
  """Searches a directory for a load of numbered image files, and stores their names"""
  def __init__(self, directory="."):
    self.images = {}
    for root, dirs, files in os.walk(directory):
      for file in files:
	m = re.search("(\d+)\.jpg$", file)
        if(m):
	  id = int(m.groups()[0]) # assume the filename has a number in it
	  self.images[id] = join(root, file)


def mergeVideoAndGpx(videoImageDir, gpxFilename, startTime, fps):
  tracklog = gpxReader(gpxFilename)
  print "Read %d points from tracklog" % len(tracklog.points)
  print "spanning %s to %s" % (formatT(tracklog.points[0][2]), formatT(tracklog.points[-1][2]))

  if(0):
    for p in tracklog.points:
      (lat,lon,t) = p
      print "%s" % formatT(t)
    return

  start = parseTime(startTime)
  b = imageLoader(videoImageDir)
  print "Loaded %d images, expecting %d" % (len(b.images), len(b.images) / fps)
  print "spanning %s to %s" % (formatT(start), formatT(start + len(b.images) / float(fps)))

  out = open("output.kml", "w")
  out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
  out.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
  out.write("<Document>\n")
  count = 0
  for i in b.images.keys():
    if(count % fps == 0): # 1 per sec
      t = start + (float(count) / float(fps))
      (lat,lon,notes) = tracklog.getPositionAt(t)
      if(notes):
	out.write("  <Placemark><name>%s</name>\n" % formatT(t))
	imageLink = "frames/%06d.jpg" % i
	imageURL = "http://dev.openstreetmap.org/~ojw/StreetPhotos/" + imageLink # eww! TODO
	shutil.copy2(b.images[i], imageLink)
	out.write("    <description><img src=\"%s\" /><br/>Location (%f, %f)<br/>Taken at %s</description>\n" % (imageURL, lat, lon, formatT(t)))
	out.write("    <Point><coordinates>%f,%f,0</coordinates></Point>\n" % (lon,lat))
	out.write("  </Placemark>\n")
      else:
	pass 
	#print "fail %s"%formatT(t)
    count += 1
  out.write("</Document>\n</kml>\n")
  out.close()

if(__name__ == "__main__"):
  mergeVideoAndGpx("video", "../latest.gpx", "2009-07-26T09:52:17Z", 25) # TODO: command-line options
