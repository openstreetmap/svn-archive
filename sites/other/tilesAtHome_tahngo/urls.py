from django.conf.urls.defaults import *
from django.views.generic.simple import direct_to_template,redirect_to
from tah.tah_intern.views import show_log

urlpatterns = patterns('',
    (r'^$', direct_to_template, {'template': 'homepage.html'}),
    (r'^Request/', include('tah.requests.urls')),
    (r'^User/', include('tah.user.urls')),
    #(r'^blank/', include('tah.tah_intern.urls_blank')),
    (r'^Browse/', include('tah.browse.urls')),
    (r'^admin/', include('django.contrib.admin.urls')),
    (r'^Log/$', show_log),
    #(r'/accounts/profile/$', 'redirect_to', {'url': '/user/'}),
)
