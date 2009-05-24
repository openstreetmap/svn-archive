#!/usr/bin/env python2.5
"""Profile timecritical functions."""

import srtm
if True: #Enable or disable psyco here
    try:
        import psyco
        psyco.full()
        #psyco.log()
        #psyco.profile()
        print "Psyco loaded!"
    except ImportError:
        print "Psyco not found!"

downloader =  srtm.SRTMDownloader(cachedir="testcache")
downloader.loadFileList()
tile       = downloader.getTile(49, 11)

def test1():
    """Call getAltitudeFromLatLon many times."""
    testvalue = 10000000
    for i in range(testvalue):
        f = float(i)/testvalue
        tile.getAltitudeFromLatLon(49.0+f, 11.0+f)

test1()

# Profiling slows done the execution a lot.
# Use fewer iterations to get results in reasonable time!
# Profiling doesn't work with psyco.

#import hotshot.stats
#import hotshot
#prof = hotshot.Profile("test1.prof")
#prof.runcall(test1)
#prof.close()
#stats = hotshot.stats.load("test1.prof")
#stats.strip_dirs()
#stats.sort_stats('time', 'calls')
#stats.print_stats(20)