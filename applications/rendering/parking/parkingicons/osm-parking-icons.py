# -*- coding: utf-8 -*-
# by kay - basic functions

DSN = 'dbname=gis'

import sys
import psycopg2
import csv

if len(sys.argv) > 1:
    DSN = sys.argv[1]

print "Opening connection using dns:", DSN
conn = psycopg2.connect(DSN)
print "Encoding for this connection is", conn.encoding
curs = conn.cursor()

openlayertextfile = csv.writer(open('/home/kayd/shellscripts/parkingicons.txt', 'w'), delimiter='\t',quotechar='"', quoting=csv.QUOTE_MINIMAL)
openlayertextfile.writerow(['lat','lon','title','description','icon','iconSize','iconOffset'])

#curs.execute("SELECT column_name FROM information_schema.columns WHERE table_name ='planet_osm_line'")
#colnames = curs.fetchall()
#print colnames

### display disc - maxstay
"""
curs.execute("SELECT osm_id,ST_Y(ST_Transform(ST_Centroid(way),4326)),ST_X(ST_Transform(ST_Centroid(way),4326)),\"parking:condition:left:maxstay\" FROM planet_osm_line WHERE \"parking:condition:left:maxstay\" is not NULL")
pclm_ids = curs.fetchall()
curs.execute("SELECT osm_id,ST_Y(ST_Transform(ST_Centroid(way),4326)),ST_X(ST_Transform(ST_Centroid(way),4326)),\"parking:condition:right:maxstay\" FROM planet_osm_line WHERE \"parking:condition:right:maxstay\" is not NULL")
pcrm_ids = curs.fetchall()
"""
curs.execute("SELECT osm_id,ST_Y(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),ST_X(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),\"parking:condition:left:maxstay\" FROM planet_osm_line WHERE \"parking:condition:left:maxstay\" is not NULL and \"parking:condition:left\"='disc'")
pclm_ids = curs.fetchall()
curs.execute("SELECT osm_id,ST_Y(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),ST_X(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),\"parking:condition:right:maxstay\" FROM planet_osm_line WHERE \"parking:condition:right:maxstay\" is not NULL and \"parking:condition:right\"='disc'")
pcrm_ids = curs.fetchall()
pcm_ids = pclm_ids + pcrm_ids
#print pcm_ids

for pcm_id in pcm_ids:
    openlayertextfile.writerow([pcm_id[1],pcm_id[2],'Disc parking','Maximum parking time:<br>'+pcm_id[3],'parkingicons/pi-disc.png','16,16','-8,-8'])

### display vehicles

curs.execute("SELECT osm_id,ST_Y(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),ST_X(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),\"parking:condition:left:vehicles\" FROM planet_osm_line WHERE \"parking:condition:left:vehicles\" is not NULL")
pclv_ids = curs.fetchall()
curs.execute("SELECT osm_id,ST_Y(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),ST_X(ST_Transform(ST_line_interpolate_point(way,0.5),4326)),\"parking:condition:right:vehicles\" FROM planet_osm_line WHERE \"parking:condition:right:vehicles\" is not NULL")
pcrv_ids = curs.fetchall()
pcv_ids = pclv_ids + pcrv_ids

vehicle_icons = {"car":"parkingicons/pi-car.png" , "bus":"parkingicons/pi-bus.png" , "motorcycle":"parkingicons/pi-motorcycle.png"}

for pcv_id in pcv_ids:
    vehicle_icon = vehicle_icons.get(pcv_id[3],"parkingicons/pi-unkn.png");
    openlayertextfile.writerow([pcv_id[1],pcv_id[2],'Parking only for','Vehicle: '+pcv_id[3],vehicle_icon,'16,16','-8,-8'])

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








#print "------------------------------"

#print zip(*colnames)

print "--------------------------------------------------"
print "Set no_stopping on trunk roads"
curs.execute("UPDATE planet_osm_line set \"parking:lane:both\"='no_stopping' where highway IN ('trunk','trunk_link') and \"parking:lane:both\" is NULL and \"parking:lane:left\" is NULL and \"parking:lane:right\" is NULL")
print "-> " , curs.statusmessage

print "Set no_stopping on roundabouts"
curs.execute("UPDATE planet_osm_line set \"parking:lane:both\"='no_stopping' where junction='roundabout' and \"parking:lane:both\" is NULL and \"parking:lane:left\" is NULL and \"parking:lane:right\" is NULL")
print "-> " , curs.statusmessage



print "--------------------------------------------------"
print "Finland conversion:"

print "Converting :both to :left + :right"
for lane in ['inline','orthogonal','diagonal','no']:
  print "Writing 'parking:left/right=%s' where 'parking:both=%s'" % (lane,lane)
  curs.execute("UPDATE planet_osm_line SET \"parking:left\"=%s,\"parking:right\"=%s,\"parking:both\"=NULL WHERE \"parking:both\"=%s",(lane,lane,lane))
  print "-> " , curs.statusmessage

print "Converting halting:both to :left + :right"
for lane in ['no']:
  print "Writing 'halting:left/right=%s' where 'halting:both=%s'" % (lane,lane)
  curs.execute("UPDATE planet_osm_line SET \"halting:left\"=%s,\"halting:right\"=%s,\"halting:both\"=NULL WHERE \"halting:both\"=%s",(lane,lane,lane))
  print "-> " , curs.statusmessage

