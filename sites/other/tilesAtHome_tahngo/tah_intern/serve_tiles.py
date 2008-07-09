from tah.tah_intern.Tile import Tile
import struct, os, re
from mod_python import apache #needed for mod_python tile serving


#mod_python handler for serving tiles
def handler(req):
  r = re.compile('/(\w+)/(\d+)/(\d+)/(\d+)(\.png)?$')
  m = r.search(req.uri)
  if m:
    (layername,z,x,y,ext) = m.groups()
    data = serve_tile(layername,z,x,y)
    if data: 
      #req.content_type = 'text/plain'
      req.content_type = 'image/png'
      req.write(data)
      return apache.OK
    else: return apache.HTTP_NOT_FOUND
  return apache.HTTP_BAD_REQUEST
  #TODO use sendfile(  	path[, offset, len])

def serve_tile(layername,z,x,y):
    z=int(z)
    x=int(x)
    y=int(y)
    t=Tile(layername,z,x,y)
    (layer, base_z,base_x,base_y) = t.basetileset() 
    # basetileset returns (None,None,None,None) if invalid!
    # basetilepath could be take from the Settings, hardcode for efficiency
    basetilepath='/mnt/agami/openstreetmap/tah/Tiles'
    tilesetfile = os.path.join(basetilepath,layername+'_'+str(base_z),str(base_x)+'_'+str(base_y))
    try:
      f = open(tilesetfile,'rb')
      #calculate file offset
      offset = 8 # skip the header data
      i_step = 4 # bytes per index entry
      z_pow = pow(2,z - base_z) # cache pow^1 of how many z levels away from base_z 
      #skip (1,4,16,...) tiles foe next zoom levels
      for cur_z in range (0,z-base_z): offset += i_step * pow(4,cur_z)
      #skip to corresponding y-row
      offset += i_step * (y - base_y*z_pow) * z_pow
      #skip to corresponding x tile
      offset += i_step * (x - base_x*z_pow)
      f.seek(offset)
      data = f.read(8)
      (d_offset,d_offset_next) = struct.unpack('II',data)
      #return  "(%d,%d,%d) as %d offset1 %d  offset2 %d " % (z,x,y,offset,d_offset,d_offset_next)
    except IOError:
      d_offset = 0

    if d_offset > 3:
      f.seek(d_offset)
      data = f.read(d_offset_next-d_offset)
    else:
      # return blankness value
      blankpng = ['unknown.png','sea.png','land.png','transparent.png']
      f_png = open(os.path.join(basetilepath,blankpng[d_offset]),'rb')
      data = f_png.read()
      f_png.close()
    try: f.close()
    except: pass
    return data

if __name__ == '__main__':
  serve_tile('maplint',12,3813,1204)
