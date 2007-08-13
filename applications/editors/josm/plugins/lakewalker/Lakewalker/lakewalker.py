#!/usr/bin/python

# Lakewalker II.
# 2007-07-09: Initial public release (v0.2) by Dshpak
# 2007-07-10: v0.3: Added support for OSM input files. Also reduced start_radius_big, and fixed a division-by-zero error in the degenerate case of point_line_distance().
# 2007-08-04: Added experimental non-recursive Douglas-Peucker algorithm (--dp-nr). Added --startdir option.
# 2007-08-06: v0.4: Bounding box support (--left, --right, --top, and --bottom), as well as new --josm mode for JOSM integration. This isn't perfect yet, but it's a good start.

"""Lakewalker II - A tool to automatically download and trace Landsat imagery, to generate OpenStreetMap data.

Requires the Python Imaging Library, available from http://www.pythonware.com/products/pil/"""

version="Lakewalker II v0.4"

# TODO:
# - Accept threshold tags in OSM input files.
# - Command line options:
#   - direction (deosil/widdershins)
#   - layer to use (monochrome only?)
#   - additional tags for the way
#   - Landsat download retries (count and delay)
#   - radii for loop detection
#   - fname for z12 tile output (list or script)
#   - path to tilesGen for z12 script output
#   - debug mode (extra tags on nodes) (make this non-uploadable?)
# - The "got stuck" message should output coords
# - Better "got stuck" detection (detect mini loops)
# - Offset nodes outwards by a half-pixel or so, to prevent duplicate segments
# - Automatic threshold detection
# - (Correct/tested) non-recursive DP simplification
#
#
# For JOSM integration:
# - Add a --tmpdir option that controls the location of the Landsat
#   tiles, the results text file, and the lake.osm file (unless it's
#   got an absolute path
# - Add a --nocache option that keeps WMS tiles in memory but not on disk.

import math
import os
import urllib
from PIL import Image
import OSMReader
import time
import optparse

options = None

start_radius_big = 0.001
start_radius_small = 0.0002

dirs = ((1, 0), (1, 1), (0, 1), (-1, 1), (-1, 0), (-1, -1), (0, -1), (1, -1))
dirnames = ["east", "northeast", "north", "northwest", "west", "southwest", "south", "southeast"]
dirabbrevs = ["e", "ne", "n", "nw", "w", "sw", "s", "se"]

class FatalError(Exception):
    pass

def message(s):
    if options.josm_mode:
        print "m %s" % s
    else:
        print s
        
def error(s):
    if options.josm_mode:
        print "e %s" % s
    else:
        print s
        
class BBox:
    def __init__(self, top = 90, left = -180, bottom = -90, right = 180):
        self.left = left
        self.right = right
        self.top = top
        self.bottom = bottom
    def contains(self, loc):
        (lat, lon) = loc
        if lat > self.top or lat < self.bottom:
            return False
        if (self.right - self.left) % 360 == 0:
            return True
        return (lon - self.left) % 360 <= (self.right - self.left) % 360

def download_landsat(c1, c2, width, height, fname):
    layer = "global_mosaic_base"
    style = "IR1"

    (min_lat, min_lon) = c1
    (max_lat, max_lon) = c2
    
    message("Downloading Landsat tile for (%.6f,%.6f)-(%.6f,%.6f)." % (min_lat, min_lon, max_lat, max_lon))
    url = "http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&layers=%s&styles=%s&srs=EPSG:4326&format=image/png&bbox=%0.6f,%0.6f,%0.6f,%0.6f&width=%d&height=%d" % (layer, style, min_lon, min_lat, max_lon, max_lat, width, height)
    #print url
    try:
        urllib.urlretrieve(url, fname)
    except IOError, e:
        raise FatalError("Error downloading tile: %s" % e.strerror)
    if not os.path.exists(fname):
        raise FatalError("Error: Could not retreive url %s" % url)

def xy_to_geo(xy):
    (x, y) = xy
    (lat, lon) = (y / float(options.resolution), x / float(options.resolution))
    return (lat, lon)

def geo_to_xy(geo):
    (lat, lon) = geo
    coord = lambda L: math.floor(L * options.resolution + 0.5)
    (x, y) = (coord(lon), coord(lat))
    return (x, y)

