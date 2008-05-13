# derived from 'mp2osm_ukraine.py' 
# modified by simon@mungewell.org
# license: GPL V2 or later

import xml.etree.ElementTree as ET

attribution = 'Calgary Area Trail Mapping Project'
file_mp = open('short.mp')

# flags and global variable
poi = False
polyline = False
polygon = False
roadid = ''

# debug/stats counters
poi_counter = 0
polyline_counter = 0
polygon_counter = 0

osm = ET.Element("osm", version='0.5', generator='mp2osm_catmp' )
nodeid = -1

for line in file_mp:
    # Marker for start of sections
    if '[POI]' in line:
        node = ET.Element("node", visible='true', id=str(nodeid))
        nodeid -= 1
        node.append(ET.Element('tag', k='source',v=attribution))
        poi = True
        poi_counter += 1

    if '[POLYLINE]' in line:
        node = ET.Element("way", visible='true', id=str(nodeid))
        nodeid -= 1
        node.append(ET.Element('tag', k='source',v=attribution))
        polyline = True
        startnode = nodeid
        polyline_counter += 1

    if '[POLYGON]' in line:
        node = ET.Element("way", visible='true', id=str(nodeid))
        nodeid -= 1
        node.append(ET.Element('tag', k='source',v=attribution))
        polygon = True
        startnode = nodeid
        polygon_counter += 1

    # parsing data
    if poi or polyline or polygon:
        if 'Label' in line:
            node.append(ET.Element('tag', k='name',v=line.split('=')[1].strip()))
        if 'Type' in line:
            node.append(ET.Element('tag', k='Garmin-Type',v=line.split('=')[1].strip()))
        if 'RoadID' in line:
            roadid = line.split('=')[1].strip()
            node.append(ET.Element('tag', k='catmp-RoadID',v=roadid))
        if 'RouteParam' in line:
            node.append(ET.Element('tag', k='Garmin-RouteParam',v=line.split('=')[1].strip()))

        # Get nodes from all zoom levels (ie. Data0, Data1, etc)
        if 'Data' in line:
            if poi:
                coords = line.split('=')[1].strip()
                coords = coords.split(',')
                node.set('lat',str(float(coords[0][1:])))
                node.set('lon',str(float(coords[1][:-1])))

            if polyline or polygon:
                # Have to write out nodes as they are parsed
                coords = line.split('=')[1].strip() + ','
                while coords != '':
                    coords = coords.split(',', 2)
                    nodes = ET.Element("node", visible='true', id=str(nodeid), lat=str(float(coords[0][1:])), lon=str(float(coords[1][:-1])))
                    nodeid -= 1
                    nodes.append(ET.Element('tag', k='attribution',v=attribution))
                    if roadid:
                        nodes.append(ET.Element('tag', k='catmp-RoadID',v=roadid))

                    osm.append(nodes)
                    coords = coords[2]

        if '[END]' in line:
            if polyline or polygon:
                currentnode = startnode
                while currentnode != nodeid:
                    node.append(ET.Element('nd', ref=str(currentnode)))
                    currentnode -= 1

            if polygon:
                node.append(ET.Element('nd', ref=str(startnode)))

            poi = False 
            polyline = False 
            polygon = False 
            roadid = ''

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
