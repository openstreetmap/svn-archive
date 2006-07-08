#!/usr/bin/ruby -w

require 'cgi'
require 'osm/dao.rb'
require 'osm/ox.rb'
require 'rexml/document'

include Apache
include REXML


r = Apache.request
r.content_encoding='text/xml'
dao = OSM::Dao.instance

if r.request_method == "GET"
  
  cgi = CGI.new
  segmentid = cgi['segmentid'].to_i
 

  if segmentid != 0
    ox = OSM::Ox.new

    segment = dao.getsegment(segmentid)

    if segment
      if segment.visible && segment.node_a.visible && segment.node_b.visible
        ox.add_segment(segment)
        ox.print_http(r)
      else
        exit HTTP_GONE
      end
    else
      exit HTTP_NOT_FOUND
    end
  end

else

  if r.request_method == "PUT"

    r.setup_cgi_env
    segmentid = r.args.match(/segmentid=([0-9]+)/).captures.first.to_i
    userid = dao.useridfromcreds(r.user, r.get_basic_auth_pw)
    doc = Document.new $stdin.read
    gpxsegmentid = -1

    doc.elements.each('osm/segment') do |seg|
      gpxsegmentid = seg.attributes['id'].to_i
      
      exit BAD_REQUEST unless gpxsegmentid == segmentid

      node_a_id = seg.attributes['from'].to_i
      node_b_id = seg.attributes['to'].to_i

      tags = []
      seg.elements.each('tag') do |tag|
        tags << [tag.attributes['k'],tag.attributes['v']]
      end
      
      tags = tags.collect { |k,v| "#{k}=#{v}" }.join(';')

      exit HTTP_NOT_FOUND unless dao.getnode(node_a_id).visible 
      exit HTTP_NOT_FOUND unless dao.getnode(node_b_id).visible
      #exit BAD_REQUEST unless tags
      tags = '' unless tags

      if segmentid == 0
        new_seg_id = dao.create_segment(node_a_id, node_b_id, userid, tags).to_i

        if new_seg_id && new_seg_id > 0
          puts new_seg_id
        else
          exit HTTP_INTERNAL_SERVER_ERROR
        end
      else
        exit HTTP_NOT_FOUND unless dao.getsegment(gpxsegmentid).visible
        if dao.update_segment?(segmentid, userid, node_a_id, node_b_id, tags)
          exit
        else
          exit HTTP_INTERNAL_SERVER_ERROR
        end
      end

    end

  else
    if r.request_method == "DELETE"
      userid = dao.useridfromcreds(r.user, r.get_basic_auth_pw)
      #cgi doesnt work with DELETE so extract manually:
      segmentid = r.args.match(/segmentid=([0-9]+)/).captures.first.to_i

      if userid != 0 && segmentid != 0
        segment = dao.getsegment(segmentid)
        if segment
          if segment.visible && segment.node_a.visible && segment.node_b.visible 
            if dao.delete_segment?(segmentid, userid)
              exit
            else
              exit HTTP_INTERNAL_SERVER_ERROR
            end
          else
            exit HTTP_GONE
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

