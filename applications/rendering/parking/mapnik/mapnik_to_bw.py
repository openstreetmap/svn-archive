# -*- coding: utf-8 -*-
# by kay

import sys,os,subprocess
from optparse import OptionParser
#from xml.dom.minidom import parse, parseString
import pxdom
#import colorsys

simple_colors = {
    'aliceblue': 'f0f8ff',
    'antiquewhite': 'faebd7',
    'aqua': '00ffff',
    'aquamarine': '7fffd4',
    'azure': 'f0ffff',
    'beige': 'f5f5dc',
    'bisque': 'ffe4c4',
    'black': '000000',
    'blanchedalmond': 'ffebcd',
    'blue': '0000ff',
    'blueviolet': '8a2be2',
    'brown': 'a52a2a',
    'burlywood': 'deb887',
    'cadetblue': '5f9ea0',
    'chartreuse': '7fff00',
    'chocolate': 'd2691e',
    'coral': 'ff7f50',
    'cornflowerblue': '6495ed',
    'cornsilk': 'fff8dc',
    'crimson': 'dc143c',
    'cyan': '00ffff',
    'darkblue': '00008b',
    'darkcyan': '008b8b',
    'darkgoldenrod': 'b8860b',
    'darkgray': 'a9a9a9',
    'darkgreen': '006400',
    'darkkhaki': 'bdb76b',
    'darkmagenta': '8b008b',
    'darkolivegreen': '556b2f',
    'darkorange': 'ff8c00',
    'darkorchid': '9932cc',
    'darkred': '8b0000',
    'darksalmon': 'e9967a',
    'darkseagreen': '8fbc8f',
    'darkslateblue': '483d8b',
    'darkslategray': '2f4f4f',
    'darkturquoise': '00ced1',
    'darkviolet': '9400d3',
    'deeppink': 'ff1493',
    'deepskyblue': '00bfff',
    'dimgray': '696969',
    'dodgerblue': '1e90ff',
    'feldspar': 'd19275',
    'firebrick': 'b22222',
    'floralwhite': 'fffaf0',
    'forestgreen': '228b22',
    'fuchsia': 'ff00ff',
    'gainsboro': 'dcdcdc',
    'ghostwhite': 'f8f8ff',
    'gold': 'ffd700',
    'goldenrod': 'daa520',
    'gray': '808080',
    'green': '008000',
    'greenyellow': 'adff2f',
    'grey': '808080',
    'honeydew': 'f0fff0',
    'hotpink': 'ff69b4',
    'indianred ': 'cd5c5c',
    'indigo ': '4b0082',
    'ivory': 'fffff0',
    'khaki': 'f0e68c',
    'lavender': 'e6e6fa',
    'lavenderblush': 'fff0f5',
    'lawngreen': '7cfc00',
    'lemonchiffon': 'fffacd',
    'lightblue': 'add8e6',
    'lightcoral': 'f08080',
    'lightcyan': 'e0ffff',
    'lightgoldenrodyellow': 'fafad2',
    'lightgrey': 'd3d3d3',
    'lightgreen': '90ee90',
    'lightpink': 'ffb6c1',
    'lightsalmon': 'ffa07a',
    'lightseagreen': '20b2aa',
    'lightskyblue': '87cefa',
    'lightslateblue': '8470ff',
    'lightslategray': '778899',
    'lightsteelblue': 'b0c4de',
    'lightyellow': 'ffffe0',
    'lime': '00ff00',
    'limegreen': '32cd32',
    'linen': 'faf0e6',
    'magenta': 'ff00ff',
    'maroon': '800000',
    'mediumaquamarine': '66cdaa',
    'mediumblue': '0000cd',
    'mediumorchid': 'ba55d3',
    'mediumpurple': '9370d8',
    'mediumseagreen': '3cb371',
    'mediumslateblue': '7b68ee',
    'mediumspringgreen': '00fa9a',
    'mediumturquoise': '48d1cc',
    'mediumvioletred': 'c71585',
    'midnightblue': '191970',
    'mintcream': 'f5fffa',
    'mistyrose': 'ffe4e1',
    'moccasin': 'ffe4b5',
    'navajowhite': 'ffdead',
    'navy': '000080',
    'oldlace': 'fdf5e6',
    'olive': '808000',
    'olivedrab': '6b8e23',
    'orange': 'ffa500',
    'orangered': 'ff4500',
    'orchid': 'da70d6',
    'palegoldenrod': 'eee8aa',
    'palegreen': '98fb98',
    'paleturquoise': 'afeeee',
    'palevioletred': 'd87093',
    'papayawhip': 'ffefd5',
    'peachpuff': 'ffdab9',
    'peru': 'cd853f',
    'pink': 'ffc0cb',
    'plum': 'dda0dd',
    'powderblue': 'b0e0e6',
    'purple': '800080',
    'red': 'ff0000',
    'rosybrown': 'bc8f8f',
    'royalblue': '4169e1',
    'saddlebrown': '8b4513',
    'salmon': 'fa8072',
    'sandybrown': 'f4a460',
    'seagreen': '2e8b57',
    'seashell': 'fff5ee',
    'sienna': 'a0522d',
    'silver': 'c0c0c0',
    'skyblue': '87ceeb',
    'slateblue': '6a5acd',
    'slategray': '708090',
    'snow': 'fffafa',
    'springgreen': '00ff7f',
    'steelblue': '4682b4',
    'tan': 'd2b48c',
    'teal': '008080',
    'thistle': 'd8bfd8',
    'tomato': 'ff6347',
    'turquoise': '40e0d0',
    'violet': 'ee82ee',
    'violetred': 'd02090',
    'wheat': 'f5deb3',
    'white': 'ffffff',
    'whitesmoke': 'f5f5f5',
    'yellow': 'ffff00',
    'yellowgreen': '9acd32'
    }

