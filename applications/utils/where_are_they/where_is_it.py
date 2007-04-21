#!/usr/bin/python
#					Where is it?
#					------------
#
# An OpenStreetMap powered python script to find where places are.
#
# Can output as html (format=html - default) or xml (format=xml)
#  or OSM XML (format=osm) or GPX XML (format=gpx)
#
# Requires that Plant.OSM has been loaded into a local postgres database
#
# See http://wiki.openstreetmap.org/index.php/Where_Are_They
#
# GPL
#
# Nick Burch
#		v0.05  (06/08/2006)

import sys
from osm_io_helper import hasOpt, getOpt, hasAnyOpts, printHTTPHeaders, renderResults
from mini_osm import mini_osm_pgsql

# What place types do we accept?
validPlaceTypes = ["hamlet","village","town","suburb","region","city"]

# Do they want HTML, XML, or OSM?
formats = [ "html", "xml", "osm", "gpx" ]
format = ""
if hasOpt("format"):
	format = getOpt("format")
if not formats.__contains__(format):
	format = formats[0]

# Do the HTTP headers
printHTTPHeaders(format)

# What place + type did they request?
searchtype = 'help'
place = ""
placeTypes = ["city","town"]
if hasOpt("place"):
	place = getOpt("place")
	searchtype = "place"
if hasOpt("type"):
	typestr = getOpt("type")
	types = typestr.split(",")
	placeTypes = []
	for type in (types):
		if validPlaceTypes.__contains__(type):
			placeTypes.append(type)

# Avoid XSS from user string inputs
if not place == None:
	new_place = place
	new_place = new_place.replace('<','&gt;')
	place = new_place

# Connect to the database
miniosm = mini_osm_pgsql()

# ##########################################################################

def displayError(message):
	global format
	global type
	global place
	global placeTypes
	global validPlaceTypes

	if format == "xml" or format == "osm" or format == "gpx":
		print '<?xml version="1.0" encoding="UTF-8"?>'
		print '<error>%s</error>' % message
	if format == "html":
		title = "Error - %s" % message
		if not hasAnyOpts():
			title = "Where Is It?"

		print "<html><head><title>%s</title></head><body>" % title
		print "<h1>%s</h1>" % title
		print "<h3>More Information:</h3>"
		print "<p>For more information, see <a href='http://wiki.openstreetmap.org/index.php/Where_Are_They'>http://wiki.openstreetmap.org/index.php/Where_Are_They</a>.</p>"
		print "<form method='get'>"
		print "<h3>What to find:</h3>"
		print "<p><label for='place'>Place:</label> <input type='text' name='place' value='%s' /> (case sensitive)</p>" % place
		print "<p><label for='type'>Type:</label><br />"
		for type in (validPlaceTypes):
			sel = ""
			if placeTypes.__contains__(type):
				sel = " checked='yes'"
			print "  <span style='margin-left:20px'><input type='checkbox' name='type' value='%s'%s>%s</option></span><br />" % (type,sel,type)
		print "</p>"
		print "<p><label for='format'>Output Format:</label><br />"
		print "<select name='format'>"
		print "  <option value='html' selected>HTML</option>"
		print "  <option value='xml'>XML (simple)</option>"
		print "  <option value='osm'>OSM</option>"
		print "  <option value='gpx'>GPX (waypoints)</option>"
		print "</select>"
		print "</p>"
		print "<p><input type='submit' value='search' /></p>"
		print "</form></body></html>"

	sys.exit()

def displayResults(type,objects):
	global format
	global place
	renderResults(type,objects,format,"Places with name %s"%place,"latlong")

# ##########################################################################

# Check we got sensible things
if place == None or place == "":
	displayError("No place given")
if len(placeTypes) == 0:
	displayError("Invalid place types given")

# Do the work
if searchtype == "place":
	# Grab places
	places = miniosm.getNodesWithTagNameAndType(place,"place",placeTypes)
	miniosm.splatTagsOntoObjects(places, ["name","place"])

	# Set a 0 distance and bearing
	for node in (places.keys()):
		places[node]["bearing"] = 0
		places[node]["distance"] = 0
		places[node]["type"] = places[node]["place"]

	displayResults("places", places)
else:
	displayError("No search type given")
