#!/usr/bin/python
############################################################################
#   gpxamr2wav: Convert amr audio references in a GPX file to wav format.
#    Copyright (C) 2009   Graham Jones
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
###########################################################################
#
# NAME: gpxamr2wav.py [-i <gpxinputfile>] [-o <gpxoutputfile>] [-m mode]
# DESC: Adds <link href="filename.wav"> elements to the input gpx file
#       to produce the specified output gpx file.
#       The method of determining the filename depends on the mode.  Valid modes
#       are:
#           gjGPSMid - Graham's version of GPSMid which adds link elements.
#           name     - uses the element's name to determine the filename.
#       The audio files found in the input GPX file is converted to wav format
#       using ffmpeg.
#       It writes the gpx file to the output, with the only change
#       being the <link> elements point to the new wav files instead of
#       the amr ones.
# HIST: 10apr2009  GJ  ORIGIAL VERSION (0.1).
#       13apr2009  GJ  Added different modes as options (0.2)
#
#   Copyright 2009  Graham Jones (grahamjones139 <at> gmail.com).
#
###########################################################################

import sys
import os
from parseGPX import parseGPX
from optparse import OptionParser

usage = "Usage %prog [options]\n\
Reads the input GPX file and adds <link> tags to point to audio files associated\
with the data.  \n\
The method of determining the audio filename is specified by the mode option.\n\
It is assumed that the audio files are in amr format, and so they\
are converted to .wav format using ffmpeg."
version = "0.2"

modes = ["gjgpsmid","gpsmid"]

parser = OptionParser(usage=usage,version=version)
parser.add_option("-i", "--infile", dest="infilename", 
                  help="input file name (default: waypoints.gpx)",
                  metavar="INFILE")
parser.add_option("-o", "--outfile", dest="outfilename",
                  help="filename to use for output (default: waypoints_wav.gpx)",
                  metavar="OUTFILE")
parser.add_option("-m", "--mode", dest="modestr",
                  help="method of identifying files to convert (default: gpsmid)\
 valid values are %s" % modes,
                  metavar="MODE")
parser.add_option("-v", "--verbose", action="store_true",dest="verbose",
                  help="Include verbose output")
parser.add_option("-d", "--debug", action="store_true",dest="debug",
                  help="Include debug output")
parser.set_defaults(
    infilename="waypoints.gpx",
    outfilename="waypoints_wav.gpx",
    modestr = "gpsmid",
    debug=False,
    verbose=False)
(options,args)=parser.parse_args()

if (options.debug):
    options.verbose = True
    print "options   = %s" % options
    print "arguments = %s" % args
        
#if len(args)<1:
#    print "Error: You must supply at least one GPX file to analyse.\n"
#    sys.exit(-1)
#else:
print "Reading Input File %s." % options.infilename
pgpx = parseGPX(options.infilename,options.debug,options.verbose)

try:
    modeno = modes.index(options.modestr)
    if (options.debug): print "Using mode number %d (%s) to detect filenames" \
            % (modeno,options.modestr)
except ValueError:
    modeno = -1
    print "Error: Specified mode %s does not exist.  Valid modes are: %s" \
        % (options.modestr,modes)
    exit(-1)


print "Opening Output File %s for output." % options.outfilename
of = open(options.outfilename,'w')
of.write("<?xml version='1.0' encoding='UTF-8'?>")
of.write("<gpx version='1.1' creator='amr2wav' xmlns='http://www.topografix.com/GPX/1/1'>")
print "Processing all waypoints in input file...."
for waypt in pgpx.wayPts:
    fname = "undefined"
    if options.debug: print waypt
    of.write("<wpt lat='%s' lon='%s'>\n" % (waypt['lat'],waypt['lon']))
    if 'name' in waypt:
        of.write("<name>%s</name>\n" % waypt['name'])
        if modeno==1:   # gpsmid mode
            namestr = waypt['name']
            nameparts = namestr.split('AudioMarker-')
            if options.debug: print "nameparts=%s" % nameparts
            fnamebase = nameparts[1]
            fname = fnamebase.lower() + '.amr'
            if options.debug: print "fname = %s" % fname
    if 't' in waypt:
        of.write("<com><time>%s</time></com>\n" % waypt['time'])
    if 'link' in waypt:
        if modeno==0:   # gjGPSMid Mode
            if options.debug: print "link=%s" % waypt['link']
            linkstr = waypt['link']
            linkparts = linkstr.split('"')
            fileURL = linkparts[1]
            fileURLparts = fileURL.split('//')
            fname = fileURLparts[1]
        #print "fname = %s" % fname
        else:
            if (options.debug): print "Ignoring existing link tag in waypoint %s, because you specified mode %s" % (waypt,options.modestr)
    # Do the actual conversion from amr to wav, if we have identified a filename
    # to convert.
    if fname != "undefined":        
        fnameparts = fname.split('.')
        fnamebase = fnameparts[0]
        fnameext = fnameparts[1]
        if os.path.isfile("%s.%s" % (fnamebase,fnameext)):
            print "Converting file %s.%s" % (fnamebase, fnameext)
            if options.debug:
                os.system("ffmpeg -y -v 0 -i %s.%s %s.%s" % \
                              (fnamebase,fnameext,fnamebase,"wav"))
            else:
                os.system("ffmpeg -y -v 1 -i %s.%s %s.%s 2>! amr2wav.log" % \
                          (fnamebase,fnameext,fnamebase,"wav"))

            of.write('<link href="file://%s/%s.%s">audio</link>\n' \
                         % (os.getcwd(),fnamebase,"wav"))
        else:
            print "Warning, File %s.%s does not exist - skipping conversion.." % \
                (fnamebase,fnameext)
    else:
        print "\nOh no, have not managed to extract a filename from waypoint %s - skipping\n" % waypt
    of.write("</wpt>\n")
of.write("</gpx>\n")
of.close()


