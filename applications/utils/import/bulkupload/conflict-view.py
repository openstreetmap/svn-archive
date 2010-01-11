#! /usr/bin/python
# vim: fileencoding=utf-8 encoding=utf-8 et sw=4

# Copyright (C) 2009 Andrzej Zaborowski <balrogg@gmail.com>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


import os
import sys
import math
import codecs
import locale

import httplib

import xml.etree.cElementTree as ElementTree

import locale, codecs
locale.setlocale(locale.LC_ALL, "en_US.UTF-8")
encoding = locale.getlocale()[1]
sys.stdout = codecs.getwriter(encoding)(sys.stdout, errors = "replace")
sys.stderr = codecs.getwriter(encoding)(sys.stderr, errors = "replace")

def osmparse(filename):
    tree = ElementTree.parse(filename).getroot()
    elems = {}
    if tree.tag == "osm" and tree.attrib.get("version") == "0.6":
        for element in tree:
            if not "id" in element.attrib:
                continue
            id = element.attrib["id"]
            if "version" in element.attrib:
                v = element.attrib["version"]
            else:
                v = "0"
            if "action" in element.attrib:
                elems[id] = (element.attrib["action"], v, element)
    elif tree.tag == "osmChange" and \
            tree.attrib.get("version") in [ "0.3", "0.6" ]:
        for op in tree:
            for element in op:
                id = element.attrib["id"]
                if "version" in element.attrib:
                    v = element.attrib["version"]
                else:
                    v = "0"
                elems[id] = (op.tag, v, element)
    else:
        print >>sys.stderr, u"File %s is in unknown format %s v%s!" % \
                (filename, tree.tag, tree.attrib["version"])
        sys.exit(1)
    return elems

if len(sys.argv) != 3:
    print >>sys.stderr, u"Synopsis:"
    print >>sys.stderr, u"    %s <osm-or-osc-file> <osm-or-osc-file>"
    sys.exit(1)

a = osmparse(sys.argv[1])
b = osmparse(sys.argv[2])

for id in a:
    if id in b:
        print a[id][2].tag + " " + id + ":", \
                "A:", a[id][0], "(v" + a[id][1] + ")", \
                "B:", b[id][0], "(v" + b[id][1] + ")"
