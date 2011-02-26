#!/usr/bin/python

"""NED.py: NED (National Elevation Dataset) preprocessing for TopOSM."""

import numpy
import mapnik
from os import path, system
from math import floor, ceil

from env import *
from common import *

__author__      = "Lars Ahlzen"
__copyright__   = "(c) Lars Ahlzen 2008-2011"
__license__     = "GPLv2"

# Size (side length, in degrees) of NED tiles that are cut up into
# more manageable chunks.
STEP = 0.5

def getTilepath(basename):
    return os.path.join(
        NED13DIR, basename, 'grd' + basename + '_13', 'w001001.adf')

def getTiles(envLL):
    """Gets the (basename, envelope) of all (existing) 1/3 NED tiles
    to cover the specified envelope"""
    fromx = int(floor(envLL.minx))
    tox = int(floor(envLL.maxx))
    fromy = int(ceil(envLL.miny))
    toy = int(ceil(envLL.maxy))
    tiles = []
    for y in range(fromy, toy+1):
        for x in range(fromx, tox+1):
            basename = 'n%02dw%03d' % (y, -x)
            tilepath = getTilepath(basename)
            if path.isfile(tilepath):
                tiles.append((basename, mapnik.Envelope(x, y-1, x+1, y)))
    return tiles

def getSlice(prefix, x, y, suffix = '.tif'):
    filename = prefix + '_%.1f_%.1f%s' % (float(x), float(y), suffix)
    return path.join(TEMPDIR, filename)

def getSlices(prefix, envLL, suffix = '.tif'):
    fromx = floor(envLL.minx/STEP)*STEP
    fromy = floor(envLL.miny/STEP)*STEP
    tox = ceil(envLL.maxx/STEP)*STEP
    toy = ceil(envLL.maxy/STEP)*STEP
    slices = []
    for y in numpy.arange(fromy, toy, STEP):
        for x in numpy.arange(fromx, tox, STEP):
            slicefile = getSlice(prefix, x, y, suffix)
            if path.isfile(slicefile):
                slices.append(slicefile)
    return slices

def getTilecoords(lat, lon, step = 1):
    return (int(ceil(lat/float(step)))*float(step), \
        int(floor(lon/float(step)))*float(step))

def getTilename(lat, lon, step = 1):
    (y, x) = get_ned13_tilecoords(lat, lon, step)
    if step == 1:
        return 'n%02dw%03d' % (y, -x)
    return 'n%02.2fw%03.2f' % (y, -x)
                        
def prepDataFile(basename, env):
    print 'Preparing NED 1/3" data file:', basename
    print '  Converting to GeoTIFF...'
    sourcepath = getTilepath(basename)
    geotiff = path.join(TEMPDIR, basename + '.tif')
    if not path.isfile(geotiff):
        cmd = 'gdal_translate "%s" "%s"' % (sourcepath, geotiff)
        #call(cmd, shell = True)
        os.system(cmd)

    print '  Generating contour lines...'
    # split the GeoTIFF, since it's often too large otherwise
    for y in numpy.arange(env.miny, env.maxy, STEP):
        for x in numpy.arange(env.minx, env.maxx, STEP):
            print '  Cutting geotiff slice...'
            nedslice = getSlice('ned', x, y)
            if not path.isfile(nedslice):
                cmd = 'gdalwarp -q -te %f %f %f %f "%s" "%s"' % \
                    (x, y, x+STEP, y+STEP, geotiff, nedslice)
                #call(cmd, shell=True)
                os.system(cmd)
            
            print '  Generating contour lines...'
            contourbasefile = path.join(TEMPDIR, 'contours_' + str(x) + '_' + str(y))
            contourfile = contourbasefile + '.shp'
            if not path.isfile(contourfile):
                cmd = 'gdal_contour -i %f -snodata 32767 -a height "%s" "%s"' % \
                    (CONTOUR_INTERVAL, nedslice, contourfile)
                #call(cmd, shell=True)
                os.system(cmd)

                print '  Importing contour lines...'
                # NOTE: this assumes that the table is already set up
                cmd = 'shp2pgsql -a -g way "%s" "%s" | psql -q "%s"' % \
                    (contourfile, CONTOURS_TABLE, DATABASE)
                #call(cmd, shell=True)
                os.system(cmd)
                
                # Clear contents (but keep file to prevent us from importing
                # these contours again).
                writeEmpty(contourfile)
                
                # Remove shape index and attribute files.
                tryRemove(contourbasefile + ".shx")
                tryRemove(contourbasefile + ".dbf")

            print '  Generating hillshade slice...'
            hillshadeslice = getSlice('hillshade', x, y)
            hillshadeslicePng = getSlice('hillshade', x, y, '.png')
            if not path.isfile(hillshadeslicePng):
                cmd = '"%s" "%s" "%s" -z 0.00001' % \
                    (HILLSHADE, nedslice, hillshadeslice)
                #call(cmd, shell = True)
                os.system(cmd)
                # convert to PNG + world file to save space
                cmd = 'gdal_translate -quiet -of PNG -co WORLDFILE=YES "%s" "%s"' % \
                    (hillshadeslice, hillshadeslicePng)
                #call(cmd, shell = True)
                os.system(cmd)
                tryRemove(hillshadeslice)

            print '  Generating colormap slice...'
            colormapslice = getSlice('colormap', x, y)
            colormapslicePng = getSlice('colormap', x, y, '.png')
            if not path.isfile(colormapslicePng):
                cmd = '"%s" "%s" "%s" "%s"' % \
                    (COLORRELIEF, nedslice, COLORFILE, colormapslice)
                #call(cmd, shell = True)
                os.system(cmd)
                # convert to PNG + world file to save space
                cmd = 'gdal_translate -quiet -of PNG -co WORLDFILE=YES "%s" "%s"' % \
                    (colormapslice, colormapslicePng)
                #call(cmd, shell = True)
                os.system(cmd)
                tryRemove(colormapslice)

            writeEmpty(nedslice) # don't need the raw slice anymore.
                
    writeEmpty(geotiff) # done with this GeoTIFF.

