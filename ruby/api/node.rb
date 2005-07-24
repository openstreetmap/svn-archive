#!/usr/bin/ruby -w


require 'cgi'
require 'osm/dao'
require 'osm/gpx'

include Apache

#puts ENV['REMOTE_USER']

r = Apache.request
cgi = CGI.new

nodeid = cgi['nodeid'].to_i


if nodeid != 0
  dao = OSM::Dao.new
  gpx = OSM::Gpx.new

  node = dao.getnode(nodeid)

  gpx.addnode(node)
  puts gpx
end



#if token != 'ERROR'
#  login = Element.new 'login', {'a' => 'b'}
#  puts login
#  puts 'hello'
#else
#  puts 'hrm'
#  exit(Apache::HTTP_UNAUTHORIZED)
#end




#el1 = Element.new "myelement" 
#el1.text = "Hello world!" 
# -> <myelement>Hello world!</myelement> 
#el1.add_text "Hello dolly" 
# -> <myelement>Hello world!Hello dolly</element> 
#el1.add Text.new("Goodbye") 
# -> <myelement>Hello world!Hello dollyGoodbye</element> 
#el1 << Text.new(" cruel world") 
# -> <myelement>Hello world!Hello dollyGoodbye cruel world</element>

#doc.add el1
#puts doc

