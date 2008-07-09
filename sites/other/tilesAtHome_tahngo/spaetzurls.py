from django.conf.urls.defaults import *

#filter out irrelevant URL prefixes
urlpatterns = patterns('',
 (r'^~spaetz/', include('tah.urls')),
 (r'^tahngo/', include('tah.urls')),
)
