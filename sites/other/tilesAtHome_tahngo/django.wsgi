import os
import sys

sys.path.append('/var/www/tilesAtHome')
os.environ['DJANGO_SETTINGS_MODULE'] = 'tah.settings'

import django.core.handlers.wsgi
application = django.core.handlers.wsgi.WSGIHandler()
