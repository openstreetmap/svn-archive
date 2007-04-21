$: << ".."
require 'data/core'
require 'test/unit'
require 'test/unit/assertions'

include OSM

class OsmDataTest < Test::Unit::TestCase

  def test_uid
    n = Node.new 1,2,123
    s = Segment.new Node.new(1,2),Node.new(3,4), 1
    w = Way.new 1
    assert_not_equal n.to_uid, s.to_uid
    assert_not_equal n.to_uid, w.to_uid
    assert_not_equal s.to_uid, w.to_uid
  end

  def test_idclass_to_uid
    assert_not_equal idclass_to_uid(23,Node), idclass_to_uid(23,Segment)
    assert_not_equal idclass_to_uid(23,Node), idclass_to_uid(42,Node)
    assert_not_equal idclass_to_uid(23,Node), idclass_to_uid(23,Way)
    assert_not_equal idclass_to_uid(23,Segment), idclass_to_uid(23,Way)
  end

  def test_uid_to_class
    assert_equal Node, uid_to_class(0)
    assert_equal Segment, uid_to_class(9)
    assert_equal Way, uid_to_class("18")
    assert_equal nil, uid_to_class(7)
  end

  def test_osm_primitive
    osm = OsmPrimitive.new nil,1,Time.now

    assert !osm.respond_to?(:timestamp=)

    assert_nil osm['key']
    osm['key'] = 'value'
    assert_equal 'value', osm['key']
    osm['key'] = nil
    assert_nil osm['key']
    assert_not_nil osm.timestamp
    assert osm.timestamp <= Time.now
    assert_equal 1,osm.to_i
  end

  def test_node
    n = Node.new 1,2, {"foo"=>"bar"}, 123
    assert_equal 1,n.lat
    assert_equal 2,n.lon
    assert_equal 123, n.to_i
    assert_equal [1,2,1,2], n.bbox
    assert_equal nil, n.timestamp
    assert_equal "bar", n["foo"]
    n = Node.new :lat => 1, :lon => 2
    assert_equal 1, n.lat
  end

  def test_segment
    s = Segment.new Node.new(3,4), Node.new(1,2)
    assert_equal 4, s.from.lon
    assert_equal 1, s.to.lat
    assert_equal [1,2,3,4], s.bbox
    s = Segment.new :from=>Node.new(3,4), :to=>Node.new(1,2), :timestamp=>"null"
    assert_equal [1,2,3,4], s.bbox
    assert_equal nil, s.timestamp
  end

  def test_way
    w = Way.new [], nil, 123
    assert_equal 123, w.to_i
    assert_equal 0, w.segments.size
    w = Way.new [Segment.new(Node.new(42,-2), Node.new(3,4)), Segment.new(Node.new(3,4), Node.new(5,6))]
    assert_equal 2, w.segments.size
    assert_equal 3, w.segments[0].to.lat
    assert_equal [3,-2,42,6], w.bbox
    w = Way.new [Segment.new(Node.new(-1,-1), Node.new(-1,-1))]
    assert_equal [-1,-1,-1,-1], w.bbox
    w = Way.new :segments=>[]
    assert_equal [], w.segments
  end
end

