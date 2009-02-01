from xml.sax.handler import ContentHandler
import sys, re, xml.sax

try:
    from xml.etree.cElementTree import Element, SubElement, tostring
except:
    sys.path.append("..")
    from third.ElementTree import Element, SubElement, tostring

try:
    import httplib2
except ImportError, E:
    try:
        from third import httplib2
    except ImportError, E2:
        httplib2 = False
        httplib2_error = "%s/%s" % (E, E2)

def indent(elem, level=0):
    """Used for pretty printing XML."""
    i = "\n" + level*"  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        for e in elem:
            indent(e, level+1)
            if not e.tail or not e.tail.strip():
                e.tail = i + "  "
        if not e.tail or not e.tail.strip():
            e.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i


class OSMObj:
    """
    >>> o = osmparser.OSMObj(type='node')
    >>> o.loc = (-5, -5)
    >>> o.tags['created_by'] = 'osmparser'
    >>> o.toxml()
    '<osm version="0.5">\\n  <node lat="-5" lon="-5">\\n    <tag k="created_by" v="osmparser" />\\n  </node>\\n</osm>'
    >>> o.save(username, password)
    '339668244'
    >>> o.id
    339668244
    >>> o.delete()
    Traceback (most recent call last):
      File "<stdin>", line 1, in <module>
    TypeError: delete() takes exactly 3 arguments (1 given)
    >>> o.delete(username, password)
    ' '
    >>> o.id
    >>> o.delete(username, password)
    Traceback (most recent call last):
      File "<stdin>", line 1, in <module>
      File "lib/osmparser.py", line 162, in delete
        raise Exception("Can't delete object with no id")
    Exception: Can't delete object with no id
    """
    type = None
    id = None
    user = None
    timestamp = None
    
    loc = None

    tags = None

    nodes = None
    members = None

    def __init__(self, id=None, type=None, site_url="http://openstreetmap.org"):
        self.id = id
        self.tags = {}
        if type:
            self.setType(type)
        self.site_url = site_url

    def setType(self, type):
        self.type = type
        if self.type == "way":
            self.nodes = []
        elif self.type == "relation":
            self.members = []
        elif self.type == "node":
            self.loc = (-181, -91)
    
    def display(self):
        d = str(self)
        if 'name' in self.tags:
            d = "%s (%s)" % (self.tags['name'], d)
        return d

    def api_url(self):
        id = self.id or "create"
        return "%s/api/0.5/%s/%s" % (self.site_url, self.type, id)
    
    def local_url(self):
        return "/%s/%s/" % (self.type, self.id)
    
    def user_link(self):
        if self.user:
            return "%s/user/%s" % (self.site_url, self.user)
        else: 
            return ""    
    
    def browse_url(self):
        return "%s/browse/%s/%s" % (self.site_url, self.type, self.id)
    
    def __str__(self):
        return "%s %s" % (self.type.title(), self.id)

    def __repr__(self):
        start =  "<OSM %s %s" % (self.type.title(), self.id) 
        middle = ""
        if self.nodes:
            middle = "%s nodes" % len(self.nodes)
        elif self.members:
            middle = "%s members" % (len(self.members))
        end = ">"
        return " ".join(filter(None, (start, middle, end)))

    def toxml(self, as_string=True, parent=None):
        if parent == None:
            parent = Element("osm", {"version": "0.5"})
        if self.type == "node":
            element = SubElement(parent, "node", {
                'lon': str(self.loc[0]), 
                'lat': str(self.loc[1])})
        elif self.type == "way":
            element = SubElement(parent, 'way')
            for n in self.nodes:
                id = None
                if isinstance(n, int):
                    id = n
                else:
                    id = n.id
                id = str(id)    
                SubElement(element, "nd", {'ref': id})
        elif self.type == "relation":
            element = SubElement(parent, 'relation')
            for m in self.members:
                id = None
                if isinstance(m['ref'], int):
                    id = m['ref']
                else:
                    id = m['ref'].id
                id = str(id)    
                SubElement(element, "member", {
                    'ref': id,
                    'type': m['type'],
                    'role': m['role']
                })    
        
        if self.id:
            element.attrib['id'] = str(self.id)

        keys = self.tags.keys()
        keys.sort()
        

        for key in keys:
            SubElement(element, "tag", {'k': key, 'v': self.tags[key]})
        
        indent(parent)
        if as_string:  
            return tostring(parent)
        else:
            return parent

    def save(self, username, password):
        if not httplib2:
            raise Exception("Couldn't import httplib2: %s" % httplib2_error)
        url = self.api_url()
        h = httplib2.Http()
        h.add_credentials(username, password)
        xml = self.toxml()    
        (resp, content) = h.request(url, "PUT", body=xml)
        if int(resp.status) != 200:
            raise Exception("Status was: %s, Content: %s" % (resp.status, content))
        if not self.id:
            self.id = int(content)
        return content
    def delete(self, username, password):
        if not httplib2:
            raise Exception("Couldn't import httplib2: %s" % httplib2_error)
        if not self.id:
            raise Exception("Can't delete object with no id")
        url = self.api_url()
        h = httplib2.Http()
        h.add_credentials(username, password)
        xml = self.toxml()    
        (resp, content) = h.request(url, "DELETE")
        if int(resp.status) != 200:
            raise Exception("Status was: %s, Content: %s" % (resp.status, content))
        self.id = None
        return content

