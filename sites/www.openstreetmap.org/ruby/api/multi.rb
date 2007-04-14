#!/usr/bin/ruby -w

require 'cgi'
require 'osm/dao.rb'
require 'osm/ox.rb'
require 'rexml/document'

include Apache
include REXML

r = Apache.request
dao = OSM::Dao.instance

type = :way

if r.request_method == "GET"
  cgi = CGI.new

  multi_id = cgi['multiid'].to_i
  type = :area if cgi['type'] == 'area'

  exit BAD_REQUEST if multi_id == 0
  multi = dao.get_multi(multi_id, type)
  exit HTTP_NOT_FOUND unless multi and multi.visible

  ox = OSM::Ox.new
  ox.add_multi(multi, type)
  ox.print_http(r)
else
  user_id = dao.useridfromcreds(r.user, r.get_basic_auth_pw)
  multi_id = r.args.match(/multiid=([0-9]+)/).captures.first.to_i
  type = :area if r.args.match(/type=area/)
  if r.request_method == "PUT"

    r.setup_cgi_env
    doc = Document.new $stdin.read
    xml_multi_id = -1

    doc.elements.each("osm/#{type.to_s}") do |multi|
      xml_multi_id = multi.attributes['id'].to_i

      exit BAD_REQUEST unless xml_multi_id == multi_id

      tags = []
      multi.elements.each('tag') do |tag|
        tags << [tag.attributes['k'],tag.attributes['v']]
      end

      segs = []
      multi.elements.each('seg') do |seg|
        segs << seg.attributes['id'].to_i
      end

      if multi_id == 0 #new multi
        puts dao.new_multi(user_id, tags, segs, type)
      else
        if dao.update_multi(user_id, tags, segs, type, false, multi_id)
          exit
        else
          exit HTTP_INTERNAL_SERVER_ERROR
        end
      end

    end

  else
    if r.request_method == "DELETE"
      if user_id != 0 && multi_id != 0
        multi = dao.get_multi(multi_id, type)
        
        if multi
          exit HTTP_GONE unless multi.visible
          if dao.delete_multi(multi_id, user_id, type)
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

