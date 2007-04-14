#!/usr/bin/ruby
require 'cgi'
require 'rexml/document'
require 'osm/dao.rb'

include REXML

# FIXME: abstract the rss in to its own class like the ruby rss library
# FIXME: handle sql errors that don't get caught *here* because of call_sql

cgi = CGI.new

if cgi['latitude'].length == 0 || cgi['longitude'].length == 0
  mostrecent = true
else
	latitude = cgi['latitude'].to_f
	longitude = cgi['longitude'].to_f
end

begin

	description = "OpenStreetMap most recently edited nodes"
  
  if mostrecent then
  	query = 'select latitude, longitude, timestamp, visible from current_nodes order by timestamp desc limit 20'
  else
  	query = "select latitude, longitude, timestamp, visible from current_nodes where latitude < #{latitude} + .1 and latitude > #{latitude} - .1 and longitude <  #{longitude} + .1 and longitude > #{longitude} - .1 order by timestamp desc limit 20"

  	description += " near #{latitude}, #{longitude}"
  end

  dao = OSM::Dao.instance
  res = dao.call_sql { query }
 
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
    link.text = "http://www.openstreetmap.org/index.html?lat=#{lat}&lon=#{lon}&zoom=15"
   
    description = Element.new 'description', item
    description.text = state + " node at #{lat}/#{lon} edited at " + Time.parse( row["timestamp"]).to_s
    pubDate = Element.new 'pubDate', item
    pubDate.text = Time.parse( row["timestamp"])

    lat_el = Element.new 'geo:lat', item
    lat_el.text = lat
    lon_el = Element.new 'geo:lon', item
    lon_el.text = lon
  end

  puts rss.to_s

rescue MysqlError => e
  puts "Error code: ", e.errno, "\n"
  puts "Error message: ", e.error, "\n"
end

