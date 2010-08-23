#! /usr/bin/python2
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
Re-order changes in a changeset in a "logical" way (most autonomous
items first).  Useful thing to do before splitting a changeset and
uploading in pieces.
"""

__version__ = "$Revision: 21 $"

import os
import sys
import traceback
import codecs
import locale
import subprocess

import xml.etree.cElementTree as ElementTree

import locale, codecs
locale.setlocale(locale.LC_ALL, "en_US.UTF-8")
encoding = locale.getlocale()[1]
sys.stdout = codecs.getwriter(encoding)(sys.stdout, errors = "replace")
sys.stderr = codecs.getwriter(encoding)(sys.stderr, errors = "replace")

def makename(element):
    return element.tag + element.attrib.get("id")

opers = {}

def calcdepends(element):
    deps = {}
    for sub in element:
        if sub.tag == "nd":
            name = "node" + sub.attrib.get("ref")
        elif sub.tag == "member":
            name = sub.attrib.get("type") + sub.attrib.get("ref")
        else:
            continue

        if name in opers:
            # Technically we only need to append if
            # opers[name][1] == "create", but the effect will look better
            # if we do always.
            deps[name] = 1

    return deps

def calcdownscore(deps, scale):
    score = 0
    for dep in deps:
        if opers[dep]["downscore"] + scale > score:
            score = opers[dep]["downscore"] + scale
    return score

def calcdepnum(deps):
    depnum = 0
    for dep in deps:
        depnum += opers[dep]["depnum"] + 1
    return depnum

globalbbox = ([360.0, 360.0], [-360.0, -360.0])
globalscale = 0.0
def calcbbox(element, deps):
    if element.tag == "node":
        lat = float(element.attrib.get("lat"))
        lon = float(element.attrib.get("lon"))
        if lat < globalbbox[0][0]:
            globalbbox[0][0] = lat
        if lon < globalbbox[0][1]:
            globalbbox[0][1] = lon
        if lat > globalbbox[1][0]:
            globalbbox[1][0] = lat
        if lon > globalbbox[1][1]:
            globalbbox[1][1] = lon
        return ((lat, lon), (lat, lon))

    bbox = ([360.0, 360.0], [-360.0, -360.0])
    for dep in deps:
        if opers[dep]["bbox"][0][0] < bbox[0][0]:
            bbox[0][0] = opers[dep]["bbox"][0][0]
        if opers[dep]["bbox"][0][1] < bbox[0][1]:
            bbox[0][1] = opers[dep]["bbox"][0][1]
        if opers[dep]["bbox"][1][0] > bbox[1][0]:
            bbox[1][0] = opers[dep]["bbox"][1][0]
        if opers[dep]["bbox"][1][1] > bbox[1][1]:
            bbox[1][1] = opers[dep]["bbox"][1][1]
    return ((bbox[0][0], bbox[0][1]), (bbox[1][0], bbox[1][1]))

def update_refs(name, scale):
    for dep in opers[name]["depends"]:
        if opers[dep]["upscore"] < opers[name]["upscore"] + scale:
            opers[dep]["upscore"] = opers[name]["upscore"] + scale
            update_refs(dep, scale)
def update_only_some_refs_instead(name, scale):
    for dep in opers[name]["depends"]:
        opers[dep]["depended"][name] = 1

def recursiveusefulness(op, depth):
    v = len(op["depended"])
    if depth < 3:
        for dep in op["depends"]:
            v += recursiveusefulness(opers[dep], depth + 1) - 1
    return v

queue = []
geo = None

def queueup(names):
    global geo
    global queue
    global globalscale
    levelnames = None
    while names or levelnames:
        if not levelnames:
            names = [ x for x in names if x in opers ]
            if not names:
                return
            minscore = min([ opers[x]["upscore"] for x in names ])
            levelnames = {}
            newnames = {}
            for name in names:
                if opers[name]["upscore"] == minscore:
                     levelnames[name] = 1
                else:
                     newnames[name] = 1
            names = newnames
        maxscore = -1
        max = None
        delete = []
        for name in levelnames:
            if name in opers:
                op = opers[name]
                centre = ((op["bbox"][0][0] + op["bbox"][1][0]) * 0.5,
                        (op["bbox"][0][1] + op["bbox"][1][1]) * 0.5)
                #distance = math.hypot(centre[0] - geo[0], centre[1] - geo[1])
                distance = abs(centre[0] - geo[0]) + abs(centre[1] - geo[1])

                # This is the decision maker (possibly very wrong)
                score = 10.0 - op["upscore"] + \
                        1.0 / (distance / globalscale + 0.3) - \
                        op["depnum"] / (op["orig-depnum"] + 1)
                        #op["downscore"] * 0.1 + \
                        #recursiveusefulness(op, 0) * 0.01 + \
                        #(len(op["depended"]) - len(op["depends"])) * 0.00001

                if score > maxscore:
                    maxscore = score
                    max = name
            else:
                delete.append(name)
        for name in delete:
            del levelnames[name]
        if not levelnames:
            continue

        if opers[max]["depends"]:
            queueup(opers[max]["depends"])

        op = opers.pop(max)
        queue.append((op["element"], op["operation"]))

        for dep in op["depended"]:
            del opers[dep]["depends"][max]
            opers[dep]["depnum"] -= op["orig-depnum"] + 1

        centre = ((op["bbox"][0][0] + op["bbox"][1][0]) * 0.5,
                (op["bbox"][0][1] + op["bbox"][1][1]) * 0.5)
        geo = ((geo[0] * 2 + centre[0]) / 3,
                (geo[1] * 2 + centre[1]) / 3)

try:
    this_dir = os.path.dirname(__file__)
    version = subprocess.Popen(["svnversion", this_dir], stdout = subprocess.PIPE).communicate()[0].strip()
    if len(sys.argv) not in (2,):
        print >>sys.stderr, u"Synopsis:"
        print >>sys.stderr, u"    %s <file_name>"
        sys.exit(1)

    filename = sys.argv[1]
    if len(sys.argv) > 2:
        num_parts = int(sys.argv[2])
    else:
        num_parts = 2
    if not os.path.exists(filename):
        print >>sys.stderr, u"File %r doesn't exist!" % (filename,)
        sys.exit(1)
    if filename.endswith(".osc"):
        filename_base = filename[:-4]
    else:
        filename_base = filename

    print >>sys.stderr, u"Parsing osmChange..."
    tree = ElementTree.parse(filename)
    root = tree.getroot()
    if root.tag != "osmChange" or (root.attrib.get("version") != "0.3" and
            root.attrib.get("version") != "0.6"):
        print >>sys.stderr, u"File %s is not a v0.3 osmChange file!" % (filename,)
        sys.exit(1)

    print >>sys.stderr, u"Building dependency trees..."
    # Note: assumes each element appearing only once - easy to work around
    # (we should really detect those and compress all operations on any given
    #  item into 0 (creation + deletion) or 1 operation (any other scenario).)
    # (perhaps this should be done as a separate step before running this
    #  program.)
    deldeps = {"node": {}, "way": {}, "relation": {}}
    ops = []
    for operation in root:
        ops.append(operation)
        for element in operation:
            name = makename(element)

            if operation.tag == "delete":
                depends = deldeps[element.tag].copy()
                scale = 0.01
            else:
                depends = calcdepends(element)
                scale = 1

            depnum = calcdepnum(depends)
            opers[name] = {
                    "element": element,
                    "operation": operation.tag,
                    "scale": scale,
                    "depends": depends,
                    "depended": {},
                    #"downscore": calcdownscore(depends, scale), # unused now
                    "depnum": depnum,
                    "orig-depnum": depnum,
                    "upscore": 0,
                    "bbox": calcbbox(element, depends),
            }

            # Update references
            #update_refs(name, scale) # We now update them only once, at the end
            update_only_some_refs_instead(name, scale)

            # Assume that we don't delete objects we've just created, then
            # a delete operation depends on all the modify and delete
            # operations that appear before it.  We could calculate the
            # dependencies of a delete operation with more accuracy with
            # access to the current state but not with only the contents
            # of the current diff.
            if operation.tag in [ "modify", "delete" ]:
                if element.tag == "way":
                    deldeps["node"][name] = 1
                if element.tag == "relation":
                    for el in deldeps:
                        deldeps[el][name] = 1

    for name in opers:
        if not opers[name]["depended"]:
            update_refs(name, opers[name]["scale"])

    print >>sys.stderr, u"Sorting references..."
    for operation in ops:
        root.remove(operation)
    if opers: # Take a random starting point
        geo = opers[opers.keys()[0]]["bbox"][0]
        geo = (-1000, -1000)
        geo = globalbbox[0]
    globalscale = (globalbbox[1][0] - globalbbox[0][0] +
            globalbbox[1][1] - globalbbox[0][1])
    queueup(opers)

    print >>sys.stderr, u"Writing osmChange..."
    opattrs = { "generator": "smarter-sort.py", "version": "0.3" }
    popname = "desert storm"

    for element, opname in queue:
        if opname != popname:
            op = ElementTree.SubElement(root, opname, opattrs)
            popname = opname

        op.append(element)

    tree.write(filename_base + "-sorted.osc", "utf-8")

    comment_fn = filename_base + ".comment"
    if os.path.exists(comment_fn):
        comment_file = codecs.open(comment_fn, "r", "utf-8")
        comment = comment_file.read().strip()
        comment_file.close()
        comment_fn = filename_base + "-sorted.comment"
        comment_file = codecs.open(comment_fn, "w", "utf-8")
        print >> comment_file, comment
        comment_file.close()
except Exception,err:
    print >>sys.stderr, repr(err)
    traceback.print_exc(file=sys.stderr)
    sys.exit(1)
