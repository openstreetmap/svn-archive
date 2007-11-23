import cairo
import os
class menuIcons:
    def __init__(self):
        self.images = {}
        self.cantLoad = []
    def load(self,name):
        filename = "icons/bitmap/%s.png" % name
        if(not os.path.exists(filename)):
            return(0)
        self.images[name] = cairo.ImageSurface.create_from_png(filename)
        if(self.images[name]):
            return(1)
        return(0)
    def draw(self,cr,name,x,y,w,h):
        if not name in self.images.keys():
            if(name in self.cantLoad):
                return
            if(not self.load(name)):
                self.cantLoad.append(name)
                return
        imagesize = 120.0
        cr.save()
        cr.translate(x,y)
        cr.scale(w / imagesize, h / imagesize)
        cr.set_source_surface(self.images[name],0,0)
        cr.paint()
        cr.restore()
    