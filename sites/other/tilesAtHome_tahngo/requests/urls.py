
from django.conf.urls.defaults import *
from tah.requests.views import *
from tah.requests.feeds import LatestRequests, LatestRequestsByCategory

# available RSS feeds
request_feeds = {
    'latest': LatestRequests,
    'categories': LatestRequestsByCategory,
}

urlpatterns = patterns('',
    (r'^$', index),
    (r'^show/$', show_first_page),
    (r'^show/uploads/$', show_uploads_page),
    (r'^show/page(?P<page>[0-9]+)/$', show_requests),
    (r'^show/ajax$', show_ajax),
    # an RSS feed for the latest requests
    #(r'^feeds/(?P<url>.*)/$', 'django.contrib.syndication.views.feed', {'feed_dict': request_feeds}),
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
