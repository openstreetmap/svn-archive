# from numpy import ones
from math import floor, ceil

# Geopy: http://exogen.case.edu/projects/geopy/
from geopy import distance, util

# http://pygooglechart.slowchop.com/
from pygooglechart import XYLineChart, Axis

from xml.dom import minidom

##### Pages ######
def page_main_get():
  return '<p>Welcome! Go to <a href="http://sprovoost.nl/category/gsoc/">my blog</a> to learn more.</p>'
  
def page_profile(db, utils, data, output_format, input_format):
  # Extract the route:
  route = []
  if input_format == "protobuf":
    # Doesn't work in app egine yet and completely untested
    route_pb = altitudeprofile_pb2.Route()
    route_pb.ParseFromString(data)

    for point in route_pb.point:
      route += {'lat' : point.lat, 'lon' : point.lon} 

  elif input_format == "xml":
    dom = minidom.parseString(data)
    points = dom.getElementsByTagName('gml:pos') 

    for p in points:
      point = util.parse_geo(p.firstChild.data)
      route.append({'lat' : point[1], 'lon' : point[0]})
  
  elif input_format == "get":
    # Transpose would be more elegant here
    for i in range(len(data[0])):
      route.append({'lat' : float(data[0][i]), 'lon' : float(data[1][i])})

  else:
    # Some sort of error; we're under attack! :-)
    route = []

  # Find out what the desired output is
  if output_format == "gchart":
    url = altitude_profile_gchart(db, route)
    fig = utils.fetchUrl(url)

    return ['image/png', fig]

  elif output_format == "gchart_url":
    url = altitude_profile_gchart(db, route)
    return ['text/html', url]
  
  elif output_format == "xml":
    profile = altitude_profile(db, route)
    # Now return a 'nice' XML document with the result
    xml = '<?xml version="1.0" encoding="UTF-8"?>'
    xml += '<xls:XLS xmlns:xls="http://www.opengis.net/xls" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml" version="1.1" xsi:schemaLocation="http://www.opengis.net/xls http://schemas.opengis.net/ols/1.1.0/RouteService.xsd">'
    xml += '  <xls:RouteGeometry>'
    xml += '    <gml:LineString srsName="EPSG:4326">'
    for point in route:
      xml += '      <gml:pos>' + str(point['lon']) + " " + str(point['lat']) + " " + str(point['alt']) + '</gml:pos>'

    xml += '    </gml:LineString>'
    xml += '  </xls:RouteGeometry>'
    xml += '</xls:XLS>'

    return ['text/xml', xml]

def altitude_profile_gchart(db, route):
    # First calculate the altitude profile
    profile = altitude_profile(db, route)
    
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
    chart.set_axis_labels(Axis.LEFT, [str(int(min(y_coordinates))) + " m", str(int(max(y_coordinates))) + " m"])

    # Return gchart url:
    return chart.get_url()
    
def altitude_profile(db, route):
  answer = []
  interpolateRoute(route, 100)
  for point in route:
    point['alt'] = getAltitude(db, point['lat'], point['lon'])
    answer.append(point)
  return answer
    
##### Database functions #####

def getAltitude(db, lat,lon):
  res = posFromLatLon(lat,lon)
  tl = db.fetchAltitude(res[0])
  tr = db.fetchAltitude(res[1])
  bl = db.fetchAltitude(res[2])
  br = db.fetchAltitude(res[3])
  a = res[4]
  b = res[5]  

  return bilinearInterpolation(tl, tr, bl, br, a, b)

##### Helper functions ######

def posFromLatLon(lat,lon):
  # Returns the four positions closest to (lat, lon) and the distance
  # from them along the x and y axis. The output can be used to apply
  # bilinear interpolation.

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

  lat_pos_top = floor(lat_pos / 1200) * 1200
  lat_pos_bottom = ceil(lat_pos / 1200) * 1200
  lon_pos_left = floor(lon_pos)
  lon_pos_right = ceil(lon_pos)

  # Calculate horizontal (a) and vertical (b) relative position:
  a = (lat_pos - lat_pos_top) / 1200
  b = (lon_pos - lon_pos_left)

  # Add them up to get the positions:
  tl = int(pos0 + lat_pos_top + lon_pos_left)
  tr = int(pos0 + lat_pos_top + lon_pos_right)
  bl = int(pos0 + lat_pos_bottom + lon_pos_left)
  br = int(pos0 + lat_pos_bottom + lon_pos_right)

  return [tl, tr, bl, br, a, b] 

def bilinearInterpolation(tl, tr, bl, br, a, b):
  # In the likely case that the coordinate is somewhere between
  # grid points, we will apply bilinear interpolation.

  # http://en.wikipedia.org/wiki/Bilinear_interpolation

  # We will use the simplest formula.

  # return (1.0 - a) * (1.0 - b) * tl + a * (1.0 - b) * tr + (1.0 -a) * b * bl + a * b * br

  b1 = tl
  b2 = bl - tl
  b3 = tr - tl
  b4 = tl - bl - tr + br

  return b1 + b2 * a + b3 * b + b4 * a * b

def addPointsToPair(points, n):
  # Adds n points between a pair of points (a and b).
  [a,b] = points

  for i in range(n):
    points.insert(-1,{\
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

    return max(int(floor(length / min_res) - 1),0)

def interpolateRoute(route, n):
  # Adds extra points to the route, so that no points in the route
  # are more than length_of_route / n apart. A higher value for n 
  # means a higher resolution.
  
  # First calculate the length of the route
  route_length = getRouteLength(route)
  min_res = route_length / n 

  # For each pair of points, add points in between if necessary:
  i = 0
  while(i < len(route) - 1):
    pair = [route[i], route[i+1]]
    number_of_extra_points = getNumberOfExtraPoints(pair, min_res)
    # print "Add " + str(number_of_extra_points) + " extra points after " + str(i)
    addPointsToPair(pair, number_of_extra_points)

    for j in range(len(pair) - 2):
      route.insert(i + j + 1, pair[j+1])

    i = i + len(pair) - 1 


