from google.appengine.ext import db

class Altitude(db.Model):
  pos = db.IntegerProperty()
  alt = db.IntegerProperty()

for c in Altitude.gql("limit 100"):
  c.delete() 
