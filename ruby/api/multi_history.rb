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
street_id = cgi['streetid'].to_i

from = nil
to = nil
from = Time.parse(cgi['from']) unless cgi['from'] == ''
to = Time.parse(cgi['to']) unless cgi['to'] == ''

ox = OSM::Ox.new

dao.get_street_history(street_id, from, to).each do |n|
  ox.add_street(n)
end

puts ox.to_s

