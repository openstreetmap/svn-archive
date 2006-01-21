#!/usr/bin/ruby
load 'osm/dao.rb'
require 'rexml/document'

include REXML

begin

  dao = OSM::Dao.instance

  res = dao.call_sql { 'select a.timestamp, a.name, a.size as count, a.latitude, a.longitude,c.email from 
    (select * from gpx_files where visible = true order by timestamp desc limit 20) as a, 
    (select * from users) as c
      where a.user_id = c.id order by timestamp desc;' }

  rss = Element.new 'rss'
  rss.attributes['version'] = "2.0"
  rss.attributes['xmlns:geo'] = "http://www.w3.org/2003/01/geo/wgs84_pos#"
  channel = Element.new 'channel', rss
  title = Element.new 'title', channel
  title.text =  'OpenStreetMap GPX Uploads'
  description_el = Element.new 'description', channel
  description_el.text = 'OpenStreetMap GPX Uploads'
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
    lat = 0
    lon = 0
    lat = sprintf("%0.10f", row['latitude']) unless row['latitude'].to_i == 0
    lon = sprintf("%0.10f", row['longitude']) unless row ['longitude'].to_i == 0

    title = Element.new 'title', item
    title.text = row['name']
    link = Element.new 'link', item
    link.text = "http://www.openstreetmap.org/edit.html?lat=#{lat}&lon=#{lon}&zoom=14"
   
    description = Element.new 'description', item
    description.text = "GPX file made by #{row['email'].gsub('.',' dot ').gsub('@',' at ')} with #{row['count']} points."
    pubDate = Element.new 'pubDate', item
    pubDate.text = Time.parse(row["timestamp"])

    lat_el = Element.new 'geo:lat', item
    lat_el.text = lat
    lon_el = Element.new 'geo:lon', item
    lon_el.text = lon
  end

  puts rss.to_s


rescue MysqlError => e
  print "Error code: ", e.errno, "\n"
  print "Error message: ", e.error, "\n"

end
