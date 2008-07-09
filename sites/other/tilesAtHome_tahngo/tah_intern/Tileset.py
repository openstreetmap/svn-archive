from tah.tah_intern.models import Layer
from tah.tah_intern.Tile import Tile
from django.core.exceptions import ObjectDoesNotExist
from struct import pack,unpack
import os,stat
from shutil import move
from tempfile import mkstemp
from sys import exit

class Tileset:
  x=None
  y=None
  base_z=None
  layer=None
  init=False
  #tiles[z][x][y] points to a tuple (<Tile>,<fullfilename>)
  tiles={}

  def __init__(self, layer=None, base_z=None, x=None, y=None):
    self.layer = layer
    self.base_z = base_z
    self.x = x
    self.y = y
    self.tiles = {} # empty set contains no tiles yet
    if layer and base_z!=None and x!=None and y!=None: self.init=True

  def add_tile(self,t,tilefile):
    if self.fits_tileset(t):
      if not self.tiles.has_key(t.z): self.tiles[t.z] = {} 
      if not self.tiles[t.z].has_key(t.x): self.tiles[t.z][t.x] = {} 
      self.tiles[t.z][t.x][t.y] = (t,tilefile)
      return 1
    else: return 0

  # checks whether a tile belongs in this tileset or not
  # note, that this inits and empties the tileset on the first call with a valid tile
  def fits_tileset(self,tile):
    #assert isinstance(tile,Tile)
    # return if the tile is not a valid one
    if not tile.is_valid: return 0
    (layer,base_z,x,y) = tile.basetileset()
    #if this is the first added tile, we init the tileset
    if self.init == False:
      self.__init__(layer, base_z, x, y)
    if self.x == x and self.y == y and self.base_z == base_z and layer == self.layer:
      return 1
    else:
      # tile belongs to a different tileset
      return 0

  # recursively set sub-tiles to blankness of t if they are not existing yet.
  def set_subtiles_blank(self,t):
    #abort if at lowest level
    if t.z < self.base_z+6: return

    for x in range(2*t.x, 2*t.x+2):
      for y in range(2*t.y, 2*t.y+2):
        try: 
          (subt, file) = self.tiles[t.z+1][x][y]
          if subt.blankness != None or file != None:
            #subtile is already marked blank or has a file associated.
            #nothing to do here.
            return
        except KeyError:
          # create a blank subtile
          subt = Tile(self.layer,t.z+1,x,y)

        subt.set_blank(t.blankness)
        self.add_tile(subt,None)
        self.set_subtiles_blank(subt)

  def save(self,base_tile_path):
    """
	Returns a tuple: (1,unknown_tiles) (1 being success, unknown_tiles being 
        the number of tiles with unknown blankness, which should always be 0 if 
        the tileset was complete.
    """
    assert (base_tile_path > '')
    tilepath    = os.path.join(base_tile_path,self.layer.name+'_'+str(self.base_z))
    tilesetfile = os.path.join(tilepath,str(self.x)+'_'+str(self.y))
    #f = open(os.path.join(tilepath,str(self.x)+'_'+str(self.y)),'wb')
    (tmp_fd,tmpfile) = mkstemp()
    f = os.fdopen(tmp_fd, 'w+b')
    unknown_tiles=0
    FILEVER=1
    userid=1
    d_offset = 5472 #PNG data starts here

    #write the file type version and the user id
    f.write(pack('BI',FILEVER,userid))
    i_offset=f.tell()
    for z in range(self.base_z,self.base_z+6):
      for y in range(self.y*pow(2,z-self.base_z),(1+self.y)*pow(2,z-self.base_z)):
        for x in range(self.x*pow(2,z-self.base_z),(1+self.x)*pow(2,z-self.base_z)):
          # for each possible tile in tileset do
          f.seek(i_offset)
          try: 
            (t, tilefile) = self.tiles[z][x][y]
	    if (t.blankness != None) and (tilefile == None):
              #recursively set all sub-tiles to blank too if not already
              self.set_subtiles_blank(t)
              #write out the blank state
              f.write(pack('I',t.blankness))
            else:
              # nonblank tile
              #try: 
                f.write(pack('I',d_offset))
                f_t = open(tilefile,'rb')
                f.seek(d_offset)
                f.write(f_t.read())
                f_t.close()
                d_offset += os.stat(tilefile)[stat.ST_SIZE]
	      #except:
              #  print ("OUTCH, THIS SHOULD NOT HAVE HAPPENED. (%d %d %d) File: %s") % (z,x,y,tilefile)
              #  exit ("OUTCH, THIS SHOULD NOT HAVE HAPPENED")
          except KeyError:
            unknown_tiles += 1
            f.write(pack('I',0))
          i_offset += 4
    if f:
      # write one last index entry pointing to the end of the file
      f.seek(i_offset)
      f.write(pack('I',d_offset))
      f.close()
    # finally create path if necessary and move the tmp file to its final location
    if not os.path.isdir(tilepath): os.mkdir(tilepath, 0775)
    os.chmod(tmpfile,0664)
    move(tmpfile, tilesetfile)
    return (1,unknown_tiles)
