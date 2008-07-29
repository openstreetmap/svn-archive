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
    im = Image.new('RGBA', (256,256))

    img_caption_file = StringIO.StringIO(Tile(None,z,x,y).serve_tile('caption'))
    img_caption = Image.open(img_caption_file).convert('RGBA')

    for i in range(0,2):
      for j in range(0,2):
        if z == self.base_z+5: 
          imagefile = StringIO.StringIO(Tile(None,z+1,2*x+i,2*y+j).serve_tile('captionless'))
        else: imagefile = os.path.join(self.tmpdir,"%d_%d_%d.png_nocaptions" % (z+1,2*x+i,2*y+j))
        try:
          image = Image.open(imagefile).convert('RGBA')
        except IOError, e:
          #fall back to tile image
          print "Try %d: %s at (%d,%d,%d). " % (retries,e,z+1,2*x+i,2*y+j)          
          image = Image.open(StringIO.StringIO(Tile(None,z+1,2*x+i,2*y+j).serve_tile('tile')))
          try:
            image = Image.open(imagefile).convert('RGBA')
          except IOError, e:
            #fall back to unknown image
            print "Try %d: %s at (%d,%d,%d). " % (retries,e,z+1,2*x+i,2*y+j)
            image = Image.open(StringIO.StringIO(Tile(None,0,0,1).serve_tile('tile')))


        if z==self.base_z+5:
          enh = ImageEnhance.Contrast(image)
          image = enh.enhance(1.4)
        enh = ImageEnhance.Sharpness(image)
        image = enh.enhance(0.2)
        ##image2 = image.convert('RGBA').filter(ImageFilter.BLUR)
        image = image.resize((128, 128))
        im.paste(image, (128*i,128*j,128*(i+1),128*(j+1)))

        del (image)
	del (imagefile)

    im.save(pngfilepath+'_nocaptions', "PNG")
    #im = ImageChops.multiply(im, img_caption)
    #(r,g,b,a) = img_caption.split()
    img_mask = img_caption.convert('1')
    #img_mask = ImageChops.invert(Image.merge('RGB',(r,g,b)).convert('L'))
    #im = ImageChops.composite(img_caption, im, img_mask)
    del (img_caption)
    del (img_caption_file)
    im.save(pngfilepath, "PNG")
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
  for i,(z,x,y) in enumerate(old_lowzooms.values()):
  #for i,(z,x,y) in enumerate([(6,2,1),(5,34,18)]):
    print "%i out of %i) sleep 10 seconds then do %d %d %d" % (i,n, z,x,y)
    #time.sleep(10)
    now = time.time()
    lz.create(layer,z,x,y,base_tile_path)
    print "(%d,%d,%d)Took %.1f sec." % (z,x,y,time.time()-now)
  #Finally d
  #now = time.time()
  #lz.create(layer,z,x,y,base_tile_path)
  #print "(0,0,0) took %.1f sec." % (time.time()-now)
