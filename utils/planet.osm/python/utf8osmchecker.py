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
#
# Version 0.1, 2006-08-15
#
# Quick and dirty scanner for non UTF-8 text in planet.osm
#
# The parser checks the XML for closing elements for node/segment/way elements.
# If one is found outbut_buffer2 is called.
# buffer2 stores the input since the last call of output_buffer2
# If in the meantime a character does not meet the UTF-8 condition, it sets markedforoutput.
# If this flag is set, output_buffer2 prints the content of buffer2 to the screen and 
# saves it in an error file.
# The flag is reset and buffer2 cleared.

import re, sys

xmlfilename = "planet.osm"
errorfile = "notutf.bin"

last = ""                  # ID of last node/segment/way
lasterror = ""             # ID of last offending node/segment/way
counter = 0                # number of offending elements
markedforoutput = False
buffer2 = ""

def error():
   global last, lasterror, counter, markedforoutput
   
   if lasterror != last:   # only count one error per element
      markedforoutput = True
      counter += 1
   lasterror = last
   
def output_buffer2():
   global buffer2, markedforoutput, errout
   if markedforoutput:
      print buffer2
      errout.write(buffer2)
   markedforoutput = False
   buffer2 = ""

def procinside(buf):
   global last, ma, ema, buffer2

   buffer2 += "<" + buf + ">"

   g = ma.match(buf)      # starting tag?
   if g:
      la = g.group(1)
      id = g.group(2)
      if la in ['node','segment','way']:
         last = "%s %s" % (la,id)             # "node 1234", "segment 6543", ...
         if buf[-1] == "/":                   # self closing element
            output_buffer2()
   else:
      g = ema.match(buf)                      # regular expression to find end tag
      if g:
         la = g.group(1)
         if la in ['node','segment','way']:
            output_buffer2()
   
def procoutside(buf):
   global buffer2
   buffer2 += buf

class bufferedread:         # I'm not sure if python does this anyway
   def __init__(self,fnm):
      self.buffer = None
      self.buffsize = 10000
      self.fi = open(fnm,"rb")
      self.curpos = 0
      self.maxpos = 0
   
   def readchar(self):
      if self.curpos == self.maxpos:
         self.buffer = self.fi.read(self.buffsize)
         self.maxpos = len(self.buffer)
         self.curpos = 0
         if self.buffer == '':   # EOF
            return None
            
      self.curpos += 1
      return self.buffer[self.curpos-1]

   def close(self):
      self.fi.close()
      self.fi = None
      
# uniform pattern in planet.osm
# XYZ id='xyz'
ma = re.compile("^(.+?)\sid='(.+?)'")
ema = re.compile("^/(.+?)$")

f = bufferedread(xmlfilename)
x = f.readchar()
buf = ""

errout = open(errorfile,"wb")

inbrac = False
mode = 0
while x != None:
   c = ord(x)
   if mode == 0:
      # UTF-8 conditions
      if (0x00 <= c) and (c <= 0x7f): 
         pass  # ascii
      elif (0xc0 <= c) and (c <= 0xdf):
         mode = 1  # one octet follows
      elif (c == 0xe0):
         mode = 2  # two octets follow
      elif (0xf0 <= c) and (c <= 0xf7):
         mode = 3  # three byte follow
      else:
         error()
   else:
      if (c < 0x80) or (c>0xBF):
         error()
         mode = 0
      else:
         mode -= 1
      
   if inbrac:
      if x == '>':
         procinside(buf)
         buf = ""
         inbrac = False
      else:
         buf += x
   else:
      if x == '<':
         procoutside(buf)
         buf = ""
         inbrac = True
      else:
         buf += x
   x = f.readchar()
f.close()  
errout.close()
   
print counter, "errors found"
