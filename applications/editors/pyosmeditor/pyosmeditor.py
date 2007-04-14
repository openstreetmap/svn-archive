#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2006  Michael Strecke
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#

import sys, os, re, stat
import xml.dom.minidom
import math, urllib2, httplib
import pango
import gzip, StringIO
import time, datetime

try:
   import EXIF
   exiflib = True
except ImportError:
   exiflib = False

debug = False
pyosmeditorversion = "0.1.2"
DB_ENCODING = "utf-8"

CONFIGFILENAME = "~/.pyosmeditor/pysomeditor.xml"

try:
   import pygtk
   import gobject
   pygtk.require("2.0")
except:
   pass
try:
   import gtk
   import gtk.glade
except:
   sys.exit(1)

##############################
# Import translations for .glade and .py
import locale
import gettext
APP = 'pyosmeditor'
DIR = 'locale'

gtk.glade.bindtextdomain(APP,DIR) 
gtk.glade.textdomain(APP)
gettext.bindtextdomain(APP,DIR) 
gettext.textdomain(APP)
_ = gettext.gettext

localeencoding = locale.getpreferredencoding()

######################
# os helper functions
def createsubdirforfile(fnm):
   pa = os.path.split(fnm)[0]           # dir part
   if pa:
      if not os.path.exists(pa):
         os.makedirs(pa)

def add_extension(fnm,ext):
   """ add extension to filename, if it has no exention
   """
   ro, ex = os.path.splitext(fnm)
   if ex == "":                   # no extension
     ex = ext
     
   return ro + ex
   
def filesize(fnm):
  return os.stat(fnm)[stat.ST_SIZE]

############
#  Exif helper
def exifdate(fnm):
   if exiflib:
      f=open(fnm, 'rb')
      tags=EXIF.process_file(f)
      try:
         return str(tags['Image DateTime'])
      except KeyError:
         return None    
   else:
      return None


######################
# gtk helper
def process_pending_gtk_events():
   while gtk.events_pending(): gtk.main_iteration()

def enable_widgets(options,enable = True):
   for option in options:
      option.set_sensitive(enable)

def simple_dialog(type,message,buttons, modal = True):
   # types: gtk.MESSAGE_INFO, gtk.MESSAGE_WARNING, gtk.MESSAGE_QUESTION, gtk.MESSAGE_ERROR
   # buttons: gtk.BUTTONS_NONE, gtk.BUTTONS_OK, gtk.BUTTONS_CLOSE, gtk.BUTTONS_CANCEL, 
   #          gtk.BUTTONS_YES_NO, gtk.BUTTONS_OK_CANCEL
   flags = 0
   if modal:
      flags |= gtk.DIALOG_MODAL
   if buttons == None:
      buttons = gtk.BUTTONS_NONE

   dia = gtk.MessageDialog(None, flags = flags, type=type, buttons=buttons, message_format=None)
   dia.set_markup(message)
   if modal:
      response = dia.run()
      dia.destroy()
      return response == gtk.RESPONSE_OK
   else:
      dia.show_now()
      process_pending_gtk_events()
      return dia
  
######################
# XML helper functions

def appendNodeAndText(doc,parent,element,content):
   """ append an element node with corresponding text node to parent
   
       doc:     document
       parent:  parent node to which the child will be appended
       element: name of the text node
       content: content of the text node (None -> empty node)
   """
   s = doc.createElement(element)
   if content != None:
      t = doc.createTextNode(str(content))
      s.appendChild(t)
   parent.appendChild(s)

def getChildValue(node,childname):
   """ get value of child of node with name childname
   
       return value
         None: if node exists but no text node
       raises ValueError if child does not exist
   """ 
   for ele in node.childNodes:
      if ele.nodeType == xml.dom.minidom.Node.ELEMENT_NODE:
         if childname == ele.localName:
            value = None
            for sub in ele.childNodes:
               if sub.nodeType == xml.dom.minidom.Node.TEXT_NODE:
                  return sub.nodeValue
            return value
   raise ValueError, "no such child"

def setChildValue(doc,node,childname,value):
   found = False
   for ele in node.childNodes:
      if ele.nodeType == xml.dom.minidom.Node.ELEMENT_NODE:
         if childname == ele.localName:
            # search child of element node for text nodes
            for sub in ele.childNodes:
               if sub.nodeType == xml.dom.minidom.Node.TEXT_NODE:
                  found = True
                  if value != None:
                     # set new value, if not None
                     sub.nodeValue = str(value)
                  else:
                     # remove text node, if new value *is* None
                     ele.removeChild(sub)
                  return
            
            # element node has no text child nodes
            if not found:
               # add one, if value is not None
               if value != None:
                  sub = doc.createTextNode(str(value))
                  ele.appendChild(sub)
            return
   
   # No element node with that name found
   if not found:            
      appendNodeAndText(doc,node,childname,value)

###############

def decode_time(s):
   # 2006-07-07T10:20:56Z
   erg = re.match("^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})Z$",s)
   if erg:
      return datetime.datetime(int(erg.group(1)),int(erg.group(2)),int(erg.group(3)),int(erg.group(4)),int(erg.group(5)),int(erg.group(6)))   

   # 2006:07:07 17:06:38 or 2006.07.07 17:06:38 or 2006-07-07 17:06:38
   # JJJJ:MM:DD HH:MM:SS
   erg = re.match("^(\d{4})[:|.|-](\d{2})[:|.|-](\d{2}) (\d{2}):(\d{2}):(\d{2})$",s)
   if erg:
      return datetime.datetime(int(erg.group(1)),int(erg.group(2)),int(erg.group(3)),int(erg.group(4)),int(erg.group(5)),int(erg.group(6)))   

   raise ValueError,"Unknow date format: "+s

def distance(longS, latS, longD, latD):
   # http://obivan.uni-trier.de/p/h/vb/third_b_va.html
   radius = 6370000.0
   if longS == longD and latS == latD: return 0
   dl = math.radians(abs(longS - longD))
   latS = math.radians(latS)
   latD = math.radians(latD)   
   cos_d = math.sin(latS) * math.sin(latD) + math.cos(latS) * math.cos(latD) * math.cos(dl)
   dist = math.acos(cos_d) * radius 
   
   return dist
   
def timestampit(node):
   """ set timestmp attribute on node
       current time (in GMT, I presume)
   """
   node.setAttribute("timestamp",time.strftime("%Y-%m-%d %H:%M:%S",time.gmtime()))

######################
# Simple config helper
class simpleconfigfile:

   def __init__(self,filename,rootnodename = None,version = None,elements=None,defaults=None):
      """ filename:      name of the XML file with the config data
          rootnodename:  name of the root node in the config file
          version:       if set, version attribute in root node must have the same value
          elements:      dictionary, key determines which node is part of "data", value has a translation function
          defaults:      dictionary with default values (key, value -> <key>value</key>
      """
      self.doc = None              # pointer to xml doc in memory
      self.root = None             # pointer to root element
      self.data = {}               # selected config data goes here
      self.elements = elements     # list of nodes we want in self.data
      self.filename = filename     # we need that when we write the tree to disk
      filename = os.path.expanduser(filename)
      try:
         # try to parse file
         self.doc = xml.dom.minidom.parse(filename)  
      except:
         # Create default tree
         self.doc = xml.dom.minidom.Document()    # empty tree
         self.root = self.doc.createElement(rootnodename)
         self.doc.appendChild(self.root)
         if version != None:
            self.root.setAttribute("version",str(version))
    
      # Now scan the (newly created or read) tree
      # throw exeception, if root element of XML files is not the one we expect
      
      self.root = self.doc.getElementsByTagName(rootnodename)
      assert self.root != None
      self.root = self.root[0]    # As this is the root element, only one can be available

      # check data version
      if version != None:
         v = self.root.getAttribute("version")
         if (v != None) and (v != str(version)):
            raise ValueError,"data version of config file is not compatible"

      # populate sub-tree with defaults, if nodes are not already present
      if defaults:
         self.dict2tree(defaults, overwrite = False)
      # fill local dictionary with (merged) data from the tree
      self.data = self.tree2dict(elements)

      if debug:
         print self.data
       
   def dict2tree(self,di, overwrite):
      if not di:
         return
         
      for key in di:
         nodelist = self.root.getElementsByTagName(key)
         nodecnt = len(nodelist)
         if nodecnt == 0:
             setChildValue(self.doc,self.root,key,di[key])
         elif nodecnt == 1:
             if overwrite:
                setChildValue(self.doc,self.root,key,di[key])
         else:
             raise ValueError,"unique key found more than once"
             
   def tree2dict(self,nodes):
      if not nodes:
         return {}
       
      di = {}
      for nodename in nodes:
         nodelist = self.root.getElementsByTagName(nodename)
         nodecnt = len(nodelist)
         if nodecnt == 1:
             val = getChildValue(self.root,nodename)  # remember, values are always strings!
             if val != None:
                if nodes[nodename] != None:           # use supplied conversion function
                   di[nodename] = nodes[nodename](val)
                else:
                   di[nodename] = val
             else:                                    # None remains None, regardless of the conversion function
                di[nodename] = None
                
         elif nodecnt == 0:
             pass
         else:
             raise ValueError,"unique key found more than once"
      return di 
             
   def writedata(self,filename = None):
      """ Write config file
      """
      assert self.doc != None
      out = filename
      if out == None:
         out = self.filename
      assert out != None
      
      out = os.path.expanduser(out)

      createsubdirforfile(out)
      
      # merge local dictionary into XML tree
      self.dict2tree(self.data, overwrite = True)
      if debug:
         print self.doc.toxml()
      fl = open(out,"w")
      fl.write(self.doc.toxml())     
      fl.close()
      
#############
def normalizebox_deg(lon1,lat1,lon2,lat2):
   if lon1 < lon2:
      alon = lon1
      blon = lon2
   else:
      alon = lon2
      blon = lon1
   
   if lat1 < lat2:
      alat = lat1
      blat = lat2
   else:
      alat = lat2
      blat = lat1
   
   flag = False
   if blon-alon > 200:
     a = alon
     alon = blon
     blon = a
     blon += 360
     flag = True
   
   return (alon,alat,blon,blat,flag)

def normalizebox_xy(x1,y1,x2,y2):
   if x2<x1:
     x1,x2 = x2,x1
   if y2<y1:
     y1,y2 = y2,y1
   return (x1,y1,x2,y2)

class box_xy:
   def __init__(self,start = None):
      if start == None: 
         self.clear()
      else:
         if type(start) == tuple:
            self.lx, self.ly, self.tx, self.ty = start
         else:
            self.lx = start.lx
            self.ly = start.ly
            self.tx = start.tx
            self.ty = start.ty

   def clear(self):
      self.lx = None   # Nothing set
      self.ly = None
      self.tx = None
      self.ty = None
   
   def empty(self):
      return self.lx == None
      
   def put_point(self,x,y):
      x = int(x)
      y = int(y)
      if self.empty(): # Nothing set
         self.lx = x
         self.ly = y
         self.tx = x
         self.ty = y
      else:
         self.lx = min(self.lx,x)
         self.ly = min(self.ly,y)
         self.tx = max(self.tx,x)
         self.ty = max(self.ty,y)
   
   def putbox(self,x1,y1,x2,y2):
      self.put_point(x1,y1)
      self.put_point(x2,y2)
   
   def isinbox(self,x,y):
      if self.empty():
         return False
      return (self.lx <= x) and (x <= self.tx) and (self.ly <= y) and (y <= self.ty)
   
   def getbox(self):
      if self.lx == None:
         return None
      return (self.lx, self.ly, self.tx, self.ty)

   def enlarge(self,fact):
     dx = int((self.tx-self.lx) * (fact-1) / 2)
     dy = int((self.ty-self.ly) * (fact-1) / 2)
     self.lx -= dx
     self.ly -= dy
     self.tx += dx
     self.ty += dy

   def grow(self,dx1,dy1,dx2,dy2):
     self.lx -= dx1
     self.ly -= dy1
     self.tx += dx2
     self.ty += dy2
        
class box_deg:
   # warp = True ->  negative longitude has been normalized to 180 .. 360
   # internally the "normalized" values are used          

   def __init__(self, start = None):
      self.minlon = None
      self.maxlon = None
      self.minlat = None
      self.maxlat = None
      self.warp = False

      if start != None:
         self.put_point(start[0],start[1])
         self.put_point(start[2],start[3])      
      
   def put_point(self,lon,lat):
      if self.minlon == None:
         self.minlon = lon
         self.maxlon = lon
         self.minlat = lat
         self.maxlat = lat
      else:
         if not self.warp:
            if lon - self.minlon > 200:
               self.warp = True
               if self.maxlon < 0:
                  self.maxlon += 360
               if self.minlon < 0:
                  self.maxlon += 360
         if self.warp:
            if lon<0:
               lon += 360
         self.minlon = min(self.minlon,lon)
         self.maxlon = max(self.maxlon,lon)
         self.minlat = min(self.minlat,lat)
         self.maxlat = max(self.maxlat,lat)
         
   def enlarge(self,faktor):
      flon = (self.maxlon - self.minlon) * (faktor - 1.0)
      flat = (self.maxlat - self.minlat) * (faktor - 1.0)
      self.minlon -= flon
      self.minlat -= flat
      self.maxlon += flon
      self.maxlat += flat
      
   def getbox(self):
      mx = self.maxlon
      if self.warp:
         mx -= 360
         
      return [self.minlon,self.minlat,mx,self.maxlat]

   def isinbox(self,lon,lat):
      if self.warp and lon < 0 : lon += 360
      return (self.minlon <= lon) and (lon <= self.maxlon) and \
             (self.minlat <= lat) and (lat <= self.maxlat)
 

####### selection frame
# Note: The selection frame is not an "infoelement" (see below)
#       It is drawn DIRECTLY on the screen and NOT buffered in a pixmap
class selectionframe:
   def __init__(self,drawarea):
      self.drawarea = drawarea
      self.window = drawarea.widget.window
      self.startx = None
      self.endx = None

   def framestart(self,x,y):
      self.startx = int(x)
      self.starty = int(y)
      self.endx = None
      
   def drawit(self):
      x1,y1,x2,y2 = normalizebox_xy(self.startx,self.starty,self.endx,self.endy)
      self.window.draw_rectangle(self.drawarea.gc_selframe, False, x1,y1,x2-x1+1, y2-y1+1)
      
   def framemove(self,x,y):
      if self.startx == None:
         return

      if self.endx != None:
         self.drawit()
         
      self.endx = x
      self.endy = y

      self.drawit()      

   def frameend(self):
      if self.endx == None:
         return None

      self.drawit()         
      
      res = normalizebox_xy(self.startx, self.starty, self.endx, self.endy)
      
      self.startx = None
      self.endx = None
      return res     

class unreliable_trackpoint:
   """ This class tries to guess, if a point of a track makes sense
       Current algorithm: check if the accelearation is below a threshold
   """
   def __init__(self,threshold):
      self.warnthreshold = threshold
      self.reset()
      
   def reset(self):
      """ call at the begin of every new segment
      """
      self.insegmentcount = 0
   
   def put_point(self,lon,lat,time):
      if time == None:        # no time (e.g. OSM tracks) / no check
         self.reset()
         return False
         
      unreliable = False
      self.insegmentcount += 1
      v = 0
      if self.insegmentcount > 1:
         dt = (time-self.ltime).seconds
         if dt != 0:
            ds = distance(self.llon,self.llat,lon,lat)
            v = ds / dt
      if self.insegmentcount > 2:
         v2 = 0
         if dt != 0:
            v2 = (v - self.lv) / dt
            if abs(v2)>self.warnthreshold:
               unreliable = True
      self.llon = lon
      self.llat = lat
      self.ltime = time
      self.lv = v
      return unreliable

#################### CLASS INFOELEMENT #####################
# Abstract class, that shows what a child has to implement 
class infoelement:
   def __init__(self,drawarea):
      self.drawarea = drawarea
      self.data = None
      self.visible = True
      self.recalcwaiting = False
   
   def readdata(self,fil):
      pass

   def recalcdata(self):
      if not self.must_i_recalc(): return                   # do this in child classes as well
      
   def draw(self, box = None, refresh = True):
      if not self.drawarea.screencoordsset: return          # do this in child classes as well
      pass
      
   def isvisible(self):
      return (self.data != None) and self.visible
      
   def set_visible(self,visible):
      needsrecalc = not self.visible and visible and self.recalcwaiting      
      self.visible = visible
      if needsrecalc: 
         self.recalcdata()

   def must_i_recalc(self):
      if not self.drawarea.screencoordsset: 
         return False
      if not self.visible: 
         self.recalcwaiting = True
         return False
      self.recalcwaiting = False
      return True

      
####### local gpx track
class localgpxtrack(infoelement):
   def __init__(self,drawarea, threshold):
      infoelement.__init__(self,drawarea)
      self.trackpointindex = []
      self.waypointindex = []
      self.warnthreshold = threshold

   def isvisible(self):
      return self.trackpointindex != [] and self.visible

   def readdata(self,fil):
      self.data = xml.dom.minidom.parse(fil)
      self.root = self.data.getElementsByTagName("gpx")[0]
      assert self.root != None

       # create indices
          
      self.waypointindex = []
      self.waypointcount = 0
      nodelist = self.root.getElementsByTagName("wpt")
      for node in nodelist:
         self.waypointcount += 1
         lon = float(node.getAttribute("lon"))
         lat = float(node.getAttribute("lat"))
         try:
            name = getChildValue(node,"name")
         except ValueError:
            name = None
         self.waypointindex.append([lon,lat,node,None,None,name])
      
      self.trackpointindex = []
      self.trackpointcount = 0
      
      unreliablecount = 0
      unreliablecheck = unreliable_trackpoint(self.warnthreshold)
      
      tracklist = self.root.getElementsByTagName("trk")
      for track in tracklist:
         tracksegmentlist = track.getElementsByTagName("trkseg")
         for tracksegment in tracksegmentlist:
            unreliablecheck.reset()
            first = True
            trackpointlist = tracksegment.getElementsByTagName("trkpt")
            for trackpoint in trackpointlist:
               self.trackpointcount += 1
               lon = float(trackpoint.getAttribute("lon"))
               lat = float(trackpoint.getAttribute("lat"))
               try:
                  time = decode_time(getChildValue(trackpoint,"time"))
               except ValueError:
                  time = None
                  
               unreliable = unreliablecheck.put_point(lon,lat,time)
               if unreliable:
                  unreliablecount += 1
               # 0 = lon, 1 = lat, 2 = Node, 3 = x, 4 = y, 5 = first in segment, 6 = selected, 7 = unreliable
               self.trackpointindex.append([lon,lat,trackpoint,None,None,first, False, unreliable])
               first = False
               
      return _("way points: %s\ntrack points: %s\nunreliable points: %s") % (self.waypointcount, self.trackpointcount, unreliablecount)
    
   def add_helper_track(self,fnm,box):
      # A helper track is a gpx track which is added to the normal (first) one. 
      # They are clipped to the "box" area.
      # 
      # Helper tracks are stored in the index only, not as XML
      #
      fdata = xml.dom.minidom.parse(fnm)
      root = fdata.getElementsByTagName("gpx")[0]
      assert root != None
      
      unreliablecheck = unreliable_trackpoint(self.warnthreshold)
      newpoints = 0
      tracklist = root.getElementsByTagName("trk")
      for track in tracklist:
         tracksegmentlist = track.getElementsByTagName("trkseg")
         for tracksegment in tracksegmentlist:
            unreliablecheck.reset()
            first = True
            trackpointlist = tracksegment.getElementsByTagName("trkpt")
            for trackpoint in trackpointlist:
               lon = float(trackpoint.getAttribute("lon"))
               lat = float(trackpoint.getAttribute("lat"))
               time = decode_time(getChildValue(trackpoint,"time"))
               
               unreliable = unreliablecheck.put_point(lon,lat,time)
               if box.isinbox(lon,lat):
                  # 0 = lon, 1 = lat, 2 = Node, 3 = x, 4 = y, 5 = first in segment, 6 = selected, 7 = unreliable
                  self.trackpointindex.append([lon,lat,None,None,None,first, False, unreliable])
                  first = False
                  newpoints += 1
               else: # skip point, next one will be a "first" one again
                  first = True
      return newpoints
      
   def minmax(self):
      b = box_deg()
      for poi in self.trackpointindex:
         b.put_point(poi[0],poi[1])
      return b.getbox()
      
   def recalcdata(self):
      if not self.must_i_recalc(): return

      for n in self.trackpointindex:
         n[3], n[4] = self.drawarea.transform2xy(n[0], n[1])
      for n in self.waypointindex:
         n[3], n[4] = self.drawarea.transform2xy(n[0], n[1])

   def draw(self, box = None, refresh = True):
      if not self.drawarea.screencoordsset: return

      if self.trackpointindex == []:
         return
      
      if box == None:
         b = box_xy((0,0,self.drawarea.da_width,self.drawarea.da_height))
         b.enlarge(2.0)
      else:
         b = box_xy(box)
      
      first = True   
      for n in self.trackpointindex:
         if b.isinbox(n[3],n[4]):
            if n[6]:                         # selected
               color = self.drawarea.green
            else:
               if n[7]:                      # unreliable
                  color = self.drawarea.blue
               else:
                  color = self.drawarea.red
            self.drawarea.drawcircle(n[3],n[4],4,color,False)
            
            if not n[5] and not first:     # first in line
               if ls or n[6]:              # line would go away if EITHER point is selected
                  color = self.drawarea.green
               else:
                  if ld or n[7]:           # unreliable
                     color = self.drawarea.blue
                  else:
                     color = self.drawarea.red
