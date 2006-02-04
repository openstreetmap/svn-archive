#!/usr/bin/ruby -w

require 'cgi'
require 'osm/dao.rb'
require 'osm/ox.rb'
require 'rexml/document'

include Apache
include REXML

r = Apache.request

dao = OSM::Dao.instance


if r.request_method == "GET"
  cgi = CGI.new
  nodeid = cgi['nodeid'].to_i

  if nodeid != 0
    ox = OSM::Ox.new

    node = dao.getnode(nodeid)

    if node
      if node.visible
        ox.add_node(node)
        puts ox.to_s_pretty
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
    userid = dao.useridfromcreds(r.user, r.get_basic_auth_pw)
    doc = Document.new $stdin.read

    doc.elements.each('osm/node') do |pt|
      lat = pt.attributes['lat'].to_f
      lon = pt.attributes['lon'].to_f
      xmlnodeid = pt.attributes['uid'].to_i
      tags = pt.attributes['tags']
#      exit BAD_REQUEST unless tags
      tags = '' unless tags
      if xmlnodeid == nodeid && userid != 0
        node = dao.getnode(nodeid)
        if node
          #FIXME: need to check the node hasn't moved too much
          if dao.update_node?(nodeid, userid, lat, lon, tags)
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
      userid = dao.useridfromcreds(r.user, r.get_basic_auth_pw)
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

