import sys,os
sys.path.insert(0, "/home/spaetz")
os.environ['DJANGO_SETTINGS_MODULE'] = "tah.settings"
from time import clock, time
from tah.tah_intern.Tileset import Tileset
from tah.tah_intern.Tile import Tile
from tah.tah_intern.models import Settings,Layer

class LegacyTileset(Tileset):
  leg_basetiledir='/var/www/osm/Tiles/'


  def get_blank(self,tile):
    #Legacy layer numbers in blank db:
    legacy_layer_num={'tile':1,'maplint':3, 'lowzoom':4, 'captionless':6, 'caption':7}
    if not legacy_layer_num.has_key(tile.layer.name):
      print "Unknown layer %s!" % tile.layer.name
      return 0;
    layer_num = legacy_layer_num[tile.layer.name]
    if tile.z == 0:
      # recursion abortion here, return unknown
      return 0
    try:
      cmd = "mysql -utahro tah -BN -e 'SELECT type FROM tiles_blank WHERE layer=%d and z=%d and x=%d and y=%d;'" % (layer_num,tile.z,tile.x,tile.y)
      #print cmd
      b = int(os.popen(cmd).read())
    except ValueError: 
      #Nothing found here, try one level higher
      tile.z -= 1
      tile.x //= 2
      tile.y //= 2
      print "Get_blank recurse upwards (%d,%d,%d)" % (tile.z,tile.x,tile.y)
      self.get_blank(tile)
    #print "get_blank (%d,%d,%d) returns %d" % (tile.z,tile.x,tile.y,b)
    return b

  def convert (self,layername,base_z,base_x,base_y,base_tile_path):
    """
	  Take all tile files from the old one file-per-tile system
	  that belong to a tileset and save it in the new format.
    """
    if not base_z in [0,6,12]: return (0,"Invalid base zoom level.")
    starttime = (time(),clock())    
    layer = Layer.objects.get(name=layername)
    if not layer: return (0,"Unknown layer.")
    # cache if the layer is transparent
    layer_transparent = layer.transparent
    print "Layer transparency is: %s" % layer_transparent
    files  = 0
    blanks = 0

    for z in range(base_z,base_z+6):
      for y in range(base_y*pow(2,z-base_z),(1+base_y)*pow(2,z-base_z)):
        for x in range(base_x*pow(2,z-base_z),(1+base_x)*pow(2,z-base_z)):
          fname = os.path.join(self.leg_basetiledir,layer.name,"%02d"%(z),"%03d"%(x//1000),"%.3d"%(x),"%03d"%(y//1000),"%.3d.png"%(y))
          t = Tile(layer,z,x,y)
          if os.path.isfile(fname): 
            #print "%d %d %d %s" % (z,x,y,fname)
            self.add_tile(t,fname)
            files += 1
          else: 
            if layer_transparent: 
              b = 3
            else:
              b = self.get_blank(t)
            #print "%d %d %d must be blank (%d)" % (z,x,y,b)
            blanks += 1
            t.set_blank(b)
            self.add_tile(t,None)

    savetime = time()
    print "Found %d files and %d blanks (%.1f sec.)." % (files,blanks,savetime-starttime[0])
    if self.save(base_tile_path):
      now=(time(),clock())
      print "Saving took %.1f sec. Total time %.1f sec (CPU: %.1f sec)" % (now[0]-savetime,now[0]-starttime[0],now[1]-starttime[1])
      return (1,'OK')
    else:
      return (0,'Unknown error while saving tileset.')

if __name__ == '__main__':
  base_tile_path = Settings().getSetting(name='base_tile_path')
  #layer = Layer.objects.get(name='maplint')
  #if not layer: sys.exit("Unknown layer.")
  lt = LegacyTileset()
  print str(lt.convert('tile',0,0,0,base_tile_path))
