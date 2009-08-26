#!/usr/bin/python
# -*- coding: utf-8 -*-
#
#
# This is a python version of
# the bulk_upload script for the 0.6 API.
#
# usage:
#      -i input.osm
#      -u username
#      -p password
#      -c comment for change set
#
# After each change set is sent to the server the id mappings are saved
# in inputfile.osm.db
# Subsequent calls to the script will read in these mappings,
# 
# If you change $input.osm between calls to the script (ie different data with the
# same file name) you should delete $input.osm.db
#
# Authors: Steve Singer <ssinger_pg@sympatico.ca>
#          Thomas Wood <grand.edgemaster@gmail.com>
#
# COPYRIGHT
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
import xml.etree.cElementTree as ET
import httplib2
import pickle
import os
import sys
try:
    import pygraph
except ImportError:
    from graph import graph as pygraph

__version__ = "$Id$"
user_agent = "bulk_upload.py/%s Python/%s" % (__version__.split()[2], sys.version.split()[0])

api_host='http://api.openstreetmap.org'
#api_host='http://api06.dev.openstreetmap.org'
headers = {
    'User-Agent' : user_agent,
}


class XMLException(Exception): pass
class APIError(Exception): pass

class ImportProcessor:
    currentChangeset = None
    idMap = None

    def __init__(self,user,password,idMap,tags={}):
        self.httpObj = httplib2.Http()
        self.httpObj.add_credentials(user,password)
        self.idMap = idMap
        self.tags = tags
        self.createChangeset()


    def parse(self, infile):
        relationStore = {}
        relationSort = False
        
        osmData=ET.parse(infile)
        osmRoot = osmData.getroot()
        if osmRoot.tag != "osm":
            raise XMLException("Input file must be a .osm XML file (JOSM-style)")

        for elem in osmRoot.getiterator('member'):
            if elem.attrib['type'] == 'relation':
                relationSort = True
                break
        
        for type in ('node','way','relation'):
            for elem in osmRoot.getiterator(type):
                # If elem.id is already mapped we can skip this object
                #
                id=elem.attrib['id']
                if self.idMap[type].has_key(id):
                    continue
                #
                # If elem contains nodes, ways or relations as a child
                # then the ids need to be remapped.
                if elem.tag=='way':
                    for child in elem.getiterator('nd'):
                        if child.attrib.has_key('ref'):
                            old_id=child.attrib['ref']
                            if idMap['node'].has_key(old_id):
                                child.attrib['ref'] = self.idMap['node'][old_id]
                elif elem.tag=='relation':
                    if relationSort:
                        relationStore[elem.attrib['id']] = elem
                    else:
                        self.updateRelationMemberIds(elem)
                        self.addToChangeset(elem)

                if elem.tag != 'relation':
                    self.addToChangeset(elem)

        if relationSort:
            gr = pygraph.digraph()
            gr.add_nodes(relationStore.keys())
            for id in relationStore:
                for child in relationStore[id].getiterator('member'):
                    if child.attrib['type'] == 'relation':
                        gr.add_edge(id, child.attrib['ref'])

            # Tree is unconnected, hook them all up to a root
            gr.add_node('root')
            for item in gr.node_incidence.iteritems():
                if not item[1]:
                    gr.add_edge('root', item[0])
            for relation in gr.traversal('root', 'post'):
                if relation == 'root': continue
                self.updateRelationMemberIds(relationStore[relation])
                self.addToChangeset(relationStore[relation])

        self.currentChangeset.close() # (uploads any remaining diffset changes)

    def updateRelationMemberIds(self, elem):
        for child in elem.getiterator('member'):
            if child.attrib.has_key('ref'):
                old_id=child.attrib['ref']
                old_id_type = child.attrib['type']
                if self.idMap[old_id_type].has_key(old_id):
                    child.attrib['ref'] = self.idMap[old_id_type][old_id]

    def createChangeset(self):
        self.currentChangeset = Changeset(tags=self.tags, idMap=self.idMap, httpObj=self.httpObj)

    def addToChangeset(self, elem):
        if elem.attrib.has_key('action'):
            action = elem.attrib['action']
        else:
            action = 'create'

        try:
            self.currentChangeset.addChange(action, elem)
        except ChangesetClosed:
            self.createChangeset()
            self.currentChangeset.addChange(action, elem)

class IdMap:
    # Default IdMap class, using a Pickle backend, this can be extended
    # - if ids in other files need replacing, for example
    idMap = {'node':{}, 'way':{}, 'relation':{}}

    def __init__(self, filename=''):
        self.filename = filename
        self.load()

    def __getitem__(self, item):
        return self.idMap[item]

    def load(self):
        try:
            if os.stat(self.filename):
                f=open(self.filename, "r")
                self.idMap=pickle.load(f)
                f.close()
        except:
            pass

    def save(self):
        f=open(self.filename+".tmp","w")
        pickle.dump(self.idMap,f)
        f.close()
        os.rename(self.filename+".tmp", self.filename)

class ChangesetClosed(Exception): pass

