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
# Author: Steve Singer <ssinger_pg@sympatico.ca>
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
#import sets
import optparse
import httplib2
import shelve
import os
import time
from xml.sax import make_parser, SAXParseException
from xml.sax.handler import ContentHandler

#api_host='http://api.openstreetmap.org'
api_host='http://api06.dev.openstreetmap.org'
headers = {
    'User-Agent' : 'bulk_upload.py',
}



class ImportProcessor:
    def __init__(self,httpObj,comment,idMap):
        self.comment=comment
        self.addElem=ET.Element('create')
        self.modifyElem=ET.Element('modify')
        self.deleteElem=ET.Element('delete')
        self.idMap = idMap
        self.httpCon = httpObj
        self.createChangeSet()
    def createChangeSet(self):
        createReq=ET.Element('osm',version="0.6")
        change=ET.Element('changeset')
        change.append(ET.Element('tag',k='comment',v=self.comment))
        change.append(ET.Element('tag',k='created_by', v='bulk_upload.py'))
        createReq.append(change)
        xml=ET.tostring(createReq)
        resp,content=self.httpCon.request(api_host +
                                          '/api/0.6/changeset/create','PUT',xml,headers=headers)
        if resp.status != 200:
            print 'Error creating changeset:' + str(resp.status)
            exit(-1)
        self.changesetid=content

    def createStructure(self, item):
        if item['type'] == 'node':
            struct = ET.Element('node', dict(item['attrs']))
            for tag in item['tags']:
                struct.append(ET.Element('tag',k=tag[0],v=tag[1]))
        elif item['type'] == 'way':
            struct = ET.Element('way', dict(item['attrs']))
            for tag in item['tags']:
                struct.append(ET.Element('tag',k=tag[0],v=tag[1]))
            for nd in item['childs']:
                struct.append(ET.Element('nd',ref=nd))
        elif item['type'] == 'relation':
            struct = ET.Element('relation', dict(item['attrs']))
            for tag in item['tags']:
                struct.append(ET.Element('tag',k=tag[0],v=tag[1]))
            for member in item['childs']:
                struct.append(ET.Element('member',type=member['type'],ref=member['ref'],role=member['role']))
        return struct

    def addItem(self,item):
        item = self.createStructure(item)
        item.attrib['changeset']=self.changesetid
        self.addElem.append(item)
    def deleteItem(self,item):
        item = self.createStructure(item)
        item.attrib['changeset']=self.changesetid
        self.deleteElem.append(item)
    def modifyItem(self,item):
        item = self.createStructure(item)
        item.attrib['changeset']=self.changesetid
        self.modifyElem.append(item)
    def upload(self):
        xml = ET.Element('osmChange')
        xml.append(self.addElem)
        xml.append(self.modifyElem)
        xml.append(self.deleteElem)
        print "Uploading change set:" + self.changesetid        
        resp,content = self.httpCon.request(api_host +
                                            '/api/0.6/changeset/'+self.changesetid+
                                            '/upload',
                                            'POST', ET.tostring(xml),headers=headers)        
        if resp.status != 200:
            print "Error uploading changeset:" + str(resp.status)
            print content
            exit(-1)
        else:
            self.processResult(content)
    def closeSet(self):
        resp,content=self.httpCon.request(api_host +
                                          '/api/0.6/changeset/' +
                                          self.changesetid + '/close','PUT',headers=headers)
        if resp.status != 200:
            print "Error closing changeset " + str(self.changesetid) + ":" + str(resp.status)
    #
    # Uploading a change set returns a <diffResult> containing elements
    # that map the old id to the new id
    # Process them.
    def processResult(self,content):
        diffResult=ET.fromstring(content)
        for child in diffResult.getchildren():
            old_id=child.attrib['old_id']
            if child.attrib.has_key('new_id'):
                new_id=child.attrib['new_id']
                self.idMap[old_id]=new_id.encode("utf-8")
            else:
                self.idMap[old_id]=old_id.encode("utf-8")
    
    def getAPILimit(self):
        return 500

# Allow enforcing of required arguements
# code from http://www.python.org/doc/2.3/lib/optparse-extending-examples.html
class OptionParser (optparse.OptionParser):

    def check_required (self, opt):
      option = self.get_option(opt)

      # Assumes the option's 'default' is set to None!
      if getattr(self.values, option.dest) is None:
          self.error("%s option not supplied" % option)
    
class BulkParser(ContentHandler):
    
    pathStack = []

    def getRef(self, attrs):
        ref = attrs.get('ref', None)
        if ref:
            new_id = self.idMap.get(ref.encode("utf-8"), None)
            if new_id:
                return new_id
        return ref

    def startDocument(self):
        self.httpObj = httplib2.Http()
        self.httpObj.add_credentials(options.user,options.password)
        self.idMap=shelve.open(options.infile+'.db')
        self.importer=ImportProcessor(self.httpObj,options.comment,self.idMap)
        self.object = None
        self.cnt = 0

    def endDocument(self):
        self.importer.upload()
        self.importer.closeSet()
        self.idMap.close()

    def startElement(self, name, attrs):
        self.pathStack.append(name)
        self.path = '/'.join(self.pathStack)
        if self.path in ('osm/node', 'osm/way', 'osm/relation'):
            id=attrs.get('id', None).encode("utf-8")
            if self.idMap.has_key(id):
                return

        if self.path == 'osm/node':
            self.object = {'type': 'node', 'attrs': attrs.copy(), 'tags': []}
        elif self.path == 'osm/way':
            self.object = {'type': 'way', 'attrs': attrs.copy(), 'childs': [], 'tags': []}
        elif self.path == 'osm/relation':
            self.object = {'type': 'relation', 'attrs': attrs.copy(), 'childs': [], 'tags': []}
        elif self.path in ('osm/node/tag', 'osm/way/tag', 'osm/relation/tag'):
            if self.object:
                self.object['tags'].append([attrs['k'], attrs['v']])
        elif self.path == 'osm/way/nd' and self.object:
            self.object['childs'].append(self.getRef(attrs))
        elif self.path == 'osm/relation/member' and self.object:
            member = {'type': attrs['type'], 'role': attrs['role']}
            member['ref'] = self.getRef(attrs)
            self.object['childs'].append(member)

    def endElement(self, name):
        if self.object and self.path in ('osm/node', 'osm/way', 'osm/relation'):
            if self.object:
                action = self.object['attrs'].get('action', None)
                if (action == 'delete'):
                    self.importer.deleteItem(self.object)
                elif (action == 'modify'):
                    self.importer.modifyItem(self.object)
                else:
                    self.importer.addItem(self.object)

                self.object = None

            if self.cnt >= self.importer.getAPILimit():
                self.importer.upload()
                self.importer.closeSet()
                self.importer=ImportProcessor(self.httpObj,options.comment,self.idMap)
                self.cnt=0
                time.sleep(15) # 15 seconds delay between changesets
            self.cnt=self.cnt+1
            

        del self.pathStack[-1]
        self.path = '/'.join(self.pathStack)


    def characters(self, data):
        pass

usage = "usage: %prog -i input.osm -u user -p password -c comment"

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


xmlParser = make_parser()
xmlParser.setContentHandler(BulkParser())

try:
    feedFile = open(options.infile)
except IOError, e:
    print("An error occured when opening the feed's URL: %s %s" % (options.infile, e))

try:
    xmlParser.parse(feedFile)
except SAXParseException, e:
    printr("An error occured when parsing the feed: %s line %u: %s" % (options.infile, e.getLineNumber(), e.getMessage()))

