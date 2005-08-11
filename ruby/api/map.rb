#!/usr/bin/ruby -w


require 'cgi'
require 'osm/dao'
require 'bigdecimal'
require 'osm/gpx'

include Math


r = Apache.request
cgi = CGI.new

bbox = cgi['bbox'].split(',')

bllat = bbox[0].to_f
bllon = bbox[1].to_f
trlat = bbox[2].to_f
trlon = bbox[3].to_f

dao = OSM::Dao.instance

nodes = dao.getnodes(trlat, bllon, bllat, trlon)

if nodes && nodes.length > 0
  linesegments = dao.getlines(nodes)
end

if linesegments
  linesegments.each do |key, l|
    nodes[l.node_a_uid] = dao.getnode(l.node_a_uid) unless nodes[l.node_a_uid]
    nodes[l.node_b_uid] = dao.getnode(l.node_b_uid) unless nodes[l.node_b_uid]
  end
end

gpx = OSM::Gpx.new

linesegments.each do |key, l|
  node_a = nodes[l.node_a_uid]
  node_b = nodes[l.node_b_uid]

  gpx.addline(key, node_a, node_b)
end

puts gpx.to_s_pretty

