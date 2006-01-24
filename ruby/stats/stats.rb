#!/usr/bin/ruby
require "mysql"
require 'rss/2.0'

start_time = Time.now

MYSQL_SERVER = "128.40.59.181"
MYSQL_USER = "openstreetmap"
MYSQL_PASS = "openstreetmap"
MYSQL_DATABASE = "openstreetmap"


puts '<html><head><title>OpenStreetMap stats</title></head><body>'
puts '<h2>OpenStreetMap stats report run at ' + Time.new.to_s + '</h2>'

begin

  #connect to the MySQL server
  dbh = Mysql.real_connect(MYSQL_SERVER, MYSQL_USER, MYSQL_PASS, MYSQL_DATABASE)
  q = 'select * from (select count(*) as no_of_users from users where active = true) as h, (select count(*) as gpspoints from gps_points) as j, (select count(*) as nodes from meta_nodes) as k, (select count(*) as segments from meta_segments) as l, (select count(*) as seg_names from segments where tags != \'\') as m;'
#  puts q
  res = dbh.query(q)
  res.each_hash do |row|
    puts "<table>
      <tr><td>Number of users</td><td>#{row['no_of_users']}</td></tr>
      <tr><td>Number of uploaded GPS points</td><td>#{row['gpspoints']}</td></tr>
      <tr><td>Number of nodes</td><td>#{row['nodes']}</td></tr>
      <tr><td>Number of line segments</td><td>#{row['segments']}</td></tr>
      <tr><td>Number of segments with tags</td><td>#{row['seg_names']}</td></tr>
      </table><br>"
  end

  puts '<h2>Top 10 users for uploads of gps data</h2><table><tr><td><b>User</b></td><td><b>No. of points</b></td></tr>'
  res = dbh.query('select sum(size) as size, email from gpx_files, users where user_id = users.id group by user_id order by size desc limit 10;')

  res.each_hash do |row|
    puts "<tr><td>#{row['email'].gsub('@',' at ').gsub('.',' dot ')}</td><td>#{row['size']}</td></tr>"
  end
  
  puts '</table>'


  puts '<h2>Number of users editing over the past...</h2><table><tr>
  <td><b>Data type</b></td>
  <td><b>Day</b></td>
  <td><b>Week</b></td>
  <td><b>Month</b></td>
  </tr>'

  res = dbh.query("select * from (select count(*) as day from (select user_id from gpx_files where timestamp > NOW() - INTERVAL 1 DAY group by user_id) as a) as b,
                                 (select count(*) as week from (select user_id from gpx_files where timestamp > NOW() - INTERVAL 7 DAY group by user_id) as c) as d,
                                 (select count(*) as month from (select user_id from gpx_files where timestamp > NOW() - INTERVAL 28 DAY group by user_id) as e) as f;")

  res.each_hash do |row|
    puts "<tr><td><b>GPX Files</b></td> <td>#{row['day']}</td><td>#{row['week']}</td><td>#{row['month']}</td> </tr>"
  end
 

  res = dbh.query("select * from (select count(*) as day from (select user_id from nodes where timestamp > NOW() - INTERVAL 1 DAY group by user_id) as a) as b,
                                 (select count(*) as week from (select user_id from nodes where timestamp > NOW() - INTERVAL 7 DAY group by user_id) as c) as d,
                                 (select count(*) as month from (select user_id from nodes where timestamp > NOW() - INTERVAL 28 DAY group by user_id) as e) as f;")

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

puts '<br>Report took ' + (Time.new - start_time).to_s + ' seconds to run.'
puts '</body></html>'

