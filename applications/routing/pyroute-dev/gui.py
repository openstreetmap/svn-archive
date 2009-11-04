#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Pyroute main GUI

Usage:
	gui.py

Controls:
	* drag left/right along top of window to zoom in/out
	* drag the map to move around
	* click on the map for a position menu
			* set your own position, if the GPS didn't already do that
			* set that location as a destination
			* route to that point
	* click on the top-left of the window for the main menu
			* download routing data around your position
			* browse geoRSS, wikipedia, and OSM points of interest
			* select your mode of transport for routing
					* car, bike, foot currently supported
			* toggle whether the map is centred on your GPS position
"""

__version__ = "$Rev$"[1:-2]
__license__ = """This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>."""
_debug = 0


from gtk import gdk
from math import sqrt
from time import clock
import cairo
import gobject
import gtk
import os
import pygtk
import sys
import urllib
pygtk.require('2.0')

# Our modules:
from base import pyrouteModule
from dataStore import *
from events import pyrouteEvents
from geoPosition import *
from meta import moduleInfo
from mod_osmData import osmData
from overlay import *
from poi_geoRss import geoRss
from poi_geonames import geonames
from poi_osm import osmPoiModule
from poi_waypoints import waypointsModule
from projection import Projection
from routeOrDirect import *
from sketch import sketching
from tilenames import *
from tiles import tileHandler
from tracklog import tracklog

def update(mapWidget):
	mapWidget.update();
	return(True)

class MapWidget(gtk.Widget, pyrouteModule):
	__gsignals__ = { \
		'realize': 'override',
		'expose-event' : 'override',
		'size-allocate': 'override',
		'size-request': 'override'
		}
	def __init__(self):
		gtk.Widget.__init__(self)
		self.draw_gc = None

		self.modules = {'poi':{}}
		pyrouteModule.__init__(self, self.modules)

		self.loadModules()
		self.blankData()
		self.loadData()

		self.updatePosition()

		self.timer = gobject.timeout_add(100, update, self)

	def loadModules(self):
		self.modules['poi']['rss'] = geoRss(self.modules,
																				os.path.join(os.path.dirname(__file__),
																										 'Setup', 'feeds.txt'))
		#self.modules['poi']['geonames'] = geonames(self.modules)
		#self.modules['poi']['waypoints'] = waypointsModule(self.modules, "data/waypoints.gpx")
		self.modules['poi']['osm'] = osmPoiModule(self.modules)
		self.modules['overlay'] = guiOverlay(self.modules)
		self.modules['position'] = geoPosition()
		self.modules['tiles'] = tileHandler(self.modules)
		self.modules['data'] = DataStore(self.modules)
		self.modules['events'] = pyrouteEvents(self.modules)
		self.modules['sketch'] = sketching(self.modules)
		self.modules['osmdata'] = osmData(self.modules)
		self.modules['projection'] = Projection()
		self.modules['tracklog'] = tracklog(self.modules)
		self.modules['meta'] = moduleInfo(self.modules)
		self.modules['route'] = RouteOrDirect(self.modules['osmdata'].data)

	def blankData(self):
		self.set('ownpos', {'valid':False})
		self.set('mode','cycle')
		self.set('centred',False)
		self.set('logging',True)
		self.m['projection'].recentre(51.3,-0.1, 9)

	def loadData(self):
		self.m['sketch'].load("data/sketches/latest.gpx")
		self.m['tracklog'].load("data/track.gpx")
		self.m['osmdata'].loadDefault()

	def beforeDie(self):
		print "Handling last few checks before we close"
		self.m['tracklog'].checkIfNeedsSaving(True)

	def update(self):
		self.updatePosition()
		if(self.get("needRedraw")):
			self.forceRedraw()

	def updatePosition(self):
		"""Try to get our position from GPS"""
		newpos = self.modules['position'].get()

		# If the latest position wasn't valid, then don't touch the old copy
		# (especially since we might have manually set the position)
		if(not newpos['valid']):
			return

		#check if new pos is different than current
		oldpos = self.get('ownpos')
		if ((oldpos['valid']) and (oldpos['lon'] == newpos['lon']) and (oldpos['lat'] == oldpos['lat'])):
			return

		# TODO: if we set ownpos and then get a GPS signal, we should decide
		# here what to do
		self.set('ownpos', newpos)

		self.handleUpdatedPosition()

	def handleUpdatedPosition(self):
		pos = self.get('ownpos')
		if(not pos['valid']):
			return

		# Add latest position to tracklog
		if(self.get('logging')):
			self.m['tracklog'].addPoint(pos['lat'], pos['lon'])
			self.m['tracklog'].checkIfNeedsSaving()

		# If we've never actually decided where to display a map yet, do so now
		if(not self.modules['projection'].isValid()):
			print "Projection not yet valid, centering on ownpos"
			self.centreOnOwnPos()
			return

		# This code would let us recentre if we reach the edge of the screen -
		# it's not currently in use
		x,y = self.modules['projection'].ll2xy(pos['lat'], pos['lon'])
		x,y = self.modules['projection'].relXY(x,y)
		border = 0.15
		outsideMap = (x < border or y < border or x > (1-border) or y > (1-border))

		# If map is locked to our position, then recentre it
		if(self.get('centred')):
			self.centreOnOwnPos()
		else:
			self.forceRedraw()

	def centreOnOwnPos(self):
		"""Try to centre the map on our position"""
		pos = self.get('ownpos')
		if(pos['valid']):
			self.modules['projection'].recentre(pos['lat'], pos['lon'])
			self.forceRedraw()

	def mousedown(self,x,y):
		if(self.get('sketch',0)):
			self.m['sketch'].startStroke(x,y)

	def click(self, x, y):
		print "Clicked %d,%d"%(x,y)
		"""Handle clicking on the screen"""
		# Give the overlay a chance to handle all clicks first
		if(self.m['overlay'].handleClick(x,y)):
			pass
		# If the overlay is fullscreen and it didn't respond, the click does
		# not fall-through to the map
		elif(self.m['overlay'].fullscreen()):
			return
		elif(self.get('sketch',0)):
			return
		# Map was clicked-on: store the lat/lon and go into the "clicked" menu
		else:
			lat, lon = self.m['projection'].xy2ll(x,y)
			self.set('clicked', (lat,lon))
			self.set('menu','click')
		self.forceRedraw()

	def forceRedraw(self):
		"""Make the window trigger a draw event.
		TODO: consider replacing this if porting pyroute to another platform"""
		self.set("needRedraw", False)
		try:
			self.window.invalidate_rect((0,0,self.rect.width,self.rect.height),False)
		except AttributeError:
			pass

	def move(self,dx,dy):
		"""Handle dragging the map"""

		# TODO: what happens when you drag inside menus?
		if(self.get('menu')):
			return
		if(self.get('centred') and self.get('ownpos')['valid']):
			return
		self.modules['projection'].nudge(dx,dy)
		self.forceRedraw()

	def zoom(self,dx):
		"""Handle dragging left/right along top of the screen to zoom"""
		self.modules['projection'].nudgeZoom(-1 * dx / self.rect.width)
		self.forceRedraw()

	def handleDrag(self,x,y,dx,dy,startX,startY):
		if(self.modules['overlay'].fullscreen()):
			if(self.modules['overlay'].handleDrag(dx,dy,startX,startY)):
				self.forceRedraw()
		elif(self.get('sketch',0)):
			self.m['sketch'].moveTo(x,y)
			return
		else:
			self.move(dx,dy)


	def draw(self, cr):
		start = clock()
		proj = self.modules['projection']

		# Don't draw the map if a menu/overlay is fullscreen
		if(not self.modules['overlay'].fullscreen()):

			# Map as image
			self.modules['tiles'].draw(cr)

			# The route
			if(self.modules['route'].valid()):
				cr.set_source_rgba(0.5, 0.0, 0.0, 0.5)
				cr.set_line_width(12)
				count = 0
				for i in self.modules['route'].route['route']:
					x,y = proj.ll2xy(i[0],i[1])
					if(count == 0):
						cr.move_to(x,y)
					else:
						cr.line_to(x,y)
					count = count + 1
				cr.stroke()

			# Each plugin can display on the map
			for name,source in self.modules['poi'].items():
				if source.draw:
					for group in source.groups:
						for item in group.items:
							x,y = proj.ll2xy(item.lat, item.lon)
							if(proj.onscreen(x,y)):
								cr.set_source_rgb(0.0, 0.4, 0.0)
								cr.set_font_size(12)
								cr.move_to(x,y)
								cr.show_text(item.title)
								cr.stroke()

			self.m['sketch'].draw(cr,proj)
			self.m['tracklog'].draw(cr,proj)

			pos = self.get('ownpos')
			if(pos['valid']):
				# Us
				x,y = proj.ll2xy(pos['lat'], pos['lon'])
				cr.set_source_rgb(0.0, 0.0, 0.0)
				cr.arc(x,y,14, 0,2*3.1415)
				cr.fill()
				cr.set_source_rgb(1.0, 0.0, 0.0)
				cr.arc(x,y,10, 0,2*3.1415)
				cr.fill()


		# Overlay (menus etc)
		self.modules['overlay'].draw(cr, self.rect)

		#print "Map took %1.2f ms" % (1000 * (clock() - start))

	def do_realize(self):
		self.set_flags(self.flags() | gtk.REALIZED)
		self.window = gdk.Window( \
			self.get_parent_window(),
			width = self.allocation.width,
			height = self.allocation.height,
			window_type = gdk.WINDOW_CHILD,
			wclass = gdk.INPUT_OUTPUT,
			event_mask = self.get_events() | gdk.EXPOSURE_MASK)
		self.window.set_user_data(self)
		self.style.attach(self.window)
		self.style.set_background(self.window, gtk.STATE_NORMAL)
		self.window.move_resize(*self.allocation)
	def do_size_request(self, allocation):
		pass
	def do_size_allocate(self, allocation):
		self.allocation = allocation
		if self.flags() & gtk.REALIZED:
			self.window.move_resize(*allocation)
	def _expose_cairo(self, event, cr):
		self.rect = self.allocation
		self.modules['projection'].setView( \
			self.rect.x,
			self.rect.y,
			self.rect.width,
			self.rect.height)
		self.draw(cr)
	def do_expose_event(self, event):
		self.chain(event)
		cr = self.window.cairo_create()
		return self._expose_cairo(event, cr)

class GuiBase:
	"""Wrapper class for a GUI interface"""
	def __init__(self):
		# Create the window
		win = gtk.Window()
		win.set_title('pyroute')
		win.connect('delete-event', gtk.main_quit)
		win.resize(480,640)
		win.move(50, gtk.gdk.screen_height() - 650)

		# Events
		event_box = gtk.EventBox()
		event_box.connect("button_press_event", lambda w,e: self.pressed(e))
		event_box.connect("button_release_event", lambda w,e: self.released(e))
		event_box.connect("scroll-event", lambda w,e: self.scrolled(e))
		event_box.connect("motion_notify_event", lambda w,e: self.moved(e))
		win.add(event_box)

		# Create the map
		self.mapWidget = MapWidget()
		event_box.add(self.mapWidget)

		# Finalise the window
		win.show_all()
		gtk.main()
		self.mapWidget.beforeDie()

	def pressed(self, event):
		self.dragstartx = event.x
		self.dragstarty = event.y
		#print dir(event)
		#print "Pressed button %d at %1.0f, %1.0f" % (event.button, event.x, event.y)

		self.dragx = event.x
		self.dragy = event.y
		self.mapWidget.mousedown(event.x,event.y)

	def scrolled(self, event):
		if event.direction == gtk.gdk.SCROLL_UP:
			self.mapWidget.modules['projection'].setZoom(1, True)
			self.mapWidget.set("needRedraw", True)

		if event.direction == gtk.gdk.SCROLL_DOWN:
			self.mapWidget.modules['projection'].setZoom(-1, True)
			self.mapWidget.set("needRedraw", True)

	def moved(self, event):
		"""Drag-handler"""

		self.mapWidget.handleDrag( \
			event.x,
			event.y,
			event.x - self.dragx,
			event.y - self.dragy,
			self.dragstartx,
			self.dragstarty)

		self.dragx = event.x
		self.dragy = event.y
	def released(self, event):
		dx = event.x - self.dragstartx
		dy = event.y - self.dragstarty
		distSq = dx * dx + dy * dy
		if distSq < 4:
			self.mapWidget.click(event.x, event.y)


if __name__ == "__main__":
	program = GuiBase()
