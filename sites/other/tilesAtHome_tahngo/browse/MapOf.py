from django.http import HttpResponse
from math import *
import cairo, StringIO
from tah.tah_intern.Tile import Tile
from django import newforms as forms

def latlon2relativeXY(lat,lon):
  x = (lon + 180) / 360
  y = (1 - log(tan(radians(lat)) + 1/cos(radians(lat))) / pi) / 2
  return(x,y)

def latlon2xy(lat,lon,z):
  n = pow(2,z)
  x,y = latlon2relativeXY(lat,lon)
  return(floor(n*x), floor(n*y))

def export_MapOf(request):
  form = MapOfForm(request.GET)
  if form.is_valid():
    form = form.cleaned_data
    x,y = latlon2xy(form['lat'],form['lon'],form['z'])
    h = form['h'] // 256 #number of tiles in height
    w = form['w'] // 256 #number of tiles to left and right

    pngfile = StringIO.StringIO()
    im = cairo.ImageSurface(cairo.FORMAT_ARGB32, 256*w,256*h)
    ctx = cairo.Context(im)
    for i in range(0, w+1):
      for j in range(0, h+1):
        image = cairo.ImageSurface.create_from_png(StringIO.StringIO(Tile(None,form['z'],x-w/2+i,y-h/2+j).serve_tile('tile')))
        ctx.set_source_surface(image,256*i,256*j)
        ctx.paint()
    im.write_to_png(pngfile)
    pngfile.seek(0)
    return HttpResponse(pngfile.read(), mimetype='image/png')
  else: return HttpResponse("<form ACTION=\"\" METHOD=\"GET\">%s<input type=\"submit\"></form>"%(form.as_p()))


  
class MapOfForm(forms.Form):
    lat = forms.FloatField()
    lon = forms.FloatField()
    z = forms.FloatField(initial=12)
    w = forms.FloatField(initial=1024)
    h = forms.FloatField(initial=1024)
