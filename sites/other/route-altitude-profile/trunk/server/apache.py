import sys
import os

sys.path += [os.path.dirname(__file__)]

import web

sys.stdout = sys.stderr 

import altitude

# Postgres database stuff
import database_pg
import pg

class Database:
  def __init__(self):
    self.db = pg.DB(dbname=database_pg.db,host='localhost', user=database_pg.db_user, passwd=database_pg.db_pass)
    
  def fetchAltitude(self, pos):
    sql = self.db.query("SELECT alt FROM altitude WHERE pos = " + str(pos))
    res = sql.getresult()
    return res[0][0]

db = Database()

urls = (
  '/', 'main_page',
  '/profile/(.*)/(.*)/', 'profile_page'
)

class main_page:  
  def GET(self):
     web.header('Content-Type', 'text/html')
     web.output(altitude.page_main_get())

class profile_page:  
  def POST(self, output_format, input_format):
    postdata = '<?xml version=' + web.input()['<?xml version']
    res = altitude.page_profile_post(db, postdata, output_format, input_format)
    header = res[0]
    body = res[1]
    web.header('Content-Type', header)
    web.output(body)


application = web.wsgifunc(web.webpyfunc(urls, globals()))
