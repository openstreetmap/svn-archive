# -*- coding: utf-8 -*-
# by kay

import os
#from xml.dom.minidom import parse, parseString
import pxdom
import colorsys

def color_to_bw(r,g,b):
    h,l,s = colorsys.rgb_to_hls(r, g, b)
    s = 0
    print l
    return colorsys.hls_to_rgb(h,l,s)

def parse_color(s):
    """ Parses color string in format #ABC or #AABBCC to RGB tuple. """
    l = len(s)
    assert(l in (4,5,7,9))

    if l in (4,5):
        return tuple(int(ch * 2, 16)/255.0 for ch in s[1:])
    else:
        return tuple(int(ch1 + ch2, 16)/255.0 for ch1, ch2 in \
                     zip(
                        (ch1 for ch1 in s[1::2]),
                        (ch2 for ch2 in s[2::2])
                        )
                    )

savedPath = os.getcwd()
os.chdir("original-mapnik")

document = pxdom.parse( "osm.xml", {'entities': 1} )

els = document.getElementsByTagName("CssParameter")
#print "els=",els
for el in els:
    at = el.getAttribute("name")
    if at=="stroke" or at=="fill":
        print "yeah",at," type", el.firstChild.nodeValue
        el.firstChild.nodeValue="xxx"

output= document.implementation.createLSOutput() 
output.systemId= 'file:///tmp/osmbw.xml' 
output.encoding= 'utf-8' 
serialiser= document.implementation.createLSSerializer() 
serialiser.write(document, output)

print "bw=",color_to_bw(0.9,0.5,0)
print "bw=",color_to_bw(0,0.9,0.5)
print "rgb=",parse_color("#112233")
print "rgb=",parse_color("#1f3")

os.chdir( savedPath )