#               print lx,ly,n[3],n[4]
               self.drawarea.drawline(lx,ly,n[3],n[4],color,1,arrowtype = 2)
            lx = n[3]
            ly = n[4]
            ls = n[6]
            ld = n[7]
            first = False
         else:
            first = True

      for n in self.waypointindex:
         if b.isinbox(n[3],n[4]):
            self.drawarea.drawwaypoint(n,color = self.drawarea.red,refresh = False)
      
      if refresh: 
         self.drawarea.refresh()

   def select_nodes_xy(self,box,union = True):
      b = box_xy(box)
      for n in self.trackpointindex:
         if n[2]:                         # only real track, not helper track
            if union:
               if b.isinbox(n[3],n[4]):
                  n[6] = True
            else:
               n[6] = b.isinbox(n[3],n[4])
         
   def unselect_all_nodes(self):
      for n in self.trackpointindex:
         n[6] = False
   
   def remove_selected_nodes(self):
      # delete selected nodes from trackpointindex
      #
      # Note: The entire track is still in memory in from of the XML tree
      newtrack = []
      setfirst = False
      for n in self.trackpointindex: 
         if n[6]:
            setfirst = True                   # this node is gone, mark next point as first in line
         else:
            if setfirst: n[5] = True
            newtrack.append(n)
            setfirst = False
      self.trackpointindex = newtrack   

   def remove_unselected_nodes(self):
      # delete unselected nodes from trackpointindex
      #
      # Note: The entire track is still in memory in from of the XML tree
      newtrack = []
      setfirst = False
      for n in self.trackpointindex: 
         if n[2]:                             # only "real" nodes
            if not n[6]:
               setfirst = True                # this node is gone, mark next point as first in line
            else:
               if setfirst: n[5] = True
               newtrack.append(n)
               setfirst = False
         else:
            setfirst = True                  # next real node is first, if there is a next real node
      self.trackpointindex = newtrack   

   def get_modified_gpx_track(self):
      # creates an XML representation of the modified gpx track
      # (modification (deletions) are stored in index only)
      # Note: helper tracks are stored with Node = None. They are not saved.
      data = """<?xml version="1.0" ?>
<gpx creator="pypsmeditor" version="%s" xmlns="http://www.topografix.com/GPX/1/0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd">
""" % (pyosmeditorversion,)
      
      segmentopen = False
      for n in self.trackpointindex:
         if n[2]:     # node not None
            if n[5]:     # first in line
               if segmentopen:
                  data += "</trkseg></trk>\n"
               data += "<trk><trkseg>\n"
               segmentopen = True
            data += n[2].toxml()
            
      if segmentopen:
         data += "</trkseg></trk>\n"
      data += "</gpx>\n"
      return data
   
### remote gpx track

class remotegpxtrack(localgpxtrack):

   def draw(self, box = None, refresh = True):
      if not self.drawarea.screencoordsset: return
      
      if self.data == None:
         returnw
      
      if box == None:
         b = box_xy((0,0,self.drawarea.da_width,self.drawarea.da_height))
         b.enlarge(2.0)
      else:
         b = box_xy(box)
      
      for n in self.trackpointindex:
         if b.isinbox(n[3],n[4]):
            self.drawarea.drawcircle(n[3],n[4],4,self.drawarea.green,False)
      
      if refresh: 
         self.drawarea.refresh()

### selected items

class drawselected(infoelement):
   def __init__(self,drawarea,selectedhandler):
      infoelement.__init__(self,drawarea)
      self.drawarea = drawarea
      self.data = selectedhandler
         
   def draw(self, box = None, refresh = True):
      if not self.drawarea.screencoordsset: return          # do this in child classes as well
      self.data.draw_selected(box,refresh = False)
      if refresh:
         self.drawarea.refresh()
      

### nodes, segments, ways

class osmdata(infoelement):
   # Desription of node/segment/way indices
   #
   # nodeindex:
   # 0 = longitude
   # 1 = latidtude
   # 2 = pointer to XML node
   # 3 = x    x/y will be changed when moving a node and must be recalculated each time
   # 4 = y    the display is changed (zoom/move)
   #
   # segmentindex:
   # 0 = id of "from" node
   # 1 = id of "to" node
   # 2 = pointer to XML node
   #
   # wayindex:
   # 0 = pointer to XML node
   # 1 = list of segments

   def __init__(self,drawarea):
      infoelement.__init__(self,drawarea)
      self.waysvisible = True
      self.nextownid = -1
      
   def get_next_own_id(self):
      self.nextownid -= 1
      return self.nextownid + 1

   def create_segment_index(self,root):
      segmentindex = {}
      segcount = 0
      segerror = 0
      nodelist = root.getElementsByTagName("segment")
      for node in nodelist:
         id = int(node.getAttribute("id"))
         segcount += 1
         fromnode = int(node.getAttribute("from"))
         tonode = int(node.getAttribute("to"))
         segmentindex[id] = [fromnode, tonode, node]
         if tonode == fromnode:
            segerror += 1      
      
      return segmentindex, segcount, segerror

   def create_way_or_area_index(self,name,root):
      """ name: "way" or "area"
          root: root of the XML tree
          
          result: dictionary: key: id of way/area
                              value: [ pointer to node, array of segment ids ]
      """
      wayindex = {}
      waycount = 0
      nodelist = root.getElementsByTagName(name)
      for node in nodelist:
         id = int(node.getAttribute("id"))
         wayindex[id] = [node]
         waysegs = []
         waycount += 1
         seglist = node.getElementsByTagName("seg")
         for seg in seglist:
            sid = int(seg.getAttribute("id"))
            waysegs.append(sid)
         wayindex[id].append(waysegs)

      return wayindex, waycount

   def switch_segment_orientation(self,id):
      f = self.segmentindex[id][0]
      t = self.segmentindex[id][1]
      n = self.get_segment_pointer(id)
      n.setAttribute("from",str(t))
      n.setAttribute("to",str(f))
      self.segmentindex[id][0] = t
      self.segmentindex[id][1] = f
   
   def get_segment_from_list_with_node(self, searchnode, seglist, maxallowed = 1, remove = True):
      # seaches the segments in seglist for one with the "searchnode" as from or to node
      # removes this node from the list if remove == True
      # fails, if it found more than maxallowed segemtns that meet this criteria
      
      if searchnode == None: return None
      
      hits = []
      otherid = None
      for seg in seglist:
         try:
            fr = self.segmentindex[seg][0]
            to = self.segmentindex[seg][1]
            
            if fr == searchnode:
               otherid = to
               hits.append(seg)
            if to == searchnode:
               otherid = fr
               hits.append(seg)
            
         except KeyError:    # segment not visible
            pass

      if len(hits) == 0:
         return None
      if len(hits) > maxallowed: 
         return None
      if len(hits) > 1:
         otherid = None
      if remove:
         for s in hits:
            seglist.remove(s)
      return (hits, otherid, seglist)      
   
   def is_regular_endpoint(self,id,list):
      # an endpoint may only appear once in all segments
      if list == []: return False
      
      # check how often node "id" appears in all segments
      cnt = 0
      for seg in list:
         try:
            s = self.segmentindex[seg]
         except KeyError:    # segment not visible
            s = None
         if s:
            if s[0] == id: cnt += 1
            if s[1] == id: cnt += 1
         if cnt>1: return False
      return True          
   
   
   def add_segments_to_way(self,id,idlist):
      # This subroutine adds new segments to an exiting way.
      # It will try to add the segments in a way that is consistent
      # with the existing segments of the way.
      # It first checks, if the new segments have to be added to the beginning or the
      # end of the existing way, and will then add the new segments according to their
      # from/to nodes.
      # If it fails at any point, the segments will simply appended.
      
      action = 0                         # 0 = append, 1 = append with check, 2 = prepend with check
      wayseglist = self.wayindex[id][1]  # the segments of the current way
      firstid = None                     # ID of the common node (!) between new and old segments

      # check the first node of the way
      ok = False
      found = False
      try:
         fr = self.segmentindex[wayseglist[0]][0]
         if not self.is_regular_endpoint(fr,wayseglist): fr = None
         to = self.segmentindex[wayseglist[0]][1]
         if not self.is_regular_endpoint(to,wayseglist): to = None
         ok = True
      except KeyError:               # This segment is not visible -> user could not have appended to it
         pass
            
      if ok:        # info for segment was found, i.e we can test it
         if self.get_segment_from_list_with_node(fr,idlist, maxallowed = 1, remove = False) != None:
            action = 2    # one of the new segments has the from-node of the first segment
            firstid = fr
            found = True
         elif self.get_segment_from_list_with_node(to,idlist, maxallowed = 1, remove = False) != None:
            action = 2    # one of the new segments has the to-node of the first segment
            firstid = to
            found = True
               
      if not found:
         # nothing found in the first segment of the way, let's test the last segment
         ok = False
         found = False
         try:
            fr = self.segmentindex[wayseglist[-1]][0]    # last in list
            if not self.is_regular_endpoint(fr,wayseglist): fr = None
            to = self.segmentindex[wayseglist[-1]][1]
            if not self.is_regular_endpoint(to,wayseglist): to = None
            ok = True
         except KeyError:           # This segment is not visible 
            pass

         if ok: 
            if self.get_segment_from_list_with_node(fr,idlist, maxallowed = 1, remove = False) != None:
               firstid = fr
               action = 1    # one of the new segments has the from-node of the last segment
               found = True
            elif self.get_segment_from_list_with_node(to,idlist, maxallowed = 1, remove = False) != None:
               firstid = to
               action = 1    # one of the new segments has the to-node of the last segment
               found = True
      
      n = self.get_way_pointer(id)                       # pointer to the way
         
      if firstid != None:    # we found common ground, let's try to chain the segments
         first_seg_node = n.getElementsByTagName("seg")[0]   # pointer to first segment in way

         while idlist != []:
            res = self.get_segment_from_list_with_node(firstid,idlist, maxallowed = 1, remove = True)
            # res == None: id not found
            # else: res[0]: tuple with IDs of the segments with the requested node id
            #       res[1]: ID of the second node of the returned segment (if maxallowed == 1)
            #       res[2]: segment list without the segment res[0]
            if res == None: break
            
            idlist = res[2]
            if action == 1:
               w = self.data.createElement("seg")
               w.setAttribute("id",str(res[0][0]))
               n.appendChild(w)
            if action == 2:
               w = self.data.createElement("seg")
               w.setAttribute("id",str(res[0][0]))
               n.insertBefore(w,first_seg_node)
               first_seg_node = w
            firstid = res[1]
               
      # add anything that remains at the end without any tests     
      for sid in idlist:
         w = self.data.createElement("seg")
         w.setAttribute("id",str(sid))
         n.appendChild(w)
      
      # after manipulating the XML tree, update the index (including seg list index)
      self.update_index(n)
      
   ###################################

   def readdata(self,fil):
      """ fil: filename or open file descriptor to XML file
      """
      
      self.data = xml.dom.minidom.parse(fil)
      self.root = self.data.getElementsByTagName("osm")[0]
      assert self.root != None
      
      # create indices
      
      self.nodeindex = {}
      self.nodecount = 0
      nodelist = self.root.getElementsByTagName("node")
      for node in nodelist:
         self.nodecount += 1
         id = int(node.getAttribute("id"))
         lon = float(node.getAttribute("lon"))
         lat = float(node.getAttribute("lat"))
         self.nodeindex[id] = [lon,lat,node,None,None]            

      self.segmentindex, self.segcount, self.segerror = self.create_segment_index(self.root)
      self.wayindex, self.waycount = self.create_way_or_area_index("way",self.root)
         
      return _("Nodes: %s\nSegments: %s\nErrors: %s\nWays: %s") % (self.nodecount,self.segcount,self.segerror, self.waycount)

   def recalcdata(self):
      if not self.must_i_recalc(): return
      
      if self.data != None:
         for n in self.nodeindex:
            self.nodeindex[n][3], self.nodeindex[n][4] = self.drawarea.transform2xy(self.nodeindex[n][0], self.nodeindex[n][1])

   def draw(self, box = None, refresh = True):
      if not self.drawarea.screencoordsset: return
      if self.data == None:
         return
      
      if box == None:
         b = box_xy((0,0,self.drawarea.da_width,self.drawarea.da_height))
         b.enlarge(2.0)
      else:
         b = box_xy(box)
    
      for node in self.nodeindex:
         if b.isinbox(self.nodeindex[node][3],self.nodeindex[node][4]):
            self.drawarea.drawnode(node)

      for node in self.segmentindex:
         fromnode = self.segmentindex[node][0]
         tonode = self.segmentindex[node][1]
                  
         if b.isinbox(self.nodeindex[fromnode][3],self.nodeindex[fromnode][4]):
            if b.isinbox(self.nodeindex[tonode][3],self.nodeindex[tonode][4]):
               self.drawarea.drawsegment(node, refresh = False, arrowtype = 1)

      if self.waysvisible:
         for node in self.wayindex:
            segmentlist = self.wayindex[node][1]
            for segment in segmentlist:
               try:
                  fromnode = self.segmentindex[segment][0]
                  tonode = self.segmentindex[segment][1]
               except KeyError:
#                  print "Unknown way (%s) segment: %s" % (node, segment)
                  continue
                  
               if b.isinbox(self.nodeindex[fromnode][3],self.nodeindex[fromnode][4]) or \
                  b.isinbox(self.nodeindex[tonode][3],self.nodeindex[tonode][4]):
                  self.drawarea.drawsegment(segment,color = self.drawarea.brown, linewidth = 4, refresh = False, arrowtype = 0)
      if refresh:
         self.drawarea.refresh()
         
   def get_node_xy(self,id):
      return (self.nodeindex[id][3],self.nodeindex[id][4])

   def set_ways_visible(self,setto,drawallroutine):
      if setto == self.waysvisible: return
      self.waysvisible = setto
      drawallroutine()

   def findconnectingsegments(self,nodeid):
      res = []
      res2 = []
      for seg in self.segmentindex:
         if (self.segmentindex[seg][0] == nodeid) or (self.segmentindex[seg][1] == nodeid):            
            if not seg in res:
               res.append(seg)
      return res

   def statistic_nodes_from_segments(self,seglist):
      # stat 0: nodes with exactly one occurence, 1: with more than 2, 2: dict with results
      counter = {}
      for seg in seglist:
         try:
            s = self.segmentindex[seg]
         except KeyError:    # info for this segment not in database
            s = None
         
         if s:
            try:
               counter[s[0]] += 1
            except KeyError:
               counter[s[0]] = 1
            try:
               counter[s[1]] += 1
            except KeyError:
               counter[s[1]] = 1

      ones = []
      mores = []
      for d in counter:
         if counter[d] == 1:
            ones.append(d)
         elif counter[d] > 2:
            mores.append(d)
         else:
            pass
      return (ones, mores, counter)
        
   def find_ways_with_segment(self,seg):
      ways = []      
      for way in self.wayindex:
         try:
            x = self.wayindex[way][1].index(seg)
            ways.append(way)
         except ValueError:
            pass
      return ways

   def find_segments_with_node(self,node):
      segments = []      
      for segment in self.segmentindex:
         if self.segmentindex[segment][0] == node or self.segmentindex[segment][1] == node:
            segments.append(segment)
      return segments

   def allowed_to_be_deleted(self,od):
      odstr = od[0]
      odnode = od[1]
      odid = int(odnode.getAttribute("id"))
      odname = odnode.nodeName
      
      objection = None
      if odname == "node":
         g = self.find_segments_with_node(odid)
         if len(g) > 0:
            objection = "Node %s found in %s segments\nDelete the segments if you want to delete the node." % (odid,len(g))
      elif odname == "segment":
         g = self.find_ways_with_segment(odid)
         if len(g) > 0:
            objection = "Segment %s is part of %s ways.\nDelete the ways before you delete the segment." % (odid,len(g))
      elif odname == "way":
         pass
      else:
         objection = "Unknown element"
      return objection
               
   def delete_element(self,od):
      odstr = od[0]
      odnode = od[1]
      odid = int(odnode.getAttribute("id"))
      odname = odnode.nodeName
      
      self.root.removeChild(od[1])      
      if odname == "node":
         del self.nodeindex[odid]
      elif odname == "segment":
         del self.segmentindex[odid]
      elif odname == "way":
         del self.wayindex[odid]
      else:
         raise ValueError,"Unknown element: %s" % (odname,)

   def segment_already_exists(self,id1,id2):
      for seg in self.segmentindex:
         fromid = self.segmentindex[seg][0]
         toid = self.segmentindex[seg][1]
         if ((id1 == fromid) and (id2 == toid)) or ((id1 == toid) and (id2 == fromid)):
            return True
      return False      

   def update_index(self,node):
      x = node.nodeName
      id = int(node.getAttribute("id"))
      
      if x == "node":
         lon = float(node.getAttribute("lon"))
         lat = float(node.getAttribute("lat"))
         px, py = self.drawarea.transform2xy(lon.lat)
         self.nodeindex[id] = [lon,lat,node,px,py]
         return self.nodeindex[id]            
      elif x == "segment":
         id = int(node.getAttribute("id"))
         fromnode = int(node.getAttribute("from"))
         tonode = int(node.getAttribute("to"))
         self.segmentindex[id] = [fromnode, tonode, node]
         return self.segmentindex[id]
      elif x == "way":
         self.wayindex[id] = [node]
         waysegs = []
         seglist = node.getElementsByTagName("seg")
         for seg in seglist:
            sid = int(seg.getAttribute("id"))
            waysegs.append(sid)
         self.wayindex[id].append(waysegs)
         return self.wayindex[id]
      else:
         assert False,"Unknown node type"
                    
   def segments2boundingbox_xy(self,segmentlist):
      b = box_xy()
      for x in segmentlist:
         b.put_point(self.nodeindex[self.segmentindex[x][0]][3],self.nodeindex[self.segmentindex[x][0]][4])
         b.put_point(self.nodeindex[self.segmentindex[x][1]][3],self.nodeindex[self.segmentindex[x][1]][4])
      return b
   
   def minmax_deg(self):
      b = box_deg()
      for poi in self.nodeindex:
         b.put_point(self.nodeindex[poi][0],self.nodeindex[poi][1])
      return b.getbox()

   def get_node_pointer(self,id):
      try:
         return self.nodeindex[id][2]
      except KeyError:
         return None

   def get_segment_pointer(self,id):
      try:
         return self.segmentindex[id][2]
      except KeyError:
         return None

   def get_way_pointer(self,id):
      try:
         return self.wayindex[id][0]
      except KeyError:
         return None

   def update_node_position(self,id,lon,lat):
      self.nodeindex[id][0] = lon
      self.nodeindex[id][1] = lat
      x,y = self.drawarea.transform2xy(lon,lat)
      self.nodeindex[id][3] = x
      self.nodeindex[id][4] = y
      self.nodeindex[id][2].setAttribute("lon",str(lon))
      self.nodeindex[id][2].setAttribute("lat",str(lat))
      try:
         self.nodeindex[id][2].removeAttribute("timestamp")
      except xml.dom.NotFoundErr:
         pass
