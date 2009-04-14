#!/usr/bin/python
#
#    This file is part of wherewasi.
#
#    Wherewasi is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This wherewasi is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with wherewasi.  If not, see <http://www.gnu.org/licenses/>.
#
#    Copyright Graham Jones 2009
#

import sys
import os
from parseGPX import parseGPX
#from geometry import *
from time import *
#from analyseGPX_gui import analyseGPX_gui
from optparse import OptionParser



usage = "Usage %prog [options] filename"
version = "0.1"
parser = OptionParser(usage=usage,version=version)
parser.add_option("-S", "--ss", "--startseg", dest="s_seg", type="int",
                  help="Analysis Start segment",
                  metavar="s_seg")
parser.add_option("-s", "--sp", "--startpt", dest="s_pt", type="int",
                  help="Analysis Start point number",
                  metavar="s_pt")
parser.add_option("-E", "--es", "--endseg", dest="e_seg", type="int",
                  help="Analysis End segment",
                  metavar="e_seg")
parser.add_option("-e", "--ep", "--endpt", dest="e_pt", type="int",
                  help="Analysis End point number",
                  metavar="e_pt")
parser.add_option("-l", "--list", action="store_true", dest="list",
                  help="produce a summary list of the contents of the file")
parser.add_option("--summary", "--sum", action="store_true", dest="summary",
                  help="produce a summary table of the file data")
parser.add_option("--eprofile", "--eprof", action="store_true", dest="eprofile",
                  help="produce an elevation profile listing (and graph if -g used too)")
parser.add_option("--dprofile", "--dprof", action="store_true", dest="dprofile",
                  help="produce a distance profile listing (and graph if -g used too")
parser.add_option("-g", "--graph", action="store_true", dest="graph",
                  help="produce graphical output using GnuPlot")
parser.add_option("-f", "--file", dest="outfile",
                  help="filename to use for output",
                  metavar="FILE")
parser.add_option("-v", "--verbose", action="store_true",dest="verbose",
                  help="Include verbose output")
parser.add_option("-d", "--debug", action="store_true",dest="debug",
                  help="Include debug output")
parser.set_defaults(
    s_seg = 0,
    s_pt = 0,
    e_seg = -1,
    e_pt = -1,
    filename="gpx_analsys.out",
    debug=False,
    verbose=False)
(options,args)=parser.parse_args()

if (options.debug):
    options.verbose = True
    print "options   = %s" % options
    print "arguments = %s" % args
        
if len(args)<1:
    print "Error: You must supply at least one GPX file to analyse.\n"
    sys.exit(-1)
