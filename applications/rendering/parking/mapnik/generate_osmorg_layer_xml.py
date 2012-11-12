# -*- coding: utf-8 -*-
# by kay

import sys,os,subprocess,shutil
from optparse import OptionParser
#from xml.dom.minidom import parse, parseString
import pxdom
from generate_utils import *


def osmorgdoc_adopt_roads_text_layer(doc,patch_doc):
    # replace the TextSymbolizer elements within the roads-text-style with placement-type list and placements of abbreviations
    rtn_style=None
    els = doc.getElementsByTagName("Style")
    for el in els:
        if el.getAttribute("name")=="roads-text-name":
            rtn_style=el
    assert rtn_style!=None
    rules = rtn_style.getElementsByTagName("Rule")
    for rule in rules:
        #print "isinstance" #print isinstance(rule,pxdom.Element)
        TextSymbolizers = rule.getElementsByTagName("TextSymbolizer")
        for TextSymbolizer in TextSymbolizers:
                TextSymbolizer.setAttribute("placement-type","list")
                plc1 = doc.createElement('Placement')
                plc1.appendChild(doc.createTextNode('[abbr1]'))
                TextSymbolizer.appendChild(plc1)
                plc2 = doc.createElement('Placement')
                plc2.appendChild(doc.createTextNode('[abbr2]'))
                TextSymbolizer.appendChild(plc2)
                plc3 = doc.createElement('Placement')
                plc3.appendChild(doc.createTextNode('[abbr3]'))
                TextSymbolizer.appendChild(plc3)

    # replace the "roads-text-name" layer with the one using the planet_line_join table
    mapnikdoc_cut_layer(doc,'roads-text-name')
    parking_roadnames_layer = doc.adoptNode(mapnikdoc_cut_layer(patch_doc,'roads-text-name'))
    mapnikdoc_insert_things_before_layer(doc,[parking_roadnames_layer],'text')


def patch_osmorg_style(sf,pf,df):
    dom= pxdom.getDOMImplementation('') 
    parser= dom.createLSParser(dom.MODE_SYNCHRONOUS, None)
    parser.domConfig.setParameter('entities', 0) # 1 -> exception if attribute values is set
    #parser.domConfig.setParameter('disallow-doctype', 1)
    parser.domConfig.setParameter('pxdom-resolve-resources', 1) # 1 -> replace &xyz; with text
    document = parser.parseURI(sf)
    patch_document = parser.parseURI(pf)

    osmorgdoc_adopt_roads_text_layer(document,patch_document)

    output= document.implementation.createLSOutput() 
    output.systemId= df
    output.encoding= 'utf-8' 
    serialiser= document.implementation.createLSSerializer() 
    serialiser.write(document, output)

def main_osmorg(options):
    print options
    style_name = options['stylename']
    source_mapnik_dir = options['sourcedir']
    source_mapnik_file = options['sourcemapnikfile']
    source_osmorg_dir = options['sourcedir']
    source_osmorg_file = options['sourceosmorgfile']
    dest_dir = options['destdir']

    dest_style_dir = os.path.join(dest_dir,style_name)
    dest_style_file = 'osm-{style}.xml'.format(style=style_name)
    dest_style_dirfile = os.path.join(dest_style_dir,dest_style_file)

    source_osmorg_dirfile = os.path.abspath(os.path.join(source_osmorg_dir,source_osmorg_file))
    print("patch_osmorg_style({a},{b})".format(a=source_osmorg_dirfile,b=dest_style_dirfile))
    patch_osmorg_style(os.path.join(source_mapnik_dir,source_mapnik_file),source_osmorg_dirfile,dest_style_dirfile)
    strip_doctype_n(dest_style_dirfile,5)
    add_license_files(dest_style_dir)



if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-s", "--sourcedir", dest="sourcedir", help="path to the source directory", default=".")
    parser.add_option("-f", "--sourcefile", dest="sourcefile", help="source filename, default is 'osm.xml')", default="osm.xml")
    parser.add_option("-d", "--destdir", dest="destdir", help="path to the destination directory, further dirs are created within. default is '/tmp'", default="/tmp")
    (options, args) = parser.parse_args()
    #options['stylename'] = "osmorgtrans"
    print options
    main_osmorg(options.__dict__)
    sys.exit(0)
