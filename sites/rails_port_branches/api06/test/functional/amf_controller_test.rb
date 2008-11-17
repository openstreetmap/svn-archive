require File.dirname(__FILE__) + '/../test_helper'
require 'stringio'
include Potlatch

class AmfControllerTest < ActionController::TestCase
  api_fixtures

  # this should be what AMF controller returns when the bbox of a request
  # is invalid or too large.
  BOUNDARY_ERROR = [-2,"Sorry - I can't get the map for that area."]

  def test_getway
    # check a visible way
    id = current_ways(:visible_way).id
    amf_content "getway", "/1", [id]
    post :amf_read
    assert_response :success
    amf_parse_response
    assert_equal amf_result("/1")[0], id
  end

  def test_getway_invisible
    # check an invisible way
    id = current_ways(:invisible_way).id
    amf_content "getway", "/1", [id]
    post :amf_read
    assert_response :success
    amf_parse_response
    way = amf_result("/1")
    assert_equal way[0], id
    assert way[1].empty? and way[2].empty?
  end

  def test_getway_nonexistent
    # check chat a non-existent way is not returned
    amf_content "getway", "/1", [0]
    post :amf_read
    assert_response :success
    amf_parse_response
    way = amf_result("/1")
    assert_equal way[0], 0
    assert way[1].empty? and way[2].empty?
  end

  def test_whichways
    node = current_nodes(:used_node_1)
    minlon = node.lon-0.1
    minlat = node.lat-0.1
    maxlon = node.lon+0.1
    maxlat = node.lat+0.1
    amf_content "whichways", "/1", [minlon, minlat, maxlon, maxlat]
    post :amf_read
    assert_response :success
    amf_parse_response 

    # check contents of message
    map = amf_result "/1"
    assert_equal 0, map[0]
    assert_equal Array, map[1].class
    assert map[1].include?(current_ways(:used_way).id)
    assert !map[1].include?(current_ways(:invisible_way).id)
  end

  ##
  # checks that too-large a bounding box will not be served.
  def test_whichways_toobig
    bbox = [-0.1,-0.1,1.1,1.1]
    check_bboxes_are_bad [bbox] do |map|
      assert_equal BOUNDARY_ERROR, map, "AMF controller should have returned an error."
    end
  end

  ##
  # checks that an invalid bounding box will not be served. in this case
  # one with max < min latitudes.
  def test_whichways_badlat
    bboxes = [[0,0.1,0.1,0], [-0.1,80,0.1,70], [0.24,54.34,0.25,54.33]]
    check_bboxes_are_bad bboxes do |map|
      assert_equal BOUNDARY_ERROR, map, "AMF controller should have returned an error."
    end
  end

  ##
  # same as test_whichways_badlat, but for longitudes
  def test_whichways_badlon
    bboxes = [[80,-0.1,70,0.1], [54.34,0.24,54.33,0.25]]
    check_bboxes_are_bad bboxes do |map|
      assert_equal BOUNDARY_ERROR, map, "AMF controller should have returned an error."
    end
  end

  def test_whichways_deleted
    node = current_nodes(:used_node_1)
    minlon = node.lon-0.1
    minlat = node.lat-0.1
    maxlon = node.lon+0.1
    maxlat = node.lat+0.1
    amf_content "whichways_deleted", "/1", [minlon, minlat, maxlon, maxlat]
    post :amf_read
    assert_response :success
    amf_parse_response

    # check contents of message
    map = amf_result "/1"
    assert_equal 0, map[0]
    assert_equal Array, map[1].class
    assert map[1].include?(current_ways(:used_way).id)
    assert !map[1].include?(current_ways(:invisible_way).id)
  end

  def test_whichways_deleted_toobig
    bbox = [-0.1,-0.1,1.1,1.1]
    amf_content "whichways_deleted", "/1", bbox
    post :amf_read
    assert_response :success
    amf_parse_response 

    map = amf_result "/1"
    assert_equal BOUNDARY_ERROR, map, "AMF controller should have returned an error."
  end

  def test_getrelation
    id = current_relations(:visible_relation).id
    amf_content "getrelation", "/1", [id]
    post :amf_read
    assert_response :success
    amf_parse_response
    assert_equal amf_result("/1")[0], id
  end

  def test_getrelation_invisible
    id = current_relations(:invisible_relation).id
    amf_content "getrelation", "/1", [id]
    post :amf_read
    assert_response :success
    amf_parse_response
    rel = amf_result("/1")
    assert_equal rel[0], id
    assert rel[1].empty? and rel[2].empty?
  end

  def test_getrelation_nonexistent
    id = 0
    amf_content "getrelation", "/1", [id]
    post :amf_read
    assert_response :success
    amf_parse_response
    rel = amf_result("/1")
    assert_equal rel[0], id
    assert rel[1].empty? and rel[2].empty?
  end

  def test_getway_old
    # try to get the last visible version (specified by <0) (should be current version)
    latest = current_ways(:way_with_versions)
    # try to get version 1
    v1 = ways(:way_with_versions_v1)
    {latest => -1, v1 => v1.version}.each do |way, v|
      amf_content "getway_old", "/1", [way.id, v]
      post :amf_read
      assert_response :success
      amf_parse_response
      returned_way = amf_result("/1")
      assert_equal returned_way[1], way.id
      assert_equal returned_way[4], way.version
    end
  end

  def test_getway_old_nonexistent
    # try to get the last version+10 (shoudn't exist)
    latest = current_ways(:way_with_versions)
    # try to get last visible version of non-existent way
    # try to get specific version of non-existent way
    {nil => -1, nil => 1, latest => latest.version + 10}.each do |way, v|
      amf_content "getway_old", "/1", [way.nil? ? 0 : way.id, v]
      post :amf_read
      assert_response :success
      amf_parse_response
      returned_way = amf_result("/1")
      assert returned_way[2].empty?
      assert returned_way[3].empty?
      assert returned_way[4] < 0
    end
  end

  def test_getway_history
    latest = current_ways(:way_with_versions)
    amf_content "getway_history", "/1", [latest.id]
    post :amf_read
    assert_response :success
    amf_parse_response
    history = amf_result("/1")

    # ['way',wayid,history]
    assert_equal history[0], 'way'
    assert_equal history[1], latest.id
    assert_equal history[2].first[0], latest.version
    assert_equal history[2].last[0], ways(:way_with_versions_v1).version
  end

  def test_getway_history_nonexistent
    amf_content "getway_history", "/1", [0]
    post :amf_read
    assert_response :success
    amf_parse_response
    history = amf_result("/1")

    # ['way',wayid,history]
    assert_equal history[0], 'way'
    assert_equal history[1], 0
    assert history[2].empty?
  end

  def test_getnode_history
    latest = current_nodes(:node_with_versions)
    amf_content "getnode_history", "/1", [latest.id]
    post :amf_read
    assert_response :success
    amf_parse_response
    history = amf_result("/1")

    # ['node',nodeid,history]
    assert_equal history[0], 'node'
    assert_equal history[1], latest.id
    assert_equal history[2].first[0], latest.timestamp.to_i
    assert_equal history[2].last[0], nodes(:node_with_versions_v1).timestamp.to_i
  end

  def test_getnode_history_nonexistent
    amf_content "getnode_history", "/1", [0]
    post :amf_read
    assert_response :success
    amf_parse_response
    history = amf_result("/1")

    # ['node',nodeid,history]
    assert_equal history[0], 'node'
    assert_equal history[1], 0
    assert history[2].empty?
  end


  # ************************************************************
  # AMF Helper functions

  # Get the result record for the specified ID
  # It's an assertion FAIL if the record does not exist
  def amf_result ref
    assert @amf_result.has_key?("#{ref}/onResult")
    @amf_result["#{ref}/onResult"]
  end

  # Encode the AMF message to invoke "target" with parameters as
  # the passed data. The ref is used to retrieve the results.
  def amf_content(target, ref, data)
    a,b=1.divmod(256)
    c = StringIO.new()
    c.write 0.chr+0.chr   # version 0
    c.write 0.chr+0.chr   # n headers
    c.write a.chr+b.chr   # n bodies
    c.write AMF.encodestring(target)
    c.write AMF.encodestring(ref)
    c.write [-1].pack("N")
    c.write AMF.encodevalue(data)

    @request.env["RAW_POST_DATA"] = c.string
  end

  # Parses the @response object as an AMF messsage.
  # The result is a hash of message_ref => data.
  # The attribute @amf_result is initialised to this hash.
  def amf_parse_response
    if @response.body.class.to_s == 'Proc'
      res = StringIO.new()
      @response.body.call @response, res
      req = StringIO.new(res.string)
    else
      req = StringIO.new(@response.body)
    end
    req.read(2)   # version

    # parse through any headers
	headers=AMF.getint(req)					# Read number of headers
	headers.times do						# Read each header
	  name=AMF.getstring(req)				#  |
	  req.getc				   				#  | skip boolean
	  value=AMF.getvalue(req)				#  |
	end

    # parse through responses
    results = {}
    bodies=AMF.getint(req)					# Read number of bodies
	bodies.times do							# Read each body
	  message=AMF.getstring(req)			#  | get message name
	  index=AMF.getstring(req)				#  | get index in response sequence
	  bytes=AMF.getlong(req)				#  | get total size in bytes
	  args=AMF.getvalue(req)				#  | get response (probably an array)
      results[message] = args
    end
    @amf_result = results
    results
  end

  ##
  # given an array of bounding boxes (each an array of 4 floats), call the
  # AMF "whichways" controller for each and pass the result back to the
  # caller's block for assertion testing.
  def check_bboxes_are_bad(bboxes)
    bboxes.each do |bbox|
      amf_content "whichways", "/1", bbox
      post :amf_read
      assert_response :success
      amf_parse_response

      # pass the response back to the caller's block to be tested
      # against what the caller expected.
      map = amf_result "/1"
      yield map
    end
  end
end
