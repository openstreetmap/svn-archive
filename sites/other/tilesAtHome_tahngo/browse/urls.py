from django.conf.urls.defaults import *
from tah.browse.views import *

urlpatterns = patterns('',
    (r'^$', index),
    (r'^slippy/$', slippymap),
    (r'^details/(?P<layername>\w+)/(?P<z>[0-9]+)/(?P<x>[0-9]+)/(?P<y>[0-9]+)/$', tiledetails),
    (r'^Tiles/(?P<layername>\w+)/(?P<z>[0-9]+)/(?P<x>[0-9]+)/(?P<y>[0-9]+).png$', serve),
)
