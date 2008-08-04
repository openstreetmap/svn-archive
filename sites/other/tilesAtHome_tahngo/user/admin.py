from django.contrib import admin
from tah.user.models import TahUser


class TahUserAdmin(admin.ModelAdmin):
     pass

admin.site.register(TahUser, TahUserAdmin)
#---------------------------------------------

