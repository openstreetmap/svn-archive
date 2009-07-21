#-*- coding: utf-8 -*-

###########################################################################
##                                                                       ##
## Copyrights Etienne Chové <chove@crans.org> 2009                       ##
##                                                                       ##
## This program is free software: you can redistribute it and/or modify  ##
## it under the terms of the GNU General Public License as published by  ##
## the Free Software Foundation, either version 3 of the License, or     ##
## (at your option) any later version.                                   ##
##                                                                       ##
## This program is distributed in the hope that it will be useful,       ##
## but WITHOUT ANY WARRANTY; without even the implied warranty of        ##
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         ##
## GNU General Public License for more details.                          ##
##                                                                       ##
## You should have received a copy of the GNU General Public License     ##
## along with this program.  If not, see <http://www.gnu.org/licenses/>. ##
##                                                                       ##
###########################################################################

## HomePage : http://wiki.openstreetmap.org/wiki/PythonOsmApi

###########################################################################
## History                                                               ##
###########################################################################
## 0.2.2   2009-07-13 can identify applications built on top of the lib  ##
## 0.2.1   2009-05-05 some changes in constructor -- chove@crans.org     ##
## 0.2     2009-05-01 initial import                                     ##
###########################################################################

__version__ = '0.2.2'

import httplib, base64, xml.dom.minidom

###########################################################################
## Main class                                                            ##

