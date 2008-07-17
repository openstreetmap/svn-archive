#!/usr/bin/env python

import os, sys, logging, zipfile, random, re, stat, signal
# we need to insert the basedir to the python path (strip 2 path components) if we want to directly execute this file
sys.path.insert(0, os.path.dirname(os.path.dirname(sys.path[0])))
os.environ['DJANGO_SETTINGS_MODULE'] = "tah.settings"
from datetime import datetime
from time import sleep,clock,time
from django.conf import settings as djangosettings
from tah.tah_intern.models import Settings, Layer
from tah.tah_intern.Tileset import Tileset
from tah.tah_intern.Tile import Tile
from django.contrib.auth.models import User
from tah.requests.models import Request,Upload
from django.core.exceptions import ObjectDoesNotExist

### TileUpload returns 0 on success and >0 otherwise
class TileUpload:
  unzip_path=None #path to be used for unzipping (temporary files)
  base_tilepath=None # base tile directory
  fname=None      #complete path of uploaded tileset file
  uid=None        #random unique id which is used for pathnames
  tmptiledir=None #usually unzip_path+uid contains the unzipped files

  def __init__(self,settings):
    self.unzip_path = settings.getSetting(name='unzipPath')
    self.base_tilepath = settings.getSetting(name='base_tile_path')
    if self.unzip_path == None or self.base_tilepath == None:
      sys.exit("Failed to get required settings.")

  def process(self):
   try:
    while True:
      #find the oldest unlocked upload file
      upload = None
      while not upload:
        # repeat fetching until there is one
        try:
          upload = Upload.objects.filter(is_locked=False).latest('upload_time')
        except ObjectDoesNotExist:
          #logging.debug('No uploaded request. Sleeping 5 sec.')
          #print("No uploaded request. Sleeping 5 sec.")
          sleep(10)
      starttime = (time(),clock()) # start timing tileset handling now
      self.fname = upload.get_file_filename()
      #obsolete by above line: os.path.join(djangosettings.MEDIA_ROOT,upload.file)
      if os.path.isfile(self.fname):
        logging.debug('Handling next tileset: ' + upload.file)
        self.uid = str(random.randint(0,9999999999999999999))
        if self.unzip():
          tset = self.movetiles()
          #finally save the tileset at it's place
          time_save = [time()]
          logging.debug("Saving tileset at (%s,%d,%d,%d) from user %s" % (tset.layer,tset.base_z,tset.x,tset.y,upload.user_id))
          (retval,unknown_tiles) = tset.save(self.base_tilepath, upload.user_id.id)
          time_save.append(time())
          if retval:
            # everything went fine. Add to user statistics
            self.add_user_stats(upload, 1365-unknown_tiles)
            # now match up the upload with a request and mark the request as finished
            reqs = Request.objects.filter(min_z = tset.base_z, x = tset.x ,y = tset.y, status__lt=2)
            for req in reqs:
              # remove corresponding layer from request and set it to status=2 when all layers are done
              req.layers.remove(tset.layer)
              if req.layers.count() == 0:
                req.status=2
                req.clientping_time=datetime.now()
                req.save()
            logging.debug('Finished "%s,%d,%d,%d" in %.1f sec (CPU %.1f). Saving took %.1f sec. %d unknown tiles.' % (tset.layer,tset.base_z,tset.x,tset.y,time()-starttime[0],clock()-starttime[1], time_save[1] - time_save[0], unknown_tiles))
          else:
            # saving the tileset went wrong
            logging.error('Saving tileset "%s,%d,%d,%d" failed. Aborting tileset. Took %.1f sec (CPU %.1f). %d unknown tiles.' % (tset.layer,tset.base_z,tset.x,tset.y,time()-starttime[0],clock()-starttime[1], unknown_tiles))
        self.cleanup(upload,True)

      else:
        logging.info('uploaded file not found, deleting upload.')
	upload.delete()
   except KeyboardInterrupt:
     if upload: self.cleanup(upload, False)
     logging.info('Ctrl-C pressed. Shutdown gracefully.')
     sys.exit("Ctrl-C pressed. Shutdown gracefully. Upload was: %s" % upload)
  #-----------------------------------------------------------------
  def unzip(self):
    now = clock()
    outfile = None
    self.tmptiledir = dir = os.path.join(self.unzip_path,self.uid)
    os.mkdir(dir, 0777)
    try:
      zfobj = zipfile.ZipFile(self.fname)

      for name in zfobj.namelist():
        if name.endswith('/'):
          os.mkdir(os.path.join(dir, name))
        else:
          outfile = open(os.path.join(dir, name), 'wb')
          outfile.write(zfobj.read(name))
          outfile.close()
    except zipfile.BadZipfile:
      logging.info('found bad zip file ('+self.uid+')')
      if outfile: outfile.close()
      return 0
    except:
      logging.info('unknown zip file error')
      if outfile: outfile.close()
      return(0)

    logging.debug('Unzipped tileset in %.1f sec.' % ((clock()-now)))
    return(1)

  #-----------------------------------------------------------------
  def movetiles(self):
    # 67 byte file => mark blank land;69 byte => mark blank sea; ignore every thing else below 100 bytes
    smalltiles = 0
    layer = None
    r = re.compile('^([a-zA-Z]+)_(\d+)_(\d+)_(\d+).png$')
    tset = Tileset()

    for f in os.listdir(self.tmptiledir):
      ignore_file = False
      full_filename = os.path.join(self.tmptiledir,f)
      m = r.match(f)
      if not m:
        logging.debug('found weird file '+f+' in zip. Ignoring.')
        ignore_file = True

      if not ignore_file:
        # get the layer if it's any different
        if not (layer and layer.name == m.group(1)):
          try: layer = Layer.objects.get(name=m.group(1))
          except ObjectDoesNotExist:
            logging.info('unknown layer ('+m.group(1)+')')
            return 0
        t = Tile(layer=layer,z=m.group(2),x=m.group(3),y=m.group(4))
        #print "found layer:"+m.group(1)+'z: '+m.group(2)+'x: '+m.group(3)+'y: '+m.group(4)

        #check the size to catch special meaning. This check can be removed once blankness
        #information is conveyed by other means than file size...
        fsize = os.stat(full_filename)[stat.ST_SIZE]
        if fsize < 100:
          if fsize == 67:
            #mark blank land
            t.set_blank_land()
            tset.add_tile(t,None)
          elif fsize == 69:
            #mark blank sea
            t.set_blank_sea()
            tset.add_tile(t,None)
          else:
            # ignore unknown small png files
            smalltiles += 1
        else:
          #png has regular filesize
          tset.add_tile(t,full_filename)

    if smalltiles: logging.debug('Ignored %d too small png files' % smalltiles)
    return tset

  #-----------------------------------------------------------------
  def add_user_stats(self, upload, uploaded_tiles):
    """ Update the tah user statistics after a successfull upload """
    tahuser = upload.user_id.tahuser_set.get()
    try: tahuser.kb_upload += os.stat(upload.get_file_filename())[stat.ST_SIZE] // 1024
    except OSError: pass
    tahuser.renderedTiles += uploaded_tiles
    tahuser.save()

  #-----------------------------------------------------------------
  def rm_dir(self,dir):
    if dir:
      for path in (os.path.join(dir,f) for f in os.listdir(dir)):
        if os.path.isdir(path):
          rm_rf(path)
        else:
          os.unlink(path)
      os.rmdir(dir)

  #-----------------------------------------------------------------
  def sigterm(self, signum, frame):
    """ This is called when a SIGTERM signal is issued """
    print "Received SIGTERM signal. Shutdown gracefully."
    self.cleanup(None, False)
    sys.exit(0)

  #-----------------------------------------------------------------
  def cleanup(self, upload, del_upload = True):
    """ Removes all temporary files and removes the upload object 
        (and the uploaded file if 'del_upload' is True.
    """
    # Delete the unzipped files directory
    self.rm_dir(self.tmptiledir)
    self.uid=None
    self.fname=None
    self.tmptiledir=None
    if del_upload:
      # delete the uploaded file itself
      try: os.unlink(upload.get_file_filename())
      except: pass
      # delete the upload db entry
      upload.delete()

#---------------------------------------------------------------------

if __name__ == '__main__':
  settings = Settings()
  logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(levelname)s %(message)s',
                    datefmt = "%y-%m-%d-%H:%M:%S", 
                    filename=settings.getSetting(name='logFile'),
                    filemode='w') 

  logging.info('Starting tile upload processor')
  u = TileUpload(settings)
  signal.signal(signal.SIGTERM,u.sigterm)
  if not u.process():
      logging.critical('Upload handling returned with error. Aborting.')
      sys.stderr.write('Upload handling returned with error')
      sys.exit(1)
else:
  sys.stderr.write('You need to run this as the main program.')
  sys.exit(1)
