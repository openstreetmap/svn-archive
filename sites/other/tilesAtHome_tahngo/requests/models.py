from django.db import models
from django.contrib.auth.models import User
from tah.tah_intern.models import Layer
from tah.requests.manager import RequestManager
#-----------------------------------------------------
class Request(models.Model):
  # x,y are the coordinates of the request tileset
  x = models.PositiveSmallIntegerField()
  y = models.PositiveSmallIntegerField()

  # render tiles from zoom levl min_z through max_z
  min_z = models.PositiveSmallIntegerField(default=12)
  max_z = models.PositiveSmallIntegerField(default=17)

  # list of layers we should render
  layers = models.ManyToManyField(Layer,null=True)

  #status: 0=unhandled, 1=Handed out, 2=Finished
  status = models.PositiveSmallIntegerField(default=0)

  # priority: 1=urgent 3=slow bulk
  priority = models.PositiveSmallIntegerField(default=3)

  # ipaddress, request time, and freeform "src" string of the requester
  ipaddress = models.IPAddressField(blank=True,default="")
  request_time = models.DateTimeField(auto_now_add=True)
  src  = models.CharField(max_length=64, blank=True, default='unknown')

  # renderer/user (=client), client_uuid, and time of last client contact.
  #clientping contains the last "I am still working on it" when active, on upload it indicates the upload time
  clientping_time = models.DateTimeField(blank=True,default="")
  client = models.ForeignKey(User,blank=True,null=True)
  client_uuid = models.PositiveSmallIntegerField(blank=True, default=0)
  #---- end of model definition ----

  # use RequestManager that allows to e.g. select a request "FOR UPDATE"
  objects = RequestManager()

  def __str__(self):
    return "%s|%s|%s|%s" % (self.x,self.y,self.min_z,self.layers_str)

  @property
  def layers_str(self):
    return  ','.join([a['name'] for a in self.layers.values()])

  @property
  def status_str(self):
    return  ['unhandled','active','finished'][self.status]


#-----------------------------------------------------
class Upload(models.Model):
  file = models.FileField(upload_to='Incoming')
  layer = models.ForeignKey(Layer,blank=True,null=True)
  ipaddress = models.IPAddressField(blank=True,default="")
  user_id = models.ForeignKey(User)
  client_uuid = models.PositiveSmallIntegerField(blank=True, default=0)
  #clientping contains the upload it indicates the upload time
  upload_time = models.DateTimeField(auto_now_add=True)
  # priority: 1=urgent 3=slow bulk
  priority = models.PositiveSmallIntegerField(default=3)
  # if we need to do multi-threaded processing this shows locked uploads
  is_locked = models.BooleanField(default=False)

  def __str__(self):
    return str(self.layer)+","+str(self.file)

  class Meta:
    ordering = ['upload_time']
