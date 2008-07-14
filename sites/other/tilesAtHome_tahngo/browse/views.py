import os
from stat import *
from struct import unpack, calcsize
from datetime import datetime
from django.shortcuts import render_to_response
from django.http import HttpResponse
from tah.tah_intern.Tile import Tile
from tah.tah_intern.models import Settings
from django.contrib.auth.models import User

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
  userid=None
  t=Tile(layername,z,x,y)
  (layer, base_z,base_x,base_y) = t.basetileset()
  if base_z == None:
    # basetileset returns (None,None,None,None) if invalid
    tilefile = ''
  else:
   basetilepath = Settings().getSetting(name='base_tile_path')
   tilefile = os.path.join(basetilepath,"%s_%s_%d" % (layername,base_z,base_x//1000),"%s_%s"%(base_x,base_y))
 
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

  return render_to_response('tile_details.html',{'tile':t,'basetile_fsize':basetile_fsize,'basetile_mtime':basetile_mtime, 'user': user})

def show_map_of(request):
  return HttpResponse("not implemented",mimetype="text/plain")

def serve(request,layername,z,x,y):
  return HttpResponse(serve_tile(layername,z,x,y),mimetype="image/png")