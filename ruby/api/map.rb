#!/usr/bin/ruby -w


require 'cgi'
load 'osm/dao.rb'
require 'bigdecimal'
require 'osm/gpx'

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

nodes = dao.getnodes(trlat, bllon, bllat, trlon)

if nodes && nodes.length > 0
  linesegments = dao.getlines(nodes)
end

if linesegments
  used_nodes = {}
  
  linesegments.each do |key, l|
    nodes[l.node_a_uid] = dao.getnode(l.node_a_uid) unless nodes[l.node_a_uid]
    nodes[l.node_b_uid] = dao.getnode(l.node_b_uid) unless nodes[l.node_b_uid]
  end


  gpx = OSM::Gpx.new

  linesegments.each do |key, l|
    node_a = nodes[l.node_a_uid]
    node_b = nodes[l.node_b_uid]
    
    if node_a.visible ==true && node_b.visible == true
      used_nodes[l.node_a_uid] = node_a
      used_nodes[l.node_b_uid] = node_b
    
      gpx.addline(key, node_a, node_b)
    end
  end

  dangling_nodes = nodes.to_a - used_nodes.to_a

  dangling_nodes.each do |i,n|
    gpx.addnode(n) unless n.visible == false
  end
  

  puts gpx.to_s_pretty
else
  gpx = OSM::Gpx.new
  
  if nodes && nodes.length > 0
    
    nodes.to_a.each do |i,n|
      gpx.addnode(n) unless n.visible == false
    end

  else
    # nuffin there guv

  end

  puts gpx.to_s_pretty

end
