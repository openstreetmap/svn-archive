#!/usr/bin/python
#-----------------------------------------------------------------------------
# Graphics library for drawing pyroute GUI menus
#
# Usage: 
#   (library code for pyroute GUI)
#
# Features:
#   Start by initialising a 'rectangle' that covers a certain bit of screen
#   Then you can
#     * Draw an icon there
#     * Draw text in the rectangle
#     * Make it clickable (so that it triggers an event when clicked)
#     * Split it into smaller rectangles (to do complex GUI layouts)
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

class overlayArea:
    def __init__(self,cr,x,y,dx,dy,modules,iconSet):
        self.cr = cr
        self.x1 = x;
        self.y1 = y;
        self.x2 = x + dx;
        self.y2 = y + dy;
        self.w = dx
        self.h = dy
        self.cx = x + 0.5 * dx
        self.cy = y + 0.5 * dy
        self.event = None
        self.modules = modules
        self.iconSet = iconSet
    def fill(self,r,g,b,outline=0):
        self.cr.set_source_rgb(r,g,b)
        self.cr.rectangle(self.x1,self.y1,self.w,self.h)
        if(outline):
            self.cr.stroke()
        else:
            self.cr.fill()
    def drawTextSomewhere(self,text,px1,py1,px2,py2):
        innerBox = self.copyself(px1,py1,px2,py2)
        innerBox.drawText(text)
    def drawText(self,text):
        self.cr.select_font_face('Verdana', cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
        test_fontsize = 60
        self.cr.set_font_size(test_fontsize)
        xbearing, ybearing, textwidth, textheight, xadvance, yadvance = self.cr.text_extents(text)
        # Resize the font to fit
        ratiox = textwidth / self.w
        ratioy = textheight / self.h
        ratio = max(ratiox,ratioy)
        
        self.cr.set_font_size(test_fontsize / ratio)
        textwidth = textwidth / ratio
        textheight = textheight / ratio

        marginx = 0.5 * (self.w - textwidth)
        marginy = 0.5 * (self.h - textheight)
        # Text
        self.cr.move_to(self.x1 + marginx, self.y2 - marginy)
        self.cr.set_source_rgba(0, 0, 0, 0.5)
        self.cr.show_text(text)
        
    def button(self,text=None,event=None,icon=None):
        # Rectangle
        if(not icon):
          if(text):
            self.iconSet.draw(self.cr,"generic",self.x1,self.y1,self.w,self.h)
          else:
            self.iconSet.draw(self.cr,"blank",self.x1,self.y1,self.w,self.h)
        else:
            self.iconSet.draw(self.cr,icon,self.x1,self.y1,self.w,self.h)
        if(text):
            self.drawTextSomewhere(text,0.2,0.6,0.8,0.8)
        if(event):
            self.setEvent(event)
    def setEvent(self,text):
        self.event = text
    def xc(self,p):
        return(self.x1 + p * self.w)
    def yc(self,p):
        return(self.y1 + p * self.h)
    def copyself(self,px1,py1,px2,py2):
        x1 = self.xc(px1)
        y1 = self.yc(py1)
        return(overlayArea( \
            self.cr,
            x1,
            y1,
            self.xc(px2) - x1,
            self.yc(py2) - y1,
            self.modules,
            self.iconSet))
    def xsplit(self,p):
        a = self.copyself(0,0,p,1)
        b = self.copyself(p,0,1,1)
        return(a,b)
    def ysplit(self,p):
        a = self.copyself(0,0,1,p)
        b = self.copyself(0,p,1,1)
        return(a,b)
    def xsplitn(self,px1,py1,px2,py2,n):
        dpx = (px2 - px1) / n
        cells = []
        for i in range(0,n-1):
            px = px1 + i * dpx
            cells.append(self.copyself(px,py1,px + dpx,py2))
        return(cells)
    def ysplitn(self,px1,py1,px2,py2,n):
        dpy = (py2 - py1) / n
        cells = []
        for i in range(0,n):
            py = py1 + i * dpy
            cells.append(self.copyself(px1,py,px2,py+dpy))
        return(cells)
            
    def contains(self,x,y):
        if(x > self.x1 and x < self.x2 and y > self.y1 and y < self.y2):
            return(1)
    def handleClick(self,x,y):
        if(self.event):
          self.modules['events'].handleEvent(self.event)
          return(1)
        return(0)
