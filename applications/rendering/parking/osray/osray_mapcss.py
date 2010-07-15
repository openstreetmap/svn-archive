#!/usr/bin/env python
# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET
from ceyx.MapCSS import MapCSS

# see http://wiki.openstreetmap.org/wiki/Ceyx#I_want_to_build_my_own_render_based_on_your_MapCSS_parser._How.3F

class OSMstyle:
    def __init__(self, cssfile):
        # load and parse the CSS
        self.mapcss    =  MapCSS(cssfile)

    def get_style_for_ele(self, ele, zoom):
        rules = self.mapcss.apply_to_ele(ele, zoom)
        return rules

if __name__ == '__main__':
    osm2svg = OSMstyle("style.mapcss")
    #create a fake element to ask the style for
    ele = ET.Element("way")
    tag = ET.SubElement(ele, "tag")
    tag.set("k", "highway")
    tag.set("v", "motorway")

    print str(osm2svg.get_style_for_ele(ele,13))

# example answer:
# {'width': '4.5', 'color': '#809BC0', 'linecap': 'round', 'linecaps': 'round, linejoin: round', 'z-index': '0.1', 'font-color': 'black', 'casing-width': '5', 'font-family': 'DejaVu', 'text-halo-color': 'white', 'casing-color': '#202020'}
