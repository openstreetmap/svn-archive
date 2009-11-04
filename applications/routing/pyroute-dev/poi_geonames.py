from poi_base import *
import feedparser
from xml.sax import make_parser, handler

class geonamesParser(handler.ContentHandler):
  def __init__(self, filename):
    self.entries = []
    parser = make_parser()
    parser.setContentHandler(self)
    parser.parse(filename)
  def startElement(self, name, attrs):
    if(name == "entry"):
      self.entry = {}
    self.text = ''
  def characters(self, text):
    self.text = self.text + text
  def endElement(self, name):
    if(name in ('title')):
      self.entry[name] = self.text
    if(name in ('lat','lng')):
      self.entry[name] = float(self.text)
    if(name == "entry"):
      self.entries.append(self.entry)
      
class geonames(poiModule):
    def __init__(self, modules):
        poiModule.__init__(self, modules)
        g = geonamesParser("data/wiki.xml")
        self.add("Wikipedia entries",g)

    def add(self, name, parser):
        group = poiGroup(name)
        count = 0
        for item in parser.entries:
            x = poi(item['lat'], item['lng'])
            x.title = item['title']
            group.items.append(x)
            count = count + 1
        print "Added %d items from %s" % (count, name)

        self.groups.append(group)

if __name__ == "__main__":
    g = geonamesParser("data/wiki.xml")
