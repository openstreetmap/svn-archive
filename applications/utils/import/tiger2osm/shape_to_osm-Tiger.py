#!/usr/bin/python
# Tiger road data to OSM conversion script
# based on the Massachusetts GIS script by christopher schmidt

VERSION="0.3"
# Version 0.3 is optimized for the tiger road conversion

# Tag Source  = iSource + _import_v + version + _ + date and time
iSource="tiger"
# Tag Attribution = iAttrib
iAttrib="tiger"

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

# Sets the distance that the address ways should be from the main way, in feet.
address_distance = 25

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

# Long name, short name, ISO-3166-1 alpha-2
# from http://www.census.gov/geo/www/ansi/statetables.html
fipscodes = {
    '01' : ('Alabama', 'AL', 'US'),
    '02' : ('Alaska', 'AK', 'US'),
    '04' : ('Arizona', 'AZ', 'US'),
    '05' : ('Arkansas', 'AR', 'US'),
    '06' : ('California', 'CA', 'US'),
    '08' : ('Colorado', 'CO', 'US'),
    '09' : ('Connecticut', 'CT', 'US'),
    '10' : ('Delaware', 'DE', 'US'),
    '11' : ('District of Columbia', 'DC', 'US'),
    '12' : ('Florida', 'FL', 'US'),
    '13' : ('Georgia', 'GA', 'US'),
    '15' : ('Hawaii', 'HI', 'US'),
    '16' : ('Idaho', 'ID', 'US'),
    '17' : ('Illinois', 'IL', 'US'),
    '18' : ('Indiana', 'IN', 'US'),
    '19' : ('Iowa', 'IA', 'US'),
    '20' : ('Kansas', 'KS', 'US'),
    '21' : ('Kentucky', 'KY', 'US'),
    '22' : ('Louisiana', 'LA', 'US'),
    '23' : ('Maine', 'ME', 'US'),
    '24' : ('Maryland', 'MD', 'US'),
    '25' : ('Massachusetts', 'MA', 'US'),
    '26' : ('Michigan', 'MI', 'US'),
    '27' : ('Minnesota', 'MN', 'US'),
    '28' : ('Mississippi', 'MS', 'US'),
    '29' : ('Missouri', 'MO', 'US'),
    '30' : ('Montana', 'MT', 'US'),
    '31' : ('Nebraska', 'NE', 'US'),
    '32' : ('Nevada', 'NV', 'US'),
    '33' : ('New Hampshire', 'NH', 'US'),
    '34' : ('New Jersey', 'NJ', 'US'),
    '35' : ('New Mexico', 'NM', 'US'),
    '36' : ('New York', 'NY', 'US'),
    '37' : ('North Carolina', 'NC', 'US'),
    '38' : ('North Dakota', 'ND', 'US'),
    '39' : ('Ohio', 'OH', 'US'),
    '40' : ('Oklahoma', 'OK', 'US'),
    '41' : ('Oregon', 'OR', 'US'),
    '42' : ('Pennsylvania', 'PA', 'US'),
    '44' : ('Rhode Island', 'RI', 'US'),
    '45' : ('South Carolina', 'SC', 'US'),
    '46' : ('South Dakota', 'SD', 'US'),
    '47' : ('Tennessee', 'TN', 'US'),
    '48' : ('Texas', 'TX', 'US'),
    '49' : ('Utah', 'UT', 'US'),
    '50' : ('Vermont', 'VT', 'US'),
    '51' : ('Virginia', 'VA', 'US'),
    '53' : ('Washington', 'WA', 'US'),
    '54' : ('West Virginia', 'WV', 'US'),
    '55' : ('Wisconsin', 'WI', 'US'),
    '56' : ('Wyoming', 'WY', 'US'),
    # Outlying areas w/census data
    '60' : ('American Samoa', 'AS', 'AS'),
    '66' : ('Guam', 'GU', 'GU'),
    '69' : ('Commonwealth of the Northern Mariana Islands', 'MP', 'MP'),
    '72' : ('Puerto Rico', 'PR', 'PR'),
    '78' : ('U.S. Virgin Islands', 'VI', 'VI'),
    }

