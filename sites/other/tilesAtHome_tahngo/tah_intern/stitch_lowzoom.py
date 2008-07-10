import os,sys, cairo, tempfile, shutil, StringIO, struct
# we need to insert the basedir to the python path (strip 2 path components) if we want to directly execute this file
sys.path.insert(0, os.path.dirname(os.path.dirname(sys.path[0])))
os.environ['DJANGO_SETTINGS_MODULE'] = "tah.settings"
from time import time
from tah.tah_intern.Tileset import Tileset
from tah.tah_intern.Tile import Tile
from tah.tah_intern.models import Settings,Layer

class Lowzoom(Tileset):
  def serve_tile(self,layername,z,x,y):
    z=int(z)
    x=int(x)
    y=int(y)
    t=Tile(layername,z,x,y)
    (layer, base_z,base_x,base_y) = t.basetileset() 
    # basetileset returns (None,None,None,None) if invalid!
    # basetilepath could be take from the Settings, hardcode for efficiency
    basetilepath='/mnt/agami/openstreetmap/tah/Tiles'
    tilesetfile = os.path.join(basetilepath,layername+'_'+str(base_z),str(base_x)+'_'+str(base_y))
    try:
      f = open(tilesetfile,'rb')
      #calculate file offset
      offset = 8 # skip the header data
      i_step = 4 # bytes per index entry
      z_pow = pow(2,z - base_z) # cache pow^1 of how many z levels away from base_z 
      #skip (1,4,16,...) tiles foe next zoom levels
      for cur_z in range (0,z-base_z): offset += i_step * pow(4,cur_z)
      #skip to corresponding y-row
      offset += i_step * (y - base_y*z_pow) * z_pow
      #skip to corresponding x tile
      offset += i_step * (x - base_x*z_pow)
      f.seek(offset)
      data = f.read(8)
      (d_offset,d_offset_next) = struct.unpack('II',data)
      #return  "(%d,%d,%d) as %d offset1 %d  offset2 %d " % (z,x,y,offset,d_offset,d_offset_next)
    except IOError:
      d_offset = 0

    if d_offset > 3:
      f.seek(d_offset)
      data = f.read(d_offset_next-d_offset)
    else:
      # return blankness value
      blankpng = ['unknown.png','sea.png','land.png','transparent.png']
      f_png = open(os.path.join(basetilepath,blankpng[d_offset]),'rb')
      data = f_png.read()
      f_png.close()
    try: f.close()
    except: pass
    return data


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
        if z == self.base_z+5: imagefile = StringIO.StringIO(self.serve_tile(layer.name,z+1,2*x+i,2*y+j))
        else: imagefile = os.path.join(self.tmpdir,"%d_%d_%d.png" % (z+1,2*x+i,2*y+j))
        try:
          image = cairo.ImageSurface.create_from_png(imagefile)
        except IOError, e:
          print "IOError %s at %d %d" % (e,2*x+i,2*y+j)
          image = cairo.ImageSurface.create_from_png(StringIO.StringIO(self.serve_tile('tile',100,0,0)))
        ctx.set_source_surface(image,256*i,256*j)
	#ctx.set_operator(cairo.CAIRO_OPERATOR_SOURCE)
        ctx.paint()
        #image.destroy()
    im.write_to_png(pngfilepath)
    t= Tile(layer,z,x,y)
    self.add_tile(t,pngfilepath)

if __name__ == '__main__':
  base_tile_path = Settings().getSetting(name='base_tile_path')
  layer=Layer.objects.get(name='tile')
  lz = Lowzoom()
  for x in range(8,64):
    for y in range(0,64):
       now = time()
       print "do 6 %d %d" % (x,y)
       lz.create(layer,6,x,y,base_tile_path)
       print "Took %.1f sec." % (time()-now)
  print "do 0 0 0"
  lz.create(layer,0,0,0,base_tile_path)
