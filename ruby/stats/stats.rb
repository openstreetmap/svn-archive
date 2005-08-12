#!/usr/bin/ruby
require "mysql"
require 'rss/2.0'

MYSQL_SERVER = "128.40.59.181"
MYSQL_USER = "openstreetmap"
MYSQL_PASS = "openstreetmap"
MYSQL_DATABASE = "openstreetmap"

now = Time.new

puts '<html><head><title>OpenStreetmap stats</title></head><body>'
puts '<h2>OpenStreetmap stats report run at ' + now.to_s + '</h2>'

begin

  #connect to the MySQL server
  dbh = Mysql.real_connect(MYSQL_SERVER, MYSQL_USER, MYSQL_PASS, MYSQL_DATABASE)

  res = dbh.query('select * from (select count(*) as users from user where active = true) as h, (select count(*) as gpspoints from tempPoints) as j, (select count(*) as nodes from node_meta_table) as k, (select count(*) as segments from street_segment_meta_table) as l;')

  res.each_hash do |row|
    puts "<table>
      <tr><td>Number of users</td><td>#{row['users']}</td></tr>
      <tr><td>Number of uploaded GPS points</td><td>#{row['gpspoints']}</td></tr>
      <tr><td>Number of nodes</td><td>#{row['nodes']}</td></tr>
      <tr><td>Number of line segments</td><td>#{row['segments']}</td></tr>
      </table><br>"
  end

  puts '<h2>Top 10 users for uploads of gps data</h2><table><tr><td><b>User</b></td><td><b>No. of points</b></td></tr>'
  res = dbh.query('select user, count from (select uid, count(*) as count from tempPoints group by uid) as a, (select * from user) as b where a.uid = b.uid order by count desc limit 10;')

  res.each_hash do |row|
    puts "<tr><td>#{row['user']}</td><td>#{row['count']}</td></tr>"
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

