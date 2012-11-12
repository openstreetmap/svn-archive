# -*- coding: utf-8 -*-
# by kay

import sys,os,subprocess,shutil
from optparse import OptionParser
#from xml.dom.minidom import parse, parseString
import pxdom

def strip_doctype_n(f,numlines):
    # numlines = 11 for some files, 9 for others, depends on what?
    p = subprocess.Popen(['sed','-i','2,{nl} d'.format(nl=numlines),f]) # -i means 'in place'
    p.wait()

def copy_files(src,dest,files):
    for f in files:
        if type(f) is tuple:
            shutil.copy2(os.path.join(src,f[0]),os.path.join(dest,f[1]))
        else:
            shutil.copy2(os.path.join(src,f),os.path.join(dest,f))

def add_license_files(dirname):
    f = open(os.path.join(dirname,"CONTACT"), 'w')
    f.write("This style is created by kayd@toolserver.org")
    f.close
    f = open(os.path.join(dirname,"LICENSE"), 'w')
    f.write("This derived work is published by the author, Kay Drangmeister, under the same license as the original OSM mapnik style sheet (found here: http://svn.openstreetmap.org/applications/rendering/mapnik)")
    f.close

# -------- icon stuff

"""
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
"""

def colorize_icon(sf,df,color):
    p = subprocess.Popen(['convert',sf,'-fill','#'+color,'-colorize','100',df])
    p.wait()

def hflip_icon(sf,df):
    p = subprocess.Popen(['convert',sf,'-flip',df])
    p.wait()

def stamp_icon(sf,df,stampf):
    p = subprocess.Popen(['convert',sf,stampf,'-compose','Darken','-composite',df])
    p.wait()

# -------- DOM stuff

def mapnikdoc_strip_style_and_layer(document,stylename,layername):
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

def mapnikdoc_strip_icons(document):
    mapnikdoc_strip_style_and_layer(document,"points","amenity-points")
    mapnikdoc_strip_style_and_layer(document,"power_line","power_line")
    mapnikdoc_strip_style_and_layer(document,"power_minorline","power_minorline")
    mapnikdoc_strip_style_and_layer(document,"power_towers","power_towers")
    mapnikdoc_strip_style_and_layer(document,"power_poles","power_poles")

def mapnikdoc_insert_things_before_layer(document,things,here):
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

def mapnikdoc_clone_layer(document,what):
    els = document.getElementsByTagName("Layer")
    for el in els:
        #print "layername={ln}".format(ln=el.getAttribute("name"))
        if el.getAttribute("name")==what:
            #print "found it"
            clone = el.cloneNode(True)
            return clone
    raise BaseException('Layer name {ln} not found'.format(ln=what))

def mapnikdoc_cut_layer(document,what):
    els = document.getElementsByTagName("Layer")
    for el in els:
        #print "layername={ln}".format(ln=el.getAttribute("name"))
        if el.getAttribute("name")==what:
            #print "found it"
            el.parentNode.removeChild(el)
            return el
    raise BaseException('Layer name {ln} not found'.format(ln=what))

def mapnikdoc_cut_style(document,what):
    els = document.getElementsByTagName("Style")
    for el in els:
        #print "layername={ln}".format(ln=el.getAttribute("name"))
        if el.getAttribute("name")==what:
            #print "found it"
            el.parentNode.removeChild(el)
            return el
    raise BaseException('Style name {sn} not found'.format(sn=what))
