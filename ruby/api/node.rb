#!/usr/bin/ruby -w


require 'cgi'
require 'osm/dao'
require 'osm/gpx'

include Apache

#puts ENV['REMOTE_USER']

r = Apache.request
cgi = CGI.new

nodeid = cgi['nodeid'].to_i


if nodeid != 0
  dao = OSM::Dao.instance
  gpx = OSM::Gpx.new

  node = dao.getnode(nodeid)

  gpx.addnode(node)
  puts gpx
end