#      timestampit(self.nodeindex[id][2])
      

   def split_segment(self,old_seg_id,new_node_id,new_seg_id,lon,lat):
      assert False,"obsolete function"
      if self.data == None: raise ValueError,"No OSM context"
      # create new node for the split point
      self.create_new_node(lon,lat,new_node_id)
      
      fromnode = self.segmentindex[old_seg_id][0]
      tonode = self.segmentindex[old_seg_id][1]
      old_seg_node = self.segmentindex[old_seg_id][2]
      
      # create a new segment with the fromnode of the old segment, to the split point
      # and insert it before the old segment
      self.create_new_segment(new_node_id,fromnode,new_seg_id,old_seg_node)
      # set the fromnode of the old segment to the split point
      self.segmentindex[old_seg_id][0] = new_node_id


      # look for ways that contain the old segment
      waylist = self.find_ways_with_segment(old_seg_id)
      for way in waylist:
         waynode = self.wayindex[way][0]
         seglistofway = self.wayindex[way][1]

         if len(seglistofway) == 1:
            # a way with only one segment :(
            seglistofway.append(new_seg_id)
         else:
            pos = seglistofway.index(old_seg_id)
            if pos == 0:
               # edited segment is first in line, 
               # i.e. we can not check the predecessor
               # the additional segment has the old "from id"
               # if the sucessor has it, the order is: old part - additional part - next segment
               #                         otherwise:    additional part - old part - next segment
               if self.segmentindex[seglistofway[pos+1]][0] == fromnode or self.segmentindex[seglistofway[pos+1]][1] == fromnode:
                  seglistofway.insert(pos+1,new_seg_id)
               else:
                  seglistofway.insert(pos,new_seg_id)
            else:
               # this time we check the predecessor
               # if the predecessor has the old "from id", the order is:
               #              predecessor - additional part - old part ....
               # otherwise:   predecessor - old part - additional part ....
               if self.segmentindex[seglistofway[pos-1]][0] == fromnode or self.segmentindex[seglistofway[pos-1]][1] == fromnode:
                  seglistofway.insert(pos,new_seg_id)
               else:
                  seglistofway.insert(pos+1,new_seg_id)
                  
         # at this point, seglistofway contains the updated segment list
         # we now delete the old one in the XML tree and replace it with an updated one
         for seg in waynode.getElementsByTagName("seg"):
            waynode.removeChild(seg)
         for seg in seglistofway:
            ele = self.data.createElement("seg")
            ele.setAttribute("id",seg)
            waynode.appendChild(ele)
            

   def create_new_node(self,lon,lat,id):
      """ create an OSM node in the local data tree
      """
      if self.data == None: raise ValueError,"No OSM context"
      node = self.data.createElement("node")
      node.setAttribute("id",str(id))
      node.setAttribute("lon",str(lon))
      node.setAttribute("lat",str(lat))
      self.root.appendChild(node)
      
      x,y = self.drawarea.transform2xy(lon,lat)
      self.nodeindex[id] = [lon,lat,node,x,y]

   def create_new_segment(self,fromid,toid,segid,insertBeforeNode = None):
      """ create an OSM segment in the local data tree
      """
      if self.data == None: raise ValueError,"No OSM context"
      node = self.data.createElement("segment")
      node.setAttribute("id",str(segid))
      node.setAttribute("from",str(fromid))
      node.setAttribute("to",str(toid))
      self.segmentindex[segid] = [fromid, toid, node]
      if insertBeforeNode != None:
         self.root.insertBefore(node,insertBeforeNode)
      else:
         self.root.appendChild(node)

   def create_new_way(self,sids,id):
      if self.data == None: raise ValueError,"No OSM context"
      node = self.data.createElement("way")
      node.setAttribute("id",str(id))
      
      # OSM server requires at least on tag (s.a. create_new_way in OSMAPI)
      snode = self.data.createElement("tag")
      snode.setAttribute("k","created_by")
      snode.setAttribute("v","pyosmeditor")
      node.appendChild(snode)
      
      for s in sids:
         snode = self.data.createElement("seg")
         snode.setAttribute("id",str(s))
         node.appendChild(snode)
      self.wayindex[id] = [node,sids]

#### CLASS GEOTAGHINTS ####
class geotaghints(infoelement):
   # index:
   # 0 - lon
   # 1 - lat
   # 2 - alt
   # 3 - x
   # 4 - y
   # 5 - fnm (path)
   def __init__(self,drawarea):
      self.drawarea = drawarea
      self.data = None
      self.index = []
      self.visible = True
      self.recalcwaiting = False
      self.iconsize = 10
   
   def readdata(self,fil):       # fil is name of subdir
      self.data = fil            # something not None
      files = os.listdir(fil)
      for fi in files:
         fnm = os.path.join(fil,fi)
         dat = self.get_gps_info(fnm)
         if dat:
            self.index.append([dat[0],dat[1],dat[2],0,0,fnm])

   def recalcdata(self):
      if not self.must_i_recalc(): return                   # do this in child classes as well
      for n in self.index:
            n[3], n[4] = self.drawarea.transform2xy(n[0], n[1])

   def draw(self, box = None, refresh = True):
      if not self.drawarea.screencoordsset: return          # do this in child classes as well

      if box == None:
         b = box_xy((0,0,self.drawarea.da_width,self.drawarea.da_height))
      else:
         b = box_xy(box)
         
      for dat in self.index:
         if b.isinbox(dat[3],dat[4]):
            self.drawarea.drawtriangle(dat[3],dat[4],self.iconsize,refresh = False)
      
      if refresh:
         self.drawarea.refresh()
    
   def find_geohint(self,x,y):
      h = self.iconsize / 2
      b = box_xy((x-h,y-h,x+h,y+h))
      for dat in self.index:
         if b.isinbox(dat[3],dat[4]):
            return dat[5]
      
      return None
      
   def isvisible(self):
      return (self.data != None) and self.visible

   def val_fract(self,s):
      return float(s.num) / float(s.den)

   def val_deg(self,s):
      g = self.val_fract(s[0])
      m = self.val_fract(s[1])
      s = self.val_fract(s[2])
      return g + m / 60.0 + s / 3600.0
   
   def get_gps_info(self,fnm):
      try:
         f=open(fnm, 'rb')
         tags=EXIF.process_file(f)

         lat = tags['GPS GPSLatitude']
         latR = tags['GPS GPSLatitudeRef']
         lon = tags['GPS GPSLongitude']
         lonR = tags['GPS GPSLongitudeRef']
         alt = tags['GPS GPSAltitude']
         altR = tags['GPS GPSAltitudeRef']
      except KeyError:
         return None
      
      lat = self.val_deg(lat.values)
      if latR == "S": lat = -lat
      lon = self.val_deg(lon.values)
      if lonR == "W": lon = -lon
      alt = self.val_fract(alt.values[0])
      if altR == "1": alt = -alt
      return (lon,lat,alt)

      
      

   
#################### CLASS DRAWARROW ####################
class calcarrow:
   # calculating arrowline
   # sin and cos are cached in a lookup table with a resolution of 1 degree
   # (should be sufficient for arrow heads)
   
   arrows = ( ((-10,5),(-10,-5)), \
              ((-10,3),(-10,-3)) ) 

   def __init__(self):
      self.trigolookup = {}
      
   def getsincos(self,x):
      x = int(x)
      try:
         return self.trigolookup[x]
      except KeyError:
         r = math.radians(x)
         sin = math.sin(r)
         cos = math.cos(r)
         self.trigolookup[x] = (sin,cos)
         return (sin,cos)
         
   def docalc(self, p, a):
      s, c = self.getsincos(a)
      x = p[0]*c - p[1]*s
      y = p[0]*s + p[1]*c
      return int(x), int(y)
      
   def calcarrow(self,sx,sy,ex,ey,atype = 0):
      dy = ey-sy
      dx = ex-sx
      
      if dx == 0:
         if dy == 0: raise ZeroDivisionError,"dx and dy are zero"
         if dy > 0: 
            alpha = 90
         else:
            alpha = 270
      else:
         alpha = math.degrees(math.atan2(dy,dx))     # atan2 = clever atan which puts the angle into the right quadrant
            
      dx1, dy1 = self.docalc(self.arrows[atype][0],alpha)
      dx2, dy2 = self.docalc(self.arrows[atype][1],alpha)
      
      return (ex+dx1,ey+dy1,ex+dx2,ey+dy2)
      
#################### CLASS DRAWAREA #####################

class drawarea:
   def __init__(self,widget):
      self.widget = widget
      self.osmdata = None
      self.osm_box = None
      
      x, y, self.da_width, self.da_height = self.widget.get_allocation()
         
      self.pixmap = gtk.gdk.Pixmap(self.widget.window, self.da_width, self.da_height)
    
      self.colormap = self.widget.get_colormap()
      self.white     = self.colormap.alloc_color("#ffffff")
      self.lightgrey = self.colormap.alloc_color("#eeeeee")
      self.black     = self.colormap.alloc_color("#000000")
      self.red       = self.colormap.alloc_color("#f80000")
      self.green     = self.colormap.alloc_color("#00d000")
      self.yellow    = self.colormap.alloc_color("#e0e000")
      self.brown     = self.colormap.alloc_color('#fba208')
      self.blue      = self.colormap.alloc_color('#0000ff')
      self.gc = self.widget.window.new_gc()
      
      # GC for selection frame
      self.gc_selframe = self.widget.window.new_gc()
      self.gc_selframe.set_function(gtk.gdk.INVERT)
      self.gc_selframe.set_foreground(self.black)
      self.gc_selframe.set_line_attributes(2,gtk.gdk.LINE_ON_OFF_DASH,gtk.gdk.CAP_BUTT,gtk.gdk.JOIN_MITER)
      
      # helper class
      self.calcarrow = calcarrow()
      self.arrowthreshold = 20000
      self.arrowvisible = True
      
      self.damagearea = box_xy()
      self.noderadius = 8
      self.waypointradius = 3
      self.cleardrawingarea()
#      self.font_desc = pango.FontDescription('Serif 12')
      
      self.lb_lon = None
      self.lb_lat = None
      self.rt_lon = None
      self.rt_lat = None
      self.screencoordsset = False
 
      
   def set_osm_box(self,box):
      self.osm_box = box
 
   def set_arrow_threshold(self,f):
      self.arrowthreshold = f
 
   def set_arrow_visible(self,flag):
      self.arrowvisible = flag
      
   def resizescreen(self):
      # calling routine must recalc and redraw
      x, y, self.da_width, self.da_height = self.widget.get_allocation()
      self.pixmap = gtk.gdk.Pixmap(self.widget.window, self.da_width, self.da_height)
      self.pixmap.draw_rectangle(self.widget.get_style().white_gc,True, 0, 0, self.da_width, self.da_height)

      if not self.screencoordsset: return
      lon2 = self.lb_lon + self.da_width / self.factlong
      lat1 = self.rt_lat - self.da_height / self.factlat
      
      self.setscreencoords(self.lb_lon,lat1,lon2,self.rt_lat)  
 
   def normlon(self,lon):
      if lon > 180:
        lon -= 360
      if lon < -180:
        lon += 360
      return lon
      
   def zoom(self,fact):    
      lb_lon = self.lb_lon
      lb_lat = self.lb_lat
      rt_lon = self.rt_lon
      rt_lat = self.rt_lat
      difflong = rt_lon - lb_lon
      difflat = rt_lat - lb_lat
      
      newdiff = self.da_width / (self.factlong * fact)
      delta = (difflong - newdiff) / 2

      if self.warpflag:
         lb_lon -= delta
      else:
         lb_lon += delta
      
      lb_lon = self.normlon(lb_lon)
         
      if self.warpflag:
         rt_lon += delta
      else:
         rt_lon -= delta

      rt_lon = self.normlon(rt_lon)         
      
      newdiff = self.da_height / (self.factlat * fact)
      delta = (newdiff - difflat) / 2
      
      lb_lat -= delta
      rt_lat += delta
      
      if lb_lat < -90 or rt_lat > 90:
         return None
      
      if debug: 
        print "von:", self.lb_lon,self.lb_lat,self.rt_lon,self.rt_lat
        print "nach:", lb_lon,lb_lat,rt_lon,rt_lat
        print self.lb_lon,self.lb_lat,self.rt_lon,self.rt_lat
        
      return [lb_lon,lb_lat,rt_lon,rt_lat]

   def move_xy(self,dx,dy):
      dlon = dx / self.factlong
      dlat = dy / self.factlat

      lb_lon = self.normlon(self.lb_lon - dlon)
      lb_lat = self.lb_lat + dlat
      rt_lon = self.normlon(self.rt_lon - dlon)
      rt_lat = self.rt_lat + dlat

      if lb_lat < -90 or rt_lat > 90:
         return None

      return [lb_lon,lb_lat,rt_lon,rt_lat]
              
   def setscreencoords(self,lon1,lat1,lon2,lat2,center = False):
      # data needs to be recalculated after this function
      # 
      # Note: when the main loop calls this function, 
      # check if menu options and buttons must be enabled
      
      self.lb_lon = lon1
      self.lb_lat = lat1
      self.rt_lon = lon2
      self.rt_lat = lat2
      self.screencoordsset = True
      
      self.lb_lon, self.lb_lat, self.rt_lon, self.rt_lat, self.warpflag = normalizebox_deg(lon1,lat1,lon2,lat2)
      difflong = self.rt_lon - self.lb_lon
      if self.warpflag:
         difflong = 360 - difflong
         
      difflat = self.rt_lat - self.lb_lat

      assert difflong != 0
      assert difflat != 0
            
      self.factlong =  self.da_width / difflong
      self.factlat  =  self.da_height / difflat
      
      if self.factlong > self.factlat:
         self.factlong = self.factlat
      else:
         self.factlat = self.factlong

      if center:
         remainlong = (self.da_width  / self.factlong - difflong) / 2.0
         remainlat  = (self.da_height / self.factlat - difflat) / 2.0
         self.lb_lon -= remainlong
         self.lb_lat -= remainlat
         self.rt_lon += remainlong
         self.rt_lat += remainlat
   
   def set_bookmark(self,lon,lat,scale):
      dlon = self.da_width / scale / 2
      dlat = self.da_height / scale / 2

      self.setscreencoords(lon - dlon, lat - dlat, lon + dlon, lat + dlat, True)
      
      
      
   def get_screencoordinates(self):
      return (self.lb_lon,self.lb_lat,self.rt_lon,self.rt_lat)
      
   def get_screenbookmark(self):
      m_lon = (self.lb_lon + self.rt_lon) / 2
      m_lat = (self.lb_lat + self.rt_lat) / 2
      m_fac = self.factlat

      ##TODO: warp flag
      return (m_lon, m_lat, m_fac)
      
   def damage(self,x1,y1,x2,y2):
      """ Enlarge damaged area to include rectangle (x1,y1) (x2,y2)
      """
      dax = max(min(x1,x2),0)
      day = max(min(y1,y2),0)
      dbx = max(x1,x2)
      dby = max(y1,y2)
      
      self.damagearea.putbox(dax,day,dbx,dby)

   def cleardrawingarea(self,color = None, box = None):
     if color == None:
        color1 = self.lightgrey
     if box == None:
        box = (0,0,self.da_width,self.da_height)
      
     x1, y1, x2, y2 = box
     self.gc.set_foreground(color1)
     self.pixmap.draw_rectangle(self.gc, True, x1, y1, x2 - x1 + 1, y2 - y1 + 1)
     
     if self.osm_box:
        # fill area that is covered by OSM data with white background
        lbx, lby = self.transform2xy(self.osm_box[0],self.osm_box[1])
        rtx, rty = self.transform2xy(self.osm_box[2],self.osm_box[3])
        nbx = max(lbx,x1)    # Note: 0/0 of the screen is in the top left corner
        nby = max(rty,y1)    #   but the boxes are defined by the left bottom and top right corner
        ntx = min(rtx,x2)
        nty = min(lby,y2)
     
        if nbx < ntx and nby < nty:
           self.gc.set_foreground(self.white)
           self.pixmap.draw_rectangle(self.gc, True, nbx, nby, ntx - nbx + 1, nty - nby + 1)
     
     self.damage(x1, y1, x2, y2)
   
   def refresh(self,all = False, box = None):
      """ refresh display from pixmap
          all:  True: entire area
                False: damaged area only
      """
      if all:
         self.widget.queue_draw_area(0,0,self.da_width,self.da_height)
      else:
         if not self.damagearea.empty():
            if box == None:
               box = self.damagearea.getbox()
            x1, y1, x2, y2 = box
            self.widget.queue_draw_area(x1,y1,x2-x1+1,y2-y1+1)
      self.damagearea.clear()

   def exposed(self,widget,event):
      x , y, width, height = event.area
      self.widget.window.draw_drawable(widget.get_style().fg_gc[gtk.STATE_NORMAL],self.pixmap, x, y, x, y, width, height)
       
   def drawline(self,x1,y1,x2,y2,color = None, linewidth = 1, refresh = True, arrowtype = 0):
     """ origin = left/top left top
         arrow = arrow at x2,y2
     """
     if color == None:
        color = self.black
     self.gc.set_foreground(color)
     self.gc.line_width = linewidth
     self.pixmap.draw_line(self.gc,x1,y1,x2,y2)
     self.damage(x1-linewidth,y1-linewidth,x2+linewidth,y2+linewidth)

     arrow = (arrowtype > 0) and self.arrowvisible and (self.factlong >= self.arrowthreshold)

     if arrow:
        try:
          ax1, ay1, ax2, ay2 = self.calcarrow.calcarrow(x1,y1,x2,y2,arrowtype-1)
          self.pixmap.draw_polygon(self.gc,True,[(x2,y2),(ax1,ay1), (ax2,ay2)])
          self.damagearea.put_point(ax1,ay1)
          self.damagearea.put_point(ax2,ay2)
        except ZeroDivisionError:    # caused by segments where from == to
          pass    

     if refresh:
        self.refresh()
   
   def drawcircle(self,x,y,r,color = None, refresh = True):
     if color == None:
        color = self.black
     xn = int(x - r/2)
     yn = int(y - r/2)
       
     self.gc.set_foreground(color)
     self.pixmap.draw_arc(self.gc,True,xn,yn,r,r,0,360*64)
     self.damage(xn-r,yn-r,xn+r,yn+r)
     if refresh:
        self.refresh()
        
   def drawrectangle(self,x1,y1,x2,y2,color = None, refresh = True):
     if color == None:
        color = self.black
     self.gc.set_foreground(color)
     self.pixmap.draw_rectangle(self.gc,True,x1,y1,x2-x1+1,y2-y1+1)

     self.damage(x1,y1,x2,y2)
     if refresh:
        self.refresh()

   def drawtriangle(self,x,y,d,color = None, refresh = True):
     if color == None:
        color = self.green
     w = d / 2
     x1 = x - w
     y1 = y - w
     x2 = x + w
     y2 = y + w
     self.gc.set_foreground(color)
     self.pixmap.draw_polygon(self.gc,True,[(x1,y1),(x2,y1), (x,y2)])

     self.damage(x1,y1,x2,y2)
     if refresh:
        self.refresh()
            
    
   def transform2xy(self,lon,lat):
      if self.warpflag and (lon<0):
         lon += 360.0
      lon -= self.lb_lon
      lat -= self.lb_lat
      x = int(lon * self.factlong)
      y = int(lat * self.factlat)
      y = self.da_height - y
      return (x,y)

   def transform2lonlat(self,xy):
      y = self.da_height - xy[1]
      lon = xy[0] / self.factlong
      lat = y / self.factlat
      
      lon += self.lb_lon
      lat += self.lb_lat
      return (lon,lat)
      
   def drawnode(self,id, color = None, refresh = True):
      if color == None:
         color = self.black
      # 0 = lon, 1 = lat, 2 = node, 3 = x, 4 = y
      self.drawcircle(self.osmdata.nodeindex[id][3],self.osmdata.nodeindex[id][4],self.noderadius,color, refresh)
      
   def drawsegment(self,id, color = None, linewidth = 1, refresh = True, arrowtype = 0):
      if color == None:
         color = self.black
      # 0 = from, 1 = to, 2 = node
      n1 = self.osmdata.segmentindex[id][0]
      n2 = self.osmdata.segmentindex[id][1]

      self.drawline(self.osmdata.nodeindex[n1][3],self.osmdata.nodeindex[n1][4],self.osmdata.nodeindex[n2][3],self.osmdata.nodeindex[n2][4],color,linewidth,refresh, arrowtype)

   def drawway(self,id, color = None, linewidth = 4, refresh = True, arrowtype = 0):
      if color == None:
         color = self.brown

      segs = self.osmdata.wayindex[id][1]
      for seg in segs:
         try:
            self.drawsegment(seg,color,linewidth,refresh,arrowtype = arrowtype)
         except KeyError:
            pass

   def drawwaypoint(self,waypoint, color = None, refresh = True):
      TEXT_DX = -10
      TEXT_DY = 10
      
      if color == None:
         color = self.red
      # 0 = lon, 1 = lat, 2 = node, 3 = x, 4 = y, 5 = name

      self.drawrectangle(waypoint[3]-self.waypointradius,waypoint[4]-self.waypointradius,waypoint[3] + self.waypointradius,waypoint[4]+self.waypointradius,color, refresh)
      if waypoint[5]:
           pangolayout = pango.Layout(self.widget.get_pango_context())
           pangolayout.set_text(waypoint[5])
           # optional
