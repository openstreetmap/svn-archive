from tah.tah_intern.Tile import Tile
import re
from datetime import datetime, timedelta
from mod_python import apache #needed for mod_python tile serving


#mod_python handler for serving tiles
def handler(req):
  r = re.compile('/(\w+)(\.php)?/(\d+)/(\d+)/(\d+)(\.png)?$')
  m = r.search(req.uri)
  if m:
    (layername,php,z,x,y,ext) = m.groups()
    t = Tile(None,z,x,y)
    (tilefile, offset, datalength) = t.serve_tile_sendfile(layername)
    if tilefile: 
      req.content_type = 'image/png'
      #req.update_mtime(t.mtime)
      #req.set_last_modified() #This can be used in newer mod_python versions. Does not work in 3.2.1 
      #maybe need to work around with req.finfo... req.finfo = apache.stat(req.filename, apache.APR_FINFO_MIN)
      req.finfo = apache.stat(tilefile, apache.APR_FINFO_MIN)
      if datalength > 0: req.set_content_length(datalength)
      req.headers_out['Expires']=(datetime.utcnow()+timedelta(0,0,0,0,0,3)).strftime("%a, %d %b %Y %H:%M:%S GMT")
      req.headers_out['Last-Modified']=datetime.utcfromtimestamp(t.mtime).strftime("%a, %d %b %Y %H:%M:%S GMT")
      status = req.meets_conditions()
      if status != apache.OK:
        return status
      req.sendfile(tilefile, offset, datalength)
      return apache.OK

    else: return apache.HTTP_NOT_FOUND
  return apache.HTTP_BAD_REQUEST

