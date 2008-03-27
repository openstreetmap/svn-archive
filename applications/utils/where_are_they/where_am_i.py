#!/usr/bin/python
#					Where am I?
#					-----------
#
# An OpenStreetMap powered python script to answer the question.
#
# Supports the following:
#   What main road am I on?
#     lat=...,long=....,dist=....,road
#   What main roads am I near?
#     lat=...,long=....,dist=....,roads
#   What place am I in?
#     lat=...,long=....,dist=....,place
#   What places am I near?
#     lat=...,long=....,dist=....,places
#
# Can output as html (format=html - default) or xml (format=xml)
#  or OSM XML (format=osm) or GPX XML (format=gpx)
#
# Requires that Plant.OSM has been loaded into a local postgres database,
#  using planetosm-to-db.pl (currently needs OSM 0.5)
#
# See http://wiki.openstreetmap.org/index.php/Where_Are_They
#
# GPL
#
# Nick Burch
#		v0.08  (27/03/2008)

import sys
from osm_io_helper import hasOpt, getOpt, hasAnyOpts, printHTTPHeaders, renderResults
from geo_helper import calculate_distance_and_bearing
from mini_osm import mini_osm_pgsql

# What highway and class options we want
segment_types = [ "highway", "class" ]
main_roads = [ "motorway", "trunk", "primary", "secondary" ]

# Where are they?
lat = None
long = None
if hasOpt("lat"):
	lat = getOpt("lat")
if hasOpt("latitude"):
	lat = getOpt("latitude")
if hasOpt("long"):
	long = getOpt("long")
if hasOpt("longitude"):
	long = getOpt("longitude")

# Ensure we turn lat/long of "" into None
if (not lat == None) and (not len(lat)):
	lat = None
if (not long == None) and (not len(long)):
	long = None

# Do they want HTML, XML, or OSM?
formats = [ "html", "xml", "osm", "gpx" ]
format = ""
if hasOpt("format"):
	format = getOpt("format")
if not formats.__contains__(format):
	format = formats[0]

# Do the HTTP headers
printHTTPHeaders(format)

# What did they request?
type = 'help'
subtype = None
single = 0
if hasOpt("type"):
	type = getOpt("type")
if hasOpt("road"):
	type = "road"
	single = 1
if hasOpt("roads"):
	type = "road"
if hasOpt("place"):
	type = "node"
	subtype = ("place",None)
	single = 1
if hasOpt("places"):
	type = "node"
	subtype = ("place",None)
if hasOpt("node_type") and not getOpt("node_type") == '':
	type = "node"
	if hasOpt("node_value") and not getOpt("node_value") == '':
		subtype = (getOpt("node_type"), getOpt("node_value"))
	else:
		subtype = (getOpt("node_type"), None)

# What distance do they want to search over?
distance = 250
if hasOpt("dist"):
	distance = getOpt("dist")
if hasOpt("distance"):
	distance = getOpt("distance")
distance = int(distance)
# Max distance - depends on search type
if type == "node" and not subtype == None:
	# Can afford to allow it to be quite big (500km)
	if distance > 500000:
		distance = 500000
else:
	# Limit to 5km, as will fetch all nodes on way or another (5km)
	if distance > 5000:
		distance = 5000


# Avoid XSS from user string inputs
if not subtype == None:
	new_st0 = subtype[0]
	new_st1 = subtype[1]
	new_st0 = new_st0.replace('<','&gt;')
	if not new_st1 == None:
		new_st1 = new_st1.replace('<','&gt;')
	subtype = (new_st0,new_st1)

# Work out the name for this type
type_name = type
if not subtype == None:
	if subtype[1] == None:
		type_name = subtype[0]
	else:
		type_name = subtype[1]
type_name = type_name[0:1].upper() + type_name[1:]
if not type_name[-1:] == 's':
	if type_name[-1:] == 'y':
		type_name = type_name[0:-1] + "ie"
	type_name = type_name + 's'

# Connect to the database
miniosm = mini_osm_pgsql()

# ##########################################################################

