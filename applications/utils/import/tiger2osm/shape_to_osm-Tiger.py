#!/usr/bin/python
# Tiger road data to OSM conversion script
# based on the Massachusetts GIS script by christopher schmidt

VERSION="0.2"
# Version 0.2 changes to generalize the script some for use with other data by Dalep

# Tag Source  = iSource + _import_v + version + _ + date and time
iSource="TIGER"
# Tag Attribution = iAttrib
iAttrib="TIGER"

# Ways that include these mtfccs should not be uploaded
# H1100 Connector
# H3010 Stream/River
# H3013 Braided Stream
# H3020 Canal, Ditch or Aqueduct
# L4130 Point-to-Point Line
# L4140 Property/Parcel Line (Including PLSS)
# P0001 Nonvisible Linear Legal/Statistical Boundary
# P0002 Perennial Shoreline
# P0003 Intermittent Shoreline
# P0004 Other non-visible bounding Edge (e.g., Census water boundary, boundary of an areal feature)


ignoremtfcc = [ "H1100", "H3010", "H3013", "H3020", "L4130", "L4140", "P0001", "P0002", "P0003", "P0004" ]

#Files will be split when longer than this number of nodes
maxNodes = 300000

# Set the maximum length of a way (in nodes) before it is split into
# shorter ways
Max_Waylength = 500

try:
    from osgeo import ogr
    from osgeo import osr
except:
    import ogr
    import osr

# ====================================
# Edit parse_shp_for_osm section to fit your data!
# change poFeature.GetField("    ") to contain only the shape column names for the data you want
# and  tags["   "] to match the osm tag names you wish to use for that data.
# some tags will require changing a number to a meaningful value like the Highway tag.  See the metadata for the meaning of these tags.
# For any measurements be sure to check the unit value of the original data, and convert if needed to the expected unit for osm.
# ====================================
def parse_shp_for_osm( filename ):
    #ogr.RegisterAll()

    dr = ogr.GetDriverByName("ESRI Shapefile")
    poDS = dr.Open( filename )

    if poDS == None:
        raise "Open failed."

    poLayer = poDS.GetLayer( 0 )

    poLayer.ResetReading()

    ret = []

    poFeature = poLayer.GetNextFeature()
    while poFeature:
        tags = {}
        
        # WAY ID
        tags[iSource + ":way_id"] = int( poFeature.GetField("TLID") )
        
        # FEATURE NAME
        if poFeature.GetField("FULLNAME"):
            #capitalizes the first letter of each word
            tags["name"] = poFeature.GetField( "FULLNAME" )

	# FEATURE IDENTIFICATION
        mtfcc = poFeature.GetField("MTFCC");
        if mtfcc != None:

	    if mtfcc == "L4010":	#Pipeline
		tags["man_made"] = "pipeline"
	    if mtfcc == "L4020":	#Powerline
		tags["power"] = "line"
	    if mtfcc == "L4031":	#Aerial Tramway/Ski Lift
		tags["aerialway"] = "cable_car"
	    if mtfcc == "L4110":	#Fence Line
		tags["barrier"] = "fence"
	    if mtfcc == "L4125":	#Cliff/Escarpment
		tags["natural"] = "cliff"
	    if mtfcc == "L4165":	#Ferry Crossing
		tags["route"] = "ferry"
	    if mtfcc == "R1011":	#Railroad Feature (Main, Spur, or Yard)
		tags["railway"] = "rail"
	    if mtfcc == "R1051":	#Carline, Streetcar Track, Monorail, Other Mass Transit Rail)
		tags["railway"] = "light_rail"
	    if mtfcc == "R1052":	#Cog Rail Line, Incline Rail Line, Tram
		tags["railway"] = "incline"
	    if mtfcc == "S1100":
		tags["highway"] = "primary"
	    if mtfcc == "S1200":
		tags["highway"] = "secondary"
	    if mtfcc == "S1400":
		tags["highway"] = "residential"
	    if mtfcc == "S1500":
		tags["highway"] = "track"
	    if mtfcc == "S1630":	#Ramp
		tags["highway"] = "motorway_link"
	    if mtfcc == "S1640":	#Service Drive usually along a limited access highway
		tags["highway"] = "service"
	    if mtfcc == "S1710":
		tags["highway"] = "path"
	    if mtfcc == "S1720":
		tags["highway"] = "steps"
	    if mtfcc == "S1730":	#Alley
		tags["highway"] = "service"
		tags["service"] = "alley"
	    if mtfcc == "S1740":	#Private Road for service vehicles (logging, oil, fields, ranches, etc.)
		tags["highway"] = "service"
		tags["access"] = "private"
	    if mtfcc == "S1750":	#Private Driveway
		tags["highway"] = "service"
		tags["access"] = "private"
		tags["service"] = "driveway"
	    if mtfcc == "S1780":	#Parking Lot Road
		tags["highway"] = "service"
		tags["service"] = "parking_aisle"
	    if mtfcc == "S1820":	#Bike Path or Trail
		tags["highway"] = "cycleway"
	    if mtfcc == "S1830":	#Bridle Path
		tags["highway"] = "bridleway"
	    tags["TIGER:MTFCC"] = mtfcc

        divroad = poFeature.GetField("DIVROAD");
        if divroad != None:
	    if divroad == "Y" and tags["highway"] == "residential":
                tags["highway"] = "tertiary"

        if mtfcc not in ignoremtfcc:
            # COPY DOWN THE GEOMETRY
            geom = []
            
            rawgeom = poFeature.GetGeometryRef()
            for i in range( rawgeom.GetPointCount() ):
                geom.append( (rawgeom.GetX(i), rawgeom.GetY(i)) )
    
            ret.append( (geom, tags) )
        poFeature = poLayer.GetNextFeature()
        
    return ret


