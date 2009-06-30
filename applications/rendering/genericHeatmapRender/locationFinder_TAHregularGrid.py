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
from locationFinder import locationFinder
import random
import re

#path to a local copy of the tiles directory dump
#get an up-to-date copy here:-
#http://tah.openstreetmap.org/media/filesizes.bz2

LOCALCOPY = r"c:\filesizes.txt" 

class locationFinder_TAHregularGrid(locationFinder):
    '''
    Responsible for generating a list of location objects
    this implementation generates a grid of regularly spaced
    points (and generates random values for each)
    '''
           
    def __init__(self):
        '''
        Constructor
        '''
        
    def query(self,query=None,peturb=0.05,sampleevery=100,steps=200):
        '''
        generate a regular grid of points
        from the TAH directory dump
        
        query is not used at present
        steps is resolution on both axes
        sampleevery is the random sample rate (e.g. 100=choose 1 in every 100 points)
        set sampleevery to None to include all points
        
        peturb can be used to add a small random nudge to each point
        this can improve the aesthetics when zoomed in by 
        softening the 'diamond' effect.
        '''
        
        print "generating mesh"
        
        if sampleevery==None:
            threshold=0.0
        else:
            threshold=1.0-(1.0/sampleevery)
        bbox=self.bounds
        xstep=float((bbox[2]-bbox[0])/float(steps))
        ystep=float((bbox[3]-bbox[1])/float(steps))
        maxy=int(steps)
        maxx=int(steps)
        
        fi=open(LOCALCOPY)

        recog=re.compile("^.*\s+.*\s+.*\s+.*\s+(?P<size>\d+)\s+.*\s+.*\s+(?P<X>\d+)[_](?P<Y>\d+)$")
        started=False
        for line in fi:
            if line.find('/0000')<>-1:
                started=True
            if started:
                m=recog.match(line)
                if m:
                    if random.random()>=threshold:
                        x=int(m.groupdict()['X'])
                        y=4096-int(m.groupdict()['Y'])
                        z=int(m.groupdict()['size'])
                        yy=bbox[1]+(y*ystep)
                        xx=bbox[0]+(x*xstep)+(random.random()*peturb)
                        self.addPoint('', z, xx,yy+(random.random()*peturb))
                        #print "%s,%s,%s" % (x,y,z)
        fi.close()
        print 'Done mesh'