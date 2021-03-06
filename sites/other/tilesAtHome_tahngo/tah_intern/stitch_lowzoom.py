""" call this with arguments 'z x y' or with none to stitch 
    all recently modified tilesets.
"""
import os,sys, tempfile, shutil, StringIO, re
import Image, ImageFilter, ImageChops
script_path = sys.path[0]
# we need to insert the basedir to the python path (strip 2 path components) if we want to directly execute this file
sys.path.insert(0, os.path.dirname(os.path.dirname(sys.path[0])))
if os.environ.get('DJANGO_SETTINGS_MODULE', None) == None: os.environ['DJANGO_SETTINGS_MODULE'] = "tah.settings"
import time
from datetime import datetime, timedelta
from tah.tah_intern.Tileset import Tileset
from tah.tah_intern.Tile import Tile
from tah.tah_intern.models import Layer
from tah.requests.models import Request
from django.db import settings

class Lowzoom(Tileset):

  def create(self,layer,z,x,y,base_tile_dir):
    self.__init__(layer, z, x, y)
    self.tmpdir=tempfile.mkdtemp()
    # set variables for captionless tileset
    self.layer_cless = Layer.objects.get(name='captionless')
    self.tset_cless=Tileset()

    try:
      try:
        self.stitch(layer,z,x,y)
        # save main layer (usually 'tile')
        self.save(base_tile_dir)
        # and save 'captionless' layer too
        self.tset_cless.save(base_tile_dir)
      finally:
        # even do cleanup when pressing CTRL-C (KeyboardInterrupt)
        shutil.rmtree(self.tmpdir)
    except KeyboardInterrupt:
      sys.exit("Pressed CTRL-C. Exiting.")

  def stitch(self,layer,z,x,y):
    #recurse to all tiles of tileset
    if z == self.base_z+6: return
    for rec_x in range(2*x,2*x+2):
      for rec_y in range(2*y,2*y+2):
        self.stitch(layer,z+1,rec_x,rec_y)

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
          print "Try: %s at (%d,%d,%d). " % (e,z+1,2*x+i,2*y+j)          
          image = Image.open(StringIO.StringIO(Tile(None,z+1,2*x+i,2*y+j).serve_tile('tile')))
          try: image = Image.open(imagefile)
          except IOError, e:
            print "Try: %s at (%d,%d,%d). " % (e,z+1,2*x+i,2*y+j)          
            image = Image.open(StringIO.StringIO(Tile(None,12,0,0).serve_tile('tile'))) #hacky way to get a sea tile

        im.paste(image, (256*i,256*j))
        del (image)
	del (imagefile)
        #end of loop

    if (z-self.base_z) in [4,2]:
      im = im.filter(ImageFilter.MinFilter(3))

    im = im.resize((256, 256),Image.ANTIALIAS)
    im.save(pngfilepath+'_nocaptions', "PNG")

    #If we are at base zoom, do save the captionless image 
    #to have it for further stitching
    if z == self.base_z:
      t= Tile(self.layer_cless,z,x,y)
      self.tset_cless.add_tile(t,pngfilepath+'_nocaptions')

    if img_caption.mode == 'RGB':
      # if for some strange reasons the caption is not recognized as RGBA, 
      # make it transparent.
      # our tiles have color (248,248,248) for transparency..
      # after talking with the PIL devs, this is a shortcoming of PIL
      # future versions >1.1.6 might return the transparent color like that:
      # im.info['transparency'] = (248, 248, 248)
      a = ImageChops.invert(img_caption.point(lambda p: 255 * int(p == 248)).convert('L'))
    elif img_caption.mode == 'RGBA':
      (r,g,b,a) = img_caption.split()
    elif img_caption.mode == 'L':
      img_caption.load() #<--- workaround for bug in PIL 1.1.5
      a = img_caption.point(lambda p: 255 * int(p < 230))
    elif img_caption.mode == 'P':
      #pallettized PNGs can be transparent, use that...
      (r,g,b,a) = img_caption.convert('RGBA').split()
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
  reqs = Request.objects.filter(status=2,min_z=12).iterator()
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

  return (len(old_lowzooms), old_lowzooms)


if __name__ == '__main__':
  base_tile_path = settings.TILES_ROOT
  starttime = time.time()
  if len(sys.argv) != 4:
    # find all modified tilesets
    n, old_lowzooms = find_old_lowzooms(base_tile_path)
    for i,(z,x,y) in enumerate(old_lowzooms.values()):
    #i,z =0,6   #for doing the whole world...
    #for x in range(0,64):
    # for y in range(0,64):
    #  i += 1
      sys.stdout.write ("%i out of %i: " % (i,n))
      sys.stdout.flush()
      failed = os.system("nice python %s/stitch_lowzoom.py %d %02d %20d" % (script_path,z,x,y))
      if failed: sys.exit("Child stitcher failed. Bailing out.")
    #Finally stitch the z0-5
    #now = time.time()
    #lz.create(layer,z,x,y,base_tile_path)
    #print "(0,0,0) took %.1f sec." % (time.time()-now)
    print ("Run took overall %f hours." % ((time.time()-starttime) / 3600.0))
  else:
    # called with z x y parameters, stitch specific tileset
    (z,x,y) = map(int, sys.argv[1:4])
    layer=Layer.objects.get(name='tile')
    sys.stdout.write ("Stitching %d %02d %02d " % (z,x,y))
    sys.stdout.flush()
    lz = Lowzoom()
    lz.create(layer,z,x,y,base_tile_path)
    sys.stdout.write ("took %d sec.\n" % (time.time()-starttime))