import math
def addressways(waylist, nodelist, first_id):
    id = first_id
    awaylist = {}
    lat_feet = 364613  #The approximate number of feet in one degree of latitude
    ret = []
    ret.append( "<?xml version='1.0' encoding='UTF-8'?>" )
    ret.append( "<osm version='0.6' generator='shape_to_osm.py'>" )

    for waykey, segments in waylist.iteritems():
        waykey = dict(waykey)
        rsegments = []
        lsegments = []
        for segment in segments:
            lsegment = []
            rsegment = []
            lastpoint = None
            if "tiger:lfromadd" in waykey:
                lfromadd = waykey["tiger:lfromadd"]
            else:
                lfromadd = None
            if "tiger:ltoadd" in waykey:
                ltoadd = waykey["tiger:ltoadd"]
            else:
                ltoadd = None
            if "tiger:rfromadd" in waykey:
                rfromadd = waykey["tiger:rfromadd"]
            else: 
                rfromadd = None
            if "tiger:rtoadd" in waykey:
                rtoadd = waykey["tiger:rtoadd"]
            else:
		rtoadd = None
            if rfromadd != None and rtoadd != None:
                right = True
	    else:
		right = False
            if lfromadd != None and ltoadd != None:
                left = True
	    else:
		left = False
            if left or right:
		first = True
                for point in segment:
                    pointid, (lat, lon) = nodelist[ round_point( point ) ]
                    lrad = math.radians(lat)
                    lon_feet = 365527.822 * math.cos(lrad) - 306.75853 * math.cos(3 * lrad) + 0.3937 * math.cos(5 * lrad)
		    #The approximate number of feet in one degree of longitute
                
#Calculate the points of the offset ways
                    if lastpoint != None:
                        X = (lon - lastpoint[1]) * lon_feet
		        Y = (lat - lastpoint[0]) * lat_feet
                        if Y != 0:
		            theta = math.pi/2 - math.atan( X / Y)
		            Xp = math.sin(theta) * address_distance
		            Yp = math.cos(theta) * address_distance
                        else:
                            Xp = 0
                            Yp = float(address_distance)

			if Y > 0:
			    Xp = -Xp
			else:
			    Yp = -Yp
				
			if first:
			    first = False
			    if left:
                                lpoint = (lastpoint[0] + (Yp / lat_feet), lastpoint[1] + (Xp / lon_feet))
                                lsegment.append( (id, lpoint) )
			        id += 1
			    if right:
                                rpoint = (lastpoint[0] - (Yp / lat_feet), lastpoint[1] - (Xp / lon_feet))
                                rsegment.append( (id, rpoint) )
			        id += 1

			if left:
                            lpoint = (lat + (Yp / lat_feet), lon + (Xp / lon_feet))
                            lsegment.append( (id, lpoint) )
			    id += 1
                        if right: 
                            rpoint = (lat - (Yp / lat_feet), lon - (Xp / lon_feet))
                            rsegment.append( (id, rpoint) )
                            id += 1
                    lastpoint = (lat, lon)