def displayError(message):
	global format
	global type
	global subtype

	global lat
	global long
	global distance

	if format == "xml" or format == "osm" or format == "gpx":
		print '<?xml version="1.0" encoding="UTF-8"?>'
		print '<error>%s</error>' % message
	if format == "html":
		title = "Error - %s" % message
		if not hasAnyOpts():
			title = "Where Am I?"

		if lat == None:
			lat = ""
		if long == None:
			long = ""

		print "<html><head><title>%s</title></head><body>" % title
		print "<h1>%s</h1>" % title
		print "<h3>More Information:</h3>"
		print "<p>For more information, see <a href='http://wiki.openstreetmap.org/index.php/Where_Are_They'>http://wiki.openstreetmap.org/index.php/Where_Are_They</a>.</p>"
		print "<form method='get'>"
		print "<h3>Where to search:</h3>"
		print "<p><label for='lat'>Latitude:</label> <input type='text' name='lat' value='%s' ></p>" % lat
		print "<p><label for='long'>Longitude:</label> <input type='text' name='long' value='%s' /></p>" % long
		print "<p><label for='dist'>Distance:</label> <input type='text' name='dist' value='%s' /> meters (max 5000, or 500,000 when doing node type search)</p>" % distance
		print "<h3>What to search for:</h3>"
		print "<p><input type='checkbox' name='roads' checked='yes' />Find Roads</p>"
		print "<p><i>or</i> <input type='checkbox' name='places' />Find Places</p>"
		print "<p><i>or</i> <label for='node_type'>Node Type:</label> <input type='text' name='node_type' /><br />"
		print "   <i>and</i> <label for='node_value'>Node Type Value:</label> <input type='text' name='node_value' /></p>"
		print "<h3>Output Format:</h3>"
		print "<p><select name='format'>"
		print "  <option value='html' selected>HTML</option>"
		print "  <option value='xml'>XML (simple)</option>"
		print "  <option value='osm'>OSM</option>"
		print "  <option value='gpx'>GPX (waypoints)</option>"
		print "</select></p>"
		print "<h3>Perform search:</h3>"
		print "<p><input type='submit' value='search' /></p>"
		print "</form>"
		print "</body></html>"

	sys.exit()

def displayResults(type,objects):
	global format
	global lat
	global long
	title = "%s near to %f,%f" % (type,lat,long)
	renderResults(type,objects,format,title,"distbearing")

# ##########################################################################

def sortByDistance(x,y):
	if x["distance"] == y["distance"]:
		return 0
	if x["distance"] > y["distance"]:
		return 1
	return -1

# ##########################################################################

# Check we have lat and long
if lat == None:
	displayError("Latitude must be supplied")
if long == None:
	displayError("Longitude must be supplied")
lat = float(lat)
long = float(long)

# Do the work
if type == "road":
	# We want to know about nodes, segments and ways
	# So, go and fetch them for our area
	nodes = miniosm.getNodesInArea(lat,long,distance)

	ways = {}
	if len(nodes):
		ways = miniosm.getWaysForNodes(nodes)

	# Push interesting tags down as main keys
	interesting_tags = ["name","ref"] + segment_types
	miniosm.splatTagsOntoObjects(ways, interesting_tags)

	# Grab just the ways of interest
	highways = miniosm.filterWaysByTags(ways,nodes,segment_types,main_roads)

	# Calculate the distance from the segment waypoints
	miniosm.calculateDistanceToWays(lat,long,highways,nodes)
	highways.sort(sortByDistance)

	# Sort out the type, and only have one with each name+ref
	seen_ways = {}
	want_highways = []
	for way in (highways):
		tup = (way["name"],way["ref"])
		if not seen_ways.has_key(tup):
			seen_ways[tup] = way

			# Now do type
			type = None
			for tag in (segment_types):
				if not way[tag] == None:
					type = way[tag]
			way["type"] = type
			want_highways.append(way)

	# All done, display
	if len(want_highways) == 0:
		displayResults("Roads", want_highways)
	elif single:
		displayResults("Road", [want_highways[0]])
	else:
		displayResults("Roads", want_highways)

elif type =="node":
	# We only need to get nodes
	# When we do, filter by tag name, and optionally also tag value
	nodes = miniosm.getNodesInAreaWithTag(lat,long,distance,subtype[0],subtype[1])

	if len(nodes) == 0:
		displayResults(type_name, [])
		sys.exit(0)

	# Push the tags that are interesting down to the nodes
	miniosm.splatTagsOntoObjects(nodes, ["name",subtype[0]])

	# Sort these nodes by their distance
	for node in (nodes.values()):
		dist_bearing = calculate_distance_and_bearing(lat,long,node["lat"],node["long"])
		node["distance"] = dist_bearing[0]
		node["bearing"] = dist_bearing[1]
		# Also splat into type
		node["type"] = node[subtype[0]]
	node_array = nodes.values()
	node_array.sort(sortByDistance)

	# All done, display
	if len(node_array) == 0:
		displayResults(type_name, [])
	elif single:
		displayResults(type_name, [node_array[0]])
	else:
		displayResults(type_name, node_array)
else:
	displayError("No search type given")