class WMSManager:
    def __init__(self):
        self.images = {}

    def get_tile(self, xy):
        fail_count = 0
        im = None
        while im is None and fail_count < 4:
            (x, y) = xy
            bottom_left_xy = (int(math.floor(x / options.tilesize)) * options.tilesize, int(math.floor(y / options.tilesize)) * options.tilesize)
            top_right_xy = (bottom_left_xy[0] + options.tilesize, bottom_left_xy[1] + options.tilesize)
            fname = "landsat_%d_%d_xy_%d_%d.png" % (options.resolution, options.tilesize, bottom_left_xy[0], bottom_left_xy[1])
            im = self.images.get(fname, None)
            if im is None:
                if not os.path.exists(fname):
                    bottom_left = xy_to_geo(bottom_left_xy)
                    top_right = xy_to_geo(top_right_xy)
                    download_landsat(bottom_left, top_right, options.tilesize, options.tilesize, fname)
                if not os.path.exists(fname):
                    raise FatalError("Error: Could not get image file %s" % fname)
                try:
                    im = Image.open(fname)
                    self.images[fname] = im
                    message("Using imagery in %s for %s." % (fname, xy_to_geo(xy)))
                except IOError:
                    error("Download was corrupt...Deleting %s..." % fname)
                    os.unlink(fname)
                    im = None

                if im is None:
                    message("Sleeping and retrying download...")
                    time.sleep(4)
                    fail_count = fail_count + 1

        if im is None:
            #if os.path.exists(fname):
            #    print open(fname).readlines()
            raise FatalError("Couldn't get image file %s." % fname)

        #return (im, top_left_xy)
        return (im, bottom_left_xy)
    
    def get_pixel(self, xy):
        (x, y) = xy
        (tile, (tx, ty)) = self.get_tile(xy)
        tile_xy = (x - tx, (options.tilesize - 1) - (y - ty))
        #print "%s maps to %s" % (xy, tile_xy)
        return tile.getpixel(tile_xy)

def trace_lake(loc, threshold, start_dir, bbox):
    wms = WMSManager()
    xy = geo_to_xy(loc)
    nodelist = []

    message("Starting coordinate: %.4f, %.4f" % loc)
    message("Starting position: %d, %d" % xy)

    if not bbox.contains(loc):
        raise FatalError("Error: Starting location is outside bounding box!")

    while True:
        loc = xy_to_geo(xy)
        if not bbox.contains(loc):
            break
        
        v = wms.get_pixel(xy)
        if v > threshold:
            break

        xy = (xy[0] + dirs[start_dir][0], xy[1] + dirs[start_dir][1])

    start_xy = xy
    start_loc = xy_to_geo(xy)
    message("Found shore at lat %.4f lon %.4f" % start_loc)

    #dirs = ((1, 0), (1, -1), (0, -1), (-1, -1), (-1, 0), (-1, 1), (0, 1), (1, 1))
    last_dir = start_dir

    detect_loop = False
    
    for i in xrange(options.maxnodes):
        if i % 250 == 0:
            if i > 0:
                message("%s nodes so far..." % i)

        for d in xrange(1, len(dirs)):
            new_dir = dirs[(last_dir + d + 4) % 8]
            test_xy = (xy[0] + new_dir[0], xy[1] + new_dir[1])
            test_loc = xy_to_geo(test_xy)
            if not bbox.contains(test_loc):
                break
            
            v = wms.get_pixel(test_xy)
            #print "%s: %s: %s" % (new_dir, test_xy, v)
            if v > threshold:
                break

        if d == 8:
            error("Got stuck.")
            break

        #print "Moving to %s, direction %s (was %s)" % (test_xy, new_dir, dirs[last_dir])
        last_dir = (last_dir + d + 4) % 8
        xy = test_xy

        if xy == start_xy:
            break

        loc = xy_to_geo(xy)
        nodelist.append(loc)
        
        start_proximity = (loc[0] - start_loc[0]) ** 2 + (loc[1] - start_loc[1]) ** 2
        if detect_loop:
            if start_proximity < start_radius_small ** 2:
                break
        else:
            if start_proximity > start_radius_big ** 2:
                detect_loop = True
    return nodelist

def vertex_reduce(nodes, proximity):
    test_v = nodes[0]
    reduced_nodes = [test_v]
    prox_sq = proximity ** 2
    for v in nodes:
        if (v[0] - test_v[0]) ** 2 + (v[1] - test_v[1]) ** 2 > prox_sq:
            reduced_nodes.append(v)
            test_v = v
    return reduced_nodes

