#!/usr/bin/python
import sys, os, time, threading
import numpy
#import Image # PIL
from Queue import Queue
from os import path
from math import pi,cos,sin,log,exp,atan,ceil,floor
from subprocess import call
from mapnik import *


# Check that the environment is set and import configuration
if not 'TOPOSM_ENV_SET' in os.environ:
    print "Error: TopOSM environment not set."
    sys.exit(1)
BASE_TILE_DIR = os.environ['BASE_TILE_DIR']
CONTOURS_TABLE = os.environ['CONTOURS_TABLE']
DATABASE = os.environ['DB_NAME']
TEMPDIR = os.environ['TEMP_DIR']
NED13DIR = os.environ['NED13_DIR']
HILLSHADE = os.environ['HILLSHADE']
COLORRELIEF = os.environ['COLORRELIEF']
COLORFILE = os.environ['COLORFILE']
NUM_THREADS = int(os.environ['RENDER_THREADS'])
SUBTILE_SIZE = int(os.environ['SUBTILE_SIZE'])
BORDER_WIDTH = int(os.environ['BORDER_WIDTH'])
ERRORLOG = os.environ['ERROR_LOG']

# constants
CONTOUR_INTERVAL = 15.24 # 50 ft in meters
MAPNIK_LAYERS = ['watermask', 'area', 'areansh', 'contourlines', 'contourlabels',
                 'features-main', 'features-fill', 'labels', 'labels-nohalo']
OSM_SRS = '+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 ' + \
    '+y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs +over'

# Optimal supertile size (N x N subtiles) by zoom level.
# A too low number is inefficient. A too high number uses
# large amounts of memory and sometimes breaks the gdal tools.
NTILES = {2:1, 3:1, 4:1, 5:1, 6:1, 7:1, 8:1, 9:1, 10:1,
    11:2, 12:4, 13:6, 14:8, 15:10, 16:12, 17:12, 18:12 }

# side length (in degrees) for slicing up NED files into more
# manageable pieces during preprocessing
STEP=0.5

DEG_TO_RAD = pi/180
RAD_TO_DEG = 180/pi

def minmax (a,b,c):
    a = max(a,b)
    a = min(a,c)
    return a

class GoogleProjection:
    def __init__(self,levels=20):
        self.Bc = []
        self.Cc = []
        self.zc = []
        self.Ac = []
        c = 256
        for d in range(0,levels):
            e = c/2;
            self.Bc.append(c/360.0)
            self.Cc.append(c/(2 * pi))
            self.zc.append((e,e))
            self.Ac.append(c)
            c *= 2
                
    def fromLLtoPixel(self,ll,zoom):
         d = self.zc[zoom]
         e = round(d[0] + ll[0] * self.Bc[zoom])
         f = minmax(sin(DEG_TO_RAD * ll[1]),-0.9999,0.9999)
         g = round(d[1] + 0.5*log((1+f)/(1-f))*-self.Cc[zoom])
         return (e,g)
     
    def fromPixelToLL(self,px,zoom):
         e = self.zc[zoom]
         f = (px[0] - e[0])/self.Bc[zoom]
         g = (px[1] - e[1])/-self.Cc[zoom]
         h = RAD_TO_DEG * ( 2 * atan(exp(g)) - 0.5 * pi)
         return (f,h)

GOOGLE_PROJECTION = GoogleProjection()

LATLONG_PROJECTION_DEF = "+proj=latlong"
LATLONG_PROJECTION = Projection(LATLONG_PROJECTION_DEF)

MERCATOR_PROJECTION_DEF = "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs +over"
MERCATOR_PROJECTION = Projection(MERCATOR_PROJECTION_DEF)

# global locking object for file system ops (e.g. creating directories)
fslock = threading.Lock()

# thread-safe console printing access
printlock = threading.Lock()
def print_message(message):
    printlock.acquire()
    print message
    printlock.release()

# error log
errorloglock = threading.Lock()
def log_error(message, exception = None):
    errorloglock.acquire()
    try:
        file = open(ERRORLOG, 'a')
        file.write(message)
        if exception != None:
            file.write(" %s %s\n" % \
                (str(exception), exception.message))
        file.write('\n')
        file.close()
    except:
        print "Failed to write to the error log:"
        print "%s %s" % (sys.exc_info()[0], sys.exc_info()[1])
    finally:
        errorloglock.release()

