import sys,os
# we need to insert the basedir to the python path (strip 2 path components) if we want to directly execute this file
sys.path.insert(0, os.path.dirname(os.path.dirname(sys.path[0])))
os.environ['DJANGO_SETTINGS_MODULE'] = "tah.settings"
from time import time,sleep
from django.conf import settings
from tah.tah_intern.models import Layer
from tah.tah_intern.Tileset import Tileset
from tah.tah_intern.Tile import Tile
from tah.requests.forms import CreateForm
from tah.requests.views import saveCreateRequestForm
base_tile_path = settings.TILES_ROOT

class request:
  META={'REMOTE_ADDR':'127.0.0.1'}

base_tile_path = settings.TILES_ROOT
layer=Layer.objects.get(name='tile')
CreateFormClass = CreateForm
CreateFormClass.base_fields['layers'].required = False 
r=request()

for x in range(1455,4096):
 print "x=%d" % x 
 for y in range(0,4096):
    tilepath, tilefile = Tileset(layer,12,x,y).get_filename(base_tile_path)
    tilesetfile = os.path.join(tilepath, tilefile)
    if not os.path.isfile(tilesetfile):
      # no new tilesetfile exists. now check if it's empty sea and request if not
      t = Tile(layer,12,x,y)
      t.serve_tile_sendfile('tile') # sets blankness byte if necessaty
      if t.is_blank() == 1:
        #empty sea. skip it
        print "skip empty sea %d %d" % (x,y)
        continue
      form = CreateFormClass({'min_z': 12, 'x': x, 'y': y, 'priority': 4, 'layers':[1,3], 'src':'RequestMissingTiles'})
      if form.is_valid():
        saveCreateRequestForm(r, form)
        print "x=%d,y=%d" % (x,y) 
      else:
        print "form is not valid. %s\n" % form.errors
      del form
