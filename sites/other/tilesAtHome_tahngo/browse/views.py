import os
from stat import *
from datetime import datetime
from django.shortcuts import render_to_response
from django.http import HttpResponse
from tah.tah_intern.Tile import Tile
from tah.tah_intern.models import Settings

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

def tiledetails(request,layername,z,x,y):
  z=int(z)
  x=int(x)
  y=int(y)
  t=Tile(layername,z,x,y)
  (layer, base_z,base_x,base_y) = t.basetileset()
  # basetileset returns (None,None,None,None) if invalid!
  basetilepath = Settings().getSetting(name='base_tile_path')
  tilefile = os.path.join(basetilepath,layername+"_%s" % base_z,"%s_%s"%(base_x,base_y))

  try: 
    fstat = os.stat(tilefile)
    (basetile_fsize,basetile_mtime) = (fstat[6], datetime.fromtimestamp(fstat[8]))
  except OSError: 
    (basetile_fsize,basetile_mtime) = None, None

  return render_to_response('tile_details.html',{'tile':t,'basetile_fsize':basetile_fsize,'basetile_mtime':basetile_mtime})

def show_map_of(request):
  return HttpResponse("not implemented",mimetype="text/plain")

def serve(request,layername,z,x,y):
  return HttpResponse(serve_tile(layername,z,x,y),mimetype="image/png")