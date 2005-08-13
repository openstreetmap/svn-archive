#!/usr/bin/ruby -w


require 'cgi'
require 'osm/dao'
require 'osm/gpx'

include Apache

cgi = CGI.new

nodeid = cgi['nodeid'].to_i


if nodeid != 0
  dao = OSM::Dao.instance
  gpx = OSM::Gpx.new

  node = dao.getnode(nodeid)

  if node
    gpx.addnode(node)
    puts gpx.to_s_pretty
  else
    exit HTTP_NOT_FOUND
  end
end
