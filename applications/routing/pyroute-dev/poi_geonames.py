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


from poi_base import *
from xml.sax import make_parser, handler
import feedparser

class geonamesParser(handler.ContentHandler):
	def __init__(self, filename):
		self.entries = []
		parser = make_parser()
		parser.setContentHandler(self)
		parser.parse(filename)
	def startElement(self, name, attrs):
		if(name == "entry"):
			self.entry = {}
		self.text = ''
	def characters(self, text):
		self.text = self.text + text
	def endElement(self, name):
		if(name in ('title')):
			self.entry[name] = self.text
		if(name in ('lat','lng')):
			self.entry[name] = float(self.text)
		if(name == "entry"):
			self.entries.append(self.entry)

class geonames(poiModule):
		def __init__(self, modules):
				poiModule.__init__(self, modules)
				g = geonamesParser("data/wiki.xml")
				self.add("Wikipedia entries",g)

		def add(self, name, parser):
				group = poiGroup(name)
				count = 0
				for item in parser.entries:
						x = poi(item['lat'], item['lng'])
						x.title = item['title']
						group.items.append(x)
						count = count + 1
				print "Added %d items from %s" % (count, name)

				self.groups.append(group)

if __name__ == "__main__":
		g = geonamesParser("data/wiki.xml")
