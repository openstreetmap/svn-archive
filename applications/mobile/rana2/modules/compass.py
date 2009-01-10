#!/usr/bin/python
#----------------------------------------------------------------------
# GTK widget to display current heading
#----------------------------------------------------------------------
# Copyright 2007-2009, Oliver White
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
#----------------------------------------------------------------------
from _base import *
from _map import *
from _menu import page_button
from _cairo_widget import *
import gtk
import math
import time
import cairo

class CircleDrawer:
  def __init__(self, cr, cx,cy,r, rotation=0):
    self.cr = cr
    self.cr.translate(cx, cy)
    self.cr.rotate(math.radians(180 - rotation))
    self.r = r

  def arc(self, r,g,b, portion_radius, fill, start, end,x=0,y=0):
    self.cr.set_source_rgb(r,g,b)
    self.cr.arc(x,y, self.r * portion_radius, start, end)
    if(fill):
      self.cr.fill()
    else:
      self.cr.stroke()

  def circle(self, r,g,b, fill=True, portion_radius=1.0):
    self.arc(r,g,b, portion_radius, fill, 0, 2.0*math.pi)
    
  def tickmark(self,angle,length=10):
    rad = math.radians(angle)
    self.cr.rotate(rad)
    self.cr.move_to(0,self.r)
    self.cr.rel_line_to(0,-length)
    self.cr.stroke()
    self.cr.rotate(-rad)

class CompassWidget(cairo_widget): # TODO: derive from cairo widget
  def __init__(self, d):
    cairo_widget.__init__(self, d)

  def draw(self, cr):
    if(self.d.get("test_compass", False)):
      self.drawCompass(cr, 5*time.time() % 360, 10)
    else:
      self.drawCompass(cr, self.d.get("heading", None), self.d.get("speed_mph", None))
  
  def drawCompass(self, cr, angle, speed):
    if(speed != None and speed < 1.5):
      angle = 0

    if(angle != None):
      if(angle < 0):
        angle += 360
      if(angle >= 360):
        angle -= 360

    self.background(0,0,0)

    cx = self.rect.width / 2
    cy = self.rect.height - cx
    radius = cx - 10

    cr.set_line_width(3)

    # Non-rotating portion
    cr.set_source_rgb(0,0.7,0)
    t1_size = 0.1 * radius
    t2_size = 0.1 * radius
    cr.line_to(cx+0.5*t1_size, cy-radius-t1_size)
    cr.line_to(cx+0.5*t1_size, t2_size)
    cr.line_to(cx+1.0*t2_size, t2_size)
    cr.line_to(cx, 0)
    cr.line_to(cx-1.0*t2_size, t2_size)
    cr.line_to(cx-0.5*t1_size, t2_size)
    cr.line_to(cx-0.5*t1_size, cy-radius-t1_size)
    cr.fill()
    
    heading_readout = 0.10 * radius
    self.cr.select_font_face("Sans")
    cr.move_to(cx,0)
    cr.line_to(cx,cy-radius)
    cr.line_to(cx+heading_readout,cy-radius-heading_readout)

    if(angle != None):
      text = "%03.0f = %s" % (angle, self.compassMark(angle))
    else:
      text = "Unknown"

    cr.set_font_size(30)
    cr.show_text(text)
    cr.stroke()
    
    if(speed != None):
      if(speed < 1.5):
        text = "Stopped"
      else:
        text = "%1.0f mph" % speed
      text_height = cr.text_extents(text)[3]
      cr.move_to(10,10+text_height)
      cr.set_font_size(30)
      cr.show_text(text)
      cr.stroke()
    
    # Rotating portion of the compass
    c = CircleDrawer(cr, cx,cy,radius, (angle != None) and angle or 0)
    c.circle(0.5,0,0.3)
    c.circle(1,1,1, False)

    if(angle != None):
      for a in range(36):
        if(a == 0):
          length = 1.0
        elif(a % 9 == 0):
          length = 0.3
        elif(a % 3 == 0):
          length = 0.13
        else:
          length = 0.06
        c.tickmark(a * 10, length*radius)
  
      c.arc(1,0.9,0.9, 0.2, True, 0, 2*math.pi, 0, 0.5*radius)
      
      triangle_size = 0.2 * radius
      cr.move_to(0, radius)
      cr.rel_line_to(-0.5*triangle_size, -triangle_size)
      cr.rel_line_to(triangle_size, 0)
      cr.fill()

  def compassMark(self, angle):
    angles = ('N','NNE','NE','ENE','E','ESE','SE','SSE','S','SSW','SW','WSW','W','WNW','NW','NNW')
    num = len(angles)
    sector = 360.0 / num
    index = int((angle + 0.5*sector) / sector)
    index %= num
    return(angles[index])

class compass(RanaModule):
  """Displays a compass"""
  def __init__(self, m, d, name):
    RanaModule.__init__(self,m,d,name)
    
  def startup(self):
    self.registerPage('compass')

  def page_compass(self):

    main_box = gtk.EventBox()
    if(0):
      main_box.connect("button_press_event", lambda w,e: self.pressed(e))
      main_box.connect("button_release_event", lambda w,e: self.released(e))
      main_box.connect("motion_notify_event", lambda w,e: self.moved(e))
    self.main_widget = CompassWidget(self.d)
    main_box.add(self.main_widget)

    layout = gtk.VBox(False)
    layout.pack_start(main_box, True, True, 0)
    layout.pack_start(page_button(self, "Menu", "menu"), False, True, 0)
    return(layout)

  def update(self):
    self.main_widget.update()

