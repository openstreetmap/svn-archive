#!/usr/bin/env python

"""Testcases for the whole project"""

import unittest
import srtm
import os
import hashlib
#Psyco provides about 3x speedup
try:
    import psyco
    #psyco.full()
    psyco.log()
    psyco.profile()
except ImportError:
    pass

class DownloaderTest(unittest.TestCase):
    """Testcases for the SRTMDownloader class"""
    def setUp(self):
        #try:
            #os.remove("testcache/N00E010.hgt.zip")
        #except:
            #pass
        #try:
            #os.remove("testcache/filelist")
        #except:
            #pass
        #try:
            #os.rmdir("testcache")
        #except Exception, e:
            #print e
        self.downloader = srtm.SRTMDownloader(cachedir="testcache")

    def testFilenameParser(self):
        """Check filename parser"""
        self.assertEqual(self.downloader.parseFilename("S01W002.hgt.zip"), ( -1,   -2))
        self.assertEqual(self.downloader.parseFilename("S10E020.hgt.zip"), (-10,   20))
        self.assertEqual(self.downloader.parseFilename("N10W150.hgt.zip"), ( 10, -150))
        self.assertEqual(self.downloader.parseFilename("N89E179.hgt.zip"), ( 89,  179))
        self.assertEqual(self.downloader.parseFilename("S010W020.hgt.zip"), None)
        self.assertEqual(self.downloader.parseFilename("S10W20.hgt.zip"),  None)
        self.assertEqual(self.downloader.parseFilename("X10W020.hgt.zip"), None)
        self.assertEqual(self.downloader.parseFilename("S10Y020.hgt.zip"), None)

    def testFindTile(self):
        """Check that tiles can either be found or an exception is raised"""
        #Start with no tile list
        self.assertRaises(srtm.NoSuchTileError, self.downloader.getTile, 0, 0)
        self.assertRaises(srtm.NoSuchTileError, self.downloader.getTile, 0, 10)
        #Load tile list
        self.downloader.loadFileList()
        # Tile for 0 0 still does not exist
        self.assertRaises(srtm.NoSuchTileError, self.downloader.getTile, 0, 0)
        # Tile for 0 10 should be availabe now
        self.downloader.getTile(0, 10)

    def testDownload(self):
        """Check if downloading a tile works and if the tile data is correct"""
        self.downloader.loadFileList()
        self.downloader.downloadTile("Africa", "N00E010.hgt.zip")
        self.assert_(os.path.exists("testcache/N00E010.hgt.zip"))
        self.assert_(hashlib.sha1(open("testcache/N00E010.hgt.zip", 'rb').
            read()).hexdigest() == "168b1fddb4f22b8cdc6523ff0207e0eb6be314af")

class TileTest(unittest.TestCase):
    def setUp(self):
        self.downloader =  srtm.SRTMDownloader(cachedir="testcache")
        self.downloader.loadFileList()
        self.tile      = self.downloader.getTile(49, 11)
        self.tileNorth = self.downloader.getTile(50, 11)
        self.tileEast  = self.downloader.getTile(49, 12)

    def testOffset(self):
        #  (0/1200)     0
        #  (1200/1200)  1200
        #  (0/1199)     1201
        #  (1200/1199)  2401
        #  (0/0)        1201*1200
        #  (1200/0)     1201*1201-1
        self.assertEqual(self.tile.calcOffset(0, 1200), 0)
        self.assertEqual(self.tile.calcOffset(1200, 1200), 1200)
        self.assertEqual(self.tile.calcOffset(0, 1199), 1201)
        self.assertEqual(self.tile.calcOffset(1, 1199), 1202)
        
        self.assertEqual(self.tile.calcOffset(1200, 1199), 2401)
        self.assertEqual(self.tile.calcOffset(0, 0), 1201*1200)
        self.assertEqual(self.tile.calcOffset(1200, 0), 1201*1201-1)

    def testNeighbouringLatLon(self):
        # 5123 is a random number. It should not be an integer divider or multiple of 1199, 1200 or 1201
        for testvalue in (1199, 1200, 1201, 5123, 50, 1000000):
            for i in range(testvalue):
                f = float(i)/testvalue
                self.assertAlmostEqualInt(self.tile.getAltitudeFromLatLon(49.999999, 11+f), self.tileNorth.getAltitudeFromLatLon(50, 11+f))
                self.assertAlmostEqualInt(self.tile.getAltitudeFromLatLon(49+f, 11.999999), self.tileEast.getAltitudeFromLatLon(49+f, 12))

    def assertAlmostEqualInt(self, a, b):
        a = int(a)
        b = int(b)
        if a == b:
            return
        if a + 1 == b:
            return
        if a - 1 == b:
            return
        self.fail("%d != %d (almost equal test)" % (a, b))

    def testNeighbouringXY(self):
        for i in range(1201):
            self.assertEqual(self.tile.getPixelValue(i, 1200),
                            self.tileNorth.getPixelValue(i, 0))
            self.assertEqual(self.tile.getPixelValue(1200, i),
                            self.tileEast.getPixelValue(0, i))

if __name__ == '__main__':
    unittest.main()
