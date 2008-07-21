from django.conf.urls.defaults import *

#filter out irrelevant URL prefixes
urlpatterns = patterns('',
 (r'^tah/', include('tah.urls')),
)
