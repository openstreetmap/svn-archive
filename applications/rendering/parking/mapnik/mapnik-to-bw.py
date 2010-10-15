# -*- coding: utf-8 -*-
# by kay

import os
#from xml.dom.minidom import parse, parseString
import pxdom

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

os.chdir( savedPath )
