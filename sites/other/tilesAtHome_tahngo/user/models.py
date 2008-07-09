from django.db import models
from django.contrib.auth.models import User

class TahUser(models.Model):
    renderedTiles = models.PositiveIntegerField(default=0)
    kb_upload = models.PositiveIntegerField(default=0)
    user = models.ForeignKey(User, unique=True)

    def __str__(self):
      return str(self.user)

    class Admin:
        pass