class OsmApi:
    
    def __init__(self, username = None, password = None, passwordfile = None, appid = "", created_by = "PythonOsmApi/"+__version__, api = "www.openstreetmap.org"):

        # Get username
        if username:
            self._username = username
        elif passwordfile:
            self._username =  open(passwordfile).readline().split(":")[0].strip()            
    
        # Get password
        if password:
            self._password   = password
        elif passwordfile:
            for l in open(passwordfile).readlines():
                l = l.strip().split(":")
                if l[0] == self._username:
                    self._password = l[1]

        # Get API
        self._api = api

        # Get created_by
        if appid == "":
                self._created_by = created_by
        else:
                self._created_by = appid + " (" + created_by + ")"

        # Initialisation     
        self._CurrentChangesetId = -1

    #######################################################################
    # Capabilities                                                        #
    #######################################################################
    
    def Capabilities(self):
        raise NotImplemented

    #######################################################################
    # Node                                                                #
    #######################################################################

    def NodeGet(self, NodeId, NodeVersion = -1):
        """ Returns NodeData for node #NodeId. """
        uri = "/api/0.6/node/"+str(NodeId)
        if NodeVersion <> -1: uri += "/"+str(NodeVersion)
        data = self._get(uri)
        data = xml.dom.minidom.parseString(data.encode("utf-8"))
        data = data.getElementsByTagName("osm")[0].getElementsByTagName("node")[0]
        return self._DomParseNode(data)

    def NodeUpdate(self, NodeData):
        """ Updates node with NodeData. Returns updated NodeData (without timestamp). """
        if self._CurrentChangesetId == -1:
            raise Exception, "No changeset currently opened"
        NodeData[u"changeset"] = self._CurrentChangesetId
        result = self._put("/api/0.6/node/"+str(NodeData[u"id"]), self._XmlBuild("node", NodeData))
        NodeData[u"version"] = int(result.strip())
        if u"timestamp" in NodeData: NodeData.pop(u"timestamp")
        return NodeData

    def NodeDelete(self, NodeData):
        """ Delete node with NodeData. Returns updated NodeData (without timestamp). """
        if self._CurrentChangesetId == -1:
            raise Exception, "No changeset currently opened"
        NodeData[u"changeset"] = self._CurrentChangesetId
        result = self._delete("/api/0.6/node/"+str(NodeData[u"id"]), self._XmlBuild("node", NodeData))
        NodeData[u"version"] = int(result.strip())
        NodeData[u"visible"] = False
        if u"timestamp" in NodeData: NodeData.pop(u"timestamp")
        return NodeData

    def NodeCreate(self, NodeData):
        """ Creates a node. Returns updated NodeData (without timestamp). """
        if self._CurrentChangesetId == -1:
            raise Exception, "No changeset currently opened"
        if NodeData.get(u"id", -1) > 0:
            raise Exception, "This node already exists"
        NodeData[u"changeset"] = self._CurrentChangesetId
        result = self._put("/api/0.6/node/create", self._XmlBuild("node", NodeData))
        NodeData[u"id"]      = int(result.strip())
        NodeData[u"version"] = 1
        if u"timestamp" in NodeData: NodeData.pop(u"timestamp")
        return NodeData

    def NodeHistory(self, NodeId):
        """ Returns dict(NodeVerrsion: NodeData). """
        uri = "/api/0.6/node/"+str(NodeId)+"/history"
        data = self._get(uri)
        data = xml.dom.minidom.parseString(data.encode("utf-8"))
        result = {}
        for data in data.getElementsByTagName("osm")[0].getElementsByTagName("node"):
            data = self._DomParseNode(data)
            result[data[u"version"]] = data
        return result

    def NodeWays(self, NodeId):
        """ Returns [WayData, ... ] containing node #NodeId. """
        # GET node/#/ways TODO
        raise NotImplemented
    
    def NodeRelations(self, NodeId):
        """ Returns [RelationData, ... ] containing node #NodeId. """
        # GET node/#/relations TODO
        raise NotImplemented

    def NodesGet(self, NodeIdList):
        """ Will not be implemented. """
        raise NotImplemented

    #######################################################################
    # Way                                                                 #
    #######################################################################

    def WayGet(self, WayId, WayVersion = -1):
        """ Returns WayData for way #WayId. """
        uri = "/api/0.6/way/"+str(WayId)
        if WayVersion <> -1: uri += "/"+str(WayVersion)
        data = self._get(uri)
        data = xml.dom.minidom.parseString(data.encode("utf-8"))
        data = data.getElementsByTagName("osm")[0].getElementsByTagName("way")[0]
        return self._DomParseWay(data)
    
    def WayUpdate(self, WayData):
        """ Updates way with WayData. Returns updated WayData (without timestamp). """
        if self._CurrentChangesetId == -1:
            raise Exception, "No changeset currently opened"
        WayData[u"changeset"] = self._CurrentChangesetId
        result = self._put("/api/0.6/way/"+str(WayData[u"id"]), self._XmlBuild("way", WayData))
        WayData[u"version"] = int(result.strip())
        if u"timestamp" in WayData: WayData.pop(u"timestamp")
        return WayData

    def WayDelete(self, WayData):
        """ Delete way with WayData. Returns updated WayData (without timestamp). """
        if self._CurrentChangesetId == -1:
            raise Exception, "No changeset currently opened"
        WayData[u"changeset"] = self._CurrentChangesetId
        result = self._delete("/api/0.6/way/"+str(WayData[u"id"]), self._XmlBuild("way", WayData))
        WayData[u"version"] = int(result.strip())
        WayData[u"visible"] = False
        if u"timestamp" in WayData: WayData.pop(u"timestamp")
        return WayData

    def WayCreate(self, WayData):
        """ Creates a way. Returns updated WayData (without timestamp). """
        if self._CurrentChangesetId == -1:
            raise Exception, "No changeset currently opened"
        if NodeData.get(u"id", -1) > 0:
            raise Exception, "This way already exists"
        WayData[u"changeset"] = self._CurrentChangesetId
        result = self._put("/api/0.6/way/create", self._XmlBuild("way", WayData))
        WayData[u"id"]      = int(result.strip())
        WayData[u"version"] = 1
        if u"timestamp" in WayData: WayData.pop(u"timestamp")
        return WayData

    def WayHistory(self, WayId):
        """ Returns dict(WayVerrsion: WayData). """
        uri = "/api/0.6/way/"+str(WayId)+"/history"
        data = self._get(uri)
        data = xml.dom.minidom.parseString(data.encode("utf-8"))
        result = {}
        for data in data.getElementsByTagName("osm")[0].getElementsByTagName("way"):
            data = self._DomParseWay(data)
            result[data[u"version"]] = data
        return result
    
    def WayRelations(self, WayId):
        """ Returns [RelationData, ...] containing way #WayId. """
        # GET way/#/relations
        raise NotImplemented

    def WayFull(self, WayId):
        """ Will not be implemented. """
        raise NotImplemented

    def WaysGet(self, WayIdList):
        """ Will not be implemented. """
        raise NotImplemented

    #######################################################################
    # Relation                                                            #
    #######################################################################

    def RelationGet(self, RelationId, RelationVersion = -1):
        """ Returns RelationData for relation #RelationId. """
        uri = "/api/0.6/relation/"+str(RelationId)
        if RelationVersion <> -1: uri += "/"+str(RelationVersion)
        data = self._get(uri)
        #print type(data)
        data = xml.dom.minidom.parseString(data.encode("utf-8"))
        data = data.getElementsByTagName("osm")[0].getElementsByTagName("relation")[0]
        return self._DomParseRelation(data)

    def RelationUpdate(self, RelationData):
        """ Updates relation with RelationData. Returns updated RelationData (without timestamp). """
        if self._CurrentChangesetId == -1:
            raise Exception, "No changeset currently opened"
        RelationData[u"changeset"] = self._CurrentChangesetId
        result = self._put("/api/0.6/relation/"+str(RelationData[u"id"]), self._XmlBuild("relation", RelationData))
        RelationData[u"version"] = int(result.strip())
        if u"timestamp" in RelationData: RelationData.pop(u"timestamp")
        return RelationData

    def RelationDelete(self, RelationData):
        """ Delete relation with RelationData. Returns updated RelationData (without timestamp). """
        if self._CurrentChangesetId == -1:
            raise Exception, "No changeset currently opened"
        RelationData[u"changeset"] = self._CurrentChangesetId
        result = self._delete("/api/0.6/relation/"+str(RelationData[u"id"]), self._XmlBuild("relation", RelationData))
        RelationData[u"version"] = int(result.strip())
        RelationData[u"visible"] = False
        if u"timestamp" in RelationData: RelationData.pop(u"timestamp")
        return RelationData

    def RelationCreate(self, RelationData):
        """ Creates a relation. Returns updated RelationData (without timestamp). """
        if self._CurrentChangesetId == -1:
            raise Exception, "No changeset currently opened"
        if NodeData.get(u"id", -1) > 0:
            raise Exception, "This relation already exists"
        RelationData[u"changeset"] = self._CurrentChangesetId
        result = self._put("/api/0.6/relation/create", self._XmlBuild("relation", RelationData))
        RelationData[u"id"]      = int(result.strip())
        RelationData[u"version"] = 1
        if u"timestamp" in RelationData: RelationData.pop(u"timestamp")
        return RelationData

    def RelationHistory(self, RelationId):
        """ Returns dict(RelationVerrsion: RelationData). """
        uri = "/api/0.6/relation/"+str(RelationId)+"/history"
        data = self._get(uri)
        data = xml.dom.minidom.parseString(data.encode("utf-8"))
        result = {}
        for data in data.getElementsByTagName("osm")[0].getElementsByTagName("relation"):
            data = self._DomParseRelation(data)
            result[data[u"version"]] = data
        return result
    
    def RelationRelations(self, RelationId):
        """ Returns list of RelationData containing relation #RelationId. """
        # GET relation/#/relations TODO
        raise NotImplemented

    def RelationFull(self, RelationId):
        """ Will not be implemented. """
        raise NotImplemented

    def RelationsGet(self, RelationIdList):
        """ Will not be implemented. """
        raise NotImplemented

    #######################################################################
    # Changeset                                                           #
    #######################################################################

    def ChangesetGet(self, ChangesetId):
        """ Returns ChangesetData for changeset #ChangesetId. """
        data = self._get("/api/0.6/changeset/"+str(ChangesetId))
        data = xml.dom.minidom.parseString(data.encode("utf-8"))
        data = data.getElementsByTagName("osm")[0].getElementsByTagName("changeset")[0]
        return self._DomParseChangeset(data)
    
    def ChangesetUpdate(self, ChangesetTags = {}):
        """ Updates current changeset with ChangesetTags. """
        if self._CurrentChangesetId == -1:
            raise Exception, "No changeset currently opened"
        if u"created_by" not in ChangesetTags:
            ChangesetTags[u"created_by"] = self._created_by
        result = self._put("/api/0.6/changeset/"+str(self._CurrentChangesetId), self._XmlBuild("changeset", {u"tag": ChangesetTags}))
        return self._CurrentChangesetId

    def ChangesetCreate(self, ChangesetTags = {}):
        """ Opens a changeset. Returns #ChangesetId. """
        if self._CurrentChangesetId <> -1:
            raise Exception, "Changeset alreadey opened"
        if u"created_by" not in ChangesetTags:
            ChangesetTags[u"created_by"] = self._created_by
        result = self._put("/api/0.6/changeset/create", self._XmlBuild("changeset", {u"tag": ChangesetTags}))
        self._CurrentChangesetId   = int(result)
        self._CurrentChangesetTags = ChangesetTags
        self._CurrentChangesetCpt  = 0
        return self._CurrentChangesetId
    
    def ChangesetClose(self):
        """ Closes current changeset. Returns #ChangesetId. """
        if self._CurrentChangesetId == -1:
            raise Exception, "No changeset currently opened"
        result = self._put("/api/0.6/changeset/"+str(self._CurrentChangesetId)+"/close", u"")
        CurrentChangesetId = self._CurrentChangesetId
        self._CurrentChangesetId = -1
        return CurrentChangesetId

    def ChangesetUpload(self):
        raise NotImplemented

    def ChangesetDownload(self, ChangesetId):
        """ Download data from a changeset. Returns list of dict {type: node|way|relation, action: create|delete|modify, data: {}}. """
        uri = "/api/0.6/changeset/"+str(ChangesetId)+"/download"
        data = self._get(uri)
        data = xml.dom.minidom.parseString(data.encode("utf-8"))
        data = data.getElementsByTagName("osmChange")[0]
        result = []
        for action in data.childNodes:
            if action.nodeName == u"#text": continue
            for elem in action.childNodes:
                if elem.nodeName == u"node":
                    result.append({u"action":action.nodeName, u"type": elem.nodeName, u"data": self._DomParseNode(elem)})
                elif elem.nodeName == u"way":
                    result.append({u"action":action.nodeName, u"type": elem.nodeName, u"data": self._DomParseWay(elem)})                        
                elif elem.nodeName == u"relation":
                    result.append({u"action":action.nodeName, u"type": elem.nodeName, u"data": self._DomParseRelation(elem)})
        return result

    def ChangesetsGet(self):
        raise NotImplemented

    #######################################################################
    # Other                                                               #
    #######################################################################

    def Map(self):
        raise NotImplemented

    def Trackpoints(self):
        raise NotImplemented
    
    def Changes(self):
        raise NotImplemented

    #######################################################################
    # Internal http function                                              #
    #######################################################################

    def _http(self, cmd, path, auth, send):
        h = httplib.HTTPConnection(self._api, 80)
        h.putrequest(cmd, path)
        h.putheader('User-Agent', self._created_by)
        if auth:
            h.putheader('Authorization', 'Basic ' + base64.encodestring(self._username + ':' + self._password).strip())
        if send:
            send = send.encode("utf-8")
            h.putheader('Content-Length', len(send))
        h.endheaders()
        if send:
            h.send(send)
        response = h.getresponse()
        if response.status <> 200:
            raise Exception, "API returns unexpected status code "+str(response.status)+" ("+response.reason+")"
        return response.read().decode("utf-8")
    
    def _get(self, path):
        return self._http('GET', path, False, None)

    def _put(self, path, data):
        return self._http('PUT', path, True, data)
    
    def _delete(self, path, data):
        return self._http('DELETE', path, True, data)
    
    #######################################################################
    # Internal dom function                                               #
    #######################################################################
    
    def _DomGetAttributes(self, DomElement):
        """ Returns a formated dictionnary of attributes of a DomElement. """
        result = {}
        for k, v in DomElement.attributes.items():
            k = k #.decode("utf8")
            v = v #.decode("utf8")
            if k == u"uid"         : v = int(v)
            elif k == u"changeset" : v = int(v)
            elif k == u"version"   : v = int(v)
            elif k == u"id"        : v = int(v)
            elif k == u"lat"       : v = float(v)
            elif k == u"lon"       : v = float(v)
            elif k == u"open"      : v = v=="true"
            elif k == u"visible"   : v = v=="true"
            elif k == u"ref"       : v = int(v)
            result[k] = v
        return result            
        
    def _DomGetTag(self, DomElement):
        """ Returns the dictionnary of tags of a DomElement. """
        result = {}
        for t in DomElement.getElementsByTagName("tag"):
            k = t.attributes["k"].value #.decode("utf8")
            v = t.attributes["v"].value #.decode("utf8")
            result[k] = v
        return result

    def _DomGetNd(self, DomElement):
        """ Returns the list of nodes of a DomElement. """
        result = []
        for t in DomElement.getElementsByTagName("nd"):
            result.append(int(int(t.attributes["ref"].value)))
        return result            

    def _DomGetMember(self, DomElement):
        """ Returns a list of relation members. """
        result = []
        for m in DomElement.getElementsByTagName("member"):
            result.append(self._DomGetAttributes(m))
        return result

    def _DomParseNode(self, DomElement):
        """ Returns NodeData for the node. """
        result = self._DomGetAttributes(DomElement)
        result[u"tag"] = self._DomGetTag(DomElement)
        return result

    def _DomParseWay(self, DomElement):
        """ Returns WayData for the way. """
        result = self._DomGetAttributes(DomElement)
        result[u"tag"] = self._DomGetTag(DomElement)
        result[u"nd"]  = self._DomGetNd(DomElement)        
        return result
    
    def _DomParseRelation(self, DomElement):
        """ Returns RelationData for the relation. """
        result = self._DomGetAttributes(DomElement)
        result[u"tag"]    = self._DomGetTag(DomElement)
        result[u"member"] = self._DomGetMember(DomElement)
        return result

    def _DomParseChangeset(self, DomElement):
        """ Returns ChangesetData for the changeset. """
        result = self._DomGetAttributes(DomElement)
        result[u"tag"] = self._DomGetTag(DomElement)
        return result

    #######################################################################
    # Internal xml builder                                                #
    #######################################################################

    def _XmlBuild(self, ElementType, ElementData):

        xml  = u""
        xml += u"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        xml += u"<osm version=\"0.6\" generator=\"" + self._created_by + "\">\n"

        # <element attr="val">
        xml += u"  <" + ElementType
        if u"id" in ElementData:
            xml += u" id=\"" + str(ElementData[u"id"]) + u"\""        
        if u"lat" in ElementData:
            xml += u" lat=\"" + str(ElementData[u"lat"]) + u"\""        
        if u"lon" in ElementData:
            xml += u" lon=\"" + str(ElementData[u"lon"]) + u"\""
        if u"version" in ElementData:
            xml += u" version=\"" + str(ElementData[u"version"]) + u"\""
        xml += u" visible=\"" + str(ElementData.get(u"visible", True)).lower() + u"\""
        if ElementType in [u"node", u"way", u"relation"]:
            xml += u" changeset=\"" + str(self._CurrentChangesetId) + u"\""
        xml += u">\n"

        # <tag... />
        for k, v in ElementData.get(u"tag", {}).items():
            xml += u"    <tag k=\""+self._XmlEncode(k)+u"\" v=\""+self._XmlEncode(v)+u"\"/>\n"

        # <member... />
        for member in ElementData.get(u"member", []):
            xml += u"    <member type=\""+member[u"type"]+"\" ref=\""+str(member[u"ref"])+u"\" role=\""+self._XmlEncode(member[u"role"])+"\"/>\n"

        # <nd... />
        for ref in ElementData.get(u"nd", []):
            xml += u"    <nd ref=\""+str(ref)+u"\"/>\n"

        # </element>
        xml += u"  </" + ElementType + u">\n"
        
        xml += u"</osm>\n"

        return xml

    def _XmlEncode(self, text):
        return text.replace("&", "&amp;").replace("\"", "&quot;")

