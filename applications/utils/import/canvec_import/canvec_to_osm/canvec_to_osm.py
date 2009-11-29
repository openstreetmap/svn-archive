#!/usr/bin/python
# canvec-to-osm.py
# version 0.1

# The adjacent file canvec_to_osm_feature contains a list of dictionaries describing the features to be converted. The
# contained elements are used to compose the rules, shp, and osm file names. During execution, a PYC file is generated
# out of this file, which is harmless.
# This script has been being tested with Python 2.6 on Linux, and incidentally with Python 2.5 on Windows.
# The file features.py must be in the same dir.

# Usage: canvec-to-osm.py [-c] nts_tile
#
# Options:
#   --version      show program's version number and exit
#   -h, --help     show this help message and exit
#   -v, --verbose  print many messages
#   -q, --quiet    be quiet
#   -l, --log      log output of shp-to-osm
#   -c, --cache    use local canvec cache only
# The NTS tile has to be given in the form 999X or 999X00. In the first case, all 16 tiles will be processed.

# Requirements:
# - Python 2.6 (recommended) or 2.5: http://www.python.org/
# - Java Runtime Environment: http://www.java.com/
# - recent version of shp-to-osm: http://redmine.yellowbkpk.com/projects/list_files/geo

# History
# - 11/11/2009 0.1   First release

import features
import glob
import os
import re
import shutil
import subprocess
import sys
import urllib2
import zipfile
from optparse import OptionParser


# Configuration, adjust to your own taste
BINDIR = "bin"     # Location of shp-to-osm.jar, must exist
OSMDIR = "osm"     # Location of compressed OSM output, created automatically
RULESDIR = "rules" # Location of rules files, must exist and contain rules files
SHPDIR = "shp"     # Location of shape files (existing, or to be downloaded), created automatically
TEMPDIR = "temp"   # Created automatically; if dir already exists, a subdir will be created within, and removed when done

MAXNODES = 2000      # Maximum number of nodes in file (can be exceeded, especially with large relations with many members)
JARFILE = "shp-to-osm-0.7.3-jar-with-dependencies.jar" # Full name of shp-to-osm.jar
MEMSIZE = "-Xmx512M" # Memory usage for shp-to-osm.jar


# Do not change any of the following constants
VERSION = "0.1"
CMDLINE = "java %s -cp %s/%s Main" % (MEMSIZE, BINDIR, JARFILE)
BASEURL = "http://ftp2.cits.rncan.gc.ca/pub/canvec/50k_shp"
SHPZIP_PATTERN = "canvec_%03d%s%02d_shp.zip"
DIR_PATTERN = "%s/%03d/%s/%s"
IS_26_OR_HIGHER = sys.version_info[0] > 2 or (sys.version_info[0] == 2 and sys.version_info[1] >= 6)
CMD_SHPTOOSM = "%s --maxnodes %d --shapefile %s --rulesfile %s --osmfile %s --outdir %s -t 2>>%s"
CMD_SHPTOOSM_GLOM = "%s --maxnodes %d --shapefile %s --rulesfile %s --osmfile %s --outdir %s --glomKey %s -t 2>>%s"


# ZipFile.extractall implementation, missing in Python 2.5
def zipfile_extractall(self, path=None, members=None, pwd=None):
	if members is None:
		members = self.namelist()

	for zipinfo in members:
		zipfile_extract(self, zipinfo, path, pwd)


# ZipFile.extract implementation, missing in Python 2.5
def zipfile_extract(self, zipinfo, path=None, pwd=None):
	data = self.read(zipinfo)
	outfile = zipinfo
	if path:
		outfile = "%s/%s" % (path, zipinfo)
	
	f = open(outfile, "wb")
	f.write(data)
	f.close()
	

# Checks if the given tile number is valid
def validate_tile(nts_tile):
	
	re_nts = re.compile("^(\d{1,3})([a-p])(\d{0,2})$", re.I)
	m = re_nts.match(nts_tile)
	if not m:
		return None
		
	# Validate major and minor tile numbers
	# The minor tile number can be omitted. In that case all tiles will be downloaded and processed
	nr_maj = int(m.group(1))
	letter = m.group(2).lower()
	nr_min = 0
	if m.group(3):
		nr_min = int(m.group(3))
		
	if (nr_maj >= 1 and nr_maj <= 120) or nr_maj == 340 or nr_maj == 560:
		if nr_min >= 0 and nr_min <= 16:
			return {"nr_maj": nr_maj, "letter": letter, "nr_min": nr_min}
	
	return None


