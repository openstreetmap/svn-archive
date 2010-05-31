# -*- coding: utf-8 -*-
# by kay - basic functions

### config for Toolserver
DSN = 'dbname=osm-mapnik'
openlayertextfilename = '/home/kayd/parkingicons/parkingicons.txt'

import sys
import psycopg2
import csv

if len(sys.argv) > 1:
    DSN = sys.argv[1]

print "Opening connection using dns:", DSN
conn = psycopg2.connect(DSN)
print "Encoding for this connection is", conn.encoding
curs = conn.cursor()

openlayertextfile = csv.writer(open(openlayertextfilename, 'w'), delimiter='\t',quotechar='"', quoting=csv.QUOTE_MINIMAL)
openlayertextfile.writerow(['lat','lon','title','description','icon','iconSize','iconOffset'])

latlon= "ST_Y(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),ST_X(ST_Transform(ST_line_interpolate_point(way,0.5),4326))"
FW = "FROM planet_line WHERE"

### display disc - maxstay
pc_disc_maxstay = ''
for side in ['left','right']:
    curs.execute("SELECT osm_id,"+latlon+",(tags->'parking:condition:"+side+":maxstay') as \"parking:condition:"+side+":maxstay\" "+FW+" (tags ? 'parking:condition:"+side+":maxstay') and (tags ? 'parking:condition:"+side+"') and (tags->'parking:condition:"+side+"')='disc'")
    pc_disc_maxstay += curs.fetchall()
print pc_disc_maxstay
exit(0)

for pcm_id in pcm_ids:
    openlayertextfile.writerow([pcm_id[1],pcm_id[2],'Disc parking','Maximum parking time:<br>'+pcm_id[3],'parkingicons/pi-disc.png','16,16','-8,-8'])

### display vehicles

curs.execute("SELECT osm_id,"+latlon+",\"parking:condition:left:vehicles\" "+FW+" \"parking:condition:left:vehicles\" is not NULL")
pclv_ids = curs.fetchall()
curs.execute("SELECT osm_id,"+latlon+",\"parking:condition:right:vehicles\" "+FW+" \"parking:condition:right:vehicles\" is not NULL")
pcrv_ids = curs.fetchall()
pcv_ids = pclv_ids + pcrv_ids

vehicle_icons = {"car":"parkingicons/pi-car.png" , "bus":"parkingicons/pi-bus.png" , "motorcycle":"parkingicons/pi-motorcycle.png"}

for pcv_id in pcv_ids:
    vehicle_icon = vehicle_icons.get(pcv_id[3],"parkingicons/pi-unkn.png");
    openlayertextfile.writerow([pcv_id[1],pcv_id[2],'Parking only for','Vehicle: '+pcv_id[3],vehicle_icon,'16,16','-8,-8'])

conn.rollback()

sys.exit(0)



"""
SELECT
   ST_Y(way) AS lat_wgs84,
   ST_X(way) AS lon_wgs84,
   ST_X(transform(way, 31466)) AS KOORD_X,
   ST_Y(transform(way, 31466)) AS KOORD_Y,
   skeys(tags) AS key,
   svals(tags) AS value,
   osm_id
FROM
   dortmund_point
WHERE
   exist(tags, 'amenity')
LIMIT 10;

SELECT
   ST_Y(way) AS lat_wgs84,
   ST_X(way) AS lon_wgs84,
   ST_X(transform(way, 31466)) AS KOORD_X,
   ST_Y(transform(way, 31466)) AS KOORD_Y,
   key,
   value,
   tags->'name' as name,
   osm_id
FROM
   (SELECT
       osm_id, way, tags,
       (each(tags)).key,
       (each(tags)).value
    FROM
       dortmund_point
    WHERE
       exist(tags, 'amenity') /*AND exist(tags, 'name')*/
   ) AS sq
WHERE
   key = 'amenity'
LIMIT 10;

SELECT
   ST_Y(way) AS lat_wgs84,
   ST_X(way) AS lon_wgs84,
   ST_X(transform(way, 31466)) AS KOORD_X,
   ST_Y(transform(way, 31466)) AS KOORD_Y,
   key,
   value,
   tags->'name' as name,
   tags->'sport' as sport,
   osm_id
FROM
   (SELECT
       osm_id, tags,
       ST_Centroid(way) as way,
       (each(tags)).key,
       (each(tags)).value
    FROM
       dortmund_polygon
    WHERE
       tags->'natural' = 'water'
       AND
       tags->'sport' = 'swimming'
   ) AS sq
WHERE
   key = 'natural'
LIMIT 10;
"""
