from django.contrib import admin
from tah.tah_intern.models import Settings, Layer


class SettingsAdmin(admin.ModelAdmin):
     pass

admin.site.register(Settings, SettingsAdmin)
#---------------------------------------------
class LayerAdmin(admin.ModelAdmin):
     pass
admin.site.register(Layer, LayerAdmin)
#---------------------------------------------
