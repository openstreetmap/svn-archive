import sys,os
# we need to insert the basedir to the python path (strip 2 path components) if we want to directly execute this file
sys.path.insert(0, os.path.dirname(os.path.dirname(sys.path[0])))
os.environ['DJANGO_SETTINGS_MODULE'] = "tah.settings"
from time import clock, time
from tah.tah_intern.Tileset import Tileset
from tah.tah_intern.Tile import Tile
from tah.tah_intern.models import Settings,Layer

class LegacyTileset(Tileset):
  leg_basetiledir='/mnt/agami/openstreetmap/tah/Tiles/'


  def get_blank(self,tile):
    if self.blankness != None: return self.blankness
    #Legacy layer numbers in blank db:
    legacy_layer_num={'tile':1,'maplint':3, 'lowzoom':4, 'captionless':6, 'caption':7}
    if not legacy_layer_num.has_key(tile.layer.name):
      print "Unknown layer %s!" % tile.layer.name
      return 0;
    if tile.z < 12:
      # Don't handle low zoom blanks
      return 0
    try:
          #Lookup in the oceans tile
          #For z12 and above the file will tell us directly what type it is
          (layern, base_z,base_x,base_y) = tile.basetileset()
          f_oceant = open(os.path.join('/var/www/tah/Tiles',"oceantiles_12.dat"),'rb')
          f_oceant.seek((4096*base_y + base_x) >> 2)
          data = ord(f_oceant.read(1));
          #take 2 bits of the char
          bit_off = 3 - (base_x % 4);
          type = (data >> 2*bit_off) & 3
          # map tile type to blankness type used in the server
          blank = [0,2,1,0]
          self.blankness = blank[type]
    except: self.blankness= 0
    return self.blankness

  def convert (self,layer,base_z,base_x,base_y,base_tile_path):
    """
	  Take all tile files from the old one file-per-tile system
	  that belong to a tileset and save it in the new format.
    """
    self.blankness = None
    if not base_z in [0,6,12]: return (0,"Invalid base zoom level.")
    starttime = time()   
    # cache if the layer is transparent
    layer_transparent = layer.transparent
    files  = 0
    blanks = 0

    for z in range(base_z,base_z+6):
      for y in range(base_y*pow(2,z-base_z),(1+base_y)*pow(2,z-base_z)):
        for x in range(base_x*pow(2,z-base_z),(1+base_x)*pow(2,z-base_z)):
          fname = os.path.join(self.leg_basetiledir,layer.name,"%02d"%(z),"%03d"%(x//1000),"%.3d"%(x),"%03d"%(y//1000),"%.3d.png"%(y))
          t = Tile(layer,z,x,y)
          if os.path.isfile(fname): 
            #print "add %d %d %d %s" % (z,x,y,fname)
            self.add_tile(t,fname)
            files += 1
          else:
            blanks += 1
            if not self.tiles.has_key(x) or not self.tiles[x].has_key(y):
              #the tile does not exist yet (not created by subtile blanking)
              if layer_transparent: 
                b = 3
              else:
                b = self.get_blank(t)
              t.set_blank(b)
              #print "add blank (%d) %d %d %d (basetile %s %s %s)" % (b,t.z,t.x,t.y,self.base_z,self.x,self.y)
              self.add_tile(t,None)

    savetime = time()
    if self.save(base_tile_path):
      now=time()
      if base_y%50 == 0:print "%d blanks %d tiles. Saving %.1f sec. Total %.1f sec" % (files,blanks,now-savetime,now-starttime)
      return (1,'OK')
    else:
      return (0,'Unknown error while saving tileset.')

if __name__ == '__main__':
  base_tile_path = Settings().getSetting(name='base_tile_path')
  layer=Layer.objects.get(name='tile')
  for x in range (0,4096):
      for y in range (0,4096):
        if y%50==0: print "Converting: %s (12,%d,%d)" % (layer.name,x,y)
        tilesetfile = os.path.join(base_tile_path,"%s_%d" % (layer.name,12),"%04d" % x, str(x)+'_'+str(y))
        if not os.path.isfile(tilesetfile):
            lt = LegacyTileset()
            lt.convert(layer,12,x,y,base_tile_path)
