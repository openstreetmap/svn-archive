#! /usr/bin/python2
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
Patches .osc files with .diff.xml files resulting from an upload of
a previous chunk of a multipart upload.
"""

__version__ = "$Revision: 21 $"

import os
import subprocess
import sys
import traceback
import codecs
import locale

import locale, codecs
locale.setlocale(locale.LC_ALL, "en_US.UTF-8")
encoding = locale.getlocale()[1]
sys.stdout = codecs.getwriter(encoding)(sys.stdout, errors = "replace")
sys.stderr = codecs.getwriter(encoding)(sys.stderr, errors = "replace")

if len(sys.argv) < 2 or sys.argv[1] == "--help":
    print >>sys.stderr, u"Synopsis:"
    print >>sys.stderr, u"    %s <file.diff.xml> [osm-files-to-patch...]"
    sys.exit(1)

dd = {}

diff = open(sys.argv[1], "r")
sys.stdout.write("Parsing diff\n")
for line in diff:
    oldpos = line.find("old_id=\"")
    newpos = line.find("new_id=\"")
    if oldpos < 0 or newpos < 0:
        continue

    # For the moment assume every element is operated on only
    # once in a changeset (TODO)
    old = line[oldpos + 8:]
    new = line[newpos + 8:]
    old = old[:old.find("\"")]
    new = new[:new.find("\"")]
    dd[old] = new

for f in sys.argv[2:]:
    sys.stdout.write("Parsing " + f + "\n")
    change = open(f, "r")
    newchange = open(f + ".diffed", "w")
    for line in change:
        refpos = line.find("ref=\"")
        if refpos > -1:
            ref = line[refpos + 5:]
            ref = ref[:ref.find("\"")]
            if ref in dd:
                line = line.replace("ref=\"" + ref + "\"", "ref=\"" + dd[ref] + "\"")
        newchange.write(line)
    newchange.close()
