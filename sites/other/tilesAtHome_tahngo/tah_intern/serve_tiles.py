from tah.tah_intern.Tile import Tile
import re
from mod_python import apache #needed for mod_python tile serving


#mod_python handler for serving tiles
def handler(req):
  r = re.compile('/(\w+)/(\d+)/(\d+)/(\d+)(\.png)?$')
  m = r.search(req.uri)
  if m:
    (layername,z,x,y,ext) = m.groups()
    t = Tile(None,z,x,y)
    data = t.serve_tile(layername)
    if data: 
      #req.content_type = 'text/plain'
      req.content_type = 'image/png'
      req.write(data)
      return apache.OK
    else: return apache.HTTP_NOT_FOUND
  return apache.HTTP_BAD_REQUEST
  #TODO use sendfile(  	path[, offset, len])

if __name__ == '__main__':
  serve_tile('maplint',12,3813,1204)
