
import os,sys, tempfile, shutil, StringIO, re
import Image, ImageEnhance, ImageFilter, ImageChops
# we need to insert the basedir to the python path (strip 2 path components) if we want to directly execute this file
sys.path.insert(0, os.path.dirname(os.path.dirname(sys.path[0])))
os.environ['DJANGO_SETTINGS_MODULE'] = "tah.settings"
import time
from datetime import datetime, timedelta
from tah.tah_intern.Tileset import Tileset
from tah.tah_intern.Tile import Tile
from tah.tah_intern.models import Settings,Layer
from tah.requests.models import Request

class Lowzoom(Tileset):

  def create(self,layer,z,x,y,base_tile_dir):
    self.__init__(layer, z, x, y)
    self.tmpdir=tempfile.mkdtemp()
    self.stitch(layer,z,x,y)
    print "saving to %s" % base_tile_dir
    self.save(base_tile_dir)
    shutil.rmtree(self.tmpdir)

  def stitch(self,layer,z,x,y):
    #recurse to all tiles of tileset
    if z == self.base_z+6: return
    for rec_x in range(2*x,2*x+2):
      for rec_y in range(2*y,2*y+2):
        self.stitch(layer,z+1,rec_x,rec_y)

    # temporarily insert sleeps to no stress the NFS disk so much
    #if ((x+y) % 10 == 0): time.sleep(5)
    #print "tile %s %d %d %d " % (layer,z,x,y)
    pngfilepath = os.path.join(self.tmpdir,"%d_%d_%d.png" % (z,x,y))
    im = Image.new('RGB', (512,512))

    img_caption_file = StringIO.StringIO(Tile(None,z,x,y).serve_tile('caption'))
    img_caption = Image.open(img_caption_file)

    for i in range(0,2):
      for j in range(0,2):
        if z == self.base_z+5: 
          imagefile = StringIO.StringIO(Tile(None,z+1,2*x+i,2*y+j).serve_tile('captionless'))
        else: imagefile = os.path.join(self.tmpdir,"%d_%d_%d.png_nocaptions" % (z+1,2*x+i,2*y+j))
        try:
          image = Image.open(imagefile).convert('RGB')
        except IOError, e:
          #fall back to tile image
          print "Try %d: %s at (%d,%d,%d). " % (retries,e,z+1,2*x+i,2*y+j)          
          image = Image.open(StringIO.StringIO(Tile(None,z+1,2*x+i,2*y+j).serve_tile('tile')))
          image = Image.open(imagefile)

        im.paste(image, (256*i,256*j))
        del (image)
	del (imagefile)
        #end of loop

    if (z-self.base_z) in [4,2]:
      im = im.filter(ImageFilter.MinFilter(3))

    im = im.resize((256, 256),Image.ANTIALIAS)
    im.save(pngfilepath+'_nocaptions', "PNG")

    if img_caption.mode == 'RGB':
      #if for some strange reasons the caption is not recognized as RGBA, make anything transparent that has some color.
      cb = img_caption.split()
      a, = img_caption.convert('L').split()
      a = a.point(lambda p: 255 * int(p < 230))
      img_caption = Image.merge('RGBA',cb+(a,))
    elif img_caption.mode == 'RGBA':
      (r,g,b,a) = img_caption.split()
    elif img_caption.mode == 'L':
      img_caption.load() #<--- workaround for bug in PIL 1.1.5
      a = img_caption.point(lambda p: 255 * int(p < 230))
    else:
      sys.exit("unknown image mode %s at (%d,%d,%d)" %(img_caption.mode,z,x,y))
    im.paste(img_caption,(0,0),a)
    del (img_caption)
    del (img_caption_file)
    im.convert('P', palette=Image.ADAPTIVE).save(pngfilepath, "PNG")
    t= Tile(layer,z,x,y)
    self.add_tile(t,pngfilepath)

def find_old_lowzooms(base_tile_path):
  old_lowzooms = {} # return a dict with (z,x,y) tuples that need an update
  lz_path = os.path.join(base_tile_path,'tile_6')
  reqs = Request.objects.filter(status=2,min_z=12)
  now = datetime.now()
  for r in reqs:
    #should all be newer than 2 days
    if r.clientping_time + timedelta(2) >= now:
      x,y = r.x>>6, r.y>>6
      try: 
          lz_file = os.path.join(lz_path,"%04d"%x,"%d_%d" % (x,y))
          lz_mtime = os.stat(os.path.join(lz_path,lz_file)).st_mtime
          if r.clientping_time > datetime.fromtimestamp(lz_mtime):
            #print "New tiles in old lowzoom (%d %d ) found" % (x,y)
            old_lowzooms["6_%d_%d"%(x,y)]=(6,x,y)        
      except:
          #lowzoom file didn't exist yet. create.
          old_lowzooms["6_%d_%d"%(x,y)]=(6,x,y)        


  #print "requesting %d lowzooms." % len(old_lowzooms)
  return old_lowzooms

if __name__ == '__main__':
  base_tile_path = Settings().getSetting(name='base_tile_path')
  layer=Layer.objects.get(name='tile')
  lz = Lowzoom()
  old_lowzooms = find_old_lowzooms(base_tile_path)
  n = len(old_lowzooms)
  #for i,(z,x,y) in enumerate(old_lowzooms.values()):
  for i,(z,x,y) in enumerate([(6,34,18)]):   #for doing one test tileset
  #i,z =0,6   #for doing the whole world...
  #for x in range(0,64):
  # for y in range(0,64):
  #  i += 1
    print "%i out of %i) sleep 10 seconds then do %d %d %d" % (i,n, z,x,y)
    #time.sleep(10)
    now = time.time()
    lz.create(layer,z,x,y,base_tile_path)
    print "(%d,%d,%d)Took %.1f sec." % (z,x,y,time.time()-now)
  #Finally d
  #now = time.time()
  #lz.create(layer,z,x,y,base_tile_path)
  #print "(0,0,0) took %.1f sec." % (time.time()-now)
