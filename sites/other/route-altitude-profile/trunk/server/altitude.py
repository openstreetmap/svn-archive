from SimpleXMLRPCServer import SimpleXMLRPCServer
from numpy import ones
from math import floor, ceil
import database
import pg

# Geopy: http://exogen.case.edu/projects/geopy/
from geopy import distance

# http://pygooglechart.slowchop.com/
from pygooglechart import XYLineChart, Axis

##### XML_RPC functions ######

def altitude_profile_function(route):
    answer = []
    for point in route:
      point['alt'] = getAltitude(point['lat'], point['lon'])
      answer.append(point)

    return answer

def altitude_profile_gchart_function(route):
    # First calculate the altitude profile
    profile = altitude_profile_function(route)
    
    # Create gchart
    # http://code.google.com/apis/chart/#line_charts
    # http://pygooglechart.slowchop.com/

    # For the horizontal scale, we need to know the horizontal distance 
    # between the coordinates. First point will have coordinate 0, last 
    # point the sum of all distances.
    
    # y coordinates are the altitudes.
    
    x_coordinates = [0]
    y_coordinates = []  

    for i in range(len(profile)-1):
      x_coordinates.append(x_coordinates[i])

      x_coordinates[i+1] += distance.distance(
        (profile[i]['lat'],profile[i]['lon'] ),
        (profile[i+1]['lat'],profile[i+1]['lon'] )
      ).kilometers
 
      y_coordinates.append(profile[i]['alt'])

    y_coordinates.append(profile[-1]['alt'])

    # Create gchart
    # http://code.google.com/apis/chart/#line_charts
    # http://pygooglechart.slowchop.com/
    chart = XYLineChart(325, 200, 
                        x_range=(0,max(x_coordinates)), y_range=(min(y_coordinates),max(y_coordinates)))
    chart.add_data(x_coordinates)
    chart.add_data(y_coordinates)
    
    chart.set_axis_labels(Axis.BOTTOM, ['0', str(max(x_coordinates))[0:4] + " km"])
    chart.set_axis_labels(Axis.LEFT, [str(min(y_coordinates)) + " m", str(max(y_coordinates)) + " m"])

    # Return gchart url:
    return {'gchart_url' : chart.get_url()}
    
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
  server.register_function(altitude_profile_gchart_function, 'altitude_profile_gchart')
  
  try:
    print 'started server...'
    server.serve_forever()
  except KeyboardInterrupt:
    print '^C received, shutting down server'
    server.socket.close()

