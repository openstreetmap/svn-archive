import struct,os.path

class Tile:
  x=None
  y=None
  z=None
  layer=None
  blankness=None

  def __init__(self, layer, z, x, y):
    self.layer=layer
    self.z=int(z)
    self.x=int(x)
    self.y=int(y)
    self.layer = layer

  def is_blank(self):
    #blankness=1 sea, blankness=2 land
    return self.blankness

  def set_blank(self,b):
    self.blankness = b

  def set_blank_sea(self):
    #blankness=1 sea, 2:land. 3:transparent
    if self.layer.transparent: self.blankness=3
    else: self.blankness=1

  def set_blank_land(self):
    #blankness=1 sea, 2:land. 3:transparent
    if self.layer.transparent: self.blankness=3
    else: self.blankness=2

  def is_valid(self):
    #allow 'None' layers for valid tiles. Tile serving tiles don't have one.
    #if self.layer == None: return 0
    if self.z < 0 or self.z > 17: return 0
    if self.x < 0 or self.y < 0: return 0
    if self.x >= pow(2,self.z) or self.y >= pow(2,self.z): return 0 
    return 1

  def basetileset(self):
    # return None tuple, if the tile is wrong
    if not self.is_valid(): return (None, None, None, None)
    if self.z >= 6:
      if self.z >= 12: base_z = 12
      else: base_z = 6
    else: base_z = 0
    base_x = self.x // pow(2,(self.z-base_z))
    base_y = self.y // pow(2,(self.z-base_z))
    return (self.layer, base_z, base_x, base_y)


  def serve_tile(self, layername):
    """ Return the PNG data of a tile """
    (tilesetfile, offset, datalength) = self.serve_tile_sendfile(layername)
    f = open(tilesetfile,'rb')
    f.seek(offset)
    data = f.read(datalength)
    f.close()
    return data

  def serve_tile_sendfile(self, layername):
    """ Return the (filename,offset,datalength) of a tile. If a full file should be sent, 
        offset is 0, and datalength -1. This is geared to apache.sendfile()
    """
    (layer, base_z,base_x,base_y) = self.basetileset() 
    # basetilepath could be taken from the Settings, hardcode for efficiency 
    basetilepath='/SX40/Tiles'
    blanktilepath='/SX40/Tiles'
    self.mtime = 0
    if base_z == None:
      # basetileset returns (None,None,None,None) if invalid
      tilesetfile = ''
    else:
      tilesetfile = os.path.join(basetilepath,"%s_%d"%(layername,base_z),"%04d"%(base_x),str(base_x)+'_'+str(base_y))
    try:
      self.mtime = os.stat(tilesetfile)[8] #set mtime to be used by 'Last-Modified' header
      f = open(tilesetfile,'rb')
      #calculate file offset
      offset = 8 # skip the header data
      i_step = 4 # bytes per index entry
      z_pow = pow(2, self.z - base_z) # cache pow^1 of how many z levels away from base_z 
      #skip (1,4,16,...) tiles foe next zoom levels
      for cur_z in range (0, self.z-base_z): offset += i_step * pow(4,cur_z)
      #skip to corresponding y-row
      offset += i_step * (self.y - base_y*z_pow) * z_pow
      #skip to corresponding x tile
      offset += i_step * (self.x - base_x*z_pow)
      f.seek(offset)
      data = f.read(8)
      (d_offset,d_offset_next) = struct.unpack('II',data)
      #return  "(%d,%d,%d) as %d offset1 %d  offset2 %d " % (self.z,self.x,self.y,offset,d_offset,d_offset_next)
    except OSError, IOError:
      d_offset = 0
      # check blanktiles.dat for blankness value
      blankness = self.blankness_value(layername)
      d_offset = blankness      # use returned blankness value
    if d_offset > 3:
      # we got a regular tilesetfile here
      #make sure that the second offset really points to data end and is not blank either
      while (d_offset_next <= 3):
        data = f.read(4)
        d_offset_next, = struct.unpack('I',data)
      return (tilesetfile,d_offset,d_offset_next-d_offset)
    else:
      # return complete blankness tile
      # first set blankness var to the appropriate value, so the caller knows
      self.blankness = d_offset
      blankpng = ['unknown.png','sea.png','land.png','transparent.png']
      tilesetfile = os.path.join(blanktilepath,blankpng[d_offset])
      d_length = os.stat(tilesetfile)[6] # tile length
      return (tilesetfile, 0, d_length)


  def blankness_value(self, layername):
    """ Returns: a blanktile value (0,..,3) as 'int' """
    oceantilesdat_path ='/SX40/oceantiles_12.dat'

    #fall to blanktile database
    #Return transparent for hardcoded transparent layers
    if layername in ['maplint','caption']: return 3

    if self.z < 12:
      #We only handle z12 and above this way. Lowzooms are required to have actual tiles
      return 0
    else:
      #Lookup in the oceans tile
      #For z12 and above the file will tell us directly what type it is
      try:
        (layern, base_z,base_x,base_y) = self.basetileset()
        f_oceant = open(oceantilesdat_path,'rb')
        f_oceant.seek((4096*base_y + base_x) >> 2)
        data = ord(f_oceant.read(1));
        #take 2 bits of the char
        bit_off = 3 - (base_x % 4);
        type = (data >> 2*bit_off) & 3
        # map tile type to blankness type used in the server
        blank = [0,2,1,1]
        return blank[type]
      except:
        # return 0 if we don't find oceantiles.dat
        return 0
 