def point_line_distance(p0, p1, p2):
    ((x0, y0), (x1, y1), (x2, y2)) = (p0, p1, p2)

    if x2 == x1 and y2 == y1:
        # Degenerate cast: the "line" is actually a point.
        return math.sqrt((x1-x0)**2 + (y1-y0)**2)
    else:
        # I don't understand this at all. Thank you, Mathworld.
        # http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
        return abs((x2-x1)*(y1-y0) - (x1-x0)*(y2-y1)) / math.sqrt((x2-x1)**2 + (y2-y1)**2)

def douglas_peucker(nodes, epsilon):
    #print "Running DP on %d nodes" % len(nodes)
    farthest_node = None
    farthest_dist = 0
    first = nodes[0]
    last = nodes[-1]

    for i in xrange(1, len(nodes) - 1):
        d = point_line_distance(nodes[i], first, last)
        if d > farthest_dist:
            farthest_dist = d
            farthest_node = i

    if farthest_dist > epsilon:
        seg_a = douglas_peucker(nodes[0:farthest_node+1], epsilon)
        seg_b = douglas_peucker(nodes[farthest_node:-1], epsilon)
        #print "Minimized %d nodes to %d + %d nodes" % (len(nodes), len(seg_a), len(seg_b))
        nodes = seg_a[:-1] + seg_b
    else:
        return [nodes[0], nodes[-1]]

    return nodes

def dp_findpoint(nodes, start, end):
    farthest_node = None
    farthest_dist = 0
    #print "dp_findpoint(nodes, %s, %s)" % (start, end)
    first = nodes[start]
    last = nodes[end]

    for i in xrange(start + 1, end):
        d = point_line_distance(nodes[i], first, last)
        if d > farthest_dist:
            farthest_dist = d
            farthest_node = i

    return (farthest_node, farthest_dist)

def douglas_peucker_nonrecursive(nodes, epsilon):
    #print "Running DP on %d nodes" % len(nodes)
    command_stack = [(0, len(nodes) - 1)]
    result_stack = []

    while len(command_stack) > 0:
        cmd = command_stack.pop()
        if type(cmd) == tuple:
            (start, end) = cmd
            (node, dist) = dp_findpoint(nodes, start, end)
            if dist > epsilon:
                command_stack.append("+")
                command_stack.append((start, node))
                command_stack.append((node, end))
            else:
                result_stack.append((start, end))
        elif cmd == "+":
            first = result_stack.pop()
            second = result_stack.pop()
            if first[-1] == second[0]:
                result_stack.append(first + second[1:])
                #print "Added %s and %s; result is %s" % (first, second, result_stack[-1])
            else:
                error("ERROR: Cannot connect nodestrings!")
                #print first
                #print second
                return
        else:
            error("ERROR: Can't understand command \"%s\"" % (cmd,))
            return

    if len(result_stack) == 1:
        return [nodes[x] for x in result_stack[0]]
    else:
        error("ERROR: Command stack is empty but result stack has %d nodes!" % len(result_stack))
        return

    farthest_node = None
    farthest_dist = 0
    first = nodes[0]
    last = nodes[-1]

    for i in xrange(1, len(nodes) - 1):
        d = point_line_distance(nodes[i], first, last)
        if d > farthest_dist:
            farthest_dist = d
            farthest_node = i

    if farthest_dist > epsilon:
        seg_a = douglas_peucker(nodes[0:farthest_node+1], epsilon)
        seg_b = douglas_peucker(nodes[farthest_node:-1], epsilon)
        #print "Minimized %d nodes to %d + %d nodes" % (len(nodes), len(seg_a), len(seg_b))
        nodes = seg_a[:-1] + seg_b
    else:
        return [nodes[0], nodes[-1]]

    return nodes

def output_to_josm(lakelist):
    # Description of JOSM output format:
    # m text - Status message text, to be displayed in a display window
    # e text - Error message text
    # s nnn - Start full node list, nnn tracings following
    # t nnn - Start tracing node list, nnn nodes following (i.e. there will be one of these for each lake where multiple start points specified)
    # n lat lon - A node
    # x - End of data
    print "s %s" % len(lakelist)
    for nodelist in lakelist:
        print "t %s" % len(nodelist)
        for node in nodelist:
            print "n %.7f %.7f" % (node[0], node[1])
    print "x"

