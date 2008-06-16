import xmlrpclib
from os import curdir, sep
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer

# Define example routes.
# See http://wiki.openstreetmap.org/index.php/Route_Altitude_Profile_Example_Routes
# Convert with:
# s/  <point id="\(.*\)" lat="\(.*\)" lon="\(.*\)" \/>/{'id' : \1, 'lat' : \2, 'lon' : \3},

route1 = [\
  {'id' : 1, 'lat' : -37.817460, 'lon' : 144.967450},
  {'id' : 2, 'lat' : -37.806643, 'lon' : 144.962394}
]

route2 = [\
  {'id' : 1, 'lat' : -37.794528, 'lon' : 144.989826},
  {'id' : 2, 'lat' : -37.795407, 'lon' : 145.000048},
  {'id' : 3, 'lat' : -37.791279, 'lon' : 145.013591},
  {'id' : 4, 'lat' : -37.791322, 'lon' : 145.029184},
  {'id' : 5, 'lat' : -37.790325, 'lon' : 145.039400},
  {'id' : 6, 'lat' : -37.790672, 'lon' : 145.049962},
  {'id' : 7, 'lat' : -37.785138, 'lon' : 145.060183},
  {'id' : 8, 'lat' : -37.783656, 'lon' : 145.069780},
  {'id' : 9, 'lat' : -37.780077, 'lon' : 145.078862},
  {'id' : 10, 'lat' : -37.779669, 'lon' : 145.094697},
  {'id' : 11, 'lat' : -37.781045, 'lon' : 145.098131},
  {'id' : 12, 'lat' : -37.786668, 'lon' : 145.101099},
  {'id' : 13, 'lat' : -37.790987, 'lon' : 145.105694},
  {'id' : 14, 'lat' : -37.793852, 'lon' : 145.109749},
  {'id' : 15, 'lat' : -37.797316, 'lon' : 145.123297},
  {'id' : 16, 'lat' : -37.797042, 'lon' : 145.129311},
  {'id' : 17, 'lat' : -37.798220, 'lon' : 145.141970}
]

route3 = [\
 {'id' : 1, 'lat' : -37.757051, 'lon' : 145.588828},
 {'id' : 2, 'lat' : -37.755150, 'lon' : 145.588350},
 {'id' : 3, 'lat' : -37.753840, 'lon' : 145.588630},
 {'id' : 4, 'lat' : -37.750630, 'lon' : 145.590980},
 {'id' : 5, 'lat' : -37.749408, 'lon' : 145.591019},
 {'id' : 6, 'lat' : -37.748424, 'lon' : 145.591550},
 {'id' : 7, 'lat' : -37.746884, 'lon' : 145.591616},
 {'id' : 8, 'lat' : -37.745234, 'lon' : 145.592089},
 {'id' : 9, 'lat' : -37.743067, 'lon' : 145.592000},
 {'id' : 10, 'lat' : -37.741730, 'lon' : 145.591536},
 {'id' : 11, 'lat' : -37.740398, 'lon' : 145.592294},
 {'id' : 12, 'lat' : -37.739951, 'lon' : 145.592997},
 {'id' : 13, 'lat' : -37.738844, 'lon' : 145.593178},
 {'id' : 14, 'lat' : -37.736896, 'lon' : 145.593187},
 {'id' : 15, 'lat' : -37.736136, 'lon' : 145.593429},
 {'id' : 16, 'lat' : -37.735095, 'lon' : 145.593588},
 {'id' : 17, 'lat' : -37.734520, 'lon' : 145.594400},
 {'id' : 18, 'lat' : -37.733656, 'lon' : 145.594230},
 {'id' : 19, 'lat' : -37.733499, 'lon' : 145.593709},
 {'id' : 20, 'lat' : -37.732933, 'lon' : 145.593085},
 {'id' : 21, 'lat' : -37.732407, 'lon' : 145.593070},
 {'id' : 22, 'lat' : -37.731918, 'lon' : 145.592466},
 {'id' : 23, 'lat' : -37.730634, 'lon' : 145.592464},
 {'id' : 24, 'lat' : -37.730296, 'lon' : 145.592879},
 {'id' : 25, 'lat' : -37.729805, 'lon' : 145.592849},
 {'id' : 26, 'lat' : -37.729680, 'lon' : 145.592251},
 {'id' : 27, 'lat' : -37.729440, 'lon' : 145.591970},
 {'id' : 28, 'lat' : -37.728740, 'lon' : 145.592350},
 {'id' : 29, 'lat' : -37.727986, 'lon' : 145.594543},
 {'id' : 30, 'lat' : -37.727315, 'lon' : 145.595498},
 {'id' : 31, 'lat' : -37.725260, 'lon' : 145.596140},
 {'id' : 32, 'lat' : -37.724945, 'lon' : 145.596918},
 {'id' : 33, 'lat' : -37.723526, 'lon' : 145.596857},
 {'id' : 34, 'lat' : -37.722854, 'lon' : 145.595763},
 {'id' : 35, 'lat' : -37.722560, 'lon' : 145.594690},
 {'id' : 36, 'lat' : -37.721411, 'lon' : 145.594441},
 {'id' : 37, 'lat' : -37.718561, 'lon' : 145.595810},
 {'id' : 38, 'lat' : -37.717234, 'lon' : 145.595553},
 {'id' : 39, 'lat' : -37.715411, 'lon' : 145.596968},
 {'id' : 40, 'lat' : -37.714600, 'lon' : 145.597150},
 {'id' : 41, 'lat' : -37.714658, 'lon' : 145.596673},
 {'id' : 42, 'lat' : -37.715133, 'lon' : 145.596102},
 {'id' : 43, 'lat' : -37.716074, 'lon' : 145.592537},
 {'id' : 44, 'lat' : -37.715890, 'lon' : 145.591910},
 {'id' : 45, 'lat' : -37.714920, 'lon' : 145.590900},
 {'id' : 46, 'lat' : -37.714300, 'lon' : 145.589399},
 {'id' : 47, 'lat' : -37.713340, 'lon' : 145.588830},
 {'id' : 48, 'lat' : -37.712090, 'lon' : 145.587100},
 {'id' : 49, 'lat' : -37.711530, 'lon' : 145.587240},
 {'id' : 50, 'lat' : -37.710181, 'lon' : 145.585396},
 {'id' : 51, 'lat' : -37.709587, 'lon' : 145.584916},
 {'id' : 52, 'lat' : -37.709078, 'lon' : 145.583933},
 {'id' : 53, 'lat' : -37.708300, 'lon' : 145.583453},
 {'id' : 54, 'lat' : -37.707649, 'lon' : 145.582418},
 {'id' : 55, 'lat' : -37.707180, 'lon' : 145.582000},
 {'id' : 56, 'lat' : -37.705687, 'lon' : 145.581218},
 {'id' : 57, 'lat' : -37.705231, 'lon' : 145.581301},
 {'id' : 58, 'lat' : -37.704299, 'lon' : 145.581816},
 {'id' : 59, 'lat' : -37.703990, 'lon' : 145.581650},
 {'id' : 60, 'lat' : -37.704010, 'lon' : 145.580850},
 {'id' : 61, 'lat' : -37.703184, 'lon' : 145.580396},
 {'id' : 62, 'lat' : -37.702780, 'lon' : 145.580133},
 {'id' : 63, 'lat' : -37.702760, 'lon' : 145.579432},
 {'id' : 64, 'lat' : -37.702459, 'lon' : 145.578904},
 {'id' : 65, 'lat' : -37.701463, 'lon' : 145.578541},
 {'id' : 66, 'lat' : -37.701160, 'lon' : 145.577460},
 {'id' : 67, 'lat' : -37.700844, 'lon' : 145.577135},
 {'id' : 68, 'lat' : -37.700285, 'lon' : 145.576689},
 {'id' : 69, 'lat' : -37.699702, 'lon' : 145.575396},
 {'id' : 70, 'lat' : -37.700140, 'lon' : 145.573850},
 {'id' : 71, 'lat' : -37.701910, 'lon' : 145.571290}
]

