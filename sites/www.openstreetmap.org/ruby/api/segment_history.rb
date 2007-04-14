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
segment_id = cgi['segmentid'].split(",")

from = nil
to = nil
from = Time.parse(cgi['from']) unless cgi['from'] == ''
to = Time.parse(cgi['to']) unless cgi['to'] == ''

ox = OSM::Ox.new

segment_id.each do |id|
  dao.get_segment_history(id.to_i, from, to).each do |n|
    ox.add_segment(n)
  end
end

ox.print_http(r)

