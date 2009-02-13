from django.conf.urls.defaults import *

urlpatterns = patterns('osmeditor.simple.views',
    (r'^$', 'home'),
    (r'^help/$', 'help'),
    (r'^login/$', 'login'),
    (r'^logout/$', 'logout'),
    (r'^api/0.5/(?P<url>.+)', 'api_proxy'),
    (r'^(?P<type>node|way|relation)/(?P<id>\d+)/', 'load'),
)    
