# -*- coding: UTF8 -*-
'''
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
'''

from location import location
import string

class locationFinder(object):
    '''
    Responsible for generating a list of location objects
    A series of location objects with (latitude/longitude/value)
    This is a root class, specific locationFinder
    implementations should subclass this class!
    '''
    locations=[]
    bounds=None
    
    def __init__(self):
        '''
        Constructor
        '''
    
    def addPoint(self, name, count, longitude,latitude):
        if self.bounds == None:
            self.locations.append(location(name,longitude,latitude,count))
        else:
            if (longitude>=self.bounds[0] and longitude<=self.bounds[2] and latitude>=self.bounds[1] and latitude<=self.bounds[3]):
                self.locations.append(location(name,longitude,latitude,count))
        
    def query(self,query):
        '''
        Use this to populate self.locations with
        a set of location objects
        '''
        pass
    
    def getX(self):
        '''
        returns list of longitudes
        '''
        return [l.long for l in self.locations] 
    
    def getY(self):
        '''
        returns list of latitudes
        '''
        return [l.lat for l in self.locations]
    
    def getZ(self):
        '''
        returns list of heights
        '''
        return [l.value for l in self.locations]
    
    def getNames(self):
        '''
        List of location titles
        '''
        return [l.name for l in self.locations]
    
    def getBounds(self):
        '''
        returns Bottom-left, top-right coords
        (BL long, BL lat, TR long, TR lat)
        '''
        x=self.getX()
        y=self.getY()
        minX=min(x)
        maxX=max(x)
        minY=min(y)
        maxY=max(y)
        return (minX,minY,maxX,maxY)
    