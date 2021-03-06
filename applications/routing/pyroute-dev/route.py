#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Routing for OSM data

Usage as library:
	router = Router(LoadOsmObject)
	result, route = router.doRoute(node1, node2, transport)

Usage from command-line:
	route.py filename.osm node1 node2 transport

(where transport is cycle, foot, car, etc...)
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


from loadOsm import *
import math
import sys

class Router:
	def __init__(self, data):
		self.data = data
	def distance(self,n1,n2):
		"""Calculate distance between two nodes"""
		lat1 = self.data.nodes[n1][0]
		lon1 = self.data.nodes[n1][1]
		lat2 = self.data.nodes[n2][0]
		lon2 = self.data.nodes[n2][1]
		# TODO: projection issues
		dlat = lat2 - lat1
		dlon = lon2 - lon1
		dist2 = dlat * dlat + dlon * dlon
		return(math.sqrt(dist2))
	def doRouteAsLL(self,start,end,transport):
		result, nodes = self.doRoute(start,end,transport)
		if(result != 'success'):
			return(result,[])
		pos = []
		for node in nodes:
			lat,lon = self.data.nodes[node]
			pos.append((lat,lon))
		return(result,pos)
	def doRoute(self,start,end,transport):
		"""Do the routing"""
		self.searchEnd = end
		closed = [start]
		self.queue = []

		# Start by queueing all outbound links from the start node
		blankQueueItem = {'end':-1,'distance':0,'nodes':str(start)}
		try:
			for i, weight in self.data.routing[transport][start].items():
				self.addToQueue(start,i, blankQueueItem, weight)
		except KeyError:
			return('no_such_node',[])

		# Limit for how long it will search
		count = 0
		while count < 10000:
			count = count + 1
			try:
				nextItem = self.queue.pop(0)
			except IndexError:
				# Queue is empty: failed
				return('no_route',[])
			x = nextItem['end']
			if x in closed:
				continue
			if x == end:
				# Found the end node - success
				routeNodes = [int(i) for i in nextItem['nodes'].split(",")]
				return('success', routeNodes)
			closed.append(x)
			try:
				for i, weight in self.data.routing[transport][x].items():
					if not i in closed:
						self.addToQueue(x,i,nextItem, weight)
			except KeyError:
				pass
		else:
			return('gave_up',[])

	def addToQueue(self,start,end, queueSoFar, weight = 1):
		"""Add another potential route to the queue"""

		# If already in queue
		for test in self.queue:
			if test['end'] == end:
				return
		distance = self.distance(start, end)
		if(weight == 0):
			return
		distance = distance / weight

		# Create a hash for all the route's attributes
		distanceSoFar = queueSoFar['distance']
		queueItem = { \
			'distance': distanceSoFar + distance,
			'maxdistance': distanceSoFar + self.distance(end, self.searchEnd),
			'nodes': queueSoFar['nodes'] + "," + str(end),
			'end': end}

		# Try to insert, keeping the queue ordered by decreasing worst-case distance
		count = 0
		for test in self.queue:
			if test['maxdistance'] > queueItem['maxdistance']:
				self.queue.insert(count,queueItem)
				break
			count = count + 1
		else:
			self.queue.append(queueItem)

if __name__ == "__main__":
	data = LoadOsm(sys.argv[1])

	try:
		transport = sys.argv[4]
	except IndexError:
		transport = 'cycle'
		print "WARNING: No transport type specified, assuming \"%s\"" % transport

	router = Router(data)
	result, route = router.doRouteAsLL(int(sys.argv[2]), int(sys.argv[3]), transport)

	if result == 'success':
		print "Route: %s" % ",".join("%1.4f,%1.4f" % (i[0],i[1]) for i in route)
	else:
		print "Failed (%s)" % result

