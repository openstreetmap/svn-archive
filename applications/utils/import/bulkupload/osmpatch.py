#! /usr/bin/python
# vim: fileencoding=utf-8 encoding=utf-8 et sw=4

# Copyright (C) 2009 Jacek Konieczny <jajcus@jajcus.net>
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


"""
Patches .osm files with .diff.xml files resulting from an upload of
an .osc file, for use when uploading a .osc file produced by osm2change.py
Note this removes deleted elements from the .osm file.
"""

__version__ = "$Revision: 21 $"

import os
import subprocess
import sys
import traceback
import codecs
import locale

import xml.etree.cElementTree as ElementTree

import locale, codecs
locale.setlocale(locale.LC_ALL, "en_US.UTF-8")
encoding = locale.getlocale()[1]
sys.stdout = codecs.getwriter(encoding)(sys.stdout, errors = "replace")
sys.stderr = codecs.getwriter(encoding)(sys.stderr, errors = "replace")

if len(sys.argv) < 2 or sys.argv[1] == "--help":
    print >>sys.stderr, u"Synopsis:"
    print >>sys.stderr, u"    %s <file.diff.xml> [osm-files-to-patch...]"
    sys.exit(1)

dd = [ {}, {}, {} ]
ddv = [ {}, {}, {} ]

# TODO: use ElementTree
# TODO: take multiple diff arguments
diff = open(sys.argv[1], "r")
sys.stdout.write("Parsing diff\n")
for line in diff:
    oldpos = line.find("old_id=\"")
    newpos = line.find("new_id=\"")
    newvpos = line.find("new_version=\"")
    if oldpos < 0:
        continue
    if line.find("node") >= 0:
        idx = 0
    elif line.find("way") >= 0:
        idx = 1
    elif line.find("relation") >= 0:
        idx = 2
    else:
        continue

    old = line[oldpos + 8:]
    old = old[:old.find("\"")]
    if newpos >= 0 and newvpos >= 0:
        new = line[newpos + 8:]
        newv = line[newvpos + 13:]
        new = new[:new.find("\"")]
        newv = newv[:newv.find("\"")]
    else:
        new = 0
        newv = 0
    dd[idx][old] = new
    ddv[idx][old] = newv

for filename in sys.argv[2:]:
    sys.stdout.write("Parsing " + filename + "\n")

    if not os.path.exists(filename):
        print >>sys.stderr, u"File %r doesn't exist!" % (filename,)
        sys.exit(1)
    if filename.endswith(".osm"):
        filename_base = filename[:-4]
    else:
        filename_base = filename

    tree = ElementTree.parse(filename)
    root = tree.getroot()
    if root.tag != "osm" or root.attrib.get("version") != "0.6":
        print >>sys.stderr, u"File %s is not a v0.6 osm file!" % (filename,)
        sys.exit(1)

    output_attr = {"version": "0.6", "generator": root.attrib.get("generator")}
    output_root = ElementTree.Element("osm", output_attr)
    output_tree = ElementTree.ElementTree(output_root)

    for element in root:
        copy = 1

        if "id" in element.attrib:
            old = element.attrib["id"]
            idx = [ "node", "way", "relation" ].index(element.tag)
            if old in dd[idx]:
                if dd[idx][old] and ddv[idx][old]:
                    element.attrib["id"] = dd[idx][old]
                    element.attrib["version"] = ddv[idx][old]
                else:
                    copy = 0

                if "action" in element.attrib:
                    action = element.attrib.pop("action")

                    if action in [ "delete" ]:
                        if copy:
                            print "Bad delete on id " + old
                    elif action in [ "create", "modify" ]:
                        if not copy:
                            print "Bad create/modify on id " + old
                    else:
                        print "Bad action on id " + old

            for member in element:
                if member.tag in [ "nd", "member" ]:
                    idx = 0;
                    if member.tag == "member":
                        idx = [ "node", "way", "relation" ].index(
                                member.attrib["type"])

                    ref = member.attrib["ref"]
                    if ref in dd[idx]:
                        member.attrib["ref"] = dd[idx][ref]

        if copy:
            output_root.append(element)

    output_tree.write(filename_base + ".osm.diffed", "utf-8")
