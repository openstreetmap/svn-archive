#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
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


from base import pyrouteModule
from loadOsm import LoadOsm
import os
import urllib

class osmData(pyrouteModule):
	def __init__(self, modules):
		pyrouteModule.__init__(self,modules)
		self.data = LoadOsm(None)

	def defaultFilename(self):
		return("data/routing.osm")

	def loadDefault(self):
		self.load(self.defaultFilename())

	def load(self,filename):
		print "Loading OSM data from %s"%filename
		self.data.loadOsm(filename)

	def download(self,params):
		ownpos = self.get('ownpos')
		if(not ownpos['valid']):
			print "Can only download around own position"
			return

		lat = ownpos['lat']
		lon = ownpos['lon']
		size = 0.2

		url = "http://informationfreeway.org/api/0.5/way[bbox=%1.4f,%1.4f,%1.4f,%1.4f][highway|railway|waterway=*]" % (lon-size,lat-size,lon+size,lat+size)

		print "downloading %s" % url

		filename = self.defaultFilename()

		if(os.path.exists(filename)):
			print "Removing existing file"
			os.remove(filename)

		urllib.urlretrieve(url, filename)

		print "Finished downloading"
		self.load(filename)
