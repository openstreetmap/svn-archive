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
  doc.elements.each('osm/node') do |pt|
    lat = pt.attributes['lat'].to_f
    lon = pt.attributes['lon'].to_f
    tags = pt.attributes['tags']
    tags = '' unless tags

    if userid > 0
      #FIXME: need to check the node hasn't moved too much
      new_node_uid = dao.create_node(lat, lon, userid, tags)
         
      if new_node_uid
        puts new_node_uid
      else
        exit HTTP_INTERNAL_SERVER_ERROR
      end
    else
      exit AUTH_REQUIRED
    end
  end
end