def color_to_bw(rgb):
    r,g,b=rgb
    #method 1:
    #y = 0.229*r + 0.587*g + 0.114*b
    y = 0.25*r + 0.62*g + 0.13*b
    return (y,y,y)
    """
    #method 2:
    h,l,s = colorsys.rgb_to_hls(r, g, b)
    s *= 0   # desaturate
    return colorsys.hls_to_rgb(h,l,s)
    """

def parse_color(s):
    """ Parses color string in format #ABC or #AABBCC to RGB tuple. """
    s = s.lower()
    if simple_colors.has_key(s):  # translate color names to rgb
        s = '#'+simple_colors.get(s)
    l = len(s)
    assert(l in (4,7))
#    print "s=",s
    if l==4:
        return tuple(int(ch * 2, 16)/255.0 for ch in s[1:])
    else:
        return tuple(int(ch1 + ch2, 16)/255.0 for ch1, ch2 in \
                     zip(
                        (ch1 for ch1 in s[1::2]),
                        (ch2 for ch2 in s[2::2])
                        )
                    )

def rgb_to_css(rgb):
    r,g,b=rgb
    return "#{r:02x}{g:02x}{b:02x}".format(r=int(r*255.0),g=int(g*255.0),b=int(b*255.0))

def element_convert_attibute_to_grey(document,elementName,attributeName):
    els = document.getElementsByTagName(elementName)
    for el in els:
        col = el.getAttribute(attributeName)
        assert(col!='')
        assert(col!=None)
        bw=rgb_to_css(color_to_bw(parse_color(col)))
        #print "converted {ele}:{att} from {a} to {bw}." .format(ele=elementName,att=attributeName,a=col,bw=bw)
        el.setAttribute(attributeName,bw)

