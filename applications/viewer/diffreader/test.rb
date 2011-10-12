require 'rubygems'
require 'sqlite3'

#Just a little test to see if ruby SQLite3 gem is working

output_db = "./baseball/baseball-edits.db"

@db = SQLite3::Database.new(output_db ) 

#@db.execute( "INSERT INTO edits (timestamp, element_type, osm_id, user_name, changeset) VALUES (?, ?, ?, ?, ?);",
#   "test", 1234, "test", 1234 )



