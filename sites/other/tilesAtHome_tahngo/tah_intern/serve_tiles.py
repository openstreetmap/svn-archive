from tah.tah_intern.Tile import Tile
import re
from time import time
from email.Utils import formatdate
from mod_python import apache

_URL_PATTERN = re.compile('/(\w+)(\.php)?/(\d+)/(\d+)/(\d+)(\.png)?$')
_EXPIRES = 3 * 3600     # expire in 3 hours

#mod_python handler for serving tiles
def handler(req):
  m = _URL_PATTERN.search(req.uri)
  if m:
    (layername, php, z, x, y, ext) = m.groups()
    t = Tile(None,z,x,y)
    (tilefile, offset, datalength) = t.serve_tile_sendfile(layername)
    if tilefile: 
      req.content_type = 'image/png'
      #req.update_mtime(t.mtime)
      #req.set_last_modified() #This can be used in newer mod_python versions. Does not work in 3.2.1 
      if datalength > 0:
        req.set_content_length(datalength)
      req.headers_out['Cache-Control'] = 'max-age=10800, must-revalidate'
      req.headers_out['Expires'] = formatdate(time() + _EXPIRES, usegmt=True) 
      req.headers_out['Last-Modified'] = formatdate(t.mtime,usegmt=True)

      status = req.meets_conditions()
      if status != apache.OK:
        return status
      req.sendfile(tilefile, offset, datalength)
      return apache.OK

    else:
      return apache.HTTP_NOT_FOUND
  return apache.HTTP_BAD_REQUEST
