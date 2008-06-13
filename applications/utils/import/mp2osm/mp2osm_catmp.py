# derived from 'mp2osm_ukraine.py' 
# modified by simon@mungewell.org
# modified by Karl Newman (User:SiliconFiend) to preserve routing topology and parse RouteParam 
# license: GPL V2 or later

import xml.etree.ElementTree as ET

attribution = 'Calgary Area Trail Mapping Project'
file_mp = open('short.mp')

# flags and global variable
poi = False
polyline = False
polygon = False
roadid = ''
rNodeToOsmId = {} # map routing node ids to OSM node ids

# debug/stats counters
poi_counter = 0
polyline_counter = 0
polygon_counter = 0

osm = ET.Element('osm', version='0.5', generator='mp2osm_catmp' )
osm.text = '\n  '
osm.tail = '\n'
source = ET.Element('tag', k='source',v=attribution)
source.tail = '\n    '
nodeid = -1
# Define the mapping from Garmin type codes to OSM tags
# Note that single items in parentheses need a trailing comma
poitagmap = {# Warning: this particular 'gate' typecode usage is specific to Calgary Trails maps
     (0x1612, 0x1c00, 0x6400): {'highway': 'gate'}, 
     (0x2b00,): {'tourism': 'hotel'},
     (0x2b01,): {'tourism': 'motel'},
     (0x2b03,): {'tourism': 'caravan_site'},
     (0x2e02,): {'shop': 'supermarket'},
     (0x2f08,): {'amenity': 'bus_station'},
     (0x4400,): {'amenity': 'fuel'},
     (0x4700,): {'leisure': 'slipway'},
     (0x4800,): {'tourism': 'campsite'},
     (0x4900,): {'leisure': 'park'},
     (0x4a00,): {'tourism': 'picnic_site'},
     (0x4c00,): {'tourism': 'information'},
     (0x4d00,): {'amenity': 'parking'},
     (0x4e00,): {'amenity': 'toilets'},
     (0x5100,): {'amenity': 'telephone'},
     (0x5200,): {'tourism': 'viewpoint'},
     (0x5400,): {'sport': 'swimming'},
     (0x5904,): {'aeroway': 'helipad'},
     (0x5905,): {'aeroway': 'aerodrome'},
     (0x5904,): {'aeroway': 'helipad'},
     (0x5a00,): {'distance_marker': 'yes'}, # Not approved
     (0x6401,): {'bridge': 'yes'}, # Apply to points?
     (0x6401,): {'building': 'yes'},
     (0x6406,): {'highway': 'crossing'},
     (0x640c,): {'man_made': 'mineshaft'},
     (0x640d,): {'man_made': 'pumping_rig', 'type': 'oil'},
     (0x6411,): {'man_made': 'tower'},
     (0x6412,): {'highway': 'trailhead'}, # This is not even a proposed value
     (0x6413,): {'tunnel': 'yes'}, # Apply to points?
     (0x6500, 0x650d): {'natural': 'water'},
     (0x6508,): {'waterway': 'waterfall'},
     (0x6605,): {'natural': 'bench'},
     (0x6616,): {'natural': 'peak'}
    }
polylinetagmap = {
     (0x2,): {'highway': 'trunk'},
     (0x3,): {'highway': 'primary'},
     (0x4,): {'highway': 'secondary'},
     (0x5,): {'highway': 'tertiary'},
     (0x6,): {'highway': 'residential'},
     (0xa,): {'highway': 'track', 'surface': 'unpaved'},
     (0x16,): {'highway': 'footway'},
     (0x18,): {'waterway': 'stream'},
     (0x1f,): {'waterway': 'river'},
     (0x29,): {'power': 'line'}
    }
polygontagmap = {
     (0x5,): {'amenity': 'parking', 'area': 'yes'},
     (0xd,): {'landuse': 'reservation', 'area': 'yes'}, # reservation is not even a proposed value
     (0x3c, 0x40, 0x41): {'natural': 'water', 'area': 'yes'},
     (0x48, 0x49): {'waterway': 'riverbank', 'area': 'yes'},
     (0x4c,): {'waterway': 'intermittent', 'area': 'yes'},
     (0x51,): {'natural': 'marsh', 'area': 'yes'}
    }

