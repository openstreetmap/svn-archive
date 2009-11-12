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
    
    @Author : Steven Kay
    
    quick-and-dirty example of a node density heatmap
    takes a partial OSM XML file (such as one downloaded from Geofabrik.de) and
    bins nodes into an array, then plots as a heatmap in matplotlib
    
'''


from pylab import *
import string
import math
import matplotlib.cm as cm
import matplotlib.pyplot as plt
import numpy as np
import math
import random
from matplotlib.mlab import griddata
import matplotlib.image as im
import matplotlib.pylab as pylab
import numpy.ma as ma

from xml.sax import make_parser 
from xml.sax.handler import ContentHandler
import xml.etree.ElementTree as ET


# bottom left corner 8W, 54N
bounds=(-8.0,54.0) # bottom left-corner
cdata = np.zeros((800, 1100)) 

class Way(object):
    
    def __init__(self):
        pass

class Tag_Handler(ContentHandler): 

    # search using sax parser for elements    
    buckets={}
    ways=0
    maxx=0
    maxy=0
    
    def __init__(self): 
        pass
   
    def register(self, lat, lon):
        
        xpos=int((lon-bounds[0])*100.0) # 100 cells per degree
        ypos=int((lat-bounds[1])*100.0)
        try:
            cdata[800-ypos,xpos]+=1
        except:
            # ignore points outside grid
            self.maxx=xpos
            self.maxy=ypos
            
        if self.ways % 100000==0:
            print self.ways
        
    def startElement(self, name, attrs):
        
        if name=='node':            
            self.elementID=attrs.get('id')
            self.register(float(attrs.get('lat')),float(attrs.get('lon')))
            self.ways += 1         
        return 
    
    def characters (self, ch): 
        
        pass
    
    def endElement(self, name):
        
        pass

# parse an osm xml file
print "Parsing"
fi=open(r"d:\xfer\osm data\scotland.xml","r")
parser = make_parser()    
curHandler = Tag_Handler() 
parser.setContentHandler(curHandler) 
parser.parse(fi)
fi.close()

cdata=np.log1p(cdata) # log coloring

imshow(cdata, interpolation='mitchell',cmap=cm.jet,alpha=1.0)
grid(False)
show()