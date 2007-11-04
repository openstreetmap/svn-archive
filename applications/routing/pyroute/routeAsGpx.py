#!/usr/bin/python
#----------------------------------------------------------------
# routeAsOsm - routes within an OSM file, and generates a
# GPX file containing the result
#
#------------------------------------------------------
# Usage: 
#  routeAsGpx.py [input OSM file] [start node] [end node] [description]
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
#------------------------------------------------------
from route import *
  
def routeToGpx(nodeList, osmData, description=""):
  """Format a route (as list of nodes) into a GPX file"""
  
  output = ''
  output = output + "<?xml version='1.0'?>\n";
  
  output = output + "<gpx version='1.1' creator='pyroute' xmlns='http://www.topografix.com/GPX/1/1' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd'>\n"
    
  output = output + " <rte>\n"
  output = output + "  <name>%s</name>\n" % description
  
  count = 0;
  for i in nodeList:
    output = output + "   <rtept lat='%f' lon='%f'>\n" % ( \
      osmData.nodes[i][0],
      osmData.nodes[i][1])
    output = output + "    <name>%d</name>\n" % count
    output = output + "   </rtept>\n"
    count = count + 1
  output = output + " </rte>\n</gpx>\n"
  
  return(output)

if __name__ == "__main__":
  data = LoadOsm(sys.argv[1])
  
  router = Router(data)
  result, route = router.doRoute(int(sys.argv[2]), int(sys.argv[3]))
  
  try:
    description = sys.argv[4]
  except IndexError:
    description = "(no name)"

  if result == 'success':
    print routeToGpx(route, data, description)
  else:
    sys.stderr.write("Failed (%s)" % result)
