#!/usr/bin/ruby -w

require 'cgi'
require 'time'
require 'date'
require 'osm/dao.rb'
require 'osm/ox.rb'
require 'rexml/document'

include Apache
include REXML

r = Apache.request

dao = OSM::Dao.instance

cgi = CGI.new
puts cgi['nodeid']
node_id = cgi['nodeid'].split(",")


from = nil
to = nil
from = Time.parse(cgi['from']) unless cgi['from'] == ''
to = Time.parse(cgi['to']) unless cgi['to'] == ''

ox = OSM::Ox.new

node_id.each do |id|
  dao.get_node_history(id, from, to).each do |n|
    ox.add_node(n)
  end
end
  

ox.print_http(r)