def write_osm(f, lakelist, waysize):
    f.write('<osm version="0.4">')
    cur_id = -1
    way_count = 0
    for nodelist in lakelist:
        first_node_id = cur_id
        cur_way = []
        ways = [cur_way]
        for loc in nodelist:
            #f.write('  <node id="%d" lat="%.6f" lon="%.6f"><tag k="order" v="%d"/><tag k="x" v="%d"/><tag k="y" v="%d"/></node>\n' % (cur_id, loc[0], loc[1], -cur_id, geo_to_xy(loc)[0], geo_to_xy(loc)[1]))
            f.write('<node id="%d" lat="%.6f" lon="%.6f"/>' % (cur_id, loc[0], loc[1]))
            last_node_id = cur_id
            cur_id = cur_id - 1

        # print "Nodes: %d, %d" % (first_node_id, last_node_id)

        first_segment_id = cur_id
        for seg in xrange(first_node_id, last_node_id, -1):
            f.write('<segment id="%d" from="%d" to="%d"/>' % (cur_id, seg, seg-1))
            cur_id = cur_id - 1
        f.write('<segment id="%d" from="%d" to="%d"/>' % (cur_id, last_node_id, first_node_id))
        last_segment_id = cur_id
        cur_id = cur_id - 1

        # print "Segments: %d, %d" % (first_segment_id, last_segment_id)
        
        for seg in xrange(first_segment_id, last_segment_id - 1, -1):
            if len(cur_way) >= waysize:
                cur_way = []
                ways.append(cur_way)
            cur_way.append(seg)
        for way in ways:
            f.write('<way id="%d">' % cur_id)
            cur_id = cur_id - 1
            for seg in way:
                f.write('<seg id="%d"/>' % seg)
            f.write('<tag k="natural" v="%s"/>' % options.natural_type)
            f.write('<tag k="source" v="Dshpak_landsat_lakes"/>')
            f.write('</way>')
        way_count = way_count + len(ways)
    
    f.write('</osm>')
    message("Generated %d %s." % (way_count, ["way", "ways"][way_count > 0]))

def get_locs(infile):
    nodes = []
    segments = []
    ways = []
    reader = OSMReader.OSMReader()
    reader.nodeHandler = lambda x: nodes.append(x)
    reader.segmentHandler = lambda x: segments.append(x)
    reader.wayHandler = lambda x: ways.append(x)
    reader.run(file(infile))
    if len(segments) > 0 or len(ways) > 0:
        raise FatalError("Error: Input file must only contain nodes -- no segments or ways.")
    if len(nodes) == 0:
        raise FatalError("Error: No nodes found in input file.")
    return [((node.lat, node.lon), options.threshold) for node in nodes]

