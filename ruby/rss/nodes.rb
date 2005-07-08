#!/usr/bin/ruby
require 'cgi'
require "mysql"
require 'rss/2.0'

# FIXME use and xml or rss library
# FIXME use an a

MYSQL_SERVER = "128.40.59.181"
MYSQL_USER = "openstreetmap"
MYSQL_PASS = "openstreetmap"
MYSQL_DATABASE = "openstreetmap"

cgi = CGI.new

latitude = cgi['latitude'].to_f
longitude = cgi['longitude'].to_f

begin

  #connect to the MySQL server
  dbh = Mysql.real_connect(MYSQL_SERVER, MYSQL_USER, MYSQL_PASS, MYSQL_DATABASE)
  # get server version string and display it

  res = dbh.query("select a.latitude, a.longitude, a.timestamp, a.visible, b.user  from (select * from nodes where latitude < #{latitude} + .01 and latitude > #{latitude} - .01 and longitude <  #{longitude} + .01 and longitude > #{longitude} - .01 order by timestamp desc) as a, (select * from user) as b where a.user_uid = b.uid group by a.uid limit 40;")

  rss = RSS::Rss.new("2.0")
  chan = RSS::Rss::Channel.new
  chan.title = "OpenStreetMap nodes near #{latitude}/#{longitude}"
  chan.description = "This is a list of the nodes near #{latitude}/#{longitude}"
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

    if row['visible'] == '1'
      state = 'Active'
    else
      state = 'Removed'
    end

    item.title = state + ' node'
    item.link = "http://www.openstreetmap.org/edit/viewMap.jsp?lat=#{row['latitude']}&lon=#{row['longitude']}&scale=10404.917"
    
    item.description = state + " node at #{row['latitude']}/#{row['longitude']} last edited by #{row['user'].gsub('.',' dot ').gsub('@',' at ')} "
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
