from django.contrib import admin
from tah.requests.models import Request,Upload


class RequestAdmin(admin.ModelAdmin):
     pass
admin.site.register(Request, RequestAdmin)
#---------------------------------------------
class UploadAdmin(admin.ModelAdmin):
     pass
admin.site.register(Upload, UploadAdmin)
#---------------------------------------------
