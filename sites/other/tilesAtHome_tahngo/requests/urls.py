from django.conf.urls.defaults import *
from tah.requests.views import *

urlpatterns = patterns('',
    (r'^$', index),
    (r'^show/$', show_first_page),
    (r'^show/uploads/$', show_uploads_page),
    (r'^show/page(?P<page>[0-9]+)/$', show_requests),
    (r'^create/$', create),
    (r'^create/changedTiles/$', request_changedTiles),
    (r'^take/$', take),
    (r'^feedback/$', feedback),
    (r'^upload/$',upload_request),
    (r'^upload/go_nogo',upload_gonogo),
    (r'^expireTiles',expire_tiles),
    (r'^stats/munin/(?P<status>\w+)/$',stats_munin_requests),
    (r'^latestClient',show_latest_client_version),
)
