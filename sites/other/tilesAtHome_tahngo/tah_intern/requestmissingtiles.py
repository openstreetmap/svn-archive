import sys,os
# we need to insert the basedir to the python path (strip 2 path components) if we want to directly execute this file
sys.path.insert(0, os.path.dirname(os.path.dirname(sys.path[0])))
os.environ['DJANGO_SETTINGS_MODULE'] = "tah.settings"
from time import time,sleep
from django.conf import settings
from tah.tah_intern.models import Layer
from tah.tah_intern.Tileset import Tileset
from tah.requests.forms import CreateForm
from tah.requests.models import Request
from tah.requests.views import saveCreateRequestForm
base_tile_path = settings.TILES_ROOT

class request:
  META={'REMOTE_ADDR':'127.0.0.1'}

base_tile_path = settings.TILES_ROOT
layer=Layer.objects.get(name='tile')
r=request()

for x in range(753,1000):
 print "x=%d" % x
 for y in range(1000,2000):
    tilepath, tilefile = Tileset(layer,12,x,y).get_filename(base_tile_path)
    tilesetfile = os.path.join(tilepath, tilefile)
    if not os.path.isfile(tilesetfile):
      CreateFormClass = CreateForm
      CreateFormClass.base_fields['max_z'].required = False 
      CreateFormClass.base_fields['layers'].required = False 
      CreateFormClass.base_fields['status'].required = False 
      form = CreateFormClass({'min_z': 12, 'x': x, 'y': y, 'priority': 4, 'layer':[1]})
      if form.is_valid():
        req, reason = saveCreateRequestForm(r, form)
      else:
        print "form is not valid. %s\n" % form.errors
