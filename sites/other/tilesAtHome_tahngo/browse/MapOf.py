from django.http import HttpResponse
from math import *
import StringIO
from PIL import Image
from tah.tah_intern.Tile import Tile
from django import forms
from django.views.decorators.cache import cache_control

def latlon2relativeXY(lat,lon):
  x = (lon + 180) / 360
  y = (1 - log(tan(radians(lat)) + 1/cos(radians(lat))) / pi) / 2
  return(x,y)

def latlon2xy(lat,lon,z):
  n = pow(2,z)
  x,y = latlon2relativeXY(lat,lon)
  return(n*x, n*y)

@cache_control(max_age=864000) #cache for 10 days
def export_MapOf(request):
  x_range = []
  y_range= []
  form = MapOfForm(request.GET)
  if form.is_valid():
    form = form.cleaned_data
    x,y = latlon2xy(form['lat'],form['long'],form['z'])
    x_range.append(int(x - form['w'] / 512.0))  # minimum x tile
    x_range.append(int(x + form['w'] / 512.0))  # maximum x tile
    y_range.append(int(y - form['h'] / 512.0))  # minimum y tile
    y_range.append(int(y + form['h'] / 512.0))  # maximum y tile
    pngfile = StringIO.StringIO()
    im = Image.new('RGBA', (256*(x_range[1]-x_range[0]+1),256*(y_range[1]-y_range[0]+1)))

    for i in range(x_range[0], x_range[1]+1):
      for j in range(y_range[0], y_range[1]+1):
        image =  Image.open(StringIO.StringIO(Tile(None,form['z'],i,j).serve_tile('tile')))
        im.paste(image,(256*(i-x_range[0]),256*(j-y_range[0])))
    marg_w = int(256*(x-x_range[0]-form['w']/512.0))
    marg_h  = int(256*(1-(y-y_range[0]-form['h']/512.0)))
    im = im.crop((marg_w,marg_h,marg_w+form['w'],marg_h+form['h']))
    #return HttpResponse(str(marg_w)+" "+str(marg_h)+" " +str(x) + str(x_range) + str(y)+str(y_range))
    im.save(pngfile,form['format'])
    pngfile.seek(0)
    response = HttpResponse(pngfile.read(), mimetype= 'image/'+str(form['format']))
    response['Content-Disposition'] = 'attachment; filename=map.'+form['format']
    return response
  else: return HttpResponse("<form ACTION=\"\" METHOD=\"GET\">%s<input type=\"submit\"></form>"%(form.as_p()))


  
class MapOfForm(forms.Form):
    lat = forms.FloatField()
    long = forms.FloatField()
    z = forms.IntegerField(initial=12)
    w = forms.IntegerField(initial=1024)
    h = forms.IntegerField(initial=1024)
    format = forms.ChoiceField(choices=[('png','png'),('jpeg','jpeg')])

    def clean_h(self):
      h = self.cleaned_data['h']
      if h > 2000:
          raise forms.ValidationError('Image height needs to be <= 2000 pixels.')
      else: return h

    def clean_w(self):
      w = self.cleaned_data['w']
      if w > 2000:
          raise forms.ValidationError('Image width needs to be <= 2000 pixels.')
      else: return w