route4 = [\
 {'id' : 1, 'lat' : -30.088850, 'lon' : 145.937740},
 {'id' : 2, 'lat' : -30.094060, 'lon' : 145.936930},
 {'id' : 3, 'lat' : -30.094060, 'lon' : 145.936930},
 {'id' : 4, 'lat' : -30.097138, 'lon' : 145.947483},
 {'id' : 5, 'lat' : -30.097252, 'lon' : 145.947610},
 {'id' : 6, 'lat' : -30.101589, 'lon' : 145.950951},
 {'id' : 7, 'lat' : -30.108497, 'lon' : 145.957300},
 {'id' : 8, 'lat' : -30.134053, 'lon' : 145.978325},
 {'id' : 9, 'lat' : -30.141927, 'lon' : 145.980714},
 {'id' : 10, 'lat' : -30.143489, 'lon' : 145.980673},
 {'id' : 11, 'lat' : -30.145621, 'lon' : 145.979606},
 {'id' : 12, 'lat' : -30.147628, 'lon' : 145.980114},
 {'id' : 13, 'lat' : -30.152628, 'lon' : 145.985230},
 {'id' : 14, 'lat' : -30.661910, 'lon' : 146.403530},
 {'id' : 15, 'lat' : -30.666818, 'lon' : 146.403791},
 {'id' : 16, 'lat' : -30.671185, 'lon' : 146.407458},
 {'id' : 17, 'lat' : -30.677133, 'lon' : 146.417411},
 {'id' : 18, 'lat' : -31.542346, 'lon' : 147.159028},
 {'id' : 19, 'lat' : -31.559080, 'lon' : 147.188810}
]

routes = [route1, route2, route3, route4]

# ULR of altitude server:
server_url = 'http://localhost:8000/';
server = xmlrpclib.Server(server_url);

# HTTP server to serve the example routes
class MyHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/index.html":
            f = open(curdir + sep + "www" + sep + "index.html") 

            self.send_response(200)
            self.send_header('Content-type',	'text/html')
            self.end_headers()
            self.wfile.write(f.read())
            f.close()
            return

        if self.path[:-1] == "/index.html?route=":
            route_number = int(self.path[-1])

            # Get route altitude profile:
            
            result = server.altitude_profile(routes[route_number - 1])
            
            self.send_response(200)
            self.send_header('Content-type',	'text/xml')
            self.end_headers()

            tuple_params = tuple(result)
            self.wfile.write(xmlrpclib.dumps(tuple_params, 'route' + str(route_number)))
            return

        return

if __name__ == '__main__':
    try:
        httpserver = HTTPServer(('', 80), MyHandler)
        print 'started httpserver...'
        httpserver.serve_forever()
    except KeyboardInterrupt:
        print '^C received, shutting down server'
        httpserver.socket.close()
   
