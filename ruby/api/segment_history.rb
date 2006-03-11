#!/usr/bin/ruby -w

require 'cgi'
require 'time'
require 'date'
load 'osm/dao.rb'
load 'osm/ox.rb'
require 'rexml/document'

include Apache
include REXML

r = Apache.request

dao = OSM::Dao.instance

cgi = CGI.new
segment_id = cgi['segmentid'].to_i

from = nil
to = nil
from = Time.parse(cgi['from']) unless cgi['from'] == ''
to = Time.parse(cgi['to']) unless cgi['to'] == ''

ox = OSM::Ox.new

dao.get_segment_history(segment_id, from, to).each do |n|
  ox.add_segment(n)
end

puts ox.to_s

