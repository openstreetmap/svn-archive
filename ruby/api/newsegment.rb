#!/usr/bin/ruby -w

require 'cgi'
load 'osm/dao.rb'
require 'rexml/document'

include Apache
include REXML


r = Apache.request

dao = OSM::Dao.instance

if r.request_method == "PUT"
  r.setup_cgi_env
  userid = dao.useruidfromcreds(r.user, r.get_basic_auth_pw)
  doc = Document.new $stdin.read

  doc.elements.each('osm/segment') do |seg|
    node_a_uid = seg.attributes['from'].to_i
    node_b_uid = seg.attributes['to'].to_i
    tags = seg.attributes['tags']
    tags = '' unless tags
    
    n = dao.getnode(node_a_uid)
    exit HTTP_NOT_FOUND if n.nil? || n.visible
    n = dao.getnode(node_b_uid)
    exit HTTP_NOT_FOUND if n.nil? || n.visible

    new_seg_id = dao.create_segment(node_a_uid, node_b_uid, userid, tags).to_i

    if new_seg_id && new_seg_id > 0
      print new_seg_id
    else
      exit HTTP_INTERNAL_SERVER_ERROR
    end
  end

end

