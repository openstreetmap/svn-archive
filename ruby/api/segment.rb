#!/usr/bin/ruby -w

require 'cgi'
#require 'osm/dao'
load 'osm/dao.rb'
require 'osm/gpx'

include Apache

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
    puts 'put'
  else
    if r.request_method == "DELETE"
      userid = dao.useruidfromemail(r.user)
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

