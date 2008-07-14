import os,sys, cairo, tempfile, shutil, StringIO, re
# we need to insert the basedir to the python path (strip 2 path components) if we want to directly execute this file
sys.path.insert(0, os.path.dirname(os.path.dirname(sys.path[0])))
os.environ['DJANGO_SETTINGS_MODULE'] = "tah.settings"
from time import time
from tah.tah_intern.Tileset import Tileset
from tah.tah_intern.Tile import Tile
from tah.tah_intern.models import Settings,Layer

class Lowzoom(Tileset):

  def create(self,layer,z,x,y,base_tile_dir):
    self.__init__(layer, z, x, y)
    self.tmpdir=tempfile.mkdtemp()
    self.stitch(layer,z,x,y)
    self.save(base_tile_dir)
    shutil.rmtree(self.tmpdir)

  def stitch(self,layer,z,x,y):
    #recurse to all tiles of tileset
    if z == self.base_z+6: return
    for rec_x in range(2*x,2*x+2):
      for rec_y in range(2*y,2*y+2):
        self.stitch(layer,z+1,rec_x,rec_y)

    #print "tile %s %d %d %d " % (layer,z,x,y)
    pngfilepath = os.path.join(self.tmpdir,"%d_%d_%d.png" % (z,x,y))
    #print "write to %s" % pngfilepath
    im = cairo.ImageSurface(cairo.FORMAT_ARGB32, 256,256)
    ctx = cairo.Context(im)
    ctx.scale(0.5, 0.5)
    for i in range(0,2):
      for j in range(0,2):
        if z == self.base_z+5: imagefile = StringIO.StringIO(Tile(None,z+1,2*x+i,2*y+j).serve_tile('captionless'))
        else: imagefile = os.path.join(self.tmpdir,"%d_%d_%d.png" % (z+1,2*x+i,2*y+j))
        try:
          image = cairo.ImageSurface.create_from_png(imagefile)
        except IOError, e:
          print "IOError %s at %d %d" % (e,2*x+i,2*y+j)
          image = cairo.ImageSurface.create_from_png(StringIO.StringIO(Tile(None,0,0,1).serve_tile('tile')))
        ctx.set_source_surface(image,256*i,256*j)
	#ctx.set_operator(cairo.CAIRO_OPERATOR_SOURCE)
        ctx.paint()
        #image.destroy()
    im.write_to_png(pngfilepath)
    t= Tile(layer,z,x,y)
    self.add_tile(t,pngfilepath)

def find_old_lowzooms(base_tile_path):
  m = re.compile("\D+")
  old_lowzooms = {} # return a dict with (z,x,y) tuples that need an update
  lz_path = os.path.join(base_tile_path,'tile_6_0')
  now = time()
  for i in range (0,5):
    path= os.path.join(base_tile_path,'tile_12_%d' % i)
    for f in os.listdir(path):
      mtime = os.stat(os.path.join(path,f)).st_mtime
      age = now - mtime
      #if newer than 2 days
      if age < 87846:
        [x,y] = tuple(m.split(f))
        x,y = int(x)>>6,int(y)>>6
        try: 
          lz_file = os.path.join(lz_path,"%d_%d" % (x,y))
          lz_mtime = os.stat(os.path.join(lz_path,lz_file)).st_mtime
        except:
          #lowzoom file didn't exist yet. create.
          old_lowzooms["6_%d_%d"%(x,y)]=(6,x,y)        

        if mtime > lz_mtime:
          #print "New tiles in old lowzoom (%d %d ) found" % (x,y)
          old_lowzooms["6_%d_%d"%(x,y)]=(6,x,y)        


  #finally always dd (0,0,0)
  old_lowzooms['0_0_0']=(0,0,0)
  return old_lowzooms

if __name__ == '__main__':
  base_tile_path = Settings().getSetting(name='base_tile_path')
  layer=Layer.objects.get(name='tile')
  lz = Lowzoom()
  for (z,x,y) in find_old_lowzooms(base_tile_path).values():
    now = time()
    print "do %d %d %d" % (z,x,y)
    lz.create(layer,6,x,y,base_tile_path)
    print "Took %.1f sec." % (time()-now)
