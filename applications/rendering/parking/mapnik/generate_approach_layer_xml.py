# -*- coding: utf-8 -*-
# by kay

import sys,os,subprocess,shutil
from optparse import OptionParser
#from xml.dom.minidom import parse, parseString
import pxdom

def transmogrify_file(sf,df):
    dom= pxdom.getDOMImplementation('') 
    parser= dom.createLSParser(dom.MODE_SYNCHRONOUS, None)
    parser.domConfig.setParameter('entities', 0) # 1 -> exception if attribute values is set
    #parser.domConfig.setParameter('disallow-doctype', 1)
    parser.domConfig.setParameter('pxdom-resolve-resources', 1) # 1 -> replace &xyz; with text
    document = parser.parseURI(sf)

#    dom_convert_to_grey(document)
    
    output= document.implementation.createLSOutput() 
    output.systemId= df
    output.encoding= 'utf-8' 
    serialiser= document.implementation.createLSSerializer() 
    serialiser.write(document, output)


def strip_doctype(f):
    p = subprocess.Popen(['sed','-i','2,9 d',f]) # -i means 'in place'
    p.wait()

def create_approach_icons(source_symbols_dir,dest_symbols_dir):
    create_approach_area_icons(source_symbols_dir,dest_symbols_dir)
    create_approach_point_icons(source_symbols_dir,dest_symbols_dir)
    
def create_approach_area_icons(source_symbols_dir,dest_symbols_dir):
    return

def create_approach_point_icons(source_symbols_dir,dest_symbols_dir):
    copy_files(source_symbols_dir,dest_symbols_dir,['approach-target.png'])

def copy_files(src,dest,files):
    for f in files:
        if type(f) is tuple:
            shutil.copy2(os.path.join(src,f[0]),os.path.join(dest,f[1]))
        else:
            shutil.copy2(os.path.join(src,f),os.path.join(dest,f))

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

def main_approach(options):
    style_name = options['stylename']
    source_dir = options['sourcedir']
    source_file = options['sourcefile']
    #source_symbols_dir_mapnik = os.path.join(source_dir,"symbols")
    source_symbols_dir_style = os.path.join(source_dir,"approach-symbols-src")
    dest_dir = options['destdir']

    dest_dir_style = os.path.join(dest_dir,style_name)
    dest_dir_style_symbols = os.path.join(dest_dir_style,"symbols")
    dest_file_style = 'osm-{style}.xml'.format(style=style_name)
    style_file = os.path.join(dest_dir_style,dest_file_style)
    if not os.path.exists(dest_dir_style_symbols):
        os.makedirs(dest_dir_style_symbols)

    create_approach_icons(source_symbols_dir_style,dest_dir_style_symbols)
    transmogrify_file(os.path.join(source_dir,source_file),style_file)
    strip_doctype(style_file)
    add_license_files(dest_dir_style)

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-s", "--sourcedir", dest="sourcedir", help="path to the source directory", default=".")
    parser.add_option("-f", "--sourcefile", dest="sourcefile", help="source filename, default is 'osm.xml')", default="osm.xml")
    parser.add_option("-d", "--destdir", dest="destdir", help="path to the destination directory, further dirs are created within. default is '/tmp'", default="/tmp")
    (options, args) = parser.parse_args()
    options['stylename'] = "approach"
    print options
    main_approach(options.__dict__)
    sys.exit(0)
