# -*- coding: utf-8 -*-
# by kay

import sys,os,subprocess,shutil
from optparse import OptionParser
#from xml.dom.minidom import parse, parseString
import pxdom
#import colorsys
import mapnik_to_bw,generate_parking_layer_xml

def add_license_files(dirname):
    f = open(os.path.join(dirname,"CONTACT"), 'w')
    f.write("This style is created by kayd@toolserver.org")
    f.close
    f = open(os.path.join(dirname,"LICENSE"), 'w')
    f.write("This derived work is published by the author, Kay Drangmeister, under the same license as the original OSM mapnik style sheet (found here: http://svn.openstreetmap.org/applications/rendering/mapnik)")
    f.close

"""
deploy_base_dir, e.g. /tmp/kays-styles/
style_name, e.g. bw-noicons
"""
def setup_deploy_directory(deploy_base_dir,style_name):
    dest_dir_style = os.path.join(deploy_base_dir,style_name)
    dest_dir_style_symbols = os.path.join(dest_dir_style,"symbols")
    dest_style_file_name = 'osm-'+style_name+'.xml'
    dest_style_file = os.path.join(dest_dir_style,dest_style_file_name)
    if not os.path.exists(dest_dir_style_symbols):
        os.makedirs(dest_dir_style_symbols)
    add_license_files(dest_dir_style)

def copy_files(src,dest,files):
    for f in files:
        if type(f) is tuple:
            shutil.copy2(os.path.join(src,f[0]),os.path.join(dest,f[1]))
        else:
            shutil.copy2(os.path.join(src,f),os.path.join(dest,f))

def copy_settings_files(settings_dir,target_inc_dir):
    copy_files(settings_dir,target_inc_dir,[
        ("datasource-settings.xml.inc.toolserver","datasource-settings.xml.inc"),
        ("fontset-settings.xml.inc.toolserver","fontset-settings.xml.inc"),
        ("settings.xml.inc.toolserver","settings.xml.inc")]
    )

