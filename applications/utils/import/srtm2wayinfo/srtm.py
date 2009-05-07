#!/usr/bin/env python

"""Load and process SRTM data."""

from __future__ import with_statement
import ftplib
import re
import pickle
import os.path
import os

class NoSuchTileError(Exception):
    """Raised when there is no tile for a region."""
    def __init__(self, lat, lon):
        self.lat = lat
        self.lon = lon

    def __str__(self):
        return "No SRTM tile for %d %d available!" % (self.lat, self.lon)


class SRTMDownloader:
    """Automatically download SRTM tiles."""
    def __init__(self, server="e0srp01u.ecs.nasa.gov",
            directory="/srtm/version2/SRTM3", cachedir="cache"):
        self.server = server
        self.directory = directory
        self.cachedir = cachedir
        if not os.path.exists(cachedir):
            os.mkdir(cachedir)
        self.filelist = {}
        self.filename_regex = re.compile(r"([NS])(\d{2})([EW])(\d{3})\.hgt\.zip")
        self.filelist_file = self.cachedir + "/filelist"
        self.ftpfile = None
        self.ftp_bytes_transfered = 0

    def loadFileList(self):
        """Load a previously created file list or create a new one if none is
            available."""
        try:
            data = open(self.filelist_file, 'rb')
        except IOError:
            print "No cached file list. Creating new one!"
            self.createFileList()
            return
        try:
            self.filelist = pickle.load(data)
        except:
            print "Unknown error loading cached file list. Creating new one!"
            self.createFileList()

    def createFileList(self):
        """SRTM data is split into different directories, get a list of all of
            them and create a dictionary for easy lookup."""
        ftp = ftplib.FTP(self.server)
        try:
            ftp.login()
            ftp.cwd(self.directory)
            continents = ftp.nlst()
            for continent in continents:
                print "Downloading file list for", continent
                ftp.cwd(self.directory+"/"+continent)
                files = ftp.nlst()
                for filename in files:
                    self.filelist[self.parseFilename(filename)] = (continent, filename)
        finally:
            ftp.close()
        # Add meta info
        self.filelist["server"] = self.server
        self.filelist["directory"] = self.directory
        with open(self.filelist_file , 'wb') as output:
            pickle.dump(self.filelist, output)

    def parseFilename(self, filename):
        """Get lat/lon values from filename."""
        match = self.filename_regex.match(filename)
        if match is None:
            # TODO?: Raise exception?
            print "Filename", filename, "unrecognized!"
            return None
        lat = int(match.group(2))
        lon = int(match.group(4))
        if match.group(1) == "S":
            lat = -lat
        if match.group(3) == "W":
            lon = -lon
        return lat, lon

    def getTile(self, lat, lon):
        """Get a SRTM tile object. This function can return either an SRTM1 or
            SRTM3 object depending on what is available, however currently it
            only returns SRTM3 objects."""
        try:
            continent, filename = self.filelist[(lat, lon)]
        except KeyError:
            raise NoSuchTileError(lat, lon)
        print filename
        if not os.path.exists(self.cachedir + "/" + filename):
            self.downloadTile(continent, filename)
        f = open(self.cachedir + "/" + filename, 'rb')
        return SRTM3Tile(f)

    def downloadTile(self, continent, filename):
        """Download a tile from NASA's server and store it in the cache."""
        ftp = ftplib.FTP(self.server)
        try:
            ftp.login()
            ftp.cwd(self.directory+"/"+continent)
            # WARNING: This is not thread safe
            self.ftpfile = open(self.cachedir + "/" + filename, 'wb')
            self.ftp_bytes_transfered = 0
            print ""
            try:
                ftp.retrbinary("RETR "+filename, self.ftpCallback)
            finally:
                self.ftpfile.close()
                self.ftpfile = None
        finally:
            ftp.close()

    def ftpCallback(self, data):
        """Called by ftplib when some bytes have been received."""
        self.ftpfile.write(data)
        self.ftp_bytes_transfered += len(data)
        print "\r%d bytes transfered" % self.ftp_bytes_transfered,


class SRTMTile:
    """Base class for all SRTM tiles."""
    def __init__(self, f, size, lat, lon):
        self.size = size
        self.f = f
        self.lat = lat
        self.lon = lon


class SRTM3Tile(SRTMTile):
    """SRTM class handling the specifics of SRTM3 tiles."""
    def __init__(self, f, lat, lon):
        SRTMTile.__init__(f, 1201, lat, lon)

#DEBUG ONLY
if __name__ == '__main__':
    x = SRTMDownloader()
    x.loadFileList()
    x.getTile(0, 10)
    #x.createFileList()
