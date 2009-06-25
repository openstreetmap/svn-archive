#!/usr/bin/env python
# -*- coding: UTF8 -*-

from locationFinder_flickr_locations import locationFinder_flickr_locations as locator
from heatmapPNGgenerator import heatmapPNGgenerator as PNGrenderer
from KMLGenerator import KMLGenerator

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

if __name__ == '__main__':
    
    # coordinate finder
    finder = locator()
      
    # bounds is used for the flickR query as the data set contains lots of outliers.
    finder.bounds=(-30.0,0.0,20.0,80.0)
    
    # find the X,Y,Z points
    # note that you can query several areas;
    # the query() calls are cumulative, and do not
    # override the previous results! This means that
    # several neighbouring areas can be mapped.
    
    finder.query("/United+Kingdom/London/London","region")
    finder.query("/United+Kingdom/Wales/Cardiff","region")
    finder.query("/United+Kingdom/Scotland/Edinburgh","region")
    finder.query("/United+Kingdom/Northern+Ireland/Belfast","region")
    
    print finder.getBounds()
    
    PNGfile=r'c:\testxyz.png'
    KMLfile=r'c:\testxyz.kml'
    heatmap=PNGrenderer(finder, 
                        opacity=0.66, 
                        bands=100, 
                        save=True,
                        target= PNGfile,
                        display=False,
                        logarithmic=True)
    heatmap.render()
    KMLGenerator().render(finder, PNGfile, KMLfile)
    print "Done!"