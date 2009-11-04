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


from struct import *

def encodeLL(lat,lon):
	pLat = (lat + 90.0) / 180.0
	pLon = (lon + 180.0) / 360.0
	iLat = encodeP(pLat)
	iLon = encodeP(pLon)
	return(pack("II", iLat, iLon))

def encodeP(p):
	i = int(p * 4294967296.0)
	return(i)


def decodeLL(data):
	iLat,iLon = unpack("II", data)
	pLat = decodeP(iLat)
	pLon = decodeP(iLon)
	lat = pLat * 180.0 - 90.0
	lon = pLon * 360.0 - 180.0
	return(lat,lon)

def decodeP(i):
	p = float(i) / 4294967296.0
	return(p)
