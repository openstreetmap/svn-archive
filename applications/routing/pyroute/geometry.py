#!/usr/bin/python
#-----------------------------------------------------------------------------
# Handles geometry on the earth's surface (e.g. bearing/distance)
#
# Usage: 
#   (library code)
#-----------------------------------------------------------------------------
# Copyright 2007, Oliver White
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#-----------------------------------------------------------------------------
import math

def bearing(a,b):
    dlat = math.radians(b[0] - a[0])
    dlon = math.radians(b[1] - a[1])

    dlon = dlon * math.cos(math.radians(a[0]))
    
    return(math.degrees(math.atan2(dlon, dlat)))

def distance(a,b):
    dlat = math.radians(a[0] - b[0])
    dlon = math.radians(a[1] - b[1])

    dlon = dlon * math.cos(math.radians(a[0]))
    #print "d = %f, %f" % (dlat, dlon)
    # todo: mercator proj
    dRad = math.sqrt(dlat * dlat + dlon * dlon)

    c = 40000 # earth circumference,km
    
    return(dRad * c)
    

if(__name__ == "__main__"):
    a = (51.477,-0.4856)
    b = (51.477,-0.4328)

    print bearing(a,b)
