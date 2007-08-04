import xml.sax
import OSM

(EMPTY, ROOT, NODE, SEGMENT, WAY) = range(5)

class OSMReader(xml.sax.handler.ContentHandler):
    def __init__(self):
        self.state = EMPTY
        self.obj = None
        self.nodeHandler = None
        self.segmentHandler = None
        self.wayHandler = None
        self.tagHandler = None

    def run(self, f):
        self.state = EMPTY
        self.obj = None
        parser = xml.sax.make_parser()
        parser.setContentHandler(self)
        parser.parse(f)
        
    def startElement(self, name, attrs):
        if name == "node":
            if self.state != ROOT:
                raise Exception("Got node in %d state" % self.state)
            self.state = NODE
            node_id = int(attrs["id"])
            lat = float(attrs["lat"])
            lon = float(attrs["lon"])
            timestamp = attrs.get("timestamp", None)
            self.obj = OSM.Node(node_id, lat, lon, timestamp)
        elif name == "tag":
            if self.obj is None:
                raise Exception("Got tag when not inside any object")
            k = attrs.get("k")
            v = attrs.get("v")
            self.obj.tags[k] = v
            if self.tagHandler:
                self.tagHandler(k, v)
        elif name == "segment":
            if self.state != ROOT:
                raise Exception("Got segment in %d state" % self.state)
            self.state = SEGMENT
            self.obj = OSM.Segment(attrs)
        elif name == "way":
            if self.state != ROOT:
                raise Exception("Got way in %d state" % self.state)
            self.state = WAY
            self.obj = OSM.Way(attrs)
        elif name == "seg":
            if self.state != WAY:
                raise Exception("Got seg in state %d!" % self.state)
            self.obj.segs.append(int(attrs["id"]))
        elif name == "osm":
            if self.state != EMPTY:
                raise Exception("Got osm in %d state" % self.state)
            self.state = ROOT
        
    def endElement(self, name):
        if name == "node":
            if self.state != NODE:
                raise Exception("Got /node in state %d!" % self.state)
            if self.nodeHandler:
                self.nodeHandler(self.obj)
            self.obj = None
            self.state = ROOT
        elif name == "segment":
            if self.state != SEGMENT:
                raise Exception("Got /segment in state %d!" % self.state)
            if self.segmentHandler:
                self.segmentHandler(self.obj)
            self.obj = None
            self.state = ROOT
        elif name == "way":
            if self.state != WAY:
                raise Exception("Got /way in state %d!" % self.state)
            if self.wayHandler:
                self.wayHandler(self.obj)
            self.obj = None
            self.state = ROOT
        elif name == "osm":
            if self.state != ROOT:
                raise Exception("Got /osm in state %d!" % self.state)
            self.state = EMPTY
