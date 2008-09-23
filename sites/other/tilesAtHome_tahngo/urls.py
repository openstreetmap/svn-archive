from django.conf.urls.defaults import *
from django.contrib import admin
from django.views.generic.simple import redirect_to
from tah.tah_intern.views import homepage, show_log, show_tile
from tah.browse.MapOf import export_MapOf

admin.autodiscover()

urlpatterns = patterns('',
    (r'^$', homepage),
    (r'^Request/', include('tah.requests.urls')),
    (r'^User/', include('tah.user.urls')),
    (r'^Browse/', include('tah.browse.urls')),
    (r'^admin/(.*)', admin.site.root),
    (r'^Log/$', show_log),
    (r'^MapOf/', export_MapOf),
    (r'Tiles/(?P<layername>\w+)/(?P<z>\d+)/(?P<x>\d+)/(?P<y>\d+)(.png)?/$',show_tile),
    (r'^Log/Requests/Recent/$', redirect_to, {'url': '/Request/show/'}), #temporary redirect
    (r'Requests/Version\.php$', redirect_to, {'url': '/tahngo/Request/latestClientVersion/'}), #temporary redirect
    #(r'/accounts/profile/$', 'redirect_to', {'url': '/user/'}),
)
