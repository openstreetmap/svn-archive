# Google app engine ingredients:

import cgi
import wsgiref.handlers

from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.api import urlfetch

from google.appengine.api import memcache

##### Database #####
class Altitude(db.Model):
  alt = db.IntegerProperty()

import altitude
class Database:
  def fetchAltitude(self, pos):
    key = "P" + str(pos)
    data = memcache.get(key)
    if data is not None:
      return data.alt
    else:
      data = Altitude.get_by_key_name(key)
      memcache.add(key, data, 30)
      return data.alt

class Utils:
  def fetchUrl(self, url):
    res = urlfetch.fetch(url)
    if res.status_code == 200:
      return res.content

db = Database()
utils = Utils()

##### Pages #####
class MainPage(webapp.RequestHandler):
  def get(self):
    self.redirect("http://wiki.openstreetmap.org/index.php/Route_altitude_profiles_SRTM")

class Profile(webapp.RequestHandler):
  def get(self, output_format):
    lats_str = self.request.get("lats")
    lons_str = self.request.get("lons")
    lats = lats_str.split(",")
    lons = lons_str.split(",")

    self.out(altitude.page_profile(db, utils, [lats, lons], output_format, "get"))
    
  def post(self, output_format, input_format):
    self.out(altitude.page_profile(db, utils, self.request.body, output_format, input_format))

  def out(self,res):
    header = res[0]
    body = res[1]

    self.response.headers["Content-Type"] = header
    self.response.out.write(body)


def main():
  application = webapp.WSGIApplication(
                                       [
                                        ('/', MainPage),
                                        (r'/profile/(.*)/(.*)/', Profile),
                                        ('/profile/(.*)', Profile)
                                       ],
                                       debug=True)
  wsgiref.handlers.CGIHandler().run(application)

if __name__ == '__main__':
  main() 
