#!/usr/bin/python
#----------------------------------------------------------------------------
# Library for handling GPX data
#
# Based on ParseOsm.py from pyrender
#
# Handles:
#   * Interesting nodes, with tags (store as list)
#   * Ways, with tags (store as list)
#   * Position of all nodes (store as hash)
#----------------------------------------------------------------------------
# Copyright 2008, Oliver White (initial ParseOSM.py code)
#           2009, Graham Jones (modified to parse and analyse GPX files)
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
#
# HISTORY:
#     Feb/March 2009  GJ  ORIGIAL VERSION (part of wherewasi.py)
#     10Apr2009       GJ   Added support for audio waypoints to allow it to be
#                          used in gpxamr2wav
#
#---------------------------------------------------------------------------
import sys
import os
from xml.sax import make_parser, handler
import xml
from time import *
from geo import *

class parseGPX(handler.ContentHandler):
  def __init__(self, filename,opt_debug,opt_verbose):
    """Load a GPX XML file into memory"""
    self.debug = opt_debug
    self.verbose = opt_verbose
    self.inTrk = 0
    self.inTrkseg = 0
    self.inTrkpt = 0
    self.inWpt = 0
    self.inLink = 0
    self.inName = 0
    self.inTime = 0
    self.inEle = 0
    self.trk = []
    self.wayPts = []
    self.trkseg = []
    self.fname = "no file loaded"
    if(filename != None):
      self.loadGPX(filename)

  def loadGPX(self, filename):
    """Load a GPX XML file into memory"""
    #print "loadGPX()"
    if(not os.path.exists(filename)):
      print "Error - File %s does not exist." % filename
      return
    try:
      parser = make_parser()
      parser.setContentHandler(self)
      parser.parse(filename)
      self.fname = filename
    except xml.sax._exceptions.SAXParseException:
      print "Error loading %s" % filename
    

  def startElement(self, name, attrs):
    """Handle XML elements"""
   # print "name=%s, atrr=%s" % (name,attrs)
    if name == "trk":
      #print "Starting track"
      self.inTrk = 1
    if name == "trkseg":
      #print "Starting track segment"
      self.inTrkseg = 1
    if name == "trkpt":
      self.tags = { \
        'lat':float(attrs.get('lat')), 
        'lon':float(attrs.get('lon'))}
      self.inTrkpt = 1
    if name == "time":
      if self.inTrkpt or self.inWpt:
        self.inTime = 1
      else:
        print "startElement: ERROR time element found outside of a waypoint or trackpoint???"
      self.timeStr = ''
    if name == "ele":
      if self.inTrkpt:
        self.inEle = 1
      self.eleStr = ''
    if name == "wpt":
      self.tags = { \
        'lat':float(attrs.get('lat')), 
        'lon':float(attrs.get('lon'))}
      self.inWpt = 1
    if name == "link":
      if self.inWpt:
        self.inLink = 1
      else:
        print("startElement: ERROR - Link element found outside of a waypoint??")
      self.linkStr = ''
    if name == "name":
      if self.inWpt:
        self.inName = 1
      else:
        print("startElement: ERROR - Name element found outside of a waypoint??")
      self.nameStr = ''
      
  def characters(self, content):
    if(self.inTime):
      self.timeStr = self.timeStr + content
    if(self.inEle):
      self.eleStr = self.eleStr + content
    if(self.inLink):
      self.linkStr = self.linkStr + content
    if(self.inName):
      self.nameStr = self.nameStr + content
  
  def endElement(self, name):
    if name == 'time':
      if(self.inTime):
        self.inTime = 0
        self.tags['t'] = mktime(strptime(self.timeStr[0:-1], "%Y-%m-%dT%H:%M:%S"))
        self.tags['time'] = self.timeStr
    if name == 'ele':
      if (self.inEle):
        self.inEle = 0
        self.tags['ele'] = float(self.eleStr)
    if name == 'trkpt':
      self.trkseg .append(self.tags)
      self.inTrkpt = 0
    if name =='trkseg':
      #print "Ending Track Segment"
      self.trk.append(self.trkseg)
      self.trkseg = []
      self.inTrkseg = 0
    if name == 'trk':
      #print "Ending Track"
      self.inTrk = 0
    if name == 'link':
      self.inLink = 0
      self.tags['link'] = self.linkStr
    if name == 'name':
      self.inName = 0
      self.tags['name'] = self.nameStr
    if name == 'wpt':
      self.inWpt = 0
      self.wayPts.append(self.tags)


  def getTrackAnalysis(self,s_seg,s_pt,e_seg,e_pt):
    """Analyse the track between the segment s_seg, point s_pt,
    and segment e_seg, point e_pt and calculate a number of statistics
    which are returned as a tuple.
    Specifying the end point e_pt as -1 will result in the whole
    segment being analysed.
    GJ 19 Feb 2009  ORIGINAL VERSION
    """

    if self.debug: 
      print "getTrackAnalysis: s_seg=%d, s_pt=%d, e_seg=%d, e_pt=%d\n" % \
          (s_seg,s_pt,e_seg,e_pt)
    if not self.segNoValid(s_seg):
      print "segment numbers must lie in the range 0 to %d.\n" % (self.getNumTrkSeg()-1)
      return -1
    if not self.segNoValid(e_seg):
      print "segment numbers must lie in the range 0 to %d.\n" % (self.getNumTrkSeg()-1)
      return -1
      
    if not self.ptNoValid(s_seg,s_pt):
      print "Start point out of range - must be in range %d to %d\n" % \
          (-1,self.getNumPts(s_seg)-1)
      return -1
    if e_pt == -1:
      e_pt = self.getNumPts(e_seg)-1
    if not self.ptNoValid(e_seg,e_pt):
      print "Start point out of range - must be in range %d to %d\n" % \
          (-1,self.getNumPts(e_seg)-1)
      return -1

    if e_seg==-1:
        e_seg = self.getNumTrkSeg()-1
        if self.debug: print "Default e_seg used - set to %d" % e_seg
    else:
        if self.debug: print "e_seg=%s" % e_seg


    if e_pt==-1:
        e_pt = self.getNumPts(e_seg)-1
        if self.debug: print "Default e_pt used - set to %d" \
                % e_pt
    else:
        if self.debug: print "e_pt=%d" % e_pt



    results = {}
    npts = 0
    climb = 0  # total climb (m)
    dist = 0   # total distance (km)
    time = 0   # total time (sec)
    maxSpeed = 0 # maximum speed (km/hr)
    maxSpeedSeg = 0 # segment containing maximum speed
    maxSpeedPt = 0 # point within segment of maximum speed
    maxTime = 0    # latest time (sec)
    maxTimeSeg = 0 # segment containing latest time
    maxTimePt = 0  # point within segment of latest time
    trkpt = self.getTrkPt(s_seg,s_pt)
    minTime = trkpt['t'] # earliest time (sec)
    minTimeSeg = s_seg   # segment containing earliest time
    minTimePt = s_pt     # point within segment of earliest time
    prev = -999

    for seg in range(s_seg,e_seg+1):
      # Start and end points for current segment
      seg_s_pt = -1
      seg_e_pt = -1
      if (seg==s_seg):
        seg_s_pt = s_pt
      else:
        seg_s_pt = 0

      if (seg==e_seg):
        seg_e_pt = e_pt
      else:
        seg_e_pt = self.getNumPts(seg)-1


      if self.debug:
        print "getTrackAnalysis(): Analysing segment %d, from point %d to point %d." % (seg,seg_s_pt,seg_e_pt)

      for pt in range(seg_s_pt,seg_e_pt+1):
        trkpt = self.getTrkPt(seg,pt)
        npts = npts + 1
        #print "seg=%d, pt=%d trkpt=%s" % (seg,pt,trkpt)
        # Check max and min time
        if trkpt['t']>maxTime:
          maxTime = trkpt['t']
          maxTimeSeg = seg
          maxTimePt = pt
        if trkpt['t']<minTime:
          minTime = trkpt['t']
          minTimeSeg = seg
          minTimePt = pt

        if prev != -999:
          # calculate distance travelled
          #a = (trkpt['lat'],trkpt['lon'])
          #b = (prev['lat'],prev['lon'])
          #ddist = distance(a,b)
          ddist = distance(trkpt['lat'],trkpt['lon'],prev['lat'],prev['lon'])
          dist = dist + ddist

          # calculate increase in elevation
          if trkpt['ele']>prev['ele']:
            climb = climb + trkpt['ele']-prev['ele']
            #print "climb=%f" % climb

          # calculate time difference between trkpt and prev
          dtime = trkpt['t'] - prev['t']
          time = time + dtime
          #print "time=%f" % time

          # calculate speed
          speed = ddist/(dtime/3600.0)
          if speed>maxSpeed:
            maxSpeed = speed
            maxSpeedSeg = seg
            maxSpeedPt = pt
        else:
          pass
          #print "prev=-999 - skipping first point"
        prev = trkpt
    results['npts'] = npts
    results['climb']=climb
    results['dist'] = dist
    results['time'] = time
    if (time!=0):
      results['avSpeed'] = dist/(time/3600.)
    else:
      results['avSpeed'] = 0.0
    results['maxSpeed']=maxSpeed
    results['maxSpeedSeg'] = maxSpeedSeg
    results['maxSpeedPt'] = maxSpeedPt
    results['minTime'] = minTime
    results['minTimeSeg'] = minTimeSeg
    results['minTimePt'] = minTimePt
    results['maxTime'] = maxTime
    results['maxTimeSeg'] = maxTimeSeg
    results['maxTimePt'] = maxTimePt
    #print results
    return results


  def getProfileData(self,s_seg,s_pt,e_seg,e_pt):
    """Get the elevation profile of the track between the segment 
    s_seg, point s_pt,
    and segment e_seg, point e_pt
    Specifying the end point e_pt as -1 will result in the whole
    segment being analysed.
    GJ 20 Feb 2009  ORIGINAL VERSION
    """

    #print "getProfileData: s_seg=%d, s_pt=%d, e_seg=%d, e_pt=%d\n" % \
    #    (s_seg,s_pt,e_seg,e_pt)
    if not self.segNoValid(s_seg):
      print "segment numbers must lie in the range 0 to %d.\n" % (self.getNumTrkSeg()-1)
      return -1
    if not self.segNoValid(e_seg):
      print "segment numbers must lie in the range 0 to %d.\n" % (self.getNumTrkSeg()-1)
      return -1
      
    if not self.ptNoValid(s_seg,s_pt):
      print "Start point out of range - must be in range %d to %d\n" % \
          (0,self.getNumPts(s_seg)-1)
      return -1
    if e_pt == -1:
      e_pt = self.getNumPts(e_seg)-1
    if not self.ptNoValid(e_seg,e_pt):
      print "Start point out of range - must be in range %d to %d\n" % \
          (0,self.getNumPts(e_seg)-1)
      return -1

    if e_seg==-1:
        e_seg = self.getNumTrkSeg()-1
        if self.debug: print "Default e_seg used - set to %d" % e_seg
    else:
        if self.debug: print "e_seg=%s" % e_seg


    if e_pt==-1:
        e_pt = self.getNumPts(e_seg)-1
        if self.debug: print "Default e_pt used - set to %d" \
                % e_pt
    else:
        if self.debug: print "e_pt=%d" % e_pt


    results = []
    trkpt = self.getTrkPt(s_seg,s_pt)
    s_time = trkpt['t']
    prev = -999
    dist = 0
    speed = 0

    for seg in range(s_seg,e_seg+1):
      # Start and end points for current segment
      seg_s_pt = -1
      seg_e_pt = -1
      if (seg==s_seg):
        seg_s_pt = s_pt
      else:
        seg_s_pt = 0

      if (seg==e_seg):
        seg_e_pt = e_pt
      else:
        seg_e_pt = self.getNumPts(seg)-1


      #print "getProfileData(): Analysing segment %d, from point %d to point %d." % (seg,seg_s_pt,seg_e_pt)


      for pt in range(seg_s_pt,seg_e_pt+1):
        trkpt = self.getTrkPt(seg,pt)

        if prev != -999:
          # calculate distance travelled
          #a = (trkpt['lat'],trkpt['lon'])
          #b = (prev['lat'],prev['lon'])
          #ddist = distance(a,b)
          ddist = distance(trkpt['lat'],trkpt['lon'],prev['lat'],prev['lon'])
          dist = dist + ddist
          dtime = trkpt['t'] - prev['t']
          speed = ddist / (dtime/3600.)
        else:
          pass
          #print "prev=-999 - skipping first point"
        resrec = (trkpt['t'],trkpt['t']-s_time,dist,trkpt['ele'],speed)
        results.append(resrec)
        prev = trkpt
    return results




  def getNumTrkSeg(self):
    """Returns the number of track segments read from the GPX file."""
    return len(self.trk)

  def getNumPts(self,segno):
    """Returns the number of points in a given track segment."""
    if not self.segNoValid(segno):
      print "getNumPts(): Error: segment numbers must lie in the range 0 to %d.\n" \
          % (self.getNumTrkSeg()-1)
      return -1
    else:
      return len(self.trk[segno])

  def segNoValid(self,segno):
    """ Returns true if the segment number provided is within the allowable
    range, or false if it is not"""
    if segno < -1 or segno> len(self.trk)-1:
      print "segNoValid(): Error: segment number %d invalid: segment numbers must lie in the range 0 to %d.\n" \
          % (segno,len(self.trk)-1)
      return False
    else:
      return True

  def ptNoValid(self,segno,ptno):
    """Returns true if the point number specified is within the allowable range
    for segment number segno, otherwise returns false."""
    if not self.segNoValid(segno):
      print "ptNoValid(): Error: Segment Number %d invalid: segment numbers must lie in the range -1 to %d.\n" \
          % (segno,self.getNumTrkSeg()-1)
      return False
    else:
      if ptno<-1 or ptno > self.getNumPts(segno)-1:
        print "ptNoValid(): Error: Point numbers for segment %d  must lie in the range -1 to %d.\n" \
          % (self.getNumPts(segno)-1)
        return False
      else:
        return True

  def getTrkPt(self,segno,ptno):
    """Returns the track point, tuple of 'lat', 'lon', 'ele' and 'time'
    for the specified segment number and point number."""
    if self.ptNoValid(segno,ptno):
      return self.trk[segno][ptno]
    else:
      return -1

  def getPos(self,segno,ptno):
    """Returns the latitude and longitude of the point number ptno in 
    segment number segno."""
    
    if self.ptNoValid(segno,ptno):
      pt = self.trk[segno][ptno]
      pos = (pt['lat'],pt['lon'])
      return pos
    else:
      return -1

  def getEle(self,segno,ptno):
    """Returns the elevation of the point number ptno in 
    segment number segno."""
    
    if self.ptNoValid(segno,ptno):
      pt = self.trk[segno][ptno]
      ele = pt['ele']
      return ele
    else:
      return -1


if __name__ == "__main__":
  track = parseGPX(sys.argv[1])

  numseg = len(track.trk)
  print "There are %d segments in the Track" % numseg
  for segno in range(0,numseg):
    print "Segment %d Contains %d points" % (segno,len(track.trk[segno]))

#  for pos in track.samples:
#    print "%s: (%f,%f), ele=%s\n" % (pos['t'],pos['lat'],pos['lon'],pos['ele'])