def main():
    parser = optparse.OptionParser(version=version)

    parser.add_option("--lat", type="float", metavar="LATITUDE", help="Starting latitude. Required, unless --infile is used..")
    parser.add_option("--lon", type="float", metavar="LONGITUDE", help="Starting longitude. Required, unless --infile is used.")
    parser.add_option("--startdir", type="string", default="east", metavar="DIR", help="Direction to travel from start position when seeking land. Defaults to \"east\".")
    parser.add_option("--infile", "-i", type="string", metavar="FILE", help="OSM file containing nodes representing starting points.")
    parser.add_option("--out", "-o", default="lake.osm", dest="outfile", metavar="FILE", help="Output filename. Defaults to lake.osm.")
    parser.add_option("--threshold", "-t", type="int", default="35", metavar="VALUE", help="Maximum gray value to accept as water (based on Landsat IR-1 data). Can be in the range 0-255. Defaults to 35.")
    parser.add_option("--maxnodes", type="int", default="50000", metavar="N", help="Maximum number of nodes to generate before bailing out. Defaults to 50000.")
    parser.add_option("--waylength", type="int", default=250, metavar="MAXLEN", help="Maximum nuber of nodes allowed in one way. Defaults to 250.")
    parser.add_option("--landsat-res", type="int", default=4000, dest="resolution", metavar="RES", help="Resolution of Landsat tiles, measured in pixels per degree. Defaults to 4000.")
    parser.add_option("--tilesize", type="int", default=2000, help="Size of one landsat tile, measured in pixels. Defaults to 2000.")
    parser.add_option("--no-dp", action="store_false", dest="use_dp", default=True, help="Disable Douglas-Peucker line simplification (not recommended)")
    parser.add_option("--dp-epsilon", type="float", metavar="EPSILON", default=0.0003, help="Accuracy of Douglas-Peucker line simplification, measured in degrees. Lower values give more nodes, and more accurate lines. Defaults to 0.0003.")
    parser.add_option("--dp-nr", action="store_true", dest="dp_nr", default=False, help="Use experimental non-recursive DP implementation")
    parser.add_option("--vr", action="store_true", dest="use_vr", default=False, help="Use vertex reduction before applying line simplification (off by default).")
    parser.add_option("--vr-epsilon", type="float", default=0.0005, metavar="RADIUS", help="Radius used for vertex reduction (measured in degrees). Defaults to 0.0005.")
    parser.add_option("--water", action="store_const", const="water", dest="natural_type", default="coastline", help="Tag ways as natural=water instead of natural=coastline")
    parser.add_option("--left", type="float", metavar="LONGITUDE", default=-180, help="Left (west) longitude for bounding box")
    parser.add_option("--right", type="float", metavar="LONGITUDE", default=180, help="Right (east) longitude for bounding box")
    parser.add_option("--top", type="float", metavar="LATITUDE", default=90, help="Top (north) latitude for bounding box")
    parser.add_option("--bottom", type="float", metavar="LATITUDE", default=-90, help="Bottom (south) latitude for bounding box")
    parser.add_option("--josm", action="store_true", dest="josm_mode", default=False, help="Operate in JOSM plugin mode")

    global options # Ugly, I know...
    (options, args) = parser.parse_args()

    if len(args) > 0:
        parser.print_help()
        return

    (start_lat, start_lon, infile) = (options.lat, options.lon, options.infile)

    if (start_lat is None or start_lon is None) and infile is None:
        if not options.josm_mode:
            parser.print_help()
            print
        error("Error: you must specify a starting latitude and longitude.")
        return

    if infile is not None:
        if start_lat is not None or start_lon is not None:
            error("Error: you cannot use both --infile and --lat or --lon.")
            return
        try:
            locs = get_locs(infile)
            #print locs
        except FatalError, e:
            error("%s" % e)
            return
    else:
        locs = [((start_lat, start_lon), options.threshold)]

    dirname = options.startdir.lower()
    if dirname in dirnames:
        startdir = dirnames.index(dirname)
    elif dirname in dirabbrevs:
        startdir = dirabbrevs.index(dirname)
    else:
        error("Error: Can't understand starting direction \"%s\". Vaild options are %s." % (dirname, ", ".join(dirnames + dirabbrevs)))
        return

    message("Starting direction is %s." % dirnames[startdir])

    bbox = BBox(options.top, options.left, options.bottom, options.right)

    try:
        lakes = []
        for (loc, threshold) in locs:
            nodes = trace_lake(loc, threshold, startdir, bbox)

            message("%d nodes generated." % len(nodes))

            if len(nodes) > 0:
                if options.use_vr:
                    nodes = vertex_reduce(nodes, options.vr_epsilon)
                    message("After vertex reduction, %d nodes remain." % len(nodes))

                if options.use_dp:
                    try:
                        if options.dp_nr:
                            nodes = douglas_peucker_nonrecursive(nodes, options.dp_epsilon)
                            #print "Final result: %s" % (nodes,)
                        else:
                            nodes = douglas_peucker(nodes, options.dp_epsilon)
                        message("After Douglas-Peucker approximation, %d nodes remain." % len(nodes))
                    except FatalError, e:
                        raise e
                    except:
                        raise FatalError("Line simplification failed -- there are probably too many nodes.")

                lakes.append(nodes)

        if options.josm_mode:
            output_to_josm(lakes)
        else:
            message("Writing to %s" % options.outfile)
            f = open(options.outfile, "w")
            write_osm(f, lakes, options.waylength)
            f.close()
        
        #tiles = tilelist(nodes)
        
        #for tile in tiles:
        #    print "./tilesGen.pl xy %d %d; ./upload.pl" % tile
        
        #print tiles
    except FatalError, e:
        error("%s" % e)
        error("Bailing out...")

if __name__ == "__main__":
    main()

