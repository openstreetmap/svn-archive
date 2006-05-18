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
node_id = cgi['nodeid'].to_i

from = nil
to = nil
from = Time.parse(cgi['from']) unless cgi['from'] == ''
to = Time.parse(cgi['to']) unless cgi['to'] == ''

ox = OSM::Ox.new

dao.get_node_history(node_id, from, to).each do |n|
  ox.add_node(n)
end

ox.print_http(r)

