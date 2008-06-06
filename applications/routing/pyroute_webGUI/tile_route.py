from tile_base import TileBase

class TileRoute(TileBase):
  def __init__(self):
    pass
  def draw(self):
    self.ctx.set_source_rgb(0, 0, 0)
    #(x,y) = self.proj.project(lat,lon)
    self.ctx.move_to(50,50)
    self.ctx.line_to(150,150)
    self.ctx.stroke()