class Changeset:
    id = None
    tags = {}
    currentDiffSet = None
    opened = False
    closed = False
    
    itemcount = 0

    def __init__(self,tags={},idMap=None, httpObj=None):
        self.id = None
        self.tags = tags
        self.idMap = idMap
        self.httpObj = httpObj
        
        self.createDiffSet()

    def open(self):
        createReq = ET.Element('osm', version="0.6")
        change = ET.SubElement(createReq, 'changeset')
        for tag in self.tags:
            ET.SubElement(change, 'tag', k=tag, v=self.tags[tag])
        
        xml = ET.tostring(createReq)
        resp,content = self.httpObj.request(api_host +
            '/api/0.6/changeset/create','PUT',xml,headers=headers)
        if resp.status != 200:
            raise APIError('Error creating changeset:' + str(resp.status))
        self.id = content
        print "Created changeset: %s" % self.id
        self.opened = True

    def close(self):
        if not self.opened:
            return
        self.currentDiffSet.upload()
        
        resp,content = self.httpObj.request(api_host +
            '/api/0.6/changeset/' +
            self.id + '/close','PUT',headers=headers)
        if resp.status != 200:
            print "Error closing changeset " + str(self.id) + ":" + str(resp.status)
        print "Closed changeset: %s" % self.id
        self.closed = True

    def createDiffSet(self):
        self.currentDiffSet = DiffSet(self, self.idMap, self.httpObj)

    def addChange(self,action,item):
        if not self.opened:
            self.open() # So that a changeset is only opened when required.
        if self.closed:
            raise ChangesetClosed
        item.attrib['changeset']=self.id
        try:
            self.currentDiffSet.addChange(action,item)
        except DiffSetClosed:
            self.createDiffSet()
            self.currentDiffSet.addChange(action,item)
        
        self.itemcount += 1
        if self.itemcount >= self.getItemLimit():
            self.currentDiffSet.upload()
            self.close()

    def getItemLimit(self):
        # This is actually dictated by the API's capabilities call
        return 50000

class DiffSetClosed(Exception): pass

class DiffSet:
    itemcount = 0
    closed = False
    
    def __init__(self, changeset, idMap, httpObj):
        self.elems = {
            'create': ET.Element('create'),
            'modify': ET.Element('modify'),
            'delete': ET.Element('delete')
        }
        self.changeset = changeset
        self.idMap = idMap
        self.httpObj = httpObj

    def __getitem__(self, item):
        return self.elems[item]

    def addChange(self,action,item):
        if self.closed:
            raise DiffSetClosed
        self[action].append(item)

        self.itemcount += 1
        if self.itemcount >= self.getItemLimit():
            self.upload()

    def upload(self):
        if not self.itemcount:
            return False
    
        xml = ET.Element('osmChange')
        for elem in self.elems.values():
            xml.append(elem)
        print "Uploading to changeset %s" % self.changeset.id

        xmlstr = ET.tostring(xml)
        #f = open("/tmp/%s.osc" % self.changeset.id, 'a')
        #f.write(xmlstr)
        #f.write("\n\n")
        #f.close()

        resp,content = self.httpObj.request(api_host +
                                            '/api/0.6/changeset/'+self.changeset.id+
                                            '/upload',
                                            'POST', xmlstr,headers=headers)
        if resp.status != 200:
            print "Error uploading changeset:" + str(resp.status)
            print content
            exit(-1)
        else:
            self.processResult(content)
            self.idMap.save()
            self.closed = True

    # Uploading a diffset returns a <diffResult> containing elements
    # that map the old id to the new id
    # Process them.
    def processResult(self,content):
        diffResult=ET.fromstring(content)
        for child in diffResult.getchildren():
            id_type = child.tag
            old_id=child.attrib['old_id']
            if child.attrib.has_key('new_id'):
                new_id=child.attrib['new_id']
                self.idMap[id_type][old_id]=new_id
            else:
                # (Object deleted)
                self.idMap[id_type][old_id]=old_id

    def getItemLimit(self):
        # This is an arbitrary self-imposed limit (that must be below the changeset limit)
        # so to limit upload times to sensible chunks.
        return 1000


if __name__ == "__main__":
    import optparse
    # Allow enforcing of required arguements
    # code from http://www.python.org/doc/2.3/lib/optparse-extending-examples.html
    class OptionParser (optparse.OptionParser):

        def check_required (self, opt):
            option = self.get_option(opt)

            # Assumes the option's 'default' is set to None!
            if getattr(self.values, option.dest) is None:
                self.error("%s option not supplied" % option)

    usage = "usage: %prog -i input.osm -u user -p password"

    parser = OptionParser(usage)
    parser.add_option("-i", "--input", dest="infile", help="read data from input.osm")
    parser.add_option("-u", "--user", dest="user", help="username")
    parser.add_option("-p", "--password", dest="password", help="password")
    parser.add_option("-c", "--comment", dest="comment", help="ChangeSet Comment")
    (options, args) = parser.parse_args()

    parser.check_required("-i")
    parser.check_required("-u")
    parser.check_required("-p")
    parser.check_required("-c")

    idMap = IdMap(options.infile + ".db")
    tags = {
        'created_by': user_agent,
        'comment': options.comment
    }
    importProcessor = ImportProcessor(options.user,options.password,idMap,tags)
    importProcessor.parse(options.infile)
