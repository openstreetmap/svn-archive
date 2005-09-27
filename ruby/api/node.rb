#!/usr/bin/ruby -w


require 'cgi'
load 'osm/dao.rb'
require 'osm/gpx'
require 'rexml/document'

include Apache
include REXML

r = Apache.request

dao = OSM::Dao.instance


if r.request_method == "GET"
  cgi = CGI.new
  nodeid = cgi['nodeid'].to_i

  if nodeid != 0
    gpx = OSM::Gpx.new

    node = dao.getnode(nodeid)

    if node
      if node.visible
        gpx.addnode(node)
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
    nodeid = r.args.match(/nodeid=([0-9]+)/).captures.first.to_i
    userid = dao.useruidfromcreds(r.user, r.get_basic_auth_pw)
    doc = Document.new $stdin.read

    doc.elements.each('gpx/wpt') do |pt|
      lat = pt.attributes['lat'].to_F
      lon = pt.attributes['lon'].to_f
      gpxnodeid = pt.get_text.value.to_i

      if gpxnodeid == nodeid && userid != 0
        node = dao.getnode(nodeid)
        if node
          #FIXME: need to check the node hasn't moved too much
          if dao.update_node?(nodeid, userid, lat, lon)
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
    exit HTTP_INTERNAL_SERVER_ERROR
    
  else
    if r.request_method == "DELETE"
      userid = dao.useruidfromcreds(r.user, r.get_basic_auth_pw)
      #cgi doesnt work with DELETE so extract manually:
      nodeid = r.args.match(/nodeid=([0-9]+)/).captures.first.to_i

      if userid > 0 && nodeid != 0
        node = dao.getnode(nodeid)
        if node
          if node.visible  
            if dao.delete_node?(nodeid, userid)
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