def dom_convert_to_grey(document):
    element_convert_attibute_to_grey(document,"Map","background-color")
    """ vorher: CssParameter fuer die Farben bei LineSymbolizer und PolygonSymbolizer
    els = document.getElementsByTagName("CssParameter")
    #print "els=",els
    for el in els:
        at = el.getAttribute("name")
        if at=="stroke" or at=="fill":
            col=el.firstChild.nodeValue
            bw=rgb_to_css(color_to_bw(parse_color(col)))
            #print "converted {typ} from {a} to {bw}." .format(typ=at,a=col,bw=bw)
            el.firstChild.nodeValue=bw
    """
    element_convert_attibute_to_grey(document,"LineSymbolizer","stroke")
    element_convert_attibute_to_grey(document,"PolygonSymbolizer","fill")

    #<TextSymbolizer ... fill="#6699cc"/>
    els = document.getElementsByTagName("TextSymbolizer")
    for el in els:
        col = el.getAttribute("fill")
        assert(col!='')
        assert(col!=None)
        bw=rgb_to_css(color_to_bw(parse_color(col)))
        #print "converted {typ} from {a} to {bw}." .format(typ='TS-fill',a=col,bw=bw)
        el.setAttribute("fill",bw)
        #<TextSymbolizer halo-fill="#fed7a5"/> (optional)
        col = el.getAttribute("halo-fill")
        assert(col!=None)
        if col!='':
            bw=rgb_to_css(color_to_bw(parse_color(col)))
            #print "converted {typ} from {a} to {bw}." .format(typ='TS-halo-fill',a=col,bw=bw)
            el.setAttribute("halo-fill",bw)


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

def dom_strip_rules_from_style(document,stylename,filters):
#    print '    filter val = "'+filter+'"'
    filterSet = set(filters)
    removeElements=[]
    # find the style
    thestyle = None
    els = document.getElementsByTagName("Style")
    for el in els:
        if el.getAttribute("name")==stylename:
            thestyle = el
            break
    assert(thestyle != None)
    # find the rule where the filter matches
    print thestyle
    rules = thestyle.childNodes
    for rule in rules:
        print "new rule"
        ruleElements = rule.childNodes
        for re in ruleElements:
            print "  ruleelement "+re.nodeName
            if re.nodeName=="Filter":
                filterValue=re.firstChild.nodeValue
                print '    filter val = "'+filterValue+'"'
                if filterValue in filterSet:
                    removeElements.append(rule)
                    filterSet.remove(filterValue)
    if (len(filterSet)!=0): # not all filters have been found
        for filter in filterSet:
            print 'Filter "{f}" not found'.format(f=filter)
    for el in removeElements:
        parent = el.parentNode
        parent.removeChild(el)
    
def dom_strip_POIs(document):
    removeTexts=[
                 "[amenity]='pub' or [amenity]='restaurant' or [amenity]='cafe' or [amenity]='fast_food' or [amenity]='biergarten'",
                 "[amenity]='bar'",
                 "[amenity]='library' or [amenity]='theatre' or [amenity]='courthouse'",
                 "[amenity]='cinema'",
                 "[amenity]='parking' and ([access] = 'public' or not [access] != '')",
                 "[amenity]='parking' and ([access] != '' and not [access] = 'public')",
                 "[amenity] = 'police'",
                 "[amenity] = 'fire_station'",
#                 "[amenity] = 'place_of_worship'",
#                 "[natural] = 'wood'",
#                 "[natural] = 'peak'",
#                 "[natural] = 'peak' and not [name] != ''",
#                 "[natural] = 'peak' and [name] != ''",
#                 "[natural] = 'volcano'",
#                 "[natural] = 'volcano' and not [name] != ''",
#                 "[natural] = 'volcano' and [name] != ''",
#                 "[natural] = 'cave_entrance'",
                 "[historic] = 'memorial' or [historic]='archaeological_site'",
#                 "[natural] = 'water' or [natural] = 'lake' or [landuse] = 'reservoir' or [landuse] = 'basin'",
                 "([leisure] != '' or [landuse] != '') and [point] = 'yes'",
                 "[natural] = 'bay'",
                 "[natural] = 'spring'",
                 "[tourism] = 'alpine_hut'",
                 "[tourism] = 'alpine_hut'",
                 "[amenity]='shelter'",
                 "[amenity] = 'bank'",
                 "[tourism] = 'hotel' or [tourism]='hostel' or [tourism]='chalet'",
                 "[amenity] = 'embassy'",
                 "[tourism]='guest_house'",
                 "[tourism]='bed_and_breakfast'",
                 "[amenity] = 'fuel' or [amenity]='bus_station'",
                 "[tourism] = 'camp_site'",
                 "[tourism] = 'caravan_site'",
                 "[waterway] = 'lock'",
                 "[leisure] = 'marina'",
                 "[leisure] = 'marina'",
                 "[tourism] = 'theme_park'",
                 "[tourism] = 'theme_park'",
                 "[tourism]='museum'",
                 "[amenity]='prison'",
                 "[tourism] = 'attraction'",
                 "[amenity] = 'university'",
                 "[amenity] = 'school' or [amenity] = 'college'",
                 "[amenity] = 'kindergarten'",
                 "[man_made] = 'lighthouse'",
                 "[man_made] = 'windmill'",
                 "[amenity] = 'hospital'",
                 "[amenity] = 'pharmacy'",
                 "[shop]='bakery' or [shop]='clothes' or [shop]='fashion' or [shop]='convenience' or [shop]='doityourself' or [shop]='hairdresser' or [shop]='butcher' or [shop]='car' or [shop]='car_repair' or [shop]='bicycle' or [shop]='florist'",
                 "[shop]='supermarket' or [shop]='department_store'",
                 "[military] = 'danger_area'",
                 "[aeroway] = 'gate'"
                 ]
    dom_strip_rules_from_style(document,"text",removeTexts)

