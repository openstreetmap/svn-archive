# -*- coding: cp1251 -*- 
# file POIforSHAPKA_final.mp uses cp1251 encoding

import elementtree.ElementTree as ET
# to transfer larger data sets use cElementTree
# it is much faster
# included in standard Python 2.5, for earlier versions can be downloaded from
# http://effbot.org/zone/celementtree.htm

attribution = 'travelgps.com.ua'

# places that are already mapped in the region, ignored in generated file
places_ignore = set(['Донецьк','Маріуполь','Курахове'])

nas_numbers = dict()

# cities with population > 100 thousand
# http://ru.wikipedia.org/wiki/%D0%9D%D0%B0%D1%81%D0%B5%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5_%D0%94%D0%BE%D0%BD%D0%B5%D1%86%D0%BA%D0%BE%D0%B9_%D0%BE%D0%B1%D0%BB%D0%B0%D1%81%D1%82%D0%B8
cities = set([ "Донецьк", "Макіївка", "Маріуполь", "Горлівка", "Краматорськ", "Слов'янськ" ])

# file from http://www.travelgps.com.ua
# http://www.travelgps.com.ua/forum/viewtopic.php?t=198
file_mp = open('travelgps/POIforSHAPKA_final.mp')

poi_start = False
regions_start = False
region = ''
name_rus = ''
name = ''
type = ''
nas = ''

# set of regions that are considred "good"
# this is used to filter by regions
good_regions = set()
# string that must be in region for region to be "good"
good_criteria_str = 'Дон.обл.'

osm = ET.Element("osm", generator='mp2osm_urkaine', version='0.5')
nodeid = -1

# counters to check sums of places with different types in mp and osm files
town_counter = 1
city_counter = 1
village_counter = 1

for line in file_mp:

    if '[Regions]' in line:
        regions_start = True

    # selecting good regions based on criteria string
    if regions_start:
        if good_criteria_str in line:
            position_eq = line.find('=')
            position_n = line.find('n')
            good_region = line[position_n+1:position_eq]
            good_regions.add(good_region)

    if '[POI]' in line:
        poi_start = True

    # writing point
    if '[END]' in line:
        poi_start = False 
        regions_start = False 
        if name_rus != '' and (name not in places_ignore):
            type = 'village'
            if name in cities:
                type = 'city'
                city_counter += 1
            else:
                if nas=='М' or nas=='ВМ':
                    type = 'town'
                    town_counter += 1
                else:
                    village_counter += 1
            node = ET.Element("node", visible='true', id=str(nodeid), lat=str(lat), lon=str(lon))
            node.append(ET.Element('tag', k='name',v=name.decode('cp1251')))
            node.append(ET.Element('tag', k='name:ru',v=name_rus.decode('cp1251')))
            node.append(ET.Element('tag', k='place',v=type))
            node.append(ET.Element('tag', k='is_in',v='Ukraine'))
            node.append(ET.Element('tag', k='koatuu',v=koatuu))
            node.append(ET.Element('tag', k='attribution',v=attribution))
            osm.append(node)
            nodeid -= 1
        region = ''
        name = ''
        name_rus = ''
        type = ''
        koatuu = ''
        nas = ''

    # parsing point
    if poi_start:
        if 'Type' in line:
            type = line.split('=')[1].strip()
        if 'Data' in line:
            coords = line.split('=')[1].strip()
            coords = coords.split(',')
            lat = float(coords[0][1:])
            lon = float(coords[1][:-1])
        if 'Region' in line:
            region = line.split('=')[1].strip()
        if region in good_regions: 
            if 'russian' in line:
                name_rus = line.split('=')[1].strip()
            if 'ukrainian' in line:
                name = line.split('=')[1].strip()
            if 'KOATUU' in line:
                koatuu = line.split('=')[1].strip()
            if 'Nas' in line:
                nas = line.split('=')[1].strip()
                if nas_numbers.has_key(nas):
                    nas_numbers[nas] += 1
                else:
                    nas_numbers[nas] = 1

# writing to file
f = open('out.osm', 'w')
f.write(ET.tostring(osm).encode('cp1251'))

# checking different places types
print 'types in MP'
total = 0
for nas in nas_numbers.keys():
    print nas.decode('cp1251'), nas_numbers[nas]
    total += nas_numbers[nas]
print 'total ', total 

print '\ntypes in OSM'
print 'city ', city_counter-1
print 'town ', town_counter-1
print 'village ', village_counter-1
print 'ignoring ', len(places_ignore)
print 'total ', city_counter+town_counter+village_counter-3+len(places_ignore)
