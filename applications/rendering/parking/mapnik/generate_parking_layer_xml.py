# -*- coding: utf-8 -*-
# by kay

import sys,os,subprocess
from optparse import OptionParser
#from xml.dom.minidom import parse, parseString
import pxdom
#import colorsys

simple_colors = {
    'aliceblue': 'f0f8ff',
    'yellowgreen': '9acd32'
    }


"""
def dom_convert_to_grey(document):
    els = document.getElementsByTagName("CssParameter")
    #print "els=",els
    for el in els:
        at = el.getAttribute("name")
        if at=="stroke" or at=="fill":
            col=el.firstChild.nodeValue
            bw=rgb_to_css(color_to_bw(parse_color(col)))
            print "converted {typ} from {a} to {bw}." .format(typ=at,a=col,bw=bw)
            el.firstChild.nodeValue=bw

    #<Map bgcolor="---" srs="+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs +over" minimum_version="0.7.1">
    els = document.getElementsByTagName("Map")
    for el in els:
        col = el.getAttribute("bgcolor")
        assert(col!='')
        assert(col!=None)
        bw=rgb_to_css(color_to_bw(parse_color(col)))
        print "converted {typ} from {a} to {bw}." .format(typ='bgcolor',a=col,bw=bw)
        el.setAttribute("bgcolor",bw)

    #<TextSymbolizer ... fill="#6699cc"/>
    els = document.getElementsByTagName("TextSymbolizer")
    for el in els:
        col = el.getAttribute("fill")
        assert(col!='')
        assert(col!=None)
        bw=rgb_to_css(color_to_bw(parse_color(col)))
        print "converted {typ} from {a} to {bw}." .format(typ='TS-fill',a=col,bw=bw)
        el.setAttribute("fill",bw)
        #<TextSymbolizer halo_fill="#fed7a5"/> (optional)
        col = el.getAttribute("halo_fill")
        assert(col!=None)
        if col!='':
            bw=rgb_to_css(color_to_bw(parse_color(col)))
            print "converted {typ} from {a} to {bw}." .format(typ='TS-halo_fill',a=col,bw=bw)
            el.setAttribute("halo_fill",bw)
"""

def dom_strip_style_and_layer(document,stylename,layername):
    removeElements=[]
    # remove <Style name="points"> and <Layer name="amenity-points">
    els = document.getElementsByTagName("Style")
    for el in els:
        if el.getAttribute("name")==stylename:
            removeElements.append(el)
    els = document.getElementsByTagName("Layer")
    for el in els:
        if el.getAttribute("name")==layername:
            removeElements.append(el)
    print removeElements
    for el in removeElements:
        parent = el.parentNode
        parent.removeChild(el)

def dom_strip_icons(document):
    dom_strip_style_and_layer(document,"points","amenity-points")
    dom_strip_style_and_layer(document,"power_line","power_line")
    dom_strip_style_and_layer(document,"power_minorline","power_minorline")
    dom_strip_style_and_layer(document,"power_towers","power_towers")
    dom_strip_style_and_layer(document,"power_poles","power_poles")

def transmogrify_file(sf,dfgrey,dfnoicons):
    dom= pxdom.getDOMImplementation('') 
    parser= dom.createLSParser(dom.MODE_SYNCHRONOUS, None)
    parser.domConfig.setParameter('entities', 0) # 1 -> exception if attribute values is set
    #parser.domConfig.setParameter('disallow-doctype', 1)
    parser.domConfig.setParameter('pxdom-resolve-resources', 1) # 1 -> replace &xyz; with text
    document = parser.parseURI(sf)

#    dom_convert_to_grey(document)
    
    output= document.implementation.createLSOutput() 
    output.systemId= dfgrey
    output.encoding= 'utf-8' 
    serialiser= document.implementation.createLSSerializer() 
    serialiser.write(document, output)

"""
    dom_strip_icons(document)
    
    output= document.implementation.createLSOutput() 
    output.systemId= dfnoicons
    output.encoding= 'utf-8' 
    serialiser= document.implementation.createLSSerializer() 
    serialiser.write(document, output)
"""

def strip_doctype(f):
    p = subprocess.Popen(['sed','-i','2,9 d',f]) # -i means 'in place'
    p.wait()

def create_parking_icons(source_symbols_dir,dest_symbols_dir):
    image_files = os.listdir(source_symbols_dir)
    image_files = [f for f in image_files if f.endswith('png')]
    for f in image_files:
        # convert ./original-mapnik/symbols/*.png -fx '0.25*r + 0.62*g + 0.13*b' ./bw-mapnik/symbols/*.png
        sf = os.path.join(source_symbols_dir,f)
        df = os.path.join(dest_symbols_dir,f)
        p = subprocess.Popen(['convert',sf,'-fx','0.25*r + 0.62*g + 0.13*b',df])
        p.wait()

def add_license_files(dirname):
    f = open(os.path.join(dirname,"CONTACT"), 'w')
    f.write("This style is created by kayd@toolserver.org")
    f.close
    f = open(os.path.join(dirname,"LICENSE"), 'w')
    f.write("This derived work is published by the author, Kay Drangmeister, under the same license as the original OSM mapnik style sheet (found here: http://svn.openstreetmap.org/applications/rendering/mapnik)")
    f.close

def main(options):
    source_dir = options['sourcedir']
    source_file = options['sourcefile']
    source_symbols_dir = os.path.join(source_dir,"symbols")
    dest_dir = options['destdir']

    dest_dir_parktrans = os.path.join(dest_dir,"parktrans")
    dest_dir_parktrans_symbols = os.path.join(dest_dir_parktrans,"symbols")
    dest_file_parktrans = 'osm-parktrans.xml'
    parktrans_file = os.path.join(dest_dir_parktrans,dest_file_parktrans)
    if not os.path.exists(dest_dir_parktrans_symbols):
        os.makedirs(dest_dir_parktrans_symbols)

    create_parking_icons(source_symbols_dir,dest_dir_parktrans_symbols)
    transmogrify_file(os.path.join(source_dir,source_file),parktrans_file,"")
    strip_doctype(parktrans_file)
#    strip_doctype(parktrans_noicons_file)
    add_license_files(dest_dir_parktrans)
#    add_license_files(dest_dir_parktrans_noicons)

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-s", "--sourcedir", dest="sourcedir", help="path to the source directory", default=".")
    parser.add_option("-f", "--sourcefile", dest="sourcefile", help="source filename, default is 'osm.xml')", default="osm.xml")
    parser.add_option("-d", "--destdir", dest="destdir", help="path to the destination directory, further dirs are created within. default is '/tmp'", default="/tmp")
    (options, args) = parser.parse_args()
    print options
    main(options.__dict__)
    sys.exit(0)
