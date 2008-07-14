from django.conf.urls.defaults import *
from tah.browse.views import *

urlpatterns = patterns('',
    (r'^$', index),
    (r'^slippy/$', slippymap),
    (r'^details/$', tiledetails_base),
    (r'^details/(?P<layername>\w+)/(?P<z>[0-9]+)/(?P<x>[0-9]+)/(?P<y>[0-9]+)/$', tiledetails),
    (r'^MapOf/$', show_map_of),
    (r'^Tiles/(?P<layername>\w+)/(?P<z>[0-9]+)/(?P<x>[0-9]+)/(?P<y>[0-9]+).png$', serve),
)
