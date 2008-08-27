from django.conf.urls.defaults import *
from tah.user.views import *

urlpatterns = patterns('tah.requests',
    (r'^$', index),
    (r'^show/$', show_user),
    (r'^show/byname/(.+)/$', show_single_user_byname, {'by': 'username'}),
    (r'^show/byid/(.+)/$', show_single_user, {'by': 'pk'}),
    #(r'^login/$', login),
)


