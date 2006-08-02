module OSM

  OK = "HTTP/1.1 200/OK\r\nServer: little-osm\r\nContent-type: text/plain\r\n\r\n"
  HEADER = "<?xml version='1.0' encoding='UTF-8'?>\n<osm version='0.3' generator='little-osm'>"
  FOOTER = '</osm>'

  def bad_request reason = "Bad Request"
    print "HTTP/1.1 400/#{reason}\r\n\r\n"
    throw :little_osm_done
  end

  # shortcut to deliver the return of yield to the user. The return will be cached to send an error
  # in case of failure.
  def cached_deliver session
    osm = yield
    session << OK
    session << HEADER
    session << osm.to_xml
    session << FOOTER
  end

  # Create and return an osm object out of the database query array
  def make_osm q
    case uid_to_class(q[0]).to_s
    when "OSM::Node"
      Node.new q[4], q[5], nil, uid_to_id(q[0]), q[2]
    when "OSM::Segment"
      from, to = q[3].split ','
      Segment.new uid_to_id(from), uid_to_id(to), nil, uid_to_id(q[0]), q[2]
    when "OSM::Way"
      segs = q[3].split(',').collect do |x| uid_to_id(x) end
      Way.new segs, nil, uid_to_id(q[0]), q[2]
    end
  end

end
