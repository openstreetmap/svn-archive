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
import sets
import optparse
import httplib2
import pickle
import os

#api_host='http://api.openstreetmap.org'
api_host='http://api06.dev.openstreetmap.org'


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
                                          '/api/0.6/changeset/create','PUT',xml)
        if resp.status != 200:
            print 'Error creating changeset:' + str(resp.status)
            exit(-1)
        self.changesetid=content
    def addItem(self,item):
        item.attrib['changeset']=self.changesetid
        self.addElem.append(item)
    def deleteItem(self,item):
        item.attrib['changeset']=self.changesetid
        self.deleteElem.append(item)
    def modifyITem(self,item):
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
                                            'POST', ET.tostring(xml))
        if resp.status != 200:
            print "Error uploading changeset:" + str(resp.status)
            print content
            exit(-1)
        else:
            self.processResult(content)
    def closeSet(self):
        resp,content=self.httpCon.request(api_host +
                                          '/api/0.6/changeset/' +
                                          self.changesetid + '/close','PUT')
        if resp.status != 200:
            print "Error closing changeset " + self.changesetid + ":" + resp.status
    #
    # Uploading a change set returns a <diffResult> containing elements
    # that map the old id to the new id
    # Process them.
    def processResult(self,content):
        diffResult=ET.fromstring(content)
        for child in diffResult.getchildren():
            old_id=child.attrib['old_id']
            new_id=child.attrib['new_id']
            idMap[old_id]=new_id
    
    def getAPILimit(self):
        return 1000

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

osmData=ET.parse(options.infile)
httpObj = httplib2.Http()
httpObj.add_credentials(options.user,options.password)
idMap={}
cnt=0
try:
    if os.stat(options.infile + ".db"):
        hasCache=True
except:
    hasCache=False
if hasCache:
    f=open(options.infile + ".db","r")
    idMap=pickle.load(f)
    f.close()


importProcessor=ImportProcessor(httpObj,options.comment,idMap)
for type in ('node','way','relation'):
    for elem in osmData.getiterator(type):
        # If elem.id is already mapped we can skip this object
        #
        id=elem.attrib['id']
        if idMap.has_key(id):
            continue
        #
        # If elem contains nodes, ways or relations as a child
        # then the ids need to be remapped.
        if elem.tag=='way':
            for child in elem.getiterator('nd'):
                if child.attrib.has_key('ref'):
                    old_id=child.attrib['ref']
                    if idMap.has_key(old_id):
                        child.attrib['ref'] = idMap[old_id]
        elif elem.tag=='relation':
            for child in elem.getiterator():
                if child.attrib.has_key('ref'):
                    old_id=child.attrib['ref']
                    if idMap.has_key(old_id):
                        child.attrib['ref'] = idMap[old_id]
        importProcessor.addItem(elem)
        if cnt >= importProcessor.getAPILimit():
            importProcessor.upload()
            f=open(options.infile+".db","w")
            pickle.dump(idMap,f)
            f.close()
            importProcessor=ImportProcessor(httpObj,options.comment,idMap)
            cnt=0
        cnt=cnt+1
    #for
importProcessor.upload()
f=open(options.infile+".db","w")
pickle.dump(idMap,f)
f.close()
