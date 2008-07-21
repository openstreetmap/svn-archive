from django.db import models
from django.core.exceptions import ObjectDoesNotExist

class Layer(models.Model):
  #name describes layer and names the directory in which they are
  name = models.CharField(maxlength=30)
  decription = models.CharField(maxlength=250)
  transparent = models.BooleanField(default=False)
  max_z = models.PositiveSmallIntegerField(default=17)
  min_z = models.PositiveSmallIntegerField(default=12)
  default = models.BooleanField(default=False)

  def __str__(self):
    #string representation of a layer
    return self.name

  class Admin:
    # we want to edit in the adin interface
    pass

class Tile(models.Model):
  layer = models.ForeignKey(Layer)
  z =  models.PositiveSmallIntegerField();
  x =  models.PositiveSmallIntegerField();
  y =  models.PositiveSmallIntegerField();
  quadtile =  models.PositiveIntegerField(blank=True);
  #blank = 1:land 2:sea
  blankness =  models.PositiveSmallIntegerField();

  def __str__(self):
    return "%s(%d,%d,%d)" % (self.layer,self.z,self.x,self.y)

  def is_valid(self):
    if not self.layer: return False
    if self.x < 0 or self.y < 0 or self.x >=  pow(2, int(self.z)) or self.y >=  pow(2, int(self.z)): return False
    if self.z <  self.layer.min_z or self.z >  self.layer.max_z: return False
    # this is a valid tile
    return True
#-----------------------------------------------------------------------------
class Blank(Tile):
  #on saving a new blank tile, we calculate its quadtile number
  def save(self):
    #quadtile from x,y
    x=self.x
    y=self.y
    self.quadtile=0;
    for i in range(0,15):
        self.quadtile <<= 1;
	if (x & 0x8000): self.quadtile =  self.quadtile | 1
	x<<=1;
        self.quadtile <<= 1;
	if (y & 0x8000): self.quadtile =  self.quadtile | 1
	y<<=1;
    super(Blank, self).save() # Call the "real" save() method.

  def __str__(self):
    #string representation of a blank tile
    b = int(self)
    if b ==1: return 'land'
    elif b==2: return 'sea'
    else: return 'unknown'

  def __int__(self):
    #int representation of a blank tile, search up reciprocally
    try:
      b = Blank.objects.get(layer=self.layer,z=self.z,x=self.x,y=self.y)
      return b.blankness
    except ObjectDoesNotExist:
      # stop looking below zoom level 2
      if self.z == 1: return 0
      else: 
	b = Blank(layer=self.layer)
        b.z = int(self.z)-1
        b.x = int(self.x) / 2
        b.y = int(self.y) / 2
        return int(b)
    
  def set_blank(self,blanktype):
    #if the blanktype is already ok, then do nothing
    if int(self) == blanktype: return 0
    Blank.objects.create(layer=self.layer, z=self.z, x=self.x, y=self.y,blankness=blanktype)

  class Admin:
    # we want to edit in the adin interface
    pass

#-----------------------------------------------------------------------------
class Settings(models.Model):
  #name describes layer and names the directory in which they are
  name = models.CharField(maxlength=30,unique=True)
  value = models.CharField(maxlength=255)

  def __str__(self):
    return self.name
  
  def getSetting(self, name):
    try: s = Settings.objects.get(name__iexact=name)
    except Settings.DoesNotExist: return None
    return s.value

  def setSetting(self, name, value):
    return Settings.objects.create(name= name, value= value)

  class Admin:
    # we want to edit in the adin interface
    pass
