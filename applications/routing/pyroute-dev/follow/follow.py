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


from time import *
from xml.sax import make_parser, handler
import sys

class LoadTrack(handler.ContentHandler):
	def __init__(self, filename):
		self.inTrkpt = 0
		self.inTime = 0
		self.samples = []
		parser = make_parser()
		parser.setContentHandler(self)
		parser.parse(filename)

	def startElement(self, name, attrs):
		"""Handle XML elements"""
		if name == "trkpt":
			self.tags = { \
				'lat':float(attrs.get('lat')),
				'lon':float(attrs.get('lon'))}
			self.inTrkpt = 1
		if name == "time":
			if self.inTrkpt:
				self.inTime = 1
				self.timeStr = ''

	def characters(self, content):
		if(self.inTime):
			self.timeStr = self.timeStr + content

	def endElement(self, name):
		if name == 'time':
			if(self.inTime):
				self.inTime = 0
				self.tags['t'] = mktime(strptime(self.timeStr[0:-1], "%Y-%m-%dT%H:%M:%S"))
		if name == 'trkpt':
			self.samples.append(self.tags)
			self.inTrkpt = 0

	def play(self, filename):
		for pos in self.samples:
			file = open(filename, 'w')
			file.write("%f,%f\n" % (pos['lat'],pos['lon']))
			file.close
			sleep(0.5)

if __name__ == "__main__":
	track = LoadTrack(sys.argv[1])
	track.play(sys.argv[2])
