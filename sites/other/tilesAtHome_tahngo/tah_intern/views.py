from django.http import HttpResponse
from tah.tah_intern.models import Blank, Layer
from django.core.exceptions import ObjectDoesNotExist
from django.views.decorators.cache import cache_control

@cache_control(no_cache=True)
def show_log(request):
  read_size=1024
  #TODO use a setting for the log file location?
  f = open('/var/www/tah/tah_intern/tah.log', 'rU')
  offset = read_size
  f.seek(0, 2)
  file_size = f.tell()
  if file_size < offset:
    offset = file_size
  f.seek(-1*offset, 2)
  read_str = f.read(offset)
  # Remove newline at the end
  if read_str[offset - 1] == '\n':
    read_str = read_str[:-1]
  lines = read_str.split('\n')
  f.close()
  return HttpResponse("\n".join(lines[-10:]), mimetype="text/plain")

def index(request):
  return HttpResponse('')

def show(request,layer,x,y,z):
  retcode = 0
  file=("/var/www/osm/Tiles/Gfx/unknown.png","/var/www/osm/Tiles/Gfx/sea.png","/var/www/osm/Tiles/Gfx/sea.png","/var/www/osm/Tiles/Gfx/error.png")
  try:
    l = Layer.objects.get(name=layer)
  except ObjectDoesNotExist:
    retcode = 3

  if retcode == 0:
    retcode = int(Blank(layer=l,z=z,x=x,y=y))

  image_data = open(file[retcode], "rb").read()
  return HttpResponse(image_data, mimetype="image/png")