#           pangolayout.set_font_description(self.drawarea.font_desc)
           
           text_width, text_height = pangolayout.get_pixel_size()
           self.pixmap.draw_layout(self.gc, waypoint[3]+TEXT_DX, waypoint[4]+TEXT_DY, pangolayout, foreground = color)
           self.damage(waypoint[3]+TEXT_DX,waypoint[4]+TEXT_DY,waypoint[3]+TEXT_DX+text_width,waypoint[4]+TEXT_DY+text_height)
      if refresh:
         self.refresh()

 
      
######################## CLASS OSMAPI ################
     
class osmapi:
   # API doc: http://wiki.openstreetmap.org/index.php/API
   APIVERSION = '0.3'
   

   def __init__(self,sitename,passwordrequest,errorfunction):
      self.osmsite = sitename
      self.passwordrequest = passwordrequest
      self.errorfunction = errorfunction
 
   def httprequest(self,host,cmd,url,body = None):
      ##TODO: cancel pressed -> Exception
      username, password = self.passwordrequest()
      
      headers = {
                'User-Agent': 'pyosmeditor/%s' % (pyosmeditorversion,),
                 'Authorization': "Basic " + (username + ":" + password).encode("base64").rstrip(),
                 'Accept-Encoding': 'gzip'
                 }
                 
      if body != None:
         body = body.decode(localeencoding)
         body = body.encode(DB_ENCODING)
         headers["Content-Length"] = len(body)
    
      conn = httplib.HTTPConnection(host)
      conn.request(cmd,url,headers = headers, body = body)
      response = conn.getresponse()
      if response.status == 200:
         data = response.read()
         if response.getheader("content-encoding") == "gzip":
            data = gzip.GzipFile(fileobj=StringIO.StringIO(data))
         else:
            data = StringIO.StringIO(data)
      else:
         if self.errorfunction:
            self.errorfunction(response.status,response.reason)
         data = None
      response.close()
      conn.close()     
      
      return data, response.status
   
   def apiprefix(self):
      return "/api/%s/" % (self.APIVERSION,)
   
   def getmap(self,bllon,bllat,trlon,trlat):
      url = self.apiprefix() + "map?bbox=%s,%s,%s,%s" % (bllon,bllat,trlon,trlat)
      response, status = self.httprequest(self.osmsite,"GET",url)
      ## TODO: check f.status
      # 200 ok
      # 401 Auth required
      return response
   
   def getelements(self,ids,single,multiple = None):
      if type(ids) == tuple:
         assert multiple != None,"tuple but no name for multiple query"
         liste = ""
         for id in ids:
            if liste == "":
               liste = str(id)
            else:
               liste += "," + str(id)
         url = "%s%s%s" % (self.apiprefix(),multiple,liste)
      else:
         url = "%s%s/%s" % (self.apiprefix(),single,ids)

      response, status = self.httprequest(self.osmsite,"GET",url)
      ## TODO: check f.status
      # 200 ok
      # 401 Auth required
      return response
    
   def getnodes(self,nodes):
      return self.getelements(nodes,"node","nodes?nodes=")
         
   def getway(self,way):
      return self.getelements(way,"way","ways/")
       
   def getsegment(self,segment):
      return self.getelements(segment,"segment")
      
   def ways_for_segment(self,segment):
      url = self.apiprefix() + "segment/%s/ways" % (segment,)
      response, status = self.httprequest(self.osmsite,"GET",url)
      return response

   def areas_for_segment(self,segment):
      url = self.apiprefix() + "segment/%s/areas" % (segment,)
      response, status = self.httprequest(self.osmsite,"GET",url)
      return response
   
   def gettrackpoints(self,bllon,bllat,trlon,trlat,page = 0):
      ## TODO: catch HTTP errors
      url = self.apiprefix() + "trackpoints?bbox=%s,%s,%s,%s&page=%s" % (bllon,bllat,trlon,trlat,page)
      response, status = self.httprequest(self.osmsite,"GET",url)
      ## TODO: check f.status
      # 200 ok
      # 401 Auth required
      return response

   def create_new_node(self,lon,lat):
      body = "<osm version=\"%s\" generator=\"pyosmeditor\"><node lon='%s' lat='%s' id='0'></node></osm>\n" % (self.APIVERSION,lon,lat)
      url = self.apiprefix() + "node/0"
      response, status = self.httprequest(self.osmsite,"PUT",url,body = body)
      if status == 200:
         res = response.read()
         response.close()
         try:
            res = int(res)
            return res
         except ValueError:
            return None
      else:
         return None
      
   def create_new_segment(self,fromid,toid):
      body = "<osm version=\"%s\" generator=\"pyosmeditor\"><segment from='%s' to='%s' id='0'></segment></osm>\n" % (self.APIVERSION,fromid,toid)
      url = self.apiprefix() + "segment/0"
      response, status = self.httprequest(self.osmsite,"PUT",url,body = body)
      if status == 200:
         res = response.read()
         response.close()
         try:
            res = int(res)
            return res
         except ValueError:
            return None
      else:
         return None

   def create_new_way(self,seglist):
      liste = "<tag k='created_by' v='pyosmeditor' />" # dummy tag, as OSM server requires it
      for sid in seglist:
         liste += "<seg id='%s' />\n" % (sid,)
      body = "<osm version='%s'>\n<way id='0'>\n%s</way></osm>\n" % (self.APIVERSION,liste)
      url = self.apiprefix() + "way/0"
      
      response, status = self.httprequest(self.osmsite,"PUT",url,body = body)
      if status == 200:
         res = response.read()
         response.close()
#         print "way response:", res
         try:
            res = int(res)
            return res
         except ValueError:
            return None
      else:
         return None

   def get_ways_of_segment(self,id):
      url = self.apiprefix() + "segment/%s/ways" % (id,)
      response, status = self.httprequest(self.osmsite,"GET",url)
      if status == 200:
         res = response.read()
         response.close()
         return res
      return None
    
   def count_ways_of_segment(self,id):
      data = self.get_ways_of_segment(id)
      if data == None: return None
      doc = xml.dom.minidom.parseString(data)
      root = doc.getElementsByTagName("osm")[0]
      assert root != None
      nodelist = root.getElementsByTagName("way")
      return len(nodelist)

   def get_areas_of_segment(self,id):
      url = self.apiprefix() + "segment/%s/areas" % (id,)
      response, status = self.httprequest(self.osmsite,"GET",url)
      if status == 200:
         res = response.read()
         response.close()
         return res
      return None

   def count_areas_of_segment(self,id):
      data = self.get_areas_of_segment(id)
      if data == None: return None
      doc = xml.dom.minidom.parseString(data)
      root = doc.getElementsByTagName("osm")[0]
      assert root != None
      nodelist = root.getElementsByTagName("area")
      return len(nodelist)

   def delete_element(self,id):
      url = self.apiprefix() + "%s" % (id,)
      response, status = self.httprequest(self.osmsite,"DELETE",url)
      if status == 200:
         res = response.read()
         response.close()
         return True
      return False
      
   def send_updated_data(self,tree):
      # 200 ok
      # ??? BAD REQUEST
      name = tree.nodeName
      id = tree.getAttribute("id")
      url = "%s%s/%s" % (self.apiprefix(),name,id) # e.g. "baseurl/node/123"
      data = tree.toxml() 
      body = "<osm version=\"%s\" generator=\"pyosmeditor\">%s</osm>" % (self.APIVERSION,data)
      
      response, status = self.httprequest(self.osmsite,"PUT",url,body = body)
      if status == 200:
         res = response.read()
         response.close()
         return True
      return False

 
########################## CLASS TAGEDITOR #########################

class tageditor:
   def __init__(self,treeview,button_add,button_del,button_apply,editable):
      assert treeview != None
      self.tree = treeview
      self.doc = None                          # XML root of OSM data
      self.editable = editable

      self.store = gtk.ListStore(str,str)
      self.tree.set_model(self.store)          # replaces glades empty store with ours

      column = gtk.TreeViewColumn("key")
      self.tree.append_column(column)
      self.renderer1 = gtk.CellRendererText()
      self.renderer1.set_property('editable', editable)
      self.renderer1.connect('edited', self.cell_edited, 0)  # user data = no. of column
      column.pack_start(self.renderer1, True)
      column.add_attribute(self.renderer1, 'text', 0)

      column = gtk.TreeViewColumn("value")
      self.tree.append_column(column)
      self.renderer2 = gtk.CellRendererText()
      self.renderer2.set_property('editable', editable)
      self.renderer2.connect('edited', self.cell_edited, 1) # user data = no. of column
      column.pack_start(self.renderer2, True)
      column.add_attribute(self.renderer2, 'text', 1)
           
      self.button_add = button_add
      self.button_del = button_del
      self.button_apply = button_apply
      
      self.datanode = None
      self.dirty = False
      self.setbuttonstatus()
      
   def set_xml_root(self,doc):
      # We need this information do modify the data in the XML tree
      self.doc = doc
      
   def setbuttonstatus(self):
      if self.datanode == None:
         # tree, add, del, apply
         enable = [False,False,False,False]
      else:
         if self.editable:
            (path,column) = self.tree.get_cursor()
            selected = (path != None)
            enable = [True,True,selected,self.dirty]
         else:
            enable = [True,False,False,False]

      if self.tree != None:
         self.tree.set_sensitive(enable[0])
      self.button_add.set_sensitive(enable[1])
      self.button_del.set_sensitive(enable[2])
      self.button_apply.set_sensitive(enable[3])        
         
   def set_editable(self,editable):
      if editable == None: return
      
      self.editable = editable
      self.renderer1.set_property('editable', self.editable)
      self.renderer2.set_property('editable', self.editable)
      self.setbuttonstatus()
   
   def set_tags(self,datanode,editable = None):
      self.datanode = datanode
      self.set_editable(editable)
      
      self.store.clear()
      self.dirty = False
      self.setbuttonstatus()
      if datanode  == None: return
      
      liste = self.datanode.getElementsByTagName("tag")
      for tag in liste:
         k = tag.getAttribute("k")
         v = tag.getAttribute("v")
         self.store.append([k,v])
         
   def addbutton(self):
      self.dirty = True
      self.store.append(["key","value"])
      self.setbuttonstatus()
      
   def delbutton(self):
      self.dirty = True
      (path,column) = self.tree.get_cursor()
      if path == None: return
      iter = self.store.get_iter(path)
      self.store.remove(iter)
      self.setbuttonstatus()
      
   def applybutton(self):
      try:
         self.datanode.removeAttribute("timestamp")
      except xml.dom.NotFoundErr:
         pass
#      timestampit(self.datanode)
      
      liste = self.datanode.getElementsByTagName("tag")
      for tag in liste:
         self.datanode.removeChild(tag)
         
      it = self.store.get_iter_first()
      while it:
         k = self.store.get_value(it,0)
         v = self.store.get_value(it,1)
         tag = self.doc.createElement("tag")
         tag.setAttribute("k",k)
         tag.setAttribute("v",v)
         self.datanode.appendChild(tag)
         
         it = self.store.iter_next(it)
      
      self.dirty = False
      self.setbuttonstatus()
   
   def cell_edited(self,cell, path, new_text, data = None):
      # completion of edit in textcell
      # cell: edited cell, user_data: Nr. of column (see connect command)
      self.dirty = True
      self.store[path][data] = new_text
      self.setbuttonstatus()

   def cursor_changed(self):
      self.setbuttonstatus()

################ CLASS STATUSBARHANDLER ################
class statusbarhandler:
   def __init__(self,widget):
      self.statusbar = widget
      self.lonlat_context_id = self.statusbar.get_context_id("longitude and latitude")
      self.msgid = None
   
   def put(self,txt):
      if self.msgid != None:
         self.statusbar.pop(self.lonlat_context_id)
      self.msgid = self.statusbar.push(self.lonlat_context_id,txt)

################ CLASS TOOLBAR HANDLER ################
class toolbarhandler:

   def __init__(self,osmeditor):
      self.osmeditor = osmeditor
      self.last_mode = ""
      
      self.widgets = (self.osmeditor.xml.get_widget('tb_delete'),
                      self.osmeditor.xml.get_widget('tb_var1'),
                      self.osmeditor.xml.get_widget('tb_var2') )
 
      self.labels = {
         "node":         (_("delete"),None,None),
         "nodes":        (None,_("create\nsegment(s)"),None),
         "segment":      (_("delete"),_("create\nway"),_("from <> to")),
         "segments":     (None,_("create\nway"),_("align\nf->t")),
         "way":          (_("delete"),None,None),
         "way/segments": (None,_("add to\nway"),None)
         }

      self.functions = {
         "node":          (self.osmeditor.tb_delete_element,
                           None,
                           None),
         "nodes":         (None,
                           self.osmeditor.tb_create_segments,
                           None),
         "segment":       (self.osmeditor.tb_delete_element,
                           self.osmeditor.tb_create_way,
                           self.osmeditor.tb_align_from_to),
         "segments":      (None,
                           self.osmeditor.tb_create_way,
                           self.osmeditor.tb_align_from_to),
         "way":           (self.osmeditor.tb_delete_element,
                           None,
                           None),
         "way/segments":  (None,
                           self.osmeditor.tb_add_segments_to_way,
                           None)
         }
      
    
   def set_tb_icons(self,mode):
      if self.osmeditor.tb_mode != 1: mode = ""     # No icons if not in Edit mode
 
      if mode == self.last_mode:
         return
         
      self.last_mode = mode
      
      try:
         labels = self.labels[mode]
      except KeyError:
         labels = (None,None,None)

      for i in range(3):
         if labels[i] == None:
            if i>0:
              self.widgets[i].set_visible_horizontal(False)
            self.widgets[i].set_sensitive(False)
         else:
            if i>0:   # delete icon stays
               self.widgets[i].set_label(labels[i])
            self.widgets[i].set_visible_horizontal(True)
            self.widgets[i].set_sensitive(True)
            
   def call_function(self,no):
      try:
         function = self.functions[self.last_mode][no]
      except KeyError:
         function = None
         
      if function:
         function()


################ CLASS SELECTED_ELEMENTS_HANDLER ###############