print "Converting no to no_parking"
print "Writing 'parking:left=no_parking' where 'parking:left=no'"
curs.execute("UPDATE planet_osm_line SET \"parking:left\"='no_parking' WHERE \"parking:left\"='no'",())
print "-> " , curs.statusmessage
print "Writing 'parking:right=no_parking' where 'parking:right=no'"
curs.execute("UPDATE planet_osm_line SET \"parking:right\"='no_parking' WHERE \"parking:right\"='no'",())
print "-> " , curs.statusmessage

print "Converting halting:no to no_stopping"
print "Writing 'parking:left=no_stopping' where 'halting:left=no'"
curs.execute("UPDATE planet_osm_line SET \"parking:left\"='no_stopping' WHERE \"halting:left\"='no'",())
print "-> " , curs.statusmessage
print "Writing 'parking:right=no_stopping' where 'halting:right=no'"
curs.execute("UPDATE planet_osm_line SET \"parking:right\"='no_stopping' WHERE \"halting:right\"='no'",())
print "-> " , curs.statusmessage

print "Converting parking:left/right to parking:lane:left/right"
for lane in ['inline','orthogonal','diagonal','no_parking','no_stopping','fire_lane']:
  print "Writing 'parking:lane:left=%s' where 'parking:left=%s'" % (lane,lane)
  curs.execute("UPDATE planet_osm_line SET \"parking:lane:left\"=%s WHERE \"parking:left\"=%s",(lane,lane))
  print "-> " , curs.statusmessage
  print "Writing 'parking:lane:right=%s' where 'parking:right=%s'" % (lane,lane)
  curs.execute("UPDATE planet_osm_line SET \"parking:lane:right\"=%s WHERE \"parking:right\"=%s",(lane,lane))
  print "-> " , curs.statusmessage

print "Converting parking:fee=yes to parking:condition:both=ticket"
print "Writing 'parking:condition:both=ticket' where 'parking:fee=yes'"
curs.execute("UPDATE planet_osm_line SET \"parking:condition:both\"='ticket' WHERE \"parking:fee\"='yes'",())

print "End Finland conversion"
print "--------------------------------------------------"

print "--------------------------------------------------"
print "Converting :both to :left + :right"

for lane in ['inline','orthogonal','diagonal','no_parking','no_stopping','fire_lane']:
  print "Writing 'parking:lane:left/right=%s' where 'parking:lane:both=%s'" % (lane,lane)
  #print curs.mogrify("UPDATE planet_osm_line SET \"parking:lane:left\"=%s,\"parking:lane:right\"=%s,\"parking:lane:both\"=NULL WHERE \"parking:lane:both\"=%s",(lane,lane,lane))
  curs.execute("UPDATE planet_osm_line SET \"parking:lane:left\"=%s,\"parking:lane:right\"=%s,\"parking:lane:both\"=NULL WHERE \"parking:lane:both\"=%s",(lane,lane,lane))
  print "-> " , curs.statusmessage

for condition in ['free','disc','ticket','customers','residents','private']:
  print "Writing 'parking:condition:left/right=%s' where 'parking:lane:both=%s'" % (condition,condition)
  #print curs.mogrify("UPDATE planet_osm_line SET \"parking:condition:left\"=%s,\"parking:condition:right\"=%s,\"parking:condition:both\"=NULL WHERE \"parking:condition:both\"=%s and osm_id=47907684",[condition,condition,condition])
  curs.execute("UPDATE planet_osm_line SET \"parking:condition:left\"=%s,\"parking:condition:right\"=%s,\"parking:condition:both\"=NULL WHERE \"parking:condition:both\"=%s",[condition,condition,condition])
  print "-> " , curs.statusmessage

print "--------------------------------------------------"
print "Customer parking"

print "Writing 'parking:condition:area=customers' where 'amenity=parking' and 'access=customers'"
curs.execute("UPDATE planet_osm_polygon set \"parking:condition:area\"='customers' where amenity='parking' and access='customers' and \"parking:condition:area\" is NULL")
print "-> " , curs.statusmessage

print "Disc parking"

print "Writing 'parking:condition:area=disc' where 'amenity=parking' and 'disc_parking=yes'"
curs.execute("UPDATE planet_osm_polygon set \"parking:condition:area\"='disc' where amenity='parking' and disc_parking='yes' and \"parking:condition:area\" is NULL")
print "-> " , curs.statusmessage
print "Writing 'parking:condition:area=disc' where 'amenity=parking' and 'maxstay not NULL'"
curs.execute("UPDATE planet_osm_polygon set \"parking:condition:area\"='disc' where amenity='parking' and maxstay is not NULL and \"parking:condition:area\" is NULL")
print "-> " , curs.statusmessage

print "--------------------------------------------------"
print "unknown parking (lane but no condition)"

curs.execute("UPDATE planet_osm_line set \"parking:condition:left\"='unknown' where  \"parking:lane:left\" IN ('inline','orthogonal','diagonal') and \"parking:condition:left\" is NULL")
print "-> " , curs.statusmessage

curs.execute("UPDATE planet_osm_line set \"parking:condition:right\"='unknown' where  \"parking:lane:right\" IN ('inline','orthogonal','diagonal') and \"parking:condition:right\" is NULL")
print "-> " , curs.statusmessage

#conn.rollback()
conn.commit()

sys.exit(0)

