#!/usr/bin/python
#-----------------------------------------------------------------------------
# Menu graphics (icons)
#
# Usage: 
#   (library code for pyroute GUI, not for direct use)
#-----------------------------------------------------------------------------
# Copyright 2007, Oliver White
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
#-----------------------------------------------------------------------------
import cairo
import os
class menuIcons:
  def __init__(self):
    self.images = {}
    self.cantLoad = []
    self.load('blank')
    self.load('generic')
  def load(self,name):
    filename = "icons/bitmap/%s.png" % name
    if(not os.path.exists(filename)):
      print "Can't load %s" % filename
      return(0)
    
    image = cairo.ImageSurface.create_from_png(filename)
    if(not image):
      return(0)
    w = float(image.get_width())
    h = float(image.get_height())
    #print "Loaded %s: %d x %d" % (name,w,h)
    self.images[name] = {'image':image,'w':w,'h':h}
    return(1)
  def draw(self,cr,name,x,y,w,h):
    if not name in self.images.keys():
      if(name in self.cantLoad):
        name = 'generic'
      elif(not self.load(name)):
        self.cantLoad.append(name)
        name='generic'
        
    icon = self.images[name]
    cr.save()
    cr.translate(x,y)
    cr.scale(w / icon['w'], h / icon['h'])
    cr.set_source_surface(icon['image'],0,0)
    cr.paint()
    cr.restore()
  