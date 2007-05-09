#!/usr/bin/python

import xml.sax, psycopg2, StringIO, sys, time, datetime

class CopyBuffer (StringIO.StringIO):
    def read (self, n):
        n = min(n,len(self.buf))
        data = self.buf[:n]
        self.buf = self.buf[n:]
        return data.encode("utf-8")

    def readlines (self):
        n = self.buf.index("\n")+1
        return self.read(n)

class planetOsmHandler (xml.sax.handler.ContentHandler):
    txnSize = 20000
    queueColumns = {
        "node"          : ("id", "lat", "lon", "user_id",
                           "visible", "tags", "timestamp", "geom"),
        "segment"       : ("id", "from", "to", "user_id",
                           "visible", "tags", "timestamp", "bbox"),
        "way"           : ("id","user_id","timestamp","visible","bbox"),
        "way_segment"   : ("id", "segment_id","sequence_id"),
        "way_tag"       : ("id", "k", "v")
    };

    def __init__ (self, db):
        xml.sax.handler.ContentHandler.__init__(self)
        self.current = None
        self.db = db
        self.count = self.total = 0
        self.start = time.time()
        self.resetQueue()

    def resetQueue (self):
        self.queue = {}
        for table in self.queueColumns.keys():
            self.queue[table] = []
        if self.count:
            self.total += self.count
            print >>sys.stderr, "%d (%.1f/s)" % (
                    self.total, self.total/(time.time()- self.start))
        self.count = 0

    def startElement (self, name, attrs):
        if name == "osm":
            return
        elif name == "tag":
	    k = self.sanitize_tag(attrs["k"])
	    v = self.sanitize_tag(attrs["v"])
            self.current["tags"][k] = v
            if self.current["table"] == "way":
                self.queue["way_tag"].append({
                                        "table": "way_tag", 
                                        "id": self.current["attrs"]["id"],
                                        "k": k, "v": v})
                self.count += 1
        elif name == "seg":
            self.queue["way_segment"].append({ 
                                        "table": "way_segment", 
                                        "id": self.current["attrs"]["id"], 
                                        "segment_id": attrs["id"] })
            self.count += 1
        else:
            self.current = {"table": name, "attrs": attrs, "tags":{}}
            if name == "way": self.current["segs"] = []
            self.count += 1
    
    def endElement (self, name):
        if name in ("tag","seg"): return
        if name == "osm":
            self.flushQueue()
        else:
            table = self.current["table"]
            self.queue[table].append(self.current)
            self.current = None
            if self.count >= self.txnSize: self.flushQueue()

    def sanitize_tag (self, tags):
        try:
            tagstr = tags.decode("latin-1")
        except:
            tagstr = tags
        tagstr = tagstr.encode("utf-8").encode("string-escape")
	return tagstr

    def flushQueue (self):
        cursor = self.db.cursor()
        for table, columns in self.queueColumns.items():
            io = CopyBuffer()
            for item in self.queue[table]:
                vals = {"user_id": "0", "visible": "t", "geom": "***NULL***",
                                    "sequence_id": "0", "bbox": "***NULL***"}
                vals.update(item)
                if "attrs" in item: vals.update(item["attrs"])
                if "tags" in item:
		    tags = [("%s=%s" % tag) for tag in item["tags"].items()]
	            vals["tags"] = ";".join(tags)
                if "timestamp" in columns and "timestamp" not in vals:
                    vals["timestamp"] = str(datetime.datetime.now())
                try:
		    record = "\t".join([vals[col] for col in columns])
		    #print >>sys.stderr, record
                    io.write(record + "\n")
                except KeyError, E:
                    print >>sys.stderr, "%s %s missing key: %s" % (
                                    vals["table"], vals["id"], str(E))
            if io.getvalue():
                cursor.copy_from(io, "current_"+table+"s", "\t", "***NULL***")
        cursor.close()
        self.db.commit()
        self.resetQueue()

if __name__ == "__main__":
    import sys
    db = psycopg2.connect("dbname=" + sys.argv[1])
    xml.sax.parse(sys.stdin, planetOsmHandler(db = db))
