# Helpers for OSM python scripts such as "where am I" and "where is it"
#
# GPL
#
# Nick Burch
#		v0.01  (06/08/2006)

import os
import sys
import time

global opts

# Grab our options
if os.environ.has_key("QUERY_STRING"):
	if len(os.environ["QUERY_STRING"]):
		opts = os.environ["QUERY_STRING"].split("&")
	else:
		opts = ()
else:
	opts = sys.argv[1:]

def hasOpt(opt):
	global opts
	for thisopt in (opts):
		if thisopt == opt:
			return True
		if thisopt.startswith(opt + "="):
			return True
	return False
def getOpt(opt):
	global opts
	ret=""
	for thisopt in (opts):
		if thisopt.startswith(opt + "="):
			if len(ret):
				ret += ","
			ret += thisopt[(thisopt.find("=")+1):]
	return ret

def hasAnyOpts():
	global opts
	if len(opts):
		return True
	return False

def printHTTPHeaders(format):
	# Everything except HTML is XML
	content_type = "text/xml"
	if format == "html":
		content_type = "text/html"

	# Print out a content type for them
	print "Content-Type: %s" % content_type
	print ""

# ##########################################################################

def findMinMaxLatLong(objects):
	global min_lat
	global min_long
	global max_lat
	global max_long
	(min_lat,min_long,max_lat,max_long) = (90,90,-90,-90)
	if len(objects) == 0:
		(min_lat,min_long,max_lat,max_long) = (0,0,0,0)

	def do_node(node):
		global min_lat
		global min_long
		global max_lat
		global max_long
		if node["lat"] < min_lat:	
			min_lat = node["lat"]
		if node["lat"] > max_lat:	
			max_lat = node["lat"]
		if node["long"] < min_long:	
			min_long = node["long"]
		if node["long"] > max_long:	
			max_long = node["long"]
	def do_seg(seg):
		do_node(seg["node_a_node"])
		do_node(seg["node_b_node"])

	for obj in (objects):
		if obj.has_key("lat"):
			do_node(obj)
		else:
			do_seg(obj)

	return (min_lat,min_long,max_lat,max_long)

# ##########################################################################

def renderResults(type,objects,format,title,renderType):
	# If we got a hash, just get the values
	if isinstance(objects,dict):
		objects = objects.values()

	if format == "xml":
		print '<?xml version="1.0" encoding="UTF-8"?>'
		print '<%s>' % type.lower()
		tag = type.lower()[0:-1]
		for obj in (objects):
			if not obj.has_key("ref"):
				obj["ref"] = None
			latlong = ""
			if obj.has_key("lat"):
				latlong = " latitude='%2.6f' longitude='%2.6f'" % (float(obj['lat']),float(obj['long']))
			print "  <%s ref='%s' type='%s' distance='%d' bearing='%d'%s>%s</%s>" % (tag,obj["ref"],obj["type"],int(obj["distance"]),int(obj["bearing"]),latlong,obj["name"],tag)
		print '</%s>' % type.lower()
	if format == "osm":
		print '<?xml version="1.0" encoding="UTF-8"?>'
		print '<osm version="0.3" generator="where_am_i.py">'

		def osm_do_node(obj):
			print " <node id='%s' lat='%s' long='%s'>" % (obj["id"],obj["lat"],obj["long"])
			for tag in obj["tags"]:
				print "  <tag k='%s' v='%s' />" % (tag[0],tag[1])
			print " </node>"
		def osm_do_seg(obj):
			osm_do_node(obj["node_a_node"])
			osm_do_node(obj["node_b_node"])
			print " <segment id='%s' from='%s' to='%s'>" % (obj["id"],obj["node_a"],obj["node_b"])
			for tag in obj["tags"]:
				print "  <tag k='%s' v='%s' />" % (tag[0],tag[1])
			print " </segment>"

		for obj in (objects):
			if obj.has_key("lat"):
				osm_do_node(obj)
			else:
				osm_do_seg(obj)

		print '</osm>'
	if format == "gpx":
		print '<?xml version="1.0" encoding="UTF-8"?>'
		print '<gpx version="1.0"'
		print ' creator="Where Am I? - http://gagravarr.org/code/"'
		print ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"'
		print ' xmlns="http://www.topografix.com/GPX/1/0"'
		print ' xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd">'

		# Render
		print '<time>' + time.strftime("%Y-%m-%dT%H:%M:%SZ") + '</time>'
		print '<bounds minlat="%s" minlon="%s" maxlat="%s" maxlon="%s" />' % findMinMaxLatLong(objects)


		def gpx_do_node(obj):
			print '<wpt lat="%s" lon="%s">' % (obj["lat"],obj["long"])
			if obj.has_key("name"):
				print ' <name>%s</name>' % obj["name"]
			if obj.has_key("ref"):
				print ' <cmt>%s</cmt>' % obj["ref"]
			if obj.has_key("type"):
				print ' <desc>%s</desc>' % obj["type"]
			print ' <sym>Waypoint</sym>'
			print '</wpt>'
		def gpx_do_seg(obj):
			print '<trk>'
			print ' <name>%s</name>' % obj["name"]
			if obj.has_key("ref"):
				print ' <cmt>%s</cmt>' % obj["ref"]
			print ' <type>%s</type>' % obj["type"]
			print '</trk>'

		# Do waypoints (nodes) first
		for obj in (objects):
			if obj.has_key("lat"):
				gpx_do_node(obj)
			else:
				gpx_do_node(obj["node_a_node"])
				gpx_do_node(obj["node_b_node"])
		# Now do segments
		for obj in (objects):
			if obj.has_key("lat"):
				pass
			else:
				gpx_do_seg(obj)

		print '</gpx>'
	if format == "html":
		print "<html><head><title>%s</title></head>" % (type)
		print "<body><h1>%s</h1>" % type
		if len(objects) > 1:
			print "<ul>"

		for obj in (objects):
			bearing = int(obj["bearing"])
			if bearing < 0:
				bearing += 360.0
			distance = "%dm" % int(obj["distance"])
			if int(obj["distance"]) > 10000:
				distance = "%dkm" % int(obj["distance"]/1000)
			if not obj.has_key("ref"):
				obj["ref"] = None

			if renderType == "latlong":
				print "  <li><b>%s</b> - %s - <a href='http://www.openstreetmap.org/?lat=%s&lon=%s&zoom=12'>%s %s</a> - <a href='where_am_i.py?lat=%s&long=%s'>search around here</a></li>" % (obj["name"],obj["type"],obj["lat"],obj["long"],obj["lat"],obj["long"],obj["lat"],obj["long"])
			else:
				if obj["name"] == obj["ref"] or obj["ref"] == None:
					print "  <li><b>%s</b> - %s - %s - heading %d<sup>o</sup></li>" % (obj["name"], obj["type"], distance, bearing)
				else:
					print "  <li><b>%s</b> - %s - %s - %s - heading %d<sup>o</sup></li>" % (obj["name"], obj["ref"], obj["type"], distance, bearing)

		if len(objects) > 1:
			print "</ul>"
			print "<p>Data from <a href='http://www.openstreetmap.org/'>OpenStreetMap</a></p>"
		if len(objects) == 0:
			print "<p>No results found - either OpenStreetMap doesn't have any data on this area, or that data that it does have hasn't yet been correctly tagged. Sorry about that.</p>"
		print "</body></html>"

# ##########################################################################

def sortByDistance(x,y):
	if x["distance"] == y["distance"]:
		return 0
	if x["distance"] > y["distance"]:
		return 1
	return -1

# ##########################################################################
