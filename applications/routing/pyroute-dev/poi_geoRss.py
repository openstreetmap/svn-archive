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
import feedparser

class geoRss(poiModule):
	def __init__(self, modules, filename):
		poiModule.__init__(self, modules)
		f = open(filename,"r")
		try:
			for line in f:
				url = line.rstrip()
				if(url):
					feed = feedparser.parse(url)
					name = feed.feed.title
					self.add(name, feed)
		finally:
			f.close()
		print self.groups

	def getLatLon(self,item):
		# Try getting the "point = lat lon"
		try:
			lat,lon = [float(ll) for ll in item['point'].split(" ")]
			return(lat,lon,True)
		except KeyError:
			pass
		except ValueError:
			pass

		# Try getting the "pos = lat lon"
		try:
			lat,lon = [float(ll) for ll in item['pos'].split(" ")]
			return(lat,lon,True)
		except KeyError:
			pass
		except ValueError:
			pass

		# Try getting the "geo:lat = lat, geo:lon = lon"
		try:
			lat = float(item['geo_lat'])
			lon = float(item['geo_lon'])
			return(lat,lon,True)
		except KeyError:
			pass

		return(0,0,False)

	def add(self, name, feed):
		group = poiGroup(name)
		count = 0
		for item in feed.entries:
			lat, lon, valid = self.getLatLon(item)
			if(valid):
				x = poi(lat,lon)
				x.title = item.title
				group.items.append(x)
				count = count + 1
		#print "Added %d items from %s" % (count, name)

		self.groups.append(group)

if __name__ == "__main__":
	g = geoRss('../Setup/feeds.txt')
	name = g.groups.keys()[0]
	print g.groups[name].items

