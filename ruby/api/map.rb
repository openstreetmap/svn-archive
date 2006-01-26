#!/usr/bin/ruby -w

require 'cgi'
load 'osm/dao.rb'
load 'osm/ox.rb'
require 'bigdecimal'
require 'zlib'


include Apache
include REXML

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
    nodes[l.node_a_id] = dao.getnode(l.node_a_id) unless nodes[l.node_a_id]
    nodes[l.node_b_id] = dao.getnode(l.node_b_id) unless nodes[l.node_b_id]
  end

end

#if gzip encoding, use gzip stream instead of plain
out = $stdout
gzipped = r.headers_in['Accept-Encoding'] && r.headers_in['Accept-Encoding'].match(/gzip/)
if gzipped
  r.headers_out['Content-Encoding'] = 'gzip'
  r.content_type = 'text/html'
  r.send_http_header
  out = Zlib::GzipWriter.new $stdout
end

#now send nodes first, segments after
out.puts "<osm version='0.2'>"

if nodes
  nodes.each do |i,n|
    if n.visible == true
      e = Element.new 'node'
      e.attributes['uid'] = n.id
      e.attributes['lat'] = n.latitude
      e.attributes['lon'] = n.longitude
      e.attributes['tags'] = n.tags
      e.write out
      out.puts "\n"
    end
  end
end


if linesegments
  linesegments.each do |key, l|
    node_a = nodes[l.node_a_id]
    node_b = nodes[l.node_b_id]
    
    if node_a.visible ==true && node_b.visible == true
      e = Element.new 'segment'
      e.attributes['uid'] = l.id
      e.attributes['from'] = l.node_a_id
      e.attributes['to'] = l.node_b_id
      e.attributes['tags'] = l.tags
      e.write out
      out.puts "\n"
    end
  end

end

out.puts "</osm>"

GC.start

out.close unless not gzipped
