#!/usr/bin/python
# polybuffer.py
# version 0.2

# History
# - 12/01/2009 0.1   First release
# - 12/10/2009 0.2   More advanced DB connection, improvements in polygon parsing


# Usage: ./polybuffer.py -h
# Example use with stdin and stdout: cat poly/quebec2pts.txt | ./polybuffer.py >out.txt

# Requirements:
# * Recent Python version (tested with 2.6)
# * PostGIS (tested with 1.3)
# * PyGreSQL (http://www.pygresql.org/)

# Notes:
# * Only tested on Ubuntu, but should run everywhere
# * In the input file, holes in polygons are not (yet?) recognized, but they are generated in the output file
# * Poly file is assumed to be in WGS84
# * TODO Use a different SRS in PostGIS, eventually make it an option. For now, WGS84 is sufficient


import getpass
import random
import re
import sys
import _pg
from optparse import OptionParser


# Do not change any of the following constants
VERSION = "0.2"


# Read a polygon from file
# NB: holes aren't supported yet
def read_polygon(f):

	coords = []
	first_coord = True
	while True:
		line = f.readline()
		if not(line):
			break;
			
		line = line.strip()
		if line == "END":
			break
		
		# NOTE: needs to happen before not(line).
		# There can be a blank line in the poly file if the centroid wasn't calculated!
		if first_coord:
			first_coord = False
			continue	
		
		if not(line):
			continue
		
		ords = line.split()
		coords.append("%f %f" % (float(ords[0]), float(ords[1])))
	
	if len(coords) < 3:
		return None
	
	polygon = "((" + ",".join(coords) + "))"
	
	return polygon


# Read a multipolygon from the file
# First line: name (discarded)
# Polygon: numeric identifier, list of lon, lat, END
# Last line: END
def read_multipolygon(f):
	
	polygons = []
	while True:
		dummy = f.readline()
		if not(dummy):
			break
		
		polygon = read_polygon(f)
		if polygon != None:
			polygons.append(polygon)
		
	wkt = "MULTIPOLYGON(" + ",".join(polygons) + ")"
	
	return wkt


# Write a polygon to the file
def write_polygon(f, wkt, p):

	match = re.search("^\(\((?P<pdata>.*)\)\)$", wkt)
	pdata = match.group("pdata")
	rings = re.split("\),\(", pdata)
	
	first_ring = True
	for ring in rings:
		coords = re.split(",", ring)			
	
		p = p + 1
		if first_ring:
			f.write(str(p) + "\n")
			first_ring = False
		else:
			f.write("!" + str(p) + "\n")
		
		for coord in coords:
			ords = coord.split()
			f.write("\t%E\t%E\n" % (float(ords[0]), float(ords[1])))
		
		f.write("END\n")
	
	return p


# Write a multipolygon to the file
def write_multipolygon(f, wkt):

	match = re.search("^MULTIPOLYGON\((?P<mpdata>.*)\)$", wkt)
	
	if match:
		mpdata = match.group("mpdata")
		polygons = re.split("(?<=\)\)),(?=\(\()", mpdata)
	
		p = 0
		for polygon in polygons:
			p = write_polygon(f, polygon, p)
		
		return
		
	match = re.search("^POLYGON(?P<pdata>.*)$", wkt)
	if match:
		pdata = match.group("pdata")
		write_polygon(f, pdata, 0)
	

# Calculate a buffer around the polygon, using PostGIS
def buffer_polygon(wkt, db, buffer_distance):

	# Generate random table name
	table_name = "polybuffer_"
	for i in random.sample("abcdefghijklmnopqrstuvwxyz", 10):
		table_name += i
	
	# Create a table in the DB
	sql = "CREATE TABLE %s ( id integer );" % (table_name)
	db.query(sql)
	sql = "SELECT AddGeometryColumn('', '%s', 'the_geom', '-1', 'GEOMETRY' ,2);" % (table_name)
	db.query(sql)

	# Load data into it
	sql = "INSERT INTO %s (id, the_geom) VALUES (1, GeomFromText('%s'));" % (table_name, wkt)
	db.query(sql)
	
	# Convert buffer distance from meter to degree
	dist = float(buffer_distance) * 9 / 1000000
	
	# Simplify geometry
	sql = "UPDATE %s SET the_geom = ST_Simplify(the_geom, %f);" % (table_name, dist / 10)
	db.query(sql)
	
	# Calculate buffer
	sql = "UPDATE %s SET the_geom = ST_Buffer(the_geom, %f);" % (table_name, dist)
	db.query(sql)
	
	# Extract data from it
	sql = "SELECT AsText(the_geom) AS wkt FROM %s;" % (table_name)
	result = db.query(sql).dictresult()
	
	for record in result:
		wkt = record["wkt"]
		break
	
	# Delete table
	sql = "SELECT DropGeometryColumn('', '%s', 'the_geom')" % (table_name)
	db.query(sql)
	sql = "DROP TABLE %s;" % (table_name)
	db.query(sql)
	
	return wkt


# Perform all the conversion steps
def convert_poly (input_file, output_file, buffer_distance, db):

	# Steps:
	# 1. Convert poly to WKT
	# 2. PostGIS actions:
	#    a. Create temp table
	#    b. Load data
	#    c. Buffer data
	#    d. Get data as WKT
	# 3. Convert WKT to poly
	
	# Note that holes aren't supported yet on import. They are on export.
	
	# Read input file, and convert polygon to WKT
	if input_file:
		f = open(input_file, "r")
	else:
		f = sys.stdin
		
	name = f.readline().strip()
	wkt = read_multipolygon(f)	
	f.close()

	# Buffer the polygon
	wkt = buffer_polygon(wkt, db, buffer_distance)

	# Convert WKT to polygon, and write output file
	if output_file:
		f = open(output_file, "w")
	else:
		f = sys.stdout
	
	name = "%s_%f" % (name, float(buffer_distance))
	f.write(name + "\n")		
	write_multipolygon(f, wkt)
	f.write("END\n")
	f.close()	

	return


def main():

	default_user = getpass.getuser()

	# Parse arguments
	arg_list = "-i <input> -o <output> [-b <distance>] [-d <db> ...]"
	usage = "Usage: %prog " + arg_list
	version = "%prog " + VERSION
	parser = OptionParser(usage, version=version)
	parser.add_option("-i", "--input", dest="input", help="input file (default stdin)")
	parser.add_option("-o", "--output", dest="output", help="output file (default stdout)")
	parser.add_option("-b", "--buffer", type="float", dest="buffer", help="buffer distance in m (default '1000')", default=1000)
	parser.add_option("-d", "--dbname", dest="dbname", help="database")
	parser.add_option("--host", dest="host", help="host (default 'localhost')", default="localhost")
	parser.add_option("--port", type="int", dest="port", help="port (default '5432')", default="5432")
	parser.add_option("-u", "--user", dest="user", help="user name (default '%s')" % (default_user), default=default_user)
	parser.add_option("-w", "--password", action="store_true", dest="passwd", help="password", default=False)
	(options, args) = parser.parse_args()
	
	passwd = ""
	if options.passwd:
		passwd = getpass.getpass("Please enter your password: ")
	
	db = _pg.connect(dbname=options.dbname,
					 host=options.host,
					 port=options.port, 
					 user=options.user, 
					 passwd=passwd)
					 
	convert_poly(options.input, options.output, options.buffer, db)


if __name__ == "__main__":
	main()

