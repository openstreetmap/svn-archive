from django.http import HttpResponse
from math import *
import StringIO
from PIL import Image
from tah.tah_intern.Tile import Tile
from django import newforms as forms
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
  form = MapOfForm(request.GET)
  if form.is_valid():
    form = form.cleaned_data
    x,y = latlon2xy(form['lat'],form['long'],form['z'])
    h = ceil(form['h'] / 256.0)  # tiles in height
    if h % 2 == 0: h += 1   # (next higher odd number)
    w = ceil(form['w'] / 256.0)  # tiles in width
    if w % 2 == 0: w += 1   # (next higher odd number)

    pngfile = StringIO.StringIO()
    im = Image.new('RGBA', (256*w,256*h))
    for i in range(0, w):
      for j in range(0, h):
        image =  Image.open(StringIO.StringIO(Tile(None,form['z'],floor(x)-w//2+i,floor(y)-h//2+j).serve_tile('tile')))
        im.paste(image,(256*i,256*j))
    marg_w, marg_h = int((256*w-form['w'])/2 + (256 * x % 1.0)), int((256*h-form['h'])/2 - (265 * y% 1.0))
    im = im.crop((marg_w,marg_h,marg_w+form['w'],marg_h+form['h']))
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
      if h > 1024:
          raise forms.ValidationError('Image height needs to be <= 1024 pixels.')
      else: return h

    def clean_w(self):
      w = self.cleaned_data['w']
      if w > 1024:
          raise forms.ValidationError('Image width needs to be <= 1024 pixels.')
      else: return w