class selected_elements_handler:
   def __init__(self,osmeditor):
      self.osmeditor = osmeditor
      self.tageditor = self.osmeditor.tageditor
      self.selected_item_textentry = self.osmeditor.xml.get_widget('selected_item')
      self.notebook = self.osmeditor.xml.get_widget('notebook')
      self.editable = False
      self.selected_elements = []
   
   def clear(self):
      for ele in self.selected_elements:
         ele.draw(self.osmeditor.drawarea, box = None, selected = False)
      self.osmeditor.drawarea.refresh()
      
      self.selected_elements = []
      
      # clear info
      self.selected_item_textentry.set_text("")
      
      # clear tageditor
      self.tageditor.set_tags(None,False)
      # clear infopane
      self.osmeditor.print_to_infopane("")

      self.osmeditor.toolbarhandler.set_tb_icons("")
   
   def get_selected_list(self):
      return self.selected_elements
   
   def get_selected_list_ids(self):
      res = []
      for l in self.selected_elements:
         res.append(l.get_id())
      return res

   def get_count(self):
      return len(self.selected_elements)
      
   def get_type(self):
      if self.selected_elements == []:
         return None
      else:
         return self.selected_elements[0].get_type()
   
   def get_info(self):
      return (len(self.selected_elements), self.get_type())
   
   def already_exists(self,ele):
      info = ele.get_osm_info()
      for lele in self.selected_elements:
         if lele.get_osm_info() == info: return True
      return False
   
   def get_type(self):
      cnt = self.get_count()
      if cnt == 0: return None
      basetype = self.selected_elements[0].get_type()
      if cnt == 1: return basetype
      basetype2 = self.selected_elements[1].get_type()
      if basetype == "node" and basetype2 == "node": return "nodes"           
      if basetype == "segment" and basetype2 == "segment": return "segments"           
      if basetype == "way" and basetype2 == "segment": return "way/segments"
      return None           
       
   def allowed_to_be_added(self,element):
      # node node node Screen 1
      # seg seg seg    Screen 2
      # way seg seg    Screen 3
       
      if element == None: return False
      if self.already_exists(element): return False
      
      cnt = self.get_count()
      if cnt == 0: return True
      basetype = self.selected_elements[0].get_type()
      eletype = element.get_type()
      
      if cnt == 1:
         if eletype == "way": return False
         if basetype == eletype: return True
         if basetype == "way" and eletype == "segment": return True
         return False
        
      basetype2 = self.selected_elements[1].get_type()
      if eletype == basetype2: return True
      return False
            
   def set_element(self,element,editable = False, add = False):
      if add:
         if not self.allowed_to_be_added(element): return
      else:
         self.clear()

      self.selected_elements.append(element)
      if self.get_count()>1: editable = False

      # set info
      cnt = len(self.selected_elements)
      tp = self.get_type()
      assert tp, "unknow combination of elements"
      
      txt = element.get_short_info()
      if cnt > 1:
         txt = "(%s) - last: %s" % (len(self.selected_elements),txt)
         
      self.selected_item_textentry.set_text(txt)
      self.tageditor.set_tags(element.data)
      if cnt > 1:
         txt = ""
         for ele in self.selected_elements:
            txt += ele.get_short_info() + "\n"
      else:
         txt = element.get_long_info()
      self.osmeditor.print_to_infopane(txt)

      if editable != None: self.set_editable(editable)
      element.draw(self.osmeditor.drawarea,selected = True, box = None, refresh = True)
      
      self.osmeditor.toolbarhandler.set_tb_icons(self.get_type())
    
   def set_editable(self,editable):
      if editable == None: return
      self.editable = editable
      self.editable = editable
      self.tageditor.set_editable(editable)
   
   def draw_selected(self,box,refresh):
      for ele in self.selected_elements:
         ele.draw(self.osmeditor.drawarea, box = box, selected = True)
      
   def mode_change(self,id):
      self.set_editable(id==1 and self.get_count() == 1)
      
      
########################## CLASS selectable_element #########################

class selectable_element:
   def __init__(self,data):
      self.data = data
      self.id = self.get_id()

   def get_type(self):
      return ""
      
   def get_id(self):
      return int(self.get_attribute("id"))
      
   def get_attribute(self,attr):
      if self.data == None: return None
      return self.data.getAttribute(attr)
         
   def get_short_info(self):
      return ""
      
   def get_long_info(self):
      return ""
   
   def get_osm_info(self):
      return None
      
   def draw(self,drawarea,selected,box,refresh=True): # box = None -> entire screen, not used yet anyway
      # something
      if refresh:                 # or refresh by the related drawarea function
         drawarea.refresh()
      


class selected_node(selectable_element):
   def get_type(self):
      return "node"
      
   def get_short_info(self):
      return "Node %s" % (self.id,)
      
   def get_long_info(self):
      lon = self.get_attribute("lon")
      lat = self.get_attribute("lat")
      ts = self.get_attribute("timestamp")
      return "Node ID: %s\nlon: %s\nlat: %s\ntime: %s" % (self.id,lon,lat,ts)
      
   def get_osm_info(self):
      return ("node/%s" % (self.id,),self.data)

   def draw(self,drawarea,selected,box,refresh=False):
      color = drawarea.black
      if selected: color = drawarea.green
      drawarea.drawnode(self.id,color,refresh)
            
class selected_segment(selectable_element):
   def get_type(self):
      return "segment"
   
   def get_short_info(self):
      return _("Segment %s") % (self.id,)

   def get_long_info(self):
      fromid = self.get_attribute("from")
      toid = self.get_attribute("to")

      return _("Segment ID: %s\nfrom node #: %s\nto node #: %s") % (self.id, fromid, toid)

   def get_osm_info(self):
      return ("segment/%s" % (self.id,),self.data)

   def draw(self,drawarea,selected,box,refresh=False):
      color = drawarea.black
      if selected: color = drawarea.green
      drawarea.drawsegment(self.id,color,refresh)

class selected_way(selectable_element):
   def get_type(self):
      return "way"
      
   def get_short_info(self):
      return _("Way %s") % (self.id,)

   def get_long_info(self):
      ts = self.get_attribute("timestamp")
      
      text = _("Way ID %s\n%s\n\n") % (self.id,ts)
      
      for ele in self.data.getElementsByTagName("seg"):
         segid = ele.getAttribute("id")
         text += str(segid) + "\n"

      return text

   def get_osm_info(self):
      return ("way/%s" % (self.id,),self.data)

   def draw(self,drawarea,selected,box,refresh=False):
      color = drawarea.brown
      if selected: color = drawarea.green
      drawarea.drawway(self.id,color = color,refresh = refresh, linewidth = 4)

########################## CLASS BOOKMARKS_HANDLER #########################
class bookmarks_handler:
   def __init__(self,editor,bookmarks):
      self.osmeditor = editor
      self.bookmarks = bookmarks
      self.config    = self.osmeditor.config
      
      self.bookmark_window = self.osmeditor.xml.get_widget('bookmark_window')
      self.bookmark_name = self.osmeditor.xml.get_widget('bookmark_name')
      self.bookmark_description = self.osmeditor.xml.get_widget('bookmark_description')     
      self.bookmark_info = self.osmeditor.xml.get_widget('bookmark_info')
      self.bookmark_submenu = self.osmeditor.xml.get_widget('bookmark_select')
      self.bookmark_list = self.osmeditor.xml.get_widget('bookmark_list')

      cell = gtk.CellRendererText()
      col = gtk.TreeViewColumn("bookmark", cell)
      col.set_cell_data_func(cell,self.bookmark_cell_to_str)
      self.bookmark_list.append_column(col)
      self.bookmark_list.set_property("reorderable",True)

      self.update_bookmark_submenu_and_list()
      
      self.currentlon = None
      self.currentlat = None
      self.currentscale = None
      
   def bookmark_cell_to_str(self,column, cell, model, iter):
      bm = model.get_value(iter, 0)
      if bm:
         res = getChildValue(bm,"name")
      else:
         res = ""

      cell.set_property('text', res)
      
   def add_bookmark(self,name,desc,lon,lat,scale):
      bookmark = self.config.doc.createElement("bookmark")
      appendNodeAndText(self.config.doc,bookmark,"name",name)
      appendNodeAndText(self.config.doc,bookmark,"description",desc)
      appendNodeAndText(self.config.doc,bookmark,"lon",lon)
      appendNodeAndText(self.config.doc,bookmark,"lat",lat)
      appendNodeAndText(self.config.doc,bookmark,"scale",scale)
      bm = self.bookmarks.appendChild(bookmark)

      # editor menu
      self.update_bookmark_submenu_and_list()
      self.select_bookmark(bookmark)
      # edit it
      self.show_bookmark_dialog(bookmark)
      return bookmark
      
   def update_bookmark_submenu_and_list(self):
      bml = self.bookmarks.getElementsByTagName("bookmark")

      menu = gtk.Menu()     
      store = gtk.ListStore(object)
      for bm in bml:
         piter = store.append([bm])
         menu_items = gtk.MenuItem(getChildValue(bm,"name"))
         menu_items.connect("activate", self.osmeditor.on_bookmark_selected, bm)   # data of hook: pointer to bookmark
         menu.append(menu_items)
         menu_items.show()
         
      self.bookmark_list.set_model(store)
      self.bookmark_submenu.set_submenu(menu)
         
   def set_info_field(self):
      if self.currentlon == None:
         info = ""
      else:
         info = _("Longitude (E): %s\nLatitude (N): %s\nScale: %s") % (self.currentlon,self.currentlat,self.currentscale)         
      self.bookmark_info.get_buffer().set_text(info)
   
   def bookmark_to_fields(self,bookmark):
      if bookmark == None:
         name = ""
         desc = ""
         info = ""
         self.currentlon = None
         self.currentlat = None
         self.currentscale = None
      else:
         name = getChildValue(bookmark,"name")
         desc = getChildValue(bookmark,"description")
         self.currentlon = getChildValue(bookmark,"lon")
         self.currentlat = getChildValue(bookmark,"lat")
         self.currentscale = getChildValue(bookmark,"scale")
      
      if name == None: name = ""
      if desc == None: desc = ""
      self.bookmark_name.set_text(name)
      self.bookmark_description.get_buffer().set_text(desc)
      self.set_info_field()
      
   def show_bookmark_dialog(self,bookmark):
      self.bookmark_to_fields(bookmark)
      self.select_bookmark(bookmark)
      self.bookmark_window.show_all()
   
   def select_bookmark(self,bookmark):
      selection = self.bookmark_list.get_selection()
      selection.unselect_all()
      if bookmark == None: return
      # search the bookmark in the listview store
      store = self.bookmark_list.get_model()
      iter = store.get_iter_first()
      while iter:
         if store.get_value(iter,0) == bookmark:
             selection.select_iter(iter)
             (path,column) = self.bookmark_list.get_cursor()

             break
         iter = store.iter_next(iter)
   
   def show_edit(self):
      self.show_bookmark_dialog(None)
   
   def get_selected_bookmark(self):
      selection = self.bookmark_list.get_selection()
      if selection == None: return None
      (model, iter) = selection.get_selected()
      return model.get(iter,0)[0]
   
   def cursor_changed(self):
      bm = self.get_selected_bookmark()
      self.current_bookmark = bm 
      self.bookmark_to_fields(bm)
      
   def button_remove(self): 
      bm = self.get_selected_bookmark()
      if bm == None: return
      self.bookmarks.removeChild(bm)
      self.current_bookmark = None
      self.update_bookmark_submenu_and_list()

   def button_apply(self):
      bm = self.get_selected_bookmark()
      if bm == None: return
      buf = self.bookmark_description.get_buffer()
      name = self.bookmark_name.get_text()
      desc = buf.get_text(buf.get_start_iter(),buf.get_end_iter())
      setChildValue(self.config.doc,bm,"name",name)
      setChildValue(self.config.doc,bm,"description",desc)
      setChildValue(self.config.doc,bm,"lon",str(self.currentlon))
      setChildValue(self.config.doc,bm,"lat",str(self.currentlat))
      setChildValue(self.config.doc,bm,"scale",str(self.currentscale))

      self.update_bookmark_submenu_and_list()
      self.bookmark_window.hide()
      return False
      
   def button_current(self):
      bm = self.get_selected_bookmark()
      if bm == None: return
      co = self.osmeditor.drawarea.get_screenbookmark()
      if co:
         self.currentlon, self.currentlat, self.currentscale = co
         self.set_info_field()
         
      return False 
            
   def drag_end(self):
      # order in treeview has changed
      # building new subtree to reflect changes
      
      childlist = self.bookmarks.getElementsByTagName("bookmark")
      for child in childlist: self.bookmarks.removeChild(child)
            
      store = self.bookmark_list.get_model()
      iter = store.get_iter_first()
      while iter:  
         self.bookmarks.appendChild(store.get_value(iter,0))
         iter = store.iter_next(iter)
      self.update_bookmark_submenu_and_list()
   
####################################################################

class picture_viewer_handler:
   def __init__(self,window,drawarea,config,infolabel):
      self.drawarea = drawarea
      self.draw_width, self.draw_height = self.drawarea.size_request()
      self.win = window
      self.unscaled_picture = None
      self.scale = 1.0
      self.draw_pixmap = None   
      self.filename = None
      self.filesize = None      
      self.exifdate = None
      self.infolabel = infolabel
      self.main_config = config
      w = config.root.getElementsByTagName("geotagpics")
      if w:
         self.my_config = w[0]
      else:
         self.my_config = config.doc.createElement("geotagpics")
         config.root.appendChild(self.my_config)
      
   def get_picture_data(self,filename,filesize):
      wl = self.my_config.getElementsByTagName("pictureinfo")
      for w in wl:
         fnm = w.getAttribute("filename")
         fsz = int(w.getAttribute("filesize"))
         if fnm == filename and fsz == filesize:
            try:
               scale = float(getChildValue(w,"scale"))
               offsetx = int(getChildValue(w,"offsetx"))
               offsety = int(getChildValue(w,"offsety"))
            except ValueError:
               return None
            return (offsetx, offsety, scale)
      return None 
  
   def put_picture_data(self,filename,filesize,offsetx,offsety,scale):
      wl = self.my_config.getElementsByTagName("pictureinfo")
      found = None
      for w in wl:
         fnm = w.getAttribute("filename")
         fsz = int(w.getAttribute("filesize"))
         if fnm == filename and fsz == filesize:
            found = w
            break
      if found == None:
         found = self.main_config.doc.createElement("pictureinfo")
         found.setAttribute("filename",filename)
         found.setAttribute("filesize",str(filesize))
         self.my_config.appendChild(found)
      setChildValue(self.main_config.doc,found,"scale",str(scale))
      setChildValue(self.main_config.doc,found,"offsetx",str(offsetx))
      setChildValue(self.main_config.doc,found,"offsety",str(offsety))
            
   def scale_picture(self):
      pw = int(self.pic_width * self.scale)
      ph = int(self.pic_height * self.scale)
       
      self.scale_picbuf = self.unscaled_picture.scale_simple(pw,ph,gtk.gdk.INTERP_BILINEAR)
      self.draw_buffer()
      
   def draw_buffer(self):
      self.put_picture_data(self.filename,self.filesize,self.offsetX,self.offsetY,self.scale)
      info = _("%s - scale %.4f") % (self.filename,self.scale)
      if self.exifdate:
         info += "\n" + self.exifdate
      self.infolabel.set_text(info)
      pw = int(self.pic_width * self.scale)
      ph = int(self.pic_height * self.scale)
      w = min(pw,self.draw_width)
      h = min(ph,self.draw_height)
      sx = 0
      sy = 0
      dx = self.offsetX
      dy = self.offsetY
      if dx < 0:
         sx = -dx
         w = pw + dx
         dx = 0
      if dy < 0:
         sy = -dy
         h = ph + dy
         dy = 0

#      print sx,sy,dx,dy,w,h,self.draw_width,self.draw_height

      self.draw_pixmap.draw_rectangle(self.drawarea.get_style().white_gc,True, 0, 0, self.draw_width, self.draw_height)
      self.draw_pixmap.draw_pixbuf(None,self.scale_picbuf,sx,sy,dx,dy,w,h)
      self.drawarea.window.draw_drawable(self.drawarea.get_style().fg_gc[gtk.STATE_NORMAL],self.draw_pixmap, 0,0,0,0,self.draw_width,self.draw_height)    
      
      self.last_pw = pw
      self.last_ph = ph
   
   def load_image(self, fnm):
      self.win.show()
      
      self.unscaled_picture = gtk.gdk.pixbuf_new_from_file(fnm)
      self.filename = os.path.basename(fnm)
      self.filesize = filesize(fnm)
      self.exifdate = exifdate(fnm)
      self.pic_width = self.unscaled_picture.get_width()
      self.pic_height = self.unscaled_picture.get_height()
      self.last_pw = None
      self.last_ph = None
      dt = self.get_picture_data(self.filename,self.filesize)
      if dt:
         self.offsetX = dt[0]
         self.offsetY = dt[1]
         self.scale = dt[2]
         self.scale_picture()
         self.draw_buffer()
      else:
         self.fit()
      self.win.window.raise_()
      
   def exposed(self,widget,event):
      if self.draw_pixmap:
         x , y, width, height = event.area
         self.drawarea.window.draw_drawable(widget.get_style().fg_gc[gtk.STATE_NORMAL],self.draw_pixmap, x, y, x, y, width, height)
         
   def configure(self,widget,event):
      x, y, self.draw_width, self.draw_height = self.drawarea.get_allocation()
      self.draw_pixmap = gtk.gdk.Pixmap(widget.window, self.draw_width, self.draw_height)
      self.draw_pixmap.draw_rectangle(widget.get_style().white_gc,True, 0, 0, self.draw_width, self.draw_height)
      self.scale_picbuf = gtk.gdk.Pixbuf(gtk.gdk.COLORSPACE_RGB,False,8,self.draw_width,self.draw_height)
      if self.filename:          # only if picture is already loaded
         self.scale_picture()
         self.draw_buffer()

   def rescale(self,fact):
      self.scale *= fact
      nw = int(self.pic_width * self.scale)
      nh = int(self.pic_height * self.scale)
      
      if self.last_pw:
         h2 = self.draw_width / 2
         self.offsetX = int(h2 + ((self.offsetX - h2) * nw / self.last_pw))

      if self.last_ph:
         h2 = self.draw_height / 2
         self.offsetY = int(h2 + ((self.offsetY - h2)*nh / self.last_ph))

      self.scale_picture()
   
   def zoom_in(self):    self.rescale(1.5)
   def zoom_out(self):   self.rescale(1 / 1.5)
      
   def fit(self):
      assert self.pic_width != 0
      assert self.pic_height != 0
      self.scale = min(float(self.draw_width) / float(self.pic_width), float(self.draw_height) / float(self.pic_height))
      pw = int(self.pic_width * self.scale)
      ph = int(self.pic_height * self.scale)

      self.offsetX = int((self.draw_width - pw) / 2)
      self.offsetY = int((self.draw_height - ph) / 2)
      self.last_pw = pw
      self.last_ph = ph
      self.scale_picture()

   def mouse(self,pressed,widget,event):
      if event.button == 1:
         if pressed:
            self.pressX = event.x
            self.pressY = event.y            
         else:
            dx = int(event.x - self.pressX)
            dy = int(event.y - self.pressY)
            
            if dx != 0 or dy != 0:
               self.offsetX += dx
               self.offsetY += dy
               self.draw_buffer()

########################## CLASS PREFERENCES_HANDLER #########################               
#
# Descripiton of the template structure
#
# Position      
# ..._WIDGETNAME   Name of the widget(s) in the GUI (e.g. one name for TextEntry,
#                      multiple names for Radio buttons)
# ..._DATA_OUT     when data is retrieved from the widget, this function will be
#                      used to convert them back, if an exception occurs the data will not
#                      be written back (e.g. float, int)
# ..._DATA_IN      data source type, see PREF_xxx
# ..._DATA         the actual data source, a dict or a pointer to an XML tree
# ..._DATA_PARAM1  parameter for data source, e.g. name of key in dict, name of XML node, etc.
PREF_TEMPL_WIDGETNAME = 0
PREF_TEMPL_DATA_OUT = 1
PREF_TEMPL_DATA_IN = 2
PREF_TEMPL_DATA = 3
PREF_TEMPL_DATA_PARAM1 = 4

PREF_DICT = 1

