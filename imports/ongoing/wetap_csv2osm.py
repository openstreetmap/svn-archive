#!/usr/bin/python
## ##  Author: Bryce Nesbitt, June 2013
##  Licence: Public Domain, no rights reserved
##
##  WeTap is an android application for collecting drinking water locations,
##  built specifically to fee to OSM.  This converts the Google Fusion Table
##  to a JOSM file.  The JOSM file is hand curated into OSM.
##
import sys, re, urllib, urllib2
import csv  #demjson
import xml.sax.saxutils
from   pprint     import pprint

osmid = 0

if len(sys.argv) != 2 :
    sys.stderr.write("Please supply a wetap spreadsheet filename\n")
    sys.exit(5)

sys.stderr.write("Importing spreadsheet from WeTap fusion table\n")

print('<?xml version="1.0" encoding="UTF-8"?>')
print('<osm version="0.6" generator="osmsync:wetap">')
print('<changeset>')
print(u'\t<tag k="{}" v="{}"/>'.format('source','wetap.org android application'))
print(u'\t<tag k="{}" v="{}"/>'.format('bot','curated'))
print(u'\t<tag k="{}" v="{}"/>'.format('note','import of data collected via wetap android application.  Each location is brought into JOSM for review and tweaks prior to upload to OSM.'))
print('</changeset>')

with open(sys.argv[1], 'rb') as csvfile:
    csvreader = csv.DictReader(csvfile)
    for row in csvreader:

        if not row['timestamp']:
            continue
        if row['inappropriate']:
            continue
        if row['deleted'] and row['deleted'] != '0':
            continue
        if row['source'] == 'nyc':
            continue

        # Skip for now the conflation efforts
        if not row['source:pkey']:
            continue

        for k in row:
            row[k]=urllib2.unquote(row[k])
            row[k]=row[k].replace("+"," ")
            row[k]=row[k].replace("&","&amp;")
            row[k]=row[k].replace("'","&apos;")

        osmid = osmid-1
        lat, lng = row['latlon'].split(',')
        print("<node action='{}' lat='{}' lon='{}' id='{}'>").format('add',lat,lng,osmid)
        print("\t<tag k='{}' v='{}'/>").format('amenity','drinking_water')

        v = (row['name'] + ' ' + row['description']).strip()
        if v:
            print("\t<tag k='{}' v='{}'/>").format('description',v)

        v = row['source:pkey']
        if v:
            print("\t<tag k='{}' v='{}'/>").format('source:pkey',v)
        v = row['bottle']
        if v:
            print("\t<tag k='{}' v='{}'/>").format('wetap:bottle',v)
        v = row['image']
        if v:
            # The given URL is blocked from direct viewing:
            # http://mdcwetap.atwebpages.com/Alberto1330636753054.jpg
            # So there is a script of this form:
            #'http://mdcwetap.atwebpages.com/jpg.php?i=Alberto1330636753054.jpg'
            v = re.sub(r'mdcwetap.atwebpages.com/',r'mdcwetap.atwebpages.com/jpg.php?i=',v)
            print("\t<tag k='{}' v='{}'/>").format('wetap:photo',v)           #  URL!!!
        v = row['stateofrepair']
        if v:
            print("\t<tag k='{}' v='{}'/>").format('wetap:status',v)
        v = row['stateofrepairnote']
        if v:
            print("\t<tag k='{}' v='{}'/>").format('wetap:statusnote',v)
        print("\t<tag k='{}' v='{}'/>").format('source','wetap.org')

        print("</node>")

print('</osm>')
