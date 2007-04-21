Notes on loadcsv.sql
--------------------

1 - It is a load script to populate current map tables in MySQL, based on mysql schema 
out of OSM svn /sql folder, from .csv files output from Osm2Csv.java.

2 - This assumes a test user exists of id '5147' - that user was poked into table 'users'.

3 - The current imports have a lot of missing (referenced ids but no corresponding rows) entities
across all tables.
