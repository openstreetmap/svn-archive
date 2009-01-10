#!/usr/bin/python
#----------------------------------------------------------------------
# GTK widget to display current speed
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
from math import *
import time
import cairo

class Polar:
  def __init__(self,x,y,r, angle=0, angle_scale=1):
    self.x = x
    self.y = y
    self.r = r
    self.angle = angle
    self.angle_scale = angle_scale
  def xy(self, value, portion_r=1.0):
    angle = radians(self.angle + self.angle_scale * value)
    x = self.x + self.r * portion_r * sin(angle)
    y = self.y - self.r * portion_r * cos(angle)
    return(x,y)

class SpeedWidget(cairo_widget): # TODO: derive from cairo widget
  def __init__(self, d):
    cairo_widget.__init__(self, d)
    self.show_average = False
    self.last_meta = None

  def update_meta(self, speed):
    t = time.time()
    if(self.last_meta != None):
      dt = t - self.last_meta
      if(dt > 0 and dt < 10):
        self.distance += speed * dt
        self.time += dt
    self.last_meta = t

  def draw_text(self, cr, text, x, y, size=30):
    cr.set_font_size(size)
    (tw,th) = cr.text_extents(text)[2:4]
    cr.move_to(x,y+th)
    cr.show_text(text)
    cr.stroke()

  def draw(self, cr):
    speed = self.d.get("speed_mph", None)
    
    if(self.d.get("test_speedo", False)):
      speed = 30 + 20 * sin(time.time() / 5)

    fsd = self.d.get("speedo_range", 120) # full-scale deflection, miles per hour
    speed = max(min(speed, fsd),0)

    self.background(0,0,0)

    cx = self.rect.width / 2
    cy = self.rect.height - cx
    radius = cx - 10

    # Background of the dial face
    cr.set_source_rgb(0,0,0.5)
    cr.arc(cx,cy,radius, radians(-180), radians(0))
    cr.fill_preserve()
    cr.set_source_rgb(1,1,0)
    cr.stroke()
    
    # Main text
    cr.set_font_size(80)
    text = "%1.0f mph" % (speed,)
    (tw,th) = cr.text_extents(text)[2:4]
    cr.move_to(30,cy+30+th)
    cr.show_text(text)
    cr.stroke()
    
    # Meta-display (average, distance, timer)
    if(self.show_average):
      self.update_meta(speed)
      if(self.time > 0):
        cr.set_source_rgb(0.6,0.6,0)
        self.draw_text(cr, "Average %1.1f mph" % (self.distance / self.time), 30, cy+137)
        self.draw_text(cr, "%1.1f miles in %1.0fs" % (self.distance / 3600.0, self.time), 30, cy+185)

    p = Polar(cx,cy,radius, -90, 180.0 / fsd)
    
    # The dial face
    cr.set_font_size(18)
    cr.set_line_width(3)
    for s in range(10, fsd, 10):
      (x,y) = p.xy(s, 1)
      cr.move_to(x,y)
      x,y = p.xy(s, 0.9)
      cr.line_to(x,y)
      cr.stroke()

      (tw,th) = cr.text_extents(text)[2:4]
      if(s < 0.3*fsd):
        x1 = x
      elif(s>0.7*fsd):
        x1 = x - tw
      else:
        x1 = x - 0.5 * tw
      cr.move_to(x1, y+5+th)
      
      text = "%d" % s
      cr.show_text(text)
      cr.stroke()
    
    text = "mph"
    (tw,th) = cr.text_extents(text)[2:4]
    cr.move_to(cx+radius-tw-20, cy-0.5*th)
    cr.show_text(text)
    cr.stroke()
    
    # The needle
    cr.set_line_width(5)
    cr.set_source_rgb(1,0,0)
    (x,y) = p.xy(speed, 0.92)
    cr.move_to(cx,cy)
    cr.line_to(x,y)
    cr.stroke()
    cr.arc(cx,cy,10, 0, radians(360))
    cr.fill()
    
  def clicked(self):
    if(self.show_average):
      self.show_average = False
    else:
      self.distance = 0
      self.time = 0
      self.show_average = True
    self.forceRedraw()

class speedo(RanaModule):
  """Displays speed"""
  def __init__(self, m, d, name):
    RanaModule.__init__(self,m,d,name)
    
  def startup(self):
    self.registerPage('speedo')

  def page_speedo(self):

    main_box = gtk.EventBox()
    main_box.connect("button_press_event", lambda w,e: self.main_widget.clicked())
    self.main_widget = SpeedWidget(self.d)
    main_box.add(self.main_widget)

    layout = gtk.VBox(False)
    layout.pack_start(main_box, True, True, 0)
    layout.pack_start(page_button(self, "Menu", "menu"), False, True, 0)
    return(layout)

  def update(self):
    self.main_widget.update()