#Write the nodes of the offset ways
		if right:
                    first = True
                    for i, point in rsegment:
                        if not first:
			    ret.append( "</node>" )
		        ret.append( "<node id='-%d' action='create' visible='true' lat='%f' lon='%f' >" % (i, point[0], point[1] ) )
	    	        if first:
                            ret.append( "<tag k=\"addr:housenumber\" v=\"%s\" />" % rfromadd )
                            first = False
                    ret.append( "<tag k=\"addr:housenumber\" v=\"%s\" />" % rtoadd )
		    ret.append( "</node>" )
		if left:
                    first = True
                    for i, point in lsegment:
                        if not first:
			    ret.append( "</node>" )
		        ret.append( "<node id='-%d' action='create' visible='true' lat='%f' lon='%f' >" % (i, point[0], point[1] ) )
	    	        if first:
                            ret.append( "<tag k=\"addr:housenumber\" v=\"%s\" />" % lfromadd )
                            first = False
                    ret.append( "<tag k=\"addr:housenumber\" v=\"%s\" />" % ltoadd )
		    ret.append( "</node>" )
		if right:
                    rsegments.append( rsegment )
		if left:
                    lsegments.append( lsegment )
	        if right:
		    ret.append( "<way id='-%d' action='create' visible='true'> " % id)
		    id += 1
                    for rsegment in rsegments:
                        for point in rsegment:
                            ret.append( "<nd ref='-%d' /> " % point[0])

		    tofromint = True	#Do the addresses convert to integers?
		    try: rfromint = int(rfromadd)
		    except:
			print("Non integer address: %s" % rfromadd)
			tofromint = False
		    try: rtoint = int(rtoadd)
		    except:
			print("Non integer address: %s" % rtoint)
			tofromint = False
		    if tofromint == True:
                        if ((int(rfromadd) % 2) == 0) and ((int(rtoadd) % 2) == 0):
                            ret.append( "<tag k=\"addr:interpolation\" v=\"even\" />" )
                        elif ((int(rfromadd) % 2) == 1) and ((int(rtoadd) % 2) == 1):
                            ret.append( "<tag k=\"addr:interpolation\" v=\"odd\" />" )
                    if "name" in waykey:
                        name = waykey["name"]
                    ret.append( "<tag k=\"addr:street\" v=\"%s\" />" % name )
                    ret.append( "</way>" )
		if left:
		    ret.append( "<way id='-%d' action='create' visible='true'> " % id)
		    id += 1
                    for lsegment in lsegments:
                        for point in lsegment:
                            ret.append( "<nd ref='-%d' /> " % point[0])
		    tofromint = True	#Do address convert to ints?
		    try: lfromint = int(lfromadd)
		    except:
			print("Non integer address: %s" % lfromadd)
			tofromint = False
		    try: ltoint = int(ltoadd)
		    except:
			print("Non integer address: %s" % ltoint)
			tofromint = False
		    if tofromint == True:
                        if (lfromint % 2) == 0 and (ltoint % 2) == 0:
                            ret.append( "<tag k=\"addr:interpolation\" v=\"even\" />" )
                        elif (lfromint % 2) == 1 and (ltoint % 2) == 1:
                            ret.append( "<tag k=\"addr:interpolation\" v=\"odd\" />" )
                    if "name" in waykey:
                        name = waykey["name"]
                    ret.append( "<tag k=\"addr:street\" v=\"%s\" />" % name )
                    ret.append( "</way>" )

    ret.append( "</osm>" )
    return ret

def fipsstate(fips):
    tags = {}

    if not fips:
        tags['is_in'] = 'USA'
        tags['is_in:country'] = 'USA'
        tags['is_in:country_code'] = 'US'
        return tags

    if fips not in fipscodes:
        raise KeyError, 'missing FIPS code', fips

    state, statecode, isocode = fipscodes[fips]

    tags["is_in"] =  'USA, '+state
    tags["is_in:state"] =  state
    tags["is_in:state_code"] = statecode
    tags["is_in:country_code"] = isocode

    if isocode == 'US':
        tags["is_in:iso_3166_2"] =  isocode+':'+statecode
        tags["is_in:country"] = "USA"

    else:
        # Reasonable to specify both here
        tags["is_in:country"] = 'USA;'+state

    return tags

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
	    tags["tiger:mtfcc"] = mtfcc

        divroad = poFeature.GetField("DIVROAD")
        if divroad != None:
	    if divroad == "Y" and tags["highway"] == "residential":
                tags["highway"] = "tertiary"
            tags["tiger:separated"] = divroad

        statefp = poFeature.GetField("STATEFP")
        if statefp != None:
           tags.update( fipsstate(statefp) )

        tlid = poFeature.GetField("TLID")
        if tlid != None:
            tags["tiger:tlid"] = tlid

        lfromadd = poFeature.GetField("LFROMADD")
        if lfromadd != None:
            tags["tiger:lfromadd"] = lfromadd

        rfromadd = poFeature.GetField("RFROMADD")
        if rfromadd != None:
            tags["tiger:rfromadd"] = rfromadd

        ltoadd = poFeature.GetField("LTOADD")
        if ltoadd != None:
            tags["tiger:ltoadd"] = ltoadd

        rtoadd = poFeature.GetField("RTOADD")
        if rtoadd != None:
            tags["tiger:rtoadd"] = rtoadd

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

    print "preparing address ways"
    ret = addressways(waylist, nodelist, i)
    osm_filename = "%s%d.osm" % (base_filename, filenumber)
    print "writing %s" %osm_filename
    fp = open( osm_filename, "w" )
    fp.write( "\n".join( ret ) )
    fp.close()
    filenumber += 1

    
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
