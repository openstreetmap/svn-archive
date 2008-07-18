from os import curdir, sep
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import urllib
import altitudeprofile_pb2

from mod_python import apache

# Define example routes.
# See http://wiki.openstreetmap.org/index.php/Route_Altitude_Profile_Example_Routes
# Convert with:
# s/  <point id="\(.*\)" lat="\(.*\)" lon="\(.*\)" \/>/{'id' : \1, 'lat' : \2, 'lon' : \3},

# Or if you have a list like:
# 49.407810, 8.681080
# You do: 
# %s/\(.*\), \(.*\)/{'id' : , 'lat' : \1, 'lon' : \2}, 
# And add the id numbers

route1 = [\
{'id' : 1, 'lat' : 49.407810, 'lon' : 8.681080},
{'id' : 2, 'lat' : 49.407770, 'lon' : 8.684210},
{'id' : 3, 'lat' : 49.408950, 'lon' : 8.692368},
{'id' : 4, 'lat' : 49.407040, 'lon' : 8.692670},
{'id' : 5, 'lat' : 49.406880, 'lon' : 8.693919},
{'id' : 6, 'lat' : 49.407620, 'lon' : 8.694270},
{'id' : 7, 'lat' : 49.413360, 'lon' : 8.692300},
{'id' : 8, 'lat' : 49.414800, 'lon' : 8.692110},
{'id' : 9, 'lat' : 49.414730, 'lon' : 8.693110}\
]

routes = [route1]

def demo(req, route, input, output, server):
  # ULR of altitude server:
  if server == "pg":
    #url_server_root = 'http://altitude-pg/';
    url_server_root = 'http://altitude-pg.sprovoost.nl/';
  elif server == "app":
    #url_server_root = 'http://localhost:8080/';
    url_server_root = 'http://altitude.sprovoost.nl/';
  else:
    exit()

  # Determine route number (substract 1 because arrays start 
  # at 0): 
  route_number = int(route) - 1

  input_type = input
  output_type = output
  
  if input_type == "" or output_type == "":
    exit()

  f = fetchResult(url_server_root, input_type, routes[route_number], output_type)      
  
  if output_type == "xml":
  
    s = f.read()
    f.close()

    req.content_type = 'text/xml'

    req.write(s)
  
  elif output_type == "gchart":
  
    s = f.read()
    f.close()
    
    req.content_type = 'text/html'

    req.write('<html>')
    req.write('<head></head>')
    req.write('<body>')
    req.write('<img src="' + s +  '" alt="Altitude profile"/>')
  
  #return apache.OK

def fetchResult(url_server_root, input_type, route, output_type):
  if input_type == "protobuf":  
    # Prepare route for transmission (Protocol Buffer)
    route_pb = altitudeprofile_pb2.Route()

    for p in route:
      point = route_pb.point.add()
      point.lat = p['lat']
      point.lon = p['lon']

    route_pb_string = route_pb.SerializeToString()

    # Get route altitude profile:
                

    return urllib.urlopen(url_server_root + "profile/xml/protobuf/", route_pb_string)
    
  elif input_type == "xml":
    # Let's make an xlsRouteGeometry object...
    route_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    route_xml += "<xls:XLS xmlns:xls=\"http://www.opengis.net/xls\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:gml=\"http://www.opengis.net/gml\" version=\"1.1\" xsi:schemaLocation=\"http://www.opengis.net/xls http://schemas.opengis.net/ols/1.1.0/RouteService.xsd\">\n"
    route_xml += "  <xls:RouteGeometry>\n"
    route_xml += "    <gml:LineString srsName=\"EPSG:4326\">\n"
    
    for point in route:
      route_xml += "      <gml:pos>"
      route_xml += str(point['lon']) + " " + str(point['lat'])
      route_xml += "</gml:pos>\n"          
      
    route_xml += "    </gml:LineString>\n"
    route_xml += "  </xls:RouteGeometry>\n"
    route_xml += "</xls:XLS>"

    return urllib.urlopen(url_server_root + "profile/" + output_type + "/xml/", route_xml)

if __name__ == '__main__':
  None 
