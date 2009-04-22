#custom authentication system that checks OSM users and creates them as needed.
from django.conf import settings
#from django.core.validators import email_re
from django.forms.fields import email_re
from django.contrib.auth.models import User, check_password
from tah.user.models import TahUser
import logging
import urllib2
import re

class OSMBackend:
    def get_user(self, user_id):
        try:
            return User.objects.get(pk=user_id)
        except User.DoesNotExist:
            return None

    def authenticate(self, username=None, password=None):
        #If username is an email address, then try to pull it up
        if email_re.search(username):
            try:
                user = User.objects.get(email__iexact=username)
            except User.DoesNotExist:
                #user not found locally. check OSM
                usernick = self.checkOSMpasswd(username, password)
                if usernick == None:
                   #Not locally known and wrong OSM password too
                   logging.info("Authentication with unknown local username '%s' failed." % username)
                   return None
                logging.info("Imported user '%s' from OpenStreetMap successfully" % username)
                user = self.insertOSMuser(usernick, username, password)
                t = TahUser(user = user)
                t.save()
        else:
            #We have a non-email address username we should try username
            try:
                user = User.objects.get(username=username)
            except User.DoesNotExist:
                return None

        if user.check_password(password):
           if user.is_active:
             return user
           else:
             logging.info("Deactivated user '%s' tried to login" % username)
             return None
        else:
          #user exists, but password was wrong. try to update from OSM
          usernick = self.checkOSMpasswd(user.username, password)
          if usernick == None:
            # OSM auth failed too
             logging.info("user '%s' failed to authenticate (even after checking OSM)" % username)
             return None
          else:
            #OSM auth succeeded, user must have updated password
            logging.info("user '%s' updated password from OSM successfully" % username)
            user.set_password(password)
            user.save()
            return user
    #-------------------------------------------------------------

    def checkOSMpasswd(self,username=None, password=None):
        password_mgr = urllib2.HTTPPasswordMgrWithDefaultRealm()
        top_level_url = "http://www.openstreetmap.org/api/0.5"
        password_mgr.add_password(None, top_level_url, username, password)
        handler = urllib2.HTTPBasicAuthHandler(password_mgr)

        opener = urllib2.build_opener(handler)
        try:
            data = opener.open('http://www.openstreetmap.org/api/0.6/user/details').read() 
        except urllib2.HTTPError, e:
            # if e.code == 404: could be used for more specific action
            # The OSM auth was unsuccessful. Bail out without auth.
            return None 

        #reply line looks like this
        #<user display_name="spaetz" account_created="2007-03-19T19:00:56+00:00">

        p = re.compile('<user display_name="(\w+)"')
	m = p.search(data)

	if (m):
            return m.group(1)
        else:
            # Authentication was OK, but we did not find a valid username.
            # weird!!! construct username from email
            p = re.compile('\W+')
            return p.sub('_', username)


    def insertOSMuser(self, usernick, email, password):
        ###TODO: if user already exists, just update passwd
        user = User.objects.create_user(usernick, email, password)
        user.save()
        return user
