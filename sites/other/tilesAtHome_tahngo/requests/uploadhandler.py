from django.conf import settings
from MySQLdb import OperationalError
from tah.requests.models import Request
from django.contrib.auth.models import User
from tempfile import mkstemp
from datetime import datetime
import logging, os, struct, stat
from shutil import move
from tah.tah_intern.Tileset import Tileset
from tah.tah_intern.models import Layer

# handle uploaded tileset file
# returns true, if it has been successfully handled (no need
# to save the entry in the db anymore)
def handle_uploaded_tileset(file, form):
  # only handle .tileset files now
  if not file.name.lower().endswith('.tileset'):
      return False

  user = User.objects.get(pk=form['user_id'])
  logging.info("Handle uploaded file %s directly (user %s)" % (file.name,form['user_id']))
  # if temporary_file_path exists, the file has been saved to a tmp loc,
  # otherwise it's in RAM and still needs saving
  try:
      tmp_fullpath = file.temporary_file_path()
      f = open(tmp_fullpath, 'r+b')
  except AttributeError:
      (tmp_fd, tmp_fullpath) = mkstemp()
      f = os.fdopen(tmp_fd, 'r+b')
      for chunk in file.chunks():
          f.write(chunk)

  # set upload user id field
  f.seek(4)
  f.write(struct.pack('<I',form['user_id']))
  f.close()
  # more liberal permisssions
  os.chmod(tmp_fullpath,0664)

  tset_fsize = os.stat(tmp_fullpath)[stat.ST_SIZE]

  ## init layer and Tileset object to retrieve the file locations
  try:
      layer = Layer.objects.get(id=form['layer'])
      tset = Tileset(layer=layer, base_z= int(form['z']), x= int(form['x']), y= int(form['y']))
  except KeyError:
      # one of the required form values was not set
      logging.info("A required variable was not set")
      return False

  # retrieve file locations
  (tset_path, tset_fname) = tset.get_filename(settings.TILES_ROOT)

  if tset == None:
      # tileset not valid
      logging.info("%s is no valid tileset, layer=%s, (z,x,y)=(%s,%s,%s)" % (file.name,layer, form['z'], form['x'], form['y']))
      return False

  # if the path does not exist yet, create it
  if not os.path.isdir(tset_path): os.makedirs(tset_path, 0775)

  # move tmp tilesetfile to final location, if it's a regular tileset
  if tset_fsize > 1:
    move(tmp_fullpath, os.path.join(tset_path, tset_fname))
    logging.info("file moved to %s" % (os.path.join(tset_path, tset_fname)))
  else:
    # if it's empty (0 or 1 byte, delete the existing one)
    os.unlink(tmp_fullpath)
    os.unlink(os.path.join(tset_path, tset_fname))
    logging.info("delete empty tileset, layer=%s (%s,%s,%s)" % (layer, form['z'], form['x'], form['y']))



  ### mark all satisfied requests as such.
  # now match upload with a request and mark request finished
  reqs = Request.objects.filter(min_z = tset.base_z, \
                                x = tset.x ,y = tset.y, status__lt=2)

  for req in reqs:
      # remove corresponding layer from request and set to 
      # status=2 when all layers are done
      req.layers.remove(tset.layer)
      if req.layers.count() == 0:
          req.status=2
          req.clientping_time=datetime.now()
          req.client_id = user.id
          try:
              req.save()
              logging.info("tileset marked request as fulfilled (z,x,y) (%d,%d,%d)" % (req.min_z,req.x,req.y))
          except OperationalError, (errnum,errmsg):
              # e.g. MySQL transaction timeout, quit
              return False

  ### add user stats for this upload
  tahuser = user.tahuser_set.get()
  try: tahuser.kb_upload += tset_fsize // 1024
  except OSError: pass # don't crap out if we don't find a file
  tahuser.renderedTiles += 1365
  tahuser.save()

  return True
