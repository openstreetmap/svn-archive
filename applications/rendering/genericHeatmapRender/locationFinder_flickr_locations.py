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


import flickrapi
from location import location
import string
from locationFinder import locationFinder

# Enter your API key below
api_key = '<enter your api key here>' 

class locationFinder_flickr_locations(locationFinder):
    '''
    Responsible for generating a list of location objects
    A series of location objects with (latitude/longitude/value)
    '''
    
    assert api_key <> '<enter your api key here>', 'You need to set up a Flickr API key!'
    flickr = flickrapi.FlickrAPI(api_key)
       
    def __init__(self):
        '''
        Constructor
        '''
        
    def query(self,query,scope,debug=False):
        '''
        Flickr api query example
        query should be a URL
        e.g. United+States/New+York/New+York
        scope should be one of
        {country,region,county,location} etc.
        
        bbox should be a tuple (minlong,minlat,maxlong,maxlat)
        if bbox not None, points are only added if inside the bounding box.
        this is a kludge put in because flickr api sometimes includes outliers in error.
        
        '''
        bbox=self.bounds
        places = self.flickr.places_getInfoByUrl(url=query)
        print "URL %s found successfully" % query
        locality=places.find('.//place/%s' % scope)
        woeid =  locality.get('woeid')
        print "Finding children locations ..."
        places=self.flickr.places_getChildrenWithPhotosPublic(woe_id=woeid)
        for pl in places.find('places'):
            name= (string.split(pl.text,","))[0]
            latitude=float(pl.get('latitude'))
            longitude=float(pl.get('longitude'))
            count=int(pl.get('photo_count'))
            if debug: print "%s has %d photos (%.2f,%.2f)" % (name,count,longitude,latitude)
            self.addPoint(name,count,longitude,latitude)
            