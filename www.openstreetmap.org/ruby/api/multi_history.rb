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
multi_id = cgi['multiid'].split(",")

from = nil
to = nil
from = Time.parse(cgi['from']) unless cgi['from'] == ''
to = Time.parse(cgi['to']) unless cgi['to'] == ''

type = :way
type = :area if cgi['type'] == 'area'

ox = OSM::Ox.new

multi_id.each do |id|
  dao.get_multi_history(id.to_i, type, from, to).each do |n|
    ox.add_multi(n, type)
  end
end

ox.print_http(r)

