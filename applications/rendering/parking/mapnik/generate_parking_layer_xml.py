# -*- coding: utf-8 -*-
# by kay

import sys,os,subprocess
from optparse import OptionParser
#from xml.dom.minidom import parse, parseString
import pxdom
#import colorsys

condition_colors = {
    'free': '7fff00',
    'disc': '50a100',
    'cust': 'b68529',
    'resi': '785534',
    'priv': '3f2920',
    'fee':  '67a1eb',
    'unkn': 'bc73e2'
    }

forbidden_colors = {
    'nopa': 'f8b81f',
    'nost': 'f85b1f',
    'fire': 'f81f1f'
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
    create_parking_lane_icons(source_symbols_dir,dest_symbols_dir)
    create_parking_area_icons(source_symbols_dir,dest_symbols_dir)
    
def create_parking_lane_icons(source_symbols_dir,dest_symbols_dir):
    # first create mirror images (from left to right)
    image_files = os.listdir(source_symbols_dir)
    image_files = [f for f in image_files if f.startswith('park-l') and f.endswith('png')]
    for f in image_files:
        sf = os.path.join(source_symbols_dir,f)
        df = os.path.join(source_symbols_dir,f) # this is changed so that we write in the source dir
        hflip_icon(sf,df.replace('-l','-r'))

    # then, create the colors
    image_files = os.listdir(source_symbols_dir)
    image_files = [f for f in image_files if f.endswith('source.png')]
    for f in image_files:
        # convert ./original-mapnik/symbols/*.png -fx '0.25*r + 0.62*g + 0.13*b' ./bw-mapnik/symbols/*.png
        sf = os.path.join(source_symbols_dir,f)
        df = os.path.join(dest_symbols_dir,f)
        if 'n-' in f:      # then it's a non-parking thing
            for c in forbidden_colors.iterkeys():
                colorize_icon(sf,df.replace('source',c),forbidden_colors.get(c))
        else:
            for c in condition_colors.iterkeys():
                colorize_icon(sf,df.replace('source',c),condition_colors.get(c))

def create_parking_area_icons(source_symbols_dir,dest_symbols_dir):
    df = os.path.join(dest_symbols_dir,"parking_area_source.png")
    stampf = os.path.join(source_symbols_dir,"parking_area_stamp.png")
    for c in condition_colors.iterkeys():
        p = subprocess.Popen(['convert','-size','16x16','xc:#'+condition_colors.get(c),stampf,'-compose','Darken','-composite',df.replace('source',c)])
        p.wait()

def create_parking_area_icons_delete(source_symbols_dir,dest_symbols_dir):
    image_files = os.listdir(source_symbols_dir)
    image_files = [f for f in image_files if f.startswith('parking_area') and f.endswith('source.png')]
    print "all image files:", image_files
    for f in image_files:
        sf = os.path.join(source_symbols_dir,f)
        df = os.path.join(dest_symbols_dir,f)
        stampf = sf.replace('source','stamp')
        print "stampf", stampf
        for c in condition_colors.iterkeys():
            colorize_icon(sf,df.replace('source',c),condition_colors.get(c))
            stamp_icon(df.replace('source',c),df.replace('source',c),stampf)

def colorize_icon(sf,df,color):
    p = subprocess.Popen(['convert',sf,'-fill','#'+color,'-colorize','100',df])
    p.wait()

def hflip_icon(sf,df):
    p = subprocess.Popen(['convert',sf,'-flip',df])
    p.wait()

def stamp_icon(sf,df,stampf):
    p = subprocess.Popen(['convert',sf,stampf,'-compose','Darken','-composite',df])
    p.wait()

def add_license_files(dirname):
    f = open(os.path.join(dirname,"CONTACT"), 'w')
    f.write("This style is created by kayd@toolserver.org")
    f.close
    f = open(os.path.join(dirname,"LICENSE"), 'w')
    f.write("This derived work is published by the author, Kay Drangmeister, under the same license as the original OSM mapnik style sheet (found here: http://svn.openstreetmap.org/applications/rendering/mapnik)")
    f.close

def main_parktrans(options):
    style_name = options['stylename']
    source_dir = options['sourcedir']
    source_file = options['sourcefile']
    #source_symbols_dir_mapnik = os.path.join(source_dir,"symbols")
    source_symbols_dir_style = os.path.join(source_dir,"parking-symbols-src")
    dest_dir = options['destdir']

    # parktrans - transparent layer to be put on top of mapnik or bw-noicons base layer
    
    dest_dir_style = os.path.join(dest_dir,style_name)
    dest_dir_style_symbols = os.path.join(dest_dir_style,"symbols")
    dest_file_style = 'osm-{style}.xml'.format(style=style_name)
    style_file = os.path.join(dest_dir_style,dest_file_style)
    if not os.path.exists(dest_dir_style_symbols):
        os.makedirs(dest_dir_style_symbols)

    create_parking_icons(source_symbols_dir_style,dest_dir_style_symbols)
    transmogrify_file(os.path.join(source_dir,source_file),style_file,"")
    strip_doctype(style_file)
    add_license_files(dest_dir_style)

def main_parking(options):
    style_name = options['stylename']
    source_dir = options['sourcedir']
    source_file = options['sourcefile']
    source_symbols_dir_style = os.path.join(source_dir,"parking-symbols-src")
    dest_dir = options['destdir']

    # parking - bw-noicons background ans parking information on top - single layer containing all

    dest_dir_style = os.path.join(dest_dir,style_name)
    dest_dir_style_symbols = os.path.join(dest_dir_style,"symbols")
    dest_file_style = 'osm-{style}.xml'.format(style=style_name)
    style_file = os.path.join(dest_dir_style,dest_file_style)
    if not os.path.exists(dest_dir_style_symbols):
        os.makedirs(dest_dir_style_symbols)

    create_parking_icons(source_symbols_dir_style,dest_dir_style_symbols)
    transmogrify_file(os.path.join(source_dir,source_file),style_file,"")
    strip_doctype(style_file)
    add_license_files(dest_dir_style)

def main_parking_neu(options):
    style_name = options['stylename']
    source_dir = options['sourcedir']
    source_file = options['sourcefile']
    source_symbols_dir_style = os.path.join(source_dir,"parking-symbols-src")
    dest_dir = options['destdir']

    dest_dir_style = os.path.join(dest_dir,style_name)
    dest_dir_style_symbols = os.path.join(dest_dir_style,"symbols")
    dest_file_style = 'osm-parking-neu.xml'
    style_file = os.path.join(dest_dir_style,dest_file_style)
    if not os.path.exists(dest_dir_style_symbols):
        os.makedirs(dest_dir_style_symbols)

    create_parking_icons(source_symbols_dir_style,dest_dir_style_symbols)
    transmogrify_file(os.path.join(source_dir,source_file),style_file,"")
    strip_doctype(style_file)
    add_license_files(dest_dir_style)

    

"""
./generate_xml.py osm-parking-src.xml    osm-parking.xml    --accept-none --host sql-mapnik --dbname osm_mapnik --prefix planet --inc ./parking-inc --symbols ./parking-symbols/ --world_boundaries /home/project/o/s/m/osm/data/world_boundaries/ --password ''
./generate_xml.py osm-parking-bw-src.xml osm-parking-bw.xml --accept-none --host sql-mapnik --dbname osm_mapnik --prefix planet --inc ./parking-inc --symbols ./parking-symbols/ --world_boundaries /home/project/o/s/m/osm/data/world_boundaries/ --password ''
./generate_xml.py osm-parkerr-src.xml    osm-parkerr.xml    --accept-none --host sql-mapnik --dbname osm_mapnik --prefix planet --inc ./parking-inc --symbols ./parking-symbols/ --world_boundaries /home/project/o/s/m/osm/data/world_boundaries/ --password ''
"""

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-s", "--sourcedir", dest="sourcedir", help="path to the source directory", default=".")
    parser.add_option("-f", "--sourcefile", dest="sourcefile", help="source filename, default is 'osm.xml')", default="osm.xml")
    parser.add_option("-d", "--destdir", dest="destdir", help="path to the destination directory, further dirs are created within. default is '/tmp'", default="/tmp")
    (options, args) = parser.parse_args()
    options['stylename'] = "parktrans"
    print options
    main_parktrans(options.__dict__)
    options['sourcefile'] = "osm-parking-src.xml"
    options['stylename'] = "parking"
    main_parking(options.__dict__)
    sys.exit(0)
