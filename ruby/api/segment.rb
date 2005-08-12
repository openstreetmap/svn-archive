#!/usr/bin/ruby -w


require 'cgi'
require 'osm/dao'
require 'osm/gpx'

include Apache


r = Apache.request
cgi = CGI.new

segmentid = cgi['segmentid'].to_i


if segmentid != 0
  dao = OSM::Dao.instance
  gpx = OSM::Gpx.new

  segment = dao.getsegment(segmentid)

  
  gpx.addline(segment.uid, segment.node_a, segment.node_b)
  puts gpx.to_s_pretty
end

