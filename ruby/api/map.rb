#!/usr/bin/ruby -w

require 'cgi'
require 'osm/dao.rb'
require 'osm/ox.rb'
require 'bigdecimal'
include Apache

r = Apache.request
cgi = CGI.new

bbox = cgi['bbox'].split(',')

#bllon, bllat, trlong, trlat = bbox.map { |elem| elem.to_f }

bllon = bbox[0].to_f
bllat = bbox[1].to_f
trlon = bbox[2].to_f
trlat = bbox[3].to_f

to = nil
to = Time.parse(cgi['to']) unless cgi['to'] == ''

if bllat > trlat || bllon > trlon
  exit BAD_REQUEST
end

dao = OSM::Dao.instance
ox = OSM::Ox.new

nodes = dao.getnodes(trlat, bllon, bllat, trlon, to)

if nodes && nodes.length > 0
  linesegments = dao.getlines(nodes, to)
end

seg_ids = []

if linesegments # get nodes we dont have yet
  nodes_missing = []
  
  linesegments.each do |key, l|
    seg_ids << key
    nodes_missing << l.node_a_id unless nodes[l.node_a_id]
    nodes_missing << l.node_b_id unless nodes[l.node_b_id]
  end

  nodes.merge!(dao.get_nodes_by_ids(nodes_missing, to))
end

[:way, :area].each do |type|
  dao.get_multis_from_segments(seg_ids, type).each do |n|
    ox.add_multi(n,type)
  end
end

if nodes
  nodes.each do |i,n|
	  ox.add_node(n) unless !n.visible
  end
end

if linesegments
  linesegments.each do |key, l|
    if nodes[l.node_a_id].visible && nodes[l.node_b_id].visible
      ox.add_segment(l)
    end
  end
end

ox.print_http(r)

GC.start
