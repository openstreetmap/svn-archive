#!/usr/bin/ruby -w

require 'cgi'
load 'osm/dao.rb'
load 'osm/ox.rb'
require 'rexml/document'

include Apache
include REXML

r = Apache.request

dao = OSM::Dao.instance

if r.request_method == "GET"

  cgi = CGI.new
  street_id = cgi['streetid'].to_i

  if street_id != 0
    ox = OSM::Ox.new

    street = dao.get_street(street_id)

    if street
      exit HTTP_NOT_FOUND unless street.visible
      ox.add_street(street)
      puts ox.to_s
    end
  end
else
  user_id = dao.useridfromcreds(r.user, r.get_basic_auth_pw)
  street_id = r.args.match(/streetid=([0-9]+)/).captures.first.to_i
  if r.request_method == "PUT"

    r.setup_cgi_env
    doc = Document.new $stdin.read
    xml_street_id = -1

    doc.elements.each('osm/street') do |street|
      xml_street_id = street.attributes['id'].to_i

      exit BAD_REQUEST unless xml_street_id == street_id

      tags = []
      street.elements.each('tag') do |tag|
        tags << [tag.attributes['k'],tag.attributes['v']]
      end

      segs = []
      street.elements.each('seg') do |seg|
        segs << seg.attributes['id'].to_i
      end

      if street_id == 0 #new street
        puts dao.new_street(user_id, tags, segs)
      else
        if dao.update_street(user_id, tags, segs, false, street_id)
          exit
        else
          exit HTTP_INTERNAL_SERVER_ERROR
        end
      end

    end

  else
    if r.request_method == "DELETE"
      if user_id != 0 && street_id != 0
        street = dao.get_street(street_id)
        
        if street
          exit HTTP_GONE unless street.visible
          if dao.delete_street(street_id, user_id)
            exit
          else
            exit HTTP_INTERNAL_SERVER_ERROR
          end
        else
          exit HTTP_NOT_FOUND
        end
      else
        exit BAD_REQUEST
      end
    end

  end
end

