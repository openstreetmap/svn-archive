# Read srtm data files and put them in the database.
import pg, psycopg2
from osgeo import gdal, gdal_array
import database 
import sys
from math import sqrt
import os

import re
import zipfile

from data import util

# Main functions

def loadTile(continent, filename):
  # Unzip it
  zf = zipfile.ZipFile('data/' + continent + '/' + filename + ".hgt.zip")
  for name in zf.namelist():
    outfile = open('data/' + continent + '/' + name, 'wb')
    outfile.write(zf.read(name))
    outfile.flush()
    outfile.close()
  
  # Read it
  srtm = gdal.Open('data/' + continent + '/' + filename + '.hgt')
  
  # Clean up
  os.remove('data/' + continent + '/' + filename + '.hgt')
  
  return gdal_array.DatasetReadAsArray(srtm)



def createTableAltitude(db):
  tables = db.get_tables()
    
  if not('public.altitude' in tables): 
    db.query(" \
      CREATE TABLE altitude ( \
        pos bigint NOT NULL, \
        alt int NULL , \
        PRIMARY KEY ( pos ) \
      ); \
    ")
  return True
  
def connectToDatabase(database):
    return pg.DB(dbname=database.db,host='localhost', user=database.db_user, passwd=database.db_pass)
      
def connectToDatabasePsycopg2(database):
    conn = psycopg2.connect("dbname='" + database.db + "' host='localhost' user='" + database.db_user + "' password='" + database.db_pass + "'")
    return conn.cursor()

def dropAllTables(db):
    db.query("DROP TABLE IF EXISTS altitude;")

def checkDatabaseEmpty(db):
    # Test is the test database is as we expect it after setUp:
    return db.get_tables() == ['information_schema.sql_features', 'information_schema.sql_implementation_info', 'information_schema.sql_languages', 'information_schema.sql_packages', 'information_schema.sql_parts', 'information_schema.sql_sizing', 'information_schema.sql_sizing_profiles', 'public.geometry_columns', 'public.spatial_ref_sys']

def posFromLatLon(lat,lon):
  return (lat * 360 + lon) * 1200 * 1200

def fetchTopLeftAltitude(db, lat, lon):
  pos = posFromLatLon(lat,lon)
  sql = db.query(" \
    SELECT \
      alt \
    FROM altitude \
    WHERE \
      pos = " + str(pos) + "\
   ")
  return int(sql.getresult()[0][0])

def insertTileIntoDatabase(cur, db_name, tile, lat0, lon0):
  # I use the Psycopg2 connection, with its copy_to and 
  # copy_from commands, which use the more efficient COPY command. 
  # This method requires a temporary file.

  # Calculate begin position
  begin = posFromLatLon(lat0,lon0)

  # First we write the data into a temporary file.
  f = open('/tmp/tempcopy', 'w')
  # We drop the top row and right column.
  for row in range(1, len(tile)):
    for col in range(0, len(tile) - 1):
      f.write(str(\
      begin + (row-1) * 1200 + col\
      ) + "\t" + str(tile[row][col] ) + "\n")

  f.close() 

  # Now we read the data from the temporary file and put it in the
  # altitude table.

  f = open('/tmp/tempcopy', 'r')
  #cur.copy_from(f, 'altitude') 
  
  # Unfortunately the copy_from() command hangs on my computer for
  # some reason, so we try something else. See also:
  # http://lists.initd.org/pipermail/psycopg/2007-October/005684.html

  import subprocess

  #psqlcmd = os.path.join('c:\\', 'Program Files', 'PostgreSQL', '8.2',
  #'bin', 'psql')
  
  psqlcmd = "/usr/bin/psql"

  p = subprocess.Popen('psql ' + db_name +  ' -c "COPY altitude FROM STDIN;"', stdin=f, shell=True);
  p.wait()

  f.close
        