# Downloads a series of tiles from the NRCan server
def download_data(val_tile, verbose):
	start = 1
	end = 16
	if val_tile["nr_min"] > 0:
		start = end = val_tile["nr_min"]
		
	nr_maj = val_tile["nr_maj"]
	letter = val_tile["letter"]	
	shpzip_dir = DIR_PATTERN % (SHPDIR, nr_maj, letter, "")
	
	for nr_min in range(start, end + 1):
		# Create various names
		shpzip_name = SHPZIP_PATTERN % (nr_maj, letter, nr_min)
		url = DIR_PATTERN % (BASEURL, nr_maj, letter, shpzip_name)
		shpzip_path = DIR_PATTERN % (SHPDIR, nr_maj, letter, shpzip_name)
		
		if os.path.exists(shpzip_path):
			if verbose:
				print "File already exists:", shpzip_name
			continue
		
		# Download data
		try:
			if verbose:
				print "Download file:", shpzip_name
			url_file = urllib2.urlopen(url)
		except urllib2.HTTPError:
			if verbose:
				print "Could not download file:", shpzip_name
			continue

		# Save the downloaded file			
		if not os.path.exists(shpzip_dir):
			os.makedirs(shpzip_dir)
			
		shpzip_file = open(shpzip_path, "wb")
		data = url_file.read()
		url_file.close()
		shpzip_file.write(data)
		shpzip_file.close()


