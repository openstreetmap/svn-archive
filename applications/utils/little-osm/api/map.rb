# Map servers an bounding box area. All objects, that are within the bounding box
# are delivered.

# Note, that unlike the map-request of the current server, this one
# may deliver incomplete line segments. (segments, where one node is outside the bbox)

require 'mysql'

module OSM

  # return whether the bounding box array is malformed.
  def malformed_bbox bbox
    return true if bbox.size != 4
    b = bbox.collect do |x| x.to_f end
    return true if b[0]>b[2] or b[1]>b[3]
    return true unless (-180..180)===b[0] and (-90..90)===b[1] and (-180..180)===b[2] and (-90..90)===b[3]
    false
  end


  def map uri, queries, session
    bad_request "Missing required argument 'bbox'" unless queries['bbox']
    bbox = queries['bbox'].split ","
    bad_request "Malformed bounding box: #{bbox.join(",")}" if malformed_bbox bbox

    # sending ok to the client. Hope that nothing went wrong from now on
    session << OK
    session << HEADER
    # doing the request.
    db = Mysql.real_connect "localhost", "root", "", "little-osm"
    db.query "select * from data where minlat < #{bbox[3]} and minlon < #{bbox[2]} and maxlat > #{bbox[1]} and maxlon > #{bbox[0]}" do |result|
      result.each do |line|
        session << "  " << make_osm(line).to_xml << "\n"
      end
    end
    session << FOOTER
  end

end