class RenderThread:
    def __init__(self, q, maxz, threadNumber):
        self.q = q
        self.maxz = maxz
        self.threadNumber = threadNumber
        self.currentz = 0
        
    def init_zoomlevel(self, z):
        self.currentz = z
        self.ntiles = NTILES[z]
        self.tilesize = getTileSize(self.ntiles, True)
        self.maps = {}
        for mapName in MAPNIK_LAYERS:
            self.maps[mapName] = Map(self.tilesize, self.tilesize)
            load_map(self.maps[mapName], mapName + ".xml")

    def print_message(self, message):
        print_message("[%02d] %s" % (self.threadNumber+1, message))

    def prepare_data(self, basename, env):
        self.print_message("Preparing %s at %s" % (basename, env))
        try:
            prep_ned13_data_file(basename, env)
        except Exception as ex:
            message = "Failed prepare_data %s %s" % (basename, env)
            self.print_message(message)
            log_error(message, ex) 

    def render_topo_tiles(self, z, x, y):
        ntiles = NTILES[z]
        for mapname in ['hillshade', 'colormap']:    
            self.print_message("Rendering %s %s %s %s" % (mapname, z, x, y))
            try:
                render_geotiff_tile(z, x, y, ntiles, mapname)
            except Exception as ex:
                message = "Failed render_topo_tiles %s %s %s %s" % (mapname, z, x, y)
                self.print_message(message)
                log_error(message, ex)

    def render_mapnik_tiles(self, z, x, y):
        if (z != self.currentz):
            self.init_zoomlevel(z)
        for mapname in self.maps.keys():
            self.print_message("Rendering %s %s %s %s" % (mapname, z, x, y))
            try:
                render_mapnik_tile(z, x, y, self.ntiles, mapname, self.maps[mapname])
            except Exception as ex:
                message = "Failed render_mapnik_tiles %s %s %s %s" % (mapname, z, x, y)
                self.print_message(message)
                log_error(message, ex)

    def combine_topo_tiles(self, z, x, y):
        ntiles = NTILES[z]
        if allSubtilesExist('color-relief', z, x, x+ntiles-1, y, y+ntiles-1, '.jpg'):
            self.print_message("All topo subtiles exist. Skipping.")
            return
        try:
            self.print_message("Combining topo tiles at %s %s %s" % (z, x, y))
            command = "./combine-color-relief-tiles %s %d %d %d %d" % \
                (BASE_TILE_DIR, z, x, y, getTileSize(ntiles, False));
            call(command, shell=True)
            slice_tile(z, x, y, ntiles, 'color-relief', '.jpg')
        except Exception as ex:
            message = "Failed combine_topo_tiles %s %s %s" % (z, x, y)
            self.print_message(message)
            log_error(message, ex)

    def combine_mapnik_tiles(self, z, x, y):
        ntiles = NTILES[z]
        if allSubtilesExist('contours', z, x, x+ntiles-1, y, y+ntiles-1, '.png') and \
            allSubtilesExist('features', z, x, x+ntiles-1, y, y+ntiles-1, '.png'):
            self.print_message("All mapnik subtiles exist. Skipping.")
            return
        try:
            self.print_message("Combining mapnik tiles at %s %s %s" % (z, x, y))
            command = "./combine-mapnik-tiles %s %d %d %d %d" % \
                (BASE_TILE_DIR, z, x, y, getTileSize(ntiles, False));
            call(command, shell=True)
            slice_tile(z, x, y, ntiles, 'contours', '.png')
            slice_tile(z, x, y, ntiles, 'features', '.png')
        except Exception as ex:
            message = "Failed combine_mapnik_tiles %s %s %s" % (z, x, y)
            self.print_message(message)
            log_error(message, ex)

    def merge_topo_subtiles(self, z, x, y):
        ntiles = NTILES[z]
        for dx in range(x, x+ntiles):
            for dy in range(y, y+ntiles):
                if subtileExists('color-relief', z, dx, dy, '.jpg'):
                    self.print_message('Subtile exists. Skipping.')
                    continue
                try:
                    self.print_message("Merging subtiles at %s %s %s" % (z, dx, dy))
                    merge_subtiles(z, dx, dy, 'color-relief', '.jpg')
                except Exception as ex:
                    message = "Failed merge_subtiles %s %s %s" % (z, dx, dy)
                    self.print_message(message)
                    log_error(message, ex)
            
    def render_loop(self):
        self.currentz = 0
        while True:
            r = self.q.get()
            if (r == None):
                self.q.task_done()
                break
            (action, z, x, y) = r
            self.render(action, z, x, y)
            self.q.task_done()

    def render(self, action, z, x, y):
        if action == 'prepare_data':
            self.prepare_data(x, y)
        elif action == 'render':
            # NOTE: Mapnik tiles must be rendered before topo
            if allFinalMapnikSubtilesExist(z, x, y):
                self.print_message('Mapnik tiles exist. Skipping.')
            else:
                self.render_mapnik_tiles(z, x, y)
                self.combine_mapnik_tiles(z, x, y)
            if allFinalTopoSubtilesExist(z, x, y):
                self.print_message('Topo exist at %d %d %d.' % (z, x, y))
            else:
                if z == self.maxz:
                    self.render_topo_tiles(z, x, y)
                    self.combine_topo_tiles(z, x, y)
                else:
                    self.merge_topo_subtiles(z, x, y)     