## End of main class                                                     ##
###########################################################################


###########################################################################
## Reverting tools : DO NOT USE SINCE IT'S INT DEV                       ##

def RevertAnalyse(self, ChangesetId):
    """ Returns a string saying if changes are revertable or not. """
    
    data   = self.ChangesetDownload(ChangesetId)
    result = u""
    
    for elem in data:
                
        if elem[u"type"] == u"node":
            hist = self.NodeHistory(elem[u"data"][u"id"])
            elem_latest = hist[sorted(hist.keys())[-1]]
        elif elem[u"type"] == u"way":
            hist = self.WayHistory(elem[u"data"][u"id"])
            elem_latest = hist[sorted(hist.keys())[-1]]
        elif elem[u"type"] == u"relation":
            hist = self.RelationHistory(elem[u"data"][u"id"])
            elem_latest = hist[sorted(hist.keys())[-1]]
        
        result += elem[u"action"]           + u" " * (10-len(elem[u"action"]))
        result += elem[u"type"]             + u" " * (10-len(elem[u"type"]))
        result += str(elem[u"data"][u"id"]) + u" " * (10-len(str(elem[u"data"][u"id"])))
        result +=  u" => "
        if elem[u"data"][u"version"] == elem_latest[u"version"]:
            result += u"revertable\n"
        else:
            result += u"not revertable\n"
            
    return result.strip()

def RevertIfPossible(self, ChangesetId):
    """ Revert all changes if possible. """
    return

#z = OsmApi()
#print RevertAnalyse(z, 912290)

## End of reverting tools                                                ##
###########################################################################
