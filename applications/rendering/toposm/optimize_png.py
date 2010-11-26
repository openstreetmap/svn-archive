#!/usr/bin/python
import sys, os, glob, subprocess

# NOTE: This script is mostly obsolete, since the
# combine-mapnik-tiles script runs the optimizer as each
# tile is rendered.

def optimize(filename):
	print "  Optimizing: ", filename
	command = "optipng -q " + filename
	subprocess.call(command, shell=True)

def process_dir(dirpath):
	print "Processing: ", dirpath
	for filename in glob.glob(os.path.join(dirpath, '*.png')):
		optimize(filename)
	for dirname in os.listdir(dirpath):
		subdirpath = os.path.join(dirpath, dirname)
		if os.path.isdir(subdirpath):
			process_dir(subdirpath)

if __name__ == "__main__":
	if len(sys.argv) < 2:
		process_dir(".")
	else:
		for dirname in sys.argv[1:]:
			process_dir(dirname)

