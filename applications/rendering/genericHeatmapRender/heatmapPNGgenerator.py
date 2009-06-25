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


import numpy as np
from matplotlib.mlab import griddata
import matplotlib.pyplot as plt
import matplotlib.image as im
import matplotlib.pylab as pylab
import numpy.ma as ma
from numpy.random import uniform
from numpy.random import random_integers
import string
from location import location
import math

class heatmapPNGgenerator(object):
    '''
    Uses matplotlib to generate a PNG of a heatmap
    (currently, on the local file system only)
    Using PNG as output to allow alpha transparency
    replace the following attributes with paramaters
    when calling Render() to tweak the settings
    '''

    showContours=False      #show contours?
    showLocations=False     #show locations?
    showFill=True           #fill bands?
    logarithmic=True        #log10 scale
    bands=50                #number of contour bands
    opacity=0.7             #opacity
    colorscheme=plt.cm.jet  #colour scheme
    target="c:\\w00t.png"   #save to filename
    title="Heatmap"         #not used
    save=True               #save to file
    display=False           #show on Matplotlib window
    resolution=(400,300)    #grid resolution (width, height). May want to increase for larger areas to reduce pixelisation (but takes longer)

    def __init__(self,_locations,**kwargs):
        '''
        Constructor, allow override of defaults shown above
        e.g. hmg=heatmapPNGgenerator(mylocations,opacity=0.4,logarithmic=False,bands=10,resolution=(200,100))
        '''
        self.locations=_locations
        if len(kwargs)>0:
            # overwrite defaults shown above.
            for key in kwargs.keys():
                self.__setattr__(key,kwargs[key])
            
    def render(self):
        '''
        Render to target png
        '''
        x=self.locations.getX()
        y=self.locations.getY()
        z=self.locations.getZ()
        
        assert not(self.save and self.display), "cannot call render() with BOTH save and display options set to True."
        assert len(x)==len(y)==len(z), "x, y and z arrays must be same size"
        assert len(x)>2, "Need at least 3 points to generate a polygon  (got %d)" % len(x)
        
        if self.logarithmic: z=[math.log10(u) for u in z]
        minx,miny,maxx,maxy=self.locations.getBounds()
        print "Bounds BL(%.2f,%.2f) TR(%.2f,%.2f)" % (minx,miny,maxx,maxy)
    
        # max fullscreen...
        fig=plt.figure()
        left, bottom, width, height = (0.00,0.0,1.0,1.0) 
        llbounds = (left,bottom,width,height) 
        ax = fig.add_axes(llbounds) 
        fig.figurePatch.set_alpha(0.8)
        
        # define grid.
        xi = np.linspace(minx,maxx,self.resolution[0])
        yi = np.linspace(miny,maxy,self.resolution[1])
        
        # grid the data.
        zi = griddata(x,y,z,xi,yi)
        
        # contour the gridded data, plotting dots at the randomly spaced data points.
        if self.showContours : CS = plt.contour(xi,yi,zi,bands,linewidth=0.0,colors='k')
        if self.showFill: CS = plt.contourf(xi,yi,zi,self.bands,alpha=self.opacity,linewidth=0.0,cmap=self.colorscheme)
        
        # plot data points.
        if self.showLocations: plt.scatter(x,y,marker='+',c='b',s=5)
    
        plt.xlim(minx,maxx)
        plt.ylim(miny,maxy)
        plt.title(str(self.title))
        if self.display: plt.show()
        if self.save: plt.savefig(self.target,transparent=True) 