# ====================================
# to do read .prj file for this data
# Change the Projcs_wkt to match your datas prj file.
# ====================================
projcs_wkt = \
"""GEOGCS["GCS_North_American_1983",
	DATUM["D_North_American_1983",
	SPHEROID["GRS_1980",6378137,298.257222101]],
	PRIMEM["Greenwich",0],
	UNIT["Degree",0.017453292519943295]]"""

from_proj = osr.SpatialReference()
from_proj.ImportFromWkt( projcs_wkt )

# output to WGS84
to_proj = osr.SpatialReference()
to_proj.SetWellKnownGeogCS( "EPSG:4326" )

tr = osr.CoordinateTransformation( from_proj, to_proj )

def unproject( point ):
    pt = tr.TransformPoint( point[0], point[1] )
    return (pt[1], pt[0])

def round_point( point, accuracy=8 ):
    return tuple( [ round(x,accuracy) for x in point ] )

def compile_nodelist( parsed_gisdata, first_id=1 ):
    nodelist = {}
    
    i = first_id
    for geom, tags in parsed_gisdata:
        if len( geom )==0:
            continue
        
        for point in geom:
            r_point = round_point( point )
            if r_point not in nodelist:
                nodelist[ r_point ] = (i, unproject( point ))
                i += 1
            
    return (i, nodelist)

def adjacent( left, right ):
    left_left = round_point(left[0])
    left_right = round_point(left[-1])
    right_left = round_point(right[0])
    right_right = round_point(right[-1])
    
    return ( left_left == right_left or
             left_left == right_right or
             left_right == right_left or
             left_right == right_right )
             
def glom( left, right ):
    
    left = list( left )
    right = list( right )
    
    left_left = round_point(left[0])
    left_right = round_point(left[-1])
    right_left = round_point(right[0])
    right_right = round_point(right[-1])
    
    if left_left == right_left:
        left.reverse()
        return left[0:-1] + right
        
    if left_left == right_right:
        return right[0:-1] + left
        
    if left_right == right_left:
        return left[0:-1] + right
        
    if left_right == right_right:
        right.reverse()
        return left[0:-1] + right
        
    raise 'segments are not adjacent'

def glom_once( segments ):
    if len(segments)==0:
        return segments
    
    unsorted = list( segments )
    x = unsorted.pop(0)
    
    while len( unsorted ) > 0:
        n = len( unsorted )
        
        for i in range(0, n):
            y = unsorted[i]
            if adjacent( x, y ):
                y = unsorted.pop(i)
                x = glom( x, y )
                break
                
        # Sorted and unsorted lists have no adjacent segments
        if len( unsorted ) == n:
            break
            
    return x, unsorted
    
def glom_all( segments ):
    unsorted = segments
    chunks = []
    
    while unsorted != []:
        chunk, unsorted = glom_once( unsorted )
        chunks.append( chunk )
        
    return chunks
        
                

def compile_waylist( parsed_gisdata, blank_way_id ):
    waylist = {}
    
    #Group by iSource:way_id
    for geom, tags in parsed_gisdata:
        way_key = tags.copy()
        way_key = ( way_key[iSource + ':way_id'], tuple( [(k,v) for k,v in way_key.iteritems()] ) )
        
        if way_key not in waylist:
            waylist[way_key] = []
            
        waylist[way_key].append( geom )
    
    ret = {}
    for (way_id, way_key), segments in waylist.iteritems():
        
        if way_id != blank_way_id:
            ret[way_key] = glom_all( segments )
        else:
            ret[way_key] = segments
        
    return ret
            

