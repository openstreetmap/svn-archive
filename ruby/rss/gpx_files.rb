#!/usr/bin/ruby
require "mysql"
require 'rss/2.0'

# FIXME use and xml or rss library
# FIXME use an a

MYSQL_SERVER = "128.40.59.181"
MYSQL_USER = "openstreetmap"
MYSQL_PASS = "openstreetmap"
MYSQL_DATABASE = "openstreetmap"

begin

  #connect to the MySQL server
  dbh = Mysql.real_connect(MYSQL_SERVER, MYSQL_USER, MYSQL_PASS, MYSQL_DATABASE)
  # get server version string and display it

  res = dbh.query('select a.timestamp, a.name, b.count, b.latitude, b.longitude,c.user from (select * from points_meta_table where visible = true order by timestamp desc limit 20) as a, (select count(*) as count ,gpx_id,latitude, longitude from tempPoints group by gpx_id) as b, (select * from user) as c where a.uid = b.gpx_id and a.user_uid = c.uid order by timestamp desc;')

  rss = RSS::Rss.new("2.0")
  chan = RSS::Rss::Channel.new
  chan.title = 'OpenStreetMap GPX Uploads'
  chan.description = 'This is a list of the most recent GPX file uploads to openstreetmap.org'
  chan.link = 'http://www.openstreetmap.org/'
  rss.channel = chan

  image = RSS::Rss::Channel::Image.new
  image.url = "http://www.openstreetmap.org/feeds/mag_map-rss2.0.png"
  image.title = "OpenStreetMap"
  image.width = 100
  image.height = 100
  image.link = chan.link
  chan.image = image
  
  res.each_hash do |row|
    
    item = RSS::Rss::Channel::Item.new
    item.title = row['name']
    item.link = "http://www.openstreetmap.org/edit/edit-map.html?lat=#{row['latitude']}&lon=#{row['longitude']}&scale=6.6666666e-05"
    item.description = "GPX file made by #{row['user'].gsub('.',' dot ').gsub('@',' at ')} with #{row['count']} points."
    item.date = Time.at( row["timestamp"].to_i / 1000 )
    chan.items << item 

  end

  puts rss.to_s

rescue MysqlError => e
  print "Error code: ", e.errno, "\n"
  print "Error message: ", e.error, "\n"

ensure
  # disconnect from server
  dbh.close if dbh
end
