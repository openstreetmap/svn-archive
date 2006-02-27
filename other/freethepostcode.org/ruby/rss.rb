#!/usr/bin/ruby
require 'cgi'
require "mysql"
require 'rss/2.0'


MYSQL_SERVER = "127.0.0.1"
MYSQL_USER = "postcode"
MYSQL_PASS = "kc8dFusmw"
MYSQL_DATABASE = "postcode"

cgi = CGI.new

latitude = cgi['latitude'].to_f
longitude = cgi['longitude'].to_f

begin

  #connect to the MySQL server
  dbh = Mysql.real_connect(MYSQL_SERVER, MYSQL_USER, MYSQL_PASS, MYSQL_DATABASE)
  # get server version string and display it

  res = dbh.query("select part1, part2, lat, lon, date from codes where confirmed = 1 order by date desc limit 10;")
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

rescue MysqlError => e
  print "Error code: ", e.errno, "\n"
  print "Error message: ", e.error, "\n"

ensure
  # disconnect from server
  dbh.close if dbh
end