class preferences_handler:
   def __init__(self,win,xml,pageswitcher,pageswitcherpane,tabs,data_transfer_template):
      self.window = win
      self.xml = xml       # The widget tree
      self.data_transfer_template = data_transfer_template
      self.page_switcher = pageswitcher
      self.page_switcher_pane = pageswitcherpane
      self.tabs = tabs
   
      # Pageswitcher-ListStore
      liststore = gtk.ListStore(str)
      piter = liststore.append([_('OSM server')])
      piter = liststore.append([_('Display settings')])

      cell = gtk.CellRendererText() 
      column = gtk.TreeViewColumn('section',cell,text = 0) 
      self.page_switcher.append_column(column)
      self.page_switcher.set_model(liststore)
      self.switch_to_page(0)
      
   def switcher_cursor_changed(self,widget):
      selection = widget.get_selection()
      if selection == None: return None
      (model, iter) = selection.get_selected()
      path = model.get_path(iter)
      page = path[0]
      self.tabs.set_current_page(page)
      
   def switch_to_page(self,num):
      store = self.page_switcher.get_model()
      selection = self.page_switcher.get_selection()
      if selection:
         selection.unselect_all()
      cnt = 0
      iter = store.get_iter_first()
      while iter:
         if cnt == num: 
            selection.select_iter(iter)
            self.tabs.set_current_page(num)
            break
         cnt += 1
         iter = store.iter_next(iter)

   def data_to_widget(self):
      for dt in self.data_transfer_template:
         if type(dt[PREF_TEMPL_WIDGETNAME]) == tuple:
            wid = self.xml.get_widget(dt[PREF_TEMPL_WIDGETNAME][0])
         else:
            wid = self.xml.get_widget(dt[PREF_TEMPL_WIDGETNAME])
         
         assert wid != None,"pref widget '%s' does not exist" % (dt[PREF_TEMPL_WIDGETNAME],)
         if dt[PREF_TEMPL_DATA_IN] == PREF_DICT:
            data = dt[PREF_TEMPL_DATA][dt[PREF_TEMPL_DATA_PARAM1]]
         
         if type(wid) == gtk.Entry:
            wid.set_text(str(data))
            
         if type(wid) == gtk.RadioButton:
            wid = self.xml.get_widget(dt[PREF_TEMPL_WIDGETNAME][data])
            assert wid != None,"pref widget '%s' does not exist" % (dt[PREF_TEMPL_WIDGETNAME][data],)
            wid.set_active(True)
         
   def widget_to_data(self,forreal = False):
      for dt in self.data_transfer_template:
         dcf = dt[PREF_TEMPL_DATA_OUT]
         if type(dt[PREF_TEMPL_WIDGETNAME]) == tuple:
            self.curr_widget_name = dt[PREF_TEMPL_WIDGETNAME][0]
            wid = self.xml.get_widget(self.curr_widget_name)
         else:
            self.curr_widget_name = dt[PREF_TEMPL_WIDGETNAME]
            wid = self.xml.get_widget(self.curr_widget_name)

         try:
            if type(wid) == gtk.Entry:
               data = dcf(wid.get_text())
            
            if type(wid) == gtk.RadioButton:
               res = None
               cnt = 0
               for wn in dt[PREF_TEMPL_WIDGETNAME]:
                  if self.xml.get_widget(wn).get_active():
                     res = cnt
                     break
                  cnt += 1
               if res != None:
                 data = dcf(res)
         except:
            raise ValueError,_("Invalid value in field %s") % (self.curr_widget_name,)
            
         if forreal:
            if dt[PREF_TEMPL_DATA_IN] == PREF_DICT:
               dt[PREF_TEMPL_DATA][dt[PREF_TEMPL_DATA_PARAM1]] = data
   
   def run(self,showonly = None):
      self.data_to_widget()
      if showonly == None:
         self.page_switcher_pane.show()
      else:
         self.switch_to_page(showonly)   
         self.page_switcher_pane.hide()

      again = True
      while again:      
         response = self.window.run()
         if response == gtk.RESPONSE_OK:
            # store and close
            try:
               self.widget_to_data(forreal = False)
            except ValueError,info:
               ok = simple_dialog(gtk.MESSAGE_ERROR,str(info),gtk.BUTTONS_CLOSE,modal = True)
            else:
               self.widget_to_data(forreal = True)
               again = False
         else:
            again = False      
      self.window.hide()
      return response == gtk.RESPONSE_OK
   
         
########################## CLASS OSMEDITOR #########################
    
class osmeditor:
   ######################
   # Glade predefinied function(s)
   
   # If a window is closed, the ressources are freed and the window can not be shown again.
   # If the delete_event is set to gtk_widget_hide, this function is called, which only hides the
   # window, and eats the event, so that the ressources stay intact.
   def gtk_widget_hide(self, widget, data=None):
      widget.hide()
      return True            # eat event
      
   def __init__(self):
      self.bookmarks_handler = None

      # Read GUI
      self.xml = gtk.glade.XML('pyosmeditor.glade', domain = APP)
      # connect signal routines to class functions with the same name
      self.xml.signal_autoconnect(self)

      
      # read config file and get values
      self.config = simpleconfigfile(CONFIGFILENAME, \
                                     "osmedit",                        \
                                     "1",                              \
                                     
                    elements =   {"username": str, "password": str, "passsave": int,  \
                                  "savecurrentdisplaycoord": int,
                                  "currentdisplaycoord": str,
                                  "displaywidth": int, "displayheight": int, \
                                  "arrowthreshold": int, \
                                  "lastosmdir": str, \
                                  "lastgpxdir": str, \
                                  "geotagdir": str, \
                                  "osmserver": str, \
                                  "gpxwarnthreshold": float
                                 }, \
                    defaults =   {"username": "", "password": "", "passsave": 0,
                                  "savecurrentdisplaycoord": 1,
                                  "currentdisplaycoord": None, \
                                  "displaywidth": 500, "displayheight": 300, \
                                  "arrowthreshold": 20000, \
                                  "lastosmdir": None, \
                                  "lastgpxdir": None, \
                                  "geotagdir": "./geotags", \
                                  "osmserver": "www.openstreetmap.org", \
                                  "gpxwarnthreshold": 2.0
                                 })

      # Get info that is not stored in the dict
      self.bookmarks = self.config.root.getElementsByTagName("bookmarks")
      # create empty sub-tree if no bookmarks       
      if self.bookmarks == []:
         self.bookmarks = self.config.doc.createElement("bookmarks")
         self.config.root.appendChild(self.bookmarks)
      else:
         self.bookmarks = self.bookmarks[0]
       

      self.bookmarks_handler = bookmarks_handler(self, self.bookmarks)


      self.TBSM_NONE = 0
      self.TBSM_POSSIBLE_SCREEN_MOVE = 1
      self.TBSM_POSSIBLE_POINT_MOVE = 2
      self.TBSM_CREATING_NODES = 3
      self.TBSM_CONFIRMED_POINT_MOVE = 4
      
      self.preferences_handler = preferences_handler(self.xml.get_widget("preferences_dialog"), self.xml, \
         self.xml.get_widget("pref_page_switcher"),self.xml.get_widget("pref_page_switcher_pane"),self.xml.get_widget("preferences_pane"),
         [ ("pref_server",str,PREF_DICT,self.config.data,"osmserver"), \
           ("pref_password",str,PREF_DICT,self.config.data,"password"), \
           ("pref_email",str,PREF_DICT,self.config.data,"username"), \
           (("pref_store_option","pref_store_password_disk"),int,PREF_DICT,self.config.data,"passsave"), \
           ("pref_arrow_threshold",int,PREF_DICT,self.config.data,"arrowthreshold"), \
           ("pref_gpx_warn",float,PREF_DICT,self.config.data,"gpxwarnthreshold"), \
           
         ])
           
      # data now in self.config.data dictionary

      self.drawarea = drawarea(self.xml.get_widget('drawingarea'))
      self.drawarea.set_arrow_threshold(self.config.data["arrowthreshold"])
      self.mainwindow = self.xml.get_widget('pyosmeditor')
      self.mainwindow.resize(self.config.data["displaywidth"],self.config.data["displayheight"])
      
      self.infopane = self.xml.get_widget('infopane')
      self.passworddialog = self.xml.get_widget('passworddialog')
      self.emailentry = self.xml.get_widget('emailentry')
      self.passwordentry = self.xml.get_widget('passwordentry')
      self.saveoption = self.xml.get_widget('pw_duration')
      self.pwsavebuttons = [self.xml.get_widget('pw_duration'), self.xml.get_widget('pw_save')]
      # set version number in about dialog
      self.aboutdialog = self.xml.get_widget('aboutdialog')
      self.aboutdialog.set_version(pyosmeditorversion)

      self.view_geotag = self.xml.get_widget("view_geotag")
      self.geo_tag_enabled = False      # selection button
      # enable geotag options if exiflib is set
      if exiflib:
         enable_widgets([self.xml.get_widget('geo_scan_directory')])
            
      # Edit Mode selection: 0 = View, 1 = Edit
      self.tb_modes = [self.xml.get_widget('tb_viewmode'),self.xml.get_widget('tb_edit')]
      self.tb_mode  = 0
      self.tb_submode = 0
      self.tb_modes[0].set_active(True)
      
      self.livemode = False
      
      self.statusbar = statusbarhandler(self.xml.get_widget('statusbar'))
      self.picture_viewer_handler = picture_viewer_handler(self.xml.get_widget("image_window"),self.xml.get_widget("geo_image"),self.config,self.xml.get_widget("geo_info"))
      
      self.select_way_dialog_completion()
     
      self.tageditor = tageditor(self.xml.get_widget('treeview'),self.xml.get_widget('tagadd'),self.xml.get_widget('tagdel'),self.xml.get_widget('tagapply'),False)
      self.selected_elements_handler = selected_elements_handler(self)
      self.toolbarhandler = toolbarhandler(self)
      
      # menu options to be enabled ...
      # ... when a local track is being loaded
      self.localgpxdatamenuoptions = (self.xml.get_widget('fit_track'),self.xml.get_widget('unselect_gpx_nodes'), \
                                      self.xml.get_widget('remove_selected_gpx_nodes'),self.xml.get_widget('view_local_trace'), \
                                      self.xml.get_widget('remove_unselected_gpx_nodes'),self.xml.get_widget('save_gpx_data'))
      # ... when remote tracks are being loaded
      self.remotegpxdatamenuoptions = (self.xml.get_widget('view_remote_trace'),)
                                      
      # ... when the screen coordinates are being set
      #  e.g. if the coords are set via the config file
      #    or set if a local track is fit to the screen
      self.validscreencoordsoptions = (self.xml.get_widget('drawingarea'),
                                       self.xml.get_widget('zoomin'),self.xml.get_widget('zoomout'), \
                                       self.xml.get_widget('osmdata'),self.xml.get_widget('get_osm_tracks'))
      # ... when OSM data are present (either downloaded from the internet or loaded as file)
      self.osmdataloaded            = (self.xml.get_widget('saveosmdata'),self.xml.get_widget('viewways'))

      self.osmapi = osmapi(self.config.data["osmserver"],self.passwordrequest,self.osm_error_display)
       

      self.osmdata = osmdata(self.drawarea)             # data section == None by default
      self.localgpxdata = localgpxtrack(self.drawarea,self.config.data["gpxwarnthreshold"])
      self.remotegpxdata = remotegpxtrack(self.drawarea,self.config.data["gpxwarnthreshold"])
      self.drawselected = drawselected(self.drawarea,self.selected_elements_handler)
      self.geotaghints = geotaghints(self.drawarea)
      
      # select selection
      self.select_selects = [self.xml.get_widget("sel_node"),self.xml.get_widget("sel_segment"),self.xml.get_widget("sel_way"),self.xml.get_widget("sel_geo")]
      
      self.selectionframe = selectionframe(self.drawarea)

      self.drawarea.osmdata = self.osmdata              # to convert ID -> x/y

      # drawable elements
      # also determines order in which elements are drawn
      self.infoelements = [self.remotegpxdata, self.geotaghints, self.localgpxdata, self.osmdata, self.drawselected]

      try:
         coord = self.config.data["currentdisplaycoord"]
         if coord != None:
            coord = coord.split(",")
            self.drawarea.setscreencoords(float(coord[0]),float(coord[1]),float(coord[2]),float(coord[3]))
            self.enable_screencoordset()
      except:   # set an arbitrary box
         self.drawarea.setscreencoords(6.929844,50.932309,6.942344,50.941684)
                 
      self.recalcall()
      self.drawall()

   def get_select_selection(self):
      res = []
      for wid in self.select_selects:
         res.append(wid.get_active())
      return res
      


   def osm_error_display(self,status,reason):
      msg = _("<b>Server error %d</b>\n%s") % (status,reason)
      ok = simple_dialog(gtk.MESSAGE_ERROR,msg,gtk.BUTTONS_CLOSE,modal = True)
      
   ############
   # Select Way Dialog
   
   def select_way_dialog_completion(self):
      # the parts that glade could not do
      # to be executed once on start_up
      select_way_dialog_listview = self.xml.get_widget('select_way_dialog_listview')
      column = gtk.TreeViewColumn("way id")
      select_way_dialog_listview.append_column(column)
      renderer = gtk.CellRendererText()
      column.pack_start(renderer, True)
      column.add_attribute(renderer, 'text', 0)
      
   def select_way_dialog(self,id,waylist):
      # get widgets
      select_way_dialog = self.xml.get_widget('select_way_dialog')
      select_way_dialog_label = self.xml.get_widget('select_way_dialog_label')
      select_way_dialog_label.set_text(_("The selected segment %s\nis used in different ways.\nChoose one:") % (id,))
      select_way_dialog_ok_button = self.xml.get_widget('select_way_dialog_ok_button')
      select_way_dialog_ok_button.set_sensitive(False)
      select_way_dialog_listview = self.xml.get_widget('select_way_dialog_listview')
      
      # create list
      store = gtk.ListStore(str)
      for w in waylist:
         store.append([str(w)])
      select_way_dialog_listview.set_model(store)          # replaces glades empty store with ours


      response = select_way_dialog.run()

      select_way_dialog.hide()

      if response == gtk.RESPONSE_OK:    
         (path,column) = select_way_dialog_listview.get_cursor()
         if path == None: return None
         iter = store.get_iter(path)
         w = int(store.get_value(iter,0))
         return w
      else:
         return None

   def on_select_way_dialog_listview_cursor_changed(self,widget,data = None):
      select_way_dialog_ok_button = self.xml.get_widget('select_way_dialog_ok_button')
      select_way_dialog_ok_button.set_sensitive(True)

   def display_selected_item(self,tx):
      selecteditem = self.xml.get_widget('selected_item')
      selecteditem.set_text(tx)

   def on_pref_page_switcher_cursor_changed(self,widget,data=None):
      self.preferences_handler.switcher_cursor_changed(widget)

   ###########

   def enable_screencoordset(self):
      enable_widgets(self.validscreencoordsoptions)
   
   def print_to_infopane(self,text):   
      if text == None: text = ""
      self.infopane.get_buffer().set_text(text)
      self.infopane.show_now()
      
   def drawall(self, drawbox = None, clearbox = None, refresh = True):
      self.drawarea.cleardrawingarea(box = clearbox)
      
      for element in self.infoelements:
        if element.isvisible():
           element.draw(box = drawbox, refresh = False)

      if refresh:
         self.drawarea.refresh(box = clearbox)


   def recalcall(self):
     for element in self.infoelements:
        element.recalcdata()
           
   def passwordrequest(self,askalways = False):
      user = self.config.data["username"]
      if user == None: user = ""
      pw = self.config.data["password"]
      if pw == None: pw = ""
      save = self.config.data["passsave"]
      
      erg = None
      if user == "" or askalways:
         if self.preferences_handler.run(showonly = 0):
            user = self.config.data["username"]
            pw = self.config.data["password"]
            erg = (user,pw)        
      else:
         erg = (user,pw)
               
      return erg


   def showwaitdialog(self,info):
      self.waitdialog = simple_dialog(gtk.MESSAGE_INFO,info,buttons = None, modal = False)
      # wait until the widget is drawn
      process_pending_gtk_events()
      
   def destroywaitdialog(self):
      self.waitdialog.destroy()

   def on_view_geotags_activate(self,widget):
      self.geotaghints.set_visible(widget.get_active())
      self.drawall()

   def on_viewways_activate(self,widget):
      self.osmdata.set_ways_visible(widget.get_active(),self.drawall)
      
   def on_pyosmeditor_delete_event(self, widget, event, data=None):
      gtk.main_quit()
      return False
   
   def on_drawingarea_expose_event(self,widget, event):
      self.drawarea.exposed(widget,event)
      return False
    
   def on_drawingarea_configure_event(self,widget,event):
      self.drawarea.resizescreen()
      self.recalcall()
      self.drawall()
      return True                      ## why true?
      
   def on_geo_image_expose_event(self,widget,event):
      self.picture_viewer_handler.exposed(widget,event)    # load tags
      return False

   def on_geo_image_configure_event(self,widget,event):
      self.picture_viewer_handler.configure(widget,event)
      return False
    
   def on_geo_zoom_in_clicked(self,widget):
      self.picture_viewer_handler.zoom_in()

   def on_geo_zoom_out_clicked(self,widget):
      self.picture_viewer_handler.zoom_out()
 
   def on_geo_fit_clicked(self,widget):
      self.picture_viewer_handler.fit()   

   def on_geo_image_button_press_event(self,widget, event):
      self.picture_viewer_handler.mouse(True,widget,event)
      
   def on_geo_image_button_release_event(self,widget, event):
      self.picture_viewer_handler.mouse(False,widget,event)

  
      
   ##### MOUSE CLICKS + MOUSE MOVEMENT #########################################
   #
   ##############################################################################
   # Edit mode:
   #
   # clear area:         -> submode "move screen"
   # + ctrl              -> new node
   #
   # point:
   # without modifier    -> select single node, show info, -> submode "possible point move"
   # + shift             -> add to selection
   # + button "create segments" -> make segments (if multiple nodes are selected)
   #
   # segment:
   # without modifier    -> single segment, show info editable
   # + ctrl + same segment -> split segment
   # + shift
   #   if segment(s) are selected -> add to selection
   #   if way is selected         -> add to way
   # + button "create way" -> make way (single segment or multiple segments)
   #
   # way:
   # without modifier    -> single way, show info editable
   #
   # tb_submode:
   # 0 -> None
   # 1 -> possible screen move
   # 2 -> possible point move
   # 3 -> creating new nodes in progress 
   # 4 -> confirmed point move
   # TBSM constants definied in __init__
   
   def on_drawingarea_button_press_event(self,widget, event):
      ctrl_key = (gtk.gdk.CONTROL_MASK & event.state) != 0
      shift_key = (gtk.gdk.SHIFT_MASK & event.state) != 0
      
      if not self.drawarea.screencoordsset: return True
      
      if event.button == 1:  # LMB
         clearsubmode = True                             # reset self.tb_submode, if not requested otherwise

         sel_node, sel_seg, sel_way, sel_geo = self.get_select_selection()
         sel_geo = sel_geo and self.geo_tag_enabled
   
         # position is in event.x, event.y
         self.pressX = event.x
         self.pressY = event.y      

         id = None

         # did click hit a node ?
         # - don't look for nodes, if ctrl is pressed
         #   (in this case, we want to set a new node, points in the near vicinity would prevent that)
         if ctrl_key:
            id = None
         else:
            if sel_node:
               id = self.findnearestnode_xy(event.x,event.y)         
            
         if id != None:                     # A node was selected
            sele = selected_node(self.osmdata.get_node_pointer(id))
            if not ctrl_key and not shift_key:
               
               self.selected_elements_handler.set_element(sele, add = False,editable = (self.tb_mode == 1))
               if self.tb_mode == 1:
                  self.tb_submode = self.TBSM_POSSIBLE_POINT_MOVE
                  clearsubmode = False
                  # info needed for point move
                  self.selectedid = id
                  # calculate the box that must be refreshed
                  self.innerbox = self.osmdata.segments2boundingbox_xy(self.osmdata.findconnectingsegments(id))
                  px, py = self.osmdata.get_node_xy(id)
                  self.innerbox.put_point(px, py)
                  self.innerbox.grow(self.drawarea.noderadius, self.drawarea.noderadius, self.drawarea.noderadius, self.drawarea.noderadius)
            elif shift_key and not ctrl_key:
               if self.tb_mode == 1:                  # edit mode only
                  self.selected_elements_handler.set_element(sele,add = True, editable = True)
            else:
               pass
              
         # Did we hit a way?     
         else:
            if sel_way:
               id = self.findnearestway_xy(event.x,event.y)    
            if id != None:
               sele = selected_way(self.osmdata.get_way_pointer(id))
         
               if not ctrl_key and not shift_key:
                  self.selected_elements_handler.set_element(sele,add = False,editable = (self.tb_mode == 1))

            # Did we hit a segment?
            else:
               if sel_seg:
                  id = self.findnearestsegment_xy(event.x,event.y)
               if id != None:                                          # A segment was selected
                  sele = selected_segment(self.osmdata.get_segment_pointer(id))
               
                  if not ctrl_key and not shift_key:
                     self.selected_elements_handler.set_element(sele,add = False,editable = self.tb_mode == 1)
                  elif ctrl_key and not shift_key:
                     if self.tb_mode == 1 and self.selected_elements_handler.get_type() == "segment" and self.selected_elements_handler.selected_elements[0].get_id() == id:
                        self.split_segment(id, event.x, event.y)
                        
                  elif shift_key and not ctrl_key:
                     if self.tb_mode == 1:
                        self.selected_elements_handler.set_element(sele,add = True, editable = True)        

               # geohint?
               else:
                  if sel_geo:
                     id = self.geotaghints.find_geohint(event.x, event.y)
                  if id != None:
                     self.print_to_infopane(_("filename: ")+id)
                     self.picture_viewer_handler.load_image(id)
                     pass    # display picture
               
                     
                  # Nothing was hit:
                  else:
                                      
                     if not ctrl_key and not shift_key:
                        self.selected_elements_handler.clear()
                        self.tb_submode = self.TBSM_POSSIBLE_SCREEN_MOVE
                        clearsubmode = False
                        widget.window.set_cursor(gtk.gdk.Cursor(gtk.gdk.FLEUR))
                  
                     elif shift_key and not ctrl_key:  # same as last, but without unselecting elements
                        self.tb_submode = self.TBSM_POSSIBLE_SCREEN_MOVE
                        clearsubmode = False
                        widget.window.set_cursor(gtk.gdk.Cursor(gtk.gdk.FLEUR))
                                         
                     elif ctrl_key and not shift_key:
                        if self.tb_mode == 1:
                           if self.osmdata.data != None:
                              if self.tb_submode != self.TBSM_CREATING_NODES :         # first new node
                                 info = self.selected_elements_handler.get_info() # (len. type)
                                 if info != (1,"node"):                    # 1 existing node + new nodes
                                    self.selected_elements_handler.clear() # clear selected elements list
                              
                              id = self.create_new_node(event.x,event.y)   # create new node
                              if id != None:
                                 sele = selected_node(self.osmdata.get_node_pointer(id))       # add node to the list
                                 self.selected_elements_handler.set_element(sele,add = True,editable = False)
                              self.tb_submode = self.TBSM_CREATING_NODES        # don't clear the list next time
                              clearsubmode = False
                       
                           else:
                              simple_dialog(gtk.MESSAGE_ERROR,_("No OSM data\nLoad or download OSM data first"),gtk.BUTTONS_OK)
            
      
         if clearsubmode:
            self.tb_submode = self.TBSM_NONE
      
      if event.button == 3:
         self.selectionframe.framestart(event.x,event.y)
         
      return True         

   def on_drawingarea_button_release_event(self,widget, event):
      mindistancetomovesquare = 100

      if not self.drawarea.screencoordsset: return True
      
      if event.button == 1:
         dx = event.x - self.pressX
         dy = event.y - self.pressY

         # drag point
         if self.tb_submode == self.TBSM_POSSIBLE_POINT_MOVE:
            self.tb_submode = self.TBSM_NONE         # drag end
            
         if self.tb_submode == self.TBSM_CONFIRMED_POINT_MOVE:   
            self.update_node_position(self.selectedid,dx,dy)                  
            self.tb_submode = self.TBSM_NONE         # drag end
               
         # drag screen
         elif self.tb_submode == self.TBSM_POSSIBLE_SCREEN_MOVE:
            widget.window.set_cursor(None)
            self.tb_submode = self.TBSM_NONE         # end drag
            
            # Move window only, if distance is over threshold
            if (dx * dx + dy * dy) > mindistancetomovesquare:
               new = self.drawarea.move_xy(dx, dy)
               if new != None:
                 self.drawarea.setscreencoords(new[0],new[1],new[2],new[3])
                 self.recalcall()
                 self.drawall()                 
      
      # right mouse button
      if event.button == 3:
        res = self.selectionframe.frameend()
        self.localgpxdata.select_nodes_xy(res)
        self.drawarea.damage(res[0],res[1],res[2],res[3])
        self.localgpxdata.draw(box = None,refresh = True)   # box = None: change may go beyond frame (1 selected node
                                                            # may cause selected 'segments' to be selected which may
                                                            # lay outside 'res'
      return True         

   def on_drawingarea_motion_notify_event(self, widget, event):
      mindistancetomovepointsquare = 9

      if not self.drawarea.screencoordsset: return True
      
      if event.is_hint:
         x, y, state = event.window.get_pointer()
       
         # update status bar
         lon, lat = self.drawarea.transform2lonlat((x,y))
         self.statusbar.put(_("lon %s, lat %s, x %s, y %s, f %s") % (lon,lat, x, y, self.drawarea.factlong))
         
         # LMB
         if (state & gtk.gdk.BUTTON1_MASK):
            dx = x - self.pressX
            dy = y - self.pressY

            # drag point
            if self.tb_submode == self.TBSM_POSSIBLE_POINT_MOVE:
               if (dx * dx + dy * dy) > mindistancetomovepointsquare:
                  self.tb_submode = self.TBSM_CONFIRMED_POINT_MOVE
            
            if self.tb_submode == self.TBSM_CONFIRMED_POINT_MOVE:
               b = box_xy(self.innerbox)
               # enlarge with current point position (including radius to draw the point)
               b.put_point(event.x+self.drawarea.noderadius,event.y+self.drawarea.noderadius)
               b.put_point(event.x-self.drawarea.noderadius,event.y-self.drawarea.noderadius)
               # setXY point 
               self.osmdata.nodeindex[self.selectedid][3] = int(event.x)
               self.osmdata.nodeindex[self.selectedid][4] = int(event.y)

               c = box_xy(b)
               b.enlarge(1.25)
               self.drawall(drawbox = b.getbox(), clearbox = c.getbox(), refresh = False)
               self.drawarea.refresh(c.getbox())
         # RMB
         elif (state & gtk.gdk.BUTTON3_MASK):
            self.selectionframe.framemove(x,y)
            
      else:
         x = event.x
         y = event.y
         state = event.state
         
