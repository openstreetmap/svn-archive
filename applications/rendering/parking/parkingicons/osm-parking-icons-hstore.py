# -*- coding: utf-8 -*-
# by kay - basic functions

### config for Toolserver
#DSN = 'dbname=osm_mapnik host=sql-mapnik'
#openlayertextfilename = '/home/kayd/parkingicons/parkingicons.txt'

### config for devserver
#DSN = '?'
#openlayertextfilename = '/osm/parking/parkingicons/parkingicons.txt'


import sys
import psycopg2
import csv

if len(sys.argv) == 2:
    DSN = sys.argv[1]
    openlayertextfilename = sys.argv[2]
else:
    print "usage: tool 'dbname=osm_mapnik host=sql-mapnik' '/home/kayd/parkingicons/parkingicons.txt'"
    exit(0);

print "Opening connection using dns:", DSN
conn = psycopg2.connect(DSN)
print "Encoding for this connection is", conn.encoding
curs = conn.cursor()

openlayertextfile = csv.writer(open(openlayertextfilename, 'w'), delimiter='\t',quotechar='"', quoting=csv.QUOTE_MINIMAL)
openlayertextfile.writerow(['lat','lon','title','description','icon','iconSize','iconOffset'])

latlon= "ST_Y(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),ST_X(ST_Transform(ST_line_interpolate_point(way,0.5),4326))"
FW = "FROM planet_line WHERE"

### display disc - maxstay

pc_disc_maxstay = []
for side in ['left','right','both']:
    curs.execute("SELECT osm_id,"+latlon+",(tags->'parking:condition:"+side+":maxstay') as \"parking:condition:"+side+":maxstay\" "+FW+" (tags ? 'parking:condition:"+side+":maxstay') and (tags ? 'parking:condition:"+side+"') and (tags->'parking:condition:"+side+"')='disc'")
    pc_disc_maxstay += curs.fetchall()

for pc_dm in pc_disc_maxstay:
    openlayertextfile.writerow([pc_dm[1],pc_dm[2],'Disc parking','Maximum parking time:<br>'+pc_dm[3],'parkingicons/pi-disc.png','16,16','-8,-8'])

### display vehicles

pc_vehicles = []
for side in ['left','right','both']:
    curs.execute("SELECT osm_id,"+latlon+",(tags->'parking:condition:"+side+":vehicles') as \"parking:condition:"+side+":vehicles\" "+FW+" (tags ? 'parking:condition:"+side+":vehicles')")
    pc_vehicles += curs.fetchall()

vehicle_icons = {"car":"parkingicons/pi-car.png" , "bus":"parkingicons/pi-bus.png" , "motorcycle":"parkingicons/pi-motorcycle.png"}

for pc_v in pc_vehicles:
    vehicle_icon = vehicle_icons.get(pc_v[3],"parkingicons/pi-unkn.png");
    openlayertextfile.writerow([pc_v[1],pc_v[2],'Parking only for','Vehicle: '+pc_v[3],vehicle_icon,'16,16','-8,-8'])

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