def getTileDir(mapname, z):
    return path.join(BASE_TILE_DIR, mapname, str(z))

def getTilePath(mapname, z, x, y, suffix = ".png"):
    return path.join(getTileDir(mapname, z), 's' + str(x) + '_' + str(y) + suffix)

def getSubtileDir(mapname, z, x):
    return path.join(getTileDir(mapname, z), str(x))

def getSubtilePath(mapname, z, x, y, suffix = ".png"):
    return path.join(getSubtileDir(mapname, z, x), str(y) + suffix)

def getTileSize(ntiles, includeBorder = True):
    if includeBorder:
        return SUBTILE_SIZE * ntiles + 2 * BORDER_WIDTH;
    else:
        return SUBTILE_SIZE * ntiles;

def tileExists(mapname, z, x, y, suffix = ".png"):
    return path.isfile(getTilePath(mapname, z, x, y, suffix))

def subtileExists(mapname, z, x, y, suffix = ".png"):
    return path.isfile(getSubtilePath(mapname, z, x, y, suffix))
    
def allSubtilesExist(mapname, z, fromx, tox, fromy, toy, suffix = ".png"):
    for x in range(fromx, tox+1):
        for y in range(fromy, toy+1):
            if not subtileExists(mapname, z, x, y, suffix):
                return False
    return True

def allFinalTopoSubtilesExist(z, x, y):
    ntiles = NTILES[z]
    return allSubtilesExist('color-relief', z, x, x+ntiles-1, y, y+ntiles-1, '.jpg')

def allFinalMapnikSubtilesExist(z, x, y):
    ntiles = NTILES[z]
    return \
        allSubtilesExist('contours', z, x, x+ntiles-1, y, y+ntiles-1, '.png') and \
        allSubtilesExist('features', z, x, x+ntiles-1, y, y+ntiles-1, '.png')

def ensureDirExists(path):
    fslock.acquire()
    try:
        if not os.path.isdir(path):
            os.makedirs(path)
    finally:
        fslock.release()

def tryRemove(filename):
    fslock.acquire()
    try:
	    if path.isfile(filename):
		    os.remove(filename)
    finally:
        fslock.release()

def writeEmpty(filename):
	"Overwrites the specified filename with a new empty file."
	fslock.acquire()
	try:
	    open(filename, 'w').close();
	finally:
	    fslock.release()
	
def render_mapnik_tile(z, x, y, ntiles, mapname, map):
    bboxMerc = get_bbox_merc(z, x, y, ntiles, True)
    to_x = x + ntiles
    to_y = y + ntiles
    if tileExists(mapname, z, x, y, '.png'):
        pass
    else:
        map.zoom_to_box(bboxMerc)
        tilesize = getTileSize(ntiles, True)
        image = Image(tilesize, tilesize)
        render(map, image)
        view = image.view(BORDER_WIDTH, BORDER_WIDTH, tilesize, tilesize)
        ensureDirExists(getTileDir(mapname, z))
        view.save(getTilePath(mapname, z, x, y, '.png'))

