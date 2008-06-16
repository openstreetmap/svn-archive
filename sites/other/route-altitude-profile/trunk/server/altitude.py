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
    interpolateRoute(route, 100)
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
  # The file name of an srtm tile corresponds to the coordinate
  # of its bottom left element. However, the files start at top left. 

  # The top left element (row 1, col 1) has index 0, the left most element
  # (col 1) of the second row has index 1200, etc. 
  # The bottom left element (row 1200, col 1) of a tile therefore has index
  # 1199 * 1200 = 1 438 800

  # First we determine the tile and its offset, pos0:
  lat0 = floor(lat)
  lon0 = floor(lon)
  pos0 = (lat0 * 360 + lon0) * 1200 * 1200
  
  # Then we determine the position within the tile
  lat_pos = (1199./1200 - (lat - floor(lat))) * 1200 * 1200
  lon_pos = (lon - floor(lon)) * 1200

  # We then round lat_pos and lon_pos in case the coordinate was somewhere
  # between the grid points.

  lat_pos = round(lat_pos / 1200) * 1200
  lon_pos = round(lon_pos)

  # Add them up to get the position:
  pos = pos0 + lat_pos + lon_pos 

  return int(pos) 

def addPointsToPair(points, n):
  # Adds n points between a pair of points (a and b).
  [a,b] = points

  # id fields must be corrected:
  id_start = a['id']
  b['id'] = b['id'] + n

  for i in range(n):
    points.insert(-1,{\
      'id' : id_start + i + 1,
      'lat' : a['lat'] + (b['lat'] - a['lat']) / (n + 1) * (i + 1),
      'lon' : a['lon'] + (b['lon'] - a['lon']) / (n + 1) * (i + 1)
    })
  
  return points

def getRouteLength(route):
    # Returns the length of the route in kilometers.
    length = 0

    for i in range(len(route) - 1):
      origin = [route[i]['lat'], route[i]['lon']]
      destination = route[i + 1]['lat'], route[i + 1]['lon']
      length += distance.distance(origin, destination).kilometers

    return length

def getNumberOfExtraPoints(pair, min_res):
    origin = [pair[0]['lat'], pair[0]['lon']]
    destination = pair[1]['lat'], pair[1]['lon']
    length = distance.distance(origin, destination).kilometers

    return int(floor(length / min_res) - 1)

def interpolateRoute(route, n):
  # Adds extra points to the route, so that no points in the route
  # are more than length_of_route / n apart. A higher value for n 
  # means a higher resolution.
  
  # First calculate the length of the route
  route_length = getRouteLength(route)
  min_res = route_length / n 

  # For each pair of points, add points in between if necessary:
  for i in range(len(route) - 1):
    pair = [route[i], route[i+1]]
    number_of_extra_points = getNumberOfExtraPoints(pair, min_res)
    addPointsToPair(pair, number_of_extra_points)
    for j in range(len(pair) - 2): 
      route.insert(-1, pair[j + 1])    

    i = i + len(pair) - 2

# Whether testing or runnins, always connect to the database

db = connectToDatabase(database)

if __name__ == '__main__':
  # Create server
  server = SimpleXMLRPCServer(("", 8000))
  server.register_introspection_functions()

  server.register_function(altitude_profile_function, 'altitude_profile')
  server.register_function(altitude_profile_gchart_function, 'altitude_profile_gchart')
  
  try:
    print 'started server...'
    server.serve_forever()
  except KeyboardInterrupt:
    print '^C received, shutting down server'
    server.socket.close()

