#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Translates between lat/long and the slippy-map tile
numbering scheme

http://wiki.openstreetmap.org/index.php/Slippy_map_tilenames
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


from math import *

def numTiles(z):
	return(pow(2,z))

def sec(x):
	return(1/cos(x))

def latlon2relativeXY(lat,lon):
	x = (lon + 180) / 360
	y = (1 - log(tan(radians(lat)) + sec(radians(lat))) / pi) / 2
	return(x,y)

def latlon2xy(lat,lon,z):
	n = numTiles(z)
	x,y = latlon2relativeXY(lat,lon)
	return(n*x, n*y)

def tileXY(lat, lon, z):
	x,y = latlon2xy(lat,lon,z)
	return(int(x),int(y))

def xy2latlon(x,y,z):
	n = numTiles(z)
	relY = y / n
	lat = mercatorToLat(pi * (1 - 2 * relY))
	lon = -180.0 + 360.0 * x / n
	return(lat,lon)

def latEdges(y,z):
	n = numTiles(z)
	unit = 1 / n
	relY1 = y * unit
	relY2 = relY1 + unit
	lat1 = mercatorToLat(pi * (1 - 2 * relY1))
	lat2 = mercatorToLat(pi * (1 - 2 * relY2))
	return(lat1,lat2)

def lonEdges(x,z):
	n = numTiles(z)
	unit = 360 / n
	lon1 = -180 + x * unit
	lon2 = lon1 + unit
	return(lon1,lon2)

def tileEdges(x,y,z):
	lat1,lat2 = latEdges(y,z)
	lon1,lon2 = lonEdges(x,z)
	return((lat2, lon1, lat1, lon2)) # S,W,N,E

def mercatorToLat(mercatorY):
	return(degrees(atan(sinh(mercatorY))))

def tileSizePixels():
	return(256)

def tileLayerExt(layer):
	if(layer in ('oam')):
		return('jpg')
	return('png')

def tileLayerBase(layer):
	layers = { \
		"tah": "http://tah.openstreetmap.org/Tiles/tile/",
		"oam": "http://oam1.hypercube.telascience.org/tiles/1.0.0/openaerialmap-900913/",
		"mapnik": "http://tile.openstreetmap.org/mapnik/"
		}
	return(layers[layer])

def tileURL(x,y,z,layer):
	return "%s%d/%d/%d.%s" % (tileLayerBase(layer),z,x,y,tileLayerExt(layer))

if __name__ == "__main__":
	for z in range(0,17):
		x,y = tileXY(51.50610, -0.119888, z)

		s,w,n,e = tileEdges(x,y,z)
		print "%d: %d,%d --> %1.3f :: %1.3f, %1.3f :: %1.3f" % (z,x,y,s,n,w,e)
		#print "<img src='%s'><br>" % tileURL(x,y,z)