def get_bbox_ll(z, x, y, ntiles, includeBorder = True):
    "Returns the lat/lon bbox for the specified tile"
    border = 0
    if includeBorder:
        border = BORDER_WIDTH
    p0 = GOOGLE_PROJECTION.fromPixelToLL(
        (x * SUBTILE_SIZE - border,
         (y + ntiles) * SUBTILE_SIZE + border), z)
    p1 = GOOGLE_PROJECTION.fromPixelToLL(
        ((x + ntiles) * SUBTILE_SIZE + border,
         y * SUBTILE_SIZE - border), z)
    return Envelope(p0[0], p0[1], p1[0], p1[1])
					
def get_bbox_merc(z, x, y, ntiles, includeBorder = True):
    "Returns the mercator bbox for the specified tile"
    border = 0
    if includeBorder:
        border = BORDER_WIDTH
    p0 = GOOGLE_PROJECTION.fromPixelToLL(
        (x * SUBTILE_SIZE - border,
         (y + ntiles) * SUBTILE_SIZE + border), z)
    p1 = GOOGLE_PROJECTION.fromPixelToLL(
        ((x + ntiles) * SUBTILE_SIZE + border,
         y * SUBTILE_SIZE - border), z)
    c0 = MERCATOR_PROJECTION.forward(Coord(p0[0], p0[1]))
    c1 = MERCATOR_PROJECTION.forward(Coord(p1[0], p1[1]))            
    return Envelope(c0.x, c0.y, c1.x, c1.y)

def get_tile_from_ll(lat, lon, z):
    "Returns the subtile number (x, y) at the specified lat/lon/zoom"
    (px,py) = GOOGLE_PROJECTION.fromLLtoPixel((lon,lat),z)
    return ((int)(px/SUBTILE_SIZE), (int)(py/SUBTILE_SIZE))

def get_ned13_tilepath(basename):
    return os.path.join(NED13DIR, basename, 'grd' + basename + '_13', 'w001001.adf')

def get_ned13_tiles(envLL):
    "Gets the (basename, envelope) of all (existing) 1/3 NED tiles to cover the specified envelope"
    fromx = int(floor(envLL.minx))
    tox = int(floor(envLL.maxx))
    fromy = int(ceil(envLL.miny))
    toy = int(ceil(envLL.maxy))
    tiles = []
    for y in range(fromy, toy+1):
        for x in range(fromx, tox+1):
            basename = 'n%02dw%03d' % (y, -x)
            tilepath = get_ned13_tilepath(basename)
            if os.path.isfile(tilepath):
                tiles.append((basename, Envelope(x, y-1, x+1, y)))
    return tiles

def get_ned13_slice(prefix, x, y, suffix = '.tif'):
    filename = prefix + '_%.1f_%.1f%s' % (float(x), float(y), suffix)
    return path.join(TEMPDIR, filename)

def get_ned13_slices(prefix, envLL, suffix = '.tif'):
    fromx = floor(envLL.minx/STEP)*STEP
    fromy = floor(envLL.miny/STEP)*STEP
    tox = ceil(envLL.maxx/STEP)*STEP
    toy = ceil(envLL.maxy/STEP)*STEP
    slices = []
    for y in numpy.arange(fromy, toy, STEP):
        for x in numpy.arange(fromx, tox, STEP):
            slicefile = get_ned13_slice(prefix, x, y, suffix)
            if os.path.isfile(slicefile):
                slices.append(slicefile)
    return slices

def get_ned13_tilecoords(lat, lon, step = 1):
    return (int(ceil(lat/float(step)))*float(step), int(floor(lon/float(step)))*float(step))

def get_ned13_tilename(lat, lon, step = 1):
    (y, x) = get_ned13_tilecoords(lat, lon, step)
    if step == 1:
        return 'n%02dw%03d' % (y, -x)
    return 'n%02.2fw%03.2f' % (y, -x)
                        
