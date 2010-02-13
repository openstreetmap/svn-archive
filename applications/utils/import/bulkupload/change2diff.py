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


"""
Generate a .diff.xml file (the response from the server after a diff upload)
from an uploaded changeset file (downloadable through
http://www.openstreetmap.org/api/0.6/changeset/<id>/download) -- this is
useful if the network connection broke after uploading the changeset but
before receiving the server response.
"""

__version__ = "$Revision: 21 $"

import os
import sys
import traceback
import codecs
import locale
import subprocess

import httplib

import xml.etree.cElementTree as ElementTree

import locale, codecs
locale.setlocale(locale.LC_ALL, "en_US.UTF-8")
encoding = locale.getlocale()[1]
sys.stdout = codecs.getwriter(encoding)(sys.stdout, errors = "replace")
sys.stderr = codecs.getwriter(encoding)(sys.stderr, errors = "replace")

try:
    this_dir = os.path.dirname(__file__)
    version = subprocess.Popen(["svnversion", this_dir], stdout = subprocess.PIPE).communicate()[0].strip()
    if len(sys.argv) != 2:
        print >>sys.stderr, u"Synopsis:"
        print >>sys.stderr, u"    %s <file_name>"
        sys.exit(1)

    filename = sys.argv[1]
    if not os.path.exists(filename):
        print >>sys.stderr, u"File %r doesn't exist!" % (filename,)
        sys.exit(1)
    if filename.endswith(".osc"):
        filename_base = filename[:-4]
    else:
        filename_base = filename

    tree = ElementTree.parse(filename)
    root = tree.getroot()
    if root.tag != "osmChange" or (root.attrib.get("version") != "0.3" and
            root.attrib.get("version") != "0.6"):
        print >>sys.stderr, u"File %s is not a v0.3 osmChange file!" % (filename,)
        sys.exit(1)

    diff_attr = {"version": "0.6", "generator": root.attrib.get("generator")}
    diff_root = ElementTree.Element("diffResult", diff_attr)
    diff_tree = ElementTree.ElementTree(diff_root)

    # Note this is broken, it assumes the nodes in the resulting osmChange
    # are in the same order they were in the osmChange sent to the server
    # and that the negative IDs there started at -1 and were increasing by
    # -1 with each new element.
    # A better idea (but still wrong) would be to parse the input osmChange
    # xml at the same time and assume that the elements in input and output
    # come in the same order, possibly with additional checks (lat/lon..)
    old_id = -1
    for operation in root:
        for element in operation:
            attr = {}
            if operation.tag == "create":
                attr["old_id"] = str(old_id)
                attr["new_id"] = element.attrib.get("id")
                attr["new_version"] = element.attrib.get("version")
                old_id -= 1
            elif operation.tag == "modify":
                attr["old_id"] = element.attrib.get("id")
                attr["new_id"] = element.attrib.get("id")
                attr["new_version"] = element.attrib.get("version")
            elif operation.tag == "delete":
                attr["old_id"] = element.attrib.get("id")
            else:
                print "unknown operation", operation.tag
                sys.exit(-1)
            diff = ElementTree.SubElement(diff_root, element.tag, attr)

    diff_tree.write(filename_base + ".diff.xml", "utf-8")

except Exception,err:
    print >>sys.stderr, repr(err)
    traceback.print_exc(file=sys.stderr)
    sys.exit(1)
