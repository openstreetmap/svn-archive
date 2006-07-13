require 'data/core'
require 'data/xml'
require 'sqlite3'

require 'tools'

def to_id s
  s.to_i >> 3
end

def make_osm q
  case q[0].to_i & 0x7
  when 0
    Node.new q[4], q[5], to_id(q[0]), q[2]
  when 1
    from, to = q[3].split ','
    Segment.new(to_id(from), to_id(to), to_id(q[0]), q[2])
  when 2
    segs = q[3].split(',').collect do |x| to_id(x) end
    Way.new segs, to_id(q[0]), q[2]
  end
end

bbox = []
queries = Thread.current['uri'].query.split('&').each do |x| bbox = x.scan(/[0-9,]+/)[0].split ',' if x =~ /^bbox=/ end

ok
header

if bbox.size == 4
  db = SQLite3::Database.new 'planet.db'
  db.execute "select * from data where minlat < #{bbox[3]} and minlon < #{bbox[2]} and maxlat > #{bbox[1]} and maxlon > #{bbox[0]}" do |line|
    make_osm(line).to_xml.write
    puts
  end
end

puts '</osm>'
