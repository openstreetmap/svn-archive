from SimpleXMLRPCServer import SimpleXMLRPCServer
from numpy import ones

# Create server
server = SimpleXMLRPCServer(("localhost", 8000))
server.register_introspection_functions()

def altitude_profile_function(route):
    # Just return a list of ones:
    # Can't use numpy for this.
    answer = []
    for point in route:
      point['alt'] = 1
      answer.append(point)

    return answer
server.register_function(altitude_profile_function, 'altitude_profile')

# Run the server's main loop
server.serve_forever()

