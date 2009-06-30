#!/usr/bin/env python
# -*- coding: UTF8 -*-

from locationFinder_regularGrid import locationFinder_regularGrid as locator
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
    
    # example using the locationFinder_regularGrid
    
    # coordinate finder
    finder = locator()
      
    # set to contain all possible points
    finder.bounds=(0.0,0.0,4096.0,4096.0)
    
    # find the X,Y,Z points
    
    finder.query("none",steps=4096.0)    
    print finder.getBounds()
    
    PNGfile=r'c:\testxyz.png'

    heatmap=PNGrenderer(finder, 
                        opacity=1.0, 
                        bands=100, 
                        save=False,
                        target= PNGfile,
                        display=True,
                        logarithmic=True)
    heatmap.render()
    
    print "Done!"