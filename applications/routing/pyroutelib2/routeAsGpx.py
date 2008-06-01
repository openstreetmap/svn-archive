#!/usr/bin/python
#----------------------------------------------------------------
# routeAsOsm - routes within an OSM file, and generates a
# GPX file containing the result
#
#------------------------------------------------------
# Usage: 
#  routeAsGpx.py [input OSM file] [start node] [end node] [transport] [description]
#------------------------------------------------------
# Copyright 2007, Oliver White
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#------------------------------------------------------
# Changelog:
#  2007-11-04  OJW  Created
#  2007-11-05  OJW  Multiple forms of transport
#------------------------------------------------------
from route import *
  
def routeToGpx(lat1,lon1,lat2,lon2, transport, description="", style="track"):
  """Format a route (as list of nodes) into a GPX file"""
  data = LoadOsm(transport)

  node1 = data.findNode(lat1,lon1)
  node2 = data.findNode(lat2,lon2)

  router = Router(data)
  result, route = router.doRoute(node1, node2)
  if result != 'success':
    return

  output = ''
  output = output + "<?xml version='1.0'?>\n";
  
  output = output + "<gpx version='1.1' creator='pyroute' xmlns='http://www.topografix.com/GPX/1/1' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd'>\n"
    
  if(style == 'track'):
    output = output + " <trk>\n"
    output = output + "  <name>%s</name>\n" % description
    output = output + "  <trkseg>\n"
    count = 0;
    for i in route:
      node = data.rnodes[i]
      output = output + "   <trkpt lat='%f' lon='%f'>\n" % ( \
        node[0],
        node[1])
      output = output + "   </trkpt>\n"
      count = count + 1
    output = output + "  </trkseg>\n  </trk>\n</gpx>\n"

  elif(style == 'route'):
    output = output + " <rte>\n"
    output = output + "  <name>%s</name>\n" % description
    
    count = 0;
    for i in route:
      node = data.rnodes[i]
      output = output + "   <rtept lat='%f' lon='%f'>\n" % ( \
        node[0],
        node[1])
      output = output + "    <name>%d</name>\n" % count
      output = output + "   </rtept>\n"
      count = count + 1
    output = output + " </rte>\n</gpx>\n"
  
  return(output)

if __name__ == "__main__":
  
  print routeToGpx(52.552394,-1.818763, 52.563368,-1.818291, "cycle", "myroute", 'track')


  if(0):
    try:
      pass
      #sys.argv[1]
    except IndexError:
      # Not enough argv[]s
      sys.stderr.write("Usage: routeAsGpx.py ...\n")