#         print "No hint",x,y,state
         if state & gtk.gdk.BUTTON1_MASK:
#            print "Button 1"
            pass

         if state & gtk.gdk.BUTTON3_MASK:
#            print "Button 3"
            pass
      return True


   ########################################################################

            # Update strategy (move point)
            # 1) bounding box around the moving node and all connected segments
            # 2) list of all segments that have one node in the bounding box
            # 3) clear the bounding box in the pixmap
            # 4) redraw listed nodes and segments in enlarged bounding box
            # 5) copy to drawarea
            # 6) once the move is finished, redraw the entire area
            # !) the coordinates of moving point are manipulated in the XY section of the node index
            #    before the big redraw, the XY coordinates have to be translated to lon/lat
   
   def update_node_position(self,nodeid,dx,dy):
      mindestancepointmovesquare = 1
               
      if (dx * dx + dy * dy) > mindestancepointmovesquare:
         # [3] and [4] have been used during mouse movement
         # recalculating values
         x, y = self.drawarea.transform2xy(self.osmdata.nodeindex[nodeid][0],self.osmdata.nodeindex[nodeid][1])
         lon, lat = self.drawarea.transform2lonlat((x+dx,y+dy))
         self.osmdata.update_node_position(nodeid,lon,lat)    
         self.drawall()
         if self.livemode:
            dia = simple_dialog(gtk.MESSAGE_INFO,_("Contacting OSM\nto update node position"),buttons = None, modal = False)
            id = self.osmapi.send_updated_data(self.osmdata.get_node_pointer(nodeid))
            dia.destroy()

   def split_segment(self,click2_segment_id,x,y):
      if self.selected_elements_handler.get_count() != 1:
         return
         
      # get segment id of previously selected segment
      segid = self.selected_elements_handler.selected_elements[0].get_id()
      if segid != click2_segment_id: 
         return       # split point needs to be on the same segment
      lon, lat = self.drawarea.transform2lonlat((x,y))

      if self.livemode:
         dia = simple_dialog(gtk.MESSAGE_INFO,_("Contacting OSM\nChecking dependencies"),buttons = None, modal = False)
         waycnt = self.osmapi.count_ways_of_segment(segid)
         areacnt = self.osmapi.count_areas_of_segment(segid)
         dia.destroy()
         
         if waycnt != 0 or areacnt != 0:
            ok = simple_dialog(gtk.MESSAGE_ERROR,_("This segment is part of a way or an area\nThis condition is not yet implemented"),buttons = gtk.BUTTONS_OK)
            return

      new_node_id = self.create_new_node(x,y)
      if new_node_id == None: return None

      # before:   from ----------> to
      # after:    from(old) ---new seg---> to [split point] from ---old seg--> to(old)
      old_seg_node  = self.osmdata.segmentindex[click2_segment_id][2]
      old_from_node = self.osmdata.segmentindex[click2_segment_id][0]
      old_to_node   = self.osmdata.segmentindex[click2_segment_id][1]

      new_segment_id = self.create_new_segment(old_from_node,new_node_id)
      if new_segment_id == None: return None
      
      self.update_segment_from_to(click2_segment_id,new_node_id,old_to_node)
      
#         dia = simple_dialog(gtk.MESSAGE_INFO,"Contacting OSM\nto create segment",buttons = None, modal = False)
#            id = self.osmapi.create_new_segment(node1id,node2id)
#            dia.destroy()
#            if id == None:
#               ok = simple_dialog(gtk.MESSAGE_ERROR,"OSM error - segment",buttons = gtk.BUTTONS_OK)
#         else:
#            id = None
            
#         if id == None:
#            id = self.osmdata.get_next_own_id()

#      new_node_id = self.osmdata.get_next_own_id()
#      new_seg_id = self.osmdata.get_next_own_id()
#      self.osmdata.split_segment(segid,new_node_id,new_seg_id,lon,lat)
      self.drawarea.drawnode(new_node_id)
      self.drawarea.drawsegment(new_segment_id)
   
   def extend_way(self,idlist):
      # extend way (first id) with segments on the OSM server and locally
      self.osmdata.add_segments_to_way(idlist[0],idlist[1:])
      n = self.osmdata.get_way_pointer(idlist[0])
      if self.livemode:
         dia = simple_dialog(gtk.MESSAGE_INFO,_("Contacting OSM\nto update way %s") % (idlist[0],),buttons = None, modal = False)
         ok = self.osmapi.send_updated_data(n)
         dia.destroy()

      self.drawarea.drawway(idlist[0])
      
   def update_segment_from_to(self,id,fromid, toid):
      # update the from and to ID of an existing segment on the OSM server and locally
      n = self.osmdata.get_segment_pointer(id)
      # save old values
      f = n.getAttribute("from")
      t = n.getAttribute("to")
      n.setAttribute("from",str(fromid))
      n.setAttribute("to",str(toid))
      ok = True
      if self.livemode:
         ok = self.osmapi.send_updated_data(n)
      if not ok:
         n.setAttribute("from",f)
         n.setAttribute("to",t)
      self.osmdata.update_index(n)
      self.drawarea.drawsegment(id)
      

   def create_new_node(self,x,y):
      # create new node on OSM and locally
      lon, lat = self.drawarea.transform2lonlat((x,y))
      if self.livemode:
         dia = simple_dialog(gtk.MESSAGE_INFO,_("Contacting OSM\nto create node"),buttons = None, modal = False)
         id = self.osmapi.create_new_node(lon,lat)
         dia.destroy()
         if id == None:
            ok = simple_dialog(gtk.MESSAGE_ERROR,_("OSM error - node"),buttons = gtk.BUTTONS_OK)
            return None
      else:
         id = None
            
      if id == None:
         id = self.osmdata.get_next_own_id()
      self.osmdata.create_new_node(lon,lat,id)
      self.drawarea.drawnode(id)
      return id

   def create_new_segment(self,fromid,toid):
      # create new segment on OSM and locally
      if self.osmdata.segment_already_exists(fromid, toid):    # don't create already existing segments
         return None
         
      if self.livemode:
         dia = simple_dialog(gtk.MESSAGE_INFO,_("Contacting OSM\nto create segment"),buttons = None, modal = False)
         id = self.osmapi.create_new_segment(fromid, toid)
         dia.destroy()
         if id == None:
            ok = simple_dialog(gtk.MESSAGE_ERROR,_("OSM error - segment from node %s -> node %s") % [fromid,toid],buttons =gtk.BUTTONS_OK)
            return None
      else:
         id = self.osmdata.get_next_own_id()

      self.osmdata.create_new_segment(fromid, toid, id)
      self.drawarea.drawsegment(id, arrowtype = 1)
      return id
      
   
   def on_tb_delete_clicked(self,widget):
      self.toolbarhandler.call_function(0)

   def on_tb_var1_clicked(self,widget):
      self.toolbarhandler.call_function(1)

   def on_tb_var2_clicked(self,widget):
      self.toolbarhandler.call_function(2)
      

################################################# functions via toolbar

   def tb_delete_element(self):
      # delete any kind of element, depending on what is selected

      what = self.selected_elements_handler.selected_elements[0].get_osm_info()
      if what == None: return
      
      what = what[0].split("/")[0]
#      print what
      if not what in ["node","segment","way"]: return             # only single elements
      
      o = self.selected_elements_handler.selected_elements[0].get_osm_info()
      if o != "":
         objection = self.osmdata.allowed_to_be_deleted(o)
         if objection == None:
            if self.livemode:
               dia = simple_dialog(gtk.MESSAGE_INFO,_("Contacting OSM\nto <b>delete</b> %s") %(o[0],),buttons = None, modal = False)
               ok = self.osmapi.delete_element(o[0])
               dia.destroy()
            else:
               ok = True

            if ok:     
               self.selected_elements_handler.clear()
               self.osmdata.delete_element(o)            # delete the element here as well
            self.drawall()
         else:
            ok = simple_dialog(gtk.MESSAGE_ERROR,objection,gtk.BUTTONS_CLOSE,modal = True)

   def tb_add_segments_to_way(self):
      if self.selected_elements_handler.get_type() == "way/segments":
         # first id is way, following IDs are segments
         sl = self.selected_elements_handler.get_selected_list_ids()
         self.extend_way(sl)

   def tb_create_segments(self):
      if self.selected_elements_handler.get_type() == "nodes":
         sl = self.selected_elements_handler.get_selected_list()
         first = True
         skipped = False
         tocreate = []
         for l in sl:
            if not first:
               fromid = ll.get_id()
               toid = l.get_id()
               if self.osmdata.segment_already_exists(fromid, toid):    # don't create already existing segments
                  skipped = True
               else:
                  tocreate.append((fromid,toid))
               
            first = False
            ll = l

         if skipped:
            dia = simple_dialog(gtk.MESSAGE_INFO,_("Some segments already existed\nThey have been skipped"),buttons = gtk.BUTTONS_OK)
         
         if len(tocreate)>0:
            if self.livemode:
               dia = simple_dialog(gtk.MESSAGE_INFO,_("Contacting OSM\nto create segment"),buttons = None, modal = False)
               
            for seg in tocreate:
               fromid = seg[0]
               toid = seg[1]
               if self.livemode:
                  id = self.osmapi.create_new_segment(fromid, toid)
                  if id == None:
                     ok = simple_dialog(gtk.MESSAGE_ERROR,_("OSM error - segment from node %s -> node %s") % [fromid,toid],buttons =gtk.BUTTONS_OK)
               else: 
                  id = self.osmdata.get_next_own_id()

               self.osmdata.create_new_segment(fromid, toid, id)
               self.drawarea.drawsegment(id, arrowtype = 1)
               
            if self.livemode:
               dia.destroy()

   def tb_create_way(self):
      tp = self.selected_elements_handler.get_type()
      if  tp == "segments" or tp == "segment":
         seglist = self.selected_elements_handler.get_selected_list_ids()
         
         if len(seglist)>0:
            if self.livemode:
               dia = simple_dialog(gtk.MESSAGE_INFO,_("Contacting OSM\nto create way"),buttons = None, modal = False)
               
               id = self.osmapi.create_new_way(seglist)
               if id == None:
                  ok = simple_dialog(gtk.MESSAGE_ERROR,_("OSM error while creating way"),buttons =gtk.BUTTONS_OK)
                  return
            else: 
               id = self.osmdata.get_next_own_id()

            self.osmdata.create_new_way(seglist,id)
            self.drawarea.drawway(id, arrowtype = 1)
               
            if self.livemode:
               dia.destroy()
               
   def tb_align_from_to(self):
      if self.selected_elements_handler.get_type() == "segment":          # single segment
         segments_to_switch = seglist = self.selected_elements_handler.get_selected_list_ids()
   
      elif self.selected_elements_handler.get_type() == "segments":         # multiple segments
         seglist = self.selected_elements_handler.get_selected_list_ids()
         stat = self.osmdata.statistic_nodes_from_segments(seglist)
         # stat 0: nodes with exactly one occurence, 1: with more than 2, 2: dict with results
