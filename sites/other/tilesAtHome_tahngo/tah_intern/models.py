from django.db import models

class Layer(models.Model):
  #name describes layer and names the directory in which they are
  name = models.CharField(max_length=30)
  decription = models.CharField(max_length=250)
  transparent = models.BooleanField(default=False)
  max_z = models.PositiveSmallIntegerField(default=17)
  min_z = models.PositiveSmallIntegerField(default=12)
  default = models.BooleanField(default=False)

  def __str__(self):
    #string representation of a layer
    return self.name

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

#-----------------------------------------------------------------------------
class Settings(models.Model):
  #name describes layer and names the directory in which they are
  name = models.CharField(max_length=30,unique=True)
  value = models.CharField(max_length=255)

  def __str__(self):
    return self.name
  
  def getSetting(self, name):
    try: s = Settings.objects.get(name__iexact=name)
    except Settings.DoesNotExist: return None
    return s.value

  def setSetting(self, name, value):
    s, created = Settings.objects.get_or_create(name= name, value= value)
    return s.value
