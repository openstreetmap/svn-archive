#!/usr/bin/ruby -w

require 'cgi'
require 'osm/dao.rb'

include Apache

r = Apache.request
dao = OSM::Dao.instance


cgi = CGI.new
id = cgi['id'].to_i
type = cgi['type']

lat = 0.0
lon = 0.0
zoom = 0

p '<html><body>Finding a better way...</body></html>'

if type == 'way'
  res = dao.call_sql { " select latitude, longitude from (select * from current_way_segments where id = #{id} limit 1) as a join current_segments join current_nodes where a.segment_id = current_segments.id and current_segments.node_a = current_nodes.id;" }
  res.each_hash do |row|
    lat = row['latitude'].to_f
    lon = row['longitude'].to_f
    zoom = 14
  end
end

r.headers_out["Location"] = "http://www.openstreetmap.org/index.html?lat=#{lat}&lon=#{lon}&zoom=#{zoom}"
exit Apache::REDIRECT