def prep_ned13_data_file(basename, env):
    print 'Preparing NED 1/3" data file:', basename
    print '  Converting to GeoTIFF...'
    sourcepath = get_ned13_tilepath(basename)
    geotiff = path.join(TEMPDIR, basename + '.tif')
    if not path.isfile(geotiff):
        cmd = 'gdal_translate "%s" "%s"' % (sourcepath, geotiff)
        call(cmd, shell = True)

    print '  Generating contour lines...'
    # split the GeoTIFF, since it's often too large otherwise
    for y in numpy.arange(env.miny, env.maxy, STEP):
        for x in numpy.arange(env.minx, env.maxx, STEP):
            print '  Cutting geotiff slice...'
            nedslice = get_ned13_slice('ned', x, y)
            if not path.isfile(nedslice):
                cmd = 'gdalwarp -q -te %f %f %f %f "%s" "%s"' % \
                    (x, y, x+STEP, y+STEP, geotiff, nedslice)
                call(cmd, shell=True)
            
            print '  Generating contour lines...'
            contourbasefile = path.join(TEMPDIR, 'contours_' + str(x) + '_' + str(y))
            contourfile = contourbasefile + '.shp'
            if not path.isfile(contourfile):
                cmd = 'gdal_contour -i %f -snodata 32767 -a height "%s" "%s"' % \
                    (CONTOUR_INTERVAL, nedslice, contourfile)
                call(cmd, shell=True)

                print '  Importing contour lines...'
                # NOTE: this assumes that the table is already set up
                cmd = 'shp2pgsql -a -g way "%s" "%s" | psql -q "%s"' % \
                    (contourfile, CONTOURS_TABLE, DATABASE)
                call(cmd, shell=True)
                
                # Clear contents (but keep file to prevent us from importing
                # these contours again).
                writeEmpty(contourfile)
                
                # Remove shape index and attribute files.
                tryRemove(contourbasefile + ".shx")
                tryRemove(contourbasefile + ".dbf")

            print '  Generating hillshade slice...'
            hillshadeslice = get_ned13_slice('hillshade', x, y)
            hillshadeslicePng = get_ned13_slice('hillshade', x, y, '.png')
            if not path.isfile(hillshadeslicePng):
                cmd = '"%s" "%s" "%s" -z 0.00001' % \
                    (HILLSHADE, nedslice, hillshadeslice)
                call(cmd, shell = True)
                # convert to PNG + world file to save space
                cmd = 'gdal_translate -quiet -of PNG -co WORLDFILE=YES "%s" "%s"' % \
                    (hillshadeslice, hillshadeslicePng)
                call(cmd, shell = True)
                tryRemove(hillshadeslice)

            print '  Generating colormap slice...'
            colormapslice = get_ned13_slice('colormap', x, y)
            colormapslicePng = get_ned13_slice('colormap', x, y, '.png')
            if not path.isfile(colormapslicePng):
                cmd = '"%s" "%s" "%s" "%s"' % \
                    (COLORRELIEF, nedslice, COLORFILE, colormapslice)
                call(cmd, shell = True)
                # convert to PNG + world file to save space
                cmd = 'gdal_translate -quiet -of PNG -co WORLDFILE=YES "%s" "%s"' % \
                    (colormapslice, colormapslicePng)
                call(cmd, shell = True)
                tryRemove(colormapslice)

            writeEmpty(nedslice) # don't need the raw slice anymore.
                
    writeEmpty(geotiff) # done with this GeoTIFF.

def pad_envelope(envLL, z, ntiles):
    """Extends the specified area with the width/height of a subtile toward
    increasing x/y, plus one unit of padding on all sides. Used to ensure that
    enough data is included for rendering a set of tiles."""
    (xmin, ymax) = GOOGLE_PROJECTION.fromLLtoPixel((envLL.minx, envLL.miny), z)
    (xmax, ymin) = GOOGLE_PROJECTION.fromLLtoPixel((envLL.maxx, envLL.maxy), z)
    xmin -= BORDER_WIDTH
    ymin -= BORDER_WIDTH
    xmax += BORDER_WIDTH + SUBTILE_SIZE
    ymax += BORDER_WIDTH + SUBTILE_SIZE
    (lonmin, latmin) = GOOGLE_PROJECTION.fromPixelToLL((xmin, ymax), z)
    (lonmax, latmax) = GOOGLE_PROJECTION.fromPixelToLL((xmax, ymin), z)
    return Envelope(lonmin, latmin, lonmax, latmax)

