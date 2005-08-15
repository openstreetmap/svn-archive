#!/usr/bin/ruby -w


require 'cgi'
#require 'osm/dao.rb'
load 'osm/dao.rb'
require 'osm/gpx'

include Apache

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
    puts 'put'
  else
    if r.request_method == "DELETE"
      userid = dao.useruidfromemail(r.user)
      #cgi doesnt work with DELETE so extract manually:
      nodeid = r.args.match(/nodeid=([0-9]+)/).captures.first.to_i

      if userid != 0 && nodeid != 0
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

