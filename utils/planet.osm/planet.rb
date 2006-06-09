#!/usr/bin/ruby -w

#require 'cgi'
require 'osm/dao.rb'
require 'bigdecimal'
#include Apache
# This should be put in the directory above the 'osm' directory containing
# 'dao.rb' to work properly

def get_kv(tags)
	kv = {}
	tagarray = tags.split(';')
	tagarray.each do |kvpair|
		kvpairarray = kvpair.split('=')
		kv[kvpairarray[0]] = kvpairarray[1]
	end
	return kv
end
		
def print_kv(kv)
	kv.each do |k,v|
		unless v==nil
			v1 = v.gsub(/['"]/,"&quot;") # escape quotes
			v2 = v1.gsub(/</,"&lt;") # escape <
			v3 = v2.gsub(/>/,"&gt;") # escape >
			puts "<tag k='#{k}' v='#{v3}' />"
		end
	end
end

#r = Apache.request
#cgi = CGI.new


#bllon, bllat, trlong, trlat = bbox.map { |elem| elem.to_f }

if ARGV.length < 4
	puts "Usage: planet.rb bllon bllat trlon trlat"
	exit
end

bllon = ARGV[0].to_f
bllat = ARGV[1].to_f
trlon = ARGV[2].to_f
trlat = ARGV[3].to_f

to = nil
#to = Time.parse(cgi['to']) unless cgi['to'] == ''

if bllat > trlat || bllon > trlon
    puts "That is not a sensible bounding box, you silly person."
	exit
end

dao = OSM::Dao.instance

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

  nodes.merge!(dao.get_nodes_by_ids(nodes_missing, to)) unless nodes_missing.empty?
end

puts "<?xml version='1.0'?>"
puts "<osm version='0.3'>"

if nodes
  nodes.each do |i,n|
	  #ox.add_node(n) unless !n.visible
	  unless !n.visible
	  	puts "<node id='#{i}' lat='#{n.latitude}' lon='#{n.longitude}' >" 
		kv = get_kv(n.tags)
		print_kv(kv)
		puts "</node>"
	  end
  end
end

if linesegments
  linesegments.each do |key, l|
    if nodes[l.node_a_id].visible && nodes[l.node_b_id].visible
      #ox.add_segment(l)

	  puts "<segment id='#{key}' from='#{l.node_a_id}' to='#{l.node_b_id}' >" 
	  kv = get_kv(l.tags)
	  print_kv(kv)
	  puts "</segment>"
    end
  end
end

[:way, :area].each do |type|
  if seg_ids != []
    dao.get_multis_from_segments(seg_ids, type).each do |n|
      #ox.add_multi(n,type)
	  puts "<#{type} id='#{n.id}'>"
	  print_kv(n.tags)
	  n.segs.each do |segid|
	  	puts "<seg id='#{segid}' />"
	  end
	  puts "</#{type}>"
    end
  end
end

puts "</osm>"
GC.start