for line in file_mp:
    # Marker for start of sections
    if line.startswith(('[POI]','[RGN10]','[RGN20]')):
        node = ET.Element('node', visible='true', id=str(nodeid))
        nodeid -= 1
        node.append(source)
        poi = True
        elementtagmap = poitagmap
        poi_counter += 1

    if line.startswith(('[POLYLINE]','[RGN40]')):
        node = ET.Element('way', visible='true', id=str(nodeid))
        nodeid -= 1
        node.append(source)
        polyline = True
        elementtagmap = polylinetagmap
        rnodes = {} # Track routing nodes for current polyline
        polyline_counter += 1

    if line.startswith(('[POLYGON]','[RGN80]')):
        node = ET.Element('way', visible='true', id=str(nodeid))
        nodeid -= 1
        node.append(source)
        polygon = True
        elementtagmap = polygontagmap
        polygon_counter += 1

    # parsing data
    if poi or polyline or polygon:
        
        if line.startswith('Label'):
            label = line.split('=')[1].strip()
            # Now strip out control codes such as ~[0x2f]
            codestart = label.find('~[')
            if codestart != -1:
                codeend = label.find(']',codestart)
                if codeend != -1:
                    label = label[0:codestart] + ' ' + label[codeend+1:]
            tag = ET.Element('tag', k='name',v=label.strip().title()) # convert to title case
            tag.tail = '\n    '
            node.append(tag)
        if line.startswith('Type'):
            typecode = line.split('=')[1].strip()
            tag = ET.Element('tag', k='garmin_type',v=typecode)
            tag.tail = '\n    '
            node.append(tag)
            typecode = int(typecode, 16) 
            for codes, taglist in elementtagmap.iteritems():
                if typecode in codes:
                    for key, value in taglist.iteritems():
                        tag = ET.Element('tag', k=key, v=value)
                        tag.tail = '\n    '
                        node.append(tag)
        if line.startswith('RoadID'):
            roadid = line.split('=')[1].strip()
            tag = ET.Element('tag', k='catmp-RoadID',v=roadid)
            tag.tail = '\n    '
            node.append(tag)
        if line.startswith('RouteParam'):
            rparams = line.split('=')[1].split(',')
            # speedval has speeds in km/h corresponding to RouteParam speed value index
            speedval = [8, 20, 40, 56, 72, 93, 108, 128]
            speed = ET.Element('tag', k='maxspeed', v=str(speedval[int(rparams[0])]))
            speed.tail = '\n    '
            node.append(speed)
            rclass = ET.Element('tag', k='garmin_road_class', v=str(rparams[1]))
            rclass.tail = '\n    '
            node.append(rclass)
            for att, attval in zip(('oneway', 'toll'), rparams[2:3]):
                if int(attval):
                    attrib = ET.Element('tag', k=att, v='true')
                    attrib.tail = '\n    '
                    node.append(attrib)
            # Note: taxi is not an approved access key
            vehicles = ['emergency', 'goods', 'motorcar', 'psv', 'taxi', 'foot', 'bicycle', 'hgv']
            for veh, res in zip(vehicles, rparams[4:]):
                vehtag = ET.Element('tag', k=veh, v=('yes', 'no')[int(res)]) 
                vehtag.tail = '\n    '
                node.append(vehtag)

        # Get nodes from all zoom levels (ie. Data0, Data1, etc)
        # TODO: Only grab the lowest-numbered data line (highest-resolution) and ignore the rest
        if line.startswith('Data'):
            if poi:
                coords = line.split('=')[1].strip()
                coords = coords.split(',')
                node.set('lat',str(float(coords[0][1:])))
                node.set('lon',str(float(coords[1][:-1])))
            if polyline or polygon:
                # Just grab the line and parse it later when the [END] element is encountered
                coords = line.split('=')[1].strip() + ','
                # TODO: parse out "holes" in a polygon by reading multiple Data0 lines and
                # constructing a multipolygon relation
        if line.startswith('Nod'):
            if polyline:
                # Store the point index and routing node id for later use
                nod = line.split('=')[1].strip().split(',', 2)
                rnodes[nod[0]] = nod[1]
        if line.startswith('[END]'):
            if polyline or polygon:
                # Have to write out nodes as they are parsed
                nodidx = 0
                nodIds = []
                reused = False
                while coords != '':
                    coords = coords.split(',', 2)
                    if str(nodidx) in rnodes:
                        if rnodes[str(nodidx)] in rNodeToOsmId:
                            curId = rNodeToOsmId[str(rnodes[str(nodidx)])]
                            reused = True
                        else:
                            curId = nodeid
                            nodeid -= 1
                            rNodeToOsmId[str(rnodes[str(nodidx)])] = curId
                    else:
                        curId = nodeid
                        nodeid -= 1
                    nodIds.append(curId)
                    # Don't write another node element if we reused an existing one
                    if not reused:
                        nodes = ET.Element('node', visible='true', id=str(curId), lat=str(float(coords[0][1:])), lon=str(float(coords[1][:-1])))
                        nodes.text = '\n    '
                        nodes.tail = '\n  '
                        osm.append(nodes)
                    coords = coords[2]
                    reused = False
                    nodidx += 1
                nodidx = 0
                for ndid in nodIds:
                    nd = ET.Element('nd', ref=str(ndid))
                    nd.tail = '\n    '
                    node.append(nd)
            if polygon:
                nd = ET.Element('nd', ref=str(nodIds[0]))
                nd.tail = '\n    '
                node.append(nd)

            poi = False 
            polyline = False 
            polygon = False 
            roadid = ''
            rnodes = {} # Clear out routing nodes to prepare for next entity

            node.text = '\n    '
            node.tail = '\n  '
            
            osm.append(node)

# writing to file
f = open('out.osm', 'w')
f.write(ET.tostring(osm))

# dump some stats
print '======'
print 'Totals'
print '======'
print 'POI', poi_counter
print 'POLYLINE', polyline_counter
print 'POLYGON', polygon_counter
print 'Last nodeid', nodeid
