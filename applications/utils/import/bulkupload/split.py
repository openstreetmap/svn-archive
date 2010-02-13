#! /usr/bin/python3
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
Split large osmChange files.
"""

__version__ = "$Revision: 21 $"

import os
import sys
import traceback
import codecs
import locale
import subprocess

import http

import xml.etree.cElementTree as ElementTree

#locale.setlocale(locale.LC_ALL, "en_US.UTF-8")
#encoding = locale.getlocale()[1]
#sys.stdout = codecs.getwriter(encoding)(sys.stdout, errors = "replace")
#sys.stderr = codecs.getwriter(encoding)(sys.stderr, errors = "replace")

try:
    this_dir = os.path.dirname(__file__)
    version = subprocess.Popen(["svnversion", this_dir], stdout = subprocess.PIPE).communicate()[0].strip()
    if len(sys.argv) not in (2, 3):
        sys.stderr.write("Synopsis:\n")
        sys.stderr.write("    %s <file_name> [<num_of_pieces>\n]" % (sys.argv[0],))
        sys.exit(1)

    filename = sys.argv[1]
    if len(sys.argv) > 2:
        num_parts = int(sys.argv[2])
    else:
        num_parts = 2
    if not os.path.exists(filename):
        sys.stderr.write("File %r doesn't exist!\n" % (filename,))
        sys.exit(1)
    if filename.endswith(".osc"):
        filename_base = filename[:-4]
    else:
        filename_base = filename

    tree = ElementTree.parse(filename)
    root = tree.getroot()
    if root.tag != "osmChange" or (root.attrib.get("version") != "0.3" and
            root.attrib.get("version") != "0.6"):
        sys.stderr.write("File %s is not a v0.3 osmChange file!\n" % (filename,))
        sys.exit(1)

    element_count = 0
    for operation in root:
        element_count += len(operation)

    sys.stderr.write("Number of parts: %r\n" % (element_count,))
    part_size = int((element_count + num_parts - 1) / num_parts)

    part = 1
    operation_iter = iter(root)
    operation = next(operation_iter)
    elements = list(operation)
    while elements and operation:
        filename = "%s-part%i.osc" % (filename_base, part)
        part_root = ElementTree.Element(root.tag, root.attrib)
        part_tree = ElementTree.ElementTree(part_root)
        current_size = 0
        while operation and current_size < part_size:
            part_op = ElementTree.SubElement(part_root, operation.tag, operation.attrib)
            this_part_elements = elements[:(part_size-current_size)]
            elements = elements[(part_size-current_size):]
            for element in this_part_elements:
                part_op.append(element)
                current_size += 1
            if not elements:
                try:
                    while not elements:
                        operation = next(operation_iter)
                        elements = list(operation)
                except StopIteration:
                    operation = None
                    elements = []
        part_tree.write(filename, "utf-8")
        part += 1
    comment_fn = filename_base + ".comment"
    if os.path.exists(comment_fn):
        comment_file = codecs.open(comment_fn, "r", "utf-8")
        comment = comment_file.read().strip()
        comment_file.close()
        for part in range(1, num_parts + 1):
            comment_fn = "%s-part%i.comment" % (filename_base, part)
            comment_file = codecs.open(comment_fn, "w", "utf-8")
            comment_file.write("%s, part %i/%i" % (comment, part, num_parts))
            comment_file.close()
except Exception as err:
    sys.stderr.write(repr(err))
    traceback.print_exc(file=sys.stderr)
    sys.exit(1)