def main(options):
    original_mapnik_dir = options['mapnikdir']
    original_parking_dir = options['parkingdir']
    settings_dir = options['settingsdir']
    temp_dir = options['tempdir']
    deploy_dir = options['deploydir']

    # clean temp and deploy dirs
    print "** cleaning temp dir "+temp_dir
    shutil.rmtree(temp_dir,ignore_errors=True)
    print "** cleaning deploy dir "+deploy_dir
    shutil.rmtree(deploy_dir,ignore_errors=True)

    # (1) copy the mapnik to temp and patch with local settings.
    patched_mapnik_dir = os.path.join(temp_dir,"mapnik")
    #os.makedirs(patched_mapnik_dir)
    #print "shutil.copytree(original_mapnik_dir={o},patched_mapnik_dir={p})".format(o=original_mapnik_dir,p=patched_mapnik_dir)
    shutil.copytree(original_mapnik_dir,patched_mapnik_dir)
    copy_settings_files(settings_dir,os.path.join(patched_mapnik_dir,"inc"))

    # (2) create the bw styles
    mapnik_to_bw.main({'sourcedir':patched_mapnik_dir, 'sourcefile':'osm.xml', 'destdir':deploy_dir})

    # (3) create the parking styles
    parking_dir = os.path.join(temp_dir,"parking")
    parking_inc_dir = os.path.join(parking_dir,"inc")
    os.makedirs(parking_dir)
    #print "shutil.copytree(original_mapnik_dir={o},patched_mapnik_dir={p})".format(o=original_mapnik_dir,p=patched_mapnik_dir)
    shutil.copy2(os.path.join(original_parking_dir,"colorents.xml.inc"),os.path.join(parking_dir,"colorents.xml.inc"))
    shutil.copy2(os.path.join(original_parking_dir,"colorents-bw.xml.inc"),os.path.join(parking_dir,"colorents-bw.xml.inc"))
    # TODO: ^^^^ remove this
    shutil.copy2(os.path.join(original_parking_dir,"osm-parktrans-src.xml"),os.path.join(parking_dir,"osm-parktrans-src.xml"))
    shutil.copy2(os.path.join(original_parking_dir,"osm-parking-src.xml"),os.path.join(parking_dir,"osm-parking-src.xml"))
    shutil.copy2(os.path.join(original_parking_dir,"osm-parking2-src.xml"),os.path.join(parking_dir,"osm-parking2-src.xml"))
    # TODO: ^^^ weg mit dem
    shutil.copytree(os.path.join(patched_mapnik_dir,"inc"),parking_inc_dir)
    original_parking_inc_dir = os.path.join(original_parking_dir,"parking-inc-src")
    #shutil.copytree(original_parking_inc_dir,parking_inc_dir)
    ### files einzeln kopieren...
    copy_files(original_parking_inc_dir,parking_inc_dir,["layer-parking-entities.xml.inc","layer-parking-area.xml.inc","layer-parking-lane.xml.inc"])
    shutil.copytree(os.path.join(original_mapnik_dir,"symbols"),os.path.join(parking_dir,"symbols"))
    shutil.copytree(os.path.join(original_parking_dir,"parking-symbols-src"),os.path.join(parking_dir,"parking-symbols-src"))

    # TODO: kludge to copy bw icons to parking/symbols dir
    # this is ugly because it relies on knowledge of mapnik_to_bw.main() i.e. how the dirs are named.
    shutil.copytree(os.path.join(os.path.join(deploy_dir,"bw-noicons"),"symbols"),os.path.join(os.path.join(deploy_dir,"parking"),"symbols"))

    generate_parking_layer_xml.main_parktrans({'sourcedir':parking_dir, 'sourcefile':'osm-parktrans-src.xml', 'destdir':deploy_dir, 'stylename':'parktrans'})
    generate_parking_layer_xml.main_parking({'sourcedir':parking_dir, 'sourcefile':'osm-parking-src.xml', 'destdir':deploy_dir, 'stylename':'parking'})
    # this line below leads to parking entities, e.g. &pa_stroke_opacity not being resolved.
    #but maybe i can resolve it with entity declarations.
    generate_parking_layer_xml.main_parking_neu({'sourcebwndir':os.path.join(deploy_dir,'bw-noicons'), 'sourcebwnfile':'osm-bw-noicons.xml', 'sourcepdir':parking_dir, 'sourcepfile':'osm-parking2-src.xml', 'destdir':deploy_dir, 'stylename':'parking-new'})
    #generate_parking_layer_xml.main_parking_neu({'sourcebwndir':os.path.join(deploy_dir,'bw-noicons'), 'sourcebwnfile':'osm-bw-noicons.xml', 'sourcepdir':os.path.join(deploy_dir,'parktrans'), 'sourcepfile':'osm-parktrans.xml', 'destdir':deploy_dir, 'stylename':'parking-new'})
    # TODO: kludge to copy bw icons to parking-new/symbols dir
    # this is ugly because it relies on knowledge of parking(-old) i.e. how the dirs are named.
    shutil.rmtree(os.path.join(os.path.join(deploy_dir,"parking-new"),"symbols"))
    shutil.copytree(os.path.join(os.path.join(deploy_dir,"parking"),"symbols"),os.path.join(os.path.join(deploy_dir,"parking-new"),"symbols"))

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-m", "--mapnikdir", dest="mapnikdir", help="path to the mapnik directory", default="./mapnik")
    parser.add_option("-p", "--parkingdir", dest="parkingdir", help="path to the parking source directory", default=".")
    parser.add_option("-s", "--settingsdir", dest="settingsdir", help="path to the mapnik settings directory", default="./mapnik-patch")
    parser.add_option("-t", "--tempdir", dest="tempdir", help="path to the temporary directory", default="/tmp/kays-styles-mapnik")
    parser.add_option("-d", "--deploydir", dest="deploydir", help="path to the deploy directory, further dirs are created within. default is '/tmp'", default="/tmp")
    (options, args) = parser.parse_args()
    print options
    main(options.__dict__)
    sys.exit(0)
