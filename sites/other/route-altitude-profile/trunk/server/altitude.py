from SimpleXMLRPCServer import SimpleXMLRPCServer
from numpy import ones
from math import floor, ceil
import database
import pg

##### XML_RPC functions ######

def altitude_profile_function(route):
    # Just return a list of ones:
    # Can't use numpy for this.
    answer = []
    for point in route:
      point['alt'] = getAltitude(point['lat'], point['lon'])
      answer.append(point)

    return answer
    
##### Database functions #####

def connectToDatabase(database):
    return pg.DB(dbname=database.db,host='localhost', user=database.db_user, passwd=database.db_pass)
 
def getAltitude(lat,lon):
  pos = posFromLatLon(lat,lon)
  sql = db.query("SELECT alt FROM altitude WHERE pos = " + str(pos))
  res = sql.getresult()
  return res[0][0]

##### Helper functions ######

def posFromLatLon(lat,lon):
  # First we determine the tile and its offset, pos0:
  lat0 = ceil(lat)
  lon0 = floor(lon)
  pos0 = (-lat0 * 360 + lon0) * 1200 * 1200
  
  # Then we determine the position within the tile
  lat_pos = (ceil(lat) - lat) * 1200 * 1200
  lon_pos = (lon - floor(lon)) * 1200

  # We then round lat_pos and lon_pos in case the coordinate was somewhere
  # between the grid points.

  lat_pos = round(lat_pos / 1200) * 1200
  lon_pos = round(lon_pos)

  # Add them up to get the position:
  pos = pos0 + lat_pos + lon_pos 

  return int(pos) 

# Whether testing or runnins, always connect to the database

db = connectToDatabase(database)

if __name__ == '__main__':
  # Create server
  server = SimpleXMLRPCServer(("localhost", 8000))
  server.register_introspection_functions()

  server.register_function(altitude_profile_function, 'altitude_profile')
  try:
    print 'started server...'
    server.serve_forever()
  except KeyboardInterrupt:
    print '^C received, shutting down server'
    server.socket.close()

