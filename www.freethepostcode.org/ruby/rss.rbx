#!/usr/bin/ruby
require 'cgi'
require 'dao.rb'
require 'rexml/document'

include REXML

dao = OSM::Dao.instance
cgi = CGI.new

latitude = cgi['latitude'].to_f
longitude = cgi['longitude'].to_f

res = dao.call_sql { "select part1, part2, lat, lon, date from codes where confirmed = 1 order by date desc limit 10;" }


  description = 'New postcodes entered at freethepostcode.org'

  rss = Element.new 'rss'
  rss.attributes['version'] = "2.0"
  rss.attributes['xmlns:geo'] = "http://www.w3.org/2003/01/geo/wgs84_pos#"
  channel = Element.new 'channel', rss
  title = Element.new 'title', channel
  title.text = description
  description_el = Element.new 'description', channel
  description_el.text = description
  link = Element.new 'link', channel
  link.text = 'http://www.freethepostcode.org/'

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
  link.text = 'http://www.freethepostcode.org/'
 
  res.each_hash do |row|
    item = Element.new 'item', channel

    lat = sprintf("%0.10f", row['lat'])
    lon = sprintf("%0.10f", row['lon'])
    postcode = row['part1'] + ' ' + row['part2']

    title = Element.new 'title', item
    title.text = postcode

    link = Element.new 'link', item
    link.text = "http://www.freethepostcode.org/geocode?lat=#{lat}&lon=#{lon}"
   
    description = Element.new 'description', item
    description.text = "Postcode #{postcode} at #{lat}/#{lon}"
    pubDate = Element.new 'pubDate', item
    pubDate.text = Time.parse( row['date'])

    lat_el = Element.new 'geo:lat', item
    lat_el.text = lat
    lon_el = Element.new 'geo:lon', item
    lon_el.text = lon
  end

puts rss.to_s
