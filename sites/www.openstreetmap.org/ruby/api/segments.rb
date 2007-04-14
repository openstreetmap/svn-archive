#!/usr/bin/ruby -w

# REST call to GET multiple segments
# Richard Fairhurst, July 2006
# (first contribution to OSM svn, please treat me gently)

require 'cgi'
require 'osm/dao.rb'
require 'osm/ox.rb'
require 'rexml/document'

include Apache
include REXML

r = Apache.request
dao = OSM::Dao.instance

cgi = CGI.new
segments = cgi['segments']

ox = OSM::Ox.new
segments.scan(/[0-9]+/){ |segmentid|

  segment = dao.getsegment(segmentid)
  
  if segment && segment.visible && segment.node_a.visible && segment.node_b.visible
    ox.add_segment(segment)
  end
}

puts ox.to_s