##         print stat[0]
##         print stat[1]
##         print stat[2]
         if len(stat[0]) != 2 or len(stat[1]) > 0:
            ok = simple_dialog(gtk.MESSAGE_ERROR,_("That looks strange.\nSorry, I don't know how to align these segments"),buttons =gtk.BUTTONS_OK)
            return

         rn = seglist[0]
         segments_to_modify = []
         startid = None
         if self.osmdata.segmentindex[rn][0] in stat[0]: 
            startid = self.osmdata.segmentindex[rn][0] 
         if self.osmdata.segmentindex[rn][1] in stat[0]: 
            startid = self.osmdata.segmentindex[rn][1] 
            
         if startid == None:
            ok = simple_dialog(gtk.MESSAGE_ERROR,_("First selected segment is not the start of the chain.\nSelect in correct order and try again."),buttons =gtk.BUTTONS_OK)
            return
         
         segments_to_switch = []
         while seglist != []:
            found = False
            for x in range(len(seglist)):
               if self.osmdata.segmentindex[seglist[x]][0] == startid:
                  startid = self.osmdata.segmentindex[seglist[x]][1]    # next id to look for
                  del seglist[x]                                # del from search
                  found = True
                  break
               elif self.osmdata.segmentindex[seglist[x]][1] == startid:
                  segments_to_switch.append(seglist[x])
                  startid = self.osmdata.segmentindex[seglist[x]][0]    # next id to look for
                  del seglist[x]                                # del from search
                  found = True
                  break
               else:
                  pass
            if not found:
               ok = simple_dialog(gtk.MESSAGE_ERROR,_("Node %s not found in way.") % (startid,), buttons =gtk.BUTTONS_OK)
               return
      else:
         return
                  
      if len(segments_to_switch)>0:
         if self.livemode:
            dia = simple_dialog(gtk.MESSAGE_INFO,_("Contacting OSM\nUpdating segments"),buttons = None, modal = False)
         for x in segments_to_switch:
            # modify local data first, so that we can send the modified "node" XML to the OSM server
            self.osmdata.switch_segment_orientation(x)
            if self.livemode:
               node = self.osmdata.get_segment_pointer(x)
               ok = self.osmapi.send_updated_data(node)
               if not ok:
                  ok = simple_dialog(gtk.MESSAGE_ERROR,_("Error updating segment %s.\nReload OSM data.") % (x,),buttons =gtk.BUTTONS_OK)
                  break
         self.drawall()

         if self.livemode:
            dia.destroy()

   ##################### Menu
   
   
   def on_httplibdebug_activate(self,widget):
      if widget.get_active(): 
         httplib.HTTPConnection.debuglevel = 2
      else:
         httplib.HTTPConnection.debuglevel = 0
   
   def on_preferences_activate(self,widget):
      ok = self.preferences_handler.run()
      
   def on_view_arrows_activate(self,widget):
      akt = widget.get_active()
      self.drawarea.set_arrow_visible(akt)
      self.drawall()

   def on_view_remote_trace_activate(self,widget):
      self.remotegpxdata.set_visible(widget.get_active())
      self.drawall()

   def on_view_local_trace_activate(self,widget):
      self.localgpxdata.set_visible(widget.get_active())
      self.drawall()
   
   def on_info_activate(self,widget):
      about = self.xml.get_widget('aboutdialog')
      about.show()
      
   def on_test_activate(self, widget): # at the moment for debug purposes
      httplib.HTTPConnection.debuglevel = 2

      return
         
   # mode selection: view, edit
   def on_tb_mode_toggled(self,widget,data = None):
      mo = self.tb_modes.index(widget)
      akt = widget.get_active()
      if akt:
         self.tb_mode = mo
         self.selected_elements_handler.mode_change(mo)
         self.toolbarhandler.set_tb_icons(self.selected_elements_handler.get_type())
         
   def on_live_toggle_button_toggled(self,widget,data=None):
      akt = widget.get_active()
      if akt:
         w = self.passwordrequest()
         if w == None: 
            akt = False
            widget.set_active(False)
            
      self.livemode = akt
      
   def on_setosmpassword_activate(self,widget):
      w = self.passwordrequest(askalways = True)

   def on_saveosmdata_activate(self,widget):
      dialog = gtk.FileChooserDialog("Save..",
                               None,
                               gtk.FILE_CHOOSER_ACTION_SAVE,
                               (gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL,
                                gtk.STOCK_SAVE, gtk.RESPONSE_OK))
      dialog.set_default_response(gtk.RESPONSE_OK)

      filter = gtk.FileFilter()
      filter.set_name("All files")
      filter.add_pattern("*")
      dialog.add_filter(filter)

      filter = gtk.FileFilter()
      filter.set_name("OSM data")
      filter.add_pattern("*.osm")

      if self.config.data["lastosmdir"]:
         dialog.set_current_folder(self.config.data["lastosmdir"])
      response = dialog.run()

      draw = False
      if response == gtk.RESPONSE_OK:
         fnm = add_extension(dialog.get_filename(),".osm")
         self.config.data["lastosmdir"] = os.path.dirname(fnm)
         fl = open(fnm,"w")
         fl.write(self.osmdata.data.toxml())     
         fl.close()
      dialog.destroy()
      
   def on_geo_scan_directory_activate(self,widget):
      try:
         dia = simple_dialog(gtk.MESSAGE_INFO,_("Scanning directory for\ngeotagged pictures"),buttons = None, modal = False)
         self.geotaghints.readdata(self.config.data["geotagdir"])
      except OSError:
         dia.destroy()
         ok = simple_dialog(gtk.MESSAGE_ERROR,_("%s not found") % (self.config.data["geotagdir"],),gtk.BUTTONS_CLOSE,modal = True)
         return
      dia.destroy()

      enable_widgets([self.xml.get_widget("view_geotag"),self.xml.get_widget("sel_geo")])
      self.geo_tag_enabled = True
      self.geotaghints.set_visible(True)                   # and display them
      self.drawall()
      self.view_geotag.set_active(True)                    # update the GUI element
      
      ok = simple_dialog(gtk.MESSAGE_INFO,_("%s geotagged pictures found") % (len(self.geotaghints.index),),gtk.BUTTONS_CLOSE,modal = True)

   def on_loadgpx_activate(self,widget):
      dialog = gtk.FileChooserDialog("Open GPX file..",
                               None,
                               gtk.FILE_CHOOSER_ACTION_OPEN,
                               (gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL,
                                gtk.STOCK_OPEN, gtk.RESPONSE_OK))
      dialog.set_default_response(gtk.RESPONSE_OK)

      filter = gtk.FileFilter()
      filter.set_name(_("GPX files"))
      filter.add_pattern("*.gpx")
      dialog.add_filter(filter)

      filter = gtk.FileFilter()
      filter.set_name(_("All files"))
      filter.add_pattern("*")
      dialog.add_filter(filter)

      if self.config.data["lastgpxdir"]:
         dialog.set_current_folder(self.config.data["lastgpxdir"])

      response = dialog.run()

      draw = False
      if response == gtk.RESPONSE_OK:
         self.config.data["lastgpxdir"] = os.path.dirname(dialog.get_filename())
##TODO: catch exceptions      
         info = self.localgpxdata.readdata(dialog.get_filename())
         self.print_to_infopane(info)
         self.localgpxdata.recalcdata()
         draw = True

      elif response == gtk.RESPONSE_CANCEL:
         pass

      dialog.destroy()
      process_pending_gtk_events()
      if draw:
         self.drawall()
         
      # enable menu options relating to local tracks
      # if screen coords are invalied, fit track to screen
      if (self.localgpxdata.data != None):
         enable_widgets(self.localgpxdatamenuoptions)
         if not self.drawarea.screencoordsset:
            self.fitlocaltracktoscreen()

   def on_load_gpx_helper_files_activate(self,widget):
      dialog = gtk.FileChooserDialog(_("Open multiple GPX files.."),
                               None,
                               gtk.FILE_CHOOSER_ACTION_OPEN,
                               (gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL,
                                gtk.STOCK_OPEN, gtk.RESPONSE_OK))
      dialog.set_default_response(gtk.RESPONSE_OK)
      dialog.set_select_multiple(True)

      filter = gtk.FileFilter()
      filter.set_name(_("GPX files"))
      filter.add_pattern("*.gpx")
      dialog.add_filter(filter)

      filter = gtk.FileFilter()
      filter.set_name(_("All files"))
      filter.add_pattern("*")
      dialog.add_filter(filter)

      if self.config.data["lastgpxdir"]:
         dialog.set_current_folder(self.config.data["lastgpxdir"])
         
      response = dialog.run()
      filelist = dialog.get_filenames()
      dialog.destroy()
      
      draw = False
      if response == gtk.RESPONSE_OK:     
##TODO: catch exceptions
         box = box_deg(self.drawarea.get_screencoordinates())
         box.enlarge(2.0)
         info = ""
         
         dia = simple_dialog(gtk.MESSAGE_INFO,_("Scanning %s GPX tracks") %(len(filelist),),buttons = None, modal = False)
         
         for fnm in filelist:
            points = self.localgpxdata.add_helper_track(fnm,box)
            info += _("%s added %s points\n") % (os.path.basename(fnm),points)
            self.print_to_infopane(info)

         self.localgpxdata.recalcdata()

         if len(filelist)>0:
            enable_widgets(self.localgpxdatamenuoptions)

         dia.destroy()
         
         self.localgpxdata.recalcdata()
         draw = True

      elif response == gtk.RESPONSE_CANCEL:
         pass

      process_pending_gtk_events()
      if draw:
         self.drawall()
         
      # enable menu options relating to local tracks
      # if screen coords are invalied, fit track to screen
      if (self.localgpxdata.data != None):
         enable_widgets(self.localgpxdatamenuoptions)
         if not self.drawarea.screencoordsset:
            self.fitlocaltracktoscreen()


   def on_save_gpx_data_activate(self,widget):
      dialog = gtk.FileChooserDialog(_("Save local GPX data.."),
                               None,
                               gtk.FILE_CHOOSER_ACTION_SAVE,
                               (gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL,
                                gtk.STOCK_SAVE, gtk.RESPONSE_OK))
      dialog.set_default_response(gtk.RESPONSE_OK)

      filter = gtk.FileFilter()
      filter.set_name(_("All files"))
      filter.add_pattern("*")
      dialog.add_filter(filter)

      filter = gtk.FileFilter()
      filter.set_name(_("GPX data"))
      filter.add_pattern("*.gpx")

      if self.config.data["lastgpxdir"]:
         dialog.set_current_folder(self.config.data["lastgpxdir"])
      response = dialog.run()

      draw = False
      if response == gtk.RESPONSE_OK:
         fnm = add_extension(dialog.get_filename(),".gpx")
         self.config.data["lastgpxdir"] = os.path.dirname(fnm)
         dt = self.localgpxdata.get_modified_gpx_track()
         fl = open(fnm,"w")
         fl.write(dt)     
         fl.close()
      dialog.destroy()


   def on_loadosmfile_activate(self,widget):
      dialog = gtk.FileChooserDialog(_("Open OSM file.."),
                               None,
                               gtk.FILE_CHOOSER_ACTION_OPEN,
                               (gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL,
                                gtk.STOCK_OPEN, gtk.RESPONSE_OK))
      dialog.set_default_response(gtk.RESPONSE_OK)

      filter = gtk.FileFilter()
      filter.set_name(_("OSM files"))
      filter.add_pattern("*.osm")
      dialog.add_filter(filter)
      
      filter = gtk.FileFilter()
      filter.set_name(_("All files"))
      filter.add_pattern("*")
      dialog.add_filter(filter)

      if self.config.data["lastosmdir"]:
         dialog.set_current_folder(self.config.data["lastosmdir"])
      response = dialog.run()

      draw = False
      if response == gtk.RESPONSE_OK:
         self.selected_elements_handler.clear()     
         self.config.data["lastosmdir"] = os.path.dirname(dialog.get_filename())
##TODO: catch exceptions      
         info = self.osmdata.readdata(dialog.get_filename())
         self.tageditor.set_xml_root(self.osmdata.data)
         self.print_to_infopane(info)
         box = self.osmdata.minmax_deg()
         self.drawarea.set_osm_box(box)
         self.drawarea.setscreencoords(box[0],box[1],box[2],box[3])
         self.recalcall()
         self.drawall()

#      elif response == gtk.RESPONSE_CANCEL:
#         pass

      dialog.destroy()
      if draw:
         self.drawall()
   
      enable_widgets(self.osmdataloaded,enable = (self.osmdata.data != None))


   def on_osmdata_activate(self,widget):
      # Note: This code is called from the menu as well as from a button in the toolbar.
      self.showwaitdialog(_("Accessing OSM server\nfor OSM street data"))
      f = self.osmapi.getmap(self.drawarea.lb_lon,self.drawarea.lb_lat,self.drawarea.rt_lon,self.drawarea.rt_lat)
      info = self.osmdata.readdata(f)
      f.close()
      # Note: the osm surrounding box is set to the REQUESTED box, not to the box derived
      #       from the received data (which might be smaller). Extreme case: horizontal
      #       motorway with no surronding nodes. If requested from OSM server the empty space
      #       is guaranteed to be empty. The calculated box would only be a small strip.
      self.drawarea.set_osm_box( (self.drawarea.lb_lon, self.drawarea.lb_lat, self.drawarea.rt_lon, self.drawarea.rt_lat))

      self.tageditor.set_xml_root(self.osmdata.data)
      self.destroywaitdialog()
      self.print_to_infopane(info)
      self.osmdata.recalcdata()
      self.drawall()
      if self.osmdata.data:
         enable_widgets(self.osmdataloaded)
         

   def on_get_osm_tracks_activate(self,widget):
      self.showwaitdialog(_("Accessing OSM server\nfor OSM tracks"))
      f = self.osmapi.gettrackpoints(self.drawarea.lb_lon, self.drawarea.lb_lat, self.drawarea.rt_lon, self.drawarea.rt_lat, 0)
      info = self.remotegpxdata.readdata(f)
      f.close()
      if self.remotegpxdata.data != None:
         enable_widgets(self.remotegpxdatamenuoptions)

      self.print_to_infopane(info)
      self.destroywaitdialog()
      self.remotegpxdata.recalcdata()
      self.drawall()
      
   def on_unselect_gpx_nodes_activate(self,widget):
      self.localgpxdata.unselect_all_nodes()
      self.drawall()

   def on_remove_selected_gpx_nodes_activate(self,widget):
      self.localgpxdata.remove_selected_nodes()
      self.drawall()

   def on_remove_unselected_gpx_nodes_activate(self,widget):
      self.localgpxdata.remove_unselected_nodes()
      self.drawall()
    
   def on_fit_track_activate(self,widget):
      self.fitlocaltracktoscreen()

############ Menu: Bookmarks

   def on_bookmark_add_activate(self,widget):
      co = self.drawarea.get_screenbookmark()
      self.bookmarks_handler.add_bookmark(_("new bookmark"),None,co[0],co[1],co[2])
               
   def on_bookmark_selected(self,widget,data):
      if data:
#         name = getChildValue(data,"name")
         lon = float(getChildValue(data,"lon"))
         lat = float(getChildValue(data,"lat"))
         scale = float(getChildValue(data,"scale"))
         self.drawarea.set_bookmark(lon,lat,scale)
         self.recalcall()
         self.drawall()

   def on_bookmark_edit_activate(self,widget,data=None):         self.bookmarks_handler.show_edit()
   def on_bookmark_list_cursor_changed(self,widget,data=None):   self.bookmarks_handler.cursor_changed()
   
   def on_bookmark_remove_clicked(self,widget):
      return self.bookmarks_handler.button_remove()

   def on_bookmark_apply_clicked(self,widget):
      return self.bookmarks_handler.button_apply()
      
   def on_bookmark_list_drag_end(self,widget,data=None):
      return self.bookmarks_handler.drag_end()
     
   def on_bookmark_current_clicked(self,widget):
      return self.bookmarks_handler.button_current()
      
   def on_bookmark_list_row_activated(self,view, path, view_column, data=None):
      model = view.get_model()
      iter = model.get_iter(path)
      bm = model.get_value(iter,0) 
      if bm == None: return
      
      lon = float(getChildValue(bm,"lon"))
      lat = float(getChildValue(bm,"lat"))
      scale = float(getChildValue(bm,"scale"))
      self.drawarea.set_bookmark(lon,lat,scale)
      self.recalcall()
      self.drawall()
      return False
               
#################################################################
      
   def fitlocaltracktoscreen(self):
      if self.localgpxdata.data:
         b = self.localgpxdata.minmax()
         self.drawarea.setscreencoords(b[0],b[1],b[2],b[3],center = True)
         self.enable_screencoordset()
         self.recalcall()
         self.drawall()

   def findnearestnode_xy(self,x,y):
      if self.osmdata.data == None:
         return None
      
      findradius = 10
      lx = x - findradius
      tx = x + findradius
      ly = y - findradius
      ty = y + findradius
      
      id = None
      for n in self.osmdata.nodeindex:
         px = self.osmdata.nodeindex[n][3]
         py = self.osmdata.nodeindex[n][4]
         if (lx <= px) and (px <= tx) and (ly <= py) and (py <= ty):
            px -= x
            py -= y
            # calculate distance (square thereof)
            dist = px * px + py * py
            if id != None:
               if dist < ldist:
                  id = n
                  ldist = dist
            else:
               id = n
               ldist = dist
      return id  

   def on_tagadd_clicked(self,widget,data = None):
      self.tageditor.addbutton()

   def on_tagdel_clicked(self,widget,data = None):
      self.tageditor.delbutton()

   def on_tagapply_clicked(self,widget,data = None):
      self.tageditor.applybutton()
      if self.livemode:
         # apply only accessible if only one element is selected
         sel = self.selected_elements_handler.selected_elements[0]
         if sel:
            data = sel.get_osm_info()
            if data != None:
               self.showwaitdialog(_("Updating tag data on OSM server"))
               ok = self.osmapi.send_updated_data(data[1])
               self.destroywaitdialog()
   
   def on_treeview_cursor_changed(self,widget,data = None):
      self.tageditor.cursor_changed()

   def on_quit_activate(self,widget):
      gtk.main_quit()
      
   def findnearestsegment_xy(self,px,py):
      if self.osmdata.data == None:
         return None
         
      maxdistance = 20
      
      nseg = None
      lh = None
      
      for seg in self.osmdata.segmentindex:
         fromid = self.osmdata.segmentindex[seg][0]
         toid = self.osmdata.segmentindex[seg][1]
         if fromid == toid:   # fault in database 
            continue
         fromX = self.osmdata.nodeindex[fromid][3]
         fromY = self.osmdata.nodeindex[fromid][4]
         toX = self.osmdata.nodeindex[toid][3]
         toY = self.osmdata.nodeindex[toid][4]
                  
         toX -= fromX
         toY -= fromY
         p1X = px - fromX
         p1Y = py - fromY
         
         if (toX == 0) and (toY == 0):
            continue
            
         # scalar product p1 / to
         sc = toX * p1X + toY * p1Y

         if sc < 0:
            continue
         
         toxl = math.sqrt(toX * toX + toY * toY)
         l = (p1X * toX + p1Y * toY) / toxl
         if l > toxl:
            continue
            
         hsq = p1X * p1X + p1Y * p1Y - l * l
         if hsq < 0:
            continue
         if nseg == None:
            nseg = seg
            lh = hsq
         else:
            if hsq < lh:
               nseg = seg
               lh = hsq
      
      if lh > maxdistance:
         return None   
      return nseg         

   def findnearestway_xy(self,px,py):
      if self.osmdata.data == None: return None
      if not self.osmdata.waysvisible: return None
      
      seg = self.findnearestsegment_xy(px,py)
      if seg == None: return None

      ways = self.osmdata.find_ways_with_segment(seg)
            
      if len(ways) == 0:
         return None
      if len(ways) == 1:
         return ways[0]
      w = self.select_way_dialog(seg,ways)
      return w
   
   def on_zoomin_clicked(self,widget):
      new = self.drawarea.zoom(1.25)
      if new != None:
         self.drawarea.setscreencoords(new[0],new[1],new[2],new[3])
         self.recalcall()
         self.drawall()
      
   def on_zoomout_clicked(self,widget):
      new = self.drawarea.zoom(1 / 1.25)
      if new != None:
         self.drawarea.setscreencoords(new[0],new[1],new[2],new[3])
         self.recalcall()
         self.drawall()
    
   def modifyconfigbeforeclosing(self):
      # don't save credentials, if the user doesnt want it
      if osmedit.config.data["passsave"] == 0:
         osmedit.config.data["username"] = ""
         osmedit.config.data["password"] = ""
     
      if osmedit.config.data["savecurrentdisplaycoord"] == 1: 
         co = self.drawarea.get_screencoordinates()
         osmedit.config.data["currentdisplaycoord"] = "%s,%s,%s,%s" % (co[0], co[1], co[2], co[3])
      else:
         del osmedit.config.data["currentdisplaycoord"]

      xy = self.mainwindow.get_size()
      self.config.data["displaywidth"] = self.mainwindow.get_allocation().width
      self.config.data["displayheight"] = self.mainwindow.get_allocation().height
               
if __name__ == "__main__":
   osmedit = osmeditor()
   gtk.main()
   osmedit.modifyconfigbeforeclosing()
   osmedit.config.writedata()
