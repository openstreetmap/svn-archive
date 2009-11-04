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


Weightings = { \
	'motorway': {'car':10},
	'trunk': {'car':10, 'cycle':0.05},
	'primary': {'cycle': 0.3, 'car':2, 'foot':1, 'horse':0.1},
	'secondary': {'cycle': 1, 'car':1.5, 'foot':1, 'horse':0.2},
	'tertiary': {'cycle': 1, 'car':1, 'foot':1, 'horse':0.3},
	'unclassified': {'cycle': 1, 'car':1, 'foot':1, 'horse':1},
	'minor': {'cycle': 1, 'car':1, 'foot':1, 'horse':1},
	'cycleway': {'cycle': 3, 'foot':0.2},
	'residential': {'cycle': 3, 'car':0.7, 'foot':1, 'horse':1},
	'track': {'cycle': 1, 'car':1, 'foot':1, 'horse':1, 'mtb':3},
	'service': {'cycle': 1, 'car':1, 'foot':1, 'horse':1},
	'bridleway': {'cycle': 0.8, 'foot':1, 'horse':10, 'mtb':3},
	'footway': {'cycle': 0.2, 'foot':1},
	'steps': {'foot':1, 'cycle':0.3},
	'rail':{'train':1},
	'light_rail':{'train':1},
	'subway':{'train':1}
	}

def getWeight(transport, wayType):
	try:
		return(Weightings[wayType][transport])
	except KeyError:
		# Default: if no weighting is defined, then assume it can't be routed
		return(0)