# TODO: rename method... e.g. render_topo_tile
def render_geotiff_tile(z, x, y, ntiles, mapname):
    bbox = get_bbox_merc(z, x, y, ntiles, False)
    bboxLL = get_bbox_ll(z, x, y, ntiles, False)
    destdir = getTileDir(mapname, z)
    # NOTE: gdalwarp won't save as png directly, hence this "hack"
    destTilePath = getTilePath(mapname, z, x, y, '.tif')
    finalTilePath = getTilePath(mapname, z, x, y, '.png')
    if os.path.isfile(destTilePath):
        pass
    else:
        ensureDirExists(destdir)
        nedSlices = get_ned13_slices(mapname, bboxLL, '.png')
        tilesize = getTileSize(ntiles, False)
        if len(nedSlices) > 0:
            cmd = 'gdalwarp -q -t_srs "%s" -te %f %f %f %f -ts %d %d -r lanczos -dstnodata 255 ' % \
                (OSM_SRS, bbox.minx, bbox.miny, bbox.maxx, bbox.maxy, tilesize, tilesize)
            for slice in nedSlices:
                cmd = cmd + '"' + slice + '" '
            cmd = cmd + '"' + destTilePath + '"'
            print cmd
            call(cmd, shell=True)
            # convert to png and remove tif (to conserve space)
            cmd = 'convert "%s" "%s" && rm "%s"' % (destTilePath, finalTilePath, destTilePath)
            call(cmd, shell=True)
        else:
            return False
    return True

def slice_tile(z, x, y, ntiles, mapname, destSuffix = '.png'):
    srcTilePath = getTilePath(mapname, z, x, y, '.png')
    for dx in range(0, ntiles):
        destDir = path.join(getTileDir(mapname, z), str(x+dx))
        ensureDirExists(destDir)
        for dy in range(0, ntiles):
            destfile = path.join(destDir, str(y+dy) + destSuffix)
            if not path.isfile(destfile):
                offsetx = dx * SUBTILE_SIZE
                offsety = dy * SUBTILE_SIZE
                cmd = 'convert "%s" -crop %dx%d+%d+%d +repage "%s"' % \
                    (srcTilePath, SUBTILE_SIZE, SUBTILE_SIZE, offsetx, offsety,
                     path.join(destDir, str(y+dy) + destSuffix))
                call(cmd, shell=True)
            else:
                pass
    tryRemove(srcTilePath)


def merge_subtiles(z, x, y, mapname, suffix = '.jpg'):
    """Merges (up to) four subtiles from the next higher
    zoom level into one subtile at the specified location"""
#    print 'Merging tile ', z, x, y, mapname
    cmd = 'convert -size 512x512 xc:white'
    for dx in range(0,2):
        for dy in range(0,2):
            srcx = x*2 + dx
            srcy = y*2 + dy
            srcpath = getSubtilePath(mapname, z+1, srcx, srcy, suffix)
            if os.path.isfile(srcpath):
                cmd = cmd + ' "' + srcpath + '"'
                cmd = cmd + ' -geometry +' + str(dx*256) + '+' + str(dy*256)
                cmd = cmd + ' -composite'
    cmd = cmd + ' -scale 256x256'
    ensureDirExists(getSubtileDir(mapname, z, x))
    destpath = getSubtilePath(mapname, z, x, y, suffix)
    cmd = cmd + ' "' + destpath + '"'
    call(cmd, shell=True)

def create_jpeg_tile(z, x, y, quality):
    print 'Creating jpeg tile at', z, x, y, '...',
    colorreliefsrc = getSubtilePath('color-relief', z, x, y, '.jpg')
    contourssrc = getSubtilePath('contours', z, x, y, '.png')
    featuressrc = getSubtilePath('features', z, x, y, '.png')
    desttile = getSubtilePath('jpeg' + str(quality), z, x, y, '.jpg')
    if path.isfile(colorreliefsrc) and path.isfile(featuressrc):
        ensureDirExists(path.dirname(desttile))

        #tile = Image.open(colorreliefsrc)
        #if path.isfile(contourssrc):
        #    contours = Image.open(contourssrc)
        #    tile.paste(contours, None)
        #features = Image.open(featuressrc)
        #tile.paste(features, None)
        #ensure_dir_exists(path.dirname(desttile))
        #tile.save(desttile, 'JPEG', quality=quality, optimize=True)
        
        # PIL generates internal errors opening the JPEG
        # tiles so it's back to ImageMagick for now...
        cmd = "convert " + colorreliefsrc;
        if path.isfile(contourssrc):
            cmd = cmd + " " + contourssrc + " -composite"
        cmd = cmd + " " + featuressrc + " -composite"
        cmd = cmd + " -quality " + str(quality) + " " + desttile
        call(cmd, shell=True)
        
        print 'done.'
    else:
        print 'source tiles not found. skipping.', colorreliefsrc, featuressrc


