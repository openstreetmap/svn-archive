import os
from stat import *
from struct import unpack, calcsize
from datetime import datetime
from django.contrib.auth.models import User
from django.db.models import Q
from django.conf import settings
from django.shortcuts import render_to_response
from django.http import HttpResponse
from tah.requests.models import Request
from tah.tah_intern.Tile import Tile
from tah.tah_intern.Tileset import Tileset
from tah.tah_intern.models import Layer


def index(request):
  layer =  request.GET.get("layer","tile")
  z     =  int(request.GET.get("z",2))
  x     =  int(request.GET.get("x",2))
  y     =  int(request.GET.get("y",1))

  return render_to_response('base_browse.html',{'layer': layer,'z':z, 'x':x, 'y':y, 'x_range': range(x-1,x+2), 'y_range': range(y-1,y+2)})

def slippymap(request):
  layer =  request.GET.get("layer","tile")
  z     =  int(request.GET.get("z",2))
  x     =  int(request.GET.get("x",1))
  y     =  int(request.GET.get("y",1))

  return render_to_response('base_browse_slippy.html',{'layer': layer,'z':z, 'x':x, 'y':y})

def tiledetails_base(request):
 return tiledetails('tile',0,0,0)

def tiledetails(request,layername,z,x,y):
  z=int(z)
  x=int(x)
  y=int(y)
  userid=None
  layer = Layer.objects.get(name=layername)
  t=Tile(layer,z,x,y)
  # bail out if the tile coordinates were not valid.
  if not t.is_valid():
    return render_to_response('base_errormessage.html',{'header': 'Tile detail view','reason':'The tile coordinates were invalid. Please check zoom, x, and y values.'})
  (layer, base_z,base_x,base_y) = t.basetileset()
  # basetileset returns (None,None,None,None) if invalid
  (tilepath, tilefile) = Tileset(layer, base_z, base_x, base_y).get_filename(settings.TILES_ROOT)
  tilefile = os.path.join(tilepath, tilefile)
  try: 
    fstat = os.stat(tilefile)
    (basetile_fsize,basetile_mtime) = (fstat[6], datetime.fromtimestamp(fstat[8]))
    f=open(tilefile, 'rb')
    FILEVER, userid = unpack('BI',f.read(calcsize("BI")))
    f.close()
  except OSError: 
    (basetile_fsize,basetile_mtime) = None, None

  if userid != None: 
    try: user = User.objects.get(pk=userid)
    except User.DoesNotExist: user = "Nonexistent User ID %d" % userid
  else: user='Unknown'

  reqs = Request.objects.filter(Q(status__lt=2,layers__id__exact=1)|Q(status=2),min_z=base_z,x=base_x,y=base_y).distinct().order_by('status','-request_time')

  #Use regular HTML template, or short machine readable version?
  if request.GET.get('format','') == 'short':
    template = 'tile_details_machine.html'
  else:
    template = 'tile_details.html'

  return render_to_response(template,{'tile':t,'basetile_fsize':basetile_fsize,'basetile_mtime':basetile_mtime, 'user': user, 'reqs':reqs})

def show_map_of(request):
  return HttpResponse("not implemented",mimetype="text/plain")

def serve(request,layername,z,x,y):
  return HttpResponse(serve_tile(layername,z,x,y),mimetype="image/png")