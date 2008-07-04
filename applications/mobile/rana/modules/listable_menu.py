import cairo

class listable_menu:
  def __init__(self,cr,x,y,w,h, clickHandler):
    self.cr = cr
    self.x = x
    self.y = y
    self.w = w
    self.h = h
    self.numItems = 10
    self.dy = self.h / self.numItems
    self.clickHandler = clickHandler
  
  def write(self,pos,text):
    border = 4
    self.cr.set_source_rgb(0,0,0)
    self.cr.set_font_size(45)
    self.cr.move_to(
      self.x + border,
      self.y + (pos+1) * self.dy - border)
    self.cr.show_text(text)

  def makeClickable(self,pos,action):
    if(self.clickHandler != None):
      self.clickHandler.registerXYWH(self.x, pos * self.dy, self.w, self.dy, action)
