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


from route import Router

class RouteOrDirect(Router):
	def __init__(self, data):
		Router.__init__(self,data)
		self.route = {'valid':False}
		self.mode = 'direct';
	def setMode(self, mode):
		self.mode = mode
	def setStartNode(self,node):
		self.route['startnode'] = node
		self.route['startpos'] = self.data.nodes[node]
	def setEndNode(self,node):
		self.route['endnode'] = node
		self.route['endpos'] = self.data.nodes[node]
	def setStartLL(self,lat,lon,transport):
		self.route['startnode'] = self.data.findNode(lat,lon,transport)
		self.route['startpos'] = (lat,lon)
		self.route['transport'] = transport
	def setEndLL(self,lat,lon,transport):
		self.route['endnode'] = self.data.findNode(lat,lon,transport)
		self.route['endpos'] = (lat,lon)
		self.route['transport'] = transport
	def update(self):
		if(self.mode == 'route'):
			result, newroute = self.doRouteAsLL( \
				self.route['startnode'],
				self.route['endnode'],
				self.route['transport'])
			if result == 'success':
				self.route['route'] = newroute
				self.route['valid'] = True
			else:
				self.route['route'] = [self.route['startpos'],self.route['endpos']]
				self.route['valid'] = True
		else:
			self.route['route'] = []
			self.route['route'].append(self.route['startpos'])
			self.route['route'].append(self.route['endpos'])
			self.route['valid'] = True

	def valid(self):
		return(self.route['valid'])
