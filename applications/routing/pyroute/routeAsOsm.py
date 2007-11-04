#!/usr/bin/python
#----------------------------------------------------------------
# routeAsOsm - routes within an OSM file, and generates an
# OSM file containing the result
#
#------------------------------------------------------
# Usage: 
#  routeAsOsm.py [input OSM file] [start node] [end node]
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
  
def routeToOsm(nodeList, osmData, description=""):
  """Format a route (as list of nodes) into an OSM file"""
  output = ''
  output = output + "<?xml version='1.0' encoding='UTF-8'?>\n";
  output = output + "<osm version='0.5' generator='pyroute'>\n";
  
  for i in nodeList:
    output = output + " <node id='%d' lat='%f' lon='%f' />\n" % ( \
      i,
      osmData.nodes[i][0],
      osmData.nodes[i][1])

  output = output + " <way id='1'>\n"
  for i in nodeList:
    output = output + "  <nd ref='%d' />\n" % i
    
  output = output + "  <tag k='route_display' v='yes' />\n"
  output = output + "  <tag k='name' v='%s' />\n" % description
  output = output + " </way>\n"
  output = output + "</osm>"
  
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
    print routeToOsm(route, data, description)
  else:
    sys.stderr.write("Failed (%s)" % result)
