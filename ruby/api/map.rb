#!/usr/bin/ruby -w


require 'cgi'
load 'osm/dao.rb'
load 'osm/ox.rb'
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

if bllat > trlat || bllon > trlon
  exit BAD_REQUEST
end

dao = OSM::Dao.instance
ox = OSM::Ox.new

nodes = dao.getnodes(trlat, bllon, bllat, trlon)

if nodes && nodes.length > 0
  linesegments = dao.getlines(nodes)
end

if linesegments # get nodes we dont have yet
  
  linesegments.each do |key, l|
    nodes[l.node_a_uid] = dao.getnode(l.node_a_uid) unless nodes[l.node_a_uid]
    nodes[l.node_b_uid] = dao.getnode(l.node_b_uid) unless nodes[l.node_b_uid]
  end

end

#now add nodes first, segments after


if nodes
  nodes.each do |i,n|
    ox.add_node(n) unless n.visible == false
  end
end


if linesegments
  linesegments.each do |key, l|
    node_a = nodes[l.node_a_uid]
    node_b = nodes[l.node_b_uid]
    
    if node_a.visible ==true && node_b.visible == true
 
      ox.add_segment(l)
    end
  end

end
 

puts ox.to_s_pretty
