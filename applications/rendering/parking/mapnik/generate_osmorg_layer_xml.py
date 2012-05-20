# -*- coding: utf-8 -*-
# by kay

import sys,os,subprocess,shutil
from optparse import OptionParser
#from xml.dom.minidom import parse, parseString
import pxdom

#import colorsys

condition_colors = {
    'free': '04c900',
    'cust': 'c17223',
    'none': '634e45',
    'fee':  '037afe',
    'unkn': 'a649b7'
    }

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
    print "removing the following elements:"
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

def create_osmorg_icons(source_symbols_dir,dest_symbols_dir):
    create_osmorg_area_icons(source_symbols_dir,dest_symbols_dir)
    create_osmorg_point_icons(source_symbols_dir,dest_symbols_dir)
    
def create_osmorg_area_icons(source_symbols_dir,dest_symbols_dir):
    return

def create_osmorg_point_icons(source_symbols_dir,dest_symbols_dir):
    copy_files(source_symbols_dir,dest_symbols_dir,['osmorg-vending.png'])
    # osmorg nodes
    for condition in condition_colors.keys():
        sf = os.path.join(source_symbols_dir,'osmorg-source.png')
        df = os.path.join(dest_symbols_dir,'osmorg_node_{cond}.png'.format(cond=condition))
        colorize_icon(sf,df,condition_colors.get(condition))
    # osmorg_6 nodes (lower zoom)
    for condition in condition_colors.keys():
        sf = os.path.join(source_symbols_dir,'osmorg-6-source.png')
        df = os.path.join(dest_symbols_dir,'osmorg_node_6_{cond}.png'.format(cond=condition))
        colorize_icon(sf,df,condition_colors.get(condition))

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

def main_osmorgtrans(options):
    style_name = options['stylename']
    source_dir = options['sourcedir']
    source_file = options['sourcefile']
    #source_symbols_dir_mapnik = os.path.join(source_dir,"symbols")
    source_symbols_dir_style = os.path.join(source_dir,"osmorg-symbols-src")
    dest_dir = options['destdir']

    # osmorgtrans - transparent layer to be put on top of mapnik or bw-noicons base layer
    
    dest_dir_style = os.path.join(dest_dir,style_name)
    dest_dir_style_symbols = os.path.join(dest_dir_style,"symbols")
    dest_file_style = 'osm-{style}.xml'.format(style=style_name)
    style_file = os.path.join(dest_dir_style,dest_file_style)
    if not os.path.exists(dest_dir_style_symbols):
        os.makedirs(dest_dir_style_symbols)

    create_osmorg_icons(source_symbols_dir_style,dest_dir_style_symbols)
    transmogrify_file(os.path.join(source_dir,source_file),style_file,"")
    strip_doctype(style_file)
    add_license_files(dest_dir_style)

def main_osmorg(options):
    style_name = options['stylename']
    source_bwn_dir = options['sourcestyledir']
    source_bwn_file = options['sourcestylefile']
#    source_p_dir = options['sourcepdir']
#  source_p_file = options['sourcepfile']
#    source_symbols_dir_style = os.path.join(source_p_dir,"osmorg-symbols-src")
    dest_dir = options['destdir']

    dest_dir_style = os.path.join(dest_dir,style_name)
    dest_dir_style_symbols = os.path.join(dest_dir_style,"symbols")
    dest_file_style = 'osm-{style}.xml'.format(style=style_name)
    style_file = os.path.join(dest_dir_style,dest_file_style)
    if not os.path.exists(dest_dir_style_symbols):
        os.makedirs(dest_dir_style_symbols)

    patch_osmorg_style(os.path.join(source_bwn_dir,source_bwn_file),style_file)
    #strip_doctype(style_file)
    add_license_files(dest_dir_style)

