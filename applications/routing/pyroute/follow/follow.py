import sys
from xml.sax import make_parser, handler
from time import *

class LoadTrack(handler.ContentHandler):
  def __init__(self, filename):
    self.inTrkpt = 0
    self.inTime = 0
    self.samples = []
    parser = make_parser()
    parser.setContentHandler(self)
    parser.parse(filename)
  
  def startElement(self, name, attrs):
    """Handle XML elements"""
    if name == "trkpt":
      self.tags = { \
        'lat':float(attrs.get('lat')), 
        'lon':float(attrs.get('lon'))}
      self.inTrkpt = 1
    if name == "time":
      if self.inTrkpt:
        self.inTime = 1
        self.timeStr = ''
      
  def characters(self, content):
    if(self.inTime):
      self.timeStr = self.timeStr + content
  
  def endElement(self, name):
    if name == 'time':
      if(self.inTime):
        self.inTime = 0
        self.tags['t'] = mktime(strptime(self.timeStr[0:-1], "%Y-%m-%dT%H:%M:%S"))
    if name == 'trkpt':
      self.samples.append(self.tags)
      self.inTrkpt = 0
    
  def play(self, filename):
    for pos in self.samples:
      file = open(filename, 'w')
      file.write("%f,%f\n" % (pos['lat'],pos['lon']))
      file.close
      sleep(0.5)
      
if __name__ == "__main__":
  track = LoadTrack(sys.argv[1])
  track.play(sys.argv[2])
