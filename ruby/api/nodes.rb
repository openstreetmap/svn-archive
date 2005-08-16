#!/usr/bin/ruby -w


require 'cgi'
load 'osm/dao.rb'
require 'osm/gpx'
require 'rexml/document'

include Apache
include REXML

r = Apache.request

dao = OSM::Dao.instance

cgi = CGI.new
nodes = cgi['nodes']

gpx = OSM::Gpx.new
nodes.scan(/[0-9]+/){ |nodeid|

  node = dao.getnode(nodeid)
  
  if node
    if node.visible
      gpx.addnode(node)
    end
  end
}

puts gpx.to_s_pretty
