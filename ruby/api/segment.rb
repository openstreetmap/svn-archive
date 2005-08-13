#!/usr/bin/ruby -w

require 'cgi'
require 'osm/dao'
require 'osm/gpx'

include Apache

cgi = CGI.new

segmentid = cgi['segmentid'].to_i


if segmentid != 0
  dao = OSM::Dao.instance
  gpx = OSM::Gpx.new

  segment = dao.getsegment(segmentid)

  if segment
    gpx.addline(segment.uid, segment.node_a, segment.node_b)
    puts gpx.to_s_pretty
  else
    exit HTTP_NOT_FOUND
  end
end

