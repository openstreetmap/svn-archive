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

if r.request_method == "GET"
  
  cgi = CGI.new
  segmentid = cgi['segmentid'].to_i
 

  if segmentid != 0
    gpx = OSM::Gpx.new

    segment = dao.getsegment(segmentid)

    if segment
      if segment.visible && segment.node_a.visible && segment.node_b.visible
        gpx.addline(segment.uid, segment.node_a, segment.node_b)
        puts gpx.to_s_pretty
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
    userid = dao.useruidfromcreds(r.user, r.get_basic_auth_pw)
    doc = Document.new $stdin.read
    gpxsegmentid = -1

    doc.elements.each('gpx/trk/name') do |n|
      gpxsegmentid = n.get_text.value.to_i
    end

    exit BAD_REQUEST unless gpxsegmentid == segmentid
    exit HTTP_NOT_FOUND unless dao.getsegment(gpxsegmentid).visible == true

    points = Array.new

    count = 0

    doc.elements.each('gpx/trk/trkseg/trkpt') do |p|
      points.push p.attributes['lat'].to_f
      points.push p.attributes['lon'].to_f
      p.elements.each('name') do |n|
        count += 1
        exit HTTP_NOT_FOUND unless dao.getnode(n.get_text.value.to_i).visible == true 
        points.push n.get_text.value.to_i
      end
    end

    exit BAD_REQUEST unless count == 2

    if dao.update_segment?(segmentid, userid, points[2], points[5])
      exit
    else
      exit HTTP_INTERNAL_SERVER_ERROR
    end

  else
    if r.request_method == "DELETE"
      userid = dao.useruidfromcreds(r.user, r.get_basic_auth_pw)
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

