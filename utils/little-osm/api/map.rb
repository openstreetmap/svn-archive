require 'data/core'
require 'data/xml'
require 'sqlite3'

require 'tools'

include OSM

def make_osm q
  case uid_to_class q[0]
  when Node
    Node.new q[4], q[5], nil, uid_to_id(q[0]), q[2]
  when Segment
    from, to = q[3].split ','
    Segment.new uid_to_id(from), uid_to_id(to), nil, uid_to_id(q[0]), q[2]
  when Way
    segs = q[3].split(',').collect do |x| uid_to_id(x) end
    Way.new segs, nil, uid_to_id(q[0]), q[2]
  end
end

# return whether the bounding box array is malformed.
def malformed_bbox bbox
  return true if bbox.size != 4
  b = bbox.collect do |x| x.to_f end
  return true if b[0]>b[2] or b[1]>b[3]
  return true unless (-180..180)===b[0] and (-90..90)===b[1] and (-180..180)===b[2] and (-90..90)===b[3]
  false
end

queries = get_queries
bad_request "Missing required argument 'bbox'" unless queries['bbox']

bbox = queries['bbox'].split ","
bad_request "Malformed bounding box: #{bbox.join(",")}" if malformed_bbox bbox


# doing the request.
ok
header

db = SQLite3::Database.new 'planet.db'
db.execute "select * from data where minlat < #{bbox[3]} and minlon < #{bbox[2]} and maxlat > #{bbox[1]} and maxlon > #{bbox[0]}" do |line|
  puts make_osm(line).to_xml
end

puts '</osm>'
