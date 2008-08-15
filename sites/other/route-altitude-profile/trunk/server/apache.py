import sys
import os

sys.path += [os.path.dirname(__file__)]

import web
from os import environ

sys.stdout = sys.stderr 

import altitude

# Postgres database stuff
import database_pg
import pg

from urllib import urlopen

class Database:
  def __init__(self):
    self.db = pg.DB(dbname=database_pg.db,host='localhost', user=database_pg.db_user, passwd=database_pg.db_pass)
    
  def fetchAltitude(self, pos):
    sql = self.db.query("SELECT alt FROM altitude WHERE pos = " + str(pos))
    res = sql.getresult()
    return res[0][0]

class Utils:
  def fetchUrl(self, url):
   f = urlopen(url)
   res = f.read()
   f.close()
   return res

db = Database()
utils = Utils()

urls = (
  '/', 'main_page',
  '/profile/(.*)/(.*)/', 'profile_page',
  '/profile/(.*)', 'profile_page'
)

class main_page:  
  def GET(self):
     web.header('Content-Type', 'text/html')
     web.output(altitude.page_main_get())

class profile_page:  
  def POST(self, output_format, input_format):
    # Determine whether the post request is a protocol buffer or
    # an XML document. (I am sure there is a more elegant way)
    try: 
      postdata =  web.input()['protobuf']
    except:
      try:
        postdata = '<?xml version=' + web.input()['<?xml version']
      except:
        web.header('Content-Type', 'html/txt')
        web.output("Wrong input format")
        #web.internalerror() 
        return
        
    res = altitude.page_profile(db, utils, postdata, output_format, input_format)
    header = res[0]
    body = res[1]
    web.header('Content-Type', header)
    web.output(body)
  
  def GET(self, output_format):
    data = web.input()
    lats = data.lats.split(",")
    lons = data.lons.split(",")
    
    res = altitude.page_profile(db, utils, [lats,lons], output_format, "get")
    header = res[0]
    body = res[1]
    web.header('Content-Type', header)
    web.output(body)


application = web.wsgifunc(web.webpyfunc(urls, globals()))
