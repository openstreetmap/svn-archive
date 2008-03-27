#!/usr/bin/python
#
#                   Mini OSM Module
#                   ---------------
#
# Provides OSM functionality from a local (normally planet.osm powered)
#  OSM database, in Python.
#
# Supports OSM v0.5
#
# GPL
#
# Nick Burch
#		v0.03  (27/03/2008)

import os
import sys
import math
from geo_helper import calculate_distance_and_bearing

class mini_osm:
	dbh = None

	#def __init__(self, type):
	#	if type == "pgsql" or type == "postgres":
	#		self = mini_osm_pgsql()
	#	else:
	#		raise "Un-supported type '%s'" % type
	def __init__(self):
		raise "Must create child classes"

	def connect(self):
		raise "Implementation must provide a connect method"

	# ######################################################################

	def getBoundingBox(self, lat, long, distance):
		"""For a given lat, long and distance, get the bounding box"""

		lat_rad = lat / 360.0 * 2.0 * math.pi
		long_rad = long / 360.0 * 2.0 * math.pi

		# Calculate how far the distance is in lat+long
		# Values should be as right as possible, whilst being compatible
		#  with the answers from geo_helper.calculate_distance_and_bearing()
		deg_lat_km = 110.574235
		deg_long_km = 110.572833 * math.cos(lat_rad)
		#deg_lat_km = 111.133 - 0.559*math.cos(2.0*lat_rad)
		delta_lat = distance / 1000.0 / deg_lat_km
		delta_long = distance / 1000.0 / deg_long_km

		# Compute the min+max lat+long
		min_lat = lat - delta_lat
		min_long = long - delta_long
		max_lat = lat + delta_lat
		max_long = long + delta_long

		return (min_lat,min_long,max_lat,max_long)

	# ######################################################################

	def _getNodesInAreaSQL(self):
		"""SQL to get all the nodes in an area"""
		sql = "SELECT id, latitude, longitude, name, value        \
		       FROM nodes                                         \
		       LEFT OUTER JOIN node_tags                          \
		            ON (id = node)                                \
		       WHERE (latitude BETWEEN %s AND %s)                 \
		       AND (longitude BETWEEN %s AND %s)"
		return sql

	def _getNodesInAreaWithTagSQL(self,tag_name,tag_value):
		"""SQL to get all the nodes in an area with the given tag name, and optionally also tag value"""
		tag_match = "name = %s"
		if not tag_value == None:
			tag_match += " AND value = %s"

		sql = "SELECT id, latitude, longitude, name, value       \
		       FROM nodes                                        \
		       INNER JOIN node_tags                              \
		            ON (id = node)                               \
		       WHERE id IN (                                     \
		          SELECT id                                      \
		          FROM nodes                                     \
		          INNER JOIN node_tags                           \
		                ON (id = node AND " + tag_match + ")     \
		          WHERE (latitude BETWEEN %s AND %s)             \
		          AND (longitude BETWEEN %s AND %s)              \
		       )"
		return sql

	def _getNodesWithTagNameAndTypeSQL(self,name,tag_type,tag_values):
		"""SQL to get all the nodes in the DB with the given name, and one of the given other tags, eg name=London, tag_type=place, tag_values=city,town"""

		in_sql = ""
		for val in (tag_values):
			if len(in_sql) > 0:
				in_sql += ","
			in_sql += "%s"

		sql = "SELECT id, latitude, longitude, name, value       \
		       FROM nodes                                        \
		       INNER JOIN node_tags AS tags                      \
		            ON (id = tags.node)                          \
		       WHERE id IN (                                     \
		          SELECT nt.node                                 \
		          FROM node_tags AS nt                           \
		          INNER JOIN node_tags AS ot                     \
		            ON (nt.name = 'name' AND nt.value = %s       \
		                AND nt.node = ot.node                    \
		                AND ot.name = %s AND ot.value IN (" + in_sql + ") \
		            )                                            \
		       )"
		return sql

	def _processNodesQuery(self, sth):
		"""Processes the given nodes query, and return a dict of the nodes"""

		nodes_db = sth.fetchall()

		nodes = {}
		last_node = { "id":-1 }
		for node in (nodes_db):
			if last_node["id"] != node.id:
				if last_node["id"] != -1:
					nodes[last_node["id"]] = last_node
				last_node = { "id":node.id, "lat":node.latitude, "long":node.longitude, "tags":[] }
			if node.name != None and len(node.name):
				last_node["tags"].append( (node.name,node.value) )
		if last_node["id"] != -1:
			nodes[last_node["id"]] = last_node

		return nodes

	def getNodesInArea(self, lat, long, distance):
		"""Fetch all the nodes within a given distance of the lat+long"""

		# Calculate how far the distance is in lat+long
		(min_lat,min_long,max_lat,max_long) = self.getBoundingBox(lat,long,distance)
		return self.getNodesInBBox(min_lat,min_long,max_lat,max_long)

	def getNodesInBBox(self, min_lat, min_long, max_lat, max_long):
		"""Find all the nodes within the given bounding box"""
		# Find nodes
		sql = self._getNodesInAreaSQL()
		sth = self.dbh.cursor()
		sth.execute(sql, min_lat, max_lat, min_long, max_long)

		return self._processNodesQuery(sth)

	def getNodesWithTagNameAndType(self,name,tag_type,tag_values):
		"""Fetch all the nodes with the given name, and given other tag having one of the supplied values"""
		if len(tag_values) == 0:
			return {}
		sql = self._getNodesWithTagNameAndTypeSQL(name,tag_type,tag_values)
		sth = self.dbh.cursor()
		params = [name, tag_type]
		for val in tag_values:
			params.append(val)
		sth.execute(sql,params)

		return self._processNodesQuery(sth)

	def getNodesInAreaWithTag(self, lat, long, distance, tag_name, tag_value=None):
		"""Fetch all the nodes within a given distance of the lat+long, with the given tag name (and optionally tag value)"""

		# Calculate how far the distance is in lat+long
		(min_lat,min_long,max_lat,max_long) = self.getBoundingBox(lat,long,distance)
		return self.getNodesInBBoxWithTag(min_lat,min_long,max_lat,max_long,tag_name,tag_value)

	def getNodesInBBoxWithTag(self, min_lat, min_long, max_lat, max_long, tag_name, tag_value=None):
		"""Fetch all the nodes within the given bounding, with the given tag name (and optionally tag value)"""
		params = [tag_name]
		if not tag_value == None:
			params.append(tag_value)
		params.append(min_lat)
		params.append(max_lat)
		params.append(min_long)
		params.append(max_long)

		# Find nodes
		sql = self._getNodesInAreaWithTagSQL(tag_name,tag_value)
		sth = self.dbh.cursor()
		sth.execute(sql, params)

		return self._processNodesQuery(sth)

	# ######################################################################

	def _getWaysForNodesSQL(self,node_ids):
		"Returns all the different ways, and their tags, but not nodes"
		sql = """SELECT way_tags.way AS id, name, value               
                 FROM way_nodes   
                 LEFT OUTER JOIN way_tags
                    ON (way_nodes.way = way_tags.way)
                 WHERE node IN (%s)""" % (node_ids)
		return sql

	def _getWaysNodesForWaysSQL(self,way_ids):
		"Returns all the different nodes for the supplied ways"
		sql = """SELECT way, node
                 FROM way_nodes   
                 WHERE way IN (%s)
                 ORDER BY node_order""" % (way_ids)
		return sql

	def getWaysForNodes(self,nodes):
		"""Fetch all the ways for the given list of nodes"""
		global dbh

		# Get list of node ids
		ids = ",".join( [str(n) for n in nodes.keys() if n] )

		# Find the ways and their tags
		sql = self._getWaysForNodesSQL(ids)
		sth = self.dbh.cursor()
		sth.execute(sql)

		ways_db = sth.fetchall()

		ways = {}
		for way in (ways_db):
			if not ways.has_key(way.id):
				ways[way.id] = { "id":way.id, "nodes":[], "tags":[] }
			if way.name != None:
				ways[way.id]["tags"].append( (way.name,way.value) )

		# Now find their tags
		ids = ",".join( [str(w) for w in ways.keys() if w] )

		if len(ids):
			sql = self._getWaysNodesForWaysSQL(ids)
			sth = self.dbh.cursor()
			sth.execute(sql)

			ways_db = sth.fetchall()
		else:
			ways_db = []

		last_way = { "id":-1 }
		for way in (ways_db):
			ways[way.way]["nodes"].append( way.node )

		return ways

	# ######################################################################

	def splatTagsOntoObjects(self,objects,want_tags):
		"""Splats certain tags down onto the main objects, setting them to None if not found"""
		for obj in (objects.values()):
			# Set to None, so always there
			for tag in (want_tags):
				obj[tag] = None
			# Now search for them
			for tag in obj["tags"]:
				(tagname,tagvalue) = tag
				if want_tags.__contains__(tagname):
					obj[tagname] = tagvalue

	def calculateDistanceToWays(self,lat,long,ways,nodes):
		"""Calculates the distances to the ways"""

		for way in (ways):
			# Get their nodes from inside the bbox
			f_nodes = []
			for node_id in way["nodes"]:
				if nodes.has_key(node_id):
					f_nodes.append( nodes[node_id] )
			# Now find the distance of each node-node gap
			for node_pos in range(len(f_nodes) - 1):
				node_a = f_nodes[node_pos]
				node_b = f_nodes[node_pos+1]

				avg_lat = 0.5 * (node_a["lat"] + node_b["lat"])
				avg_long = 0.5 * (node_a["long"] + node_b["long"])

				dist_bearing = calculate_distance_and_bearing(lat,long,avg_lat,avg_long)
				if not way.has_key("distance"):
					way["distance"] = dist_bearing[0]

				if way["distance"] <= dist_bearing[0]:
					way["distance"] = dist_bearing[0]
					way["bearing"] = dist_bearing[1]

	def filterWaysByTags(self,ways,nodes,want_tag_names,want_tag_values=None):
		"""Filters the ways, only returning ones with one of the given tags, optionally also by tag value"""

		wanted = []
		for way in (ways.values()):
			# Exclude any sections that pass out of the boundary
			for node in way["nodes"]:
				if not nodes.has_key(node):
					continue

			# Filter by tags
			for tag in way["tags"]:
				(tagname,tagvalue) = tag
				if want_tag_names.__contains__(tagname):
					# Check value, if required
					if want_tag_values == None:
						wanted.append(way)
					else:
						if want_tag_values.__contains__(tagvalue):
							wanted.append(way)
		return wanted

# ######################################################################

class mini_osm_pgsql(mini_osm):
	"""PostGreSQL Specific Mini OSM Implementation"""
	from pyPgSQL import PgSQL

	# Database settings
	dbname = "planetosm"
	dbhost = ""
	dbuser = "nick"
	dbpass = ""

	dbh = None

	def __init__(self):
		self.connect()

	def connect(self):
		""" Connect to the database"""
		if len(self.dbhost):
			self.dbh = self.PgSQL.connect(database=self.dbname, host=self.dbhost, user=self.dbuser, password=self.dbpass)
		else:
			self.dbh = self.PgSQL.connect(database=self.dbname, user=self.dbuser, password=self.dbpass)
