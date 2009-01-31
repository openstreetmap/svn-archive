from django.conf.urls.defaults import *

urlpatterns = patterns('osmeditor.simple.views',
    (r'^$', 'home'),
    (r'^login/$', 'login'),
    (r'^api/0.5/(?P<url>.+)', 'api_proxy'),
    (r'^(?P<type>node|way|relation)/(?P<id>\d+)/', 'load'),
)    