# Converts the SHP data to OSM data, and puts the result in a zip file
def convert_data(val_tile, verbose, log_shp2osm):

	oldpath = os.getcwd()

	start = 1
	end = 16
	if val_tile["nr_min"] > 0:
		start = end = val_tile["nr_min"]
		
	nr_maj = val_tile["nr_maj"]
	letter = val_tile["letter"]	

	# Determine / create temp dir
	temp_dir = TEMPDIR
	
	if os.path.exists(temp_dir):
		temp_dir = TEMPDIR + "/" + "canvec-to-osm"
	
	if not os.path.exists(temp_dir):
		if verbose:
			print "Create temp dir:", temp_dir
		os.makedirs(temp_dir)
	
	for nr_min in range(start, end + 1):
		# Create various names
		tilename = "%03d%s%02d" % (nr_maj, letter, nr_min)
		shpzip_name = SHPZIP_PATTERN % (nr_maj, letter, nr_min)
		shpzip_path = DIR_PATTERN % (SHPDIR, nr_maj, letter, shpzip_name)
		
		if not os.path.exists(shpzip_path):
			if verbose:
				print "File does not exist:", shpzip_name
			continue
		
		# Unzip shpfile, target: temp_dir + tilenr
		tempdir_tile = "%s/%02d" % (temp_dir, nr_min)
		tempdir_tile_shp = tempdir_tile + "/shp"
		tempdir_tile_osm = tempdir_tile + "/osm"
		if not os.path.exists(tempdir_tile_osm):
			os.makedirs(tempdir_tile_osm)
		if not os.path.exists(tempdir_tile_shp):
			os.makedirs(tempdir_tile_shp)
		
		if zipfile.is_zipfile(shpzip_path):
			shp_zip = zipfile.ZipFile(shpzip_path, "r")
			
			if IS_26_OR_HIGHER:
				shp_zip.extractall(tempdir_tile_shp)
			else:
				zipfile_extractall(shp_zip, tempdir_tile_shp)
			shp_zip.close()	
			
		# Perform conversions
		if verbose:
			print "Perform conversions for tile:", tilename
		log_file = "shp-to-osm_%s.log" % tilename
		if os.path.exists(log_file):
			os.remove(log_file)
			
		for feature_type in features.feature_list:
			rules_path = "%s/%s_%s_%sRULES.txt" % (RULESDIR, feature_type["code"], feature_type["geom"], feature_type["class"])
			
			for geom in feature_type["geom"]:
				shp_path = "%s/%s_*_%s_%s.shp" % (tempdir_tile_shp, tilename, feature_type["code"], geom)
			
				if not os.path.exists(rules_path):
					if verbose:
						print "Rules file can't be found:", rules_path
					continue
			
				hits = glob.glob(shp_path)
				if len(hits) == 0:
					if verbose:
						print "Shape file does not exist:", shp_path
					continue
								
				shp_path = hits[0]
				osm_prefix = "%s_%s_%s_%s" % (tilename, feature_type["code"], geom, feature_type["class"])
				
				glom_key = None
				if "glom_key" in feature_type:
					glom_key = feature_type["glom_key"]

				# NOTE: --outputFormat osm: causes extension to be 'xml', and all elements have attribute
				# visible="false". Issue reported to Ian, Nov. 8th.
				# NOTE: shp-to-osm writes a whole lot of error messages to stderr. They are captured into a log file.
				# If the user doesn't want to keep this fil, it will be remove afterwards
				if glom_key:
					cmd = CMD_SHPTOOSM_GLOM % (CMDLINE, MAXNODES, shp_path, rules_path, osm_prefix, tempdir_tile_osm, glom_key, log_file)
				else:
					cmd = CMD_SHPTOOSM % (CMDLINE, MAXNODES, shp_path, rules_path, osm_prefix, tempdir_tile_osm, log_file)
				print cmd
			
				subprocess.Popen(cmd, shell=True).wait()
				
		# Remove shp-to-osm log file, if unwanted
		if not log_shp2osm and os.path.exists(log_file):
			os.remove(log_file)

		if not os.path.exists(OSMDIR):
			os.makedirs(OSMDIR)
			
		# Zip up result
		osmzip_path = "%s/canvec_%s_osm_c2o_v%s.zip" % (OSMDIR, tilename, VERSION)
		if verbose:
			print "Compress output:", osmzip_path 

		hits = glob.glob("%s/*.osm" % (tempdir_tile_osm))
		if len(hits) == 0:
			print "No output generated for tile:", tilename
			continue
		
		# Iterate over files in temp osm dir, and write them to the zip file
		osm_zip = zipfile.ZipFile(osmzip_path, "w", zipfile.ZIP_DEFLATED)
		for osm_path in hits:
			osm_path = osm_path.replace("\\", "/")
			arcname = osm_path[osm_path.rfind("/") + 1 :]
			print arcname
			osm_zip.write(osm_path.encode("ascii"), arcname)
			
		# The following throws an exception with Python 2.5, because a readmode of "r" or "a" is expected. Corrected in 2.6.
		if IS_26_OR_HIGHER:
			osm_zip.testzip()
		osm_zip.close()
		
		# Clean up tile dir in temp_dir, to preserve space
		shutil.rmtree(tempdir_tile)
	
	if verbose:
		print "Remove temp dir:", temp_dir
	shutil.rmtree(temp_dir)
		

# Executes the program
def main():
	# Parse arguments
	arg_list = "[-c] nts_tile"
	usage = "Usage: %prog " + arg_list
	version = "%prog " + VERSION
	parser = OptionParser(usage, version=version)
	parser.add_option("-v", "--verbose", action="store_true", dest="verbose", help="print many messages", default=True)
	parser.add_option("-q", "--quiet", action="store_false", dest="verbose", help="be quiet")
	parser.add_option("-l", "--log", action="store_true", dest="log_shp2osm", help="log output of shp-to-osm", default=False)
	parser.add_option("-c", "--cache", action="store_true", dest="cacheonly", help="use local canvec cache only", default=False)
	(options, args) = parser.parse_args()

	if len(args) < 1:
		print "Usage: canvec-to-osm.py", arg_list
		print "Use -h to show help"
		return

	nts_tile = args[0]
	
	# Perform download and conversion
	val_tile = validate_tile(nts_tile)
	if not val_tile:
		print "Invalid NTS tile, format should be 999x or 999x00"
		return
	
	if not options.cacheonly:
		download_data(val_tile, options.verbose)
	elif options.verbose:
		print "Skipping file download"
		
	convert_data(val_tile, options.verbose, options.log_shp2osm)
	
	print "Done"


if __name__ == "__main__":
	main()

