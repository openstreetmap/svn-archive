#!/usr/bin/ruby
require 'cgi'
require "mysql"
require 'rexml/document'

include REXML

MYSQL_SERVER = "128.40.59.181"
MYSQL_USER = "openstreetmap"
MYSQL_PASS = "openstreetmap"
MYSQL_DATABASE = "openstreetmap"

cgi = CGI.new

if cgi['latitude'].length > 0 then
	latitude = cgi['latitude'].to_f
else
	mostrecent = true
end
if cgi['longitude'].length > 0 then
	longitude = cgi['longitude'].to_f
else
	mostrecent = true
end

begin

  #connect to the MySQL server
  dbh = Mysql.real_connect(MYSQL_SERVER, MYSQL_USER, MYSQL_PASS, MYSQL_DATABASE)
  # get server version string and display it

  if ! mostrecent then
  	query = "select a.latitude, a.longitude, a.timestamp, a.visible, b.user  from (select * from nodes where latitude < #{latitude} + .01 and latitude > #{latitude} - .01 and longitude <  #{longitude} + .01 and longitude > #{longitude} - .01 order by timestamp desc) as a, (select * from user) as b where a.user_uid = b.uid group by a.uid limit 40;"
  	description = "OpenStreetMap nodes near #{latitude}/#{longitude}"
  else
	query = "select a.latitude, a.longitude, a.timestamp, a.visible, b.user from (select * from nodes order by timestamp desc limit 40) as a, (select * from user) as b where a.user_uid = b.uid group by a.uid limit 40;"
	description = "OpenStreetMap most recently edited nodes"
  end

  res = dbh.query(query)

  rss = Element.new 'rss'
  rss.attributes['version'] = "2.0"
  rss.attributes['xmlns:geo'] = "http://www.w3.org/2003/01/geo/wgs84_pos#"
  channel = Element.new 'channel', rss
  title = Element.new 'title', channel
  title.text = description
  description_el = Element.new 'description', channel
  description_el.text = description
  link = Element.new 'link', channel
  link.text = 'http://www.openstreetmap.org/'

  image = Element.new 'image', channel
  url = Element.new 'url', image
  url.text = "http://www.openstreetmap.org/feeds/mag_map-rss2.0.png"
  title = Element.new 'title', image
  title.text = "OpenStreetMap"
  width = Element.new 'width', image
  width.text = 100
  height = Element.new 'height', image
  height.text = 100
  link = Element.new 'link', image
  link.text = 'http://www.openstreetmap.org/'
 
  res.each_hash do |row|
    item = Element.new 'item', channel

    if row['visible'] == '1'
      state = 'Active'
    else
      state = 'Removed'
    end

    lat = sprintf("%0.10f", row['latitude'])
    lon = sprintf("%0.10f", row['longitude'])

    title = Element.new 'title', item
    title.text = state + ' node'
    link = Element.new 'link', item
    link.text = "http://www.openstreetmap.org/edit/view-map.html?lat=#{lat}&lon=#{lon}&scale=6.6666666e-05"
   
    description = Element.new 'description', item
    description.text = state + " node at #{lat}/#{lon} last edited by #{row['user'].gsub('.',' dot ').gsub('@',' at ')}, " + Time.at( row["timestamp"].to_i / 1000 ).to_s
    pubDate = Element.new 'pubDate', item
    pubDate.text = Time.at( row["timestamp"].to_i / 1000 )

    lat_el = Element.new 'geo:lat', item
    lat_el.text = lat
    lon_el = Element.new 'geo:lon', item
    lon_el.text = lon
  end

  puts rss.to_s

rescue MysqlError => e
  print "Error code: ", e.errno, "\n"
  print "Error message: ", e.error, "\n"

ensure
  # disconnect from server
  dbh.close if dbh
end
