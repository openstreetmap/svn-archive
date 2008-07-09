class Tile:
  x=None
  y=None
  z=None
  layer=None
  blankness=None

  def __init__(self, layer, z, x, y):
    self.layer=layer
    self.z=int(z)
    self.x=int(x)
    self.y=int(y)
    self.layer = layer

  def is_blank(self):
    #blankness=1 sea, blankness=2 land
    return self.blankness

  def set_blank(self,b):
    self.blankness = b

  def set_blank_sea(self):
    #blankness=1 sea, 2:land. 3:transparent
    if self.layer.transparent: self.blankness=3
    else: self.blankness=1

  def set_blank_land(self):
    #blankness=1 sea, 2:land. 3:transparent
    if self.layer.transparent: self.blankness=3
    else: self.blankness=2

  def is_valid(self):
    if self.layer == None: return 0
    if self.z < 0 or self.z > 17: return 0
    if self.x < 0 or self.y < 0: return 0
    if self.x >= pow(2,self.z) or self.y >= pow(2,self.z): return 0 
    return 1

  def basetileset(self):
    # return None tuple, if the tile is wrong
    if not self.is_valid(): return (None, None, None, None)
    if self.z >= 6:
      if self.z >= 12: base_z = 12
      else: base_z = 6
    else: base_z = 0
    base_x = self.x // pow(2,(self.z-base_z))
    base_y = self.y // pow(2,(self.z-base_z))
    return (self.layer, base_z, base_x, base_y)