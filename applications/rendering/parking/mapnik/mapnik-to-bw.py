# -*- coding: utf-8 -*-
# by kay

import os
#from xml.dom.minidom import parse, parseString
import pxdom
import colorsys

source_dir = './original-mapnik'
dest_dir = './bw-mapnik'

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
    #method 2:
    h,l,s = colorsys.rgb_to_hls(r, g, b)
    s = 0
    return colorsys.hls_to_rgb(h,l,s)

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

def transmogrify_file(f):
    
    #document = pxdom.parse(os.path.join(source_dir,f), {'entities': 1})

    dom= pxdom.getDOMImplementation('') 
    parser= dom.createLSParser(dom.MODE_SYNCHRONOUS, None) 
    document = parser.parseURI(os.path.join(source_dir,f))

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

    
    output= document.implementation.createLSOutput() 
    output.systemId= os.path.join(dest_dir,f)
    output.encoding= 'utf-8' 
    serialiser= document.implementation.createLSSerializer() 
    serialiser.write(document, output)


#savedPath = os.getcwd()
#os.chdir("original-mapnik")

transmogrify_file('osm.xml')

#print "bw=",color_to_bw(0.9,0.5,0)
#print "bw=",color_to_bw(0,0.9,0.5)
#col="#7f7f7f"
#print "rgb=",parse_color(col),color_to_bw(parse_color(col)),rgb_to_css(color_to_bw(parse_color(col)))

#print "rgb=",parse_color("#1f3")
#print "rgb=",parse_color("yellow")

#os.chdir( savedPath )
