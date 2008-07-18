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
    data = t.serve_tile(layername)
    if data: 
      #req.content_type = 'text/plain'
      req.content_type = 'image/png'
      #req.mtime = t.mtime
      #req.set_last_modified() #This can be used in newer mod_python versions. Does not work in 3.2.1
      req.set_content_length(len(data))
      req.headers_out['Expires']=(datetime.utcnow()+timedelta(0,0,0,0,0,3)).strftime("%a, %d %b %Y %H:%M:%S GMT")
      req.headers_out['Last-Modified']=datetime.utcfromtimestamp(t.mtime).strftime("%a, %d %b %Y %H:%M:%S GMT")
      status = req.meets_conditions()
      if status == apache.OK:
        req.write(data)
      return status

    else: return apache.HTTP_NOT_FOUND
  return apache.HTTP_BAD_REQUEST
  #TODO use sendfile(  	path[, offset, len])?
