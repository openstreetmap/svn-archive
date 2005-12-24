#!/usr/bin/ruby
require "mysql"
require 'rss/2.0'

MYSQL_SERVER = "128.40.59.181"
MYSQL_USER = "openstreetmap"
MYSQL_PASS = "openstreetmap"
MYSQL_DATABASE = "openstreetmap"

now = Time.new

puts '<html><head><title>OpenStreetMap stats</title></head><body>'
puts '<h2>OpenStreetMap stats report run at ' + now.to_s + '</h2>'

millis =  now.to_i * 1000

begin

  #connect to the MySQL server
  dbh = Mysql.real_connect(MYSQL_SERVER, MYSQL_USER, MYSQL_PASS, MYSQL_DATABASE)
  q = 'select * from (select count(*) as users from user where active = true) as h, (select count(*) as gpspoints from tempPoints) as j, (select count(*) as nodes from node_meta_table) as k, (select count(*) as segments from street_segment_meta_table) as l, (select count(*) as seg_names from street_segments where tags != \'\') as m;'
#  puts q
  res = dbh.query(q)
  res.each_hash do |row|
    puts "<table>
      <tr><td>Number of users</td><td>#{row['users']}</td></tr>
      <tr><td>Number of uploaded GPS points</td><td>#{row['gpspoints']}</td></tr>
      <tr><td>Number of nodes</td><td>#{row['nodes']}</td></tr>
      <tr><td>Number of line segments</td><td>#{row['segments']}</td></tr>
      <tr><td>Number of segments with tags</td><td>#{row['seg_names']}</td></tr>
      </table><br>"
  end

  puts '<h2>Top 10 users for uploads of gps data</h2><table><tr><td><b>User</b></td><td><b>No. of points</b></td></tr>'
  res = dbh.query('select sum(size) as size, user from points_meta_table, user where user_uid = user.uid group by user_uid order by size desc limit 10;')

  res.each_hash do |row|
    puts "<tr><td>#{row['user'].gsub('@',' at ').gsub('.',' dot ')}</td><td>#{row['size']}</td></tr>"
  end
  
  puts '</table>'



  puts '<h2>Number of users editing over the past...</h2><table><tr>
  <td><b>Data type</b></td>
  <td><b>Day</b></td>
  <td><b>Week</b></td>
  <td><b>Month</b></td>
  </tr>'

  res = dbh.query("select * from (select count(*) as day from (select user_uid from points_meta_table where timestamp > #{millis} - (1000 * 60 *  60 * 24) group by user_uid) as a) as b, (select count(*) as week from (select user_uid from points_meta_table where timestamp > #{millis} - (1000 * 60 *  60 * 24 * 7) group by user_uid) as c) as d, (select count(*) as month from (select user_uid from points_meta_table where timestamp > #{millis} - (1000 * 60 *  60 * 24 * 7 * 4) group by user_uid) as e) as f;")

  res.each_hash do |row|
    puts "<tr><td><b>GPX Files</b></td> <td>#{row['day']}</td><td>#{row['week']}</td><td>#{row['month']}</td> </tr>"
  end
 

  res = dbh.query("select * from (select count(*) as day from (select user_uid from nodes where timestamp > #{millis} - (1000 * 60 *  60 * 24) group by user_uid) as a) as b, (select count(*) as week from (select user_uid from nodes where timestamp > #{millis} - (1000 * 60 *  60 * 24 * 7) group by user_uid) as c) as d, (select count(*) as month from (select user_uid from nodes where timestamp > #{millis} - (1000 * 60 *  60 * 24 * 7 * 4) group by user_uid) as e) as f;")

  res.each_hash do |row|
    puts "<tr><td><b>Nodes</b></td> <td>#{row['day']}</td><td>#{row['week']}</td><td>#{row['month']}</td> </tr>"
  end
 
  puts '</table>'
  
  

rescue MysqlError => e
  print "Error code: ", e.errno, "\n"
  print "Error message: ", e.error, "\n"

ensure
  # disconnect from server
  dbh.close if dbh
end

puts '<br>Report took ' + (Time.new - now).to_s + ' seconds to run.'
puts '</body></html>'