# public methods

def prepare_data_single(envLL, minz, maxz):
    ntiles = NTILES[maxz]
    envLL = pad_envelope(envLL, minz, ntiles)
    tiles = get_ned13_tiles(envLL)
    for tile in tiles:
        print tile
        prep_ned13_data_file(tile[0], tile[1])
    print '  Converting meters to feet...'
    cmd = 'echo "UPDATE %s SET height_ft = CAST(height * 3.28085 AS INT) WHERE height_ft IS NULL;" | psql -q "%s"' % (CONTOURS_TABLE, DATABASE)
    call(cmd, shell=True)

def prepare_data(envLL, minz, maxz):
    ntiles = NTILES[maxz]
    envLL = pad_envelope(envLL, minz, ntiles)
    tiles = get_ned13_tiles(envLL)
    queue = Queue(32)
    renderers = {}
    for i in range(NUM_THREADS):
        renderer = RenderThread(queue, maxz, i)
        render_thread = threading.Thread(target=renderer.render_loop)
        render_thread.start()
        renderers[i] = render_thread
    for tile in tiles:
        queue.put(('prepare_data', minz, tile[0], tile[1]))
    for i in range(NUM_THREADS):
        queue.put(None)
    queue.join()
    for i in range(NUM_THREADS):
        renderers[i].join()
    print 'Converting meters to feet...'
    cmd = 'echo "UPDATE %s SET height_ft = CAST(height * 3.28085 AS INT) WHERE height_ft IS NULL;" | psql -q "%s"' % (CONTOURS_TABLE, DATABASE)
    call(cmd, shell=True)
    print 'Done.'

def render_tiles(envLL, minz, maxz):
    print 'Using mapnik version:', mapnik_version()
    # Create mapnik rendering threads.
    queue = Queue(32)
    renderers = {}
    for i in range(NUM_THREADS):
        renderer = RenderThread(queue, maxz, i)
        render_thread = threading.Thread(target=renderer.render_loop)
        render_thread.start()
        renderers[i] = render_thread
    # Queue up jobs. High-to-low zoom, so we can merge topo-subtiles rather
    # than render from scratch at lower zoom levels.
    for z in range(maxz, minz-1, -1):
        ntiles = NTILES[z]
        (fromx, fromy) = get_tile_from_ll(envLL.maxy, envLL.minx, z)
        (tox, toy) = get_tile_from_ll(envLL.miny, envLL.maxx, z)
        for x in range(fromx, tox+1, ntiles):
            for y in range(fromy, toy+1, ntiles):
                queue.put(('render', z, x, y))
        # Join threads after each completed level, because
        # the color-relief layer of a lower zoom level depends
        # on that of the next higher level being finished.
        queue.join()
    # Signal render threads to exit and join threads
    for i in range(NUM_THREADS):
        queue.put(None)
    queue.join()
    for i in range(NUM_THREADS):
        renderers[i].join()

def render_tiles_single(envLL, minz, maxz):
    print 'Using mapnik version:', mapnik_version()
    renderer = RenderThread(None, maxz, 1)
    for z in range(maxz, minz-1, -1):
        ntiles = NTILES[z]
        (fromx, fromy) = get_tile_from_ll(envLL.maxy, envLL.minx, z)
        (tox, toy) = get_tile_from_ll(envLL.miny, envLL.maxx, z)
        for x in range(fromx, tox+1, ntiles):
            for y in range(fromy, toy+1, ntiles):
                renderer.render('render', z, x, y)

def create_jpeg_tiles(envLL, minz, maxz, quality):
    for z in range(minz, maxz+1):
        (fromx, fromy) = get_tile_from_ll(envLL.maxy, envLL.minx, z)
        (tox, toy) = get_tile_from_ll(envLL.miny, envLL.maxx, z)
        for x in range(fromx, tox+1):
            for y in range(fromy, toy+1):
                create_jpeg_tile(z, x, y, quality)

