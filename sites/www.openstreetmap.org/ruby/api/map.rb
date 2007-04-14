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
  cgi.header(
      "status" => "BAD_REQUEST",
      "Error" => "The minimum longitude must be less than the maximum longitude, and the minimum latitude must be less than the maximum latitude"
  )
  exit
end

if bllat < -90 || trlat < -90 || bllat > 90 || trlat > 90 || bllon < -180 || trlon < -180 || bllon > 180 || trlon > 180 # ||  ( (trlon - bllon) * (trlat - bllat) ) > (0.0035 * 4)
  cgi.header(
      "status" => "BAD_REQUEST",
      "Error" => "The latitudes must be between -90 and 90, and longitudes between -180 and 180"
  )
  exit
end

if ( (trlon - bllon) * (trlat - bllat) ) > (0.0035 * 4)
  cgi.header(
      "status" => "BAD_REQUEST",
      "Error" => "Your requested bounding box was too large. Either request a smaller area, or use planet.osm"
  )
  exit
end


dao = OSM::Dao.instance
ox = OSM::Ox.new

nodes = dao.getnodes(trlat, bllon, bllat, trlon, to)

if nodes && nodes.length > 0
  linesegments = dao.get_segments_from_nodes(nodes, to)
end

seg_ids = []

if linesegments # get nodes we dont have yet
  nodes_missing = []
  
  linesegments.each do |key, l|
    seg_ids << key
    nodes_missing << l.node_a_id unless nodes[l.node_a_id]
    nodes_missing << l.node_b_id unless nodes[l.node_b_id]
  end

  nodes.merge!(dao.get_nodes_by_ids(nodes_missing, to)) unless nodes_missing.empty?
end

if nodes
  nodes.each do |i,n|
	  ox.add_node(n) unless !n.visible
    if n.latitude < bllat then bllat = n.latitude end
    if n.longitude < bllon then bllon = n.longitude end
    if n.latitude > trlat then trlat = n.latitude end
    if n.longitude > trlon then trlon = n.longitude end
  end
end


ox.addbounds(bllat,bllon,trlat,trlon)

if linesegments
  linesegments.each do |key, l|
    if nodes[l.node_a_id].visible && nodes[l.node_b_id].visible
      ox.add_segment(l)
    end
  end
end

[:way].each do |type|
#[:way, :area].each do |type|
  if seg_ids != []
    dao.get_multis_from_segments(seg_ids, type).each do |n|
      ox.add_multi(n,type)
    end
  end
end



ox.print_http(r)

GC.start
