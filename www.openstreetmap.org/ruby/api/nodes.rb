#!/usr/bin/ruby -w

require 'cgi'
require 'osm/dao.rb'
require 'osm/ox.rb'
require 'rexml/document'

include Apache
include REXML

r = Apache.request
r.content_encoding='text/xml'
dao = OSM::Dao.instance

cgi = CGI.new
nodes = cgi['nodes']

ox = OSM::Ox.new
nodes.scan(/[0-9]+/){ |nodeid|

  node = dao.getnode(nodeid)
  
  if node && node.visible
    ox.add_node(node)
  end
}

puts ox.to_s
