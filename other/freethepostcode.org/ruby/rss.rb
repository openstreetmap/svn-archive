#!/usr/bin/ruby
require 'cgi'
require 'rss/2.0'
require 'dao.rb'

dao = OSM::Dao.instance
cgi = CGI.new

latitude = cgi['latitude'].to_f
longitude = cgi['longitude'].to_f

res = dao.call_sql { "select part1, part2, lat, lon, date from codes where confirmed = 1 order by date desc limit 10;" }
rss = RSS::Rss.new("2.0")
chan = RSS::Rss::Channel.new
chan.title = "Free the postcode!"
chan.description = "Latest postcodes submitted to freethepostcode.org"
chan.link = 'http://www.freethepostcode.org/'
rss.channel = chan

res.each_hash do |row|

  item = RSS::Rss::Channel::Item.new

  item.title = row['part1'] + ' ' + row['part2']
  item.link = "http://www.freethepostcode.org/currentlist"

  item.description = row['lat'] + '/' + row['lon']
  item.date = Time.parse(row['date'])
  chan.items << item 

end

puts rss.to_s