else:
    pgpx = parseGPX(args[0],options.debug,options.verbose)

    if options.e_seg==-1:
        options.e_seg = pgpx.getNumTrkSeg()-1
        if options.debug: print "Default e_seg used - set to %d" % options.e_seg
    else:
        if options.debug: print "e_seg=%s" % options.e_seg


    if options.e_pt==-1:
        options.e_pt = pgpx.getNumPts(options.e_seg)-1
        if options.debug: print "Default e_pt used - set to %d" \
                % options.e_pt
    else:
        if options.debug: print "e_pt=%d" % options.e_pt

    if options.list:
        for seg in range(0,pgpx.getNumTrkSeg()):
            segData = pgpx.getTrackAnalysis(seg,0,seg,-1)
        #print "SegmentData    = %s" % segData
            sTimeStruct = localtime(segData['minTime'])
            eTimeStruct = localtime(segData['maxTime'])
            sTimeStr = strftime("%d/%m/%Y %X",sTimeStruct)
            eTimeStr = strftime("%d/%m/%Y %X",eTimeStruct)
            print "Segment %d: (%s to %s) - %d points." % \
                (seg,sTimeStr,eTimeStr,segData['npts'])


    if options.summary:
        segData = pgpx.getTrackAnalysis(options.s_seg,
                                        options.s_pt,
                                        options.e_seg,
                                        options.e_pt)
        if options.debug: print "Segment 0 Analysis is %s\n" % segData
        print "\n"
        print "\t\tWhereWasI Output"
        print "\t\t=================\n"
        print "\tFilename=%s" % args[0]
        print "\tNumber of Track Segments\t= %d" % pgpx.getNumTrkSeg()
        print "\t---------------------------------------------"
        print "\tStart Segment / Point \t= %d/%d" % \
            (options.s_seg,options.s_pt)
        print "\tEnd Segment / Point \t= %d/%d" % \
            (options.e_seg,options.e_pt)
        sTimeStruct = localtime(segData['minTime'])
        eTimeStruct = localtime(segData['maxTime'])
        sTimeStr = strftime("%d/%m/%Y %X",sTimeStruct)
        eTimeStr = strftime("%d/%m/%Y %X",eTimeStruct)
        print "\tStart Time            \t= %s" % sTimeStr
        print "\tEnd Time              \t= %s" % eTimeStr
        print "\tNumber of Track Points\t= %s" % segData['npts']
        print "\t---------------------------------------------"
        t_hrs = int(segData['time']/3600.0)
        t_min = int((segData['time']-3600.*t_hrs)/60.)
        print "\tTotal Time            \t= %02d:%02d" % (t_hrs,t_min)
        print "\tTotal Distance        \t= %5.2f km" % segData['dist']
        print "\tTotal Climb           \t= %5.0f m" % segData['climb']
        print "\t---------------------------------------------"
        print "\tAverage Speed         \t= %5.2f km/hr" % segData['avSpeed']
        print "\tMaximum Speed         \t= %5.2f km/hr" % segData['maxSpeed']
        print "\t---------------------------------------------\n"


    if options.eprofile or options.dprofile:
        if options.debug: print "profile listing"
        profdata = pgpx.getProfileData(options.s_seg,
                                       options.s_pt,
                                       options.e_seg,
                                       options.e_pt)
        if options.debug: print "profdata=%s" % profdata

        print "\n"
        print "\t\tanalyseGPX Output"
        print "\t\t=================\n"
        print "\tFilename=%s" % args[0]
        print "\tNumber of Track Segments\t= %d" % pgpx.getNumTrkSeg()
        print "\t---------------------------------------------"
        print "\tStart Segment / Point \t= %d/%d" % \
            (options.s_seg,options.s_pt)
        print "\tEnd Segment / Point \t= %d/%d" % \
            (options.e_seg,options.e_pt)
        print "\t---------------------------------------------"
        print "Date         Time          time_t      sec  Distance (km) Elevation (m)"
        edistprofXY = []
        etimeprofXY = []
        dtimeprofXY = []
        for rec in profdata:
            timeStruct = localtime(rec[0])
            timeStr = strftime("%d/%m/%Y %X",timeStruct)
            print "%s \t %d \t %d \t %6.2f \t %6.0f" % \
                (timeStr,rec[0],rec[1], rec[2], rec[3])
            edistprofXY.append([rec[2],rec[3]])
            etimeprofXY.append([rec[1]/3600.,rec[3]])
            dtimeprofXY.append([rec[1]/3600.,rec[2]])

        if options.graph:
            try:
		import Gnuplot
                g=Gnuplot.Gnuplot()
                if options.eprofile:
                    g.title("GPX Track Elevation Profile")
                    g.xlabel('Distance from Start (km)')
                    g.ylabel('Elevation (m)')
                    g.plot(edistprofXY)
                    raw_input('Please press return to continue...\n')
                    g.reset()
                if options.dprofile:
                    g.title("GPX Track Elevation Profile")
                    g.xlabel('Time from Start (hours)')
                    g.ylabel('Distance(km)')
                    g.plot(dtimeprofXY)
                    raw_input('Please press return to continue...\n')
                    g.reset()


            except: # python-gnuplot is not available or is broken
		print 'ERROR: python gnuplot interface is not found'



if not (options.summary or options.eprofile or \
            options.dprofile or options.list):
    print("Error - I think you should specify one of the output options?")
    print(" Something like --summary??")


#gui = analyseGPX_gui()
#gui.main()

if options.debug: print "WhereWasI exiting"


