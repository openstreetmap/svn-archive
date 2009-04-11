from tah.tah_intern.models import Layer
from tah.tah_intern.Tile import Tile
from django.core.exceptions import ObjectDoesNotExist
from struct import pack
import os,stat
from shutil import move
from tempfile import mkstemp
from sys import exit

class Tileset:
  #instance variables:
  #x=None
  #y=None
  #base_z=None
  #layer=None
  #init=False
  ##tiles[z][x][y] points to a tuple (<Tile>,<fullfilename>)
  #tiles={}

  def __init__(self, layer=None, base_z=None, x=None, y=None):
    self.layer = layer
    self.base_z = base_z
    self.x = x
    self.y = y
    self.tiles = {} # empty set contains no tiles yet
    if layer and base_z!=None and x!=None and y!=None: self.init=True
    else: self.init = False

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
    if t.z == self.base_z+6: return

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


  def get_filename(self, base_tile_path):
    """
       Returns a tuple (path, filename) of a tilesetfile. base_tile_path is the base location of tiles.
       Doesn't check that the file actually exists. 
       Returns (None, None) if the tileset was not inited yet.
    """
    assert (base_tile_path > '')
    if not self.init: return (None, None)
    path = os.path.join(base_tile_path,"%s_%d" % (self.layer.name,self.base_z),"%04d"%(self.x))
    file = "%s_%s" % (self.x, self.y)
    return (path, file)

  def save(self,base_tile_path, userid = 0):
    """
	Returns a tuple: (1,unknown_tiles) (1 being success, unknown_tiles being 
        the number of tiles with unknown blankness, which should always be 0 if 
        the tileset was complete.
        It will be saved under user id 'userid'
    """
    tilepath, tilesetfile = self.get_filename(base_tile_path)
    (tmp_fd,tmpfile) = mkstemp()
    f = os.fdopen(tmp_fd, 'w+b')
    unknown_tiles = 0
    blank_tiles = 0
    FILEVER=1
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
              blank_tiles += 1
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

    if blank_tiles == 1365 and self.base_z == 12:
        # completely blank tileset at z12. Delete existing file and
        # have oceantiles.dat handle this.
        try:
            os.unlink(tmpfile)
            os.unlink(os.path.join(tilepath,tilesetfile))
        except OSError, e:
            # it's ok if the file does not exist
            if e.errno == 2: pass
            else: raise e
    else:
        # finally create path if necessary and move the tmp file to its final location
        if not os.path.isdir(tilepath): os.makedirs(tilepath, 0775)
        os.chmod(tmpfile,0664)
        move(tmpfile, os.path.join(tilepath,tilesetfile))

    return (1,unknown_tiles)
