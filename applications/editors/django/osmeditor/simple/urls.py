from django.conf.urls.defaults import *

urlpatterns = patterns('osmeditor.simple.views',
    (r'^$', 'home'),
    (r'^login/$', 'login'),
    (r'^(?P<type>node|way|relation)/(?P<id>\d+)/', 'load'),
)    
