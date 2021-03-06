#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Interface for things which can be listed

Usage:
	(base-class for pyroute GUI libraries)
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


class listable:
	"""Base-class (interface) for anything that can be listed in a menu"""
	def numItems(self):
		"""Return how many items are available"""
		return(0)

	def getItemText(self,n):
		"""Return the label for any item number"""
		return("-")

	def getItemStatus(self,n):
		"""Return current status for any item number"""
		return("-")

	def isLocation(self,n):
		"""Return true if the item represents a location"""
		return(False)

	def getItemLatLon(self,n):
		"""If the item represents a location, return it's position"""
		return(0,0)

	def getItemClickable(self,n):
		"""If not a location, return true if the item should have a button to click on"""
		return(False)

	def getItemAction(self,n):
		"""If the item isn't a location, return what happens if it's selected"""
		return("")

