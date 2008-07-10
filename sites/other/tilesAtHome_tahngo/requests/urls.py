from django.conf.urls.defaults import *
from tah.requests.views import *

urlpatterns = patterns('',
    (r'^$', index),
    (r'^show/$', show_first_page),
    (r'^show/page(?P<page>[0-9]+)/$', show_requests),
    (r'^create/$', create),
    (r'^create/changedTiles/$', request_changedTiles),
    (r'^take/$', take),
    (r'^upload/$',upload_request),
    (r'^upload/go_nogo',upload_gonogo),
    (r'^stats/munin/(?P<status>\w+)/$',stats_munin_requests),
)
