#!/usr/bin/ruby

# script that assumes an empty database and tests the correctness of the API.
# It is intended to run on a testserver.

# WARNING: The database may be screwed up. DO NOT RUN THIS SCRIPT ON THE PRODUCTION SERVER!

# This script just a start, more tests need to be added

# The test case description resides at
# http://wiki.openstreetmap.org/index.php/Testserver_rb
# If you add something, fix the wiki-page or you will be hunted down by Imi ;)

require "open-uri"
require "net/http"
require "rexml/document"

# get little-osm's data structure
$: << File.dirname(__FILE__)+"/../utils/osm-data/lib"
require "osm/data"
require "osm/rexml"

include OSM

# Grab stuff from osm server.
# An array with all osm objects in the result is returned or nil in case of an http-error.
def do_get path
  puts "GET "+path if $-v
  resp_io = open "http://localhost/api/0.3/"+path, :http_basic_authentication=>["foo@bar.baz", "foobar"]
  resp = resp_io.read
  puts "got response: 200\n"+resp if $-v
  result = []
  REXML::Document.new(resp).root.each_element do |e| result << OsmPrimitive.from_rexml(e) end
  if result.size == 1
    result[0]
  else
    result
  end
end


# Writes the given string to the given path and return the resulting integer (PUT always results in
# an single integer or nothing)
# if the server responses with a single integer. Return nil in case of an http-error.
def do_put path, body
  puts "PUT "+path+" with "+body if $-v
  put = Net::HTTP::Put.new "/api/0.3/"+path
  put.basic_auth "foo@bar.baz", "foobar"
  put.body = body
  resp = Net::HTTP.start "localhost" do |http| http.request put end
  puts "got response #{resp.code}:\n"+resp.body if $-v
  raise "PUT did not deliver a number." unless resp.body.chomp =~ /^[0-9]+$/
  resp.body.chomp.to_i
end


# Executes a GET test to the server.
def GET url_path, errmsg
  raise errmsg unless yield do_get(eval('%{'+url_path+'}'))
end

# Executes a PUT test to the server
def PUT url_path, body, errmsg
  raise errmsg unless yield do_put(eval('%{'+url_path+'}'), eval('%{'+body+'}'))
end


########################################################################################
# Starting test cases
########################################################################################

# creating a new node
PUT 'node/0', '<osm><node id="0" lat="23" lon="42"/></osm>', 'Could not create a node' do |r|
  $node1_id = r
  r != 0
end

# creating another node
PUT 'node/0', '<osm><node id="0" lat="5" lon="11"/></osm>', 'Could not create a second node' do |r|
  $node2_id = r
  r != 0 and r != $node1_id
end

# creating a line segment between both nodes
PUT 'segment/0', '<osm><segment id="0" from="#{$node1_id}" to="#{$node2_id}"/></osm>', 'Could not create a segment' do |r|
  $segment_id = r
  r != 0
end

# skip
# try to remove first node
#DELETE_ERROR 'node/#{node1_id}', '', 'Node in use should not be deletable'

# create a way out of the segment
PUT 'way/0', '<osm><way id="0"><seg id="#{$segment_id}"/></way></osm>', 'Could not create a way' do |r|
  $way_id = r
  r != 0
end

# get both nodes by id
GET 'nodes?nodes=#{$node1_id},#{$node2_id}', 'Could not retrieve both nodes together' do |r|
  $node1, $node2 = r
  $node1 and $node2 and $node1.kind_of? OsmPrimitive and $node2.kind_of? OsmPrimitive and $node1.to_i == $node1_id and $node2.to_i == $node2_id
end

# get the first node by id
GET 'node/#{$node1_id}', 'Could not get node by id' do |r|
  $node1.to_i == r.to_i
end

# get the segment by id
GET 'segment/#{$segment_id}', 'Could not get segment by id' do |r|
  $segment = r
  $segment.to_i == $segment_id
end

# get the way by id
GET 'way/#{$way_id}', 'Could not get the way by id' do |r|
  $way = r
  $way.to_i == $way_id
end

# do a map request covering both nodes in area
GET 'map?bbox=10,4,43,24', 'map request to retrieve all structures failed' do |r|
  [$node1_id,$node2_id,$segment_id,$way_id]-r.collect{|x| x.to_i} == []
end

# do a map request covering only one node
GET 'map?bbox=12,6,43,24', 'map request to retrieve at least one covered node failed' do |r|
  [$node1_id]-r.collect{|x| x.to_i} == []
end

# do a map request completly outside both nodes
GET 'map?bbox=70,70,71,71', 'map request to not retrieve our created structures failed' do |r|
  [$node1_id,$node2_id,$segment_id,$way_id]-r.collect{|x| x.to_i} == [$node1_id,$node2_id,$segment_id,$way_id]
end

# remove the segment
# try to get the way
# remove the first node
# remove the second node
# map request covering both old node locations
# try to get the first node
# get the history of the first node