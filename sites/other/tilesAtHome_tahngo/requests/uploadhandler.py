from django.conf import settings
from MySQLdb import OperationalError
from tah.requests.models import Request
from django.contrib.auth.models import User
from tempfile import mkstemp
from datetime import datetime
import logging, os, struct, stat
import shutil
from tah.tah_intern.Tileset import Tileset
from tah.tah_intern.models import Layer


def handle_uploaded_tileset(file, form):
  """Handle uploaded tileset file
     :returns: True if it has been successfully handled
               (no need to save the entry in the db anymore)
  """

  # only handle .tileset files
  if not file.name.lower().endswith('.tileset'):
      return False

  # retrieve the uploader user id
  user = User.objects.get(pk=form['user_id'])
  logging.info("Handle uploaded file %s directly (user %s)" % (file.name,form['user_id']))

  # if temporary_file_path exists, the file has been saved to a tmp loc,
  # otherwise it's in RAM and still needs saving
  try:
      tmp_fullpath = file.temporary_file_path()
  except AttributeError:
      (tmp_fd, tmp_fullpath) = mkstemp()
      f = os.fdopen(tmp_fd, 'r+b')
      for chunk in file.chunks():
          f.write(chunk)
          f.close()

  #check whether the emptyness value is set and if yes, discard the file
  #later. We rely on our blank database file in that case.
  f = open(tmp_fullpath, 'r+b')
  header = f.read(8)
  (FILEVER, levels, size, empty, userid) = struct.unpack('<BBBBI', header)

  if not empty and userid != form['user_id']:
     # set upload user id field
     f.seek(4)
     f.write(struct.pack('<I',form['user_id']))

  f.close()
  # more liberal permisssions are needed to allow reading
  os.chmod(tmp_fullpath,0664)

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

  # delete new and existing tileset files if the new is 'empty'
  if empty:
      try:
          os.unlink(tmp_fullpath)
          os.unlink(os.path.join(tset_path, tset_fname))
      except OSError, e:
          if e.errno == 2:
              pass
          else:
              raise
      logging.debug("delete empty tileset, layer=%s (%s,%s,%s)" % (layer, form['z'], form['x'], form['y']))
      tset_fsize = 0
  else:
      # file size is needed to add them to the user stats later
      tset_fsize = os.stat(tmp_fullpath)[stat.ST_SIZE]

      # if the path does not exist yet, create it
      # move tmp tilesetfile to final location
      if not os.path.isdir(tset_path): os.makedirs(tset_path, 0775)
      shutil.move(tmp_fullpath, os.path.join(tset_path, tset_fname))
      logging.debug("Tileset file moved to %s" %
                    (os.path.join(tset_path, tset_fname)))

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
              logging.info("MySQL error marking request (%d,%d,%d,%s) finished" % (req.min_z,req.x,req.y,layer))
              return False

  ### add user stats for this upload
  tahuser = user.tahuser_set.get()
  if tset_fsize:
    tahuser.kb_upload += tset_fsize // 1024
  #XXXTODO: rather than hardcoding 1265 tiles per fileset we can now calculate the true number for v2
  tahuser.renderedTiles += 1365
  tahuser.save()

  return True
