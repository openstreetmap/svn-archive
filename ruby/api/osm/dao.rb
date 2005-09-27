#!/usr/bin/ruby -w

require 'cgi'
#require 'osm/dao'
load 'osm/dao.rb'
require 'osm/gpx'
require 'rexml/document'

include Apache
include REXML


r = Apache.request

dao = OSM::Dao.instance

if r.request_method == "PUT"
  r.setup_cgi_env
  userid = dao.useruidfromcreds(r.user, r.get_basic_auth_pw)
  doc = Document.new $stdin.read

  points = Array.new
  count = 0

  doc.elements.each('gpx/trk/trkseg/trkpt') do |p|
    p.elements.each('name') do |n|
      count += 1
      exit HTTP_NOT_FOUND unless dao.getnode(n.get_text.value.to_i).visible == true 
      points.push n.get_text.value.to_i
    end
  end

  exit BAD_REQUEST unless count == 2

  new_seg_id = dao.create_segment(points[0], points[1], userid).to_i

  if new_seg_id && new_seg_id > 0
    print new_seg_id
  else
    exit HTTP_INTERNAL_SERVER_ERROR
  end

end