def readTileFromDatabase(db, lat0, lon0):
  # Calculate begin and end position
  begin = posFromLatLon(lat0,lon0)
  end = posFromLatLon(lat0 + 1, lon0 + 1)
  sql = db.query(" \
    SELECT \
      alt \
    FROM altitude \
    WHERE \
      pos >= " + str(begin) + "\
      AND pos < " + str(end) + "\
    ORDER BY pos ASC \
  ")
  res = sql.getresult()
  
  # Now turn the result into a 2D array

  tile = []
  
  # Calculate tile width (should be 1200, or 10 for test tiles)
  tile_width = int(sqrt(len(res)))
  i = 0
  for x in range(tile_width):
    row = []
    for y in range(tile_width):
      row.append(int(res[i][0]))
      i = i + 1

    tile.append(row)
  
  return tile
        
def verify(db, number_of_tiles, files_hashes, continent, north, south, west, east):
    # For every tile, verify the bottom left coordinate.
    for file in files_hashes:
      # Strip .hgt.zip extension:
      file = file[1][0:-8] 
    
      [lat,lon] = util.getLatLonFromFileName(file)
      
      # Only a smaller part of Australia (see below):
      if util.inBoundingBox(lat, lon, north, south, west, east):
      
        print "Verify " + file + "..." 
    
        # Get top left altitude from file:
        coordinate_file = loadTile(continent, file)[1][0]
    
        # Get top left altitude from database:
        coordinate_db = fetchTopLeftAltitude(db,lat,lon)
        
        if coordinate_db != coordinate_file:
          print "Mismatch tile " + file[1]
          exit() 
    
    # Check the total number of points in the database:
    
    print "Check the total number of points in the database..."
    
    sql = db.query("SELECT count(pos) FROM altitude")
    total = int(sql.getresult()[0][0])
    if not total == number_of_tiles * 1200 * 1200:
      print "Not all tiles have been (completely) inserted!"
      exit()
        
    print "All tiles seem to have made it into the database! Enjoy."
    
    exit()

def main():
  db = connectToDatabase(database) 
  from data import files
  
  try:
      continent = sys.argv[1]
      
  except: 
      print "Please specify the continent. Africa, Australia, Eurasia, Islands, North_America or South_America."

  # Does the user want to empty the database?
  if 'empty' in sys.argv:
    print "Deleting tables from databse..." 
    dropAllTables(db)
    print "Done..."
    exit()

  [north, south, west, east] = util.getBoundingBox(sys.argv, 3)
      
  files_hashes = util.getFilesHashes(continent)
  
  number_of_tiles = util.numberOfFiles(files_hashes, north, south, west, east)
  
  # Verify result?
  if 'verify' in sys.argv:
    verify(db, number_of_tiles, files_hashes, continent,  north, south, west, east)

  # If a tile name is given as the sixth argument it will resume from there.
  p = re.compile('[NSEW]\d*')
  resume_from = ""
  try:
    if(p.find(sys.argv[6])):
      resume_from = sys.argv[6]
      
  except: 
    None
    
  # Second database connection with psychopg2 
  db_psycopg2 = connectToDatabasePsycopg2(database)

  createTableAltitude(db)

  i = 0

  for file in files_hashes:
    # Strip .hgt.zip extension:
    file = file[1][0:-8] 
    # Get latitude and longitude from file name 
    [lat,lon] = util.getLatLonFromFileName(file)
    
    if util.inBoundingBox(lat, lon, north, south, west, east):
      i = i + 1
  
      # Are we resuming?
      if resume_from == file:
        resume_from = "" 
      
      if resume_from == "":

        # Load tile from file
        tile = loadTile(continent, file)

        # First check if the tile is not already in the database:

        try:
          fetchTopLeftAltitude(db, lat, lon)
          print("Skipping tile " + file + " (" + str(i) + " / " + str(number_of_tiles) + ") ...")

        except:
          print("Insert data for tile " + file + " (" + str(i) + " / " + str(number_of_tiles) + ") ...")

          insertTileIntoDatabase(db_psycopg2, "srtm" , tile, lat, lon)

  print("All tiles inserted. Pleasy verify the result with python \
  read_data.py verify")


if __name__ == '__main__':
  main()
