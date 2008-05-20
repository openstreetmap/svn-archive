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

osm = ET.Element("osm", version='0.5', generator='mp2osm_catmp' )
osm.text = '\n  '
osm.tail = '\n'
attrib = ET.Element('tag', k='source',v=attribution)
attrib.tail = '\n    '
nodeid = -1

for line in file_mp:
    # Marker for start of sections
    if '[POI]' in line:
        node = ET.Element("node", visible='true', id=str(nodeid))
        nodeid -= 1
        node.append(attrib)
        poi = True
        poi_counter += 1

    if '[POLYLINE]' in line:
        node = ET.Element("way", visible='true', id=str(nodeid))
        nodeid -= 1
        node.append(attrib)
        polyline = True
        startnode = nodeid
        rnodes = {} # Track routing nodes for current polyline
        polyline_counter += 1

    if '[POLYGON]' in line:
        node = ET.Element("way", visible='true', id=str(nodeid))
        nodeid -= 1
        node.append(attrib)
        polygon = True
        startnode = nodeid
        polygon_counter += 1

    # parsing data
    if poi or polyline or polygon:
        if line.startswith('Label'):
            tag = ET.Element('tag', k='name',v=line.split('=')[1].strip())
            tag.tail = '\n    '
            node.append(tag)
        if line.startswith('Type'):
            typecode = int(line.split('=')[1].strip())
            tag = ET.Element('tag', k='garmin_type',v=typecode)
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
            if int(rparams[2]):
                oneway = ET.Element('tag', k='oneway', v='true')
                oneway.tail = '\n    '
                node.append(oneway)
            if int(rparams[3]):
                toll = ET.Element('tag', k='toll', v='true')
                toll.tail = '\n    '
                node.append(toll)
            emergency = ET.Element('tag', k='emergency', v=('yes', 'no')[int(rparams[4])])
            emergency.tail = '\n    '
            node.append(emergency)
            delivery = ET.Element('tag', k='goods', v=('yes', 'no')[int(rparams[5])])
            delivery.tail = '\n    '
            node.append(delivery)
            motorcar = ET.Element('tag', k='motorcar', v=('yes', 'no')[int(rparams[6])])
            motorcar.tail = '\n    '
            node.append(motorcar)
            bus = ET.Element('tag', k='psv', v=('yes', 'no')[int(rparams[7])])
            bus.tail = '\n    '
            node.append(bus)
            # Note: taxi is not an approved access key
            taxi = ET.Element('tag', k='taxi', v=('yes', 'no')[int(rparams[8])])
            taxi.tail = '\n    '
            node.append(taxi)
            ped = ET.Element('tag', k='foot', v=('yes', 'no')[int(rparams[9])])
            ped.tail = '\n    '
            node.append(ped)
            bicycle = ET.Element('tag', k='bicycle', v=('yes', 'no')[int(rparams[10])])
            bicycle.tail = '\n    '
            node.append(bicycle)
            truck = ET.Element('tag', k='psv', v=('yes', 'no')[int(rparams[11])])
            truck.tail = '\n    '
            node.append(truck)

        # Get nodes from all zoom levels (ie. Data0, Data1, etc)
        # TODO: Only grab the lowest-numbered data line (highest-resolution) and ignore the rest
        if line.startswith('Data'):
            if poi:
                coords = line.split('=')[1].strip()
                coords = coords.split(',')
                node.set('lat',str(float(coords[0][1:])))
                node.set('lon',str(float(coords[1][:-1])))
            if polyline or polygon:
                # Just grab the line and parse it when the [END] element is encountered
                coords = line.split('=')[1].strip() + ','
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
                        nodes = ET.Element("node", visible='true', id=str(curId), lat=str(float(coords[0][1:])), lon=str(float(coords[1][:-1])))
                        nodes.text = '\n    '
                        nodes.tail = '\n  '
                        tag = ET.Element('tag', k='source',v=attribution)
                        tag.tail = '\n    '
                        nodes.append(tag)
                        if roadid:
                            tag = ET.Element('tag', k='catmp-RoadID',v=roadid)
                            tag.tail = '\n    '
                            nodes.append(tag)
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