def patch_osmorg_style(bwnoicons_style_file,dest_osmorg_style_file):
    dom= pxdom.getDOMImplementation('') 
    parser= dom.createLSParser(dom.MODE_SYNCHRONOUS, None)
    parser.domConfig.setParameter('entities', 0) # 1 -> exception if attribute values is set
    #parser.domConfig.setParameter('disallow-doctype', 1)
    parser.domConfig.setParameter('pxdom-resolve-resources', 1) # 1 -> replace &xyz; with text
    dest_osmorg_style_document = parser.parseURI(bwnoicons_style_file)

    output= dest_osmorg_style_document.implementation.createLSOutput() 
    output.systemId= dest_osmorg_style_file
    output.encoding= 'utf-8' 
    serialiser= dest_osmorg_style_document.implementation.createLSSerializer() 
    serialiser.write(dest_osmorg_style_document, output)

    ''' write a "rest" file to check if everything has been cut out
    output= osmorgtrans_style_document.implementation.createLSOutput() 
    output.systemId= 'rest.xml'
    output.encoding= 'utf-8' 
    serialiser= osmorgtrans_style_document.implementation.createLSSerializer() 
    serialiser.write(osmorgtrans_style_document, output)
    '''

def osmorg_dom_insert_things_before_layer(document,things,here):
    # insert things after the "leisure" layer
    els = document.getElementsByTagName("Layer")
    #print "els="
    #print els
    for el in els:
        #print "layername={ln}".format(ln=el.getAttribute("name"))
        if el.getAttribute("name")==here:
            #print "found it"
            if type(things) is list:
                for s in things:
                    el.parentNode.insertBefore(s,el)
            else:
                el.parentNode.insertBefore(things,el)
            return
    raise 'Layer name not found'

def osmorg_dom_clone_layer(document,what):
    els = document.getElementsByTagName("Layer")
    for el in els:
        #print "layername={ln}".format(ln=el.getAttribute("name"))
        if el.getAttribute("name")==what:
            #print "found it"
            clone = el.cloneNode(True)
            return clone
    raise BaseException('Layer name {ln} not found'.format(ln=what))

def osmorg_dom_cut_layer(document,what):
    els = document.getElementsByTagName("Layer")
    for el in els:
        #print "layername={ln}".format(ln=el.getAttribute("name"))
        if el.getAttribute("name")==what:
            #print "found it"
            el.parentNode.removeChild(el)
            return el
    raise BaseException('Layer name {ln} not found'.format(ln=what))

def osmorg_dom_cut_style(document,what):
    els = document.getElementsByTagName("Style")
    for el in els:
        #print "layername={ln}".format(ln=el.getAttribute("name"))
        if el.getAttribute("name")==what:
            #print "found it"
            el.parentNode.removeChild(el)
            return el
    raise BaseException('Style name {sn} not found'.format(sn=what))

"""
./generate_xml.py osm-osmorg-src.xml    osm-osmorg.xml    --accept-none --host sql-mapnik --dbname osm_mapnik --prefix planet --inc ./osmorg-inc --symbols ./osmorg-symbols/ --world_boundaries /home/project/o/s/m/osm/data/world_boundaries/ --password ''
./generate_xml.py osm-osmorg-bw-src.xml osm-osmorg-bw.xml --accept-none --host sql-mapnik --dbname osm_mapnik --prefix planet --inc ./osmorg-inc --symbols ./osmorg-symbols/ --world_boundaries /home/project/o/s/m/osm/data/world_boundaries/ --password ''
./generate_xml.py osm-parkerr-src.xml    osm-parkerr.xml    --accept-none --host sql-mapnik --dbname osm_mapnik --prefix planet --inc ./osmorg-inc --symbols ./osmorg-symbols/ --world_boundaries /home/project/o/s/m/osm/data/world_boundaries/ --password ''
"""

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-s", "--sourcedir", dest="sourcedir", help="path to the source directory", default=".")
    parser.add_option("-f", "--sourcefile", dest="sourcefile", help="source filename, default is 'osm.xml')", default="osm.xml")
    parser.add_option("-d", "--destdir", dest="destdir", help="path to the destination directory, further dirs are created within. default is '/tmp'", default="/tmp")
    (options, args) = parser.parse_args()
    options['stylename'] = "osmorgtrans"
    print options
    main_osmorgtrans(options.__dict__)
    options['sourcefile'] = "osm-osmorg-src.xml"
    options['stylename'] = "osmorg"
    main_osmorg(options.__dict__)
    sys.exit(0)