import time
from xml.sax.saxutils import escape
def shape_to_osm( shp_filename, base_filename, blank_way_id ):
    
    import_guid = time.strftime( '%Y%m%d%H%M%S' )

    print "parsing shpfile"
    parsed_features = parse_shp_for_osm( shp_filename )
    
    print "compiling nodelist"
    i, nodelist = compile_nodelist( parsed_features )
    
    print "compiling waylist"
    waylist = compile_waylist( parsed_features, blank_way_id )

    filenumber = 1
    objectcount = 0
    seen = {}
    
    print "constructing osm xml file"
    ret = []
    ret.append( "<?xml version='1.0' encoding='UTF-8'?>" )
    ret.append( "<osm version='0.6' generator='shape_to_osm.py'>" )
    
    for waykey, segments in waylist.iteritems():
        for segment in segments:
	    #write the nodes
            for point in segment:
                id, (lat, lon) = nodelist[ round_point( point ) ]
                if id not in seen:
                    seen[id] = True
		    #write node
                    ret.append( "  <node id='-%d' action='create' visible='true' lat='%f' lon='%f' >" % (id, lat, lon) )
#                    ret.append( "    <tag k=\"source\" v=\"%s_import_v%s_%s\" />" % (iSource, VERSION, import_guid) )
#                    ret.append( "    <tag k=\"attribution\" v=\"%s\" />" % (iAttrib) )
                    ret.append( "  </node>" )
                    objectcount += 1
                else:
                    pass
                    #print "Skipping node %d" %id

	    #write the way
            ret.append( "  <way id='-%d' action='create' visible='true'>" % i )
            
            ids = [ nodelist[ round_point( point ) ][0] for point in segment ]

            count = 0
            for id in ids:
                count += 1
                ret.append( "    <nd ref='-%d' />" % id )
                if (count % Max_Waylength == 0) and (count != len(ids)):	#Split the way
                    for k, v in waykey:
                        ret.append( "    <tag k=\"%s\" v=\"%s\" />" % (k, escape(str(v))) )
                    ret.append( "    <tag k=\"source\" v=\"%s_import_v%s_%s\" />" % (iSource, VERSION, import_guid) )
                    ret.append( "    <tag k=\"attribution\" v=\"%s\" />" % (iAttrib) )
                
                    ret.append( "  </way>" )
                    objectcount += 1
		    i += 1
                    ret.append( "  <way id='-%d' action='create' visible='true'>" % i )
                    ret.append( "    <nd ref='-%d' />" % id )
                
            for k, v in waykey:
                ret.append( "    <tag k=\"%s\" v=\"%s\" />" % (k, escape(str(v))) )
            ret.append( "    <tag k=\"source\" v=\"%s_import_v%s_%s\" />" % (iSource, VERSION, import_guid) )
            ret.append( "    <tag k=\"attribution\" v=\"%s\" />" % (iAttrib) )
                
            ret.append( "  </way>" )
            objectcount += 1
            
            i += 1

            if objectcount > maxNodes:	#Write a file
                ret.append( "</osm>" )
                osm_filename = "%s%d.osm" % (base_filename, filenumber)
                print "writing %s" %osm_filename
                fp = open( osm_filename, "w" )
                fp.write( "\n".join( ret ) )
                fp.close()

                objectcount = 0
                filenumber += 1
                seen = {}
                ret = []
                ret.append( "<?xml version='1.0' encoding='UTF-8'?>" )
                ret.append( "<osm version='0.6' generator='shape_to_osm.py'>" )
        
    ret.append( "</osm>" )
    
    osm_filename = "%s%d.osm" % (base_filename, filenumber)
    print "writing %s" %osm_filename
    fp = open( osm_filename, "w" )
    fp.write( "\n".join( ret ) )
    fp.close()
    
if __name__ == '__main__':
    import sys, os.path
    if len(sys.argv) < 2:
        print "%s filename.shp [filename.osm]" % sys.argv[0]
        sys.exit()
    shape = sys.argv[1]
    if len(sys.argv) > 2:
        osm = sys.argv[2]
    else:
        osm = shape[0:-4] + ".osm" 
    id = "1.shp"
	# Left over from massGIS unknown usage, but works fine hardcoded to "1.shp" which was the valu on a test of the actual mass data,
	#id = os.path.basename(shape).split("_")[-1]
    shape_to_osm( shape, osm, id )
