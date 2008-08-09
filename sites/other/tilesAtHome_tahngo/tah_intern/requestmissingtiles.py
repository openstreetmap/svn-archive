import sys,os
# we need to insert the basedir to the python path (strip 2 path components) if we want to directly execute this file
sys.path.insert(0, os.path.dirname(os.path.dirname(sys.path[0])))
os.environ['DJANGO_SETTINGS_MODULE'] = "tah.settings"
from time import time,sleep
#import urllib
from tah.tah_intern.models import Layer
from tah.tah_intern.LegacyTileset import LegacyTileset
from django.conf import settings

base_tile_path = settings.TILES_ROOT
layer=Layer.objects.get(name='tile')

for x in range(750,1000):
 for y in range(0,4096):
    tilesetfile = os.path.join(base_tile_path,"%s_%d_%d" % (layer.name,12,x//1000),str(x)+'_'+str(y))
    if not os.path.isfile(tilesetfile):
      URL = "http://tah.openstreetmap.org/tahngo/Request/create/?x=%s&y=%d&priority=4" %(x,y)
      #print "Would request %s" % tilesetfile
      urllib.urlopen(URL)
      #sleep(1)
      #now = time()
      #lt = LegacyTileset()
      #lt.convert(layer,12,x,y,base_tile_path)
      if y%50 == 0:
        print "x=%d y=%d (time %.1f sec)"% (x,y,time()-now)
