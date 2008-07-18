# Google app engine ingredients:

import cgi
import wsgiref.handlers

from google.appengine.ext import db
from google.appengine.ext import webapp

##### Database #####
class Altitude(db.Model):
  alt = db.IntegerProperty()

import altitude
class Database:
  def fetchAltitude(self, pos):
    return Altitude.get_by_key_name("P" + str(pos)).alt

db = Database()

##### Pages #####
class MainPage(webapp.RequestHandler):
  def get(self):
    self.response.out.write("""
      <html>
        <body>""")
    self.response.out.write(altitude.page_main_get())
    self.response.out.write("""       </body>
      </html>""")

class Profile(webapp.RequestHandler):
  def post(self, output_format, input_format):
    res = altitude.page_profile_post(db, self.request.body, output_format, input_format)
    header = res[0]
    body = res[1]
    
    self.response.out.write(body)

def main():
  application = webapp.WSGIApplication(
                                       [
                                        ('/', MainPage),
                                        (r'/profile/(.*)/(.*)/', Profile)
                                       ],
                                       debug=True)
  wsgiref.handlers.CGIHandler().run(application)

if __name__ == '__main__':
  main() 
