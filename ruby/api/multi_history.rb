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
multi_id = cgi['multiid'].to_i

from = nil
to = nil
from = Time.parse(cgi['from']) unless cgi['from'] == ''
to = Time.parse(cgi['to']) unless cgi['to'] == ''

type = :way
type = :area if cgi['type'] == 'area'

ox = OSM::Ox.new

dao.get_multi_history(multi_id, type, from, to).each do |n|
  ox.add_multi(n, type)
end

puts ox.print_http(r)

