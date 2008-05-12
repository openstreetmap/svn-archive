import sys
sys.path.unshift("..")
from OsmRender import OsmRenderBase

class SimpleOsmRender(OsmRenderBase):
  def imageBackgroundColour(self):
    return("white")
    
  def todo_draw(self):
    # Draw ways
    for w in self.osm.ways:
      # TODO: stuff
      last = (0,0,False)
      for n in w['n']:
        (lat,lon) = self.osm.nodes[n]
        (x,y) = self.proj.project(lat,lon)
        if(last[2]):
          self.drawContext.line((last[0], last[1], x, y), fill=128)
        last = (x,y,True)
    
    # Draw POI
    for poi in self.osm.poi:
      n = poi['id']
      (lat,lon) = self.osm.nodes[n]
      (x,y) = self.proj.project(lat,lon)
      s = 1
      self.drawContext.rectangle((x-s,y-s,x+s,y+s),fill='yellow')

if(__name__ == '__main__'):
  # Test suite: render a tile in hersham, and save it to a file
  a = SimpleOsmRender()
  a.RenderTile(17,65385,43658, "sample_simplerenderer.png")