def rearrange(output):
    new_output = {
        'nodes': {},
        'ways': {},
        'relations': {}
    }
    
    for i in output['nodes']:
        new_output['nodes'][i.id] = i
    
    for i in output['ways']:
        nodes = []
        for n in i.nodes:
            nodes.append(new_output['nodes'][n])
        i.nodes = nodes
        new_output['ways'][i.id] = i
    
    for i in output['relations']:
        members = []
        for m in i.members:
            o = new_output["%ss" % m['type']][m['ref']] 
            members.append({'type':m['type'], 'ref': o, 'role': m['role']})
        i.members = members 
        new_output['relations'][i.id] = i
    
    return new_output

class ParseObjects(ContentHandler):
    def __init__ (self, site_url="http://openstreetmap.org"):
        self.site_url = site_url
        self.output = {
           'nodes': [],
           'ways': [],
           'relations': []
        }   
       
    
    def startElement (self, name, attr):
         """Handle creating the self.current node, and pulling tags/nd refs."""
         
         if name in ['node', 'way', 'relation']:
            self.current = OSMObj(int(attr['id']), name, site_url=self.site_url)
            if 'user' in attr:
                self.current.user = attr['user']
            if 'timestamp' in attr:
                self.current.timestamp = attr['timestamp']
         
         elif name =='nd' and self.current:
             self.current.nodes.append(int(attr["ref"]))
         
         elif name == 'tag' and self.current:
             self.current.tags[attr['k']] = attr['v']
         
         elif name == "member":
            self.current.members.append({
                'type': attr['type'],
                'ref': int(attr['ref']),
                'role': attr['role']
             })   
         
         if name == "node":
            self.current.loc = (attr['lon'], attr['lat'])
         
    def endElement (self, name):
        """Switch on node type, and serialize to XML for upload or print."""
        if name in ['way', 'node','relation']:
            self.output['%ss' % name].append(self.current)

def parse(f, arrange=True, site_url = "http://openstreetmap.org"):
    parser = ParseObjects(site_url=site_url)
    xml.sax.parse( f, parser )          
    output = parser.output
    if arrange:
        try:
            output = rearrange(output)
        except:
            pass
    return output 

def parseString(data, arrange=True, site_url = "http://openstreetmap.org"):
    parser = ParseObjects(site_url=site_url)
    xml.sax.parseString( data, parser  )          
    output = parser.output
    if arrange:
        try:
            output = rearrange(output)
        except:
            pass
    return output 

if __name__ == "__main__": 
    f = sys.stdin
    parser = ParseObjects()
    xml.sax.parse( f, parser )           
    import pprint
    pprint.pprint(rearrange(parser.output))