def dom_strip_icons(document):
    dom_strip_style_and_layer(document,"points","amenity-points")
    dom_strip_POIs(document)
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

    dom_convert_to_grey(document)
    
    output= document.implementation.createLSOutput() 
    output.systemId= dfgrey
    output.encoding= 'utf-8' 
    serialiser= document.implementation.createLSSerializer() 
    serialiser.write(document, output)

    dom_strip_icons(document)
    
    output= document.implementation.createLSOutput() 
    output.systemId= dfnoicons
    output.encoding= 'utf-8' 
    serialiser= document.implementation.createLSSerializer() 
    serialiser.write(document, output)

def strip_doctype(f):
    p = subprocess.Popen(['sed','-i','2,5 d',f]) # -i means 'in place'
    p.wait()
    ### FIXME: the following line does not respect the prior attributes but rather sets its own. It should only set srs, buffer_size and maximum-extent.
    p = subprocess.Popen(['sed','-i','s/[<]Map .*[>]/\<Map background-color="#c9c9c9" srs="+init=epsg;3857" minimum-version="2.0.0" buffer-size="512" maximum-extent="-20037508.342789244,-20037508.342780735,20037508.342789244,20037508.342780709"\>/',f]) # -i means 'in place'
    p.wait()

def convert_icons_to_bw(source_symbols_dir,dest_symbols_dir):
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

    dest_dir_bw = os.path.join(dest_dir,"bw-mapnik")
    dest_dir_bw_symbols = os.path.join(dest_dir_bw,"symbols")
    dest_file_bw = 'osm-bw.xml'
    bw_file = os.path.join(dest_dir_bw,dest_file_bw)
    if not os.path.exists(dest_dir_bw_symbols):
        os.makedirs(dest_dir_bw_symbols)

    dest_dir_bw_noicons = os.path.join(dest_dir,"bw-noicons")
    dest_dir_bw_noicons_symbols = os.path.join(dest_dir_bw_noicons,"symbols")
    dest_file_bw_noicons = 'osm-bw-noicons.xml'
    bw_noicons_file = os.path.join(dest_dir_bw_noicons,dest_file_bw_noicons)
    if not os.path.exists(dest_dir_bw_noicons_symbols):
        os.makedirs(dest_dir_bw_noicons_symbols)

    convert_icons_to_bw(source_symbols_dir,dest_dir_bw_symbols)
    convert_icons_to_bw(source_symbols_dir,dest_dir_bw_noicons_symbols)
    transmogrify_file(os.path.join(source_dir,source_file),bw_file,bw_noicons_file)
    strip_doctype(bw_file)
    strip_doctype(bw_noicons_file)
    add_license_files(dest_dir_bw)
    add_license_files(dest_dir_bw_noicons)

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option("-s", "--sourcedir", dest="sourcedir", help="path to the source directory", default=".")
    parser.add_option("-f", "--sourcefile", dest="sourcefile", help="source filename, default is 'osm.xml')", default="osm.xml")
    parser.add_option("-d", "--destdir", dest="destdir", help="path to the destination directory, further dirs are created within. default is '/tmp'", default="/tmp")
    (options, args) = parser.parse_args()
    print options
    main(options.__dict__)
    sys.exit(0)
