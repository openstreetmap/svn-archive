#!/usr/bin/env python

"""Testcases for the whole project"""

# NOTE: Don't run these tests too often. They always download some amount of data.

import unittest
import srtm
import os
import hashlib

#file://localhost/usr/share/doc/python2.5-doc/html/lib/minimal-example.html

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

if __name__ == '__main__':
    unittest.main()
