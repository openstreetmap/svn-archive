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
    def load(self,name):
        filename = "icons/bitmap/%s.png" % name
        if(not os.path.exists(filename)):
            return(0)
        self.images[name] = cairo.ImageSurface.create_from_png(filename)
        if(self.images[name]):
            return(1)
        return(0)
    def draw(self,cr,name,x,y,w,h):
        if not name in self.images.keys():
            if(name in self.cantLoad):
                return
            if(not self.load(name)):
                self.cantLoad.append(name)
                return
        imagesize = 120.0
        cr.save()
        cr.translate(x,y)
        cr.scale(w / imagesize, h / imagesize)
        cr.set_source_surface(self.images[name],0,0)
        cr.paint()
        cr.restore()
    